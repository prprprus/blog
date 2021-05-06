# asyncio 学习笔记

- TODO：事件循环涉及的任务：IO、定时、callback

Python 有不少异步 IO 框架，但是根据标准的 IO 模型，很多时候说的异步 IO 并不是真正的异步，而是 IO 多路复用。asyncio 也不例外，
对于 IO 部分，asyncio 是基于 [selector](https://github.com/python/cpython/blob/3.9/Lib/asyncio/selector_events.py) 模块，
而 selector 基于 select，select 基于操作系统提供的 IO 多路复用机制，譬如 Linux 的 epoll，macOS 的 kqueue 等

但是 asyncio 也不是只能用于 IO 操作，它所提供的事件循环、Future、线程池、进程池等工具，也可以对一般的代码进行异步化，实现并发、并行的效果

asyncio 的特点和主流的异步框架（tornado、gevent）差不多，主要有以下几点：

- 擅长处理 IO 密集型任务，相比线程昂贵（创建、销毁、上下文切换），asyncio 里的协程要轻量级很多，
可以同时存在大量的协程，因此可以较好地提升应用的吞吐量，但是应用的响应时间还是要看单个协程的处理时长，跟异步与否无关
- 事件循环不能被阻塞，也就是不能存在阻塞代码，无论是自己写的、标准库的、还是第三方库的代码。这就需要搭配线程池、进程池等工具，或者要求生态要好，不然连个异步的数据库驱动都没有，也挺麻烦 😂
- 不擅长 CPU 密集型任务，可以结合进程池、Celery 等工具稍微解决这个问题

> Python 虚拟机大概执行 200 个字节码就进行一次线程切换的系统调用

> asyncio 需要 Python3.5+，最好是 Python3.7+，功能会多一些，少量新功能需要 Python3.9。另外，asyncio 的接口存在不向后兼容的情况，
> 譬如 ["Deprecated since version 3.8, will be removed in version 3.10: The loop parameter."](https://docs.python.org/3/library/asyncio-task.html#asyncio.sleep) 这类

## 事件循环

### IO 多路复用

基于 IO 多路复用的事件循环大致长这样：

```python
# 回调函数映射表
callbacks = {}

while True:
    event_list = epoll.wait(timeout)
    for fd, event in event_list:
        if event == 连接就绪:
            # 为 fd 注册读写事件
            # 注册回调函数
            ...
        elif event == 读就绪:
            # 执行对应回调函数
            ...
        elif event == 写就绪:
            # 执行对应回调函数
            ...
        elif event == 中断事件:
            # 关闭连接, 取消注册事件, 回收相关资源
            ...
        else:
            # 默认操作
            ...
```



## 协程

### 协程的定义

- 协程底层是基于生成器，区别是生成器 `yield` 出的是基础类型或者容器类型，协程 `yield` 出的只能是 `None` 或者 Future 对象
- 从上层看，可以认为被 `async` 关键字声明的异步函数就是一个协程
- 直接调用协程并不会执行，必须在前面加 `await`，而且需要放到事件循环中执行
- `await` 的语义是当遇到阻塞时，主动让出执行时间给其他协程

```python
import asyncio


async def sleep(second):
    # asyncio 提供的异步 sleep() 方法, 不会阻塞事件循环,
    # 如果换成 time.sleep() 就会阻塞事件循环了
    await asyncio.sleep(second)    
    print("hello asyncio")


async def test_sleep(second):
    # 并发执行 10 个协程
    await asyncio.gather(
        sleep(second),
        sleep(second),
        sleep(second),
        sleep(second),
        sleep(second),
        sleep(second),
        sleep(second),
        sleep(second),
        sleep(second),
        sleep(second),
    )
    

async def test_return():
    return 21


async def main():    
    # 测试并发执行一组协程
    await test_sleep(1)
    
    # 测试带返回值的协程
    res = await test_return()
    print(res)

    
if __name__ == "__main__":
    # asyncio.run() 一般作为 asyncio 应用的入口, 只被调用一次
    asyncio.run(main())
```

> 只考虑 async/await 的写法，不考虑旧的 @asyncio.coroutine 写法

Profile 一下可以看到耗时 1039ms，确实并发执行了

### Python 协程和 Go 协程的区别

- coroutine 基于 asyncio 事件循环的调度，运行在一个线程上，当线程被阻塞，所有的 coroutine 会被阻塞，一般用于 IO 密集型任务，而且需要配合异步库使用。
总的来说是并发，不是并行
- goroutine 基于 Go 运行时 GPM 模型的调度，一般运行在多个线程上，当某一个线程被阻塞时，其他 goroutine 还可以运行在其他的线程上，既可以用于 IO 密集型任务，也可以用于
CPU 密集型任务。总的来说即使并发，也是并行

## await

`await` 可以作用于三类对象：协程、Task、Future。它们都可以指代可能会发生阻塞的未完成任务，都需要放到事件循环中执行

### 设置超时时间

```python
import asyncio


async def sleep(second):
    await asyncio.sleep(second)
    print("hello asyncio")


async def test_timeout(second):
    try:
        await asyncio.wait_for(sleep(second), timeout=2)
    except asyncio.TimeoutError:
        print("asyncio.TimeoutError")
        

async def main():
    # 测试超时时间
    await test_timeout(5)


if __name__ == "__main__":
    asyncio.run(main())
```

## Task

Task 是指用来运行协程的类似 Future 的对象（继承于 Future 类）。官网不建议手动创建 Task 对象，而是通过 `asyncio.create_task(aws)` 等 API 创建。
相比协程，Task 提供更多操作可供控制

```python
import asyncio


def done_callback(task):
    try:
        print(task.result())
        print("run done_callback()")
    except asyncio.CancelledError:
        pass
    

def remove_callback():
    print("run remove_callback()")
    

async def test_task():
    async def say():
        await asyncio.sleep(2)
        return "hello asyncio task"

    task = asyncio.create_task(say())
    
    # 判断 Task 是否取消
    print(task.cancelled())

    # 判断 Task 是否完成
    print(task.done())
    
    # # 输出 Task 名字, 需要 Python3.8+
    # print(task.get_name())
    # 
    # # 输出 Task 对应的协程, 需要 Python3.8+
    # print(task.get_coro())
    
    # 设置 done callback
    task.add_done_callback(done_callback)
    
    # 设置 remove callback
    task.remove_done_callback(remove_callback)
    
    # 取消 Task
    # task.cancel()
    
    try:
        await task
    except asyncio.CancelledError:
        print("asyncio task cancelled")


asyncio.run(test_task())
```

## Future

Future 和 Task 提供操作差不多，[参考](https://docs.python.org/3/library/asyncio-future.html#future-object)

## 模块级别函数

- 异步 sleep：`asyncio.sleep(second)`
- 并发执行一组协程：`asyncio.gather([aws])`
- 将协程丢到事件循环执行：`asyncio.run(aws)`
- 创建 Task：`asyncio.create_task(aws)`
- 判断是否异步函数：`asyncio.iscoroutinefunction(func)`
- 输出当前协程：`asyncio.current_task()`
- 输出所有协程：`asyncio.all_tasks()`
