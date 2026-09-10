-- 清理商城食品类数据 部署时执行
DELETE FROM mall_cart WHERE goods_id IN (SELECT id FROM mall_goods WHERE name LIKE '%零食%' OR name LIKE '%食品%' OR name LIKE '%饮品%' OR name LIKE '%饮料%' OR name LIKE '%大礼包%');
DELETE FROM mall_order_item WHERE goods_name LIKE '%零食%' OR goods_name LIKE '%食品%' OR goods_name LIKE '%饮品%' OR goods_name LIKE '%饮料%' OR goods_name LIKE '%大礼包%';
DELETE FROM mall_goods WHERE name LIKE '%零食%' OR name LIKE '%食品%' OR name LIKE '%饮品%' OR name LIKE '%饮料%' OR name LIKE '%大礼包%' OR detail LIKE '%零食%';
DELETE FROM mall_category WHERE name LIKE '%零食%' OR name LIKE '%食品%' OR name LIKE '%饮品%' OR name LIKE '%饮料%';
