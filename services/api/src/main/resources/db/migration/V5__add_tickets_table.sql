CREATE TABLE IF NOT EXISTS tickets (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    workflow_run_id BIGINT NOT NULL UNIQUE REFERENCES workflow_runs(id) ON DELETE CASCADE,

    title TEXT NOT NULL,
    description TEXT NOT NULL,
    status TEXT NOT NULL,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX IF NOT EXISTS idx_tickets_incident_id ON tickets(incident_id);
