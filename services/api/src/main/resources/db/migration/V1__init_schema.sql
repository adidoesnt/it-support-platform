-- Create incidents table
CREATE TABLE IF NOT EXISTS incidents (
    id BIGSERIAL PRIMARY KEY,
    description TEXT NOT NULL,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create enum for status
CREATE TYPE workflow_status AS ENUM ('pending', 'in_progress', 'completed', 'failed');

-- Create enum for current step
CREATE TYPE workflow_step AS ENUM (
    'payload_validation',
    'incident_classification',
    'ticket_creation'
);

-- Create workflow_runs table
CREATE TABLE IF NOT EXISTS workflow_runs (
    id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES incidents(id),
    current_step workflow_step NOT NULL,
    status workflow_status NOT NULL,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create idempotency_keys table
CREATE TABLE IF NOT EXISTS idempotency_keys (
    key VARCHAR(255) PRIMARY KEY,
    workflow_run_id BIGINT NOT NULL REFERENCES workflow_runs(id),

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create an index on workflow_runs for incident_id
-- This allows for efficient queries to find all workflow runs for a given incident
CREATE INDEX IF NOT EXISTS idx_workflow_runs_incident_id ON workflow_runs(incident_id);