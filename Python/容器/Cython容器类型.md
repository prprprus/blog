# Cython 容器类型

对比 Python [容器类型](https://github.com/hsxhr-10/Blog/blob/master/Python/%E5%AE%B9%E5%99%A8/%E5%AE%B9%E5%99%A8%E7%B1%BB%E5%9E%8B.md#%E5%AE%B9%E5%99%A8%E7%B1%BB%E5%9E%8B) 的基准测试,
由于 Cython 快很多，所以直接测 N=400000 的级别

测试脚本大致长这样：

```cython
from libcpp.vector cimport vector


cpdef test_vector(int N):
    cdef vector[int] vec

    for i in range(N):
        vec.push_back(i)
```

```python
import hello_vector


if __name__ == "__main__":
    N = 400000
    hello_vector.test_vector(N)
```

[完整的测试报告]()，仅供参考

## vector

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/cython-3.png)

常用方法记录：

```cython
# case1: insert
from libcpp.vector cimport vector


cpdef test_vector(int N):
    cdef vector[int] vec
    cdef int _len
    cdef vector[int].iterator it

    for i in range(N):
        _len = vec.size() / 2
        it = vec.begin()
        vec.insert(it+_len, i)

        
# case2: extend
from libcpp.vector cimport vector


cpdef test_vector(int N, vector[int] sub):
    cdef vector[int] vec

    for i in range(N):
        vec.insert(vec.end(), sub.begin(), sub.end())


# case3: reverse
from libcpp.vector cimport vector


cpdef test_vector(int N):
    cdef vector[int] vec
    cdef int i
    cdef vector[int] reverse_vec
    cdef int j

    for i in range(N):
        vec.push_back(i)

    j = vec.size()
    while j >= 0:
        reverse_vec.push_back(vec[j])
        j -= 1


# case4: sort
from libcpp.algorithm cimport sort

sort(vec.begin(), vec.end())
```

## map

![]()

常用方法记录：

```cython

```

## set

![]()

常用方法记录：

```cython

```

## 参考

- [详细](https://github.com/cython/cython/tree/master/Cython/Includes/libcpp)
- [Using C++ in Cython](https://cython.readthedocs.io/en/latest/src/userguide/wrapping_CPlusPlus.html#using-c-in-cython)
