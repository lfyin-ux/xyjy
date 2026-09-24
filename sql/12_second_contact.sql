-- 二手商品增加卖家联系方式
SET @db = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'second_goods' AND COLUMN_NAME = 'seller_contact') = 0,
  'ALTER TABLE second_goods ADD COLUMN seller_contact VARCHAR(100) COMMENT ''卖家联系方式'' AFTER images',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
