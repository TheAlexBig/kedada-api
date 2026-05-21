create table users (
    id uuid primary key default gen_random_uuid(),
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    name varchar(100) not null,
    role varchar(30) not null default 'USER',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create trigger trg_users_updated_at
before update on users
for each row execute function set_updated_at();

insert into users (id, email, password_hash, name, role)
values (
    '00000000-0000-0000-0000-000000000001',
    'system@kedada.local',
    '{noop}disabled',
    'System',
    'SYSTEM'
);

alter table events
    add column owner_id uuid not null default '00000000-0000-0000-0000-000000000001';

alter table events
    alter column owner_id drop default;

create index idx_events_owner_id on events(owner_id);
create index idx_users_email on users(email);
