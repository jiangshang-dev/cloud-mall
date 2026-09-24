-- 售卖订单分期发货 + 商品分期字段
SET NAMES utf8mb4;

ALTER TABLE mall_product
  ADD COLUMN periods INT NOT NULL DEFAULT 1 COMMENT '分期发放期数' AFTER duration_days,
  ADD COLUMN redeem_url VARCHAR(255) NULL COMMENT '激活站地址' AFTER remark;

ALTER TABLE mall_order
  ADD COLUMN order_type VARCHAR(16) NOT NULL DEFAULT 'ACTIVATE' COMMENT 'SALE售卖/ACTIVATE激活' AFTER order_no,
  ADD COLUMN periods INT NULL COMMENT '总期数' AFTER installment_tip,
  ADD COLUMN issued_periods INT NULL DEFAULT 0 COMMENT '已发放期数' AFTER periods;

CREATE TABLE IF NOT EXISTS mall_order_installment (
  id            VARCHAR(32)  NOT NULL PRIMARY KEY,
  order_id      VARCHAR(32)  NOT NULL,
  order_no      VARCHAR(64)  NOT NULL,
  period_no     INT          NOT NULL COMMENT '第几期',
  cdk_id        VARCHAR(32)  NULL,
  cdk_code      VARCHAR(64)  NULL,
  status        VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ISSUED/ACTIVATED',
  expect_time   DATETIME     NULL,
  issue_time    DATETIME     NULL,
  remark        VARCHAR(255) NULL,
  create_time   DATETIME     NULL,
  update_time   DATETIME     NULL,
  UNIQUE KEY uk_order_period (order_id, period_no),
  KEY idx_inst_order (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单分期发货';

UPDATE mall_product SET periods = 1, redeem_url = 'http://127.0.0.1:5173/recharge' WHERE id IN ('p_plus_1m','p_go_1m','p_5x','p_20x','p_codex','p_ready_acc');

INSERT INTO mall_product (id, name, product_type, category, price, duration_days, periods, status, remark, redeem_url, create_time)
VALUES ('p_plus_3m', '3个月 Plus', 'Plus', 'AI_SUB', 517.93, 90, 3, 'ON', '分三期发放激活卡密', 'http://127.0.0.1:5173/recharge', NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), price=VALUES(price), periods=VALUES(periods), redeem_url=VALUES(redeem_url);

-- 为 3 个月 Plus 预置激活卡密库存
INSERT INTO mall_cdk (id, code, product_id, status, expire_time, create_time) VALUES
('cdk_p3_01', 'ZDS_3XDJG3BJJVPLCTHM5HX', 'p_plus_3m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_p3_02', 'ZDS_3XDJG3BJJVPLCTHM5HY', 'p_plus_3m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_p3_03', 'ZDS_3XDJG3BJJVPLCTHM5HZ', 'p_plus_3m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_p3_04', 'ZDS_DEMO_PLUS3M_0004', 'p_plus_3m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_p3_05', 'ZDS_DEMO_PLUS3M_0005', 'p_plus_3m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_p3_06', 'ZDS_DEMO_PLUS3M_0006', 'p_plus_3m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW())
ON DUPLICATE KEY UPDATE status=VALUES(status);
