# Gunicorn 笔记

在线上环境中，Gunicorn 一般会搭配 gevent 处理并发，下面针对常见的场景做一下实验，验证如何正确使用它们。
为了避免进程对结果的影响，Gunicorn 只会启动一个进程

> 如果 worker 类型选择了 gevent，Gunicorn 在初始化进程时就会调用 `monkey.patch_all()`
> https://github.com/benoitc/gunicorn/blob/master/gunicorn/workers/ggevent.py#L143
> https://github.com/benoitc/gunicorn/blob/master/gunicorn/workers/ggevent.py#L38

## 单个协程并发处理 IO

测试代码如下：

```python
import json
import time

from flask import Flask, Response
import requests
import gevent

app = Flask(__name__)

URLS = ["http://www.example.com"] * 5


def _download(url):
    r = requests.get(url)
    return r.status_code


def sync_download():
    return list(map(_download, URLS))


def async_download():
    coroutines = [gevent.spawn(_download, url) for url in URLS]
    gevent.joinall(coroutines)
    return [coroutine.value for coroutine in coroutines]
```

### Case1

```python
@app.route("/hello", methods=["GET"])
def handle():
    s = time.time()
    print(sync_download())
    total = time.time() - s

    resp = Response(json.dumps({"code": 0, "message": "success", "data": total}, ensure_ascii=False))
    resp.headers["Content-Type"] = "application/json; charset=utf-8"
    return resp
```

`gunicorn -w 1 -k gevent -b 0.0.0.0:12345 demo1:app`

结果：耗时约 2.5s

### Case2 ✅

```python
@app.route("/hello", methods=["GET"])
def handle():
    s = time.time()
    print(async_download())
    total = time.time() - s

    resp = Response(json.dumps({"code": 0, "message": "success", "data": total}, ensure_ascii=False))
    resp.headers["Content-Type"] = "application/json; charset=utf-8"
    return resp
```

`gunicorn -w 1 -k gevent -b 0.0.0.0:12345 demo1:app`

结果：耗时约 0.4s

## 协程之间的非阻塞

后端应用程序一般离不开数据库，使用 SQLAlchemy 操作 MySQL 就是一个常见的场景。
以为 [这个项目](https://github.com/hsxhr-10/Notes/blob/master/Python-Web/Flask/flask-sqlalchemy/README.md) 作为案例，
SQLAlchemy 版本是 1.4.15，pymysql 版本是 1.0.2（pymysql 是阻塞的 MySQL 驱动）

### 测试步骤

> 主要修改项目中 main.py 和连接池的配置

1. 为了避免其他因素的影响，把数据库连接池的大小设置成 1
2. 创建两个接口模拟慢 SQL，接口 A 执行 `SELECT SLEEP(5);` 睡 5s，接口 B 执行 `SELECT SLEEP(20);` 睡 20s
3. 分别用两个客户端发起请求，先调用接口 B，再调用接口 A
4. 在不同的条件下，观察接口 A 能否在接口 B 之前返回，也就是能否实现非阻塞的效果

### Case1 ✅

```python
@app.route("/hello")
def handle():
    def _db_sleep():
        sql = text("SELECT SLEEP(5);")
        res = dbengine.execute(sql)
        print(res)

    s = time.time()
    _db_sleep()
    total = time.time() - s

    data = {"code": 0, "message": "success", "data": total}
    result = json.dumps(data, ensure_ascii=False)
    response = Response(result, content_type="application/json; charset=utf-8")
    return response


@app.route("/bye")
def handle1():
    def _db_sleep():
        sql = text("SELECT SLEEP(20);")
        res = dbengine.execute(sql)
        print(res)

    s = time.time()
    _db_sleep()
    total = time.time() - s

    data = {"code": 0, "message": "success", "data": total}
    result = json.dumps(data, ensure_ascii=False)
    response = Response(result, content_type="application/json; charset=utf-8")
    return response
```

`gunicorn -w 1 -k gevent -b 0.0.0.0:12345 main:app`

结果：接口 A 先返回，接口 B 后返回，耗时约 20s，能实现非阻塞效果

## 总结

1. gevent 在 Gunicorn 中的表现基本没什么不同，最大区别是不需要手动执行 `monkey.patch_all()`，一旦 Gunicorn 在 gevent
   模式下启动完成，默认 IO 库就会被替换成非阻塞版本，也就是相应的函数会变成 gevent 协程，可以被 gevent 的事件循环调度（类似 asyncio）
2. gevent 能确保 Gunicorn 中每个请求（一个请求对应一个 gevent 协程）的非阻塞效果
3. 如果想在单个 gevent 协程中实现并发效果，需要 spawn 出额外的协程
