alter table users
alter column updated_at drop not null;


ALTER TABLE outbox_events ADD COLUMN last_error TEXT;
ALTER TABLE user_sessions ALTER COLUMN device_info TYPE TEXT;


