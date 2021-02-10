# *args 和 **kwargs

总的来说，`*args` 和 `**kwargs` 的作用是展开可迭代对象，前者用于单值集合（比如：list、tuple、set），后者用于键值对集合（比如：dict）。

## 规则

1. `*` 号必须在有 `,` 号间隔的符号表中才合法
2. 赋值表达式的左边只能有一个 `*` 号
3. 赋值表达式左边的 `*` 号会被解析成 list
4. 函数列表中的 `*` 号必须遵守这个顺序：some_func(fargs, *args, **kwargs)
5. *args 会被转换成 tuple，**kwargs 会被转换成 dict

规则一例子：

```python
args = [1, 2, 3]
*a = args   # SyntaxError: starred assignment target must be in a list or tuple

*a, = args
print(a)    # [1, 2, 3]
```

规则二例子：

```python
args = [1, 2, 3]
*a, *b = args   # SyntaxError: two starred expressions in assignment
```

规则三例子：

```python
args = (1, 2, 3)
*a, = args
print(a)    # [1, 2, 3]
```

```python
for a, *b in [(1, 2, 3), (4, 5, 6, 7)]:
    print(b)

# 第一轮输出：[2, 3]
# 第二轮输出：[5, 6, 7]
```

规则四例子：

```python
def foo(**kwargs, a, *args):
    pass

# SyntaxError: invalid syntax
```

规则五例子：

```python
s = {1, 2, 3}
a, b, c = *s,   # *s 会先将 s 从 set 转换成 tuple，再赋值
```

```python
args = [1, 2, 3]
kwargs = {1: 1, 2: 2, 3: 3}

def foo(*args, **kwargs):
    print(type(args))   # <class 'tuple'>
    print(type(kwargs)) # <class 'dict'>
```

## 使用场景

1. 装饰器
2. 参数不确定时的函数传参
3. 赋值

场景一例子

```python
def decorator(f):
    def inner(*args, **kwargs):
        f(*args, **kwargs)
    return inner


@decorator
def test(name):
    print('name: {}'.format(name))


test('tiger')   # name: tiger
```

场景二例子：

```python
def foo(*args):
    print(args)


foo(1)
foo(1, 2)
foo(1, 2, 3)

# (1,)
# (1, 2)
# (1, 2, 3)
```

场景三例子：

```python
*a, = [1, 2, 3]
print(a)    # [1, 2, 3]
```

## 参考

- [PEP-3132](https://www.python.org/dev/peps/pep-3132/)