-- Add OutBox events table for event publishing

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id VARCHAR(255) NOT NULL ,
    aggregate_type VARCHAR(100)NOT NULL ,
    event_type VARCHAR(100) NOT NULL ,
    payload JSONB NOT NULL ,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP
);

-- Poller queries PENDING events constantly, most critical index
CREATE INDEX idx_outbox_status_pending ON outbox_events(status)
    WHERE status = 'PENDING';

CREATE INDEX idx_outbox_status_failed ON outbox_events(status)
    where status = 'FAILED';

CREATE INDEX idx_outbox_status_published ON outbox_events(status)
    WHERE status = 'PUBLISHED';

-- Useful for debugging/querying events by aggregate (e.g "All events for this profile")
CREATE INDEX idx_outbox_aggregate ON outbox_events(aggregate_id, aggregate_type);

-- Useful for cleanup jobs (delete old published events)
CREATE INDEX idx_outbox_create_at ON outbox_events(created_at);