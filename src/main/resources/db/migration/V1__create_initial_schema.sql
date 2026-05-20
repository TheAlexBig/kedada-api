create extension if not exists pgcrypto;

create table urls (
    id uuid primary key default gen_random_uuid(),
    url varchar not null,
    description varchar(100),
    owner_id uuid not null,
    kind varchar(20) not null
);

create table categories (
    id uuid primary key default gen_random_uuid(),
    name varchar not null,
    owner_id uuid not null,
    type varchar[]
);

create table events (
    id uuid primary key default gen_random_uuid(),
    title varchar(100) not null,
    description text,
    priority integer not null default 1,
    thumbnail uuid,
    price numeric(10,2),
    site_url uuid references urls(id),
    reference_url uuid references urls(id),
    type uuid not null references categories(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    is_deleted boolean not null default false,
    deleted_at timestamptz,
    search_vector tsvector,
    constraint chk_events_priority check (priority >= 1),
    constraint chk_events_price check (price is null or price >= 0)
);

create table schedules (
    id uuid primary key default gen_random_uuid(),
    event_id uuid references events(id),
    start_date timestamptz not null,
    end_date timestamptz,
    owner_id uuid not null,
    constraint chk_schedules_date_range check (end_date is null or end_date > start_date)
);

create table event_metric_daily (
    event_id uuid not null references events(id),
    day date not null,
    views bigint not null default 0,
    shares bigint not null default 0,
    owner_id uuid not null,
    primary key (event_id, day)
);

create or replace function set_updated_at()
returns trigger as $$
begin
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;

create trigger trg_events_updated_at
before update on events
for each row execute function set_updated_at();

create or replace function set_event_search_vector()
returns trigger as $$
begin
    new.search_vector =
        setweight(to_tsvector('spanish', coalesce(new.title, '')), 'A') ||
        setweight(to_tsvector('spanish', coalesce(new.description, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(new.title, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(new.description, '')), 'B');
    return new;
end;
$$ language plpgsql;

create trigger trg_events_search_vector
before insert or update of title, description on events
for each row execute function set_event_search_vector();

create index idx_events_type on events(type);
create index idx_events_created_at on events(created_at);
create index idx_events_priority on events(priority);
create index idx_events_is_deleted on events(is_deleted);
create index idx_events_search_vector on events using gin(search_vector);
create index idx_event_metric_daily_event_day on event_metric_daily(event_id, day);
create index idx_schedules_event_id on schedules(event_id);
create index idx_schedules_start_date on schedules(start_date);
