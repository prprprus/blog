# 使用 Cython 标准库

Cython 除了最基本的自动类型转换、类型化/静态化变量的功能之外，还提供了一些比较实用的库，包括：C、C++、posix、Numpy、CPython、OpenMP 六部分，它们共同构成了 [Cython 的标准库](https://github.com/cython/cython/tree/master/Cython/Includes)。

回到 [HelloCython.md]() 中的例子：

```Python
# primes_python() 函数
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

```Cython
# hello_cython.pyx
def primes_cython_v1(int nb_primes):
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

优化的核心思想是将 Python 的类型换成 C/C++ 的类型。在上面的例子中，用 C array 替换了 Python list，但是由于 array 是固定长度的，造成优化后的代码不灵活，显得相对不那么自然。其实，Cython 提供的 C++ 标准库中就包含一些非常有用的 STL 容器，用它们可以在提升性能的同时，尽可能的增加开发效率、减少造轮子。

使用标准库 `<vector>` 后的 .pyx 代码如下：

```Cython
from libcpp.vector cimport vector


def primes_cython_v2(int nb_primes):
    cdef vector[int] p
    cdef int n, i

    n = 2

    while p.size() < nb_primes:
        for i in p:
            if n % i == 0:
                break
        else:
            p.push_back(n)
        n += 1

    return p
```

可以看到相比 v1，v2 更加贴近原来的代码形式，可读性好了一点。

经过测试 v2 和 v1 的结果是一致的，测试代码就省略了，可以参考 [HelloCython.md]() 中的方法。功能一致了，那性能方面呢？通过 `cython sdm_pog_pk.pyx -a` 命令可以得到如下分析报告：

![](https://github.com/hsxhr-10/Blog/blob/master/image/%E4%BD%BF%E7%94%A8Cython%E6%A0%87%E5%87%86%E5%BA%931.png)

可以看到，v2 相对 v1 最后一行黄色还更浅了，这是因为 v1 返回的是 Python 类型的变量，v2 返回的是 C++ 类型的变量。

然后用 CProfile 实际跑一下，测试代码和结果如下：

```Python
import hello_cython


def main():
    nb_primes = 19999
    hello_cython.primes_cython_v2(nb_primes)


if __name__ == "__main__":
    main()
```

```bash
$ python -m profile demo.py
         105 function calls in 0.923 seconds
```

从实际执行的结果上看却比 v1 的 0.846 要慢一点，但是差距还是很能接受的，原因目前不清楚，希望知道的大佬赐教下。

TODO...