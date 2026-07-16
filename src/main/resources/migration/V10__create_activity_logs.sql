-- V10__create_activity_logs.sql

CREATE TABLE IF NOT EXISTS activity_logs (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    user_id UUID REFERENCES users(id),
    event_type VARCHAR(255),
    description VARCHAR(255),
    ip_address VARCHAR(255)
);
