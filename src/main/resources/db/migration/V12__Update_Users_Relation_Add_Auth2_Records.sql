-- Set password_hash to null
alter table users
alter column password_hash drop not null,
add column provider_id varchar(255),
add column provider varchar(50);