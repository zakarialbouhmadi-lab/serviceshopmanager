-- Service Manager Database Schema
-- SQLite Database

-- Enable foreign keys
PRAGMA foreign_keys = ON;

-- Customer table
CREATE TABLE IF NOT EXISTS customer (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    notes TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index for customer name search
CREATE INDEX IF NOT EXISTS idx_customer_name ON customer(name);

-- Service Type table
CREATE TABLE IF NOT EXISTS service_type (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    price_cents INTEGER NOT NULL,
    description TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Work Order table
CREATE TABLE IF NOT EXISTS work_order (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_number TEXT NOT NULL UNIQUE,
    customer_id INTEGER NOT NULL,
    due_date TEXT,
    status TEXT NOT NULL DEFAULT 'received',
    notes TEXT,
    amount_paid INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE RESTRICT
);

-- Create indexes for work orders
CREATE INDEX IF NOT EXISTS idx_work_order_customer ON work_order(customer_id);
CREATE INDEX IF NOT EXISTS idx_work_order_status ON work_order(status);
CREATE INDEX IF NOT EXISTS idx_work_order_number ON work_order(order_number);

-- Work Order Item table
CREATE TABLE IF NOT EXISTS work_order_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    work_order_id INTEGER NOT NULL,
    service_type_id INTEGER NOT NULL,
    item_number INTEGER NOT NULL,
    barcode TEXT NOT NULL UNIQUE,
    notes TEXT,
    discount_percent REAL NOT NULL DEFAULT 0.0,
    FOREIGN KEY (work_order_id) REFERENCES work_order(id) ON DELETE CASCADE,
    FOREIGN KEY (service_type_id) REFERENCES service_type(id) ON DELETE RESTRICT
);

-- Create indexes for work order items
CREATE INDEX IF NOT EXISTS idx_work_order_item_order ON work_order_item(work_order_id);
CREATE INDEX IF NOT EXISTS idx_work_order_item_barcode ON work_order_item(barcode);

-- Settings table (key-value store)
CREATE TABLE IF NOT EXISTS settings (
    key TEXT PRIMARY KEY,
    value TEXT
);

-- Backup Log table
CREATE TABLE IF NOT EXISTS backup_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    filename TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    method TEXT NOT NULL
);

-- Create index for backup log
CREATE INDEX IF NOT EXISTS idx_backup_log_date ON backup_log(created_at);

-- Order Status History table
CREATE TABLE IF NOT EXISTS order_status_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    work_order_id INTEGER NOT NULL,
    old_status TEXT NOT NULL,
    new_status TEXT NOT NULL,
    notes TEXT,
    changed_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (work_order_id) REFERENCES work_order(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_status_history_order ON order_status_history(work_order_id);

-- Insert default settings
INSERT OR IGNORE INTO settings (key, value) VALUES ('order_counter_2025', '0');
INSERT OR IGNORE INTO settings (key, value) VALUES ('google_drive_sync_enabled', 'false');
INSERT OR IGNORE INTO settings (key, value) VALUES ('last_backup_date', '');
INSERT OR IGNORE INTO settings (key, value) VALUES ('shop_name', 'Service Shop');
INSERT OR IGNORE INTO settings (key, value) VALUES ('shop_address', '');
INSERT OR IGNORE INTO settings (key, value) VALUES ('shop_phone', '');
