# Pycharm 的 Profile 工具

这里记录如何使用专业版 Pycharm 提供的 Profile 工具对 Python 代码进行性能分析。入门案例代码如下：

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

执行完之后，会弹出一个新的 tab 窗口，里面会有两个图标，分别是统计图和调用关系图。如下图所示：

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/Pycharm-Profile-%E7%BB%9F%E8%AE%A1.png)

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/Pycharm-Profile-%E8%B0%83%E7%94%A8%E5%85%B3%E7%B3%BB.png)

Profile 报告：

0. 一般优先看我们自己写的函数或者方法的耗时，其次是第三方库的函数、方法的耗时
1. 统计图里面点击每一栏可以按照从小到大或者从大到小进行排序，主要关注 `Time` 字段
2. 调用关系图以一棵倒树的形式呈现，其中耗时越多的操作，颜色一般越鲜艳，主要关注颜色和 `Total` 字段

有了这些报告，就可以比较直观方便的定位到代码哪里存在性能问题，进而再有针对性的做优化

下面来看一个更贴合实际的案例，代码如下：

```Python
import redis
import time


def set_data(pool, number):
    for i in range(number):
        pool.set(str(i), str(i))
    print("---> set_data() done.")


def get_data(pool, number):
    res = []
    for i in range(number):
        res.append(pool.get(str(i)))
    print("---> get_data() 结果:", res)


def clear_data(pool, number):
    for i in range(number):
        pool.delete(str(i))
    print("---> clear_data() done.")


def main():
    """
    pool = redis.ConnectionPool(host='localhost', port=6379, db=0)
    r = redis.Redis(connection_pool=pool, socket_keepalive=True,max_connections=100)
    :return:
    """
    pool = redis.Redis(host="XXX", port=6379, db=0, password="XXX",
                       max_connections=100, socket_keepalive=True, decode_responses=True, health_check_interval=0)
    number = 100000

    set_data(pool, number)
    get_data(pool, number)
    # clear_data(pool, number)


if __name__ == "__main__":
    s = time.time()
    main()
    print("---> 耗时:", time.time() - s)
```

执行 Profile，终端显示耗时输出为：

```bash
---> 耗时: 63.8957998752594
```

统计图和调用关系图如下：

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/Pycharm-Profile-%E7%BB%9F%E8%AE%A11.png)

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/Pycharm-Profile-%E8%B0%83%E7%94%A8%E5%85%B3%E7%B3%BB1.png)

可以看到，Profile 结果中 `main()` 函数耗时和终端输出的结果基本一致。

下面重点看下调用关系图，`get_data()` 和 `set_data()` 这两个我们自己写的函数分别耗时为 29702ms 和 34188ms，读比写稍快一些。颜色比较鲜艳的基本上都是 I/O 操作，
网络 I/O 和文件 I/O 都有

如果性能分析后发现性能瓶颈不是自己写的代码，而是别人的代码，比如第三方库，这个时候一般可以考虑这样解决：

1. 引入并发、并行、异步等
2. 定位到对应的源码，看下在使用上是否还有需要注意的地方。更有甚者，有能力的还可以参考源码实现，造个针对局部的更好的轮子。比如上面的 `_read_from_socket()` 耗时比较多，
   可以找到对应的源码，看下是怎么实现的