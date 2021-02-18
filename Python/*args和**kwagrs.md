# *args 和 **kwargs

## What

*args 和 **kwargs 都是一种可变长参数的写法。前者可以展开单个值的集合，如 list、tuple、set 等；后者可以展开键值对的集合，如 dict。

## Why

让 Python 可以更加灵活，无论是函数参数的可变性，还是赋值时的动态展开，都能让这门语言给人很灵活的感觉。

常见使用场景：

1. 装饰器
2. 参数个数不确定时
3. 赋值展开

## How

### 语法规则

1. 在赋值操作时，`*args` 必须和 `,` 号组成表达式，不能单独存在
    ```python
    # 错误示范
    # SyntaxError: starred assignment target must be in a list or tuple
    args = [1, 2, 3]
    *a = args

    # 正确示范
    *a, = args
    print(a)    # [1, 2, 3]
    ```
2. 赋值表达式的左边只能有一个 `*` 号
    ```python
    # 错误示范
    # SyntaxError: two starred expressions in assignment
    args = [1, 2, 3]
    *a, *b = args
    ```
3. 赋值表达式左边的 `*` 号会被解析成 list
    ```python
    args = (1, 2, 3)
    *a, = args
    print(a)    # [1, 2, 3]

    for a, *b in [(1, 2, 3), (4, 5, 6, 7)]:
        print(b)
    # 第一轮输出：[2, 3]
    # 第二轮输出：[5, 6, 7]
    ```
4. 函数列表中的 `*` 号必须遵守这个顺序：some_func(fargs, *args, **kwargs)
    ```python
    # 错误示范
    # SyntaxError: invalid syntax
    def foo(**kwargs, a, *args):
        pass
    ```
5. 函数体内的 *args 会被转换成 tuple，**kwargs 会被转换成 dict
    ```python
    args = [1, 2, 3]
    kwargs = {1: 1, 2: 2, 3: 3}

    def foo(*args, **kwargs):
        print(type(args))   # <class 'tuple'>
        print(type(kwargs)) # <class 'dict'>
    ```
6. set 的 *args 会被转换成 tuple
    ```python
    s = {1, 2, 3}
    a, b, c = *s,   # *s 会先将 s 从 set 转换成 tuple，再赋值
    ```

## 参考

- [PEP-3132](https://www.python.org/dev/peps/pep-3132/)