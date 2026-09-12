-- 退款申请表迁移（可重复执行）
SET @db = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.TABLES
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'refund_apply') = 0,
  'CREATE TABLE refund_apply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT ''主键'',
    order_id BIGINT NOT NULL COMMENT ''订单ID'',
    order_no VARCHAR(50) NOT NULL COMMENT ''订单号'',
    user_id BIGINT NOT NULL COMMENT ''用户ID'',
    item_id BIGINT NOT NULL COMMENT ''订单明细ID'',
    goods_name VARCHAR(100) COMMENT ''商品名称'',
    spec VARCHAR(50) COMMENT ''规格'',
    quantity INT DEFAULT 1 COMMENT ''退货数量'',
    refund_amount DECIMAL(10,2) DEFAULT 0 COMMENT ''退款金额'',
    reason VARCHAR(500) COMMENT ''退款原因'',
    status TINYINT DEFAULT 0 COMMENT ''状态 0申请中 1已同意 2已拒绝'',
    reject_reason VARCHAR(500) COMMENT ''拒绝原因'',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'',
    KEY idx_order_id (order_id),
    KEY idx_user_id (user_id),
    KEY idx_item_id (item_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''退款申请表''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
