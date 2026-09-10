-- 校园社交平台 数据库结构脚本
-- 数据库：xyjy
-- 说明：包含用户认证、匹配、广场、聊天、校园生活、商城、内容审核、管理后台等模块

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS xyjy;
CREATE DATABASE xyjy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE xyjy;

-- 管理员表
DROP TABLE IF EXISTS sys_admin;
CREATE TABLE sys_admin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(50) NOT NULL COMMENT '登录账号',
  password VARCHAR(100) NOT NULL COMMENT '登录密码',
  nickname VARCHAR(50) COMMENT '姓名',
  avatar VARCHAR(255) COMMENT '头像',
  phone VARCHAR(20) COMMENT '手机号',
  status TINYINT DEFAULT 1 COMMENT '状态 1启用 0停用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB COMMENT='管理员表';

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
  role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
  remark VARCHAR(255) COMMENT '备注',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB COMMENT='角色表';

-- 管理员角色关联表
DROP TABLE IF EXISTS sys_admin_role;
CREATE TABLE sys_admin_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  admin_id BIGINT NOT NULL COMMENT '管理员ID',
  role_id BIGINT NOT NULL COMMENT '角色ID'
) ENGINE=InnoDB COMMENT='管理员角色关联表';

-- 权限菜单表
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  perm_name VARCHAR(50) NOT NULL COMMENT '权限名称',
  perm_code VARCHAR(80) NOT NULL COMMENT '权限编码',
  parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
  sort INT DEFAULT 0 COMMENT '排序'
) ENGINE=InnoDB COMMENT='权限菜单表';

-- 角色权限关联表
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  perm_id BIGINT NOT NULL COMMENT '权限ID'
) ENGINE=InnoDB COMMENT='角色权限关联表';

-- 操作日志表
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  admin_id BIGINT COMMENT '管理员ID',
  admin_name VARCHAR(50) COMMENT '管理员名称',
  module VARCHAR(50) COMMENT '模块',
  operation VARCHAR(100) COMMENT '操作描述',
  detail TEXT COMMENT '操作详情',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='操作日志表';

-- 用户主表
DROP TABLE IF EXISTS app_user;
CREATE TABLE app_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  openid VARCHAR(64) COMMENT '微信OpenID',
  nickname VARCHAR(50) COMMENT '昵称',
  avatar VARCHAR(255) COMMENT '头像',
  gender TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  birthday DATE COMMENT '出生日期',
  age INT COMMENT '年龄',
  height INT COMMENT '身高cm',
  intro VARCHAR(500) COMMENT '个人简介',
  school VARCHAR(100) COMMENT '学校',
  campus VARCHAR(100) COMMENT '校区',
  college VARCHAR(100) COMMENT '院系',
  grade VARCHAR(50) COMMENT '年级',
  student_no VARCHAR(50) COMMENT '学号',
  tags VARCHAR(255) COMMENT '兴趣爱好标签,逗号分隔',
  partner_type VARCHAR(255) COMMENT '需要的搭子类型',
  expect VARCHAR(255) COMMENT '交友期待',
  phone VARCHAR(20) COMMENT '手机号',
  identity_verified TINYINT DEFAULT 0 COMMENT '个人认证是否通过 0否 1是',
  school_verified TINYINT DEFAULT 0 COMMENT '学校认证是否通过 0否 1是',
  school_id BIGINT COMMENT '绑定学校ID',
  avatar_audit_status TINYINT DEFAULT 0 COMMENT '头像审核 0待审 1通过 2驳回',
  intro_audit_status TINYINT DEFAULT 0 COMMENT '简介审核 0待审 1通过 2驳回',
  status TINYINT DEFAULT 1 COMMENT '账号状态 1正常 2限制发言 3封禁',
  speak_limit_end DATETIME COMMENT '限制发言到期时间',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='用户主表';

-- 学校字典表
DROP TABLE IF EXISTS school_info;
CREATE TABLE school_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  school_name VARCHAR(100) NOT NULL COMMENT '学校全称',
  province VARCHAR(50) COMMENT '省份',
  city VARCHAR(50) COMMENT '城市'
) ENGINE=InnoDB COMMENT='学校字典表';

-- 个人认证表
DROP TABLE IF EXISTS personal_auth;
CREATE TABLE personal_auth (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  phone VARCHAR(20) COMMENT '手机号',
  real_name VARCHAR(50) COMMENT '真实姓名',
  id_card VARCHAR(20) COMMENT '身份证号',
  id_front_img VARCHAR(255) COMMENT '身份证人像面',
  id_back_img VARCHAR(255) COMMENT '身份证国徽面',
  status TINYINT DEFAULT 0 COMMENT '状态 0未提交 1审核中 2已通过 3未通过',
  reject_reason VARCHAR(255) COMMENT '驳回原因',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='个人认证表';

-- 学校认证表
DROP TABLE IF EXISTS school_auth;
CREATE TABLE school_auth (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  school_name VARCHAR(100) COMMENT '学校全称',
  college VARCHAR(100) COMMENT '院系',
  grade VARCHAR(50) COMMENT '年级',
  student_no VARCHAR(50) COMMENT '学号',
  doc_type VARCHAR(50) COMMENT '证明材料类型',
  doc_imgs VARCHAR(1000) COMMENT '证明材料照片,逗号分隔',
  remark VARCHAR(255) COMMENT '补充说明',
  status TINYINT DEFAULT 0 COMMENT '状态 0未提交 1审核中 2已通过 3未通过',
  reject_reason VARCHAR(255) COMMENT '驳回原因',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='学校认证表';

-- 用户相册表
DROP TABLE IF EXISTS user_photo;
CREATE TABLE user_photo (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  img_url VARCHAR(255) NOT NULL COMMENT '照片地址',
  sort INT DEFAULT 0 COMMENT '排序',
  audit_status TINYINT DEFAULT 0 COMMENT '审核状态 0待审 1通过 2驳回',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='用户相册表';

-- 喜欢记录表
DROP TABLE IF EXISTS user_like;
CREATE TABLE user_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '发起用户ID',
  target_id BIGINT NOT NULL COMMENT '目标用户ID',
  type TINYINT DEFAULT 1 COMMENT '类型 1喜欢 2特别关注',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='喜欢记录表';

-- 跳过记录表
DROP TABLE IF EXISTS user_skip;
CREATE TABLE user_skip (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '发起用户ID',
  target_id BIGINT NOT NULL COMMENT '目标用户ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='跳过记录表';

-- 匹配表
DROP TABLE IF EXISTS user_match;
CREATE TABLE user_match (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_a BIGINT NOT NULL COMMENT '用户A',
  user_b BIGINT NOT NULL COMMENT '用户B',
  status TINYINT DEFAULT 1 COMMENT '状态 1匹配中 0已取消',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='匹配表';

-- 访客记录表
DROP TABLE IF EXISTS user_visit;
CREATE TABLE user_visit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '访客用户ID',
  target_id BIGINT NOT NULL COMMENT '被访问用户ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='访客记录表';

-- 黑名单表
DROP TABLE IF EXISTS user_blacklist;
CREATE TABLE user_blacklist (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  target_id BIGINT NOT NULL COMMENT '被拉黑用户ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='黑名单表';

-- 校园广场动态表
DROP TABLE IF EXISTS square_post;
CREATE TABLE square_post (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '发布用户ID',
  content VARCHAR(2000) COMMENT '正文',
  images VARCHAR(1000) COMMENT '图片,逗号分隔',
  topic VARCHAR(100) COMMENT '话题或标签',
  place VARCHAR(100) COMMENT '位置',
  like_count INT DEFAULT 0 COMMENT '点赞数',
  comment_count INT DEFAULT 0 COMMENT '评论数',
  status TINYINT DEFAULT 3 COMMENT '状态 0草稿 1待审核 3已发布 4审核驳回 5违规拦截 6已删除',
  reject_reason VARCHAR(255) COMMENT '驳回原因',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='校园广场动态表';

-- 动态点赞表
DROP TABLE IF EXISTS post_like;
CREATE TABLE post_like (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  post_id BIGINT NOT NULL COMMENT '动态ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='动态点赞表';

-- 动态评论表
DROP TABLE IF EXISTS post_comment;
CREATE TABLE post_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  post_id BIGINT NOT NULL COMMENT '动态ID',
  user_id BIGINT NOT NULL COMMENT '评论用户ID',
  content VARCHAR(500) COMMENT '评论内容',
  visibility TINYINT DEFAULT 3 COMMENT '可见范围 1发布人可见 2回复人可见 3全部可见',
  reply_to_user_id BIGINT COMMENT '回复对象用户ID visibility=2时必填',
  parent_id BIGINT COMMENT '父评论ID',
  status TINYINT DEFAULT 3 COMMENT '状态 1待审核 3已发布 5违规拦截 6已删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='动态评论表';

-- 聊天会话表
DROP TABLE IF EXISTS chat_session;
CREATE TABLE chat_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_a BIGINT NOT NULL COMMENT '用户A',
  user_b BIGINT NOT NULL COMMENT '用户B',
  last_msg VARCHAR(255) COMMENT '最后一条消息',
  last_time DATETIME COMMENT '最后消息时间',
  locked TINYINT DEFAULT 0 COMMENT '是否锁定 1锁定待回复 0正常',
  initiator BIGINT COMMENT '打招呼发起方',
  status TINYINT DEFAULT 1 COMMENT '状态 1正常 0已删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='聊天会话表';

-- 聊天消息表
DROP TABLE IF EXISTS chat_message;
CREATE TABLE chat_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  session_id BIGINT NOT NULL COMMENT '会话ID',
  from_id BIGINT NOT NULL COMMENT '发送者ID',
  to_id BIGINT NOT NULL COMMENT '接收者ID',
  msg_type TINYINT DEFAULT 1 COMMENT '类型 1文字 2表情 3图片',
  content VARCHAR(1000) COMMENT '内容',
  is_read TINYINT DEFAULT 0 COMMENT '是否已读 0否 1是',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='聊天消息表';

-- 校园跑腿订单表
DROP TABLE IF EXISTS errand_order;
CREATE TABLE errand_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  publisher_id BIGINT NOT NULL COMMENT '发单人ID',
  taker_id BIGINT COMMENT '接单人ID',
  title VARCHAR(100) NOT NULL COMMENT '任务标题',
  content VARCHAR(500) COMMENT '任务内容',
  from_place VARCHAR(100) COMMENT '取件地点',
  to_place VARCHAR(100) COMMENT '送达地点',
  finish_time VARCHAR(100) COMMENT '完成时间要求',
  fee DECIMAL(10,2) DEFAULT 0 COMMENT '赏金',
  remark VARCHAR(255) COMMENT '补充说明',
  publisher_contact VARCHAR(50) COMMENT '发单人联系方式',
  taker_contact VARCHAR(50) COMMENT '接单人联系方式',
  status TINYINT DEFAULT 1 COMMENT '状态 1待接单 2进行中 3已完成 4已取消',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='校园跑腿订单表';

-- 二手商品表
DROP TABLE IF EXISTS second_goods;
CREATE TABLE second_goods (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  seller_id BIGINT NOT NULL COMMENT '卖家ID',
  name VARCHAR(100) NOT NULL COMMENT '商品名称',
  category VARCHAR(50) COMMENT '分类',
  condition_desc VARCHAR(50) COMMENT '成色',
  price DECIMAL(10,2) DEFAULT 0 COMMENT '价格',
  description VARCHAR(1000) COMMENT '描述',
  images VARCHAR(1000) COMMENT '商品图片',
  status TINYINT DEFAULT 1 COMMENT '状态 0待审核 1在售 2已下架 5违规',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='二手商品表';

-- 游戏组局表
DROP TABLE IF EXISTS game_team;
CREATE TABLE game_team (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  creator_id BIGINT NOT NULL COMMENT '发起人ID',
  game_name VARCHAR(100) NOT NULL COMMENT '游戏名称',
  play_time VARCHAR(100) COMMENT '时间',
  need_num INT DEFAULT 1 COMMENT '需要人数',
  joined_num INT DEFAULT 1 COMMENT '已加入人数',
  require_desc VARCHAR(255) COMMENT '参与要求',
  status TINYINT DEFAULT 1 COMMENT '状态 1招募中 2已满 3已结束',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='游戏组局表';

-- 组局成员表
DROP TABLE IF EXISTS game_team_member;
CREATE TABLE game_team_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  team_id BIGINT NOT NULL COMMENT '组局ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='组局成员表';

-- 商城商品分类表
DROP TABLE IF EXISTS mall_category;
CREATE TABLE mall_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(50) NOT NULL COMMENT '分类名称',
  sort INT DEFAULT 0 COMMENT '排序'
) ENGINE=InnoDB COMMENT='商城商品分类表';

-- 商城商品表
DROP TABLE IF EXISTS mall_goods;
CREATE TABLE mall_goods (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  category_id BIGINT COMMENT '分类ID',
  name VARCHAR(100) NOT NULL COMMENT '商品名称',
  cover VARCHAR(255) COMMENT '封面图',
  images VARCHAR(1000) COMMENT '详情图',
  price DECIMAL(10,2) DEFAULT 0 COMMENT '价格',
  spec VARCHAR(255) COMMENT '规格,逗号分隔',
  stock INT DEFAULT 0 COMMENT '库存',
  sales INT DEFAULT 0 COMMENT '销量',
  detail VARCHAR(2000) COMMENT '商品详情',
  status TINYINT DEFAULT 1 COMMENT '状态 1上架 0下架',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='商城商品表';

-- 购物车表
DROP TABLE IF EXISTS mall_cart;
CREATE TABLE mall_cart (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  goods_id BIGINT NOT NULL COMMENT '商品ID',
  spec VARCHAR(50) COMMENT '选择规格',
  quantity INT DEFAULT 1 COMMENT '数量',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='购物车表';

-- 商城订单表
DROP TABLE IF EXISTS mall_order;
CREATE TABLE mall_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  order_no VARCHAR(50) NOT NULL COMMENT '订单号',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  total_amount DECIMAL(10,2) DEFAULT 0 COMMENT '订单金额',
  status TINYINT DEFAULT 1 COMMENT '状态 1待付款 2待备货 3配送中 4已完成 5退款售后',
  address VARCHAR(255) COMMENT '收货地址',
  receiver VARCHAR(50) COMMENT '收货人',
  phone VARCHAR(20) COMMENT '联系电话',
  pay_time DATETIME COMMENT '支付时间',
  refund_status TINYINT DEFAULT 0 COMMENT '退款状态 0无 1申请中 2已退款 3已拒绝',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB COMMENT='商城订单表';

-- 商城订单明细表
DROP TABLE IF EXISTS mall_order_item;
CREATE TABLE mall_order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  goods_id BIGINT NOT NULL COMMENT '商品ID',
  goods_name VARCHAR(100) COMMENT '商品名称',
  goods_cover VARCHAR(255) COMMENT '商品封面',
  spec VARCHAR(50) COMMENT '规格',
  price DECIMAL(10,2) DEFAULT 0 COMMENT '单价',
  quantity INT DEFAULT 1 COMMENT '数量'
) ENGINE=InnoDB COMMENT='商城订单明细表';

-- 过滤词库表
DROP TABLE IF EXISTS filter_word;
CREATE TABLE filter_word (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  word VARCHAR(100) NOT NULL COMMENT '词语',
  word_type TINYINT DEFAULT 1 COMMENT '类型 1违规词 2审核词',
  category VARCHAR(50) COMMENT '分类',
  risk_level TINYINT DEFAULT 1 COMMENT '风险等级 1低 2中 3高',
  match_type TINYINT DEFAULT 1 COMMENT '匹配方式 1精确 2模糊 3正则',
  scope VARCHAR(255) COMMENT '适用范围,逗号分隔',
  tip VARCHAR(255) COMMENT '用户提示文案',
  enabled TINYINT DEFAULT 1 COMMENT '是否启用 1是 0否',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='过滤词库表';

-- 过滤词命中记录表
DROP TABLE IF EXISTS filter_hit_log;
CREATE TABLE filter_hit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  word VARCHAR(100) COMMENT '命中词',
  content VARCHAR(1000) COMMENT '命中内容',
  user_id BIGINT COMMENT '用户ID',
  biz_type VARCHAR(50) COMMENT '业务类型',
  handle_result VARCHAR(100) COMMENT '处理结果',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='过滤词命中记录表';

-- 举报表
DROP TABLE IF EXISTS report_record;
CREATE TABLE report_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  reporter_id BIGINT NOT NULL COMMENT '举报人ID',
  target_type VARCHAR(50) COMMENT '举报类型 post动态 comment评论 user用户 order订单',
  target_id BIGINT COMMENT '举报目标ID',
  reason VARCHAR(255) COMMENT '举报原因',
  evidence VARCHAR(1000) COMMENT '证据图片',
  status TINYINT DEFAULT 0 COMMENT '状态 0待处理 1已处理 2已驳回',
  handle_result VARCHAR(255) COMMENT '处理结果',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='举报表';

-- 违规记录表
DROP TABLE IF EXISTS violation_record;
CREATE TABLE violation_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  type VARCHAR(50) COMMENT '处置类型 warn警告 limit限制发言 ban封禁',
  reason VARCHAR(255) COMMENT '原因',
  admin_name VARCHAR(50) COMMENT '操作管理员',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='违规记录表';

-- 意见反馈表
DROP TABLE IF EXISTS user_feedback;
CREATE TABLE user_feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT COMMENT '用户ID',
  content VARCHAR(500) COMMENT '反馈内容',
  contact VARCHAR(100) COMMENT '联系方式',
  status TINYINT DEFAULT 0 COMMENT '状态 0待处理 1已处理',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='意见反馈表';

-- 话题标签表
DROP TABLE IF EXISTS topic_tag;
CREATE TABLE topic_tag (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(50) NOT NULL COMMENT '标签名称',
  post_count INT DEFAULT 0 COMMENT '关联动态数',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='话题标签表';

SET FOREIGN_KEY_CHECKS = 1;
