-- ============================================================
-- Platform Role
-- ============================================================

INSERT INTO role_platform (
    id,
    name,
    is_admin,
    description,
    created_at,
    updated_at
)
SELECT
    '11111111-1111-1111-1111-111111111111',
    'Super Admin',
    TRUE,
    'Platform Super Administrator',
    CURRENT_TIMESTAMP AT TIME ZONE 'UTC',
    CURRENT_TIMESTAMP AT TIME ZONE 'UTC'
WHERE NOT EXISTS (
    SELECT 1
    FROM role_platform
    WHERE name = 'Super Admin'
       OR id = '11111111-1111-1111-1111-111111111111'
);

-- ============================================================
-- Super Admin User
-- ============================================================

INSERT INTO users (
    id,
    email,
    password,
    full_name,
    type,
    status,
    activated_date,
    created_at,
    updated_at
)
SELECT
    '22222222-2222-2222-2222-222222222222',
    'dienpro0708@gmail.com',
    '$2a$10$XZ7gX3OpHWnsSDyEqj6sGurg7n0W6GdvxE1l8aTxFZ3TtQ0fn.XqO',
    'Platform Super Admin',
    'PLATFORM',
    'ACTIVE',
    CURRENT_TIMESTAMP AT TIME ZONE 'UTC',
    CURRENT_TIMESTAMP AT TIME ZONE 'UTC',
    CURRENT_TIMESTAMP AT TIME ZONE 'UTC'
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'dienpro0708@gmail.com'
       OR id = '22222222-2222-2222-2222-222222222222'
);

-- ============================================================
-- Assign SUPER_ADMIN role
-- ============================================================

INSERT INTO user_roles (
    id,
    user_id,
    role_platform_id,
    created_at,
    updated_at
)
SELECT
    '33333333-3333-3333-3333-333333333333',
    u.id,
    r.id,
    CURRENT_TIMESTAMP AT TIME ZONE 'UTC',
    CURRENT_TIMESTAMP AT TIME ZONE 'UTC'
FROM users u
CROSS JOIN role_platform r
WHERE u.email = 'dienpro0708@gmail.com'
  AND r.name = 'Super Admin'
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles ur
      WHERE ur.user_id = u.id
        AND ur.role_platform_id = r.id
  )
ON CONFLICT (id) DO NOTHING;