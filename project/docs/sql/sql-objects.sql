-- ============================================================
-- 外卖管理系统 — SQL 对象完整定义
-- ============================================================
-- 使用方式: mysql -u root -p takeout < sql-objects.sql
-- 包含: 3个函数 + 13个存储过程 + 2个触发器 + 7个视图
-- ============================================================

-- ============================================================
-- 第1部分: 函数 (Functions)
-- ============================================================

-- -------------------------------------------------------
-- fn_gen_order_no — 生成订单编号 (YYYYMMDD + 6位流水)
-- 替代: Java Redis 计数器
-- -------------------------------------------------------
DROP FUNCTION IF EXISTS fn_gen_order_no;
DELIMITER $$
CREATE FUNCTION fn_gen_order_no() RETURNS CHAR(14) CHARSET utf8mb4 READS SQL DATA
BEGIN
    DECLARE today CHAR(8); DECLARE max_no CHAR(14); DECLARE seq INT DEFAULT 1;
    SET today = DATE_FORMAT(CURDATE(), '%Y%m%d');
    SELECT MAX(order_no) INTO max_no FROM order_info WHERE order_no COLLATE utf8mb4_general_ci LIKE CONCAT(today, '%');
    IF max_no IS NOT NULL THEN SET seq = CAST(SUBSTRING(max_no, 9) AS UNSIGNED) + 1; END IF;
    RETURN CONCAT(today, LPAD(seq, 6, '0'));
END$$
DELIMITER ;

-- -------------------------------------------------------
-- fn_calc_order_total — 计算订单总金额
-- 替代: Java BigDecimal 循环累加
-- -------------------------------------------------------
DROP FUNCTION IF EXISTS fn_calc_order_total;
DELIMITER $$
CREATE FUNCTION fn_calc_order_total(p_order_no CHAR(14)) RETURNS DECIMAL(12,2) READS SQL DATA
BEGIN
    DECLARE total DECIMAL(12,2);
    SELECT COALESCE(SUM(unit_price * quantity), 0) INTO total FROM order_item WHERE order_no = p_order_no;
    RETURN total;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- fn_can_transition — 校验订单状态转换是否合法
-- 替代: Java OrderStatusEnum.canTransition()
-- -------------------------------------------------------
DROP FUNCTION IF EXISTS fn_can_transition;
DELIMITER $$
CREATE FUNCTION fn_can_transition(p_from VARCHAR(8), p_to VARCHAR(8)) RETURNS TINYINT DETERMINISTIC
BEGIN
    IF p_from = '已提交' AND p_to IN ('待接单','备餐中','已取消') THEN RETURN 1; END IF;
    IF p_from = '待接单' AND p_to IN ('备餐中','已取消') THEN RETURN 1; END IF;
    IF p_from = '备餐中' AND p_to = '待配送' THEN RETURN 1; END IF;
    IF p_from = '待配送' AND p_to = '配送中' THEN RETURN 1; END IF;
    IF p_from = '配送中' AND p_to = '已送达' THEN RETURN 1; END IF;
    IF p_from = '已送达' AND p_to = '已完成' THEN RETURN 1; END IF;
    RETURN 0;
END$$
DELIMITER ;


-- ============================================================
-- 第2部分: 存储过程 (Procedures) — 13个
-- ============================================================

-- -------------------------------------------------------
-- sp_register_user — 用户注册（原子操作: user + 角色子表）
-- Java调用: jdbcTemplate.update("CALL sp_register_user(?,?,?,?,?,?,?,?,@uid)", ...)
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_register_user;
DELIMITER $$
CREATE PROCEDURE sp_register_user(
    IN p_phone VARCHAR(32), IN p_password VARCHAR(128), IN p_role VARCHAR(8), IN p_name VARCHAR(50),
    IN p_province VARCHAR(20), IN p_city VARCHAR(20), IN p_district VARCHAR(20),
    IN p_addr_detail VARCHAR(100), OUT p_user_id BIGINT)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    IF EXISTS (SELECT 1 FROM `user` WHERE phone = p_phone) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Phone already registered'; END IF;
    INSERT INTO `user` (phone, password, role, status) VALUES (p_phone, p_password, p_role, '正常');
    SET p_user_id = LAST_INSERT_ID();
    IF p_role = 'CUSTOMER' THEN INSERT INTO customer (id, name) VALUES (p_user_id, COALESCE(p_name, p_phone));
    ELSEIF p_role = 'MERCHANT' THEN INSERT INTO merchant (id, name, province, city, district, address_detail, biz_status, audit_status)
        VALUES (p_user_id, COALESCE(p_name, CONCAT('Merchant', p_user_id)), COALESCE(p_province,'TBD'), COALESCE(p_city,'TBD'), COALESCE(p_district,'TBD'), COALESCE(p_addr_detail,'TBD'), '营业中', '待审核');
    ELSEIF p_role = 'RIDER' THEN INSERT INTO rider (id, name, status) VALUES (p_user_id, COALESCE(p_name, p_phone), '空闲'); END IF;
    COMMIT;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- sp_delete_user — 安全删除用户（保留订单数据）
-- Java调用: jdbcTemplate.update("CALL sp_delete_user(?)", userId)
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_delete_user;
DELIMITER $$
CREATE PROCEDURE sp_delete_user(IN p_user_id BIGINT)
BEGIN
    DECLARE v_role VARCHAR(8);
    SELECT role INTO v_role FROM `user` WHERE id = p_user_id;
    IF v_role IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'User not found'; END IF;

    IF v_role = 'CUSTOMER' THEN
        DELETE FROM cart_item WHERE customer_id = p_user_id;
        UPDATE order_info SET status = '已取消' WHERE customer_id = p_user_id AND status IN ('已提交','待接单');
        UPDATE order_info SET status = '已完成' WHERE customer_id = p_user_id AND status IN ('备餐中','待配送','配送中','已送达');
        UPDATE customer SET name = '已注销用户', default_address_id = NULL WHERE id = p_user_id;
        UPDATE address SET detail = '(已注销)', is_default = 0 WHERE customer_id = p_user_id;
    ELSEIF v_role = 'MERCHANT' THEN
        UPDATE order_info SET status = '已取消' WHERE merchant_id = p_user_id AND status IN ('已提交','待接单','备餐中');
        UPDATE dish SET status = '下架' WHERE merchant_id = p_user_id;
        UPDATE merchant SET biz_status = '休息中', audit_status = '已驳回', name = CONCAT(name, '(已注销)') WHERE id = p_user_id;
    ELSEIF v_role = 'RIDER' THEN
        UPDATE delivery SET rider_id = NULL WHERE rider_id = p_user_id AND status IN ('待取餐','配送中');
        DELETE FROM rider WHERE id = p_user_id;
    END IF;

    DELETE FROM operation_log WHERE user_id = p_user_id;
    DELETE FROM notification WHERE user_id = p_user_id;
    UPDATE `user` SET status = '禁用', phone = CONCAT('DEL_', p_user_id) WHERE id = p_user_id;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- sp_submit_order — 提交订单（原子: 校验→写表→扣库存→清购物车）
-- Java调用: jdbcTemplate.update("CALL sp_submit_order(?,?,?,?,?,?,?,?,?,?,?,?,?,?,@ono)", ...)
-- 最多支持5个菜品，不足的参数传 NULL, 0
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_submit_order;
DELIMITER $$
CREATE PROCEDURE sp_submit_order(
    IN p_customer_id BIGINT, IN p_merchant_id BIGINT, IN p_address_id BIGINT, IN p_remark VARCHAR(200),
    IN p_d1 BIGINT, IN p_q1 INT, IN p_d2 BIGINT, IN p_q2 INT,
    IN p_d3 BIGINT, IN p_q3 INT, IN p_d4 BIGINT, IN p_q4 INT,
    IN p_d5 BIGINT, IN p_q5 INT, OUT p_order_no CHAR(14))
BEGIN
    DECLARE v_auto TINYINT DEFAULT 0; DECLARE v_total DECIMAL(12,2) DEFAULT 0; DECLARE v_status VARCHAR(8);
    DECLARE v_name VARCHAR(50); DECLARE v_price DECIMAL(10,2); DECLARE v_i INT DEFAULT 1;
    DECLARE v_did BIGINT; DECLARE v_qty INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; RESIGNAL; END;
    START TRANSACTION;
    IF NOT EXISTS (SELECT 1 FROM address WHERE id = p_address_id AND customer_id = p_customer_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Address not found'; END IF;
    SELECT auto_accept INTO v_auto FROM merchant WHERE id = p_merchant_id;
    SET v_status = IF(v_auto = 1, '备餐中', '已提交');
    SET p_order_no = fn_gen_order_no();
    INSERT INTO order_info (order_no, customer_id, merchant_id, status, total_amount, remark, address_id)
    VALUES (p_order_no, p_customer_id, p_merchant_id, v_status, 0, p_remark, p_address_id);
    WHILE v_i <= 5 DO
        CASE v_i WHEN 1 THEN SET v_did=p_d1, v_qty=p_q1;
                 WHEN 2 THEN SET v_did=p_d2, v_qty=p_q2;
                 WHEN 3 THEN SET v_did=p_d3, v_qty=p_q3;
                 WHEN 4 THEN SET v_did=p_d4, v_qty=p_q4;
                 WHEN 5 THEN SET v_did=p_d5, v_qty=p_q5; END CASE;
        IF v_did IS NOT NULL AND v_qty > 0 THEN
            SELECT name, price INTO v_name, v_price FROM dish WHERE id = v_did AND status = '上架';
            IF v_name IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Dish offline'; END IF;
            IF (SELECT stock FROM dish WHERE id = v_did) < v_qty THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock insufficient'; END IF;
            INSERT INTO order_item (order_no, dish_id, dish_name, unit_price, quantity) VALUES (p_order_no, v_did, v_name, v_price, v_qty);
            UPDATE dish SET stock = stock - v_qty WHERE id = v_did;
            SET v_total = v_total + (v_price * v_qty);
            DELETE FROM cart_item WHERE customer_id = p_customer_id AND dish_id = v_did;
        END IF;
        SET v_i = v_i + 1;
    END WHILE;
    UPDATE order_info SET total_amount = v_total WHERE order_no = p_order_no;
    COMMIT;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- sp_accept_order — 商家接单
-- Java调用: jdbcTemplate.update("CALL sp_accept_order(?,?)", orderNo, merchantId)
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_accept_order;
DELIMITER $$
CREATE PROCEDURE sp_accept_order(IN p_order_no CHAR(14), IN p_merchant_id BIGINT)
BEGIN
    DECLARE v_status VARCHAR(8);
    SELECT status INTO v_status FROM order_info WHERE order_no = p_order_no AND merchant_id = p_merchant_id;
    IF v_status IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Order not found'; END IF;
    IF v_status != '已提交' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid status'; END IF;
    UPDATE order_info SET status = '备餐中' WHERE order_no = p_order_no;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- sp_prepare_complete — 备餐完成 → 待配送
-- 触发器 trg_order_update_sales 会自动生成 delivery 记录
-- Java调用: jdbcTemplate.update("CALL sp_prepare_complete(?,?)", orderNo, merchantId)
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_prepare_complete;
DELIMITER $$
CREATE PROCEDURE sp_prepare_complete(IN p_order_no CHAR(14), IN p_merchant_id BIGINT)
BEGIN
    DECLARE v_status VARCHAR(8);
    SELECT status INTO v_status FROM order_info WHERE order_no = p_order_no AND merchant_id = p_merchant_id;
    IF v_status IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Order not found'; END IF;
    IF v_status != '备餐中' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid status'; END IF;
    UPDATE order_info SET status = '待配送' WHERE order_no = p_order_no;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- sp_cancel_order — 顾客取消订单（仅限已提交/待接单）
-- Java调用: jdbcTemplate.update("CALL sp_cancel_order(?,?)", orderNo, customerId)
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_cancel_order;
DELIMITER $$
CREATE PROCEDURE sp_cancel_order(IN p_order_no CHAR(14), IN p_customer_id BIGINT)
BEGIN
    DECLARE v_status VARCHAR(8);
    SELECT status INTO v_status FROM order_info WHERE order_no = p_order_no AND customer_id = p_customer_id;
    IF v_status IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Order not found'; END IF;
    IF v_status NOT IN ('已提交','待接单') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot cancel after accepted'; END IF;
    DELETE FROM delivery WHERE order_no = p_order_no;
    UPDATE order_info SET status = '已取消' WHERE order_no = p_order_no;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- sp_request_cancel — 顾客申请取消（商家已接单后的协商取消）
-- Java调用: jdbcTemplate.update("CALL sp_request_cancel(?,?)", orderNo, customerId)
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_request_cancel;
DELIMITER $$
CREATE PROCEDURE sp_request_cancel(IN p_order_no CHAR(14), IN p_customer_id BIGINT)
BEGIN
    DECLARE v_status VARCHAR(8); DECLARE v_merchant_id BIGINT;
    SELECT status, merchant_id INTO v_status, v_merchant_id FROM order_info WHERE order_no = p_order_no AND customer_id = p_customer_id;
    IF v_status IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Order not found'; END IF;
    IF v_status IN ('已提交','待接单') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Use direct cancel'; END IF;
    INSERT INTO notification (user_id, event_type, title, content, ref_id) VALUES (v_merchant_id, 'order_ready', 'Cancel request', CONCAT('Customer requests cancel order ', p_order_no), p_order_no);
END$$
DELIMITER ;

-- -------------------------------------------------------
-- sp_merchant_cancel_review — 商家审批顾客的取消申请
-- Java调用: jdbcTemplate.update("CALL sp_merchant_cancel_review(?,?,?)", orderNo, merchantId, approved?1:0)
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_merchant_cancel_review;
DELIMITER $$
CREATE PROCEDURE sp_merchant_cancel_review(IN p_order_no CHAR(14), IN p_merchant_id BIGINT, IN p_approved TINYINT)
BEGIN
    DECLARE v_customer_id BIGINT;
    SELECT customer_id INTO v_customer_id FROM order_info WHERE order_no = p_order_no AND merchant_id = p_merchant_id;
    IF v_customer_id IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Order not found'; END IF;
    IF p_approved = 1 THEN
        DELETE FROM delivery WHERE order_no = p_order_no;
        UPDATE order_info SET status = '已取消' WHERE order_no = p_order_no;
        INSERT INTO notification (user_id, event_type, title, content, ref_id) VALUES (v_customer_id, 'delivery_arrived', 'Cancel approved', CONCAT('Merchant approved cancel for ', p_order_no), p_order_no);
    ELSE
        INSERT INTO notification (user_id, event_type, title, content, ref_id) VALUES (v_customer_id, 'delivery_arrived', 'Cancel rejected', CONCAT('Merchant rejected cancel for ', p_order_no), p_order_no);
    END IF;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- sp_confirm_receipt — 顾客确认收货
-- 触发器 trg_order_update_sales 会自动更新商家月销量
-- Java调用: jdbcTemplate.update("CALL sp_confirm_receipt(?,?)", orderNo, customerId)
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_confirm_receipt;
DELIMITER $$
CREATE PROCEDURE sp_confirm_receipt(IN p_order_no CHAR(14), IN p_customer_id BIGINT)
BEGIN
    DECLARE v_status VARCHAR(8);
    SELECT status INTO v_status FROM order_info WHERE order_no = p_order_no AND customer_id = p_customer_id;
    IF v_status IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Order not found'; END IF;
    IF v_status != '已送达' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid status'; END IF;
    UPDATE order_info SET status = '已完成' WHERE order_no = p_order_no;
    UPDATE delivery SET status = '已送达', deliver_time = NOW() WHERE order_no = p_order_no;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- sp_rider_accept_delivery — 骑手接配送单（骑手可同时接多单）
-- Java调用: jdbcTemplate.update("CALL sp_rider_accept_delivery(?,?)", deliveryId, riderId)
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_rider_accept_delivery;
DELIMITER $$
CREATE PROCEDURE sp_rider_accept_delivery(IN p_delivery_id BIGINT, IN p_rider_id BIGINT)
BEGIN
    DECLARE v_rider_id BIGINT; DECLARE v_order_no CHAR(14);
    SELECT rider_id, order_no INTO v_rider_id, v_order_no FROM delivery WHERE id = p_delivery_id;
    IF v_order_no IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Delivery not found'; END IF;
    IF v_rider_id IS NOT NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Already taken'; END IF;
    UPDATE delivery SET rider_id = p_rider_id, status = '待取餐' WHERE id = p_delivery_id;
    UPDATE rider SET status = '配送中' WHERE id = p_rider_id;
    UPDATE order_info SET status = '配送中' WHERE order_no = v_order_no;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- sp_rider_pickup — 骑手确认取餐
-- Java调用: jdbcTemplate.update("CALL sp_rider_pickup(?,?)", deliveryId, riderId)
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_rider_pickup;
DELIMITER $$
CREATE PROCEDURE sp_rider_pickup(IN p_delivery_id BIGINT, IN p_rider_id BIGINT)
BEGIN
    UPDATE delivery SET status = '配送中', pickup_time = NOW() WHERE id = p_delivery_id AND rider_id = p_rider_id;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- sp_rider_deliver — 骑手确认送达
-- Java调用: jdbcTemplate.update("CALL sp_rider_deliver(?,?)", deliveryId, riderId)
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_rider_deliver;
DELIMITER $$
CREATE PROCEDURE sp_rider_deliver(IN p_delivery_id BIGINT, IN p_rider_id BIGINT)
BEGIN
    DECLARE v_order_no CHAR(14);
    SELECT order_no INTO v_order_no FROM delivery WHERE id = p_delivery_id AND rider_id = p_rider_id;
    IF v_order_no IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Delivery not found or not yours'; END IF;
    UPDATE delivery SET status = '已送达', deliver_time = NOW() WHERE id = p_delivery_id;
    UPDATE order_info SET status = '已送达' WHERE order_no = v_order_no;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- sp_audit_merchant — 管理员审核商家
-- Java调用: jdbcTemplate.update("CALL sp_audit_merchant(?,?)", merchantId, auditStatus)
-- -------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_audit_merchant;
DELIMITER $$
CREATE PROCEDURE sp_audit_merchant(IN p_merchant_id BIGINT, IN p_audit_status VARCHAR(6))
BEGIN
    DECLARE v_province VARCHAR(20);
    SELECT province INTO v_province FROM merchant WHERE id = p_merchant_id;
    IF v_province IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Merchant not found'; END IF;
    IF p_audit_status = '已通过' AND v_province = '待完善' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Address incomplete, cannot approve';
    END IF;
    UPDATE merchant SET audit_status = p_audit_status WHERE id = p_merchant_id;
    UPDATE `user` SET status = IF(p_audit_status = '已通过', '正常', '禁用') WHERE id = p_merchant_id;
    INSERT INTO notification (user_id, event_type, title, content, ref_id) VALUES
        (p_merchant_id, 'merchant_audited', 'Audit result', CONCAT('Your merchant application was ', p_audit_status), CAST(p_merchant_id AS CHAR));
END$$
DELIMITER ;


-- ============================================================
-- 第3部分: 触发器 (Triggers) — 2个
-- ============================================================

-- -------------------------------------------------------
-- trg_dish_auto_offline — 库存归零自动下架
-- 触发: BEFORE UPDATE ON dish
-- 当 stock 被扣到 0 时，自动将 status 改为 '下架'
-- -------------------------------------------------------
DROP TRIGGER IF EXISTS trg_dish_auto_offline;
DELIMITER $$
CREATE TRIGGER trg_dish_auto_offline BEFORE UPDATE ON dish FOR EACH ROW
BEGIN
    IF NEW.stock = 0 AND NEW.status = '上架' THEN SET NEW.status = '下架'; END IF;
END$$
DELIMITER ;

-- -------------------------------------------------------
-- trg_order_update_sales — 订单完成时自动联动
-- 触发: AFTER UPDATE ON order_info
-- (1) 订单完成 → 商家月销量 +1
-- (2) 订单进入待配送 → 自动创建 delivery 记录
-- -------------------------------------------------------
DROP TRIGGER IF EXISTS trg_order_update_sales;
DELIMITER $$
CREATE TRIGGER trg_order_update_sales AFTER UPDATE ON order_info FOR EACH ROW
BEGIN
    IF NEW.status = '已完成' AND OLD.status != '已完成' THEN
        UPDATE merchant SET monthly_sales = monthly_sales + 1 WHERE id = NEW.merchant_id;
    END IF;
    IF NEW.status = '待配送' AND OLD.status != '待配送' THEN
        INSERT IGNORE INTO delivery (order_no, status) VALUES (NEW.order_no, '待取餐');
    END IF;
END$$
DELIMITER ;


-- ============================================================
-- 第4部分: 视图 (Views) — 7个
-- ============================================================

-- -------------------------------------------------------
-- v_order_detail — 订单详情（4表JOIN合一）
-- 替代: Java 代码多次查询 + 手动拼装
-- -------------------------------------------------------
CREATE OR REPLACE VIEW v_order_detail AS
SELECT
    o.order_no, o.customer_id, c.name AS customer_name,
    o.merchant_id, m.name AS merchant_name,
    o.address_id, CONCAT(a.province, a.city, a.district, a.detail) AS full_address,
    o.status, o.total_amount, o.remark, o.create_time,
    d.id AS delivery_id, d.rider_id, r.name AS rider_name,
    d.status AS delivery_status, d.pickup_time, d.deliver_time
FROM order_info o
JOIN customer c ON o.customer_id = c.id
JOIN merchant m ON o.merchant_id = m.id
LEFT JOIN address a ON o.address_id = a.id
LEFT JOIN delivery d ON o.order_no = d.order_no
LEFT JOIN rider r ON d.rider_id = r.id;

-- -------------------------------------------------------
-- v_merchant_stats — 商家统计（预聚合30天）
-- -------------------------------------------------------
CREATE OR REPLACE VIEW v_merchant_stats AS
SELECT
    m.id AS merchant_id, m.name AS merchant_name, m.biz_status, m.audit_status, m.monthly_sales,
    COUNT(o.order_no) AS total_orders,
    COALESCE(SUM(CASE WHEN o.status = '已完成' THEN o.total_amount ELSE 0 END), 0) AS total_revenue,
    COALESCE(SUM(CASE WHEN o.status = '已取消' THEN 1 ELSE 0 END), 0) AS cancelled_orders,
    COALESCE(ROUND(SUM(CASE WHEN o.status = '已完成' THEN 1 ELSE 0 END) / NULLIF(COUNT(o.order_no), 0) * 100, 1), 0) AS completion_rate,
    COUNT(DISTINCT o.order_no) AS order_count_30d
FROM merchant m
LEFT JOIN order_info o ON m.id = o.merchant_id AND o.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY m.id, m.name, m.biz_status, m.audit_status, m.monthly_sales;

-- -------------------------------------------------------
-- v_rider_performance — 骑手绩效
-- -------------------------------------------------------
CREATE OR REPLACE VIEW v_rider_performance AS
SELECT
    r.id AS rider_id, r.name AS rider_name, r.status AS current_status, r.total_deliveries,
    COUNT(d.id) AS deliveries_30d,
    COUNT(CASE WHEN d.status = '已送达' THEN 1 END) AS completed_30d,
    COALESCE(AVG(TIMESTAMPDIFF(MINUTE, d.pickup_time, d.deliver_time)), 0) AS avg_delivery_minutes,
    COUNT(CASE WHEN DATE(d.create_time) = CURDATE() THEN 1 END) AS today_deliveries
FROM rider r
LEFT JOIN delivery d ON r.id = d.rider_id AND d.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY r.id, r.name, r.status, r.total_deliveries;

-- -------------------------------------------------------
-- v_dish_sales — 菜品销量排行
-- -------------------------------------------------------
CREATE OR REPLACE VIEW v_dish_sales AS
SELECT
    d.id AS dish_id, d.name AS dish_name, d.merchant_id, m.name AS merchant_name,
    d.price, d.status,
    COALESCE(SUM(oi.quantity), 0) AS total_sold,
    COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total_revenue
FROM dish d
JOIN merchant m ON d.merchant_id = m.id
LEFT JOIN order_item oi ON d.id = oi.dish_id
LEFT JOIN order_info o ON oi.order_no = o.order_no AND o.status = '已完成'
GROUP BY d.id, d.name, d.merchant_id, m.name, d.price, d.status;

-- -------------------------------------------------------
-- v_daily_stats — 每日订单/营收统计
-- -------------------------------------------------------
CREATE OR REPLACE VIEW v_daily_stats AS
SELECT
    DATE(create_time) AS order_date,
    COUNT(*) AS order_count,
    SUM(CASE WHEN status = '已完成' THEN 1 ELSE 0 END) AS completed,
    SUM(CASE WHEN status = '已取消' THEN 1 ELSE 0 END) AS cancelled,
    COALESCE(SUM(CASE WHEN status = '已完成' THEN total_amount ELSE 0 END), 0) AS revenue
FROM order_info
GROUP BY DATE(create_time);

-- -------------------------------------------------------
-- v_customer_spending — 顾客消费统计
-- -------------------------------------------------------
CREATE OR REPLACE VIEW v_customer_spending AS
SELECT
    c.id AS customer_id, c.name AS customer_name,
    COUNT(o.order_no) AS total_orders,
    COALESCE(SUM(CASE WHEN o.status = '已完成' THEN o.total_amount ELSE 0 END), 0) AS total_spending,
    MAX(o.create_time) AS last_order_time,
    COUNT(CASE WHEN o.create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN 1 END) AS orders_30d
FROM customer c
LEFT JOIN order_info o ON c.id = o.customer_id
GROUP BY c.id, c.name;

-- -------------------------------------------------------
-- v_user_summary — 用户汇总（user + 角色子表姓名，替代3表JOIN）
-- Java: UserServiceImpl.listUsers() 已接入此View
-- -------------------------------------------------------
CREATE OR REPLACE VIEW v_user_summary AS
SELECT
    u.id, u.phone, u.role, u.status, u.create_time,
    CASE u.role
        WHEN 'CUSTOMER' THEN c.name
        WHEN 'MERCHANT' THEN m.name
        WHEN 'RIDER' THEN r.name
        ELSE '管理员'
    END AS display_name
FROM user u
LEFT JOIN customer c ON u.id = c.id AND u.role = 'CUSTOMER'
LEFT JOIN merchant m ON u.id = m.id AND u.role = 'MERCHANT'
LEFT JOIN rider r ON u.id = r.id AND u.role = 'RIDER';
