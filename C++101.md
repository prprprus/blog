# C++ 101

## 内存模型

### 作用

不同的对象会放到不同的内存分区，不同的内存分区决定着对应的生命周期规则。

### 概念

- 代码区：放二进制形式的代码，由操作系统进行管理
- 全局区：放全局变量、静态变量、常量（字符串字面值、全局常量），程序结束后由操作系统释放
- 栈区：放函数形参值、局部变量，由编译器负责释放
- 堆区：由用户分配和释放，如果没有人为释放，程序结束后由操作系统释放

## 引用

### 作用

- 简化、屏蔽裸指针
- 作为函数形参：可以通过形参修改实参，这里会存在一些副作用，所以如果在设计的时候确定不需要修改实参，应该主动声明为 const 引用
- 作为函数返回值：
    - 不要返回局部变量的引用
    - 返回引用的函数，函数调用可以作为左值

### 语法

- 简单理解：引用是别名
- 本质理解：引用是指针常量（int* const ptr = &a）
- 引用必须初始化
- 引用在初始化后，不可以改变

### 示例

```C++
#include <iostream>

int a = 1;
int& b = a;

void foo(int& a, int&b) {
    a = 0;
    b = 1;
}

void bar(const int& a, int& b) {
    std::cout << a << endl;
    b = 0;
}

int& boo(int& a){
    a = 1;
    return a;
}

boo(a) = 3;
```

## 左值

- pass

## 面向对象

### 对象特性

#### 访问权限

##### 作用

- 有了 private 访问权限，可以控制类成员的读写权限，比如可以设置成只读或者只写
- 设置属性时，可以对外部传入的数据进行检测，增强程序的健壮性

##### 语法

- public：类内可以访问，类外不能访问，子类可以访问父类的 public 内容
- protected：类内可以访问，类外不能访问，子类可以访问父类的 protected 内容
- private：类内可以访问，类外不能访问，子类不可以访问父类的 private 内容
- 默认权限：private

#### 构造函数和析构函数

##### 作用

- 构造函数：创建对象、申请资源
- 析构函数：释放资源

##### 语法

- 构造函数在创建对象时被编译器调用一次，进行申请相关资源、属性赋值等操作，可以有参数，可以发生重载
    - 构造函数的种类：
        - 无参构造函数（默认构造函数）
        - 有参构造函数
        - 拷贝构造函数
            - 拷贝构造函数的调用时机：
                - 用一个已存在的对象创建另外一个新对象时
                - 函数参数值传递时
                - 以值得方式返回局部对象时
            - 注意：如果属性有在堆区开辟的，一定要自己实现深拷贝构造函数，防止默认的浅拷贝带来的问题
    - 注意事项：
        - 调用默认构造函数时，不需要加 `()`
        - 如果用户实现了有参构造函数，编译器就不会提供默认构造函数，但是会提供拷贝构造函数（编译器提供的拷贝构造函数是浅拷贝）
        - 如果用户实现了拷贝构造函数，编译器就不会提供任何构造函数
        - 深拷贝和浅拷贝：
            - 浅拷贝：只有赋值操作
            - 深拷贝：在堆区重新申请内存，再进行赋值操作
- 析构函数在对象销毁前被编译器调用一次，进行资源释放操作，没有参数，不能发生重载

##### 示例

```C++
#include <iostream>

class People {
public:
    People() {
        
    }

    People(std::string _name, int _age): name(_name), age(_age) {

    }

    People(const People &p) {
        this->name = p.name;
        this->age = p.age;
    }

    ~People() {
        std::cout << "run ~People()" << endl;
    }

private:
    std:string name;
    int age;
};


int main() {
    // 构造函数的使用
    People* p1 = new People();
    People* p2 = new People("tiger", 23);
    People* p3 = new People(*p2);

    People p4;
    People p5("tiger", 23);
    People p6(p5);

    People p7 = People();
    People p8 = People("tiger", 23);
    People p9 = People(p8);
}
```

#### 静态成员

##### 作用

- 对象之间共享属性和方法

##### 语法

- 静态属性：
    - 所有对象共享同一份数据
    - 在编译阶段分配内存
    - 类内声明，类外初始化
- 静态方法：
    - 所有对象共享同一个方法
    - 静态方法只能访问静态属性

##### 示例

```C++
class People {
public:
    static void showAge() {
        std::cout << age << endl;
    }

private:
    static int age;
};

int main() {
    People::age = 0;
    People::showAge();
}
```

#### this 指针

##### 作用

由于在一个类中，除了非静态属性外，其他的东西（静态属性、静态方法、方法）都不属于对象本身。

那么，这些共享、只读的东西如何知道调用它们是那个对象？这就要靠 this 指针。

- this 是一个指针常量
- 类的属性名和形参同名时，可以通过 this 指针做区分
- 非静态方法想要返回对象本身时，可以 `return *this;`

##### 语法

- 谁发起调用，this 指针就指向谁
- 静态方法没有 this 指针（这也是静态方法不能访问非静态属性的原因之一）

#### const 方法和 const 对象

##### 语法

- const 方法声明：在方法签名后面加上 const
- 一般来说，const 方法不能修改属性，但是可以修改声明了 mutable 的属性
- const 对象只能调用 const 方法、mutable 属性

### 友元

#### 作用

- 让一个函数或者类，访问另一个类中的私有成员

#### 语法

- 全局函数做友元
- 类做友元
- 方法做友元

#### 示例

```C++
#include <iostream>

class People {
public:
    People(int _age): age(age) {}

private:
    int age;

// 全局函数做友元
friend void showAge(const People& p);

// 类做友元
friend class Dog;

// 方法做友元
friend void People2::showAge(const People& p);
}

void showAge(const People& p) {
    std::cout << p.age << endl;
}

class People1 {
public:
    People1(People& _p): p(_p) {}

    void showAge() {
        std::cout << p.age << endl;
    }

    void showAgeAgain() {
        std::cout << p.age << endl;
    }

private:
    People p;
}

class People2 {
public:
    void showAge(const People& p) {
        std::cout << p.age << endl;
    }
}

int main() {
    People p = People(23);
    showAge(p);

    People1 p1 = People1(p);
    p1.showAge();

    People2 p2 = People2();
    p2.showAge(p);
}
```

### 运算符重载

#### 作用

- 让自定义类型也可以进行 +、-、*、/、%、= 等运算

#### 语法

- 加号运算符重载
- 左移运算符重载
- 递增运算符重载
- 赋值运算符重载
- 关系运算符重载
- 函数调用运算符重载（仿函数、可调用对象）
- 运算符重载也可以发生函数重载

#### 示例

```C++
#include <iostream>
#include <string>

// 加号运算符重载
class People {
public:
    People(int _a): a(_a) {}

    People operator+(const People& p) {
        People tmp_p;
        tmp_p.a = a + p.a;
        return tmp_p;
    }

private:
    int a;
}

int main() {
    People p1 = People(1);
    People p2 = People(2);
    People p3 = p1 + p2;
}

// 左移运算符重载
class People {
public:
    People(int _a): a(_a) {}

private:
    int a;

friend ostream& operator<<(ostream& _cout, const People& p);
}

ostream& operator<<(ostream& _cout, const People& p) {
    _cout << p.a;
    return _cout;
}

int main() {
    People p = People(0);
    std::cout << p << endl;
}

// 递增运算符重载
class Peoepl {
public:
    People(int _a): a(_a) {}

    // 前置递增
    People& operator++() {
        a++;
        return *this;
    }

    // 后置递增（利用占位参数语法，强行进行函数重载，实际上这个参数没用到）
    People operator++(int) {
        People tmp_p;
        tmp_p.a = this->a;
        a++;
        return tmp_p;
    }

private:
    int a;
}

int main() {
    People p = People(0);
    ++p;
    p++;
}

// 赋值运算符重载
class People {
public:
    People(int _a): {
        a = new int(_a);
    }

    ~People() {
        if (a != nullptr) {
            delete a;
            a = nullptr;
        }
    }

    // 符合深拷贝的赋值运算符重载
    People& operator=(const People& p) {
        if (a != nullptr) {
            delete a;
            a = nullptr;
        }

        a = new int(p.a);
        return *this;
    }

private:
    // a 在堆区，所以赋值运算符重载需要考虑深拷贝问题
    int* a;
}

int main() {
    People p1 = People(10);
    People p2 = p1;
}

// 关系运算符重载
class People {
public:
    People(int _a): a(_a) {}

    bool operator==(const People& p) {
        if (a == p.a)
            return true;
        else:
            return false;
    }

private:
    int a;
}

int main() {
    People p1 = People(1);
    People p2 = People(2);
    std::cout << (p1 == p2) << endl;    
}

// 函数调用运算符重载
class People {
public:
    People(): a(0) {}
    People(int _a): a(_a) {}

    void operator()(std::string text) {
        std::cout << text << endl;    
    }

private:
    int a;
}

int main() {
    People p1 = People(1);
    p1("tiger");
    People()("tiger");
}
```

### 继承

#### 作用

- 抽象现实世界的方式之一
- 代码复用
- 多态的基础

#### 语法

- `class 类名 : 继承方式 父类名`
- 继承方式
    - public：父类的 public、protected 继承到子类的 public、protected 区域
    - protected：父类的 public、protected 继承到子类的 protected 区域
    - private：父类的 public、protected 继承到子类的 private 区域
    - 注意：父类中的 private 也会被子类继承，只是不允许访问
- 继承中构造和析构的顺序
    - 构造函数的顺序：先有爸爸后有儿子，所以先执行父类构造函数，在执行子类构造函数
    - 析构函数的顺序：拿一个栈先装父类，再装子类，析构相当于出栈操作，所以先执行子类析构函数，再执行父类析构函数
- 父类和子类某些属性或者方法同名时的处理方式
    - 同名属性/方法处理
        - 调用子类的：`obj.a`，`obj.func()`
        - 调用父类的：`obj.Base::a`，`obj.Base::func()`
    - 同名静态属性处理
        - 调用子类的：同上
        - 调用父类的：同上
    - 注意：反正如果想要调用父类的属性或者方法，加作用域就完事了

#### 示例

```C++

```

### 多态

#### 作用

- 让 C++ 的抽象能力更强

#### 语法

- 静态多态：函数重载，运算符重载
- 动态多态：基于继承的向上类型转换
- 静态多态和动态多态的区别：前者的函数地址在编译期就确定了，后者在运行期才确定

> 下面讨论的都是动态多态

- 多态的满足条件
    - 有继承关系
    - 子类重写了父类的虚函数
        - 重写
            - C++ 的重写必须函数名称、参数列表、返回值都要相同
            - C++ 和 Java 重写的区别：https://blog.csdn.net/weixin_30396699/article/details/95858841
- 多态使用条件
    - 父类指针或引用指向子类对象
- 多态的基本原理
    - ![多态的基本原理](https://github.com/hsxhr-10/Blog/blob/master/image/C%2B%2BVirtual原理.png)
- 純虚函数和抽象类
    - 純虚函数语法：`virtual 返回值类型 函数名(参数列表) = 0;`
    - 抽象类无法实例化
    - 子类必须重写父类的纯虚函数，否则也属于抽象类
- 虚析构和純虚析构
    - 在使用多态时，如果子类中有属性开辟到堆区，那么父类指针在释放时无法调用到子类的析构函数。解决方法是将父类的析构函数定义成虚析构或者純虚析构
    - 虚析构和純虚析构的异同点
        - 相同点
            - 都可以解决多态时父类指针无法调用子类析构函数的问题
        - 不同点
            - 含有純虚析构的类属于抽象类，无法实例化
            - 純虚析构必须父类主动实现一次

#### 示例

```C++

```

### 文件操作

#### 作用

- 读写文件，将数据持久化，是程序的基本功能之一

#### 语法

- 文件类型：文本文件、二进制文件
- fstream：读写操作
- ostream：写操作
- istream：读操作
- 打开方式
    - ios::in：以读的方式打开文件
    - ios::out：以写的方式打开文件
    - ios::app：以追加的方式打开文件
    - ios::trunc：如果文件存在则先删除，再重新创建
    - ios::binary：以二进制方式打开文件

#### 示例

```C++
// 参考: 
// - https://sample-programs.therenegadecoder.com/projects/file-io/c-plus-plus/
// - https://www3.ntu.edu.sg/home/ehchua/programming/cpp/cp10_IO.html

#include <iostream>
#include <fstream>
#include <string>

void write_file()
{
    std::fstream out("file.txt", std::ios::out);

    if(!out.is_open())
    {
        std::cout << "Error opening file!\n";
        return;
    }

    out << "This text will be written to the file!\n";
    out << "This line also will be written!\n";

    out.flush();
    out.close();
}

void read_function()
{
    std::fstream in("file.txt", std::ios::in);

    if(!in.is_open())
    {
        std::cout << "Could not open file for reading!\n";
        return;
    }

    std::string line;
    while(std::getline(in, line))
    {
        std::cout << line << "\n";
    }

    in.close();
}

int main()
{
    write_file();
    read_file();
}
```
