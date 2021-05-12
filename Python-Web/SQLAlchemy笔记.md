# SQLAlchemy 笔记

SQLAlchemy 是 Python ORM 框架，用来解耦应用程序和数据库，让应用程序可以用同一套 API 去操作不同的数据库

![](https://raw.githubusercontent.com/hsxhr-10/Notes/master/image/pythonwebsqla-1.png)

根据官网提供的架构图，可知 SQLAlchemy 分为三部分，最底层的 DBAPI 是具体数据库的驱动接口，中间的 Core 提供了各种核心组件，最上层的 ORM 负责提供对象关系映射建模和一些高级的接口

SQLAlchemy 提供了 Dialect（方言）的概念，专门用于处理、提供一些底层数据库特有的功能 

## SQLAlchemy Core

### (1) Schema/Type 组件

Schema/Type 组件负责映射底层数据库的字段数据类型

通用类型有两类：

- [Generic Types](https://docs.sqlalchemy.org/en/14/core/type_basics.html#generic-types)
- [SQL Standard and Multiple Vendor Types](https://docs.sqlalchemy.org/en/14/core/type_basics.html#sql-standard-and-multiple-vendor-types)

MySQL 方言：

- [MySQL Data Types](https://docs.sqlalchemy.org/en/14/dialects/mysql.html#mysql-data-types)

其他方言：

- [Included Dialects](https://docs.sqlalchemy.org/en/13/dialects/index.html#included-dialects)

### (2) SQL Expression Language 组件

SQL Expression Language 组件负责映射 SQL 语句的一些操作（譬如 in/or/and/not/desc/asc 等）

常用的有三部分：

- [Column Element Foundational Constructors](https://docs.sqlalchemy.org/en/14/core/sqlelement.html#column-element-foundational-constructors)
- [Column Element Modifier Constructors](https://docs.sqlalchemy.org/en/14/core/sqlelement.html#column-element-modifier-constructors)
- [ColumnElement](https://docs.sqlalchemy.org/en/14/core/sqlelement.html#sqlalchemy.sql.expression.ColumnElement)

### (3) Engine 和 Connection Pooling 组件

根据官网的示意图可知 Engine 和其他组件的关系

![](https://raw.githubusercontent.com/hsxhr-10/Notes/master/image/pythonwebsqla-2.png)

Engine 和连接池线程安全

#### create_engine() 方法说明

用于创建 Engine 对象和配置连接池

常用参数说明：

- url：数据库连接 URL，格式 `dialect+driver://username:password@host:port/database`。具体参考 [这里](https://docs.sqlalchemy.org/en/14/core/engines.html#database-urls)
- echo=False：是否开启 Engine 日志。对性能有比较大的影响，线上环境应该关闭
- echo_pool=False：是否开启连接池日志。对性能有比较大的影响，线上环境应该关闭
- isolation_level：事务隔离级别。取值 ("SERIALIZABLE", "REPEATABLE READ", "READ COMMITTED", "READ UNCOMMITTED")，一般不需要主动设置
- pool_size=5：连接池中保持打开的连接数。QueuePool 下设置为 0 代表无限制
- max_overflow=10：在 pool_size 之外还能打开的连接数，也就是最大连接数，仅在 QueuePool 下有效
- pool_pre_ping：每次从池中取出连接时，是否检测连接的有效性。一般设置为 True 确保使用有效的连接
- pool_recycle=-1：主动回收连接的时长。MySQL 默认 8 小时后如果检测到空闲连接，就会主动断开连接
- pool_timeout=30：从池中获取连接的等待时间。单位秒

## SQLAlchemy ORM

### (1) Session

Session 负责映射一次或一组 SQL 操作，默认不是 autucommit

#### sessionmaker() 类说明

用于创建 Session 对象

常用参数说明：

- bind：与 Session 关联的 Engine 对象
- autoflush=True：flush 之后 SQL 才会被执行。一般设置成 True，就不需要每条 SQL 后面 flush 一下
- autocommit=False：是否自动提交事务
- expire_on_commit=True：Session 是否在事务提交之后失效

> 相关的官方文档在 [这里](https://docs.sqlalchemy.org/en/14/orm/session_api.html#sqlalchemy.orm.Session)

Session 不是线程安全，可以用 `contextmanager` 加 `yield` 解决：

```python
from contextlib import contextmanager


@contextmanager
def session_factory():
    session = Session()
    try:
        yield session
        session.commit()
    except:
        session.rollback()
        raise
    finally:
        session.close()


with session_factory() as session:
    # use session
    pass
```

完整案例参考 [这里](https://github.com/hsxhr-10/Notes/blob/master/Python-Web/Flask/flask-sqlalchemy/database.py#L50) ，或者用 `from sqlalchemy.orm import scoped_session`

### ORM 建模

有四张表，factory 表和 product 表是一对多关系，orders 表和 product 表关系是多对多：

```sql
CREATE TABLE factory (
    `id` bigint(11) NOT NULL AUTO_INCREMENT,
    `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记, 0 是未删除, 1 是已删除',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建的时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改的时间',
    `factory_id` varchar(255) NOT NULL UNIQUE COMMENT '生产厂家ID',
    `name` varchar(45) NOT NULL COMMENT '生产厂家名称',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产厂家信息';
```

```sql
CREATE TABLE product (
    `id` bigint(11) NOT NULL AUTO_INCREMENT,
    `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记, 0 是未删除, 1 是已删除',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建的时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改的时间',
    `product_id` varchar(255) NOT NULL UNIQUE COMMENT '商品ID',
    `name` varchar(45) NOT NULL COMMENT '商品名称',
    `factory_id` varchar(255) NOT NULL UNIQUE COMMENT '关联的生产厂家ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息';
```

```sql
CREATE TABLE orders (
    `id` bigint(11) NOT NULL AUTO_INCREMENT,
    `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记, 0 是未删除, 1 是已删除',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建的时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改的时间',
    `order_id` varchar(255) NOT NULL UNIQUE COMMENT '订单ID',
    `price` decimal(13, 5) NOT NULL DEFAULT 0 COMMENT '订单金额',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单信息';
```

```sql
CREATE TABLE orders_product (
    `id` bigint(11) NOT NULL AUTO_INCREMENT,
    `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记, 0 是未删除, 1 是已删除',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建的时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改的时间',
    `order_id` varchar(255) NOT NULL UNIQUE COMMENT '订单ID',
    `product_id` varchar(255) NOT NULL UNIQUE COMMENT '商品ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表和商品表的多对多关系';
```



[comment]: <> (## 参考)

[comment]: <> (- https://stackoverflow.com/questions/6297404/multi-threaded-use-of-sqlalchemy#:~:text=Session%20objects%20are%20not%20thread,%2C%20but%20are%20thread%2Dlocal.&text=If%20you%20don't%20want,object%20by%20default%20uses%20threading.)

[comment]: <> (- https://copdips.com/2019/05/using-python-sqlalchemy-session-in-multithreading.html)
