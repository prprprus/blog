# Gunicorn 笔记

在线上环境中，Gunicorn 一般会搭配 gevent 处理并发，下面针对常见的场景做一下实验，验证如何正确使用它们。为了避免进程对结果的影响，Gunicorn 只启动一个进程

> 如果 worker 类型选择了 gevent，Gunicorn 在初始化进程时就会调用 `monkey.patch_all()`
> https://github.com/benoitc/gunicorn/blob/master/gunicorn/workers/ggevent.py#L143
> https://github.com/benoitc/gunicorn/blob/master/gunicorn/workers/ggevent.py#L38

## requests 场景

应用程序经常需要作为客户端去调用其他的服务，使用 requests 库就是一个高频场景

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

### Case2：Gunicorn sync + 非阻塞 IO

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

### Case4：Gunicorn gevent + 非阻塞 IO

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

### 结论

对于 Gunicorn + gevent 的组合，IO 操作必须被封装成 gevent 协程，才能发挥这套组合的效果。可以类比 asyncio 框架，必须把 IO 操作封装到 async 函数中，不然没法进行异步调度  

## 数据库场景

