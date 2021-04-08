# Pycharm 的 Profile 工具

这里记录如何使用 Pycharm 提供的 Profile 工具对 Python 代码进行性能分析。案例代码如下：

```Python
import time


def fun1(a, b):
    print('fun1')
    print(a, b)
    time.sleep(3)


def fun2():
    print('fun2')
    time.sleep(1)


def fun3():
    print('fun3')
    time.sleep(2)


def fun4():
    print('fun4')
    time.sleep(1.5)


def fun5():
    print('fun5')
    time.sleep(3.7)
    fun4()


if __name__ == "__main__":
    fun1('foo', 'bar')
    fun2()
    fun3()
    fun5()
```

