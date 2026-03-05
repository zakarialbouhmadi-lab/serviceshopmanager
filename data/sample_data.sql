-- Sample data for KraftSport Service Manager
-- Updated December 2025 with realistic test scenarios

-- Clear existing data first
DELETE FROM work_order_item;
DELETE FROM work_order;
DELETE FROM customer;
DELETE FROM service_type;
DELETE FROM order_status_history;
DELETE FROM settings WHERE key NOT IN ('app_language');

-- ============================================
-- CUSTOMERS (25 records)
-- ============================================
INSERT INTO customer (name, phone, email, notes, created_at) VALUES
('Jan Kowalski', '+48 123 456 789', 'jan.kowalski@email.pl', 'Stały klient, preferuje kontakt mailowy', '2024-11-01 10:30:00'),
('Anna Nowak', '+48 234 567 890', 'anna.nowak@gmail.com', 'VIP - rabat 10%', '2024-11-02 11:15:00'),
('Piotr Wiśniewski', '+48 345 678 901', 'piotr.w@wp.pl', NULL, '2024-11-03 09:45:00'),
('Maria Wójcik', '+48 456 789 012', 'maria.wojcik@onet.pl', 'Zawodniczka narciarstwa', '2024-11-04 14:20:00'),
('Krzysztof Kamiński', '+48 567 890 123', 'k.kaminski@interia.pl', NULL, '2024-11-05 16:00:00'),
('Magdalena Lewandowska', '+48 678 901 234', 'magda.lew@gmail.com', 'Sponsor lokalnej drużyny', '2024-11-06 10:10:00'),
('Adam Zieliński', '+48 789 012 345', 'a.zielinski@o2.pl', 'Kolarz amator', '2024-11-07 13:30:00'),
('Katarzyna Szymańska', '+48 890 123 456', 'kasia.sz@email.pl', 'Preferuje SMS', '2024-11-08 15:45:00'),
('Tomasz Woźniak', '+48 901 234 567', 'tomasz.w@wp.pl', NULL, '2024-11-09 09:00:00'),
('Barbara Dąbrowska', '+48 012 345 678', 'barbara.d@gmail.com', 'Tylko serwis ekspresowy', '2024-11-10 11:25:00'),
('Marcin Kozłowski', '+48 123 567 890', 'marcin.koz@interia.pl', 'Snowboardzista', '2024-11-11 14:40:00'),
('Agnieszka Jankowska', '+48 234 678 901', 'aga.jan@onet.pl', 'Często przynosi wiele rowerów', '2024-11-12 10:55:00'),
('Paweł Mazur', '+48 345 789 012', 'pawel.mazur@email.pl', NULL, '2024-11-13 12:20:00'),
('Monika Krawczyk', '+48 456 890 123', 'monika.k@gmail.com', 'Studentka - rabat', '2024-11-14 16:30:00'),
('Grzegorz Piotrowski', '+48 567 901 234', 'grzegorz.p@wp.pl', 'Trener narciarski', '2024-11-15 09:15:00'),
('Joanna Grabowska', '+48 678 012 345', 'joanna.g@o2.pl', 'Zawodniczka', '2024-11-16 13:00:00'),
('Michał Pawłowski', '+48 789 123 456', 'michal.pawl@interia.pl', NULL, '2024-11-17 15:20:00'),
('Ewa Michalska', '+48 890 234 567', 'ewa.m@email.pl', 'Potrzebuje przypomnień', '2024-11-18 10:40:00'),
('Rafał Nowakowski', '+48 901 345 678', 'rafal.now@gmail.com', 'Wypożyczalnia nart', '2024-11-19 14:15:00'),
('Aleksandra Wieczorek', '+48 012 456 789', 'ola.wieczorek@onet.pl', 'Odbiera tylko w weekendy', '2024-11-20 11:50:00'),
('Robert Król', '+48 111 222 333', 'robert.krol@email.pl', 'MTB entuzjasta', '2024-12-01 09:00:00'),
('Natalia Zając', '+48 222 333 444', 'natalia.z@gmail.com', NULL, '2024-12-01 10:30:00'),
('Łukasz Stępień', '+48 333 444 555', 'lukasz.s@wp.pl', 'Freeride snowboard', '2024-12-02 11:00:00'),
('Karolina Dudek', '+48 444 555 666', 'karolina.d@onet.pl', 'Cała rodzina na nartach', '2024-12-02 14:00:00'),
('Mateusz Kubiak', '+48 555 666 777', 'mateusz.k@interia.pl', 'Tester sprzętu', '2024-12-03 08:30:00');

-- ============================================
-- SERVICE TYPES (22 records)
-- ============================================
INSERT INTO service_type (code, name, price_cents, description, created_at) VALUES
-- Rowery / Bikes
('BK-BSC', 'Przegląd podstawowy roweru', 15000, 'Regulacja hamulców, przerzutek, smarowanie', '2024-10-01 10:00:00'),
('BK-FUL', 'Pełny przegląd roweru', 35000, 'Demontaż, czyszczenie, smarowanie, montaż', '2024-10-01 10:00:00'),
('BK-BRK', 'Naprawa hamulców', 8000, 'Wymiana klocków, regulacja', '2024-10-01 10:00:00'),
('BK-GER', 'Regulacja przerzutek', 6000, 'Regulacja przedniego i tylnego przerzutki', '2024-10-01 10:00:00'),
('BK-WHE', 'Centrowanie koła', 5000, 'Prostowanie obręczy', '2024-10-01 10:00:00'),
('BK-TYR', 'Wymiana opony', 4000, 'Wymiana opony i dętki', '2024-10-01 10:00:00'),
('BK-CHN', 'Wymiana łańcucha', 7000, 'Wymiana łańcucha rowerowego', '2024-10-01 10:00:00'),
('BK-SUS', 'Serwis amortyzatora', 12000, 'Przegląd widelca/amortyzatora', '2024-10-01 10:00:00'),

-- Narty / Skis
('SK-WAX', 'Smarowanie nart', 8000, 'Smar na gorąco', '2024-10-01 10:00:00'),
('SK-EDG', 'Ostrzenie krawędzi', 6000, 'Ostrzenie i polerowanie krawędzi', '2024-10-01 10:00:00'),
('SK-FUL', 'Pełny serwis nart', 18000, 'Smarowanie, krawędzie, naprawa ślizgu', '2024-10-01 10:00:00'),
('SK-BAS', 'Naprawa ślizgu', 12000, 'Naprawa rys i ubytków', '2024-10-01 10:00:00'),
('SK-BND', 'Regulacja wiązań', 5000, 'Regulacja wiązań do buta', '2024-10-01 10:00:00'),
('SK-BNI', 'Montaż wiązań', 9000, 'Montaż nowych wiązań', '2024-10-01 10:00:00'),
('SK-STR', 'Smar konserwacyjny', 6000, 'Smar ochronny na sezon letni', '2024-10-01 10:00:00'),

-- Snowboard
('SB-WAX', 'Smarowanie deski', 8000, 'Smar na gorąco', '2024-10-01 10:00:00'),
('SB-EDG', 'Ostrzenie krawędzi', 6000, 'Ostrzenie krawędzi deski', '2024-10-01 10:00:00'),
('SB-FUL', 'Pełny serwis deski', 18000, 'Smarowanie, krawędzie, naprawa ślizgu', '2024-10-01 10:00:00'),
('SB-BAS', 'Naprawa ślizgu', 12000, 'Naprawa P-Tex', '2024-10-01 10:00:00'),
('SB-BND', 'Montaż wiązań', 9000, 'Montaż wiązań snowboardowych', '2024-10-01 10:00:00'),
('SB-STR', 'Konserwacja letnia', 6000, 'Przygotowanie do przechowania', '2024-10-01 10:00:00'),

-- Usługa ekspresowa
('EXP-01', 'Dodatek ekspresowy', 5000, 'Realizacja w ciągu 24h', '2024-10-01 10:00:00');

-- ============================================
-- WORK ORDERS (30 records with various statuses)
-- ============================================
INSERT INTO work_order (order_number, customer_id, due_date, status, notes, amount_paid, created_at) VALUES
-- Odebrane / Picked up (old orders)
('2024-00001', 1, '2024-11-10', 'picked_up', 'Klient zadowolony', 15000, '2024-11-01 10:35:00'),
('2024-00002', 2, '2024-11-12', 'picked_up', 'Serwis ekspresowy', 40000, '2024-11-02 11:20:00'),
('2024-00003', 3, '2024-11-15', 'picked_up', NULL, 14000, '2024-11-03 09:50:00'),
('2024-00004', 4, '2024-11-18', 'picked_up', 'Przygotowanie do zawodów', 18000, '2024-11-04 14:25:00'),
('2024-00005', 5, '2024-11-20', 'picked_up', NULL, 16000, '2024-11-05 16:05:00'),

-- Gotowe do odbioru / Ready for pickup
('2024-00006', 6, '2024-12-03', 'ready', 'Zadzwoniono do klienta', 18000, '2024-11-28 10:15:00'),
('2024-00007', 7, '2024-12-03', 'ready', 'Gotowe przed terminem', 0, '2024-11-29 13:35:00'),
('2024-00008', 8, '2024-12-04', 'ready', 'SMS wysłany', 35000, '2024-11-30 15:50:00'),
('2024-00009', 9, '2024-12-04', 'ready', NULL, 15000, '2024-12-01 09:05:00'),
('2024-00010', 10, '2024-12-05', 'ready', 'Klient VIP', 23000, '2024-12-01 11:30:00'),

-- W realizacji / In progress
('2024-00011', 11, '2024-12-05', 'in_progress', 'Czeka na części', 0, '2024-12-01 14:45:00'),
('2024-00012', 12, '2024-12-06', 'in_progress', 'Wiele rowerów', 20000, '2024-12-02 11:00:00'),
('2024-00013', 13, '2024-12-06', 'in_progress', NULL, 0, '2024-12-02 12:25:00'),
('2024-00014', 14, '2024-12-07', 'in_progress', 'Pilne - studentka', 0, '2024-12-02 16:35:00'),
('2024-00015', 15, '2024-12-07', 'in_progress', 'Sprzęt trenera', 18000, '2024-12-02 09:20:00'),

-- Przyjęte / Received (newest orders)
('2024-00016', 16, '2024-12-08', 'received', 'Sprzęt zawodniczki', 0, '2024-12-03 08:05:00'),
('2024-00017', 17, '2024-12-08', 'received', NULL, 0, '2024-12-03 09:25:00'),
('2024-00018', 18, '2024-12-09', 'received', 'Regularny przegląd', 0, '2024-12-03 10:45:00'),
('2024-00019', 19, '2024-12-09', 'received', 'Wypożyczalnia - 5 par nart', 45000, '2024-12-03 11:20:00'),
('2024-00020', 20, '2024-12-10', 'received', 'Odbiór w sobotę', 0, '2024-12-03 12:55:00'),
('2024-00021', 21, '2024-12-10', 'received', 'MTB po sezonie', 0, '2024-12-03 13:30:00'),
('2024-00022', 22, '2024-12-11', 'received', NULL, 0, '2024-12-03 14:00:00'),
('2024-00023', 23, '2024-12-11', 'received', 'Deska freeride', 0, '2024-12-03 14:30:00'),
('2024-00024', 24, '2024-12-12', 'received', 'Rodzina 4 osoby', 36000, '2024-12-03 15:00:00'),
('2024-00025', 25, '2024-12-12', 'received', 'Tester - pilne', 0, '2024-12-03 15:30:00'),

-- Anulowane / Cancelled
('2024-00026', 1, '2024-11-25', 'canceled', 'Klient zrezygnował', 0, '2024-11-22 10:00:00'),
('2024-00027', 5, '2024-11-28', 'canceled', 'Zdublowane zlecenie', 0, '2024-11-25 14:00:00'),

-- Dodatkowe zlecenia z różnymi kwotami
('2024-00028', 2, '2024-12-05', 'in_progress', 'Narty + buty', 10000, '2024-12-02 10:00:00'),
('2024-00029', 3, '2024-12-06', 'received', 'Rower elektryczny', 0, '2024-12-03 16:00:00'),
('2024-00030', 4, '2024-12-07', 'received', 'Narty biegowe', 0, '2024-12-03 16:30:00');

-- ============================================
-- WORK ORDER ITEMS (50+ records)
-- ============================================
INSERT INTO work_order_item (work_order_id, service_type_id, item_number, barcode, notes, discount_percent) VALUES
-- Order 1: Basic bike service
(1, 1, 1, '2024-00001-01', 'Rower górski w dobrym stanie', 0),

-- Order 2: Full bike + express
(2, 2, 1, '2024-00002-01', 'Rower szosowy', 0),
(2, 22, 2, '2024-00002-02', 'Ekspres 24h', 0),

-- Order 3: Brake + gear service
(3, 3, 1, '2024-00003-01', 'Przednie klocki zużyte', 0),
(3, 4, 2, '2024-00003-02', 'Tylna przerzutka', 0),

-- Order 4: Full ski service
(4, 11, 1, '2024-00004-01', 'Przygotowanie przed sezonem', 0),

-- Order 5: Ski wax pair
(5, 9, 1, '2024-00005-01', 'Lewa narta', 0),
(5, 9, 2, '2024-00005-02', 'Prawa narta', 0),

-- Order 6: Full ski service
(6, 11, 1, '2024-00006-01', 'Narty slalomowe', 0),

-- Order 7: Bike wheel + tire
(7, 5, 1, '2024-00007-01', 'Tylne koło wygięte', 0),
(7, 6, 2, '2024-00007-02', 'Wymiana obu opon', 0),

-- Order 8: Full bike overhaul
(8, 2, 1, '2024-00008-01', 'Kompleksowy przegląd', 0),

-- Order 9: Basic bike service
(9, 1, 1, '2024-00009-01', 'Roczny przegląd', 0),

-- Order 10: Full ski + express
(10, 11, 1, '2024-00010-01', 'Narty zawodnicze', 0),
(10, 22, 2, '2024-00010-02', 'Pilne', 0),

-- Order 11: Snowboard full service (waiting for parts)
(11, 18, 1, '2024-00011-01', 'Deska snowboardowa', 0),

-- Order 12: Family bikes (3 items)
(12, 1, 1, '2024-00012-01', 'Rower taty', 0),
(12, 1, 2, '2024-00012-02', 'Rower mamy', 0),
(12, 1, 3, '2024-00012-03', 'Rower dziecka', 10),

-- Order 13: Snowboard wax + edge
(13, 16, 1, '2024-00013-01', 'Smarowanie', 0),
(13, 17, 2, '2024-00013-02', 'Krawędzie', 0),

-- Order 14: Student bike service (discount)
(14, 1, 1, '2024-00014-01', 'Rabat studencki', 15),

-- Order 15: Coach ski equipment
(15, 11, 1, '2024-00015-01', 'Narty instruktorskie', 0),

-- Order 16: Athlete skis
(16, 11, 1, '2024-00016-01', 'Narty GS', 0),
(16, 13, 2, '2024-00016-02', 'Regulacja wiązań', 0),

-- Order 17: Brake service
(17, 3, 1, '2024-00017-01', 'Hamulce tarczowe', 0),

-- Order 18: Bike checkup
(18, 1, 1, '2024-00018-01', 'Regularny przegląd', 0),

-- Order 19: Rental shop skis (5 pairs)
(19, 11, 1, '2024-00019-01', 'Para 1', 0),
(19, 11, 2, '2024-00019-02', 'Para 2', 0),
(19, 11, 3, '2024-00019-03', 'Para 3', 0),
(19, 11, 4, '2024-00019-04', 'Para 4', 0),
(19, 11, 5, '2024-00019-05', 'Para 5', 0),

-- Order 20: Weekend pickup
(20, 9, 1, '2024-00020-01', 'Smarowanie przed wyjazdem', 0),

-- Order 21: MTB full service
(21, 2, 1, '2024-00021-01', 'Po sezonie MTB', 0),
(21, 8, 2, '2024-00021-02', 'Serwis amortyzatora', 0),

-- Order 22: Ski edges
(22, 10, 1, '2024-00022-01', 'Ostrzenie krawędzi', 0),

-- Order 23: Freeride board
(23, 18, 1, '2024-00023-01', 'Deska freeride', 0),
(23, 20, 2, '2024-00023-02', 'Montaż wiązań', 0),

-- Order 24: Family skis (4 pairs)
(24, 11, 1, '2024-00024-01', 'Tata', 0),
(24, 11, 2, '2024-00024-02', 'Mama', 0),
(24, 9, 3, '2024-00024-03', 'Syn - tylko smar', 0),
(24, 9, 4, '2024-00024-04', 'Córka - tylko smar', 0),

-- Order 25: Tester gear
(25, 11, 1, '2024-00025-01', 'Testowe narty 1', 0),
(25, 11, 2, '2024-00025-02', 'Testowe narty 2', 0),
(25, 18, 3, '2024-00025-03', 'Testowa deska', 0),

-- Order 28: Skis + binding check
(28, 11, 1, '2024-00028-01', 'Narty all-mountain', 0),
(28, 13, 2, '2024-00028-02', 'Sprawdzenie wiązań', 0),

-- Order 29: E-bike
(29, 2, 1, '2024-00029-01', 'Rower elektryczny - kompleksowy', 0),

-- Order 30: Cross-country skis
(30, 9, 1, '2024-00030-01', 'Narty biegowe - lewa', 0),
(30, 9, 2, '2024-00030-02', 'Narty biegowe - prawa', 0);

-- ============================================
-- SETTINGS (Shop info + printer config)
-- ============================================
INSERT OR REPLACE INTO settings (key, value) VALUES
('shop_name', 'KraftSport Serwis'),
('shop_address', 'ul. Sportowa 12, 02-787 Warszawa'),
('shop_phone', '+48 22 123 45 67'),
('shop_email', 'serwis@kraftsport.pl'),
('last_order_number', '30'),
('last_order_year', '2024'),
('order_counter_2024', '30'),
('sticker_size', '36mm x 89mm (Brady Portrait)'),
('label.width.mm', '36'),
('label.height.mm', '89'),
('use_zpl_printing', 'false'),
('auto_print_a4', 'true'),
('show_print_dialog', 'false');

-- ============================================
-- SUMMARY
-- ============================================
SELECT '=== SAMPLE DATA LOADED ===' as info;
SELECT 'Customers: ' || COUNT(*) as summary FROM customer;
SELECT 'Service Types: ' || COUNT(*) as summary FROM service_type;
SELECT 'Work Orders: ' || COUNT(*) as summary FROM work_order;
SELECT 'Order Items: ' || COUNT(*) as summary FROM work_order_item;
SELECT '' as spacer;
SELECT 'Orders by status:' as info;
SELECT status, COUNT(*) as count FROM work_order GROUP BY status ORDER BY 
    CASE status 
        WHEN 'received' THEN 1 
        WHEN 'in_progress' THEN 2 
        WHEN 'ready' THEN 3 
        WHEN 'picked_up' THEN 4 
        WHEN 'canceled' THEN 5 
    END;
