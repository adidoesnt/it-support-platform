-- Alter workflow_runs table to drop enums
ALTER TABLE workflow_runs
  ALTER COLUMN current_step TYPE VARCHAR(50) USING current_step::text,
  ALTER COLUMN status TYPE VARCHAR(50) USING status::text;

-- Drop enums
DROP TYPE IF EXISTS workflow_step;
DROP TYPE IF EXISTS workflow_status;
