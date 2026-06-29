-- V1__init_database.sql

CREATE TABLE IF NOT EXISTS plan (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    name VARCHAR(255),
    monthly_price DOUBLE PRECISION,
    max_staff_account INTEGER,
    max_active_job_posting INTEGER,
    feature JSONB
);

CREATE TABLE IF NOT EXISTS tenants (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    company_name VARCHAR(255),
    domain VARCHAR(255),
    industry VARCHAR(255),
    company_size VARCHAR(255),
    region VARCHAR(255),
    company_code VARCHAR(255) UNIQUE,
    status VARCHAR(255),
    plan_id UUID REFERENCES plan(id)
);

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    full_name VARCHAR(255),
    phone VARCHAR(255),
    headline VARCHAR(255),
    address VARCHAR(255),
    date_of_birth TIMESTAMP,
    avatar VARCHAR(255),
    employee_code VARCHAR(255),
    job_title VARCHAR(255),
    status VARCHAR(255),
    type VARCHAR(255),
    activated_date TIMESTAMP,
    tenant_id UUID REFERENCES tenants(id)
);

CREATE TABLE IF NOT EXISTS role_templates (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    name VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS role_platform (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    name VARCHAR(255),
    is_admin BOOLEAN,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_roles (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    user_id UUID REFERENCES users(id),
    role_template_id UUID REFERENCES role_templates(id),
    role_platform_id UUID REFERENCES role_platform(id)
);

CREATE TABLE IF NOT EXISTS role_tenants (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    user_id UUID REFERENCES users(id),
    role_template_id UUID REFERENCES role_templates(id),
    name VARCHAR(255),
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    name VARCHAR(255),
    description VARCHAR(255),
    module VARCHAR(255),
    scope VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS role_permission_template (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    role_template_id UUID REFERENCES role_templates(id),
    permission_id UUID REFERENCES permissions(id)
);

CREATE TABLE IF NOT EXISTS role_permission_platform (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    role_id UUID REFERENCES role_platform(id),
    permission_id UUID REFERENCES permissions(id)
);

CREATE TABLE IF NOT EXISTS role_permission_tenant (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    role_tenant_id UUID REFERENCES role_tenants(id),
    permission_id UUID REFERENCES permissions(id)
);

CREATE TABLE IF NOT EXISTS user_tokens (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    user_id UUID REFERENCES users(id),
    token VARCHAR(255),
    token_type VARCHAR(255),
    expired_at TIMESTAMP,
    used BOOLEAN
);

CREATE TABLE IF NOT EXISTS skills (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    name VARCHAR(255) UNIQUE,
    category VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS candidate_resumes (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    user_id UUID REFERENCES users(id),
    file_url VARCHAR(255) NOT NULL,
    parsed_data JSONB,
    candidate_self_score DOUBLE PRECISION,
    cv_improvement_suggestions JSONB
);

CREATE TABLE IF NOT EXISTS candidate_resume_skills (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    resume_id UUID REFERENCES candidate_resumes(id),
    skill_id UUID REFERENCES skills(id),
    years_of_experience INTEGER,
    proficiency_level VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS job_postings (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    tenant_id UUID REFERENCES tenants(id),
    title VARCHAR(255),
    department VARCHAR(255),
    level VARCHAR(255),
    description TEXT,
    requirements TEXT,
    benefits TEXT,
    salary_min DOUBLE PRECISION,
    salary_max DOUBLE PRECISION,
    status VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS job_criteria (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    job_id UUID REFERENCES job_postings(id),
    criterion_name VARCHAR(255),
    description VARCHAR(255),
    weight DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS candidate_applications (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    job_posting_id UUID REFERENCES job_postings(id),
    candidate_id UUID REFERENCES users(id),
    resume_id UUID REFERENCES candidate_resumes(id),
    status VARCHAR(255),
    applied_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cv_matching_results (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    application_id UUID REFERENCES candidate_applications(id),
    matching_score DOUBLE PRECISION,
    reasoning JSONB,
    skill_gaps JSONB
);

CREATE TABLE IF NOT EXISTS interview_schedules (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    application_id UUID REFERENCES candidate_applications(id),
    user_id UUID REFERENCES users(id),
    interview_type VARCHAR(255),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    meeting_link VARCHAR(255),
    location VARCHAR(255),
    ai_brief TEXT,
    ai_questions JSONB,
    status VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS interview_feedbacks (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    interview_schedule_id UUID REFERENCES interview_schedules(id),
    interviewer_id UUID REFERENCES users(id),
    score DOUBLE PRECISION,
    strengths VARCHAR(255),
    weaknesses VARCHAR(255),
    recommendation VARCHAR(255),
    comments TEXT
);

CREATE TABLE IF NOT EXISTS email_logs (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    application_id UUID REFERENCES candidate_applications(id),
    recipient VARCHAR(255),
    subject VARCHAR(255),
    body TEXT,
    status VARCHAR(255),
    sent_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS prompts (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    prompt_type VARCHAR(255),
    title VARCHAR(255),
    prompt TEXT,
    version INTEGER,
    active BOOLEAN
);
