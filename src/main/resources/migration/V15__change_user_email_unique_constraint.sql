-- Drop the original inline unique constraint on email
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;

-- Create a partial unique index where deleted_at is null
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_deleted_at_null ON users (email) WHERE deleted_at IS NULL;
