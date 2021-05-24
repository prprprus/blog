# requests 笔记

requests 是一个常用的 HTTP/HTTPS 客户端，对应的是阻塞 IO，想要非阻塞 IO 可以配合线程、gevent 使用，或者用 [httpx](https://github.com/encode/httpx)

## requests 的使用

### Session 对象

`Session()` 会使用底层库 urllib3 的连接池，对 TCP 连接进行复用，对于向同一个目标主机进行多次请求的场景会有性能提升

```python
import requests

s = requests.Session()
s.get('https://httpbin.org/cookies')
```

版本比较新的 requests（譬如 2.25.1） 默认使用 `Session()`，源码 api.py:60

```python
def request(method, url, **kwargs):
    # 忽略大段大段的注释
    pass

    with sessions.Session() as session:
        return session.request(method=method, url=url, **kwargs)
```

### 常用配置

```python
import requests

# 连接池大小
requests.adapters.DEFAULT_POOLSIZE = 30
# 重试次数
requests.adapters.DEFAULT_RETRIES = 3

s = requests.Session()

r = s.get("http://www.example.org")
print(r.status_code)
```

### 常用操作

```BASH
import requests
import shutil

# GET
r = requests.get(<url>, params=<dict>, headers=<headers>, timeout=<timeout>)

# POST
r = requests.post(<url>, data=<dict>, headers=<headers>, timeout=<timeout>)
r = requests.post(<url>, json=<dict>, headers=<headers>, timeout=<timeout>)

# 上传
files = [
    ("image", open("test.png", "rb")),
]
r = requests.post(<url>, data=<dict>, files=files, headers=<headers>, timeout=<timeout>)

# 下载
r = requests.get(url=<url>, stream=True, headers=<headers>, timeout=<timeout>)

with open('./test.png', 'wb') as out_file:
    shutil.copyfileobj(response.raw, out_file)
    
# 客户端证书验证
r = requests.get(<url>, cert=("/path/client.cert", "/path/client.key"), timeout=<timeout>)

# SSL 证书验证
r = requests.get(<url>, verify="/path/to/certfile", timeout=<timeout>)
r = requests.get(<url>, verify=False, timeout=<timeout>)
```
