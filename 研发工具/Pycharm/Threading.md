# Pycharm 的线程可视化工具

这里记录如何使用专业版 Pycharm 提供的线程可视化工具对 Python 多线程代码进行分析：

- 每个线程的运行时间
- 每个线程等待锁的时间
- 每个线程持有锁的运行时间
- 是否发生死锁

先来一段没有加锁的多线程代码，如下：

```Python
import threading
import time
import random


def task(i):
    print(i)
    time.sleep(random.randrange(0, 3))


def main():
    workers = []
    number = 10

    for i in range(number):
        w = threading.Thread(target=task, kwargs={"i": i})
        workers.append(w)

    for w in workers:
        w.start()
    for w in workers:
        w.join()


if __name__ == "__main__":
    s = time.time()
    main()
    print("---> 耗时:", time.time() - s)
```

到 IDE 编辑区的空白处右击鼠标 --> More Run/Debug --> Concurrency...，如下图所示：

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/Pycharm%20%E7%9A%84%E7%BA%BF%E7%A8%8B%E5%8F%AF%E8%A7%86%E5%8C%96%E5%B7%A5%E5%85%B7.png)

由于没有加锁，因此在运行结束后，报告中只有每个线程的运行情况显示出来，也就是绿色部分，可以清楚看到这次运行中，每个线程所占用的运行时间是多少。如下图所示：

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/Pycharm%20%E7%9A%84%E7%BA%BF%E7%A8%8B%E5%8F%AF%E8%A7%86%E5%8C%96%E5%B7%A5%E5%85%B71.png)

然后来一段加锁的多线程代码，如下：

```Python
import threading
import time


# 互斥资源
result = 10


def task(i, lock):
    with lock:
        print(i)
        global result
        result -= 1


def main():
    workers = []
    number = 10
    lock = threading.Lock()

    for i in range(number):
        w = threading.Thread(target=task, kwargs={"i": i, "lock": lock})
        workers.append(w)

    for w in workers:
        w.start()
    for w in workers:
        w.join()

    print("---> result:", result)


if __name__ == "__main__":
    s = time.time()
    main()
    print("---> 耗时:", time.time() - s)
```

