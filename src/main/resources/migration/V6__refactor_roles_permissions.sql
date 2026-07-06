-- V6__refactor_roles_permissions.sql

-- 1. Create the new roles table
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    name VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(255)
);

-- 2. Add column role_id to user_roles
ALTER TABLE user_roles ADD COLUMN IF NOT EXISTS role_id UUID REFERENCES roles(id);

-- 3. Populate roles table with existing role platform definitions
INSERT INTO roles (id, name, description, created_at, updated_at, created_by, updated_by)
SELECT id, name, description, created_at, updated_at, created_by, updated_by
FROM role_platform
ON CONFLICT (name) DO NOTHING;

-- 4. Populate roles table with existing role template definitions
INSERT INTO roles (id, name, description, created_at, updated_at, created_by, updated_by)
SELECT id, name, description, created_at, updated_at, created_by, updated_by
FROM role_templates
ON CONFLICT (name) DO NOTHING;

-- 5. Insert standard roles if they are missing
INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES 
    ('44444444-4444-4444-4444-444444444444', 'Tenant Admin', 'Tenant Administrator Role', CURRENT_TIMESTAMP AT TIME ZONE 'UTC', CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    ('55555555-5555-5555-5555-555555555555', 'HR', 'HR Specialist Role', CURRENT_TIMESTAMP AT TIME ZONE 'UTC', CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    ('66666666-6666-6666-6666-666666666666', 'Interviewer', 'Interviewer Role', CURRENT_TIMESTAMP AT TIME ZONE 'UTC', CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    ('77777777-7777-7777-7777-777777777777', 'Candidate', 'Candidate Role', CURRENT_TIMESTAMP AT TIME ZONE 'UTC', CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
ON CONFLICT (name) DO NOTHING;

-- 6. Update user_roles to reference the new roles table based on previous role_platform_id mappings
UPDATE user_roles ur
SET role_id = ur.role_platform_id
WHERE ur.role_platform_id IS NOT NULL;

-- 7. Update user_roles to reference the new roles table based on previous role_template_id mappings
UPDATE user_roles ur
SET role_id = ur.role_template_id
WHERE ur.role_template_id IS NOT NULL AND ur.role_id IS NULL;

-- 8. Migrate user assignments in role_tenants to user_roles
INSERT INTO user_roles (id, created_at, updated_at, created_by, updated_by, user_id, role_id)
SELECT 
    gen_random_uuid(), 
    rt.created_at, 
    rt.updated_at, 
    rt.created_by, 
    rt.updated_by, 
    rt.user_id, 
    COALESCE(rt.role_template_id, (SELECT id FROM roles WHERE name = 'Tenant Admin'))
FROM role_tenants rt
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = rt.user_id 
      AND ur.role_id = COALESCE(rt.role_template_id, (SELECT id FROM roles WHERE name = 'Tenant Admin'))
);

-- 9. Drop constraints and columns from user_roles
ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS fk_user_roles_role_template;
ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS fk_user_roles_role_platform;
ALTER TABLE user_roles DROP COLUMN IF EXISTS role_template_id;
ALTER TABLE user_roles DROP COLUMN IF EXISTS role_platform_id;

-- 10. Drop the redundant tables
DROP TABLE IF EXISTS role_permission_platform CASCADE;
DROP TABLE IF EXISTS role_permission_template CASCADE;
DROP TABLE IF EXISTS role_permission_tenant CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS role_tenants CASCADE;
DROP TABLE IF EXISTS role_platform CASCADE;
DROP TABLE IF EXISTS role_templates CASCADE;
