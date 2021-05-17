# Gunicorn 笔记

在线上环境中，Gunicorn 一般会搭配 gevent 处理并发，下面针对常见的场景做一下实验，验证如何正确使用它们。
为了避免进程对结果的影响，Gunicorn 只会启动一个进程

> 如果 worker 类型选择了 gevent，Gunicorn 在初始化进程时就会调用 `monkey.patch_all()`
> https://github.com/benoitc/gunicorn/blob/master/gunicorn/workers/ggevent.py#L143
> https://github.com/benoitc/gunicorn/blob/master/gunicorn/workers/ggevent.py#L38

## 单个处理并发 IO

测试代码如下：

```python
import json
import time

from flask import Flask, Response
import requests
import gevent

app = Flask(__name__)

URLS = [
    "http://www.qq.com",
    "http://www.163.com",
    "http://www.example.com",
    "http://www.nowamagic.net/academy/detail/13321037",
    "http://www.example.com",
    "http://www.example.com",
    "http://www.example.com",
    "http://www.example.com",
    "http://www.example.com",
    "http://www.example.com",
]


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

### Case1：Gunicorn sync + 阻塞 IO

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

`gunicorn -w 1 -b 0.0.0.0:12345 demo1:app`

结果：耗时约 2.5s

### Case2：Gunicorn sync + 非阻塞 IO（gevent）

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

`gunicorn -w 1 -b 0.0.0.0:12345 demo1:app`

结果：耗时约 2.5s

### Case3：Gunicorn gevent + 阻塞 IO

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

### Case4：Gunicorn gevent + 非阻塞 IO（gevent） ✅

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

### Case5：Gunicorn sync + 非阻塞 IO（多线程） ✅

```python
@app.route("/hello", methods=["GET"])
def handle():
    s = time.time()
    print(async_download_by_thread())
    total = time.time() - s

    resp = Response(json.dumps({"code": 0, "message": "success", "data": total}, ensure_ascii=False))
    resp.headers["Content-Type"] = "application/json; charset=utf-8"
    return resp
```

`gunicorn -w 1 -b 0.0.0.0:12345 demo1:app`

结果：耗时约 0.4s

### Case6：Gunicorn gevent + 非阻塞 IO（多线程）✅

```python
@app.route("/hello", methods=["GET"])
def handle():
    s = time.time()
    print(async_download_by_thread())
    total = time.time() - s

    resp = Response(json.dumps({"code": 0, "message": "success", "data": total}, ensure_ascii=False))
    resp.headers["Content-Type"] = "application/json; charset=utf-8"
    return resp
```

`gunicorn -w 1 -k gevent -b 0.0.0.0:12345 demo1:app`

结果：耗时约 0.4s

> Case5 和 Case6 由于真正干活的是线程，所以 Gunicorn 的 worker 模式并没有影响

## 数据库场景

后端应用程序一般离不开数据库，使用 SQLAlchemy 操作 MySQL 就是一个常见的场景

以为 [这个项目](https://github.com/hsxhr-10/Notes/blob/master/Python-Web/Flask/flask-sqlalchemy/README.md) 作为案例，
SQLAlchemy 版本是 1.4.15，pymysql 版本是 1.0.2（pymysql 是阻塞的 MySQL 驱动）

### 测试步骤

> 主要修改项目中 main.py 和连接池的配置

1. 为了避免其他因素的影响，把数据库连接池的大小设置成 1
2. 创建两个接口模拟慢 SQL，接口 A 执行 `SELECT SLEEP(5);` 睡 5s，接口 B 执行 `SELECT SLEEP(20);` 睡 20s
3. 分别用两个客户端发起请求，先调用接口 B，再调用接口 A
4. 在不同的条件下，观察接口 A 能否在接口 B 之前返回，也就是能否实现非阻塞的效果

### Case1：Gunicorn sync + 阻塞 IO

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

`gunicorn -w 1 -b 0.0.0.0:12345 main:app`

结果：接口 B 先返回，接口 A 后返回，耗时约 25s，不能实现非阻塞效果

### Case2：Gunicorn sync + 非阻塞 IO（gevent）

```python
@app.route("/hello")
def handle():
    def _db_sleep():
        sql = text("SELECT SLEEP(5);")
        res = dbengine.execute(sql)
        print(res)

    s = time.time()
    gevent.joinall([gevent.spawn(_db_sleep)])
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
    gevent.joinall([gevent.spawn(_db_sleep)])
    total = time.time() - s

    data = {"code": 0, "message": "success", "data": total}
    result = json.dumps(data, ensure_ascii=False)
    response = Response(result, content_type="application/json; charset=utf-8")
    return response
```

`gunicorn -w 1 -b 0.0.0.0:12345 main:app`

结果：接口 B 先返回，接口 A 后返回，耗时约 25s，不能实现非阻塞效果

### Case3：Gunicorn gevent + 阻塞 IO

测试代码同 Case1

`gunicorn -w 1 -k gevent -b 0.0.0.0:12345 main:app`

结果：接口 A 先返回，接口 B 后返回，耗时约 20s，能实现非阻塞效果

### Case4：Gunicorn gevent + 非阻塞 IO（gevent）

测试代码同 Case2

`gunicorn -w 1 -k gevent -b 0.0.0.0:12345 main:app`

结果：接口 A 先返回，接口 B 后返回，耗时约 20s，能实现非阻塞效果

## 总结

Gunicorn + gevent 可以确保请求之间非阻塞，先完成的请求先返回。但是如果请求里面有阻塞 IO，那么这个请求还是会被阻塞

对于 Gunicorn + gevent 的组合，IO 操作需要用 gevent 协程或者多线程封装起来，才能发挥这套组合的效果。
可以类比 asyncio 框架，必须把 IO 操作封装到 async 函数中，不然事件循环没法进行调度