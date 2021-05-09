# threading 学习笔记

## Python 的并发处理

由于 GIL 的存在，无论 CPU 有多少个核心，一个时刻只能有一个线程使用 CPU 资源，所以 Python 线程是并发不是并行，适用于 IO 密集型任务，
不适用于 CPU 密集型任务

更进一步，Python 并发处理的选择：

- 如果是 IO 密集型任务，并且每个 IO 操作很慢，又需要很多任务并发执行，使用基于协程的异步方案解决（譬如 [asyncio](https://github.com/hsxhr-10/Blog/blob/master/Python/IO/asyncio%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0.md) ）
- 如果是 IO 密集型任务，但是每个 IO 操作很快，只需要有限数量的任务并发执行，使用多线程即可
- 如果是 CPU 密集型任务，使用多进程

## threading 的使用

### 模块级别的函数

```python
import threading


def main():
    t = threading.Thread(target=lambda: print("Hello threading"))
    t.start()
    
    print("当前活动线程总数:", threading.active_count())
    print(threading.current_thread() == threading.main_thread())
    print("主线程 ID:", threading.get_ident())
    print("活动线程列表:", threading.enumerate())
    print("锁的最大超时时间:", threading.TIMEOUT_MAX)
    print("main thread done.")


main()
```

### 线程对象

```python
import threading
import time


def task(i):
    time.sleep(2)
    print("task {} done.".format(i))


def main():
    # 创建线程对象
    t = threading.Thread(target=task, args=(-1, ))

    print("线程对象名字:", t.name)    #  Thread-1
    t.name = "ooxoo"
    print("新的线程对象名字:", t.name)  # ooxoo
    print("线程对象 ID:", t.ident)  # None
    print("线程对象是否已启动:", t.is_alive())    # False
    
    # 启动线程, start() 调用 run() 方法, run() 会执行 task() 函数
    t.start()

    print("线程对象是否已启动:", t.is_alive())    # True
    print("线程对象 ID:", t.ident)  # 123145445199872
    
    # 等待线程 t 执行完毕
    t.join()

    print("main thread done.")


main()
```

#### daemon

主线程结束后，如果想让其创建的子线程跟着自动结束，可以通过设置守护线程实现 

```python
import threading
import time


def task(i):
    time.sleep(2)
    print("task {} done.".format(i))


def main():
    workers = []
    for i in range(5):
        t = threading.Thread(target=task, args=(i,))

        # 设置成守护线程
        t.daemon = True

        workers.append(t)

    for worker in workers:
        worker.start()

    print("main thread done.")


main()
```

程序只会输出 main thread done.

### 线程同步

#### (1) 锁

`threading.Lock()` 可以创建锁对象，自动返回当前系统所支持的锁类型

`acquire(blocking=True, timeout=-1)` 函数说明：

- 将锁对象状态设置成 locked（申请锁），如果成功则返回 True，否则返回 False
- blocking 为 True 代表阻塞等待，直到申请锁成功，或者超时为止；为 False 代表如果不能立即申请锁，则不等待直接返回
- timeout 为 -1 代表一直等待下去，`threading.TIMEOUT_MAX` 可以获取系统支持的最大等待时间

`release()` 函数说明：

- 将锁对象状态设置成 unlocked（释放锁），可以在任意线程中执行，不一定是持有锁的线程。该函数没有返回值
- 如果有多个线程在等待锁，会随机选择一个线程持有锁
- 如果释放 unlocked 的锁对象，则会抛出 RuntimeError

`locked()` 函数说明：如果锁对象状态为 locked 则返回 True，否则返回 False

#### (2) 可重入锁

#### (3) 条件变量

#### (4) 信号量

#### (5) Event

#### (6) 定时器

#### (7) Barrier

## queue 模块

[queue](https://docs.python.org/3/library/queue.html) 模块提供了多种线程安全的队列，用于线程间通信
