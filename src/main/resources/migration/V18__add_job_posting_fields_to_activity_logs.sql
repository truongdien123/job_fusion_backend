ALTER TABLE activity_logs ADD COLUMN job_posting_id UUID;
ALTER TABLE activity_logs ADD COLUMN action VARCHAR(50);

-- Backfill existing job postings with a CREATE activity log entry
INSERT INTO activity_logs (id, created_at, created_by, user_id, event_type, description, job_posting_id, action)
SELECT 
    gen_random_uuid(), 
    created_at, 
    created_by, 
    created_by, 
    'ACTION', 
    'Created job posting: ' || title, 
    id, 
    'CREATE' 
FROM job_postings
WHERE deleted_at IS NULL;
