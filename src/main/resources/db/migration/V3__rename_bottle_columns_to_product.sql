-- Rename bottle_count to product_count
ALTER TABLE telegram_user
    RENAME COLUMN bottle_count TO product_count;

-- Rename bottle_types_id to product_id
ALTER TABLE telegram_user
    RENAME COLUMN bottle_types_id TO product_id;
