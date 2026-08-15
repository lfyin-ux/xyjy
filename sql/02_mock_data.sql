-- 校园社交平台 Mock 演示数据
-- 说明：为每个功能点补充演示数据，密码均为明文演示用途

USE xyjy;
SET NAMES utf8mb4;

-- 管理员数据 密码 admin123
INSERT INTO sys_admin (id, username, password, nickname, phone, status) VALUES
(1, 'admin', 'admin123', '超级管理员', '13800000000', 1),
(2, 'auditor', 'admin123', '审核员小王', '13800000001', 1),
(3, 'service', 'admin123', '客服小李', '13800000002', 1);

-- 角色数据
INSERT INTO sys_role (id, role_name, role_code, remark) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有全部权限'),
(2, '审核员', 'AUDITOR', '负责内容与认证审核'),
(3, '客服', 'SERVICE', '处理举报与反馈'),
(4, '运营', 'OPERATOR', '数据运营');

-- 管理员角色关联
INSERT INTO sys_admin_role (admin_id, role_id) VALUES
(1, 1), (2, 2), (3, 3);

-- 权限菜单
INSERT INTO sys_permission (id, perm_name, perm_code, parent_id, sort) VALUES
(1, '用户管理', 'user:manage', 0, 1),
(2, '内容审核', 'content:audit', 0, 2),
(3, '词库管理', 'filter:manage', 0, 3),
(4, '校园广场管理', 'square:manage', 0, 4),
(5, '校园生活管理', 'life:manage', 0, 5),
(6, '商城管理', 'mall:manage', 0, 6),
(7, '数据统计', 'stat:view', 0, 7),
(8, '系统管理', 'sys:manage', 0, 8);

-- 角色权限关联 超级管理员拥有全部
INSERT INTO sys_role_permission (role_id, perm_id) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),
(2,2),(2,3),(2,4),
(3,2),(3,4);

-- 学校字典
INSERT INTO school_info (id, school_name, province, city) VALUES
(1, '华南理工大学', '广东省', '广州市'),
(2, '中山大学', '广东省', '广州市'),
(3, '暨南大学', '广东省', '广州市'),
(4, '深圳大学', '广东省', '深圳市');

-- 用户数据 全部华南理工大学 已双认证
INSERT INTO app_user (id, openid, nickname, avatar, gender, birthday, age, height, intro, school, campus, college, grade, student_no, tags, partner_type, expect, phone, identity_verified, school_verified, school_id, avatar_audit_status, intro_audit_status, status) VALUES
(1, 'openid_001', '林小满', '/uploads/avatar/u1.png', 2, '2003-05-12', 21, 165, '喜欢用镜头记录生活，希望遇到能一起看展、散步和分享日常的人。', '华南理工大学', '五山校区', '设计学院', '大三', '20210001', '摄影,音乐节,探店', '看展搭子,旅行搭子', '认真交友', '13800001001', 1, 1, 1, 1, 1, 1),
(2, 'openid_002', '周予安', '/uploads/avatar/u2.png', 1, '2002-08-20', 22, 180, '热爱摄影和篮球，想找志同道合的朋友。', '华南理工大学', '五山校区', '计算机学院', '大四', '20200002', '摄影,篮球,编程', '运动搭子', '扩列交友', '13800001002', 1, 1, 1, 1, 1, 1),
(3, 'openid_003', '沈知夏', '/uploads/avatar/u3.png', 2, '2003-11-03', 21, 168, '艺术生一枚，喜欢逛美术馆和手冲咖啡。', '华南理工大学', '大学城校区', '艺术学院', '大三', '20210003', '绘画,咖啡,展览', '看展搭子', '认真交友', '13800001003', 1, 1, 1, 1, 1, 1),
(4, 'openid_004', '阿凯同学', '/uploads/avatar/u4.png', 1, '2001-03-15', 23, 178, '羽毛球爱好者，周末常约球。', '华南理工大学', '五山校区', '体育学院', '研一', '20240004', '羽毛球,健身,游泳', '运动搭子', '找球友', '13800001004', 1, 1, 1, 1, 1, 1),
(5, 'openid_005', '栗子', '/uploads/avatar/u5.png', 2, '2004-01-22', 20, 160, '喜欢散步、胶片和落日，安静的女孩。', '华南理工大学', '五山校区', '文学院', '大二', '20220005', '摄影,散步,阅读', '学习搭子', '慢慢了解', '13800001005', 1, 1, 1, 1, 1, 1),
(6, 'openid_006', '小林', '/uploads/avatar/u6.png', 1, '2003-07-07', 21, 175, '游戏迷，主玩MOBA和FPS。', '华南理工大学', '大学城校区', '软件学院', '大三', '20210006', '游戏,动漫,音乐', '游戏搭子', '一起上分', '13800001006', 1, 1, 1, 0, 0, 1),
(7, 'openid_007', '苏晓', '/uploads/avatar/u7.png', 2, '2002-09-18', 22, 163, '考研中，需要一起自习的伙伴。', '华南理工大学', '五山校区', '经管学院', '大四', '20200007', '阅读,考研,咖啡', '学习搭子', '互相监督', '13800001007', 1, 1, 1, 1, 1, 2),
(8, 'openid_008', '陈默', '/uploads/avatar/u8.png', 1, '2003-12-01', 21, 182, '音乐制作人，玩吉他和电子乐。', '华南理工大学', '五山校区', '音乐学院', '大三', '20210008', '音乐,吉他,livehouse', '玩乐搭子', '组乐队', '13800001008', 1, 0, 1, 0, 1, 1);

-- 个人认证数据
INSERT INTO personal_auth (user_id, phone, real_name, id_card, id_front_img, id_back_img, status) VALUES
(1, '13800001001', '林某某', '44010620030512****', '/uploads/idcard/f1.png', '/uploads/idcard/b1.png', 2),
(2, '13800001002', '周某某', '44010620020820****', '/uploads/idcard/f2.png', '/uploads/idcard/b2.png', 2),
(3, '13800001003', '沈某某', '44010620031103****', '/uploads/idcard/f3.png', '/uploads/idcard/b3.png', 2),
(8, '13800001008', '陈某某', '44010620031201****', '/uploads/idcard/f8.png', '/uploads/idcard/b8.png', 1);

-- 学校认证数据
INSERT INTO school_auth (user_id, school_name, college, grade, student_no, doc_type, doc_imgs, status) VALUES
(1, '华南理工大学', '设计学院', '大三', '20210001', '学生证', '/uploads/school/s1.png', 2),
(2, '华南理工大学', '计算机学院', '大四', '20200002', '校园卡', '/uploads/school/s2.png', 2),
(3, '华南理工大学', '艺术学院', '大三', '20210003', '学生证', '/uploads/school/s3.png', 2),
(8, '华南理工大学', '音乐学院', '大三', '20210008', '录取通知书', '/uploads/school/s8.png', 1);

-- 用户相册
INSERT INTO user_photo (user_id, img_url, sort, audit_status) VALUES
(1, '/uploads/photo/p1_1.png', 1, 1),
(1, '/uploads/photo/p1_2.png', 2, 1),
(2, '/uploads/photo/p2_1.png', 1, 1),
(3, '/uploads/photo/p3_1.png', 1, 1),
(3, '/uploads/photo/p3_2.png', 2, 0);

-- 喜欢记录 形成双向匹配
INSERT INTO user_like (user_id, target_id, type) VALUES
(1, 2, 1),
(2, 1, 1),
(1, 3, 2),
(3, 1, 1),
(4, 1, 1),
(5, 2, 1);

-- 匹配数据
INSERT INTO user_match (user_a, user_b, status) VALUES
(1, 2, 1),
(1, 3, 1);

-- 跳过记录
INSERT INTO user_skip (user_id, target_id) VALUES
(1, 6);

-- 访客记录
INSERT INTO user_visit (user_id, target_id) VALUES
(2, 1), (3, 1), (4, 1), (5, 1);

-- 黑名单
INSERT INTO user_blacklist (user_id, target_id) VALUES
(1, 6);

-- 校园广场动态
INSERT INTO square_post (id, user_id, content, images, topic, place, like_count, comment_count, status) VALUES
(1, 4, '周五晚上缺一个羽毛球搭子，水平不限，一起流汗就好！', '/uploads/post/post1.png', '运动', '大学生活动中心', 26, 1, 3),
(2, 5, '最近拍到的晚霞。想认识也喜欢散步、胶片和落日的朋友～', '/uploads/post/post2.png', '摄影', '图书馆天台', 58, 0, 3),
(3, 2, '计算机学院的同学有一起做项目的吗？前端后端都可以聊。', '', '学习', '西二教学楼', 12, 2, 3),
(4, 3, '发现一家超好喝的手冲咖啡店，求同好一起去探店！', '/uploads/post/post4.png', '探店', '五山地铁站', 34, 1, 1),
(5, 8, '想组个校园乐队，会吉他鼓贝斯的朋友快来！', '', '音乐', '音乐学院琴房', 8, 0, 3);

-- 动态点赞
INSERT INTO post_like (post_id, user_id) VALUES
(1, 1), (1, 2), (2, 1), (2, 3), (3, 1);

-- 动态评论
INSERT INTO post_comment (post_id, user_id, content, status) VALUES
(1, 1, '我可以！几点开始？', 3),
(3, 1, '我对前端感兴趣，可以聊聊', 3),
(3, 5, '带我一个', 3),
(4, 1, '求地址！', 3);

-- 聊天会话
INSERT INTO chat_session (id, user_a, user_b, last_msg, last_time, locked, initiator, status) VALUES
(1, 1, 2, '你好！看到你也喜欢摄影 👋', NOW(), 1, 2, 1),
(2, 1, 3, '周末要不要一起去看展？', NOW(), 0, 3, 1),
(3, 1, 4, '羽毛球馆见～', NOW(), 0, 1, 1);

-- 聊天消息
INSERT INTO chat_message (session_id, from_id, to_id, msg_type, content, is_read) VALUES
(1, 2, 1, 1, '你好！看到你也喜欢摄影 👋', 0),
(2, 3, 1, 1, '嗨，很高兴认识你 👋', 1),
(2, 1, 3, 1, '你好呀！', 1),
(2, 3, 1, 1, '周末要不要一起去看展？', 1),
(3, 1, 4, 1, '羽毛球馆见～', 1);

-- 校园跑腿订单
INSERT INTO errand_order (publisher_id, taker_id, title, content, from_place, to_place, finish_time, fee, status) VALUES
(1, NULL, '帮取东门快递 3 件', '三个快递，其中一个略重', '东门菜鸟驿站', '7 号宿舍楼', '40 分钟内', 8.00, 1),
(2, NULL, '食堂带一杯柠檬茶', '少冰少糖', '一食堂', '图书馆', '30 分钟内', 5.00, 1),
(3, 4, '打印资料并送到教室', 'A4 双面打印 20 页', '文印店', 'A3-204', '今天 15:00 前', 6.00, 2),
(5, 6, '代拿实验室钥匙', '', '行政楼', '实验楼', '今天内', 4.00, 3);

-- 二手商品
INSERT INTO second_goods (seller_id, name, category, condition_desc, price, description, images, status) VALUES
(1, '九成新 Sony 头戴耳机', '数码', '九成新', 399.00, '用了半年，音质很好，可当面验货', '/uploads/second/g1.png', 1),
(2, '考研英语真题全套', '书籍', '95新', 45.00, '几乎没怎么用，笔记很少', '/uploads/second/g2.png', 1),
(3, '宿舍小台灯', '生活', '全新', 25.00, '买多了一个，全新未拆', '/uploads/second/g3.png', 1),
(6, '游戏手柄 Xbox', '数码', '八成新', 180.00, '手感很好，无漂移', '/uploads/second/g4.png', 0);

-- 游戏组局
INSERT INTO game_team (creator_id, game_name, play_time, need_num, joined_num, require_desc, status) VALUES
(6, '王者荣耀 排位上分', '今晚 20:00', 5, 2, '钻石以上，有麦', 1),
(2, '英雄联盟 峡谷开黑', '周六下午', 5, 5, '不摆烂就行', 2),
(4, 'CSGO 竞技', '周日晚上', 5, 3, '有耳机能沟通', 1);

-- 组局成员
INSERT INTO game_team_member (team_id, user_id) VALUES
(1, 6), (1, 2), (3, 4), (3, 6), (3, 1);

-- 商城分类
INSERT INTO mall_category (id, name, sort) VALUES
(1, '文具好物', 1),
(2, '数码周边', 2),
(3, '生活日用', 3),
(4, '零食饮品', 4);

-- 商城商品
INSERT INTO mall_goods (id, category_id, name, cover, images, price, spec, stock, sales, detail, status) VALUES
(1, 3, '治愈系毛绒挂件', '/uploads/mall/m1.png', '/uploads/mall/m1.png', 39.00, '粉色,蓝色,米白', 100, 56, '柔软亲肤，今日可发货，多种颜色可选', 1),
(2, 3, '随行保温咖啡杯', '/uploads/mall/m2.png', '/uploads/mall/m2.png', 59.00, '白色,黑色', 80, 34, '轻巧便携，两种颜色，保温6小时', 1),
(3, 1, '简约风笔记本套装', '/uploads/mall/m3.png', '/uploads/mall/m3.png', 29.90, 'A5,B5', 200, 120, '4本装，护眼纸张', 1),
(4, 2, '无线蓝牙耳机', '/uploads/mall/m4.png', '/uploads/mall/m4.png', 129.00, '白色,黑色', 60, 88, '降噪长续航，校园限定', 1),
(5, 4, '校园联名零食大礼包', '/uploads/mall/m5.png', '/uploads/mall/m5.png', 49.00, '标准装', 150, 210, '多款零食组合，宿舍必备', 1);

-- 购物车
INSERT INTO mall_cart (user_id, goods_id, spec, quantity) VALUES
(1, 1, '粉色', 2),
(1, 3, 'A5', 1);

-- 商城订单
INSERT INTO mall_order (id, order_no, user_id, total_amount, status, address, receiver, phone, pay_time) VALUES
(1, 'DD20260810001', 1, 78.00, 4, '华南理工大学7号宿舍楼', '林小满', '13800001001', '2026-08-10 12:00:00'),
(2, 'DD20260812002', 1, 59.00, 3, '华南理工大学7号宿舍楼', '林小满', '13800001001', '2026-08-12 09:30:00'),
(3, 'DD20260814003', 2, 129.00, 1, '华南理工大学计算机学院', '周予安', '13800001002', NULL);

-- 商城订单明细
INSERT INTO mall_order_item (order_id, goods_id, goods_name, goods_cover, spec, price, quantity) VALUES
(1, 1, '治愈系毛绒挂件', '/uploads/mall/m1.png', '粉色', 39.00, 2),
(2, 2, '随行保温咖啡杯', '/uploads/mall/m2.png', '白色', 59.00, 1),
(3, 4, '无线蓝牙耳机', '/uploads/mall/m4.png', '白色', 129.00, 1);

-- 过滤词库
INSERT INTO filter_word (word, word_type, category, risk_level, match_type, scope, tip, enabled) VALUES
('赌博', 1, '违法', 3, 1, '动态,评论,聊天,简介', '内容包含违规信息，禁止发布', 1),
('诈骗', 1, '违法', 3, 1, '动态,评论,聊天,简介', '内容包含违规信息，禁止发布', 1),
('代刷', 1, '广告', 2, 2, '动态,评论,聊天', '内容涉嫌违规广告', 1),
('约炮', 1, '色情', 3, 1, '动态,评论,聊天,简介', '内容违规，禁止发布', 1),
('兼职', 2, '广告', 1, 2, '动态,评论', '', 1),
('加微信', 2, '广告', 1, 2, '动态,评论,聊天', '', 1),
('代购', 2, '广告', 1, 2, '动态,评论', '', 1),
('刷单', 1, '广告', 2, 2, '动态,评论,聊天', '内容涉嫌违规', 1);

-- 过滤词命中记录
INSERT INTO filter_hit_log (word, content, user_id, biz_type, handle_result) VALUES
('兼职', '有没有想做兼职的同学', 6, '动态', '转人工审核'),
('加微信', '加微信详聊', 6, '聊天', '拦截');

-- 举报记录
INSERT INTO report_record (reporter_id, target_type, target_id, reason, status) VALUES
(1, 'post', 5, '疑似广告信息', 0),
(2, 'user', 6, '发布不良内容', 0),
(3, 'comment', 3, '言语不当', 1);

-- 违规记录
INSERT INTO violation_record (user_id, type, reason, admin_name) VALUES
(6, 'warn', '发布疑似广告内容', '审核员小王'),
(7, 'limit', '多次发送敏感信息', '审核员小王');

-- 意见反馈
INSERT INTO user_feedback (user_id, content, contact, status) VALUES
(1, '希望增加更多兴趣标签', '13800001001', 0),
(2, '匹配推荐能不能更精准一些', '13800001002', 0);

-- 话题标签
INSERT INTO topic_tag (name, post_count) VALUES
('运动', 15),
('摄影', 28),
('学习', 20),
('探店', 12),
('音乐', 9);
