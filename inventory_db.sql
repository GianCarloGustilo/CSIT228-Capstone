-- ============================================================
--  Smart Inventory Management System
--  MySQL Database Schema
--  XAMPP  |  Host: 127.0.0.1  |  Port: 3306
--
--  HOW TO IMPORT:
--    1. Open phpMyAdmin → http://127.0.0.1/phpmyadmin
--    2. Click "Import" tab
--    3. Choose this file → click "Go"
--  OR via terminal:
--    mysql -u root -p < inventory_db.sql
-- ============================================================


-- ── Create & select database ─────────────────────────────────
CREATE DATABASE IF NOT EXISTS inventory_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE inventory_db;


-- ── 1. users ─────────────────────────────────────────────────
--  Supports Login use case. role = 'admin' | 'staff'
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    user_id    INT            NOT NULL AUTO_INCREMENT,
    username   VARCHAR(50)    NOT NULL UNIQUE,
    password   VARCHAR(255)   NOT NULL,          -- store bcrypt hash in production
    full_name  VARCHAR(100)   NOT NULL,
    role       ENUM('admin','staff') NOT NULL DEFAULT 'staff',
    is_active  TINYINT(1)     NOT NULL DEFAULT 1,
    created_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
);


-- ── 2. categories ────────────────────────────────────────────
--  Supports Categories sidebar / filter use case
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS categories (
    category_id   INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   TEXT,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (category_id)
);


-- ── 3. suppliers ─────────────────────────────────────────────
--  Supports Suppliers sidebar use case
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id   INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(150) NOT NULL,
    contact_name  VARCHAR(100),
    phone         VARCHAR(30),
    email         VARCHAR(150),
    address       TEXT,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (supplier_id)
);


-- ── 4. products ───────────────────────────────────────────────
--  Core table — Add / Edit / Delete / View / Search / Low Stock
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS products (
    product_id    INT             NOT NULL AUTO_INCREMENT,
    category_id   INT,
    supplier_id   INT,
    name          VARCHAR(150)    NOT NULL,
    description   TEXT,
    quantity      INT             NOT NULL DEFAULT 0,
    price         DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    low_stock_threshold INT       NOT NULL DEFAULT 10,   -- alert when qty < this
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                           ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (product_id),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id)
        REFERENCES categories (category_id) ON DELETE SET NULL,
    CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id)
        REFERENCES suppliers (supplier_id) ON DELETE SET NULL
);


-- ── 5. sales ─────────────────────────────────────────────────
--  Supports Record Sales + Update Inventory use cases
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sales (
    sale_id       INT             NOT NULL AUTO_INCREMENT,
    user_id       INT,                                   -- who recorded the sale
    sale_date     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount  DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    notes         TEXT,
    PRIMARY KEY (sale_id),
    CONSTRAINT fk_sale_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE SET NULL
);


-- ── 6. sale_items ────────────────────────────────────────────
--  Line items for each sale; triggers update inventory logic
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS sale_items (
    sale_item_id  INT             NOT NULL AUTO_INCREMENT,
    sale_id       INT             NOT NULL,
    product_id    INT             NOT NULL,
    quantity_sold INT             NOT NULL,
    unit_price    DECIMAL(10, 2)  NOT NULL,              -- price at time of sale
    subtotal      DECIMAL(12, 2)  NOT NULL,
    PRIMARY KEY (sale_item_id),
    CONSTRAINT fk_saleitem_sale    FOREIGN KEY (sale_id)
        REFERENCES sales (sale_id) ON DELETE CASCADE,
    CONSTRAINT fk_saleitem_product FOREIGN KEY (product_id)
        REFERENCES products (product_id) ON DELETE RESTRICT
);


-- ── 7. inventory_logs ────────────────────────────────────────
--  Audit trail for every stock change (manual or from a sale)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS inventory_logs (
    log_id        INT          NOT NULL AUTO_INCREMENT,
    product_id    INT          NOT NULL,
    user_id       INT,
    change_type   ENUM('add','remove','adjust','sale') NOT NULL,
    quantity_before INT        NOT NULL,
    quantity_change INT        NOT NULL,               -- positive = stock added
    quantity_after  INT        NOT NULL,
    note          VARCHAR(255),
    logged_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id),
    CONSTRAINT fk_log_product FOREIGN KEY (product_id)
        REFERENCES products (product_id) ON DELETE CASCADE,
    CONSTRAINT fk_log_user    FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE SET NULL
);


-- ============================================================
--  TRIGGER: auto-deduct stock when a sale_item is inserted
-- ============================================================
DELIMITER $$

CREATE TRIGGER trg_deduct_stock
AFTER INSERT ON sale_items
FOR EACH ROW
BEGIN
    DECLARE v_before INT;
    SELECT quantity INTO v_before FROM products WHERE product_id = NEW.product_id;

    UPDATE products
    SET quantity = quantity - NEW.quantity_sold
    WHERE product_id = NEW.product_id;

    INSERT INTO inventory_logs
        (product_id, change_type, quantity_before, quantity_change, quantity_after, note)
    VALUES
        (NEW.product_id, 'sale', v_before, -NEW.quantity_sold,
         v_before - NEW.quantity_sold, CONCAT('Sale ID: ', NEW.sale_id));
END$$

DELIMITER ;


-- ============================================================
--  USEFUL VIEWS
-- ============================================================

-- Low-stock products view (mirrors getLowStockProducts() in ProductDAO)
CREATE OR REPLACE VIEW v_low_stock AS
SELECT
    p.product_id,
    p.name,
    p.quantity,
    p.low_stock_threshold,
    p.price,
    c.name AS category
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
WHERE p.quantity < p.low_stock_threshold;


-- Inventory summary view (powers the stat cards)
CREATE OR REPLACE VIEW v_inventory_summary AS
SELECT
    COUNT(*)                                    AS total_skus,
    SUM(quantity * price)                       AS total_value,
    SUM(quantity < low_stock_threshold)         AS low_stock_count
FROM products;


-- Sales report view (Generate Report use case)
CREATE OR REPLACE VIEW v_sales_report AS
SELECT
    s.sale_id,
    s.sale_date,
    u.full_name   AS recorded_by,
    p.name        AS product_name,
    si.quantity_sold,
    si.unit_price,
    si.subtotal,
    s.total_amount
FROM sales s
LEFT JOIN users      u  ON s.user_id      = u.user_id
JOIN  sale_items     si ON s.sale_id      = si.sale_id
JOIN  products       p  ON si.product_id  = p.product_id
ORDER BY s.sale_date DESC;


-- ============================================================
--  SEED DATA
-- ============================================================

-- Default users  (password = 'admin123' / 'staff123' — hash these in production)
INSERT INTO users (username, password, full_name, role) VALUES
('admin',  'admin123',  'Gian Gustilo',  'admin'),
('staff1', 'staff123',  'Juan Dela Cruz', 'staff');


-- Categories
INSERT INTO categories (name, description) VALUES
('Electronics',   'Gadgets, cables, and electronic accessories'),
('Office Supplies','Pens, paper, and desk items'),
('Peripherals',   'Keyboards, mice, and input devices'),
('Storage',       'USB drives, hard drives, memory cards'),
('Networking',    'Routers, switches, and LAN equipment');


-- Suppliers
INSERT INTO suppliers (name, contact_name, phone, email, address) VALUES
('TechSource PH',   'Maria Santos',  '09171234567', 'maria@techsource.ph',   'Cebu City'),
('Global Supplies', 'Pedro Reyes',   '09281234567', 'pedro@globalsupply.ph', 'Mandaue City'),
('Prime Gadgets',   'Ana Villanueva','09391234567', 'ana@primegadgets.ph',   'Lapu-Lapu City');


-- Products
INSERT INTO products (category_id, supplier_id, name, description, quantity, price, low_stock_threshold) VALUES
(1, 1, 'Wireless Headphones',   'Bluetooth 5.0 over-ear headphones',       142,  2499.00, 10),
(3, 2, 'USB-C Hub 7-in-1',      'Multiport hub with HDMI and USB 3.0',       8,  1850.00, 10),
(3, 1, 'Mechanical Keyboard',   'TKL layout, blue switches',                55,  3200.00, 10),
(3, 2, 'Ergonomic Mouse',       'Vertical grip, 2.4 GHz wireless',           4,   980.00,  5),
(4, 3, 'USB Flash Drive 64GB',  'USB 3.0, 100 MB/s read speed',             88,   450.00, 20),
(2, 3, 'Ballpen Set (12 pcs)',  'Black ink, 0.5mm tip',                    200,   120.00, 50),
(1, 1, 'Laptop Stand',          'Aluminum adjustable stand',                30,  1200.00, 10),
(5, 2, 'Cat6 LAN Cable 5m',     'UTP, gold-plated connectors',              60,   350.00, 15),
(4, 1, 'MicroSD 128GB',         'Class 10, UHS-I, with adapter',            15,   799.00, 10),
(1, 3, 'HDMI Cable 2m',         'Full HD 1080p, gold-plated',               9,   299.00, 10);
