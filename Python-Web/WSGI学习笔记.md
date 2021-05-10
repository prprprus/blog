# WSGI 学习笔记

WSGI 是 Python Web 的一种协议和规范，分为应用端（Flask、Tornado、Django 等 Web 框架）和服务端（Gunicorn、uWSGI 等应用服务器）。
WSGI 解耦了应用端和服务端，让它们可以灵活搭配使用，譬如 Flask 既可以运行在 Gunicorn 上，也可以运行在 uWSGI 上。
其他编程语言也有类似的协议，譬如 Java 的 Servlet

> uwsgi 也是一种 Python Web 协议，而 uWSGI 是既实现了 WSGI 协议，又实现了 uwsgi 协议的服务端

## 简单实现

### WSGI 应用端

```python
class AppClass:
 
    def __call__(self, environ, start_response):
        status = '200 OK'
        response_headers = [('Content-type', 'text/plain')]
        start_response(status, response_headers)
        return ["Hello WSGI"]
```

应用端需要满足三个要求：

- 是一个可调用对象，譬如函数、实现了 `__call__()` 方法的类
- 接受两个参数，environ（一个字典，包含 WSGI 环境信息） 和 start_response（该函数由服务端实现，并传给应用端，主要功能是组装 HTTP headers、返回 `write()` 方法）
- 返回一个可迭代对象

### WSGI 服务端

```python
import os, sys

enc, esc = sys.getfilesystemencoding(), 'surrogateescape'

def unicode_to_wsgi(u):
    # Convert an environment variable to a WSGI "bytes-as-unicode" string
    return u.encode(enc, esc).decode('iso-8859-1')

def wsgi_to_bytes(s):
    return s.encode('iso-8859-1')

def run_wsgi(application):
    environ = {k: unicode_to_wsgi(v) for k,v in os.environ.items()}
    environ['wsgi.input']        = sys.stdin.buffer
    environ['wsgi.errors']       = sys.stderr
    environ['wsgi.version']      = (1, 0)
    environ['wsgi.multithread']  = False
    environ['wsgi.multiprocess'] = True
    environ['wsgi.run_once']     = True

    if environ.get('HTTPS', 'off') in ('on', '1'):
        environ['wsgi.url_scheme'] = 'https'
    else:
        environ['wsgi.url_scheme'] = 'http'

    headers_set = []
    headers_sent = []

    def write(data):
        out = sys.stdout.buffer

        if not headers_set:
             raise AssertionError("write() before start_response()")

        elif not headers_sent:
             # Before the first output, send the stored headers
             status, response_headers = headers_sent[:] = headers_set
             out.write(wsgi_to_bytes('Status: %s\r\n' % status))
             for header in response_headers:
                 out.write(wsgi_to_bytes('%s: %s\r\n' % header))
             out.write(wsgi_to_bytes('\r\n'))

        out.write(data)
        out.flush()

    def start_response(status, response_headers, exc_info=None):
        if exc_info:
            try:
                if headers_sent:
                    # Re-raise original exception if headers sent
                    raise exc_info[1].with_traceback(exc_info[2])
            finally:
                exc_info = None     # avoid dangling circular ref
        elif headers_set:
            raise AssertionError("Headers already set!")

        headers_set[:] = [status, response_headers]

        # Note: error checking on the headers should happen here,
        # *after* the headers are set.  That way, if an error
        # occurs, start_response can only be re-called with
        # exc_info set.

        return write

    result = application(environ, start_response)
    try:
        for data in result:
            if data:    # don't send headers until body appears
                write(data)
        if not headers_sent:
            write('')   # send headers now if body was empty
    finally:
        if hasattr(result, 'close'):
            result.close()
```
