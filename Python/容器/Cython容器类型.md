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


# case5: traverse
from libcpp.vector cimport vector
from cython.operator cimport preincrement, dereference


cpdef test_vector(int N):
    cdef vector[int] vec
    cdef int i
    cdef vector[int].iterator it

    for i in range(N):
        vec.push_back(i)

    it = vec.begin()
    while it != vec.end():
        print(dereference(it))
        print(dereference(it))
        preincrement(it)
```

## map

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/cython-6.png)

常用方法记录：

```cython
# case1: insert
from libcpp.map cimport map
from libcpp.pair cimport pair


cpdef test_map(int N):
    cdef map[int, int] _map
    cdef pair[int, int] _pair
    cdef int i

    for i in range(N):
        _pair.first = i
        _pair.second = i
        _map.insert(_pair)


# case2: remove by key
from libcpp.map cimport map
from libcpp.pair cimport pair


cpdef test_map(int N):
    cdef map[int, int] _map
    cdef pair[int, int] _pair
    cdef int i
    cdef map[int, int].iterator it

    for i in range(N):
        _pair.first = i
        _pair.second = i
        _map.insert(_pair)

    for i in range(N):
        it = _map.find(i)
        _map.erase(it)


# case3: pop last element
from libcpp.map cimport map
from libcpp.pair cimport pair
from cython.operator cimport preincrement, dereference


cpdef test_map(int N):
    cdef map[int, int] _map
    cdef pair[int, int] _pair
    cdef int i
    cdef map[int, int].iterator it
    cdef map[int, int].iterator it_tmp

    for i in range(N):
        _pair.first = i
        _pair.second = i
        _map.insert(_pair)

    it = _map.begin()
    for i in range(N):
        while it != _map.end():
            it_tmp = it
            preincrement(it)

    return (dereference(it_tmp).first, dereference(it_tmp).second)


# case4: traverse
from libcpp.map cimport map
from libcpp.pair cimport pair
from cython.operator cimport preincrement, dereference


cpdef test_map(int N):
    cdef map[int, int] _map
    cdef pair[int, int] _pair
    cdef int i
    cdef map[int, int].iterator it

    for i in range(N):
        _pair.first = i
        _pair.second = i
        _map.insert(_pair)

    it = _map.begin()
    while it != _map.end():
        print(dereference(it).first)
        print(dereference(it).second)
        preincrement(it)
```

总体来说，两者相差不大，起码没有 list 和 vector 大，原生 Python 的 dict 性能还是很能打的，不愧是高度优化的数据结构

## set

![]()

常用方法记录：

```cython
# case1: insert
from libcpp.set cimport set
from libcpp.pair cimport pair


def test_set(int N):
    cdef set[int] _set
    cdef pair[int, int] _pair
    cdef set[int].iterator it
    cdef set[int].iterator it_tmp
    cdef int i

    for i in range(N):
        _set.insert(i)


# case2: remove value
from libcpp.set cimport set
from libcpp.pair cimport pair
from cython.operator cimport preincrement, dereference


def test_set(int N):
    cdef set[int] _set
    cdef pair[int, int] _pair
    cdef set[int].iterator it
    cdef set[int].iterator it_tmp
    cdef int i
    cdef int result

    for i in range(N):
        _set.insert(i)

    for i in range(N):
        it = _set.begin()
        result = dereference(it)
        _set.erase(it)

    return result


# case3: traverse
from libcpp.set cimport set
from libcpp.pair cimport pair
from cython.operator cimport preincrement, dereference


def test_set(int N):
    cdef set[int] _set
    cdef pair[int, int] _pair
    cdef set[int].iterator it
    cdef set[int].iterator it_tmp
    cdef int i
    cdef int result

    for i in range(N):
        _set.insert(i)

    it = _set.begin()
    while it != _set.end():
        print(dereference(it))
        preincrement(it)
```

## 参考

- [详细](https://github.com/cython/cython/tree/master/Cython/Includes/libcpp)
- [Using C++ in Cython](https://cython.readthedocs.io/en/latest/src/userguide/wrapping_CPlusPlus.html#using-c-in-cython)
