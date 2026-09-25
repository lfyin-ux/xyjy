-- 清空业务测试数据，保留管理员、学校字典、商城商品、词库等配置
USE xyjy;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE personal_verify_daily;
TRUNCATE TABLE personal_auth;
TRUNCATE TABLE school_auth;
TRUNCATE TABLE user_photo;
TRUNCATE TABLE user_like;
TRUNCATE TABLE user_follow;
TRUNCATE TABLE user_skip;
TRUNCATE TABLE user_match;
TRUNCATE TABLE user_visit;
TRUNCATE TABLE user_blacklist;
TRUNCATE TABLE post_like;
TRUNCATE TABLE post_comment;
TRUNCATE TABLE square_post;
TRUNCATE TABLE chat_message;
TRUNCATE TABLE chat_session;
TRUNCATE TABLE errand_order;
TRUNCATE TABLE second_goods;
TRUNCATE TABLE game_team_member;
TRUNCATE TABLE game_team;
TRUNCATE TABLE mall_cart;
TRUNCATE TABLE mall_order_item;
TRUNCATE TABLE refund_apply;
TRUNCATE TABLE mall_order;
TRUNCATE TABLE user_address;
TRUNCATE TABLE filter_hit_log;
TRUNCATE TABLE report_record;
TRUNCATE TABLE violation_record;
TRUNCATE TABLE user_feedback;
TRUNCATE TABLE app_user;

UPDATE topic_tag SET post_count = 0;

SET FOREIGN_KEY_CHECKS = 1;
