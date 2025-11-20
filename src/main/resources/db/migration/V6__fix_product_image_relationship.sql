-------------------------------------------------------------
-- 1) Drop wrong FK if it exists
-------------------------------------------------------------
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_product_image_product'
    ) THEN
ALTER TABLE product_image
DROP CONSTRAINT fk_product_image_product;
END IF;
END $$;

-------------------------------------------------------------
-- 2) Drop wrong product_id column if it exists
-------------------------------------------------------------
ALTER TABLE product_image
DROP COLUMN IF EXISTS product_id;

-------------------------------------------------------------
-- 3) Ensure product_image_id column exists in product
-------------------------------------------------------------
ALTER TABLE product
    ADD COLUMN IF NOT EXISTS product_image_id BIGINT;

-------------------------------------------------------------
-- 4) Add correct FK ONLY if it does not exist
-------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_product_image'
    ) THEN
ALTER TABLE product
    ADD CONSTRAINT fk_product_image
        FOREIGN KEY (product_image_id)
            REFERENCES product_image(id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;
END IF;
END $$;

-------------------------------------------------------------
-- 5) Ensure product_image_content FK exists
-------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_product_image_content_image'
    ) THEN
ALTER TABLE product_image_content
    ADD CONSTRAINT fk_product_image_content_image
        FOREIGN KEY (product_image_id)
            REFERENCES product_image(id)
            ON DELETE CASCADE;
END IF;
END $$;
