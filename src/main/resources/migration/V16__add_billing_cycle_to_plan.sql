-- V16__add_billing_cycle_to_plan.sql
ALTER TABLE plan RENAME COLUMN monthly_price TO price;
ALTER TABLE plan ADD COLUMN billing_cycle VARCHAR(50);
UPDATE plan SET billing_cycle = 'MONTHLY' WHERE billing_cycle IS NULL;
ALTER TABLE plan ALTER COLUMN billing_cycle SET NOT NULL;
