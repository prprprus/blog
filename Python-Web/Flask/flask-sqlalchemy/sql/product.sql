CREATE TABLE product (
    `id` bigint(11) NOT NULL AUTO_INCREMENT,
    `is_deleted` tinyiny(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记, 0 是未删除, 1 是已删除',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建的时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改的时间',
    `product_id` varchar(255) NOT NULL UNIQUE COMMENT '商品ID',
    `name` varchar(45) NOT NULL COMMENT '商品名称',
    `factory_id` bigint(11) NOT NULL COMMENT '关联的生产厂家ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息';
