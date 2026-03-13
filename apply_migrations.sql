-- V9: scheduler and metrics fields
ALTER TABLE shop_wb_integrations ADD COLUMN IF NOT EXISTS sync_interval_minutes INTEGER NOT NULL DEFAULT 360;
ALTER TABLE shop_wb_integrations ADD COLUMN IF NOT EXISTS is_sync_paused BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE shop_wb_integrations ADD COLUMN IF NOT EXISTS next_sync_expected_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE shop_wb_integrations ADD COLUMN IF NOT EXISTS consecutive_failure_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE shop_wb_integrations ADD COLUMN IF NOT EXISTS last_sync_duration_ms BIGINT;
ALTER TABLE shop_wb_integrations DROP CONSTRAINT IF EXISTS chk_sync_interval;
ALTER TABLE shop_wb_integrations ADD CONSTRAINT chk_sync_interval CHECK (sync_interval_minutes >= 15 AND sync_interval_minutes <= 1440);
ALTER TABLE sync_jobs ADD COLUMN IF NOT EXISTS trigger_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL';
ALTER TABLE sync_jobs ADD COLUMN IF NOT EXISTS duration_ms BIGINT;
CREATE INDEX IF NOT EXISTS idx_wb_integration_scheduler ON shop_wb_integrations (is_active, is_sync_paused, next_sync_expected_at);

-- V10: carts
CREATE TABLE IF NOT EXISTS carts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cart_customer UNIQUE (customer_id)
);
CREATE TABLE IF NOT EXISTS cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    variant_id UUID NOT NULL REFERENCES product_variants(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    added_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_cart_variant UNIQUE (cart_id, variant_id)
);

-- V11: orders
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_status') THEN
        CREATE TYPE order_status AS ENUM ('NEW','ASSEMBLING','SHIPPING','DELIVERED','CANCELLED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'payment_status') THEN
        CREATE TYPE payment_status AS ENUM ('PENDING','WAITING_CONFIRMATION','APPROVED','REJECTED','REFUNDED');
    END IF;
END $$;
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES users(id),
    shop_id UUID NOT NULL REFERENCES shops(id),
    order_number VARCHAR(255) NOT NULL UNIQUE,
    status order_status NOT NULL DEFAULT 'NEW',
    payment_status payment_status NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL,
    shipping_address TEXT NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    customer_email VARCHAR(255),
    customer_note TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    variant_id UUID REFERENCES product_variants(id) ON DELETE SET NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_at_purchase DECIMAL(10, 2) NOT NULL,
    product_title_snapshot VARCHAR(255) NOT NULL,
    product_slug_snapshot VARCHAR(255) NOT NULL,
    variant_attributes_snapshot TEXT,
    product_image_snapshot TEXT,
    wb_nm_id_snapshot BIGINT
);

-- V12: payment confirmations
CREATE TABLE IF NOT EXISTS payment_confirmations (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    payer_name VARCHAR(255) NOT NULL,
    transfer_amount DECIMAL(10, 2) NOT NULL,
    transfer_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    receipt_image_url TEXT NOT NULL,
    submitted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP WITHOUT TIME ZONE
);

-- V13: reserved stock
ALTER TABLE product_variants ADD COLUMN IF NOT EXISTS reserved_stock INTEGER NOT NULL DEFAULT 0;

SELECT 'All Phase 5 migrations applied successfully';
