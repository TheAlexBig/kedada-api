alter table urls
    add column event_id uuid references events(id);

with ranked_links as (
    select
        u.id as url_id,
        e.id as event_id,
        row_number() over (partition by u.id order by e.created_at, e.id) as occurrence
    from urls u
    join events e on e.site_url = u.id or e.reference_url = u.id
)
update urls u
set event_id = ranked_links.event_id
from ranked_links
where ranked_links.url_id = u.id
  and ranked_links.occurrence = 1;

insert into urls (url, description, owner_id, kind, is_deleted, deleted_at, event_id)
select u.url, u.description, u.owner_id, u.kind, u.is_deleted, u.deleted_at, ranked_links.event_id
from urls u
join (
    select
        legacy_url.id as url_id,
        e.id as event_id,
        row_number() over (partition by legacy_url.id order by e.created_at, e.id) as occurrence
    from urls legacy_url
    join events e on e.site_url = legacy_url.id or e.reference_url = legacy_url.id
) ranked_links on ranked_links.url_id = u.id
where ranked_links.occurrence > 1;

alter table events
    drop column site_url,
    drop column reference_url;

create index idx_urls_event_id on urls(event_id);
