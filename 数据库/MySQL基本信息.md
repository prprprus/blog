# MySQL 基本信息

## 架构概况

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/mysql-1.png)

## InnoDB MyISAM 区别

### InnoDB

- 支持事务
- 支持行锁
- 支持数据缓存
- 支持外键
- 存储容量上限为 64TB

### MyISAM

- 不支持事务
- 只支持表锁
- 不支持数据缓存
- 不支持外键
- 存储容量上限为 256TB
