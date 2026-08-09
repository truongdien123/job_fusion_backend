ALTER TABLE candidate_applications ADD COLUMN IF NOT EXISTS reviewed BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE candidate_resume_skills DROP COLUMN IF EXISTS years_of_experience;
ALTER TABLE candidate_resume_skills DROP COLUMN IF EXISTS proficiency_level;

ALTER TABLE skills DROP COLUMN IF EXISTS category;
