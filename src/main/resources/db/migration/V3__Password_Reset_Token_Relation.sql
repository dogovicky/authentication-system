-- Password Reset Token Relation
CREATE TABLE password_reset_tokens (
   id UUID PRIMARY KEY,
   user_id UUID NOT NULL,
   token VARCHAR(64) UNIQUE NOT NULL,
   issued_at TIMESTAMP NOT NULL,
   expires_at TIMESTAMP NOT NULL,
   used BOOLEAN DEFAULT FALSE NOT NULL,
   used_at TIMESTAMP,
   ip_address VARCHAR(45),
   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_expires_at ON password_reset_tokens(expires_at);