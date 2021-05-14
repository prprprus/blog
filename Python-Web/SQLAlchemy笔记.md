# SQLAlchemy 笔记

1. [SQLAlchemy Core](https://github.com/hsxhr-10/Notes/blob/master/Python-Web/SQLAlchemy%E7%AC%94%E8%AE%B0.md#sqlalchemy-core)
2. [SQLAlchemy ORM](https://github.com/hsxhr-10/Notes/blob/master/Python-Web/SQLAlchemy%E7%AC%94%E8%AE%B0.md#sqlalchemy-orm)

SQLAlchemy 是 Python ORM 框架，用来将数据库表映射成编程语言的对象，同时解耦应用程序和数据库，让应用程序可以用同一套 API 去操作不同的数据库

![](https://raw.githubusercontent.com/hsxhr-10/Notes/master/image/pythonwebsqla-1.png)

根据官网提供的架构图，可知 SQLAlchemy 分为三部分，最底层的 DBAPI 是具体数据库的驱动接口，中间的 Core 提供了各种核心组件，最上层的 ORM 负责提供对象关系映射建模和一些高级的接口

SQLAlchemy 提供了 Dialect（方言）的概念，专门用于处理、提供一些底层数据库特有的功能 

> 讨论以 MySQL 为主

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

**⭐️ Engine 和连接池线程安全 ⭐️**

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

**⭐️ Session 不是线程安全，可以用 `contextmanager` 加 `yield` 解决⭐️**

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

完整案例参考 [这里](https://github.com/hsxhr-10/Notes/blob/master/Python-Web/Flask/flask-sqlalchemy/database.py#L50) ，或者用 `from sqlalchemy.orm import scoped_session` 解决也可以

### (2) ORM 建模

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

根据表结构，利用 Schema/Type 组件提供的各种数据类型，对应的 ORM 模型如下：

```python
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy import (
    Column,
    Integer,
    DateTime,
    String,
    DECIMAL,
    text
)
from sqlalchemy.dialects.mysql import TINYINT

_Base = declarative_base()


class _BaseMixin(_Base):
    """ 基类 ORM, 包含一些必须的字段 """
    __abstract__ = True
    __bind_key__ = 'extension_model'

    id = Column(Integer, primary_key=True)
    is_deleted = Column(TINYINT, nullable=False, default=0)
    create_time = Column(DateTime, nullable=False, default=text("CURRENT_TIMESTAMP"))
    update_time = Column(DateTime, nullable=False, default=text("CURRENT_TIMESTAMP"))
    

class Factory(_BaseMixin):
    __tablename__ = "factory"

    factory_id = Column(String(255), nullable=False, unique=True)
    name = Column(String(45), nullable=False)


class Product(_BaseMixin):
    """ Factory 和 Product 一对多 """
    __tablename__ = "product"

    product_id = Column(String(255), nullable=False, unique=True)
    name = Column(String(45), nullable=False)
    factory_id = Column(String(255), nullable=False, unique=True)


class Orders(_BaseMixin):
    __tablename__ = "orders"

    order_id = Column(String(255), nullable=False, unique=True)
    price = Column(DECIMAL(13, 5), nullable=False, default=0)


class OrdersProduct(_BaseMixin):
    """ Orders 和 Product 多对多 """
    __tablename__ = "orders_product"

    order_id = Column(String(255), nullable=False, unique=True)
    product_id = Column(String(255), nullable=False, unique=True)
```

### (3) SQL 操作

利用 ORM 本身提供的操作接口和 SQL Expression Language 组件，可以完成日常 SQL 操作

#### 单表查询

- `SELECT * FROM factory;`
    ```python
    with session_factory() as session:
        session.query(Factory).all()
    ```
- `SELECT * FROM factory WHERE name='工厂1号';`
    ```python
    with session_factory() as session:
        session.query(Factory).filter(Factory.name == "工厂1号").all()
    ```
- `SELECT * FROM factory WHERE id='a1d760f2-275e-4efb-ae02-dc4d5434fb10' AND name='工厂1号';`
    ```python
    with session_factory() as session:
        session.query(Factory).filter(Factory.id == "a1d760f2-275e-4efb-ae02-dc4d5434fb10").filter(Factory.name == "工厂1号").all()
    ```
- `SELECT * FROM factory WHERE id='a1d760f2-275e-4efb-ae02-dc4d5434fb10' OR name='工厂1号';`
    ```python
    from sqlalchemy import or_
    
    with session_factory() as session:
        session.query(Factory).filter(or_(Factory.name == "工厂1号", Factory.name == "工厂2号")).all()
    ```
- `SELECT * FROM factory LIMIT 1;`
    ```python
    with session_factory() as session:
        session.query(Factory).first()
    ```
- `SELECT * FROM factory ORDER BY name DESC LIMIT 1;`
    ```python
    with session_factory() as session:
        session.query(Factory).order_by(Factory.name.desc()).first()
    ```

更多操作参考 [这里](https://docs.sqlalchemy.org/en/14/orm/tutorial.html#common-filter-operators)

#### 连表查询

- `SELECT p.name, f.name FROM product p INNER JOIN factory f ON p.factory_id=f.factory_id;`
    ```python
    with session_factory() as session:
        session.query(Factory.name, Product.name).join(Product, Factory.factory_id == Product.factory_id).all()
    ```
    ```python
    with session_factory() as session:
        session.query(Factory.name, Product.name).filter(Factory.factory_id == Product.factory_id).all()
    ```
- `SELECT p.name, f.name FROM product p INNER JOIN factory f ON p.factory_id=f.factory_id WHERE f.name='工厂2号'";`
    ```python
    with session_factory() as session:
        session.query(Factory.name, Product.name).join(Product, Factory.factory_id == Product.factory_id).filter(Factory.name == "工厂2号").all()
    ```
- `SELECT t1.name, t2.name, t3.name FROM table1 t1 INNER JOIN t2 ON t1.id=t2.id LEFT JOIN table3 t3 ON t2.id=t3.id WHERE t1.name='aaa' AND t3.name='ccc';`
    ```python
    with session_factory() as session:
        session.query(table1.name, table2.name, table3.name)\
               .join(table2, table1.id == table2.id)\
               .outerjoin(table3, table2.id == table3.id)\
               .filter(table1.name == "aaa", table3.name == "ccc")\
               .all()
    ```

#### 更新

- `UPDATE factory SET name='工厂10号' WHERE factory_id='a1d760f2-275e-4efb-ae02-dc4d5434fb10';`
    ```python
    # 修改一条或者多条数据
    with session_factory() as session:
        session.query(Factory).filter(Factory.factory_id == "a1d760f2-275e-4efb-ae02-dc4d5434fb10").update({"name": "工厂10号"})
    ```
    ```python
    # 修改一条数据
    with session_factory() as session:
        factory = session.query(Factory).filter(Factory.factory_id == "a1d760f2-275e-4efb-ae02-dc4d5434fb10").one()
        factory.name = "工厂10号"
    ```

#### 插入

- `INSERT INTO factory(factory_id, name) VALUE("050b90a7-590f-410d-ad4b-61686b81436f", "工厂101号");`
    ```python
    with session_factory() as session:
        factory = Factory()
        factory.factory_id = "050b90a7-590f-410d-ad4b-61686b81436f"
        factory.name = "工厂101号"
    ```

#### 原生 SQL

```python
from sqlalchemy.sql import text

sql = text("select * from factory where name=:name;")
res = engine.execute(sql, {"name": "工厂1号"})
for row in res:
    for k, v in row.items():
        print("{}={}".format(k, v))
```

## 参考

- [Query API](https://docs.sqlalchemy.org/en/14/orm/query.html#query-api)
- [Multi-threaded use of SQLAlchemy](https://stackoverflow.com/questions/6297404/multi-threaded-use-of-sqlalchemy#:~:text=Session%20objects%20are%20not%20thread,%2C%20but%20are%20thread%2Dlocal.&text=If%20you%20don't%20want,object%20by%20default%20uses%20threading.)
