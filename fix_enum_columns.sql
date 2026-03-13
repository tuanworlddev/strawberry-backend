-- Convert PostgreSQL enum-typed columns to VARCHAR so JPA @Enumerated(EnumType.STRING) works
ALTER TABLE orders ALTER COLUMN status TYPE VARCHAR(50) USING status::text;
ALTER TABLE orders ALTER COLUMN payment_status TYPE VARCHAR(50) USING payment_status::text;

-- Drop the PG enum types if not used elsewhere
DROP TYPE IF EXISTS order_status CASCADE;
DROP TYPE IF EXISTS payment_status CASCADE;

-- Confirm
SELECT column_name, data_type FROM information_schema.columns
WHERE table_name = 'orders' AND column_name IN ('status', 'payment_status');
