# String 类型

## 不可变性

```
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
    /** The value is used for character storage. */
    private final char value[];
```

由于 `String` 的源码定义可知它被设计成不可继承、不可修改的类型，这样做的好处有：

- 可以被缓存起来
- 天生线程安全
- 用 `String` 做哈希计算时，可以缓存计算结果

## 性能相关问题

### 字符串拼接

字面量字符串的拼接会被编译器优化：

```
public static void main(String[] args) {
    // 会被编译器直接优化为：String str = "abc";
    String str = "a" + "b" + "c";
}
```

非字面量字符串拼接也会被编译器优化：

```
public static void main(String[] args) {
    String str = "";
    for(int i=0; i<1000; i++) {
        // 会被编译器优化为：
        // str = (new StringBuilder(String.valueOf(str))).append(i).toString();
        str = str + i;
    }
}
```

但是每次创建一个 `StringBuilder` 对象对性能也会有一定的影响，所以可以显式使用 `StringBuilder`。

### String.intern()

字符串字面量默认会被放到缓存池中，也可以通过 `intern()` 主动将字符串变量放到缓存池中。

`intern()` 的处理步骤：

1. 判断缓存池中是否存在相同值的对象
2. 如果有，直接返回该对象的引用
3. 如果没有，创建一个并放到缓存池中，再返回该对象的引用

```
String s1 = new String("aaa");
String s2 = new String("aaa");
String s3 = new String("aaa");
System.out.println(s1 == s2);   // false
System.out.println(s1 == s3);   // false

String s1 = "111";
String s2 = s1.intern();
String s3 = "111";
System.out.println(s1 == s2);   // true
System.out.println(s1 == s3);   // true
```

### String/StringBuilder/StringBuffer 的区别

从是否可变方面看：

- `String` 不可变
- `StringBuilder` 可变
- `StringBuffer` 可变

从线程安全方面看：

- `String` 线程安全
- `StringBuilder` 线程不安全
- `StringBuffer` 线程安全（通过 `synchronized` 实现）
