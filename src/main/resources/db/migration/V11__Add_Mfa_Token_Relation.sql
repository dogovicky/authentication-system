create table mfa_tokens (
    id uuid primary key,
    user_id uuid not null,
    otp varchar(50) not null,
    device_info varchar(255),
    ip_address varchar(255),
    mfa_status varchar(50),
    expires_at timestamp,
    created_at timestamp
);

create index idx_mfa_tokens_user_id on mfa_tokens(user_id);
create index idx_mfa_tokens_otp on mfa_tokens(otp);
create index idx_mfa_tokens_expires_at on mfa_tokens(expires_at);
create index idx_mfa_device_info on mfa_tokens(ip_address, device_info);