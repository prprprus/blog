# Hello Cython

## 使用 Cython 的步骤

1. 分析代码，定位到需要优化的代码，并用 Cython 进行重写
2. 借助标准库中的 distutils（可以将 C/C++ 代码编译成扩展模块），编写 `setup.py` 用于后续编译
3. 执行 `python setup.py build_ext --inplace` 编译扩展模块
4. `import` 使用扩展模块
5. 分析优化效果，不断调整（本次略）

## 案例

一个找素数的程序，比较典型的 CPU 密集型任务，用 cProfile 做基准测试，耗时大概 16s

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

### 用 Cython 优化

#### 1. 分析代码，定位到需要优化的代码，并用 Cython 进行重写

需要用 Cython 重写 `primes_python()` 函数。Cython 源码文件以 `.pyx` 结尾

```Cython
# hello_cython.pyx

# 使用 C++ 标准库
from libcpp.vector cimport vector

def primes_cython(int nb_primes):
    # 添加类型
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

#### 2. 编写 `setup.py` 用于后续编译

```Python
# setup.py

from distutils.core import setup
from distutils.extension import Extension
from Cython.Distutils import build_ext

ext_modules = [Extension(
    # Cython 扩展模块名称
    name="hello_cython",
    # Cython 源代码文件名称
    sources=["hello_cython.pyx"],
    # 生命需要翻译成 C++
    language="c++",
    # 编译参数
    extra_compile_args=["-Wno-cpp", "-Wno-unused-function", "-O2", "-march=native", '-stdlib=libc++', '-std=c++11'],
    extra_link_args=["-O3", "-march=native", '-stdlib=libc++'],
)]
setup(
    name="hello_cython pyx",
    cmdclass={'build_ext': build_ext},
    ext_modules=ext_modules,
)
```

#### 3. 执行 `python setup.py build_ext --inplace` 编译扩展模块

```bash
$ python setup.py build_ext --inplace
```

顺利的话，会生成几个文件。`hello_cython.cpp` 是 `hello_cython.pyx` 被 Cython 编译器翻译过来的 C++ 源码；`hello_cython.cpython-36m-darwin.so` 就是 Cython 扩展模块

```bash
.
├── build
├── hello_cython.cpp
├── hello_cython.cpython-37m-darwin.so
```

#### 4. `import` 使用扩展模块

```Python
# test.py

# 导入扩展模块
import hello_cython


# def primes_python(nb_primes):
#     p = []
#     n = 2
#     while len(p) < nb_primes:
#         for i in p:
#             if n % i == 0:
#                 break
#         else:
#             p.append(n)
#         n += 1
#     return p


def main():
    nb_primes = 19999
    # primes_python(nb_primes)

    # 使用扩展
    hello_cython.primes_cython(nb_primes)


if __name__ == "__main__":
    main()
```

再次用 cProfile 做基准测试，耗时大概 800ms，效果拔群~!

#### 验证结果是否一致

验证 Cython 版本和 Python 版本的结果是否一致

```python
# test.py

import hello_cython


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
    print(hello_cython.primes_cython(nb_primes) == primes_python(nb_primes))


if __name__ == "__main__":
    main()
```

输出 True，结果一致，验证 ✅
