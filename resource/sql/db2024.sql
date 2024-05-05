create database `db2024`;
use `db2024`;

drop table if exists `t_pay`;
create table t_pay
(
    id          int unsigned auto_increment primary key,
    pay_no      varchar(50)                                not null comment '支付流水號',
    order_no    varchar(50)                                not null comment '訂單流水號',
    user_id     int              default 1                 null comment '用戶帳號ID',
    amount      decimal(8, 2)    default 9.90              not null comment '交易金額',
    deleted     tinyint unsigned default '0'               not null comment '刪除標志, 默認0不刪除,1刪除',
    create_time timestamp        default CURRENT_TIMESTAMP not null comment '創建時間',
    update_time timestamp        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新時間'
)
    comment '支付交易表';

INSERT INTO `cloud2024`.t_pay (pay_no, order_no, user_id, amount, deleted) VALUES ('pay202403121420', '202403121421', 1, 9.90, 0);
INSERT INTO `cloud2024`.t_pay (pay_no, order_no, user_id, amount, deleted) VALUES ('pay202403122048', '202403122048', 1, 9.90, 0);
INSERT INTO `cloud2024`.t_pay (pay_no, order_no, user_id, amount, deleted) VALUES ('pay202403130910', '202403130910', 0, 0.00, 0);
INSERT INTO `cloud2024`.t_pay (pay_no, order_no, user_id, amount, deleted) VALUES ('pay2024031220499', '20240312204799', 1, 9.90, 0);
INSERT INTO `cloud2024`.t_pay (pay_no, order_no, user_id, amount, deleted) VALUES ('feign2024031220499', 'feign2024031220499', 1, 9.90, 0);
