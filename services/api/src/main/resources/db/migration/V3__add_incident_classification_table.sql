CREATE TABLE IF NOT EXISTS incident_classifications (
    id BIGSERIAL PRIMARY KEY,
    workflow_run_id BIGINT NOT NULL UNIQUE REFERENCES workflow_runs(id) ON DELETE CASCADE,
    incident_id BIGINT NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,

    -- Classification result
    category VARCHAR(32) NOT NULL,
    priority VARCHAR(8) NOT NULL,
    summary TEXT NOT NULL,

    -- Generation metadata
    model_provider VARCHAR(32),
    model_name VARCHAR(64),
    raw_response TEXT,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
