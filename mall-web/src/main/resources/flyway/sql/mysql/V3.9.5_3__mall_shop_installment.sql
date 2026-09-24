-- 商城下单发卡密 + 分期发货

SET NAMES utf8mb4;

ALTER TABLE mall_product
  ADD COLUMN installment_count INT NOT NULL DEFAULT 1 COMMENT '分期期数' AFTER duration_days,
  ADD COLUMN usage_guide VARCHAR(512) NULL COMMENT '使用说明' AFTER remark,
  ADD COLUMN activate_url VARCHAR(255) NULL COMMENT '激活站地址' AFTER usage_guide;

ALTER TABLE mall_order
  ADD COLUMN order_source VARCHAR(16) NOT NULL DEFAULT 'SHOP' COMMENT 'SHOP购买/REDEEM激活' AFTER status,
  ADD COLUMN paid_time DATETIME NULL AFTER installment_tip;

CREATE TABLE IF NOT EXISTS mall_order_installment (
  id              VARCHAR(32)  NOT NULL PRIMARY KEY,
  order_id        VARCHAR(32)  NOT NULL,
  order_no        VARCHAR(64)  NOT NULL,
  period_no       INT          NOT NULL,
  total_periods   INT          NOT NULL,
  status          VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/DELIVERED',
  cdk_id          VARCHAR(32)  NULL,
  cdk_code        VARCHAR(64)  NULL,
  remark          VARCHAR(255) NULL,
  usage_guide     VARCHAR(512) NULL,
  schedule_time   DATETIME     NULL COMMENT '计划发放时间',
  deliver_time    DATETIME     NULL,
  create_time     DATETIME     NULL,
  update_time     DATETIME     NULL,
  KEY idx_inst_order (order_id),
  KEY idx_inst_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单分期发货';

-- 上架 3 个月 Plus（分 3 期发卡密）
INSERT INTO mall_product (id, name, product_type, category, price, duration_days, installment_count, status, remark, usage_guide, activate_url, create_time)
VALUES (
  'p_plus_3m',
  '3个月 Plus',
  'Plus',
  'AI_SUB',
  517.93,
  90,
  3,
  'ON',
  '商城上架商品',
  '请复制卡密前往激活站完成充值；若会员状态未更新请重新登录。',
  'http://127.0.0.1:5173/recharge',
  NOW()
) ON DUPLICATE KEY UPDATE name=VALUES(name), price=VALUES(price), installment_count=VALUES(installment_count);

UPDATE mall_product SET
  installment_count = 1,
  usage_guide = '请复制卡密前往激活站完成充值；若会员状态未更新请重新登录。',
  activate_url = 'http://127.0.0.1:5173/recharge'
WHERE id IN ('p_plus_1m','p_go_1m','p_5x','p_20x','p_codex','p_ready_acc');

-- 为 3 个月套餐准备库存卡密（商城发货用）
INSERT INTO mall_cdk (id, code, product_id, status, expire_time, create_time) VALUES
('cdk_shop_01', 'ZDS_3XDJG3BJJVPLCTHM5HX', 'p_plus_3m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_shop_02', 'ZDS_SHOP_PLUS3M_0002', 'p_plus_3m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_shop_03', 'ZDS_SHOP_PLUS3M_0003', 'p_plus_3m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_shop_04', 'ZDS_SHOP_PLUS3M_0004', 'p_plus_3m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_shop_05', 'ZDS_SHOP_PLUS3M_0005', 'p_plus_3m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW()),
('cdk_shop_06', 'ZDS_SHOP_PLUS3M_0006', 'p_plus_3m', 'UNUSED', DATE_ADD(NOW(), INTERVAL 365 DAY), NOW())
ON DUPLICATE KEY UPDATE product_id=VALUES(product_id);
