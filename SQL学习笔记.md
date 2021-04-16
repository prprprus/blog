# SQL 学习笔记

## DDL

### 数据库

- 创建数据库：`CREATE DATABASE IF NOT EXISTS <database_name> CHARACTER SET <charset_name>;`
- 显示数据库：`SHOW DATABASES;`
- 删除数据库：`DROP DATABASE <database_name>;`
- 选择数据库：`USE <database_>name;`
  
### 表

- 建表：参考 [这里](https://github.com/hsxhr-10/Blog/blob/master/%E8%AE%BE%E8%AE%A1%E5%BB%BA%E6%A8%A1%E5%B7%A5%E5%85%B7/%E6%95%B0%E6%8D%AE%E5%BA%93%E8%AE%BE%E8%AE%A1.md#%E4%B8%80%E4%B8%AA%E8%A7%84%E8%8C%83%E7%9A%84%E5%BB%BA%E8%A1%A8%E8%AF%AD%E5%8F%A5)
- 显示所有表：`SHOW TABLES`;
- 显示表结构：`DESCRIBE <table_name>;`
- 显示建表定义：`SHOW CREATE TABLE <table_name>\G;`
- 删除列：`ALTER TABLE <table_name> DROP COLUMN <column_name>;`
- 给表改名：`ALTER TABLE <old_name> RENAME TO <new_name>;`
- 删除表：`DROP TABLE IF EXISTS <table_name>;`

### 列

- 新加一个列：`ALTER TABLE <table_name> ADD <column_name> <column_definition>;`
- 新加多个列：`ALTER TABLE <table_name> ADD <column_name> <column_definition>, ADD <column_name> <column_definition>, ...;`
- 修改一个列：`ALTER TABLE <table_name> MODIFY <column_name> <column_definition>;`
- 修改多个列：`ALTER TABLE <table_name> MODIFY <column_name> <column_definition>, MODIFY <column_name> <column_definition>, ...;`
- 给列改名：`ALTER TABLE <table_name> CHANGE COLUMN <old_name> <new_name> <column_definition>;`

## DML

### SELECT 执行顺序

```SQL
SELECT <column_name>, [AGG_FUNC(<column_name>/<expression>)]
FROM <table_name>
JOIN <other_table_name> ON <table_name>.<column_name > = <other_table_name>.<column_name>
WHERE <filter_expression>
GROUP BY <column_name>
HAVING <filter_expression>
ORDER BY <column_name> ASC/DESC
LIMIT <count>;
```

1. FROM...JOIN...ON
2. WHERE
3. SELECT...AGG_FUNC
4. GROUP BY
5. HAVING
6. ORDER BY
7. LIMIT

### 过滤数据

`WHERE` 可以搭配一系列判断真假的关键字，用来过滤查询结果：

- `AND`
- `OR`
- `NOT`
- `IN`/`NOT IN`
- `BETWEEN`/`NOT BETWEEN`
- `LIKE`/`NOT LIKE`
- `IS NULL`/`IS NOT NULL`

### JOIN

`join` 是关系型数据库的一大特色，可以通过连接不同的表，查出单表没有的数据

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/SQL%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0-1.png)

三表连接案例：

```SQL
SELECT pi.imageid,p.ProductCode,pi.filepath,p.name,p.defaultimage
FROM ProductInfringeReport pir INNER JOIN Product p ON pir.ProductCode = p.ProductCode
    INNER JOIN ProductImage pi ON p.ProductID = pi.ProductID
WHERE pir.adddatetime>"{}";
```

### 分组和聚合

`GROUP BY` 可以对查询结果按照某个字段做分组，并搭配聚合函数做一些统计（个人感觉叫分类更容易理解 😂 ）

- 常用聚合：`SUM()`、`COUNT()`、`MAX()`、`MIN()`、`AVG()` 等
- 还可以搭配 `HAVING` 可以对 `GROUP BY` 的结果做数据过滤

### 排序

`ORDER BY` 可以对查询结果进行排序

- 可以根据一个字段做排序
- 也可以分别对多个列做排序，比如 `ORDER BY <COLUMN_1> DESC , <COLUMN_2> ASC;`
- `DESC` 代表降序，`ASC` 代表升序，默认是升序排序

## 事务

事务是数据库（数据库里的数据）状态转移的基本单位，事务可以保护数据的完整性和正确性

### 四个特性

事务的四个特性：ACID

- A：原子性。事务里的多个 SQL 要么都成功，要么都失败
- C：一致性。事务发生之前数据是正确的，事务之后也是正确的
- I：隔离性。不同事物之间互相独立、互不影响
- D：持久性。事务的修改是永久的，数据的状态不会随着关机、重启等情况而倒退

### 事务隔离级别

没有事务隔离性会造成的问题：

- 脏写：事务 T1 和事务 T2 都能修改同一块数据
  ![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/SQL%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0--2.png)
- 脏读：事务 T1 可以读取到事务 T2 的中间状态的数据
  ![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/SQL%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0-3.png)
- 不可重复读：对于同一块数据，事务 T1 读了两次，后一次是事务 T2 提交后的更新数据，前后两次读取的结果不一致 
  ![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/SQL%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0-4.png)
- 幻读：事务 A 插入了数据 data，事务 B 刚好修改了 data，此时事务 B 就可以读取到 data，这就是幻读 
  ![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/SQL%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0-5.png)

其中，脏写是最严重的结果，一般数据库都是默认不允许发生的。为了解决这些问题，数据库提供了不同的隔离级别。 

MySQL 提供了四种事务隔离级别（RU < RC < RR < S），如下图所示，级别从低到高排序，越低的隔离级别意味着更低的系统资源消耗（加锁情况越简单）和更高的并发性能，
同时也存在更多的数据正确性问题。MySQL 权衡了数据正确性和性能后，选择了 RR 作为默认的隔离级别

![shiwu2.png](https://i.loli.net/2019/09/21/kc8TNGBzFI2vZ3U.png)

### 事务相关的 SQL

- 提交事务
    ```SQL
    START TRANSACTION;
  
    ...
  
    COMMIT;
    ```
- 回滚事务
    ```SQL
    START TRANSACTION;
    
    ...
    
    ROLLBACK;  
    ```

> MySQL 默认 `autocommit` 参数是开启的

## 执行计划

执行计划提供以下帮助：

- 分析索引使用情况，提供优化依据
- 连接查询时，显示驱动表是哪个，被驱动表是哪个（驱动表在前面，被驱动表在后面），从而可以分析被驱动表的连接列上有没有用到索引

案例：

```SQL
mysql> explain select * from employees where emp_no = 10001;
+----+-------------+-----------+------------+-------+---------------+---------+---------+-------+------+----------+-------+
| id | select_type | table     | partitions | type  | possible_keys | key     | key_len | ref   | rows | filtered | Extra |
+----+-------------+-----------+------------+-------+---------------+---------+---------+-------+------+----------+-------+
|  1 | SIMPLE      | employees | NULL       | const | PRIMARY       | PRIMARY | 4       | const |    1 |   100.00 | NULL  |
+----+-------------+-----------+------------+-------+---------------+---------+---------+-------+------+----------+-------+
1 row in set, 1 warning (0.00 sec)
```

```SQL
mysql> explain select * from employees where first_name = "Georgi";
+----+-------------+-----------+------------+------+---------------+------+---------+------+--------+----------+-------------+
| id | select_type | table     | partitions | type | possible_keys | key  | key_len | ref  | rows   | filtered | Extra       |
+----+-------------+-----------+------------+------+---------------+------+---------+------+--------+----------+-------------+
|  1 | SIMPLE      | employees | NULL       | ALL  | NULL          | NULL | NULL    | NULL | 299379 |    10.00 | Using where |
+----+-------------+-----------+------------+------+---------------+------+---------+------+--------+----------+-------------+
1 row in set, 1 warning (0.00 sec)
```

执行计划报告解析：

总的来说，主要看 `type`、`possible_keys`、`key`、`rows` 这几个字段

- `possible_keys`：表示有哪些候选的索引
- `key`：表示最终用了哪个索引
- `type`：索引的效果指标，`const` > `ref` > `range` > `index` ~= `ALL`
  - `const`：聚簇索引或者二级索引、`where` 等值匹配、单表查询、查询结果只有单行匹配
  - `ref`：二级索引、`where` 等值匹配、查询结果有多行匹配
  - `range`：二级索引、`where` 范围匹配
  - `index`：扫描聚簇索引的所有叶子结点（效果约等于全表扫描）
  - `ALL`：全表扫描
- `row`：需要扫描的总行数（当然越少越好）

## 最佳实践

### DML

- 【强制】`SELECT` 必须指定字段名，禁止 `*`。因为多余的字段会浪费网络带宽、缓存能装下的数据也变少了，影响性能
- 【强制】`INSERT` 必须指定字段名
- 【建议】`INSERT` 多组值时，避免超过 5000 个。因为可能会增加主从延迟的时间
- 【建议】`IN` 列表的值不超过 500 个。因为可以减少底层扫描，提升性能
- 【强制】注意读写分离的使用，即写入和事务的时候使用主库，只读的时候使用从库
- 【强制】除了 100 行之内的小表，`SELECT` 语句必须带 `WHERE`，且使用索引
- 【强制】生产环境禁止使用hint，如sql_no_cache，force index，ignore key，straight join等。因为hint是用来强制SQL按照某个执行计划来执行，但随着数据量变化我们无法保证自己当初的预判是正确的，因此我们要相信MySQL优化器
- 【强制】`WHERE` 等号左右两边字段的数据类型必须一致，否则无法使用索引
- 【建议】`UPDATE|DELETE` 要有 `WHERE` 子句，而且能用到索引
- 【强制】`WHERE` 子句禁止只有 `LIKE` 条件，否则无法使用索引
- 【强制】索引列不要使用函数或者表达式，否则无法使用索引
- 【建议】如果 `OR` 无法使用到索引，可以将其优化成 `UNION`，比如：`WHERE a=1 OR b=2` 改成 `WHERE a=1… UNION …WHERE b=2;`
- 【建议】用条件过滤来分页查询的性能，比如：`SELECT a,b,c FROM t1 LIMIT 10000,20;` 改成 `SELECT a,b,c FROM t1 WHERE id>10000 LIMIT 20;`

### JOIN

- 【建议】不是用子查询，可以拆分成多个查询或者 `join`
- 【建议】线上环境，`join` 不超过三个表
- 【建议】`join` 的表名使用别名
- 【建议】使用结果集少的表作为驱动表

### 事务

- 【建议】事务隔离级别为 RR
- 【建议】事务里面的 SQL 不超过 5 个
- 【建议】将外部依赖调用移出事务，避免外部依赖发生问题导致事务执行时间过长
- 【建议】对于一致性要求高的业务场景，应该开启事务并访问主库

### 排序和分组

- 【建议】减少使用 `ORDER BY`，将排序放到应用程序中做。因为 `ORDER BY`、`GROUP BY`、`DISTINCT` 这些语句比较消耗 CPU
- 【建议】`ORDER BY`、`GROUP BY`、`DISTINCT` 这些语句尽量用上索引，直接查询出排好序的数据
- 【建议】`ORDER BY`、`GROUP BY`、`DISTINCT` 这些语句的结果集控制在 1000 左右，否则性能不好

### 禁止线上使用的 SQL

- 【强制】禁止 `IN` 子查询，比如：`update t1 set … where name in(select name from user where…);`。因为性能很差
- 【强制】禁止物理删除列，改成逻辑删除
- 【强制】禁用procedure、function、trigger、views、event、外键约束。因为这些消耗数据库资源，降低数据库实例可扩展性。推荐都在应用程序端实现
