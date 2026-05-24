create table event_categories (
    event_id uuid not null references events(id) on delete cascade,
    category_id uuid not null references categories(id),
    primary key (event_id, category_id)
);

insert into event_categories (event_id, category_id)
select id, type
from events;

alter table events drop column type;

drop index if exists idx_events_type;
create index idx_event_categories_category_id on event_categories(category_id);
