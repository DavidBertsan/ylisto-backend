-- Initial schema, generated from the JPA entities and cleaned up by hand:
-- readable constraint names and indexes on the foreign keys we filter by.
-- Never edit this file once applied: add a new V2__... migration instead.

create table business (
    id   uuid         not null,
    name varchar(255) not null,
    type varchar(255),
    primary key (id)
);

create table users (
    id          uuid         not null,
    business_id uuid         not null,
    username    varchar(255) not null,
    password    varchar(255) not null,
    name        varchar(255) not null,
    mobile      varchar(255) not null,
    role        varchar(255) not null check (role in ('EMPLOYEE', 'ADMIN')),
    image_url   varchar(255),
    active      boolean      not null,
    primary key (id),
    constraint uk_users_username unique (username),
    constraint fk_users_business foreign key (business_id) references business
);

create table clocking (
    id        uuid                        not null,
    user_id   uuid                        not null,
    clock_in  timestamp(6) with time zone not null,
    clock_out timestamp(6) with time zone,
    primary key (id),
    constraint fk_clocking_user foreign key (user_id) references users
);

create table shift (
    id        uuid                        not null,
    user_id   uuid                        not null,
    starts_at timestamp(6) with time zone not null,
    ends_at   timestamp(6) with time zone not null,
    primary key (id),
    constraint fk_shift_user foreign key (user_id) references users
);

create table product (
    id          uuid         not null,
    business_id uuid         not null,
    name        varchar(255) not null,
    image_url   varchar(255),
    unit        varchar(255) not null check (unit in ('KILOGRAM', 'LITER', 'PIECE')),
    primary key (id),
    constraint fk_product_business foreign key (business_id) references business
);

create table product_variant (
    id         uuid           not null,
    product_id uuid           not null,
    label      varchar(255)   not null,
    price      numeric(10, 2) not null,
    quantity   numeric(10, 3) not null,
    version    bigint         not null,
    primary key (id),
    constraint fk_product_variant_product foreign key (product_id) references product
);

create table stock_movement (
    id              uuid                        not null,
    variant_id      uuid                        not null,
    user_id         uuid                        not null,
    type            varchar(255)                not null check (type in ('SALE', 'RESTOCK', 'ADJUSTMENT')),
    quantity_change numeric(10, 3)              not null,
    occurred_at     timestamp(6) with time zone not null,
    primary key (id),
    constraint fk_stock_movement_variant foreign key (variant_id) references product_variant,
    constraint fk_stock_movement_user foreign key (user_id) references users
);

create table conversation (
    id                 uuid                        not null,
    business_id        uuid                        not null,
    participant_one_id uuid                        not null,
    participant_two_id uuid                        not null,
    created_at         timestamp(6) with time zone not null,
    primary key (id),
    -- participants are stored ordered (smaller id first), so this keeps one conversation per pair
    constraint uk_conversation_participants unique (participant_one_id, participant_two_id),
    constraint fk_conversation_business foreign key (business_id) references business,
    constraint fk_conversation_participant_one foreign key (participant_one_id) references users,
    constraint fk_conversation_participant_two foreign key (participant_two_id) references users
);

create table message (
    id              uuid                        not null,
    conversation_id uuid                        not null,
    sender_id       uuid                        not null,
    content         varchar(2000)               not null,
    sent_at         timestamp(6) with time zone not null,
    primary key (id),
    constraint fk_message_conversation foreign key (conversation_id) references conversation,
    constraint fk_message_sender foreign key (sender_id) references users
);

-- Postgres does not index foreign keys on its own: these back the queries the services run.
create index idx_users_business on users (business_id);
create index idx_clocking_user on clocking (user_id);
create index idx_shift_user_starts_at on shift (user_id, starts_at);
create index idx_product_business on product (business_id);
create index idx_product_variant_product on product_variant (product_id);
create index idx_stock_movement_variant on stock_movement (variant_id);
create index idx_conversation_participant_two on conversation (participant_two_id);  -- participant_one is covered by the unique
create index idx_message_conversation_sent_at on message (conversation_id, sent_at);
