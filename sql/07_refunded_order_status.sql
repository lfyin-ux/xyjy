-- 将已退款订单状态修正为「退款售后」（可重复执行）
UPDATE mall_order
SET status = 5
WHERE refund_status = 2 AND status <> 5;
