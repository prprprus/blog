# Hive 笔记

Hive 是 Hadoop 全家桶中的一个数据仓库工具，提供一个查询引擎和 HQL 查询语言（语法类似 SQL），可以将底层存储的结构化数据映射成一张表，
并将 HQL 翻译成 MapReduce 程序，避免写 MapReduce 程序，降低了使用难度

Hadoop 有三个核心组件：

- HDFS：提供了将海量文件分布式存储在集群上的功能
- MapReduce：提供了在集群上进行分布式并行计算的功能
- YARN：分配运算资源，调度 MapReduce 任务

Hive 就是在 HDFS 或者 HBase 之上的组件，用来查询存储在那些地方的数据。Hive 执行一般很慢，不适合做实时处理，只用来作离线处理

## Hive 基础

### 数据类型

Hive 支持大多数关系型数据库的类型，还额外支持三中集合类型：STRUCT、MAP、ARRAY。其中 MAP、ARRAY 类似 Java 中的 MAP 和 ARRAY，STRUCT 类似 C 中的 STRUCT。
详细参考 [这里](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Types)

Hive 支持隐式类型转换，当比较 `FLOAT` 和 `DOUBLE` 时，`FLOAT` 会转换成 `DOUBLE`。所有类型的隐式转换原则是转成更大表示范围的类型。也可以显式类型转换，
譬如 `CAST('1' AS INT)`

### 文件格式

Hive 常用的四种分隔符:

- `\n`：用来分割行
- `\001`：用来分割字段
- `\002`：用来分割 ARRAY、STRUCT 中的元素
- `\003`：用来分割 MAP 中的键值对

### 建表例子

```HQL
CREATE TABLE students(
  name      STRING,   -- 姓名
  age       INT,      -- 年龄
  subject   ARRAY<STRING>,   --学科
  score     MAP<STRING,FLOAT>,  --各个学科考试成绩
  address   STRUCT<houseNumber:int, street:STRING, city:STRING, province：STRING>  --家庭居住地址
) ROW FORMAT DELIMITED FIELDS TERMINATED BY "\001"-- 列分隔符
COLLECTION ITEMS TERMINATED BY "\002"--MAP STRUCT 和 ARRAY 的分隔符(数据分割符号)
MAP KEYS TERMINATED BY "\003"-- MAP 中的 key 与 value 的分隔符
LINES TERMINATED BY "\n"-- 行分隔符
```

### DDL 和 DML

这部分使用上和 SQL 差不多，一些明显的差别有：

- `INSERT INTO` 是追加插入数据，原有的数据不会被删除
- `INSERT OVERWRITE` 是覆盖插入，原有的数据会被删除
- `INSERT INTO` 时必须提供完整的列，不支持插入复杂类型

## Python 中使用 Hive

利用 impyla 作为 Hive 驱动，pandas 操作结果

安装相关依赖

```BASH
pip install impyla==0.13.8
pip install pandas==0.25.3
```

```python
from impala.dbapi import connect as hive_connect
import pandas as pd


class UtilHive:
    def __new__(cls, *args, **kwargs):
        if not hasattr(UtilHive, "_instance"):
            UtilHive._instance = object.__new__(cls)
        return UtilHive._instance

    def __init__(self, host="127.0.0.1", port=10001, user="xxx", password="xxx", database="xxx", auth_mechanism="PLAIN"):
        """
        auth_mechanism 的值取决于 hive - site.xml 里边的一个配置
        <name>hive.server2.authentication</name>
        <value>NOSASL</value>

        :param host:
        :param port:
        :param user:
        :param password:
        :param database:
        :param auth_mechanism: 默认值 None, 取值 NOSASL, PLAIN, KERBEROS, LDAP
        """
        self.host = host
        self.port = port
        self.user = user
        self.password = password
        self.database = database
        self.auth_mechanism = auth_mechanism
        self.__conn = hive_connect(host=self.host, port=self.port, user=self.user, password=self.password,
                                   database=self.database, auth_mechanism=self.auth_mechanism)

    def ping(self):
        """如果连接断开，则重新连接"""
        pass

    def close(self):
        """关闭数据库连接"""
        self.__conn.close()
        
    def get_connection(self):
        return self.__conn


def demo():
    conn = UtilHive().get_connection()
    hql = "xxx"

    try:
        df = pd.read_sql(hql, conn)
        print("---> df:", df)
    except Exception as e:
        print('查询 hive 错误: {}'.format(e))
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    demo()
```
