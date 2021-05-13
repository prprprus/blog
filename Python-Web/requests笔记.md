# requests 笔记

## requests 的使用

### Session() 方法

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

### 常见用法

```BASH
import requests
import shutil

# GET
r = requests.get(<url>, params=<dict>, headers=<headers>)

# POST
r = requests.post(<url>, data=<dict>, headers=<headers>)
r = requests.post(<url>, json=<dict>, headers=<headers>)

# 上传
files = [
    ("image", open("test.png", "rb")),
]
r = requests.post(<url>, data=<dict>, files=files, headers=<headers>)

# 下载
r = requests.get(url=<url>, stream=True, headers=<headers>)

with open('./test.png', 'wb') as out_file:
    shutil.copyfileobj(response.raw, out_file)
    
# 客户端证书验证
r = requests.get(<url>, cert=("/path/client.cert", "/path/client.key"))

# SSL 证书验证
r = requests.get(<url>, verify="/path/to/certfile")
r = requests.get(<url>, verify=False)
```
