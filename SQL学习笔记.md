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

- 可以按照某个字段做排序
- 也可以分别对多个列做排序，比如 `ORDER BY <COLUMN_1> DESC , <COLUMN_2> ASC;`
- `DESC` 代表降序，`ASC` 代表升序，默认是升序排序

## 事务

事务可以确保一组 SQL 语句要么成功，要么不成功

- MySQL 默认 `autocommit` 参数是开启的，要使用事务要先将这个参数禁止掉
- 使用事务用
    ```bash
    START TRANSACTION;
  
    ...
  
    COMMIT;
    ```
- 回滚事务
    ```bash
    START TRANSACTION;
    
    ...
    
    ROLLBACK;  
    ```

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
