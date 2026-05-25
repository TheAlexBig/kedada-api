create table media_assets (
    id uuid primary key default gen_random_uuid(),
    object_key varchar not null unique,
    original_filename varchar(255) not null,
    content_type varchar(100) not null,
    size_bytes bigint not null,
    bucket varchar(100) not null,
    owner_id uuid not null references users(id),
    created_at timestamptz not null default now(),
    constraint chk_media_assets_size check (size_bytes > 0)
);

create index idx_media_assets_owner_id on media_assets(owner_id);

-- Previous thumbnail UUIDs were placeholders without a stored object.
update events set thumbnail = null where thumbnail is not null;

alter table events
    add constraint fk_events_thumbnail_media
    foreign key (thumbnail) references media_assets(id);
