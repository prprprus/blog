# 基本使用

下面的 Python 程序提供了这样的功能：你执行一个数字，程序返回相应数量的素数。

```Python
def primes_python(nb_primes):
    p = []
    n = 2
    while len(p) < nb_primes:
        for i in p:
            if n % i == 0:
                break
        else:
            p.append(n)
        n += 1
    return p
```

测试代码如下：

```Python
# 此处省略 primes_python()
# ...


def main():
    nb_primes = 19999
    primes_python(nb_primes)


if __name__ == "__main__":
    main()
```

nb_primes 我们指定 19999，然后执行代码，并用 CProfile 简单分析可以得出如下结果（不同机器的结果可能有出入）：

```bash
$ python -m profile demo.py
         244833 function calls in 13.773 seconds
         ... ...
```

可以看到大约花了 14s。

这是一个典型的 CPU 密集型任务，是传统 Python 不擅长的领域，也是很多人诟病 Python 性能差的一个重要原因。接下来就用 Cython 来做一个简单的优化，代码如下：

```Cython
def primes_cython_v1(int nb_primes):
    # 将原来的 Python 类型换成 C 类型
    cdef int[19999] p
    cdef int n

    # 防止数组越界
    if nb_primes > 19999:
        nb_primes = 19999
    
    len_p = 0
    n = 2

    while len_p < nb_primes:
        for i in p:
            for i in p[:len_p]:
                if n % i == 0:
                    break
            else:
                p[len_p] = n
                len_p += 1

            n += 1

    return [prime for prime in p[:len_p]]
```

将代码保存到 hello_cython.pyx 文件，下一步就是编译了，这里用的是 distutils 的方式，因此需要写一个 setup.py 文件，如下：

```Python
from distutils.core import setup
from distutils.extension import Extension
from Cython.Distutils import build_ext

ext_modules = [Extension(
    # 扩展模块名字
    name="hello_cython",
    # 扩展的源码文件名字
    sources=["hello_cython.pyx"],
)]
setup(
    name="hello_cython pyx",
    cmdclass={'build_ext': build_ext},
    ext_modules=ext_modules,
)
```

然后执行编译命令，如下：

```bash
$ python setup.py build_ext --inplace
```

一切正常的话，会多了几个文件，如下：

```bash
$ ls
build
hello_cython.c
hello_cython.cpython-36m-darwin.so
```

其中，hello_cython.c 是 hello_cython.pyx 被 Cython 编译器翻译后所对应的 C 源码，hello_cython.cpython-36m-darwin.so 则是所谓的 Cython 扩展，在 Python 中使用的就是它（其实整个编译过程是挺复杂的，目前先关注使用方面）。

扩展有了，下面肯定就是使用啦，测试代码如下：

```Python
# import Cython 扩展
import hello_cython


# 此处省略 primes_python()
# ...


def main():
    nb_primes = 23
    # 使用扩展
    print(hello_cython.primes_cython_v1(nb_primes))


if __name__ == "__main__":
    main()
```

和使用一般的库没什么区别，执行测试代码，输出如下：

```bash
[2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83]
```

可以看到，确实输出了 23 个素数。那不会存在用了 Cython 导致跟原先的结果不一致的问题呢？可以通过指定几个值较小的 nb_primes（因为较大的 Python 代码要算很久...）来验证 primes_cython_v1() 和 primes_python() 的结果是否一致。比如指定 23、52、16，测试代码如下：

```Python
import hello_cython


# 此处省略 primes_python()
# ...


def main():
    print(primes_python(23) == hello_cython.primes_cython_v1(23))
    print(primes_python(52) == hello_cython.primes_cython_v1(52))
    print(primes_python(16) == hello_cython.primes_cython_v1(16))


if __name__ == "__main__":
    main()
```

执行测试代码，发现结果是一致的，如下：

```bash
$ python demo.py
True
True
True
```

回到一开始 nb_primes 被我们指定 19999 时，Python 代码耗时 14s 的问题上，来看一下用了 Cython 提升了多少，测试代码如下：

```Python
import hello_cython


# 此处省略 primes_python()
# ...


def main():
    nb_primes = 19999

    # primes_python(nb_primes)
    hello_cython.primes_cython_v1(nb_primes)


if __name__ == "__main__":
    main()
```

执行测试代码，结果如下：

```
$ python -m profile demo.py
         105 function calls in 0.846 seconds
         ... ...
```

震惊！1s 都不到，吓得我赶紧拿出计算器算了一下，快了将近 17 倍。抱着严谨的技术精神（狗头），再验证一下 nb_primes 为 19999 时，两个版本的结果是否一致，测试代码如下：

```Python
import hello_cython


# 此处省略 primes_python()
# ...


def main():
    nb_primes = 19999
    print(primes_python(nb_primes) == hello_cython.primes_cython_v1(nb_primes))


if __name__ == "__main__":
    main()
```

结果如下：

```bash
$ python demo.py
True
```

嗯，这下安心了。

其实能快这么多也可以理解，毕竟经过 Cython 编译后得到的就是 C 代码了。