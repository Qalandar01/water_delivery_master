ALTER TABLE product
    ADD COLUMN  product_image_id BIGINT;

ALTER TABLE product
    ADD CONSTRAINT fk_product_image
        FOREIGN KEY (product_image_id)
            REFERENCES product_image(id)
            ON DELETE SET NULL
            ON UPDATE CASCADE;
