# 同步 IO

1. 三个 IO 类型：Text IO、Binary IO、Raw IO
2. IO 类继承关系和特点
3. `open()` 函数
4. 常用 IO 类的 `read()`，`write()` 方法
5. 性能和线程安全

## IO 类型

Python 支持三种 IO 类型：Text IO（文本 IO）、Binary IO（带缓冲的二进制 IO）、Raw IO（不带缓冲的二进制 IO）

- Text IO 作用于字符串对象，必须带缓冲，可以是行缓冲、系统默认缓冲大小或者自定义的缓冲大小
- Binary IO 作用于二进制对象，必须带缓冲，可以系统默认缓冲大小或者自定义缓冲大小
- Raw IO 作用于二进制对象，不能带缓冲

## IO 类继承关系

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/pythonio-1.png)

IOBase 并没有实现读写方法，而是它的子类们去实现。一般常用的 IO 类有 TextIOWrapper、StringIO、BytesIO、
BufferedReader、BufferedWriter

## open() 函数

## 读和写

## 性能和线程安全
