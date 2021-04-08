# Pycharm 的线程可视化工具

这里记录如何使用专业版 Pycharm 提供的线程可视化工具对 Python 多线程代码进行分析：查看每个线程的运行时间、等待锁的时间、持有锁的运行时间、是否发生死锁等

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

![]()
