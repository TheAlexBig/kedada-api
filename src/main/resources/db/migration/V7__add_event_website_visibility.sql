alter table events
    add column visible_on_website boolean not null default true;

create index idx_events_visible_on_website on events(visible_on_website);
