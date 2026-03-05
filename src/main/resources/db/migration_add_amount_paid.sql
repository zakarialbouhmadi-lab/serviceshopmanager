-- Add amount_paid column to work_order table
ALTER TABLE work_order ADD COLUMN amount_paid INTEGER NOT NULL DEFAULT 0;
