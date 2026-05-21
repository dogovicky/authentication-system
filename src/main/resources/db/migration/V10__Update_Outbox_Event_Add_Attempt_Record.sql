alter table outbox_events drop column topic,
add column attempts int default 0;
