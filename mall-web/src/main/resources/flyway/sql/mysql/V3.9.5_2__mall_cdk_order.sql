-- 单体卡密/订单/交付任务（非 SaaS）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS mall_product (
  id            VARCHAR(32)  NOT NULL PRIMARY KEY,
  name          VARCHAR(128) NOT NULL COMMENT '商品名称',
  product_type  VARCHAR(32)  NOT NULL COMMENT 'Plus/Go/5x/20x/Codex',
  category      VARCHAR(32)  NOT NULL DEFAULT 'AI_SUB' COMMENT 'AI_SUB/READY_ACCOUNT',
  price         DECIMAL(12,2) NOT NULL DEFAULT 0,
  duration_days INT          NULL COMMENT '有效天数',
  status        VARCHAR(16)  NOT NULL DEFAULT 'ON' COMMENT 'ON/OFF',
  remark        VARCHAR(255) NULL,
  create_by     VARCHAR(64)  NULL,
  create_time   DATETIME     NULL,
  update_by     VARCHAR(64)  NULL,
  update_time   DATETIME     NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐商品';

CREATE TABLE IF NOT EXISTS mall_cdk (
  id            VARCHAR(32)  NOT NULL PRIMARY KEY,
  code          VARCHAR(64)  NOT NULL COMMENT '卡密',
  product_id    VARCHAR(32)  NOT NULL,
  status        VARCHAR(16)  NOT NULL DEFAULT 'UNUSED' COMMENT 'UNUSED/USED/EXPIRED/DISABLED',
  expire_time   DATETIME     NULL,
  used_by       VARCHAR(64)  NULL,
  used_time     DATETIME     NULL,
  create_by     VARCHAR(64)  NULL,
  create_time   DATETIME     NULL,
  update_by     VARCHAR(64)  NULL,
  update_time   DATETIME     NULL,
  UNIQUE KEY uk_mall_cdk_code (code),
  KEY idx_mall_cdk_product (product_id),
  KEY idx_mall_cdk_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='卡密';

CREATE TABLE IF NOT EXISTS mall_order (
  id              VARCHAR(32)  NOT NULL PRIMARY KEY,
  order_no        VARCHAR(64)  NOT NULL COMMENT '订单号',
  user_id         VARCHAR(64)  NOT NULL,
  product_id      VARCHAR(32)  NOT NULL,
  product_name    VARCHAR(128) NOT NULL,
  product_type    VARCHAR(32)  NOT NULL,
  category        VARCHAR(32)  NOT NULL DEFAULT 'AI_SUB',
  cdk_id          VARCHAR(32)  NULL,
  cdk_code        VARCHAR(64)  NULL,
  amount          DECIMAL(12,2) NOT NULL DEFAULT 0,
  status          VARCHAR(16)  NOT NULL DEFAULT 'PAID' COMMENT 'PAID/PROCESSING/COMPLETED/CANCELLED/REFUNDING/REFUNDED',
  installment_tip VARCHAR(128) NULL COMMENT '分期摘要展示',
  create_by       VARCHAR(64)  NULL,
  create_time     DATETIME     NULL,
  update_by       VARCHAR(64)  NULL,
  update_time     DATETIME     NULL,
  UNIQUE KEY uk_mall_order_no (order_no),
  KEY idx_mall_order_user (user_id),
  KEY idx_mall_order_status (status),
  KEY idx_mall_order_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换订单';

CREATE TABLE IF NOT EXISTS mall_delivery_task (
  id            VARCHAR(32)  NOT NULL PRIMARY KEY,
  order_id      VARCHAR(32)  NOT NULL,
  order_no      VARCHAR(64)  NOT NULL,
  status        VARCHAR(32)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/PENDING_MANUAL/SUCCESS/FAILED',
  progress      INT          NOT NULL DEFAULT 0,
  result_msg    VARCHAR(512) NULL,
  retry_count   INT          NOT NULL DEFAULT 0,
  create_by     VARCHAR(64)  NULL,
  create_time   DATETIME     NULL,
  update_by     VARCHAR(64)  NULL,
  update_time   DATETIME     NULL,
  KEY idx_mall_delivery_order (order_id),
  KEY idx_mall_delivery_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交付任务';

-- 演示商品
INSERT INTO mall_product (id, name, product_type, category, price, duration_days, status, remark, create_time)
VALUES
('p_plus_1m', '1个月 Plus', 'Plus', 'AI_SUB', 128.00, 30, 'ON', '演示套餐', NOW()),
('p_go_1m', '1个月 Go', 'Go', 'AI_SUB', 68.00, 30, 'ON', '演示套餐', NOW()),
('p_5x', '5x 额度包', '5x', 'AI_SUB', 99.00, NULL, 'ON', '演示套餐', NOW()),
('p_20x', '20x 额度包', '20x', 'AI_SUB', 299.00, NULL, 'ON', '演示套餐', NOW()),
('p_codex', 'Codex 额度', 'Codex', 'AI_SUB', 199.00, NULL, 'ON', '演示套餐', NOW()),
('p_ready_acc', '成品账号示例', 'Ready', 'READY_ACCOUNT', 50.00, 30, 'ON', '演示', NOW());

-- 演示卡密（可直接兑换）
INSERT INTO mall_cdk (id, code, product_id, status, expire_time, create_time) VALUES
('cdk_demo_01', 'PLUS-DEMO-0001-AAAA', 'p_plus_1m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_demo_02', 'PLUS-DEMO-0002-BBBB', 'p_plus_1m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_demo_03', 'GO-DEMO-0001-CCCC', 'p_go_1m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_demo_04', '5X-DEMO-0001-DDDD', 'p_5x', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_demo_05', '20X-DEMO-0001-EEEE', 'p_20x', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_demo_06', 'CODEX-DEMO-0001-FFFF', 'p_codex', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW());
