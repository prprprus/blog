# asyncio 学习笔记

asyncio 一般被叫做异步框架，但是根据标准的 IO 模型，很多时候说的异步框架并不是真正的异步，而是 IO 多路复用。asyncio 也不例外，
是基于 [selector](https://github.com/python/cpython/blob/3.9/Lib/asyncio/selector_events.py) 模块做的 IO 多路复用框架，
而 selector 基于 select，select 基于操作系统提供的 IO 多路复用机制，譬如 Linux 的 epoll，macOS 的 kqueue 等

> 下面说的异步、异步框架实际指的是 IO 多路复用

asyncio 的特点和主流的异步框架（tornado、gevent）差不多，主要有以下几点：

- 适用于 IO 密集型任务，相比线程昂贵（创建、销毁、上下文切换），asyncio 里的协程要轻量级很多，
可以存在大量的协程，因此可以较好地提升应用的吞吐量，但是应用的响应时间还是要看单个协程的处理时长，跟异步与否无关
- 事件循环不能被阻塞，也就是不能存在阻塞代码，无论是自己写的、标准库的、还是第三方库的代码。这就要求生态要好，不然连个异步的数据库驱动都没有，也挺麻烦 😂
- 理论上不适用于 CPU 密集型任务，可以结合进程池、Celery 等工具解决这个问题

> asyncio 需要 Python3.5+，最好是 Python3.7+，功能会多一些，少量新功能需要 Python3.9。另外，asyncio 的接口存在不向后兼容的情况，
> 譬如 ["Deprecated since version 3.8, will be removed in version 3.10: The loop parameter."](https://docs.python.org/3/library/asyncio-task.html#asyncio.sleep) 这类

## 协程

### 协程的定义

- 被 `async` 关键字声明的函数就是异步函数，也可以粗略认为是一个协程
- 直接调用协程并不会执行，必须在前面加 `await`，而且需要放到事件循环中执行
- `await` 的语义是：当遇到阻塞时，主动让出执行时间给其他协程

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
    # 测试并发执行效果
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

- coroutine 基于 IO 多路复用，运行在一个线程上，当线程被阻塞，所有的 coroutine 会被阻塞，一般用于 IO 密集型任务，而且需要配合异步库使用。
总的来说是并发，不是并行
- goroutine 基于 Go 运行时 GPM 模型的调度，一般运行在多个线程上，当某一个线程被阻塞时，其他 goroutine 还可以运行在其他的线程上，既可以用于 IO 密集型任务，也可以用于
CPU 密集型任务。总的来说即使并发，也是并行

## await

`await` 可以作用于三类对象：Coroutine、Task、Future 

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

```python
import asyncio
    

async def test_task():
    async def say():
        print("hello task")

    task = asyncio.create_task(say())
    await task


asyncio.run(test_task())
```

## Future

- TODO

## 模块级别函数

```python
import asyncio


async def main():
    # 判断是否异步函数
    print(asyncio.iscoroutinefunction(test_sleep))

    # 输出当前协程
    print(asyncio.current_task())

    # 输出所有协程
    print(asyncio.all_tasks())


if __name__ == "__main__":
    asyncio.run(main())
```
