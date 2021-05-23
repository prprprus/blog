# libcpp 容器类型

## vector

在 Cython 代码中可以用 vector 代替 Python list。vector 支持 `[]` 操作符

### 对标 list.append(x)

```cython
from libcpp.vector cimport vector


cpdef test_vector():
    cdef vector[int] vec
    cdef int i

    for i in range(100):
        vec.push_back(i)
```

### 对标遍历 list

####  方法一

```cython
from libcpp.vector cimport vector


cpdef test_vector():
    cdef vector[int] vec
    cdef int i
    cdef int size

    for i in range(100):
        vec.push_back(i)

    i = 0
    size = vec.size()
    while i < size:
        print(vec[i])
        i += 1
```

#### 方法二

````cython
from libcpp.vector cimport vector
from cython.operator cimport preincrement, dereference


cpdef test_vector():
    cdef vector[int] vec
    cdef int i
    cdef vector[int].iterator it
    cdef vector[int].iterator it_end

    for i in range(100):
        vec.push_back(i)

    it = vec.begin()
    it_end = vec.end()
    while it != it_end:
        print(dereference(it))
        preincrement(it)
````

### 对标 list.extend(iterator)

```cython
from libcpp.vector cimport vector


cpdef test_vector():
    cdef vector[int] vec
    cdef vector[int] sub_vec
    cdef int i

    for i in range(100):
        vec.push_back(i)

    for i in range(100):
        sub_vec.push_back(i)

    vec.insert(vec.end(), sub_vec.begin(), sub_vec.end())
```

### 对标 list.insert(i, x)

```cython
from libcpp.vector cimport vector


cpdef test_vector():
    cdef vector[int] vec
    cdef int i
    cdef vector[int].iterator it
    cdef int size
    cdef int value

    for i in range(10):
        vec.push_back(i)

    size = vec.size() / 2
    it = vec.begin()
    value = 999
    vec.insert(it + size, value)
```

### 对标 list.remove(x)

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


cpdef test_vector():
    cdef vector[int] vec
    cdef int i
    cdef int value

    for i in range(10):
        vec.push_back(i)

    value = 3
    remove(vec, value)
```

### 对标 list.pop()

```cython
from libcpp.vector cimport vector


cpdef test_vector():
    cdef vector[int] vec
    cdef int i

    for i in range(10):
        vec.push_back(i)

    vec.pop_back()
```

### 对标 list.pop(i)

```cython
from libcpp.vector cimport vector


cpdef test_vector():
    cdef vector[int] vec
    cdef int i
    cdef int index
    cdef vector[int].iterator it

    for i in range(10):
        vec.push_back(i)

    it = vec.begin()
    vec.erase(it + 3)
```

### 对标 list.clear()

```cython
from libcpp.vector cimport vector


cpdef test_vector():
    cdef vector[int] vec
    cdef int i

    for i in range(10):
        vec.push_back(i)

    vec.clear()
```

### 对标 list.index(x)

```cython
from libcpp.vector cimport vector


cdef int index(vector[int]& vec, int value):
    cdef int i = 0
    cdef int size = vec.size()

    while i < size:
        if vec[i] == value:
            return i
        i += 1

    return -1


cpdef test_vector():
    cdef vector[int] vec
    cdef int i
    cdef int value

    for i in range(10):
        vec.push_back(i)

    value = 8
    index(vec, value)
```

### 对标 list.count(x)

```cython
from libcpp.vector cimport vector


cdef int count(vector[int]& vec, int value):
    cdef int i = 0
    cdef int size = vec.size()
    cdef int count = 0

    while i < size:
        if vec[i] == value:
            count += 1
        i += 1

    return count


cpdef test_vector():
    cdef vector[int] vec
    cdef int i
    cdef int value

    for i in range(10):
        vec.push_back(i)

    value = 7
    count(vec, value)
```

### 对标 list.sort()

```cython
from libcpp.vector cimport vector
from libcpp.algorithm cimport make_heap, sort_heap
from libcpp cimport bool


cdef inline bool greater(const int &x, const int &y):
    return x > y


cpdef test_vector():
    cdef vector[int] vec
    cdef int i

    for i in range(10):
        vec.push_back(i)

    make_heap(vec.begin(), vec.end(), &greater)
    sort_heap(vec.begin(), vec.end(), &greater)
```

### 对标 list.reverse()

```cython
from libcpp.vector cimport vector


cpdef test_vector():
    cdef vector[int] vec
    cdef int i
    cdef vector[int] reverse_vec
    cdef int j

    for i in range(10):
        vec.push_back(i)

    j = vec.size() - 1
    while j >= 0:
        reverse_vec.push_back(vec[j])
        j -= 1
```

## map

在 Cython 代码中可以用 map 代替 Python dict。map 支持 `[]` 操作符

### 对标 dict[k] = v

#### 方法一

````cython
from libcpp.map cimport map


cpdef test_map():
    cdef map[int, int] m

    for i in range(10):
        m[i] = i
````

#### 方法二

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair


cpdef test_map():
    cdef map[int, int] m
    cdef pair[int, int] _pair

    for i in range(10):
        _pair.first = i
        _pair.second = i
        m.insert(_pair)
```

### 对标遍历 dict

#### 方法一

````cython
from libcpp.map cimport map
from libcpp.pair cimport pair


cpdef test_map():
    cdef map[int, int] m
    cdef pair[int, int] _pair

    for i in range(10):
        m[i] = i

    for _pair in m:
        print(_pair.first, _pair.second)
````

#### 方法二

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair
from cython.operator cimport preincrement, dereference


cpdef test_map():
    cdef map[int, int] m
    cdef pair[int, int] _pair
    cdef int i
    cdef map[int, int].iterator it

    for i in range(10):
        _pair.first = i
        _pair.second = i
        m.insert(_pair)

    it = m.begin()
    while it != m.end():
        print(dereference(it).first, dereference(it).second)
        preincrement(it)
```

### 对标 del dict[k]

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair


cdef remove(map[int, int]& m, int key):
    cdef map[int, int].iterator it
    cdef pair[int, int] _pair

    for _pair in m:
        if key == _pair.first:
            it = m.find(key)
            m.erase(it)


cpdef test_map():
    cdef map[int, int] m
    cdef int i
    cdef int key

    for i in range(10):
        m[i] = i

    key = 3
    remove(m, key)

    key = 700
    remove(m, key)
```

### 对标 dict.clear()

```cython
from libcpp.map cimport map


cpdef test_map():
    cdef map[int, int] m
    cdef int i

    for i in range(10):
        m[i] = i
    
    m.clear()
```

### 对标 dict.pop(k)

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair
from cython.operator cimport dereference


cdef pair[int, int] pop_by_key(map[int, int]& m, int key):
    cdef map[int, int].iterator it
    cdef pair[int, int] result
    cdef pair[int, int] _pair

    for _pair in m:
        if key == _pair.first:
            it = m.find(key)
            result.first = dereference(it).first
            result.second = dereference(it).second
            m.erase(it)
            return result

    result.first = -1
    result.second = -1
    return result


cpdef test_map():
    cdef map[int, int] m
    cdef int i
    cdef pair[int, int] result
    cdef int key

    for i in range(10):
        m[i] = i

    key = 6
    result = pop_by_key(m, key)

    key = 600
    result = pop_by_key(m, key)

    key = 0
    result = pop_by_key(m, key)

    key = -8989
    result = pop_by_key(m, key)

    return result
```

### 对标 dict.popitem()

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair
from cython.operator cimport dereference


cdef pair[int, int] popitem(map[int, int]& m):
    cdef map[int, int].iterator it
    cdef pair[int, int] result

    it = m.begin()
    result.first = dereference(it).first
    result.second = dereference(it).second

    m.erase(it)

    return result


cpdef test_map():
    cdef map[int, int] m
    cdef int i
    cdef pair[int, int] result

    for i in range(10):
        m[i] = i

    result = popitem(m)

    return result
```

### 对标 dict[k]

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair
from cython.operator cimport dereference


cdef pair[int, int] get(map[int, int]& m, int key):
    cdef map[int, int].iterator it
    cdef pair[int, int] result
    cdef pair[int, int] _pair

    for _pair in m:
        if key == _pair.first:
            it = m.find(key)
            result.first = dereference(it).first
            result.second = dereference(it).second
            return result

    result.first = -1
    result.second = -1
    return result


cpdef test_map():
    cdef map[int, int] m
    cdef int i
    cdef pair[int, int] result
    cdef int key

    for i in range(10):
        m[i] = i


    key = 5
    result = get(m, key)

    key = 500
    result = get(m, key)

    return result
```

### 对标 len(dict)

````cython
from libcpp.map cimport map


cpdef test_map():
    cdef map[int, int] m
    cdef int i

    for i in range(10):
        m[i] = i

    m.size()

````

### 对标 k in dict

```cython
from libcpp.map cimport map
from libcpp.pair cimport pair


cdef int _in(map[int, int]& m, int key):
    cdef map[int, int].iterator it
    cdef pair[int, int] _pair

    for _pair in m:
        if key == _pair.first:
            return 1

    return -1


cpdef test_map():
    cdef map[int, int] m
    cdef int i
    cdef int result
    cdef int key

    for i in range(10):
        m[i] = i

    key = 7
    result = _in(m, key)

    key = 71
    result = _in(m, key)

    return result
```

## set

在 Cython 代码中可以用 set 代替 Python set

### 对标 set.add(x)

```cython
from libcpp.set cimport set


def test_set():
    cdef set[int] s
    cdef int i

    for i in range(10):
        s.insert(i)
```

### 对标遍历 set

#### 方法一

```cython
from libcpp.set cimport set


def test_set():
    cdef set[int] s
    cdef int i
    cdef int item

    for i in range(10):
        s.insert(i)

    for item in s:
        print(item)

```

#### 方法二
```cython
from libcpp.set cimport set
from cython.operator cimport preincrement, dereference


def test_set():
    cdef set[int] s
    cdef set[int].iterator it
    cdef int i

    for i in range(10):
        s.insert(i)

    it = s.begin()
    while it != s.end():
        print(dereference(it))
        preincrement(it)
```

### 对标 set.remove(x)

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
