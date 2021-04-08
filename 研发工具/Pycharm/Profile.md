# Pycharm 的 Profile 工具

这里记录如何使用 Pycharm 提供的 Profile 工具对 Python 代码进行性能分析。入门案例代码如下：

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

到 IDE 的空白处右击鼠标 --> More Run/Debug --> Profile XX，如下图所示：

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/Pycharm-Profile.png)

执行完之后，会弹出一个新的 tab 窗口，里面有耗时统计和调用关系图。如下图所示：

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/Pycharm-Profile-%E7%BB%9F%E8%AE%A1.png)

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/Pycharm-Profile-%E8%B0%83%E7%94%A8%E5%85%B3%E7%B3%BB.png)

耗时统计里面主要关注 `Time` 字段，调用关系中耗时越多，颜色一般越深，同时可以关注 `Total` 字段。有了这个，就可以比较直观方便的定位到代码哪里存在性能问题，进而再有针对性的做优化。

下面来看一个更贴合实际的案例，代码如下：

```Python

```