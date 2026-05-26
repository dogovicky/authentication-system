alter table email_verification_tokens
add column issued_at timestamp,
add column ip_address varchar(100);