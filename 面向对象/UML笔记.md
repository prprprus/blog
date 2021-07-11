# UML 笔记

几乎每个工程专业都会有绘图这一项事情，软件工程也不例外。以前刚开始知道 UML 的时候，觉得这东西特虚，顶多装逼用。
但是随着工作时间的增长，慢慢体会到适当使用 UML，还是有不少好处的，特别是在多人协作、跨多个部门协作的情境下，UML 的作用可能会更加明显

当我们准备做一个新项目、一个新需求时，UML 可以帮助理清其中逻辑，更准确更具体地描述系系统的相关情况，而且能让项目的各个参与人都有一个明确具体的可讨论的对象；
如果给一些已经完成的项目适当加上一些 UML， 无论对新来的同事或者跨部门协作开发都是有一定好处

UML 分为「结构建模图」和「行为建模图」两种大类型，共有 14 种图表类型（可能随着时间的变化会有些许出入）。常用的有
用例图，流程图、类图、时序图

## 类图

### 类表示

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-1.png)

第一部分是类名，第二部分是属性定义，第三部分是方法定义

符号解析：

- `+` 代表 public
- `#` 代表 protected
- `-` 代表 private
- `~` 代表包级别（Java）

### 继承

描述了父类和子类的关系

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-2.png)

### 实现

描述了接口和实现类的关系

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-3.png)

### 依赖

一般用强依赖就行了

#### 弱依赖

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-4.png)

#### 强依赖

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-5.png)

### 组合

一般用弱组合就行了

#### 强组合

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-6.png)

#### 弱组合

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-7.png)

### 嵌套类

嵌套类也叫内部类

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-8.png)

## 时序图

时序图能够比较好地描述多个系统之间的交互逻辑，特别是那些不是三两句话说得清的交互

比如微信支付时序图：

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-sequence-1.png)

符号解析：

- 垂直的长长的虚线叫生命线
- 生命线上的矩形叫激活点，也就是系统会发生执行某些操作
- 系统本身也可以自激活，也就是那些拐弯的箭头
- 同步消息：实线实心箭头
- 异步消息：实线非实心箭头
- 返回：虚线非实心箭头
- alt 框：可能发生，也可能不发生的情况，类似 if

## 参考

- https://sparxsystems.cn/resources/uml2_tutorial/
