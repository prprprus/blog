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
SELECT [DISTINCT] <column_name>, [AGG_FUNC(<column_name>/<expression>)]
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
3. GROUP BY
4. HAVING
5. SELECT...AGG_FUNC
6. DISTINCT
7. ORDER BY
8. LIMIT

> `DISTINCT` 是一个性能不怎么好的操作，谨慎使用

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

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/SQL%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0-1.png)

三表连接案例：

```SQL
SELECT pi.imageid,p.ProductCode,pi.filepath,p.name,p.defaultimage
FROM ProductInfringeReport pir INNER JOIN Product p ON pir.ProductCode = p.ProductCode
    INNER JOIN ProductImage pi ON p.ProductID = pi.ProductID
WHERE pir.adddatetime>"{}";
```

### 分组



### 排序

### 聚合运算

## 事务

## 执行计划

## 最佳实践
