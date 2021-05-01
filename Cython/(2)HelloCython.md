# Hello Cython

## 案例

一个找素数的程序：

```Python
# test.py

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


def main():
    nb_primes = 19999
    primes_python(nb_primes)


if __name__ == "__main__":
    main()
```

用 cProfile 做一下基准测试，耗时大概 16s

## 用 Cython 优化

这是一个比较典型的 CPU 密集型任务，也是很多人诟病 Python 性能差的一个重要场景，用 Cython 重写一下

### (1) 编写 Cython 代码

扩展以 `.pyx` 结尾

```Cython
# hello_cython.pyx

from libcpp.vector cimport vector

def primes_cython_v2(int nb_primes):
    # 将原来的 Python 类型换成 C/C++ 类型
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

### (2) 编写 setup.py

用于后续的编译

```Python
# setup.py

from distutils.core import setup
from distutils.extension import Extension
from Cython.Distutils import build_ext

ext_modules = [Extension(
    # Cython 扩展模块名
    name="hello_cython",
    # Cython 扩展的源代码文件名
    sources=["hello_cython.pyx"],
    language="c++",
    extra_compile_args=["-Wno-cpp", "-Wno-unused-function", "-O2", "-march=native", '-stdlib=libc++', '-std=c++11'],
    extra_link_args=["-O3", "-march=native", '-stdlib=libc++'],
)]
setup(
    name="hello_cython pyx",
    cmdclass={'build_ext': build_ext},
    ext_modules=ext_modules,
)
```

### (3) 编译

```bash
$ python setup.py build_ext --inplace
```

一切顺利的话，会生成几个文件：

```bash
drwxr-xr-x  3 tiger  staff    96B  5  1 11:05 build
-rw-r--r--  1 tiger  staff   121K  5  1 11:05 hello_cython.cpp
-rwxr-xr-x  1 tiger  staff    29K  5  1 11:05 hello_cython.cpython-36m-darwin.so
```

hello_cython.cpp 是 hello_cython.pyx 被 Cython 编译器翻译后的 C++ 源码，hello_cython.cpython-36m-darwin.so 就是 Cython 扩展，
在 Python 代码中可以 import 使用

### (4) 使用扩展

```Python
# test.py

import hello_cython # 导入 Cython 扩展


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


def main():
    nb_primes = 19999
    # 使用扩展
    hello_cython.primes_cython(nb_primes)


if __name__ == "__main__":
    main()
```

再次用 cProfile 做基准测试，耗时大概 800ms，效果拔群~!

### (5) 验证

验证 Cython 版本和 Python 版本的结果是否一致

```python
# test.py

import hello_cython # 导入 Cython 扩展


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


def main():
    nb_primes = 19999
    # 使用扩展
    print(hello_cython.primes_cython(nb_primes) == primes_python(nb_primes))


if __name__ == "__main__":
    main()
```

输出 True，结果一致，验证 ✅

