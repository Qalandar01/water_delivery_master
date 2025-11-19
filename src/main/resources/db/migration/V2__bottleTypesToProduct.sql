---------------------------------------------------------
-- 1. Rename the main table
---------------------------------------------------------
ALTER TABLE bottle_types RENAME TO product;

---------------------------------------------------------
-- 2. Rename foreign key column in order_product table
---------------------------------------------------------
ALTER TABLE order_product
    RENAME COLUMN bottle_types_id TO product_id;

---------------------------------------------------------
-- Drop old FK and create new FK for order_product
---------------------------------------------------------
DO $$
DECLARE
fk_name text;
BEGIN
    -- Find FK referencing bottle_types
SELECT constraint_name INTO fk_name
FROM information_schema.key_column_usage
WHERE table_name = 'order_product'
  AND column_name = 'product_id';

IF fk_name IS NOT NULL THEN
        EXECUTE 'ALTER TABLE order_product DROP CONSTRAINT ' || fk_name;
END IF;

    -- Create new FK referencing product table
EXECUTE '
        ALTER TABLE order_product
        ADD CONSTRAINT fk_order_product_product
        FOREIGN KEY (product_id)
        REFERENCES product(id)
        ON DELETE CASCADE
    ';
END $$;

---------------------------------------------------------
-- 3. Rename foreign key column in baskets table
---------------------------------------------------------
ALTER TABLE baskets
    RENAME COLUMN bottle_type_id TO product_id;

---------------------------------------------------------
-- Drop old FK and create new FK for baskets
---------------------------------------------------------
DO $$
DECLARE
fk_name text;
BEGIN
    -- Find FK referencing bottle_types
SELECT constraint_name INTO fk_name
FROM information_schema.key_column_usage
WHERE table_name = 'baskets'
  AND column_name = 'product_id';

IF fk_name IS NOT NULL THEN
        EXECUTE 'ALTER TABLE baskets DROP CONSTRAINT ' || fk_name;
END IF;

    -- Create new FK referencing product table
EXECUTE '
        ALTER TABLE baskets
        ADD CONSTRAINT fk_baskets_product
        FOREIGN KEY (product_id)
        REFERENCES product(id)
        ON DELETE CASCADE
    ';
END $$;

---------------------------------------------------------
-- 4. Rename sequence if auto-generated (PostgreSQL)
---------------------------------------------------------
DO $$
DECLARE
seq_name text;
BEGIN
SELECT pg_get_serial_sequence('product', 'id') INTO seq_name;

IF seq_name IS NOT NULL THEN
        EXECUTE 'ALTER SEQUENCE ' || seq_name || ' RENAME TO product_id_seq';
END IF;
END $$;
