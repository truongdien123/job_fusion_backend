CREATE TABLE ai_generation_histories (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    user_id UUID,
    feature_type VARCHAR(50) NOT NULL,
    prompt_input JSONB NOT NULL,
    generated_output JSONB NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE,
    created_by UUID,
    updated_by UUID
);

CREATE INDEX idx_ai_generation_histories_tenant_id ON ai_generation_histories(tenant_id);
