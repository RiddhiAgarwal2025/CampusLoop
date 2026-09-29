-- =====================================================================
--  CampusLoop — "Good Stuff. Second Chances."
--  Complete database script (schema + reference data + demo data)
--
--  Compatible with: MySQL 5.7 / 8.x and MariaDB 10.3+
--  Run with:        mysql -u root -p < database/campusloop.sql
--
--  WARNING: this script DROPS and recreates the `campusloop` database.
--
--  Demo accounts (password for all: password123)
--    riddhi@campusloop.local
--    aarav@campusloop.local
--    ananya@campusloop.local
--
--  Password format (see com.campusloop.util.PasswordUtil):
--    pbkdf2_sha256$<iterations>$<base64 salt>$<base64 derived key>
--    PBKDF2WithHmacSHA256, 120000 iterations, 16-byte salt, 256-bit key
-- =====================================================================

DROP DATABASE IF EXISTS campusloop;
CREATE DATABASE campusloop
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE campusloop;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
--  Application database account (matches src/main/resources/campusloop.properties)
--  The password satisfies MySQL's default MEDIUM validate_password policy.
-- ---------------------------------------------------------------------
CREATE USER IF NOT EXISTS 'campusloop'@'localhost' IDENTIFIED BY 'CampusLoop@2026';
GRANT SELECT, INSERT, UPDATE, DELETE ON campusloop.* TO 'campusloop'@'localhost';
FLUSH PRIVILEGES;

-- =====================================================================
--  1. STUDENTS
-- =====================================================================
CREATE TABLE students (
    student_id            INT UNSIGNED   NOT NULL AUTO_INCREMENT,
    name                  VARCHAR(80)    NOT NULL,
    email                 VARCHAR(120)   NOT NULL,
    password_hash         VARCHAR(255)   NOT NULL,
    profile_image         VARCHAR(255)   NULL,
    wallet_balance        DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    sustainability_points INT UNSIGNED   NOT NULL DEFAULT 0,
    created_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                         ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_students PRIMARY KEY (student_id),
    CONSTRAINT uq_students_email UNIQUE (email),
    CONSTRAINT ck_students_wallet CHECK (wallet_balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  2. CATEGORIES
-- =====================================================================
CREATE TABLE categories (
    category_id   INT UNSIGNED     NOT NULL AUTO_INCREMENT,
    name          VARCHAR(50)      NOT NULL,
    slug          VARCHAR(50)      NOT NULL,
    description   VARCHAR(160)     NOT NULL,
    display_order TINYINT UNSIGNED NOT NULL DEFAULT 0,
    CONSTRAINT pk_categories PRIMARY KEY (category_id),
    CONSTRAINT uq_categories_name UNIQUE (name),
    CONSTRAINT uq_categories_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  3. LISTINGS
--  status lifecycle: AVAILABLE -> RESERVED -> SOLD
--                    AVAILABLE -> REMOVED (soft delete by seller)
--                    RESERVED  -> AVAILABLE (reservation rejected/cancelled)
-- =====================================================================
CREATE TABLE listings (
    listing_id     INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    seller_id      INT UNSIGNED  NOT NULL,
    category_id    INT UNSIGNED  NOT NULL,
    title          VARCHAR(100)  NOT NULL,
    description    VARCHAR(2000) NOT NULL,
    price          DECIMAL(10,2) NOT NULL,
    item_condition ENUM('NEW','LIKE_NEW','GOOD','FAIR') NOT NULL,
    image_url      VARCHAR(255)  NOT NULL,
    status         ENUM('AVAILABLE','RESERVED','SOLD','REMOVED')
                                 NOT NULL DEFAULT 'AVAILABLE',
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_listings PRIMARY KEY (listing_id),
    CONSTRAINT fk_listings_seller   FOREIGN KEY (seller_id)
        REFERENCES students (student_id)   ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_listings_category FOREIGN KEY (category_id)
        REFERENCES categories (category_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_listings_price CHECK (price >= 10 AND price <= 100000),
    INDEX idx_listings_status_created (status, created_at),
    INDEX idx_listings_category_status (category_id, status),
    INDEX idx_listings_seller_status (seller_id, status),
    INDEX idx_listings_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  4. CART ITEMS (authenticated carts; guest carts live in a cookie)
-- =====================================================================
CREATE TABLE cart_items (
    cart_item_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    student_id   INT UNSIGNED NOT NULL,
    listing_id   INT UNSIGNED NOT NULL,
    added_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cart_items PRIMARY KEY (cart_item_id),
    CONSTRAINT uq_cart_student_listing UNIQUE (student_id, listing_id),
    CONSTRAINT fk_cart_student FOREIGN KEY (student_id)
        REFERENCES students (student_id) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_cart_listing FOREIGN KEY (listing_id)
        REFERENCES listings (listing_id) ON DELETE CASCADE ON UPDATE RESTRICT,
    INDEX idx_cart_listing (listing_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  5. TRANSACTIONS (one row per exchange request; becomes the purchase)
--
--  seller_confirmation_status: PENDING | CONFIRMED | REJECTED
--  transaction_status:
--     AWAITING_SELLER  - buyer submitted exchange details
--     AWAITING_PAYMENT - seller confirmed; listing RESERVED for this buyer
--     COMPLETED        - buyer paid from wallet; listing SOLD
--     REJECTED         - seller rejected / released the request
--     CANCELLED        - buyer cancelled, or listing removed/sold to another buyer
--  transaction_date = moment the exchange request was created.
-- =====================================================================
CREATE TABLE transactions (
    transaction_id             INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    buyer_id                   INT UNSIGNED  NOT NULL,
    seller_id                  INT UNSIGNED  NOT NULL,
    listing_id                 INT UNSIGNED  NOT NULL,
    amount                     DECIMAL(10,2) NOT NULL,
    exchange_location          VARCHAR(120)  NOT NULL,
    exchange_date              DATE          NOT NULL,
    exchange_time              TIME          NOT NULL,
    exchange_note              VARCHAR(300)  NULL,
    seller_confirmation_status ENUM('PENDING','CONFIRMED','REJECTED')
                                             NOT NULL DEFAULT 'PENDING',
    transaction_status         ENUM('AWAITING_SELLER','AWAITING_PAYMENT',
                                    'COMPLETED','REJECTED','CANCELLED')
                                             NOT NULL DEFAULT 'AWAITING_SELLER',
    transaction_date           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at               DATETIME      NULL,
    completed_at               DATETIME      NULL,
    updated_at                 DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                             ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_transactions PRIMARY KEY (transaction_id),
    CONSTRAINT fk_txn_buyer   FOREIGN KEY (buyer_id)
        REFERENCES students (student_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_txn_seller  FOREIGN KEY (seller_id)
        REFERENCES students (student_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_txn_listing FOREIGN KEY (listing_id)
        REFERENCES listings (listing_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_txn_amount CHECK (amount > 0),
    CONSTRAINT ck_txn_parties CHECK (buyer_id <> seller_id),
    INDEX idx_txn_buyer_status (buyer_id, transaction_status),
    INDEX idx_txn_seller_confirmation (seller_id, seller_confirmation_status),
    INDEX idx_txn_listing_status (listing_id, transaction_status),
    INDEX idx_txn_status_completed (transaction_status, completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  6. SELLER REVIEWS (sellers only; no product reviews)
-- =====================================================================
CREATE TABLE seller_reviews (
    review_id      INT UNSIGNED     NOT NULL AUTO_INCREMENT,
    seller_id      INT UNSIGNED     NOT NULL,
    reviewer_id    INT UNSIGNED     NOT NULL,
    transaction_id INT UNSIGNED     NOT NULL,
    rating         TINYINT UNSIGNED NOT NULL,
    comment        VARCHAR(500)     NOT NULL,
    created_at     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_seller_reviews PRIMARY KEY (review_id),
    CONSTRAINT uq_review_transaction UNIQUE (transaction_id),
    CONSTRAINT fk_review_seller   FOREIGN KEY (seller_id)
        REFERENCES students (student_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id)
        REFERENCES students (student_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_review_txn      FOREIGN KEY (transaction_id)
        REFERENCES transactions (transaction_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT ck_review_not_self CHECK (seller_id <> reviewer_id),
    INDEX idx_review_seller_created (seller_id, created_at),
    INDEX idx_review_reviewer (reviewer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  7. WALLET TRANSACTIONS (ledger; wallet_balance must equal SUM(amount))
--  amount is signed: credits positive, debits negative.
-- =====================================================================
CREATE TABLE wallet_transactions (
    wallet_txn_id  INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    student_id     INT UNSIGNED  NOT NULL,
    txn_type       ENUM('TOPUP','PURCHASE','SALE_CREDIT') NOT NULL,
    amount         DECIMAL(10,2) NOT NULL,
    balance_after  DECIMAL(10,2) NOT NULL,
    transaction_id INT UNSIGNED  NULL,
    description    VARCHAR(160)  NOT NULL,
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_wallet_transactions PRIMARY KEY (wallet_txn_id),
    CONSTRAINT fk_wallet_student FOREIGN KEY (student_id)
        REFERENCES students (student_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_wallet_txn     FOREIGN KEY (transaction_id)
        REFERENCES transactions (transaction_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_wallet_amount_nonzero CHECK (amount <> 0),
    CONSTRAINT ck_wallet_balance_after CHECK (balance_after >= 0),
    INDEX idx_wallet_student_created (student_id, created_at),
    INDEX idx_wallet_txn (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
--  VIEW: seller rating summary (computed from real reviews only)
-- =====================================================================
CREATE VIEW seller_rating_summary AS
SELECT s.student_id                           AS seller_id,
       COUNT(r.review_id)                     AS review_count,
       COALESCE(ROUND(AVG(r.rating), 1), 0.0) AS avg_rating
FROM students s
LEFT JOIN seller_reviews r ON r.seller_id = s.student_id
GROUP BY s.student_id;

-- =====================================================================
--  REFERENCE DATA: categories
-- =====================================================================
INSERT INTO categories (category_id, name, slug, description, display_order) VALUES
 (1, 'Books',             'books',       'Textbooks, reference books, guides and novels.',            1),
 (2, 'Electronics',       'electronics', 'Headphones, mice, keyboards, e-readers and accessories.',   2),
 (3, 'Calculators',       'calculators', 'Scientific, engineering and graphing calculators.',          3),
 (4, 'Gadgets',           'gadgets',     'Power banks, dev boards, smart devices and tools.',          4),
 (5, 'Hostel Essentials', 'hostel',      'Lamps, kettles, fridges, storage and room basics.',          5),
 (6, 'Stationery',        'stationery',  'Drawing kits, notebooks, pens and lab supplies.',            6),
 (7, 'Other',             'other',       'Anything else useful that deserves a second life.',          7);

-- =====================================================================
--  DEMO DATA
--  Every number below is internally consistent:
--   * wallet_balance  = SUM(wallet_transactions.amount) per student
--   * sustainability_points = 10 x completed transactions the student took part in
--   * SOLD listings have exactly one COMPLETED transaction
--   * RESERVED listing has exactly one AWAITING_PAYMENT transaction
-- =====================================================================

-- ---------- Students (password123) ----------
INSERT INTO students (student_id, name, email, password_hash, profile_image,
                      wallet_balance, sustainability_points, created_at, updated_at) VALUES
 (1, 'Riddhi Sharma', 'riddhi@campusloop.local',
  'pbkdf2_sha256$120000$7MZL2PxxLJuyIe5+k2RLnw==$xPVgnHvCVpBBauuCzaUzjIi6poLjO1qYkASiNdamIwY=',
  NULL, 3600.00, 30, '2026-08-18 10:05:00', '2026-09-14 18:05:00'),
 (2, 'Aarav Mehta', 'aarav@campusloop.local',
  'pbkdf2_sha256$120000$tSXpxZXs1HU1ut0nQe3aqQ==$1ADeTy5Ptjnj4OKXUVOYSEeMWpk9OhV+7g9EDrSUvls=',
  NULL, 2150.00, 10, '2026-08-19 09:40:00', '2026-09-02 16:45:00'),
 (3, 'Ananya Iyer', 'ananya@campusloop.local',
  'pbkdf2_sha256$120000$3w2sNZKqIaZE26nI6G2Org==$i+CCfhjsRKwJB03mtuCoDynLCzvZa4sRG8p/e/YvBK8=',
  NULL, 1750.00, 20, '2026-08-19 17:20:00', '2026-09-14 18:05:00');

-- ---------- Listings ----------
-- image_url is a context-relative path. Demo items use bundled category
-- illustrations; uploaded items use media/listings/<uuid>.<ext>.
INSERT INTO listings (listing_id, seller_id, category_id, title, description, price,
                      item_condition, image_url, status, created_at, updated_at) VALUES
 -- Riddhi (1)
 (1, 1, 1, 'Higher Engineering Mathematics – B.S. Grewal',
  '44th edition, used for two semesters. A few pencil notes in the integration chapters, no torn pages. Perfect for first-year maths.',
  350.00, 'GOOD', 'assets/images/demo/books.svg', 'SOLD', '2026-08-25 11:00:00', '2026-09-02 16:45:00'),
 (2, 1, 3, 'Casio fx-991ES Plus Scientific Calculator',
  'Exam-approved scientific calculator with 417 functions. Works perfectly, comes with the original slide cover. Battery replaced last month.',
  650.00, 'LIKE_NEW', 'assets/images/demo/calculators.svg', 'SOLD', '2026-08-26 15:30:00', '2026-09-08 13:10:00'),
 (3, 1, 1, 'Concepts of Physics – H.C. Verma (Vol 1 & 2)',
  'Both volumes together. Covers are slightly worn, inside pages are clean. Solved examples are unmarked.',
  420.00, 'GOOD', 'assets/images/demo/books.svg', 'AVAILABLE', '2026-09-10 09:15:00', '2026-09-10 09:15:00'),
 (4, 1, 2, 'Logitech M235 Wireless Mouse',
  'Compact wireless mouse with USB nano receiver. Smooth tracking, barely used since I switched to a trackpad.',
  450.00, 'LIKE_NEW', 'assets/images/demo/electronics.svg', 'AVAILABLE', '2026-09-12 18:40:00', '2026-09-12 18:40:00'),
 (5, 1, 6, 'Engineering Drawing Kit with Mini Drafter',
  'Mini drafter, compass set, set squares and a sheet container. Everything you need for the first-year ED lab.',
  300.00, 'GOOD', 'assets/images/demo/stationery.svg', 'AVAILABLE', '2026-09-15 12:10:00', '2026-09-15 12:10:00'),
 (6, 1, 5, 'Foldable Laundry Basket (60 L)',
  'Collapsible fabric laundry basket with handles. Folds flat under the bed. Washed and clean.',
  180.00, 'GOOD', 'assets/images/demo/hostel.svg', 'AVAILABLE', '2026-09-18 20:00:00', '2026-09-18 20:00:00'),
 -- Aarav (2)
 (7, 2, 2, 'boAt Rockerz 450 Bluetooth Headphones',
  'On-ear wireless headphones, around 15 hours of battery. Ear cushions in good shape. Charging cable included.',
  900.00, 'GOOD', 'assets/images/demo/electronics.svg', 'AVAILABLE', '2026-09-05 14:25:00', '2026-09-05 14:25:00'),
 (8, 2, 3, 'Casio fx-82MS Scientific Calculator',
  'Reliable basic scientific calculator. Some scratches on the case, all keys work. Good backup for exams.',
  300.00, 'FAIR', 'assets/images/demo/calculators.svg', 'AVAILABLE', '2026-09-06 10:00:00', '2026-09-06 10:00:00'),
 (9, 2, 4, 'Arduino Uno Starter Kit',
  'Arduino Uno R3 board with breadboard, jumper wires, LEDs, resistors, sensors and a servo. Used for one mini project.',
  1100.00, 'LIKE_NEW', 'assets/images/demo/gadgets.svg', 'AVAILABLE', '2026-09-09 16:45:00', '2026-09-09 16:45:00'),
 (10, 2, 1, 'Data Structures Using C – Reema Thareja',
  'Second edition. Highlighted in a few chapters on trees and graphs. Very useful for the DSA course.',
  280.00, 'GOOD', 'assets/images/demo/books.svg', 'AVAILABLE', '2026-09-11 13:05:00', '2026-09-11 13:05:00'),
 (11, 2, 4, 'Mi 10000 mAh Power Bank',
  'Dual USB output, fast charging. Holds charge well. Minor scuffs on the edges.',
  600.00, 'GOOD', 'assets/images/demo/gadgets.svg', 'AVAILABLE', '2026-09-16 19:30:00', '2026-09-16 19:30:00'),
 -- Ananya (3)
 (12, 3, 5, 'Rechargeable LED Study Lamp',
  'Touch-control desk lamp with three brightness levels and a flexible neck. Lasts about 5 hours per charge.',
  400.00, 'LIKE_NEW', 'assets/images/demo/hostel.svg', 'SOLD', '2026-08-28 21:10:00', '2026-09-14 18:05:00'),
 (13, 3, 5, 'Compact Mini Fridge (50 L)',
  'Single-door mini fridge, great for hostel rooms. Cools well, very quiet. Moving out so need it gone.',
  3200.00, 'GOOD', 'assets/images/demo/hostel.svg', 'RESERVED', '2026-09-04 08:50:00', '2026-09-22 21:30:00'),
 (14, 3, 5, 'Electric Kettle 1.5 L',
  'Stainless steel kettle with auto shut-off. Descaled and cleaned. Ideal for tea and instant noodles.',
  550.00, 'GOOD', 'assets/images/demo/hostel.svg', 'AVAILABLE', '2026-09-13 17:35:00', '2026-09-13 17:35:00'),
 (15, 3, 6, 'Notebook Set with Highlighters (Unused)',
  'Pack of four A4 ruled notebooks and five pastel highlighters. Bought extra at the start of term, never opened.',
  150.00, 'NEW', 'assets/images/demo/stationery.svg', 'AVAILABLE', '2026-09-17 11:20:00', '2026-09-17 11:20:00'),
 (16, 3, 2, 'Kindle Paperwhite (10th Generation)',
  '8 GB, waterproof, built-in light. Screen is scratch-free, comes with a folio cover. Battery lasts weeks.',
  5200.00, 'GOOD', 'assets/images/demo/electronics.svg', 'AVAILABLE', '2026-09-19 15:00:00', '2026-09-19 15:00:00'),
 (17, 3, 7, 'Chemistry Lab Coat (Size M)',
  'White cotton lab coat, size M. Worn for one semester of labs, washed and ironed. No stains.',
  250.00, 'LIKE_NEW', 'assets/images/demo/other.svg', 'AVAILABLE', '2026-09-20 10:30:00', '2026-09-20 10:30:00');

-- ---------- Transactions ----------
INSERT INTO transactions (transaction_id, buyer_id, seller_id, listing_id, amount,
                          exchange_location, exchange_date, exchange_time, exchange_note,
                          seller_confirmation_status, transaction_status,
                          transaction_date, confirmed_at, completed_at, updated_at) VALUES
 -- T1: Aarav bought Riddhi's maths book (completed, reviewed)
 (1, 2, 1, 1, 350.00, 'Central Library Entrance', '2026-09-02', '16:30:00',
  'I''ll be wearing a black backpack.',
  'CONFIRMED', 'COMPLETED', '2026-09-01 10:15:00', '2026-09-01 12:40:00', '2026-09-02 16:45:00', '2026-09-02 16:45:00'),
 -- T2: Ananya bought Riddhi's calculator (completed, reviewed)
 (2, 3, 1, 2, 650.00, 'Main Canteen', '2026-09-08', '13:00:00', NULL,
  'CONFIRMED', 'COMPLETED', '2026-09-07 18:20:00', '2026-09-07 20:05:00', '2026-09-08 13:10:00', '2026-09-08 13:10:00'),
 -- T3: Riddhi bought Ananya's study lamp (completed, reviewed)
 (3, 1, 3, 12, 400.00, 'Girls Hostel Block B Gate', '2026-09-14', '18:00:00',
  'Please bring the charging cable too.',
  'CONFIRMED', 'COMPLETED', '2026-09-13 09:30:00', '2026-09-13 11:00:00', '2026-09-14 18:05:00', '2026-09-14 18:05:00'),
 -- T4: Riddhi requested Aarav's headphones (waiting for Aarav to confirm)
 (4, 1, 2, 7, 900.00, 'Academic Block A Lobby', '2026-09-28', '15:00:00',
  'Can we test the Bluetooth pairing before I pay?',
  'PENDING', 'AWAITING_SELLER', '2026-09-23 19:10:00', NULL, NULL, '2026-09-23 19:10:00'),
 -- T5: Aarav requested Ananya's mini fridge; Ananya confirmed (awaiting Aarav's payment)
 (5, 2, 3, 13, 3200.00, 'Boys Hostel Parking', '2026-09-26', '17:30:00',
  'I''ll bring a friend to help carry it.',
  'CONFIRMED', 'AWAITING_PAYMENT', '2026-09-22 14:00:00', '2026-09-22 21:30:00', NULL, '2026-09-22 21:30:00');

-- ---------- Seller reviews (only on COMPLETED transactions, by the buyer) ----------
INSERT INTO seller_reviews (review_id, seller_id, reviewer_id, transaction_id, rating, comment, created_at) VALUES
 (1, 1, 2, 1, 5, 'Book was exactly as described with very few markings. Smooth, on-time exchange at the library.', '2026-09-02 19:00:00'),
 (2, 1, 3, 2, 4, 'Calculator works perfectly. Arrived a few minutes late but kept me informed.',                  '2026-09-08 20:00:00'),
 (3, 3, 1, 3, 5, 'Lamp is spotless and the cable was included. Friendly and reliable seller.',                     '2026-09-14 21:00:00');

-- ---------- Wallet ledger (balances reconcile with students.wallet_balance) ----------
INSERT INTO wallet_transactions (wallet_txn_id, student_id, txn_type, amount, balance_after,
                                 transaction_id, description, created_at) VALUES
 -- Riddhi: 3000 + 350 + 650 - 400 = 3600
 (1,  1, 'TOPUP',       3000.00, 3000.00, NULL, 'Wallet top-up',                                             '2026-08-20 12:00:00'),
 (4,  1, 'SALE_CREDIT',  350.00, 3350.00, 1,    'Sale credit: Higher Engineering Mathematics – B.S. Grewal', '2026-09-02 16:45:00'),
 (6,  1, 'SALE_CREDIT',  650.00, 4000.00, 2,    'Sale credit: Casio fx-991ES Plus Scientific Calculator',   '2026-09-08 13:10:00'),
 (7,  1, 'PURCHASE',    -400.00, 3600.00, 3,    'Purchase: Rechargeable LED Study Lamp',                     '2026-09-14 18:05:00'),
 -- Aarav: 2500 - 350 = 2150
 (3,  2, 'TOPUP',       2500.00, 2500.00, NULL, 'Wallet top-up',                                             '2026-08-22 09:30:00'),
 (5,  2, 'PURCHASE',    -350.00, 2150.00, 1,    'Purchase: Higher Engineering Mathematics – B.S. Grewal',    '2026-09-02 16:45:00'),
 -- Ananya: 2000 - 650 + 400 = 1750
 (2,  3, 'TOPUP',       2000.00, 2000.00, NULL, 'Wallet top-up',                                             '2026-08-21 18:15:00'),
 (8,  3, 'PURCHASE',    -650.00, 1350.00, 2,    'Purchase: Casio fx-991ES Plus Scientific Calculator',       '2026-09-08 13:10:00'),
 (9,  3, 'SALE_CREDIT',  400.00, 1750.00, 3,    'Sale credit: Rechargeable LED Study Lamp',                  '2026-09-14 18:05:00');

-- ---------- Cart items ----------
INSERT INTO cart_items (cart_item_id, student_id, listing_id, added_at) VALUES
 (1, 1, 7,  '2026-09-23 19:00:00'),  -- Riddhi: headphones (request pending)
 (2, 1, 9,  '2026-09-23 19:20:00'),  -- Riddhi: Arduino kit
 (3, 2, 13, '2026-09-22 13:50:00');  -- Aarav: mini fridge (awaiting payment)

-- =====================================================================
--  INTEGRITY CHECKS (informational; every "mismatch" column must be 0)
-- =====================================================================
SELECT 'wallet ledger vs balance' AS check_name,
       COUNT(*) AS mismatches
FROM students s
WHERE s.wallet_balance <> (SELECT COALESCE(SUM(w.amount), 0)
                           FROM wallet_transactions w
                           WHERE w.student_id = s.student_id)
UNION ALL
SELECT 'impact points vs completed resales',
       COUNT(*)
FROM students s
WHERE s.sustainability_points <> 10 * (SELECT COUNT(*) FROM transactions t
                                       WHERE t.transaction_status = 'COMPLETED'
                                         AND (t.buyer_id = s.student_id OR t.seller_id = s.student_id))
UNION ALL
SELECT 'sold listings without completed transaction',
       COUNT(*)
FROM listings l
WHERE l.status = 'SOLD'
  AND NOT EXISTS (SELECT 1 FROM transactions t
                  WHERE t.listing_id = l.listing_id AND t.transaction_status = 'COMPLETED');
