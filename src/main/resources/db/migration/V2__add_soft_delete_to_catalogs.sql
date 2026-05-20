alter table urls
    add column is_deleted boolean not null default false,
    add column deleted_at timestamptz;

alter table categories
    add column is_deleted boolean not null default false,
    add column deleted_at timestamptz;

create index idx_urls_is_deleted on urls(is_deleted);
create index idx_categories_is_deleted on categories(is_deleted);
