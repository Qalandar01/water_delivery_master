-- 1) Create product_image table (one-to-one with product)
CREATE TABLE IF NOT EXISTS product_image (
                                             id BIGSERIAL PRIMARY KEY,
                                             file_name VARCHAR(255),
    file_type VARCHAR(100),
    product_id BIGINT UNIQUE,
    CONSTRAINT fk_product_image_product
    FOREIGN KEY (product_id)
    REFERENCES product(id)
    ON DELETE CASCADE
    );

-- 2) Create product_image_content table
CREATE TABLE IF NOT EXISTS product_image_content (
                                                     id BIGSERIAL PRIMARY KEY,
                                                     content BYTEA NOT NULL,
                                                     product_image_id BIGINT UNIQUE NOT NULL,
                                                     CONSTRAINT fk_product_image_content_image
                                                     FOREIGN KEY (product_image_id)
    REFERENCES product_image(id)
    ON DELETE CASCADE
    );


-- 5) Remove old columns
ALTER TABLE product
DROP COLUMN IF EXISTS image,
    DROP COLUMN IF EXISTS image_path;

