-- V7__alter_tenant_company_size.sql
ALTER TABLE tenants ALTER COLUMN company_size TYPE INTEGER USING (
    CASE 
        WHEN company_size ~ '^[0-9]+$' THEN company_size::INTEGER
        ELSE NULL
    END
);
