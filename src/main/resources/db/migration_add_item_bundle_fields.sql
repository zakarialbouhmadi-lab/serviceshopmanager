-- Migration to add item_name and bundle_count to work_order_item table
-- These fields support:
-- 1. item_name: Description of the serviced item (e.g., "Red Trek Bike", "Blue Salomon Skis")
-- 2. bundle_count: Number of physical items in bundle (e.g., 2 for pair of skis)

ALTER TABLE work_order_item ADD COLUMN item_name TEXT;
ALTER TABLE work_order_item ADD COLUMN bundle_count INTEGER NOT NULL DEFAULT 1;
