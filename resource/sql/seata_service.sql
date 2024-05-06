create database `seata_account`;
use `seata_account`;
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT          NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';
ALTER TABLE `undo_log` ADD INDEX `ix_log_created` (`log_created`);
CREATE TABLE `t_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` bigint NULL DEFAULT NULL COMMENT '用戶ID',
  `product_id` bigint NULL DEFAULT NULL COMMENT '產品ID',
  `count` int NULL DEFAULT NULL COMMENT '數量',
  `money` decimal(11, 0) NULL DEFAULT NULL COMMENT '金額',
  `status` int NULL DEFAULT NULL COMMENT '訂單狀態: 0:創建中, 1:已完結'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

create database `seata_order`;
use `seata_order`;
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT          NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';
ALTER TABLE `undo_log` ADD INDEX `ix_log_created` (`log_created`);
CREATE TABLE `t_account`  (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` bigint NULL DEFAULT NULL COMMENT '用戶ID',
  `total` decimal(11, 0) NULL DEFAULT NULL COMMENT '總額度',
  `used` decimal(11, 0) NULL DEFAULT NULL COMMENT '已用帳戶餘額',
  `residue` decimal(11, 0) NULL DEFAULT NULL COMMENT '餘額'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
INSERT INTO `t_account` (id, user_id, total, used, residue) VALUES (1, 1, 1000, 0, 1000);

create database `seata_storage`;
use `seata_storage`;
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT          NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';
ALTER TABLE `undo_log` ADD INDEX `ix_log_created` (`log_created`);
CREATE TABLE `t_storage`  (
  `id` bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `product_id` bigint NULL DEFAULT NULL COMMENT '用戶ID',
  `total` decimal(11, 0) NULL DEFAULT NULL COMMENT '總庫存',
  `used` decimal(11, 0) NULL DEFAULT NULL COMMENT '已用庫存',
  `residue` decimal(11, 0) NULL DEFAULT NULL COMMENT '剩餘庫存'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
INSERT INTO `t_storage` (id, product_id, total, used, residue) VALUES (1, 1, 100, 0, 100);
