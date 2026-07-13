ALTER TABLE tenants
    ADD COLUMN IF NOT EXISTS expiration_date TIMESTAMP;

ALTER TABLE interview_feedbacks
    DROP CONSTRAINT IF EXISTS interview_feedbacks_interviewer_id_fkey,
    DROP CONSTRAINT IF EXISTS fkg8h45s1qw34lwfha7w9nhq067;

ALTER TABLE interview_feedbacks
    DROP COLUMN IF EXISTS interviewer_id;