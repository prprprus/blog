# asyncio 学习笔记

Python 有不少异步 IO 框架，但是根据标准的 IO 模型，很多时候说的异步 IO 并不是真正的异步，而是 IO 多路复用。asyncio 也不例外，
对于 IO 部分，asyncio 是基于 [selector](https://github.com/python/cpython/blob/3.9/Lib/asyncio/selector_events.py) 模块，
而 selector 基于 select，select 基于操作系统提供的 IO 多路复用机制，譬如 Linux 的 epoll，macOS 的 kqueue 等

但是 asyncio 也不是只能用于 IO 操作，它所提供的事件循环、Future、线程池、进程池等工具，也可以对一般的代码进行异步化，实现并发的效果

asyncio 的特点和主流的异步框架（tornado）差不多：

- 擅长处理 IO 密集型任务，相比线程昂贵（创建、销毁、上下文切换），asyncio 里的协程要轻量级很多，
可以同时存在大量的协程，因此可以较好地提升应用的吞吐量，但是应用的响应时间还是要看单个协程的处理时长，跟异步与否无关
- 事件循环不能被阻塞，也就是不能存在阻塞代码，无论是自己写的、标准库的、还是第三方库的代码。这就需要搭配线程池、进程池等工具，或者要求生态要好，不然连个异步的数据库驱动都没有，也挺麻烦 😂
- 不擅长 CPU 密集型任务，可以结合进程池、Celery 等工具缓解这个问题

> 在多线程的环境下，Python 虚拟机大概每执行 200 个字节码，就进行一次线程切换的系统调用

> asyncio 需要 Python3.5+，最好是 Python3.7+，功能会多一些，少量新功能需要 Python3.9。另外，asyncio 的接口存在不向后兼容的情况，
> 譬如 ["Deprecated since version 3.8, will be removed in version 3.10: The loop parameter."](https://docs.python.org/3/library/asyncio-task.html#asyncio.sleep) 这类

## 事件循环

### IO 多路复用

asyncio 的事件循环包含着 IO 多路复用，专门用来处理 IO 事件，多路复用本身也有一个事件循环。一般基于多路复用的代码长这样：

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

### 事件循环启动流程

1. 写个 demo 打上断点，在调试模式下可以看到调用栈

```python
import asyncio


async def sleep(second):
    await asyncio.sleep(second)
    print("hello asyncio")
    

async def main():
    await sleep(1)


if __name__ == "__main__":    
    asyncio.run(main())
```

```BASH
main, demo1.py:76 # 自己的代码
_run, events.py:88
_run_once, base_events.py:1786
run_forever, base_events.py:541
run_until_complete, base_events.py:574
run, runners.py:43
<module>, demo1.py:80 # 自己的代码
```

2. runners.py 的 run() 方法

```python
def run(main, *, debug=False):
    # 确保 asyncio.run() 方法不能运行在一个已存在的事件循环
    if events._get_running_loop() is not None:
        raise RuntimeError(
            "asyncio.run() cannot be called from a running event loop")
    
    # 判断传入的 main 对象是否协程类型
    if not coroutines.iscoroutine(main):
        raise ValueError("a coroutine was expected, got {!r}".format(main))
    
    # 新创建一个事件循环
    loop = events.new_event_loop()
    try:
        # 相关设置
        events.set_event_loop(loop)
        loop.set_debug(debug)
        # 执行 loop.run_until_complete(main) 方法
        return loop.run_until_complete(main)
    finally:
        # 相关收尾操作
        try:
            _cancel_all_tasks(loop)
            loop.run_until_complete(loop.shutdown_asyncgens())
        finally:
            events.set_event_loop(None)
            loop.close()
```

3. base_events.py 的 run_until_complete() 方法

```python
def run_until_complete(self, future):
    # 相关检查操作
    self._check_closed()
    self._check_runnung()
    
    # 这一块逻辑主要是将协程封装成 Task
    new_task = not futures.isfuture(future)
    future = tasks.ensure_future(future, loop=self)
    if new_task:
        # An exception is raised if the future didn't complete, so there
        # is no need to log the "destroy pending task" message
        future._log_destroy_pending = False

    future.add_done_callback(_run_until_complete_cb)
    try:
        # 启动事件循环
        self.run_forever()
    except:
        # 忽略异常处理代码
        ...
    finally:
        future.remove_done_callback(_run_until_complete_cb)
    # 相关错误处理
    if not future.done():
        raise RuntimeError('Event loop stopped before Future completed.')
    
    # 返回 Future 的结果
    return future.result()
```

4. base_events.py 的 run_forever() 方法

```python
def run_forever(self):
    # 相关检查
    self._check_closed()
    self._check_runnung()
    self._set_coroutine_origin_tracking(self._debug)
    self._thread_id = threading.get_ident()

    old_agen_hooks = sys.get_asyncgen_hooks()
    sys.set_asyncgen_hooks(firstiter=self._asyncgen_firstiter_hook,
                           finalizer=self._asyncgen_finalizer_hook)
    try:
        events._set_running_loop(self)
        # 事件循环本体
        while True:
            # 每次循环执行 _run_once() 方法
            self._run_once()
            # 外部调用 stop() 方法时会停止事件循环
            if self._stopping:
                break
    finally:
        # 相关收尾操作
        self._stopping = False
        self._thread_id = None
        events._set_running_loop(None)
        self._set_coroutine_origin_tracking(False)
        sys.set_asyncgen_hooks(*old_agen_hooks)
```

5. base_events.py 的 _run_once() 方法

```python
def _run_once(self):
    """Run one full iteration of the event loop.

    This calls all currently ready callbacks, polls for I/O,
    schedules the resulting callbacks, and finally schedules
    'call_later' callbacks.
    
    事件循环迭代一次就运行一次这个方法

    这个方法会执行所有就绪的回调函数，包括 IO 多路复用的回调、一般 future 的回调、
    call_later 的回调（譬如含有 asyncio.sleep() 这种语句的函数），回调函数都会被封装成 Handle 对象
    """
    
    # 1. self._scheduled 对应的数据结构是最小二叉堆，用来存放所有的 call_later 回调，根据 time 排序
    # 2. 这一块逻辑主要是将已经取消的 call_later 回调从二叉堆中删除 
    sched_count = len(self._scheduled)
    if (sched_count > _MIN_SCHEDULED_TIMER_HANDLES and
        self._timer_cancelled_count / sched_count >
            _MIN_CANCELLED_TIMER_HANDLES_FRACTION):
        # Remove delayed calls that were cancelled if their number
        # is too high
        new_scheduled = []
        for handle in self._scheduled:
            if handle._cancelled:
                handle._scheduled = False
            else:
                new_scheduled.append(handle)

        heapq.heapify(new_scheduled)
        self._scheduled = new_scheduled
        self._timer_cancelled_count = 0
    else:
        # Remove delayed calls that were cancelled from head of queue.
        while self._scheduled and self._scheduled[0]._cancelled:
            self._timer_cancelled_count -= 1
            handle = heapq.heappop(self._scheduled)
            handle._scheduled = False
    
    # 确定 IO 多路复用的等待时间
    timeout = None
    if self._ready or self._stopping:
        timeout = 0
    elif self._scheduled:
        # Compute the desired timeout.
        when = self._scheduled[0]._when
        timeout = min(max(0, when - self.time()), MAXIMUM_SELECT_TIMEOUT)
    
    # 这一块逻辑主要是基于 IO 多路复用处理 IO 事件
    # 调试模式下
    if self._debug and timeout != 0:
        t0 = self.time()
        # 基于 selector 模块的 IO 多路复用
        event_list = self._selector.select(timeout)
        # 忽略大段大段的调试代码
        ...
    # 非调试模式下
    else:
        # 基于 selector 模块的 IO 多路复用
        event_list = self._selector.select(timeout)
    self._process_events(event_list)

    # Handle 'later' callbacks that are ready.
    # 1. self._ready 对应的数据结构是 collections.deque，是整个事件循环的核心数据结构，
    # 任何就绪的回调函数都会放到这个队列，然后在每次事件循环中遍历队列，依次执行就绪的回调函数
    # 2. 这一块逻辑主要是将就绪的 call_later 回调放到 self._ready
    end_time = self.time() + self._clock_resolution
    while self._scheduled:
        handle = self._scheduled[0]
        if handle._when >= end_time:
            break
        handle = heapq.heappop(self._scheduled)
        handle._scheduled = False
        self._ready.append(handle)

    # This is the only place where callbacks are actually *called*.
    # All other places just add them to ready.
    # Note: We run all currently scheduled callbacks, but not any
    # callbacks scheduled by callbacks run this time around --
    # they will be run the next time (after another I/O poll).
    # Use an idiom that is thread-safe without using locks.
    # 
    # 这一块逻辑主要是遍历就绪队列 self._ready，
    # 逐个出队并执行 Handle 对象的 _run() 方法，_run() 方法内部执行的是就是回调函数
    ntodo = len(self._ready)
    # 遍历就绪队列
    for i in range(ntodo):
        # 出队
        handle = self._ready.popleft()
        if handle._cancelled:
            continue
        # 调试模式下
        if self._debug:
            try:
                self._current_handle = handle
                t0 = self.time()
                # 执行回调函数
                handle._run()
                # 忽略大段大段的调试代码
                ...
            finally:
                self._current_handle = None
        # 非调试模式下
        else:
            # 执行回调函数
            handle._run()
    handle = None  # Needed to break cycles when an exception occurs.
```

事件循环的启动流程和每次循环的处理逻辑大致就这样了

### 事件循环的调度流程

调度流程这块其实比并不好找，单步调试貌似最多走到 events.py:88，这一步还是比较明显，执行就绪的回调函数，但是协程间的切换体现不出来

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/pythonio-3.png)

后续到网上找了下，发现切换相关的代码在 tasks.py 的 `__step()` 和 `__wakeup()` 方法上，特别是 `__step()` 

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/pythonio-4.png)

## 可调度对象

协程在底层会被封装成 Task，而 Task 是 Future 的子类，也就是说这三种可调度对象基本都可以看成 Future 对象

### (1) 协程

#### 协程的定义

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

#### Python 协程和 Go 协程的区别

- coroutine 基于 asyncio 事件循环的调度，运行在一个线程上，当线程被阻塞，所有的 coroutine 会被阻塞，一般用于 IO 密集型任务，而且需要配合异步库使用。
总的来说是并发，不是并行
- goroutine 基于 Go 运行时 GPM 模型的调度，一般运行在多个线程上，当某一个线程被阻塞时，其他 goroutine 还可以运行在其他的线程上，既可以用于 IO 密集型任务，也可以用于
CPU 密集型任务。总的来说即使并发，也是并行

### (2) Task

Task 继承于 Future 类。官网不建议直接创建 Task 对象，而是通过 `asyncio.create_task(aws)` API 创建。Task 对外提供了比较多的操作

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

### (3) Future

Future 提供的操作和 Task 差不多，[参考](https://docs.python.org/3/library/asyncio-future.html#future-object)

## await 语句

`await` 可以作用于三类对象：协程、Task、Future

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

## 模块级别函数

- 异步 sleep：`asyncio.sleep(second)`
- 并发执行一组协程：`asyncio.gather([aws])`
- 将协程丢到事件循环执行：`asyncio.run(aws)`
- 创建 Task：`asyncio.create_task(aws)`
- 判断是否异步函数：`asyncio.iscoroutinefunction(func)`
- 输出当前协程：`asyncio.current_task()`
- 输出所有协程：`asyncio.all_tasks()`
