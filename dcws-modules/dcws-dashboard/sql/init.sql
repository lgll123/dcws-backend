DROP TABLE IF EXISTS `dashboard_file`;
CREATE TABLE `dashboard_file`
(
    `id`             bigint(64)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `module`         varchar(255) NOT NULL DEFAULT '' COMMENT '模块/类型',
    `original_name`  varchar(255) NOT NULL DEFAULT '' COMMENT '原文件名',
    `new_name`       varchar(255) NOT NULL DEFAULT '' COMMENT '新文件名',
    `extension`      varchar(20)  NOT NULL DEFAULT '' COMMENT '后缀名(如: txt、png、doc、java等)',
    `path`           varchar(255) NOT NULL DEFAULT '' COMMENT '路径',
    `url`            varchar(255) NOT NULL DEFAULT '' COMMENT '访问路径',
    `size`           bigint(64)   NOT NULL DEFAULT '0' COMMENT '文件大小',
    `download_count` int(11)      NOT NULL DEFAULT '0' COMMENT '下载次数',
    `user_name`      varchar(20)  NOT NULL DEFAULT '' COMMENT '上传用户',
    `create_date`    timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_date`    timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`    bigint(64)  null default 2 comment '创建人',
    `update_by`    bigint(64)  null default 2 comment '更新人',
    `del_flag`       tinyint(4)   NOT NULL DEFAULT '0' COMMENT '删除标记0:保留，1:删除',
    `bucket`         varchar(100) NOT NULL DEFAULT 'gc-starter' COMMENT '桶名称',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT ='文件表';

DROP TABLE IF EXISTS `dashboard_page`;
CREATE TABLE `dashboard_page`
(
    `id`          bigint(64)   NOT NULL AUTO_INCREMENT,
    `name`        varchar(100) NOT NULL DEFAULT '' COMMENT '页面中文名称',
    `code`        varchar(255) NOT NULL DEFAULT '' COMMENT '页面编码，页面唯一标识符',
    `cover_picture`        varchar(255) NOT NULL DEFAULT '' COMMENT '封面图片文件路径',
    `icon`        varchar(100) NOT NULL DEFAULT '' COMMENT '页面图标',
    `icon_color`  varchar(100) NOT NULL DEFAULT '' COMMENT '图标颜色',
    `type`        varchar(100) NOT NULL DEFAULT 'custom' COMMENT '页面类型',
    `layout`      varchar(255) NOT NULL DEFAULT '' COMMENT '组件布局，记录组件的相对位置和顺序',
    `config`      longtext COMMENT '表单属性，只有表单类型时才有这个值',
    `parent_code` varchar(255) NOT NULL DEFAULT '' COMMENT '父级目录编码',
    `order_num`   bigint(64)   NOT NULL DEFAULT '0' COMMENT '排序',
    `remark`      varchar(100) NOT NULL DEFAULT '' COMMENT '备忘',
    `model_code`  varchar(255) NOT NULL DEFAULT '' COMMENT '模型编码',
    `app_code`    varchar(255) NOT NULL DEFAULT '' COMMENT '所属应用编码',
    `update_date` timestamp                        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_date` timestamp                        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`    bigint(64)  null default 2 comment '创建人',
    `update_by`    bigint(64)  null default 2 comment '更新人',
    `del_flag`    tinyint(1)   NOT NULL DEFAULT '0' COMMENT '删除标识符 1 删除 0未删',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='页面基本信息表';

# 模板表
DROP TABLE IF EXISTS `dashboard_page_template`;
CREATE TABLE `dashboard_page_template`
(
    `id`          bigint(64)                       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        varchar(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '模板名称',
    `type`        varchar(100) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '模板分类',
    `config`      text COLLATE utf8mb4_bin                  DEFAULT NULL COMMENT '模板配置',
    `thumbnail`   varchar(255) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '缩略图',
    `remark`      varchar(255) COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '备注',
    `update_date` timestamp                        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_date` timestamp                        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`    bigint(64)  null default 2 comment '创建人',
    `update_by`    bigint(64)  null default 2 comment '更新人',
    `del_flag`    tinyint(4)                       NOT NULL DEFAULT '0' COMMENT '删除标记0:保留，1:删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT ='页面模板表';

DROP TABLE IF EXISTS `dashboard_type`;
CREATE TABLE `dashboard_type` (
  `id` bigint(64) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(255) DEFAULT NULL COMMENT '名称',
  `code` varchar(255) DEFAULT NULL COMMENT '名称',
  `type` varchar(255) DEFAULT NULL COMMENT '名称',
  `order_num`   bigint(64)   NOT NULL DEFAULT '0' COMMENT '排序',
  `update_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by`    bigint(64)  null default 2 comment '创建人',
  `update_by`    bigint(64)  null default 2 comment '更新人',
  `del_flag` tinyint(2) NOT NULL DEFAULT '0' COMMENT '删除标识',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='大屏、资源库、组件库分类';


DROP TABLE IF EXISTS `dashboard_biz_component`;
CREATE TABLE `dashboard_biz_component` (
    `id`             bigint(64)   NOT NULL AUTO_INCREMENT,
    `name`           varchar(100) NOT NULL DEFAULT '' COMMENT '业务组件中文名称',
    `code`           varchar(255) NOT NULL DEFAULT '' COMMENT '业务组件编码，唯一标识符',
    `type`          varchar(255) NOT NULL DEFAULT '' COMMENT '分组',
    `cover_picture`  varchar(255) NOT NULL DEFAULT '' COMMENT '封面图片文件路径',
    `vue_content`     longtext COMMENT 'vue组件内容',
    `setting_content` longtext COMMENT '组件配置内容',
    `order_num`      bigint(64)   NOT NULL DEFAULT '0' COMMENT '排序',
    `remark`         varchar(100) NOT NULL DEFAULT '' COMMENT '备注',
    `update_date`    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_date`    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`    bigint(64)  null default 2 comment '创建人',
    `update_by`    bigint(64)  null default 2 comment '更新人',
    `module_code` varchar(255) NOT NULL DEFAULT '' COMMENT '模块编码',
    `del_flag` tinyint(2) NOT NULL DEFAULT '0' COMMENT '删除标识',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='业务组件表';

