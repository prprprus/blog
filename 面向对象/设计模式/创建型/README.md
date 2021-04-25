创建型设计模式的核心是解耦客户端对某个类的实例化操作。不直接用该类去初始化，而是通过新增一个中间工厂类去做实例化

> 使用前提一般是类的数量比较多、继承关系比较多、能比较充分表现出多态性

使用创建型设计模式的大致的步骤是：

1. 整理好类的继承关系
2. 定义一个中间工厂类
3. 当客户端需要创建实现类时，通过调用工厂类的方法去创建，类似
    ```BASH
    factor = FactoryMiddleClass();
    obj = factor.create_object();
    ```

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F-13.png)
