-- User Session Management Table
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    refresh_token VARCHAR(500) UNIQUE NOT NULL,
    device_info VARCHAR(500),
    ip_address VARCHAR(45),
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE NOT NULL,
    last_used_at TIMESTAMP,
    revoked_at TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_refresh_token ON user_sessions(refresh_token);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);
CREATE INDEX idx_user_sessions_revoked ON user_sessions(is_revoked);