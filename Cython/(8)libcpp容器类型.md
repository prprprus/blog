# Cython 容器类型

## vector

vector 一般对标 Python 的 list

### 对标 list.append(x)

```cython
from libcpp.vector cimport vector


cpdef test_vector():
    cdef vector[int] vec
    cdef int i

    for i in range(100):
        # vector.push_back()
        vec.push_back(i)
```

### 对标遍历 list

####  方法一

```cython
from libcpp.vector cimport vector


cpdef test_vector():
    cdef vector[int] vec
    cdef int i
    cdef int j
    cdef int size

    for i in range(100):
        vec.push_back(i)

    j = 0
    size = vec.size()
    while j < len:
        print(vec[j])
        j += 1
```

#### 方法二

````cython
from libcpp.vector cimport vector
from cython.operator cimport preincrement, dereference


cpdef test_vector(int N):
    cdef vector[int] vec
    cdef int i
    cdef vector[int].iterator it
    cdef vector[int].iterator it_end
    
    for i in range(N):
        vec.push_back(i)

    it = vec.begin()
    it_end = vec.end()
    while it != it_end:
        print(dereference(it))
        preincrement(it)
````

#### extend(iterator)

```cython
from libcpp.vector cimport vector


cpdef test_vector(int N, int value):
    cdef vector[int] vec
    cdef int i

    for i in range(N):
        vec.insert(vec.end(), sub.begin(), sub.end())
```

#### insert(i,x)

```cython
from libcpp.vector cimport vector


cpdef test_vector(int N):
    cdef vector[int] vec
    cdef int i
    cdef vector[int].iterator it
    cdef int _len

    for i in range(N):
        _len = vec.size() / 2
        it = vec.begin()
        vec.insert(it+_len, i)
```

#### remove(x)

```cython
from libcpp.vector cimport vector
from cython.operator cimport preincrement, dereference


cdef remove(vector[int]& vec, int value):
    cdef vector[int].iterator it
    cdef vector[int].iterator it_end

    it = vec.begin()
    it_end = vec.end()
    while it != it_end:
        if dereference(it) == value:
            vec.erase(it)
            return
        preincrement(it)


cpdef test_vector(int N):
    cdef vector[int] vec
    cdef int i

    for i in range(N):
        vec.push_back(i)

    for i in range(N):
        remove(vec, i)
```

#### pop()

```cython
from libcpp.vector cimport vector


cpdef test_vector(int N):
    cdef vector[int] vec
    cdef int i

    for i in range(N):
        vec.push_back(i)

    for i in range(N):
        vec.pop_back()
```

#### pop(i)

```cython
from libcpp.vector cimport vector


cpdef test_vector(int N):
    cdef vector[int] vec
    cdef int i
    cdef int index
    cdef vector[int].iterator it

    for i in range(N):
        vec.push_back(i)

    for i in range(N):
        it = vec.begin()
        index = int(vec.size()/2)
        vec.erase(it + index)
```

#### clear()

```cython
from libcpp.vector cimport vector


cpdef test_vector(int N):
    cdef vector[int] vec
    cdef int i

    for i in range(N):
        vec.push_back(i)

    for i in range(N):
        vec.clear()
```

#### index(x)

```cython
from libcpp.vector cimport vector


cdef int index(vector[int]& vec, int value):
    cdef int i = 0
    cdef int len = vec.size()

    while i < len:
        if vec[i] == value:
            return i
        i += 1

    return -1


cpdef test_vector(int N):
    cdef vector[int] vec
    cdef int i

    for i in range(N):
        vec.push_back(i)

    for i in range(N):
        index(vec, i)
```

#### count(x)

```cython
from libcpp.vector cimport vector


cdef int count(vector[int]& vec, int value):
    cdef int i = 0
    cdef int len = vec.size()
    cdef int count = 0

    while i < len:
        if vec[i] == value:
            count += 1
        i += 1

    return count


cpdef test_vector(int N):
    cdef vector[int] vec
    cdef int i

    for i in range(N):
        vec.push_back(i)

    for i in range(N):
        count(vec, i)
```

#### sort

```cython
from libcpp.vector cimport vector
from libcpp.algorithm cimport sort


cpdef test_vector(int N):
    cdef vector[int] vec
    cdef int i
    cdef vector[int].iterator it_begin = vec.begin()
    cdef vector[int].iterator it_end = vec.end()

    for i in range(N):
        vec.push_back(i)

    for i in range(N):
        sort(it_begin, it_end)
```

#### reverse

```cython
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
```

## map

对标 Python 的字典操作

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/cython-10.png)

### 操作记录

#### dict[k] = v

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair


cpdef test_map(int N):
    cdef map[int, int] _map
    cdef pair[int, int] _pair

    for i in range(N):
        _pair.first = i
        _pair.second = i
        _map.insert(_pair)
```

#### 遍历

```cython
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

#### del dict[k]

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair


cdef remove(map[int, int]& _map, int key):
    cdef map[int, int].iterator it

    it = _map.find(key)
    _map.erase(it)


cpdef test_map(int N):
    cdef map[int, int] _map
    cdef pair[int, int] _pair
    cdef int i

    for i in range(N):
        _pair.first = i
        _pair.second = i
        _map.insert(_pair)

    for i in range(N):
        remove(_map, i)
```

#### clear()

```cython
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

    for i in range(N):
        _map.clear()
```

#### pop(k)

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair
from cython.operator cimport dereference

cdef pair[int, int] pop_by_key(map[int, int]& _map, int key):
    cdef map[int, int].iterator it
    cdef pair[int, int] result

    it = _map.find(key)
    result.first = dereference(it).first
    result.second = dereference(it).second

    _map.erase(it)

    return result


cpdef test_map(int N):
    cdef map[int, int] _map
    cdef pair[int, int] _pair
    cdef int i
    cdef pair[int, int] result

    for i in range(N):
        _pair.first = i
        _pair.second = i
        _map.insert(_pair)

    for i in range(N):
        result = pop_by_key(_map, i)

    return result
```

#### popitem()

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair
from cython.operator cimport dereference


cdef pair[int, int] popitem(map[int, int]& _map):
    cdef map[int, int].iterator it
    cdef pair[int, int] result

    it = _map.begin()
    result.first = dereference(it).first
    result.second = dereference(it).second

    _map.erase(it)

    return result


cpdef test_map(int N):
    cdef map[int, int] _map
    cdef pair[int, int] _pair
    cdef int i
    cdef pair[int, int] result

    for i in range(N):
        _pair.first = i
        _pair.second = i
        _map.insert(_pair)

    for i in range(N):
        result = popitem(_map)

    return result
```

#### dict[k]

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair
from cython.operator cimport dereference


cdef pair[int, int] get(map[int, int]& _map, int key):
    cdef map[int, int].iterator it
    cdef pair[int, int] result

    it = _map.find(key)
    result.first = dereference(it).first
    result.second = dereference(it).second

    return result


cpdef test_map(int N):
    cdef map[int, int] _map
    cdef pair[int, int] _pair
    cdef int i
    cdef pair[int, int] result

    for i in range(N):
        _pair.first = i
        _pair.second = i
        _map.insert(_pair)

    for i in range(N):
        result = get(_map, i)

    return result
```

#### len(dict)

````cython
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

    for i in range(N):
        _map.size()
````

#### k in dict

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair
from cython.operator cimport dereference


cdef int get(map[int, int]& _map, int key):
    cdef map[int, int].iterator it

    it = _map.find(key)

    if dereference(it).first == 0 and dereference(it).second == 0:
        return -1
    return 1


cpdef test_map(int N):
    cdef map[int, int] _map
    cdef pair[int, int] _pair
    cdef int i
    cdef int result

    for i in range(N):
        _pair.first = i
        _pair.second = i
        _map.insert(_pair)

    for i in range(N):
        result = get(_map, i)

    return result
```

总体来说，两者相差不大，起码没有 list 和 vector 大，原生 Python 的字典性能还是很能打的，不愧是高度优化的数据结构

## set

对标 Python 的集合操作

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/cython-11.png)

### 操作记录

#### add(x)

```cython
from libcpp.set cimport set


def test_set(int N):
    cdef set[int] _set
    cdef int i

    for i in range(N):
        _set.insert(i)
```

#### 遍历

```cython
from libcpp.set cimport set
from cython.operator cimport preincrement, dereference


def test_set(int N):
    cdef set[int] _set
    cdef set[int].iterator it
    cdef int i

    for i in range(N):
        _set.insert(i)

    it = _set.begin()
    while it != _set.end():
        print(dereference(it))
        preincrement(it)
```

#### remove(x)

```cython
from libcpp.set cimport set


cdef remove(set[int]& _set, int value):
    cdef set[int].iterator it

    it = _set.find(value)
    _set.erase(it)


def test_set(int N):
    cdef set[int] _set
    cdef int i

    for i in range(N):
        _set.insert(i)

    for i in range(N):
        remove(_set, i)
```

#### pop()

```cython
from libcpp.set cimport set
from cython.operator cimport dereference


cdef int pop(set[int]& _set):
    cdef set[int].iterator it
    cdef int result

    it = _set.begin()
    result = dereference(it)
    _set.erase(it)

    return  result


def test_set(int N):
    cdef set[int] _set
    cdef int i
    cdef int result

    for i in range(N):
        _set.insert(i)

    for i in range(N):
        result = pop(_set)

    return result
```

#### clear()

```cython
from libcpp.set cimport set


def test_set(int N):
    cdef set[int] _set
    cdef int i

    for i in range(N):
        _set.insert(i)

    for i in range(N):
        _set.clear()
```

#### size()

```cython
from libcpp.set cimport set


def test_set(int N):
    cdef set[int] _set
    cdef int i

    for i in range(N):
        _set.insert(i)

    for i in range(N):
        _set.size()
```

#### x in set

```cython
from libcpp.set cimport set
from cython.operator cimport dereference


cdef int get(set[int]& _set, int value):
    cdef set[int].iterator it

    it = _set.find(value)
    if dereference(it) == 0:
        return -1
    return 1


def test_set(int N):
    cdef set[int] _set
    cdef int i
    cdef int result

    for i in range(N):
        _set.insert(i)

    for i in range(N):
        result = get(_set, i)

    return result
```

## 参考

- [详细](https://github.com/cython/cython/tree/master/Cython/Includes/libcpp)
- [Using C++ in Cython](https://cython.readthedocs.io/en/latest/src/userguide/wrapping_CPlusPlus.html#using-c-in-cython)
