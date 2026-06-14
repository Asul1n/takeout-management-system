-- ==================== 外卖管理系统 数据库初始化脚本 ====================
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `takeout` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `takeout`;

-- ==================== 用户账号表 ====================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `phone`       CHAR(11)     NOT NULL                 COMMENT '手机号（登录账号）',
    `password`    VARCHAR(128) NOT NULL                 COMMENT '密码（bcrypt加密）',
    `role`        VARCHAR(8)   NOT NULL                 COMMENT '用户身份: ADMIN/MERCHANT/CUSTOMER/RIDER',
    `status`      VARCHAR(4)   NOT NULL DEFAULT '正常'   COMMENT '账号状态: 正常/禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户账号表';

-- ==================== 顾客信息表 ====================
CREATE TABLE IF NOT EXISTS `customer` (
    `id`              BIGINT       NOT NULL COMMENT '顾客ID（关联 user.id）',
    `name`            VARCHAR(20)  NOT NULL COMMENT '姓名',
    `phone`           CHAR(11)     NOT NULL COMMENT '手机号',
    `default_address_id` BIGINT   DEFAULT NULL COMMENT '默认配送地址ID',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_default_address` (`default_address_id`),
    CONSTRAINT `fk_customer_user` FOREIGN KEY (`id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='顾客信息表';

-- ==================== 配送地址表 ====================
CREATE TABLE IF NOT EXISTS `address` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '地址ID',
    `customer_id` BIGINT       NOT NULL                COMMENT '顾客ID',
    `province`    VARCHAR(20)  NOT NULL                COMMENT '省',
    `city`        VARCHAR(20)  NOT NULL                COMMENT '市',
    `district`    VARCHAR(20)  NOT NULL                COMMENT '区',
    `detail`      VARCHAR(100) NOT NULL                COMMENT '详细地址',
    `is_default`  TINYINT      NOT NULL DEFAULT 0      COMMENT '是否默认: 0否/1是',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_customer_id` (`customer_id`),
    CONSTRAINT `fk_address_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送地址表';

-- ==================== 商家信息表 ====================
CREATE TABLE IF NOT EXISTS `merchant` (
    `id`            BIGINT       NOT NULL COMMENT '商家ID（关联 user.id）',
    `name`          VARCHAR(50)  NOT NULL                COMMENT '商家名称',
    `phone`         CHAR(11)     NOT NULL                COMMENT '联系电话',
    `province`      VARCHAR(20)  NOT NULL                COMMENT '省',
    `city`          VARCHAR(20)  NOT NULL                COMMENT '市',
    `district`      VARCHAR(20)  NOT NULL                COMMENT '区',
    `address_detail` VARCHAR(100) NOT NULL               COMMENT '详细地址',
    `open_time`     CHAR(5)      DEFAULT '09:00'         COMMENT '营业开始时间',
    `close_time`    CHAR(5)      DEFAULT '21:00'         COMMENT '营业结束时间',
    `notice`        VARCHAR(500) DEFAULT NULL             COMMENT '商家公告',
    `biz_status`    VARCHAR(6)   NOT NULL DEFAULT '营业中'  COMMENT '营业状态: 营业中/休息中',
    `audit_status`  VARCHAR(6)   NOT NULL DEFAULT '待审核'  COMMENT '审核状态: 待审核/已通过/已驳回',
    `monthly_sales` INT          NOT NULL DEFAULT 0       COMMENT '月销量（近30天完成订单数）',
    `rating`        DECIMAL(2,1) DEFAULT NULL             COMMENT '商家综合评分（预留）',
    `auto_accept`   TINYINT      NOT NULL DEFAULT 0       COMMENT '是否自动接单: 0否/1是',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_audit_status` (`audit_status`),
    CONSTRAINT `fk_merchant_user` FOREIGN KEY (`id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家信息表';

-- ==================== 菜品分类表 ====================
CREATE TABLE IF NOT EXISTS `category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `merchant_id` BIGINT      NOT NULL                COMMENT '商家ID',
    `name`        VARCHAR(20) NOT NULL                COMMENT '分类名称',
    `sort`        INT         NOT NULL DEFAULT 0      COMMENT '排序序号',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    CONSTRAINT `fk_category_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品分类表';

-- ==================== 菜品信息表 ====================
CREATE TABLE IF NOT EXISTS `dish` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '菜品ID',
    `merchant_id` BIGINT        NOT NULL                COMMENT '商家ID',
    `category_id` BIGINT        NOT NULL                COMMENT '分类ID',
    `name`        VARCHAR(50)   NOT NULL                COMMENT '菜品名称',
    `price`       DECIMAL(10,2) NOT NULL                COMMENT '价格（元）',
    `description` VARCHAR(500)  DEFAULT NULL             COMMENT '描述',
    `image_url`   VARCHAR(255)  DEFAULT NULL             COMMENT '图片URL',
    `stock`       INT           NOT NULL DEFAULT 0       COMMENT '库存量',
    `status`      VARCHAR(4)    NOT NULL DEFAULT '上架'   COMMENT '上架状态: 上架/下架',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_merchant_name` (`merchant_id`, `name`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_dish_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant`(`id`),
    CONSTRAINT `fk_dish_category` FOREIGN KEY (`category_id`) REFERENCES `category`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品信息表';

-- ==================== 订单信息表 ====================
CREATE TABLE IF NOT EXISTS `order_info` (
    `order_no`    CHAR(14)       NOT NULL                COMMENT '订单编号: YYYYMMDD + 6位流水',
    `customer_id` BIGINT         NOT NULL                COMMENT '顾客ID',
    `merchant_id` BIGINT         NOT NULL                COMMENT '商家ID',
    `status`      VARCHAR(8)     NOT NULL DEFAULT '已提交'  COMMENT '订单状态',
    `total_amount` DECIMAL(12,2) NOT NULL                COMMENT '总金额（元）',
    `remark`      VARCHAR(200)   DEFAULT NULL             COMMENT '备注',
    `address_id`  BIGINT         NOT NULL                COMMENT '配送地址ID',
    `payment_method` VARCHAR(16) DEFAULT NULL            COMMENT '支付方式（预留）: alipay/wechat/cash',
    `payment_status` VARCHAR(8)  DEFAULT NULL            COMMENT '支付状态（预留）: pending/paid/refunded',
    `payment_time` DATETIME      DEFAULT NULL            COMMENT '支付时间（预留）',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`order_no`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_address_id` (`address_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_order_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer`(`id`),
    CONSTRAINT `fk_order_merchant` FOREIGN KEY (`merchant_id`) REFERENCES `merchant`(`id`),
    CONSTRAINT `fk_order_address` FOREIGN KEY (`address_id`) REFERENCES `address`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息表';

-- ==================== 订单明细表 ====================
CREATE TABLE IF NOT EXISTS `order_item` (
    `id`          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_no`    CHAR(14)       NOT NULL                COMMENT '订单编号',
    `dish_id`     BIGINT         NOT NULL                COMMENT '菜品ID',
    `dish_name`   VARCHAR(50)    NOT NULL                COMMENT '菜品名称（快照）',
    `unit_price`  DECIMAL(10,2)  NOT NULL                COMMENT '成交单价（快照）',
    `quantity`    INT            NOT NULL                COMMENT '数量',
    `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order_no` (`order_no`),
    CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_no`) REFERENCES `order_info`(`order_no`),
    CONSTRAINT `fk_order_item_dish`  FOREIGN KEY (`dish_id`)  REFERENCES `dish`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ==================== 骑手信息表 ====================
CREATE TABLE IF NOT EXISTS `rider` (
    `id`              BIGINT      NOT NULL COMMENT '骑手ID（关联 user.id）',
    `name`            VARCHAR(20) NOT NULL                COMMENT '姓名',
    `phone`           CHAR(11)    NOT NULL                COMMENT '手机号',
    `status`          VARCHAR(6)  NOT NULL DEFAULT '空闲'   COMMENT '配送状态: 空闲/配送中/离线',
    `total_deliveries` INT        NOT NULL DEFAULT 0       COMMENT '累计配送单数',
    `create_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_rider_user` FOREIGN KEY (`id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='骑手信息表';

-- ==================== 配送信息表 ====================
CREATE TABLE IF NOT EXISTS `delivery` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '配送ID',
    `order_no`     CHAR(14)    NOT NULL                COMMENT '订单编号',
    `rider_id`     BIGINT      DEFAULT NULL             COMMENT '骑手ID',
    `status`       VARCHAR(8)  NOT NULL DEFAULT '待取餐'  COMMENT '配送状态: 待取餐/配送中/已送达',
    `pickup_time`  DATETIME    DEFAULT NULL             COMMENT '取餐时间',
    `deliver_time` DATETIME    DEFAULT NULL             COMMENT '送达时间',
    `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_rider_id` (`rider_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_delivery_order` FOREIGN KEY (`order_no`) REFERENCES `order_info`(`order_no`),
    CONSTRAINT `fk_delivery_rider` FOREIGN KEY (`rider_id`) REFERENCES `rider`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='配送信息表';

-- ==================== 购物车表 ====================
CREATE TABLE IF NOT EXISTS `cart_item` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '购物车项ID',
    `customer_id` BIGINT   NOT NULL                COMMENT '顾客ID',
    `dish_id`     BIGINT   NOT NULL                COMMENT '菜品ID',
    `quantity`    INT      NOT NULL DEFAULT 1      COMMENT '数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_customer_dish` (`customer_id`, `dish_id`),
    KEY `idx_customer_id` (`customer_id`),
    CONSTRAINT `fk_cart_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer`(`id`),
    CONSTRAINT `fk_cart_dish` FOREIGN KEY (`dish_id`) REFERENCES `dish`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ==================== 通知记录表 ====================
CREATE TABLE IF NOT EXISTS `notification` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '接收用户ID',
    `event_type`  VARCHAR(32)  NOT NULL                COMMENT '事件类型: order_new/order_accepted/order_ready/delivery_accepted/delivery_arrived/merchant_audited',
    `title`       VARCHAR(100) NOT NULL                COMMENT '通知标题',
    `content`     VARCHAR(500) NOT NULL                COMMENT '通知内容',
    `ref_id`      VARCHAR(64)  DEFAULT NULL            COMMENT '关联对象ID（如订单编号）',
    `is_read`     TINYINT      NOT NULL DEFAULT 0      COMMENT '是否已读: 0未读/1已读',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    CONSTRAINT `fk_notification_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录表';

-- ==================== 操作日志表 ====================
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`     BIGINT        DEFAULT NULL             COMMENT '操作用户ID',
    `user_role`   VARCHAR(8)    DEFAULT NULL             COMMENT '操作用户身份',
    `operation`   VARCHAR(32)   NOT NULL                 COMMENT '操作类型',
    `target_type` VARCHAR(32)   DEFAULT NULL             COMMENT '操作对象类型: ORDER/DISH/MERCHANT/USER/DELIVERY',
    `target_id`   VARCHAR(64)   DEFAULT NULL             COMMENT '操作对象ID',
    `detail`      VARCHAR(500)  DEFAULT NULL             COMMENT '操作详情（摘要）',
    `ip`          VARCHAR(45)   DEFAULT NULL             COMMENT '操作IP',
    `user_agent`  VARCHAR(255)  DEFAULT NULL             COMMENT '用户代理',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_target` (`target_type`, `target_id`),
    CONSTRAINT `fk_log_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
