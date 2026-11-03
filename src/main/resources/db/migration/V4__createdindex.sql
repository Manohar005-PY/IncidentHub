CREATE INDEX idx_incidents_status_created_at
ON incidents (status, created_at DESC);

CREATE INDEX idx_incidents_severity_created_at
ON incidents (severity, created_at DESC);