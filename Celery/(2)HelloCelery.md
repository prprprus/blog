# Hello Celery

## RabbitMQ 的基本使用

RabbitMQ 提供了 rabbitmqctl 命令行工具

### 启动和停止 RabbitMQ

- 前台启动 `sudo rabbitmq-server`
- 后台启动 `sudo rabbitmq-server -detached`
- 停止 `rabbitmqctl stop`

> 用 Docker 启动的只需要 `docker exec <conrainer_id> rabbitmqctl stop` 即可

### 配置 RabbitMQ

RabbitMQ 的连接 URL 是 `amqp://<myuser>:<mypassword>@<localhost>:5672/<myvhost>`，因此需要创建对应的用户信息、虚拟主机信息、授权等

- 添加用户 `sudo rabbitmqctl add_user <myuser> <mypassword>`
- 添加虚拟主机 `sudo rabbitmqctl add_vhost <myvhost>`
- 设置用户标签 `sudo rabbitmqctl set_user_tags <myuser> <mytag>`
- 设置用户权限（三种权限为配置、写、读） `sudo rabbitmqctl set_permissions -p <myvhost> <myuser> ".*" ".*" ".*"`

> 1. 其他 Broker 的 URL 参考 [这里](https://kombu.readthedocs.io/en/latest/userguide/connections.html#urls)
> 2. 更多的 RabbitMQ 访问控制设置参考 [这里](https://www.rabbitmq.com/admin-guide.html#access-control)

## Celery 的基本使用

demo 结构：

```BASH
celery_demo
.
├── __init__.py
├── app.py
├── main.py
└── task
    ├── __init__.py
    ├── task1.py
    └── task2.py
```

### (1) 确保 Broker 和 Backend 已启动

### (2) 编写 Client 相关代码

在 app.py 中创建 Celery 对象，并进行配置

```python
from celery import Celery

app = Celery(
    main="celery_demo",
    broker="amqp://tiger:hzz2956195@localhost:5672/tiger_vhost",
    backend="redis://",
    timezone='Asia/Shanghai',
    enable_utc=True,
)

app.conf.update(
    result_expires=3600,
)
```

在 task 目录下创建任务模块

```python
# task1.py
from app import app


@app.task(bind=True)
def add(self, x, y):
    print("Run add(x, y)!")
    return x + y
```

```python
# task2.py
from app import app


@app.task(bind=True)
def sub(self, x, y):
    print("Run sub(x, y)!")
    return x - y
```

```python
# __init__.py
from task.task1 import *
from task.task2 import *
```

### (3) 启动 Worker

celery_demo 目录下执行

- 前台执行

```BASH
celery --app=task worker --concurrency=4 --loglevel=INFO
```

- 后台执行

```BASH
sudo celery multi start --app=task worker --concurrency=4 --loglevel=INFO
```

> 1. 后台执行对应的 pid 文件在 `/var/run/celery`，日志文件在 `/var/log/celery`
> 2. 停止后台执行：`sudo celery multi stop --app=task worker --concurrency=4 --loglevel=INFO`
> 3. 查看后台执行：`sudo celery multi show --app=task worker --concurrency=4 --loglevel=INFO`

看到类似输出就成功了

```BASH
[tasks]
  . task.task1.add
  . task.task2.sub

[xxxx-xx-xx 16:09:22,447: INFO/MainProcess] Connected to amqp://tiger:**@127.0.0.1:5672/tiger_vhost
[xxxx-xx-xx 16:09:22,479: INFO/MainProcess] mingle: searching for neighbors
[xxxx-xx-xx 16:09:23,573: INFO/MainProcess] mingle: all alone
[xxxx-xx-xx 16:09:23,669: INFO/MainProcess] celery@Tiger-3.local ready.
```

### (4) 测试

测试代码

```python
# main.py
from task.task1 import add
from task.task2 import sub


if __name__ == "__main__":
    res = add.delay(1, 2)
    print(res.ready())   # False 任务是否完成
    print(res.get())    # 3 获取任务结果

    res = sub.delay(11, 2)  # 9
    print(res.get(timeout=3, propagate=False))  # 当任务发生异常时，不会抛到应用程序
    print(res.traceback)  # None 主动获取任务的异常信息
```

Worker 所在终端的日志信息大致如下

```BASH
[xxxx-xx-xx 16:10:37,007: INFO/MainProcess] Received task: task.task1.add[190dee0d-df36-4401-8e1d-f427038170ea]
[xxxx-xx-xx 16:10:37,015: WARNING/ForkPoolWorker-2] Run add(x, y)!
[xxxx-xx-xx 16:10:37,037: INFO/ForkPoolWorker-2] Task task.task1.add[190dee0d-df36-4401-8e1d-f427038170ea] succeeded in 0.022642897999999967s: 3
[xxxx-xx-xx 16:10:37,043: INFO/MainProcess] Received task: task.task2.sub[06e7e232-d416-46af-99dc-7eb78909dbe6]
[xxxx-xx-xx 16:10:37,044: WARNING/ForkPoolWorker-2] Run sub(x, y)!
[xxxx-xx-xx 16:10:37,049: INFO/ForkPoolWorker-2] Task task.task2.sub[06e7e232-d416-46af-99dc-7eb78909dbe6] succeeded in 0.005043328999999375s: 9
```
