# Python 中的面向对象

## What

面向对象是一种编程范式，和面向过程一样，也可以将现实世界中的事物抽象成程序代码。相比面向过程，面向对象额外提供了：类、对象、实例化、类变量、实例变量、类方法、实例方法、静态方法、方法重写、方法重载、封装、继承、多态等概念和工具。

和传统的面向对象语言相比，Python 的面向对象机制会有比较多不同的地方，这也是重点讨论的对象。

## Why

让代码扩展性更强、耦合程度更低、减少重复代码。

## How

### 三大特性

封装、继承、多态，是面向对象的三大特性。

#### 和根正苗红的面向对象语言 Java 做对比

1. 封装的核心之一是控制好访问权限。Java 有四种访问权限，Java 的访问权限控制是真的。Python 有两种访问权限，Python 的访问权限是假的，只是一种人为的约束。
   ```ipython
   In [12]: class A:
    ...:     __name = "A"
    ...:
    ...:     def __get_data(self):
    ...:         print("A get_data")
    ...:
    ...:
    ...: a = A()
    ...: # a.__name, a.__get_data() 不能访问只是因为 Python 解析器将其改了名字, 换这种写法就能访问了
    ...: print(a._A__name)
    ...: a._A__get_data()
   A
   A get_data
   ```
2. Python 支持多继承，Java 不支持。
3. Python 可以做出多态的效果，但是由于它本身就是动态语言，没有类型限制，所以感觉多态这个概念在 Python 中略显鸡肋。

如果和标准的面向对象语言对比，Python 的面向对象支持可以算是残废的。但是 Python 也有自己的面向对象风格，也就是「鸭子类型」，具体体现在各种魔术方法上，比如通过重写这些魔术方法来实现多态的效果。

### 类变量和类方法

#### 修改类变量时的陷阱

在 Java 中，所有对象实例共享类变量，只要其中一个实例对类变量做了更新操作，其他实例再访问就会得到新的值。

但是到了 Python 中就会有一个陷阱：

```ipython
In [14]: class A:
    ...:     flag = 1
    ...:
    ...:     def __init__(self):
    ...:         pass
    ...:
    ...:
    ...: class B(A):
    ...:     def __init__(self):
    ...:         super().__init__()
    ...:
    ...:
    ...: obj_1 = B()
    ...: print(obj_1.flag)  # 期待是 1，结果也是 1
    ...:
    ...: obj_2 = B()
    ...: obj_2.flag = 2
    ...: print(obj_2.flag)  # 期待是 2，结果也是 2
    ...: print(obj_1.flag)  # 期待是 2，结果也是 1
    ...:
    ...: obj_3 = B()
    ...: obj_3.flag = 3
    ...: print(obj_3.flag)  # 期待是 3，结果也是 3
    ...: print(obj_2.flag)  # 期待是 3，结果也是 2
    ...: print(obj_1.flag)  # 期待是 3，结果也是 1
1
2
1
3
2
1
```

可以看到并没有得到像预期那样的结果，这是因为当通过实例来更新类变量时，解析器并没有修改类变量，反而是创建了一个新的实例变量。

解决这个问题最简单的方法是不要通过实例更新类变量，而是通过类本身：

```IPYTHON
In [16]: class A:
    ...:     flag = 1
    ...:
    ...:     def __init__(self):
    ...:         pass
    ...:
    ...:
    ...: class B(A):
    ...:     def __init__(self):
    ...:         super().__init__()
    ...:
    ...:
    ...: obj_1 = B()
    ...: print(B.flag)  # 期待是 1，结果也是 1
    ...:
    ...: obj_2 = B()
    ...: B.flag = 2
    ...: print(obj_2.flag)  # 期待是 2，结果也是 2
    ...: print(obj_1.flag)  # 期待是 2，结果也是 2
    ...:
    ...: obj_3 = B()
    ...: B.flag = 3
    ...: print(obj_3.flag)  # 期待是 3，结果也是 3
    ...: print(obj_2.flag)  # 期待是 3，结果也是 3
    ...: print(obj_1.flag)  # 期待是 3，结果也是 3
1
2
2
3
3
3
```

另外一个方法是 TODO。

#### 类方法

1. 类方法的 `self` 指的是类本身
2. 类方法只能使用类变量

```IPYTHON
In [25]: class A:
    ...:     flag = 1
    ...:     __flag = 2
    ...:
    ...:     def __init__(self, name):
    ...:         self.__name = name
    ...:
    ...:     @classmethod
    ...:     def show(self):
    ...:         print(self)  # 类本身
    ...:         print(A.flag)
    ...:         print(A.__flag)
    ...:         print(self.__name)  # 异常，因为不能访问实例变量
    ...:
    ...:
    ...: a = A("A")
    ...: a.show()
<class '__main__.A'>
1
2
---------------------------------------------------------------------------
AttributeError                            Traceback (most recent call last)
<ipython-input-25-c984f6e37d2f> in <module>
     15
     16 a = A("A")
---> 17 a.show()

<ipython-input-25-c984f6e37d2f> in show(self)
     11         print(A.flag)
     12         print(A.__flag)
---> 13         print(self.__name)  # 异常，因为不能访问实例变量
     14
     15

AttributeError: type object 'A' has no attribute '_A__name'
```

### 一些重要的方法

1. `__init__()` 是赋值操作，`__new__()` 才是创建对象操作
2. `super()` 用于调用父类的方法
3. `@property`/`XXX.setter` 用于简化 getter/setter 方法
