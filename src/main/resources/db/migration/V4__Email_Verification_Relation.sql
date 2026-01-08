-- Email verification tokens
CREATE TABLE email_verification_tokens (
   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
   token VARCHAR(255) UNIQUE NOT NULL,
   expires_at TIMESTAMP NOT NULL,
   used BOOLEAN DEFAULT FALSE,
   used_at TIMESTAMP,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_verification_token ON email_verification_tokens(token);
CREATE INDEX idx_verification_user ON email_verification_tokens(user_id);