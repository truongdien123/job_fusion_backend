CREATE TABLE IF NOT EXISTS system_configs (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    config_group VARCHAR(100) NOT NULL,
    config_key VARCHAR(255) UNIQUE NOT NULL,
    config_value TEXT NOT NULL
);
