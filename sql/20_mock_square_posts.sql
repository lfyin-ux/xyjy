-- 校园广场演示动态（配图走本站 /uploads，小程序可直接加载）
USE xyjy;
SET NAMES utf8mb4;

DELETE FROM post_comment WHERE post_id IN (
  SELECT id FROM (SELECT id FROM square_post WHERE id BETWEEN 1001 AND 1012) t
);
DELETE FROM square_post WHERE id BETWEEN 1001 AND 1012;

INSERT INTO square_post (id, user_id, school_id, content, images, topic, place, like_count, comment_count, status, create_time) VALUES
(1001, 1, 1, '五山校区傍晚的云太好看了！从图书馆出来随手拍了几张，同校有喜欢摄影的姐妹可以一起扫街～', '/uploads/mock/square/sunset.jpg,/uploads/mock/square/mountains.jpg', '摄影', '图书馆广场', 42, 3, 3, NOW() - INTERVAL 2 HOUR),
(1002, 2, 1, '计院大三找队友做毕设展示页，偏前端 React/Vue 都可以，后端已有同学。周末可以线下碰一次需求。', '/uploads/mock/square/coding.jpg', '学习', '西二教学楼', 18, 2, 3, NOW() - INTERVAL 5 HOUR),
(1003, 3, 1, '艺术学院毕业展布展进行中，欢迎同校同学来捧场！现场装置和版画区都很值得看。', '/uploads/mock/square/gallery.jpg,/uploads/mock/square/painting.jpg', '展览', '美术馆', 67, 3, 3, NOW() - INTERVAL 8 HOUR),
(1004, 4, 1, '今晚羽毛球馆 8 点缺 2 人，新手友好，自带拍子就行。打完一起去喝糖水！', '/uploads/mock/square/badminton.jpg', '运动', '体育馆', 31, 2, 3, NOW() - INTERVAL 11 HOUR),
(1005, 5, 1, '秋天校区小路落叶铺满了，走路特别治愈。分享几张今天的胶片感～', '/uploads/mock/square/autumn.jpg,/uploads/mock/square/forest.jpg', '摄影', '紫荆道', 89, 2, 3, NOW() - INTERVAL 1 DAY),
(1006, 6, 1, '新出的联名奶茶在学校门口，排队 20 分钟但值得！少糖版本绝了。', '/uploads/mock/square/milktea.jpg', '探店', '校门口商业街', 54, 3, 3, NOW() - INTERVAL 1 DAY - INTERVAL 3 HOUR),
(1007, 7, 1, '考研自习打卡第 47 天。今天效率不错，分享下我的书桌和今日计划表。', '/uploads/mock/square/study.jpg', '学习', '24h 自习室', 23, 1, 3, NOW() - INTERVAL 2 DAY),
(1008, 8, 1, '乐队排练花絮：下周草坪音乐会试演，欢迎来听歌，缺会打节奏的朋友私信。', '/uploads/mock/square/music.jpg,/uploads/mock/square/guitar.jpg', '音乐', '中心草坪', 36, 2, 3, NOW() - INTERVAL 2 DAY - INTERVAL 5 HOUR),
(1009, 1, 1, '食堂新窗口的麻辣香锅分量太足了，两个人吃刚刚好。', '/uploads/mock/square/food.jpg', '美食', '一食堂', 71, 2, 3, NOW() - INTERVAL 3 DAY),
(1010, 2, 1, '周末骑行去了大学城江边，风很大但风景满分。有骑行搭子下次一起？', '/uploads/mock/square/cycling.jpg,/uploads/mock/square/beach.jpg', '运动', '江边绿道', 45, 2, 3, NOW() - INTERVAL 4 DAY),
(1011, 3, 1, '手冲咖啡入门练习：今天终于拉出像样的叶子了，开心到转圈。', '/uploads/mock/square/coffee.jpg', '生活', '宿舍咖啡角', 38, 4, 3, NOW() - INTERVAL 5 DAY),
(1012, 4, 1, '社团招新摆摊收工！感谢今天来捧场的朋友们，下周还有体验课。', '/uploads/mock/square/club.jpg,/uploads/mock/square/team.jpg', '校园', '社团广场', 62, 3, 3, NOW() - INTERVAL 6 DAY);

INSERT INTO post_comment (post_id, user_id, content, visibility, status, create_time) VALUES
(1001, 2, '构图好好看！周末可以一起拍吗？', 3, 3, NOW() - INTERVAL 1 HOUR),
(1001, 5, '图书馆那边傍晚光线确实绝。', 3, 3, NOW() - INTERVAL 50 MINUTE),
(1001, 3, '求原图参数～', 3, 3, NOW() - INTERVAL 30 MINUTE),
(1002, 1, '我 Vue 比较熟，可以聊聊需求。', 3, 3, NOW() - INTERVAL 4 HOUR),
(1002, 6, '后端接口文档有吗？', 3, 3, NOW() - INTERVAL 3 HOUR),
(1003, 4, '下午去看了，装置区很震撼！', 3, 3, NOW() - INTERVAL 7 HOUR),
(1003, 1, '毕业展具体开到几号？', 3, 3, NOW() - INTERVAL 6 HOUR),
(1003, 7, '已转发给室友了。', 3, 3, NOW() - INTERVAL 5 HOUR),
(1004, 2, '我可以，大概什么水平？', 3, 3, NOW() - INTERVAL 10 HOUR),
(1004, 5, '打完糖水去哪家？', 3, 3, NOW() - INTERVAL 9 HOUR),
(1005, 3, '紫荆道今天我也路过，好美。', 3, 3, NOW() - INTERVAL 20 HOUR),
(1005, 8, '胶片感怎么调的？', 3, 3, NOW() - INTERVAL 18 HOUR),
(1006, 1, '少糖+少冰 yyds', 3, 3, NOW() - INTERVAL 1 DAY),
(1006, 4, '现在还要排队吗？', 3, 3, NOW() - INTERVAL 23 HOUR),
(1006, 7, '明天一起去！', 3, 3, NOW() - INTERVAL 22 HOUR),
(1007, 2, '加油，坚持就是胜利。', 3, 3, NOW() - INTERVAL 2 DAY),
(1008, 1, '下周几点开始？', 3, 3, NOW() - INTERVAL 2 DAY),
(1008, 6, '我可以带鼓棒来试打。', 3, 3, NOW() - INTERVAL 2 DAY),
(1009, 3, '麻辣香锅在几楼窗口？', 3, 3, NOW() - INTERVAL 3 DAY),
(1009, 5, '两个人 32 块吃到撑。', 3, 3, NOW() - INTERVAL 3 DAY),
(1010, 4, '下次骑行喊我！', 3, 3, NOW() - INTERVAL 4 DAY),
(1010, 1, '江边风大记得带外套。', 3, 3, NOW() - INTERVAL 4 DAY),
(1011, 2, '这拉花可以出师了！', 3, 3, NOW() - INTERVAL 5 DAY),
(1011, 5, '用的什么豆子？', 3, 3, NOW() - INTERVAL 5 DAY),
(1011, 7, '宿舍咖啡角太专业了。', 3, 3, NOW() - INTERVAL 5 DAY),
(1011, 8, '求教程！', 3, 3, NOW() - INTERVAL 5 DAY),
(1012, 1, '今天去领了小礼品，体验课几点？', 3, 3, NOW() - INTERVAL 6 DAY),
(1012, 3, '摊位布置好好看。', 3, 3, NOW() - INTERVAL 6 DAY),
(1012, 6, '下周见！', 3, 3, NOW() - INTERVAL 6 DAY);

-- 清理外链旧数据，避免灰块
DELETE FROM post_comment WHERE post_id IN (
  SELECT id FROM (SELECT id FROM square_post WHERE images LIKE '%unsplash.com%' OR images LIKE '%picsum.photos%') t
);
DELETE FROM square_post WHERE images LIKE '%unsplash.com%' OR images LIKE '%picsum.photos%';

-- 早期 mock 无文件配图：补上本站图或隐藏
UPDATE square_post SET images = '/uploads/mock/square/badminton.jpg' WHERE id = 1 AND (images IS NULL OR images = '' OR images LIKE '/uploads/post/%');
UPDATE square_post SET images = '/uploads/mock/square/sunset.jpg' WHERE id = 2 AND (images IS NULL OR images = '' OR images LIKE '/uploads/post/%');
UPDATE square_post SET images = '/uploads/mock/square/coding.jpg' WHERE id = 3 AND (images IS NULL OR images = '' OR images LIKE '/uploads/post/%');
UPDATE square_post SET images = '/uploads/mock/square/milktea.jpg' WHERE id = 4 AND (images IS NULL OR images = '' OR images LIKE '/uploads/post/%');
UPDATE square_post SET images = '/uploads/mock/square/music.jpg' WHERE id = 5 AND (images IS NULL OR images = '' OR images LIKE '/uploads/post/%');

UPDATE square_post p SET comment_count = (
  SELECT COUNT(*) FROM post_comment c WHERE c.post_id = p.id AND c.status = 3
) WHERE p.id BETWEEN 1001 AND 1012;
