# SQLAlchemy 笔记

SQLAlchemy 是 Python ORM 框架，用来解耦应用程序和数据库，让应用程序可以用同一套 API 去操作不同的数据库

![](https://raw.githubusercontent.com/hsxhr-10/Notes/master/image/pythonweb-1.png)

根据官网提供的架构图，可知 SQLAlchemy 分为三部分，最底层的 DBAPI 是具体数据库的驱动接口，中间的 Core 提供了各种核心组件，最上层的 ORM 负责提供对象关系映射建模和一些高级的接口

SQLAlchemy 提供了 Dialect（方言）的概念，专门用于处理、提供一些底层数据库特有的功能 

## SQLAlchemy Core

### 「Schema/Type」组件

「Schema/Type」 组件负责映射底层数据库的字段数据类型

通用类型有两类：

- [Generic Types](https://docs.sqlalchemy.org/en/14/core/type_basics.html#generic-types)
- [SQL Standard and Multiple Vendor Types](https://docs.sqlalchemy.org/en/14/core/type_basics.html#sql-standard-and-multiple-vendor-types)

MySQL 方言：

- [MySQL Data Types](https://docs.sqlalchemy.org/en/14/dialects/mysql.html#mysql-data-types)

其他方言：

- [Included Dialects](https://docs.sqlalchemy.org/en/13/dialects/index.html#included-dialects)

### 「SQL Expression Language」组件

「SQL Expression Language」组件负责映射 SQL 语句提供的一些操作（譬如 in/or/and/not/desc/asc 等）

常用的有三部分：

- [Column Element Foundational Constructors](https://docs.sqlalchemy.org/en/14/core/sqlelement.html#column-element-foundational-constructors)
- [Column Element Modifier Constructors](https://docs.sqlalchemy.org/en/14/core/sqlelement.html#column-element-modifier-constructors)
- [ColumnElement](https://docs.sqlalchemy.org/en/14/core/sqlelement.html#sqlalchemy.sql.expression.ColumnElement)

### 「Engine」组件



### 连接池

## SQLAlchemy ORM

### Session
