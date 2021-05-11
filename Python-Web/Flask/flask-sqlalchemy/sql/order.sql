CREATE TABLE order (
    `id` bigint(11) NOT NULL AUTO_INCREMENT,
    `is_deleted` tinyiny(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记, 0 是未删除, 1 是已删除',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建的时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改的时间',
    `order_id` varchar(255) NOT NULL UNIQUE COMMENT '订单ID',
    `price` decimal(8, 5) NOT NULL DEFAULT 0 COMMENT '订单金额',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息';
