-- 学校切换与跨校消费解锁

ALTER TABLE app_user ADD COLUMN current_school_id BIGINT NULL COMMENT '当前浏览的学校ID' AFTER school_id;
ALTER TABLE app_user ADD COLUMN mall_total_spent DECIMAL(12,2) DEFAULT 0 COMMENT '商城累计有效消费金额' AFTER current_school_id;

-- 默认当前学校 = 认证学校
UPDATE app_user
SET current_school_id = school_id
WHERE current_school_id IS NULL AND school_id IS NOT NULL;

-- 按已支付且未退款订单回填累计消费
UPDATE app_user u
SET mall_total_spent = (
  SELECT COALESCE(SUM(o.total_amount), 0)
  FROM mall_order o
  WHERE o.user_id = u.id
    AND o.status IN (2, 3, 4)
    AND (o.refund_status IS NULL OR o.refund_status != 2)
);
