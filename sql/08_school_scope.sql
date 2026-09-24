-- 学校隔离：字典、认证、内容表 school_id 及历史数据回填

-- school_auth 记录用户选择的学校
ALTER TABLE school_auth ADD COLUMN school_id BIGINT NULL COMMENT '用户选择的学校ID，手填时为NULL' AFTER user_id;

-- 内容表冗余 school_id（发布时快照）
ALTER TABLE square_post ADD COLUMN school_id BIGINT NULL COMMENT '所属学校ID' AFTER user_id;
ALTER TABLE errand_order ADD COLUMN school_id BIGINT NULL COMMENT '所属学校ID' AFTER publisher_id;
ALTER TABLE second_goods ADD COLUMN school_id BIGINT NULL COMMENT '所属学校ID' AFTER seller_id;
ALTER TABLE game_team ADD COLUMN school_id BIGINT NULL COMMENT '所属学校ID' AFTER creator_id;

-- 学校名称唯一（忽略已存在重复时再手动处理）
-- ALTER TABLE school_info ADD UNIQUE INDEX uk_school_name (school_name);

-- 列表查询索引
CREATE INDEX idx_square_post_school ON square_post (school_id, status, create_time);
CREATE INDEX idx_errand_order_school ON errand_order (school_id, status, create_time);
CREATE INDEX idx_second_goods_school ON second_goods (school_id, status, create_time);
CREATE INDEX idx_game_team_school ON game_team (school_id, create_time);
CREATE INDEX idx_app_user_school_id ON app_user (school_id);

-- 用户 school_id 回填（按学校名匹配字典）
UPDATE app_user u
INNER JOIN school_info s ON u.school = s.school_name
SET u.school_id = s.id
WHERE u.school_verified = 1 AND (u.school_id IS NULL OR u.school_id = 0);

-- 内容 school_id 按发布者回填
UPDATE square_post p
INNER JOIN app_user u ON p.user_id = u.id
SET p.school_id = u.school_id
WHERE p.school_id IS NULL AND u.school_id IS NOT NULL;

UPDATE errand_order o
INNER JOIN app_user u ON o.publisher_id = u.id
SET o.school_id = u.school_id
WHERE o.school_id IS NULL AND u.school_id IS NOT NULL;

UPDATE second_goods g
INNER JOIN app_user u ON g.seller_id = u.id
SET g.school_id = u.school_id
WHERE g.school_id IS NULL AND u.school_id IS NOT NULL;

UPDATE game_team t
INNER JOIN app_user u ON t.creator_id = u.id
SET t.school_id = u.school_id
WHERE t.school_id IS NULL AND u.school_id IS NOT NULL;
