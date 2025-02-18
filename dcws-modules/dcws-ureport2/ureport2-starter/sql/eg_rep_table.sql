CREATE TABLE `eg_rep_sale`
(
    `id`           bigint NOT NULL COMMENT '主键',
    `city`         varchar(100)    DEFAULT NULL COMMENT '所属城市',
    `product_name` varchar(100)    DEFAULT NULL COMMENT '产品名称',
    `amount`       bigint NOT NULL DEFAULT '0' COMMENT '销售金额',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='销售表';


CREATE TABLE `eg_rep_customer`
(
    `id`        bigint      NOT NULL COMMENT '主键',
    `user_name` varchar(32) NOT NULL COMMENT '用户名',
    `nick_name` varchar(32) NOT NULL COMMENT '昵称',
    `account`   varchar(20) NOT NULL COMMENT '账号',
    `province`  varchar(100) DEFAULT NULL COMMENT '省份',
    `city`      varchar(100) DEFAULT NULL COMMENT '城市',
    PRIMARY KEY (`id`),
    UNIQUE KEY `eg_rep_customer_key` (`user_name`, `account`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='客户表';

CREATE TABLE `eg_rep_order`
(
    `id`           bigint NOT NULL COMMENT '主键',
    `city`         varchar(100)    DEFAULT NULL COMMENT '所属城市',
    `product_name` varchar(100)    DEFAULT NULL COMMENT '产品名称',
    `amount`       bigint NOT NULL DEFAULT '0' COMMENT '订单金额',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单表';

CREATE TABLE `eg_rep_price_history`
(
    `id`     bigint NOT NULL COMMENT '主键',
    `name`   varchar(100)    DEFAULT NULL COMMENT '名称',
    `month`  bigint          DEFAULT NULL COMMENT '月份',
    `amount` bigint NOT NULL DEFAULT '0' COMMENT '金额',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='价格历史表';

CREATE TABLE `eg_rep_product`
(
    `id`            bigint NOT NULL COMMENT '主键',
    `category_name` varchar(100) DEFAULT NULL COMMENT '类别名称',
    `product_name`  varchar(100) DEFAULT NULL COMMENT '产品名称',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='产品表';

CREATE TABLE `eg_rep_user`
(
    `id`        bigint      NOT NULL COMMENT '主键',
    `user_name` varchar(32) NOT NULL COMMENT '用户名',
    `nick_name` varchar(32) NOT NULL COMMENT '昵称',
    `account`   varchar(20) NOT NULL COMMENT '账号',
    `amount`    bigint DEFAULT '0' COMMENT '金额',
    PRIMARY KEY (`id`),
    UNIQUE KEY `eg_rep_user_key` (`user_name`, `account`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户信息表';

INSERT INTO eg_rep_user (id, user_name, nick_name, account, amount)
VALUES (1, '张三', '三', '1111111', 22),
       (2, '李四', '四', '1111112', 20),
       (3, '王五', '五', '1111113', 25);

INSERT INTO eg_rep_customer (id, user_name, nick_name, account, city, province)
VALUES (1, '张三', '三', '1111111', '深圳', '广东'),
       (2, '李四', '四', '1111112', '北京', '北京'),
       (3, '王五', '五', '1111113', '广州', '广东'),
       (4, '赵六', '六', '1111114', '上海', '上海');

INSERT INTO eg_rep_order (id, city, product_name, amount)
VALUES (1, '深圳', '樱桃', 12),
       (2, '北京', '樱桃', 12),
       (3, '广州', '樱桃', 12),
       (4, '上海', '樱桃', 12),
       (5, '深圳', '黄桃', 30),
       (6, '北京', '黄桃', 30),
       (7, '广州', '黄桃', 30),
       (8, '上海', '黄桃', 30),
       (9, '深圳', '西瓜', 1),
       (10, '北京', '西瓜', 1),
       (11, '广州', '西瓜', 1),
       (12, '上海', '西瓜', 1),
       (14, '深圳', '苹果', 2),
       (15, '北京', '苹果', 2),
       (16, '广州', '苹果', 2),
       (17, '上海', '苹果', 2),
       (18, '深圳', '梨子', 3),
       (19, '北京', '梨子', 3),
       (20, '广州', '梨子', 3),
       (21, '上海', '梨子', 3),
       (22, '深圳', '樱桃', 11),
       (23, '北京', '樱桃', 11),
       (24, '广州', '樱桃', 11),
       (25, '上海', '樱桃', 11);

INSERT INTO eg_rep_product (id, product_name, category_name)
VALUES (1, '苹果', '水果'),
       (2, '西瓜', '水果'),
       (3, '黄桃', '水果'),
       (4, '樱桃', '水果');

INSERT INTO eg_rep_sale (id, city, product_name, amount)
VALUES (1, '深圳', '牛奶', 100),
       (2, '深圳', '苹果', 20),
       (3, '上海', '牛奶', 30),
       (4, '上海', '苹果', 52),
       (5, '北京', '牛奶', 300),
       (6, '北京', '苹果', 30),
       (7, '广州', '苹果', 35),
       (8, '广州', '牛奶', 60),
       (9, '深圳', '香蕉', 55),
       (10, '北京', '菠萝', 30),
       (11, '上海', '香蕉', 6),
       (12, '广州', '菠萝', 62),
       (13, '北京', '香蕉', 30),
       (14, '广州', '香蕉', 30),
       (15, '上海', '菠萝', 30),
       (16, '深圳', '菠萝', 31);

INSERT INTO eg_rep_price_history (id, name, `month`, amount)
VALUES (1, '阿里', 1, 100),
       (2, '阿里', 2, 130),
       (3, '阿里', 3, 120),
       (4, '阿里', 4, 130),
       (5, '阿里', 5, 160),
       (6, '四方', 1, 20),
       (7, '四方', 2, 19),
       (8, '四方', 3, 45),
       (9, '四方', 4, 30),
       (10, '四方', 5, 23);