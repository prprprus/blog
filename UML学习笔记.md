# UML 学习笔记

几乎每个工程专业都会有绘图这一项事情，软件工程也不例外，对于面向对象程序设计来说，UML 就是其中的业内标准。以前刚开始知道 UML 的时候，觉得这东西特虚，没什么用，顶多装逼用。
随着工作时间的增长，慢慢体会到适当使用 UML，还是有不少好处的，特别是在多人协作、跨多个部门协作的情境下，UML 的作用可能会更加明显。
当我们准备做一个新项目、一个新需求时，UML 可以帮助理清其中逻辑，更准确更具体地描述系系统的相关情况，而且能让项目的各个参与人都有一个明确具体的可讨论的对象；
如果给一些已经完成的项目适当加上一些 UML， 无论对新来的同事或者跨部门协作开发都是有一定好处。

UML 分为「结构建模图」和「行为建模图」两种大类型，共有 14 种图表类型（可能随着时间的变化会有些许出入）。一般来说，我们并不需要用到全部，就个人而言平时用得到的有：
时序图、类图、活动图，而活动图一般等价于流程图，所以下面重点记录下类图和时序图的一些基本用法。

> 画图工具用的是 [processon](https://www.processon.com)

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

继承图描述了父类和子类的关系

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-2.png)

### 实现

实现图描述了接口和实现类的关系

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-3.png)

### 依赖

依赖图描述了两个类的依赖关系，有强依赖和弱依赖之分。比如人可以抽烟，也可以不抽烟，所以人和烟草之间是弱依赖；但是人必须喝水，所以人和水之间是强依赖。
一般具体实现是所依赖的类会作为属性存在，和组合的实现会比较像，但是意义还是有区别的。

#### 强依赖

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-4.png)

#### 弱依赖

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-5.png)

### 组合

组合图描述了两个类的包含关系，有强组合和弱组合之分。比如大脑是人的一部分，大脑的生命周期目前理论上是和人一样的，所以大脑和人之间是强组合；
人可以拥有车，但是车和人可以独立存在，车也可以易主（求别杠脑移植 🐶 ），所以车和人之间是弱组合

> 一般弱组合可能用得多一点

#### 强组合

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-6.png)

#### 弱组合

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-7.png)

### 嵌套类

嵌套类或者叫内部类，在 Java、Python 这些语言中都比较常见

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-class-8.png)

### 小结

个人感觉那些符号（比如不同类关系的箭头表示），能遵守的还是尽量遵守，但是也没必要死记硬背，说极端点哪怕随便画根线，在旁边标注好是哪种类关系，照样能用。
强行硬搬设计模式，不如没有设计模式，画 UML 同理

## 时序图

时序图能够比较好地描述多个系统之间的交互逻辑，特别是那些不是三两句话说得清的交互

案例，微信支付时序图：

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/uml-sequence-1.png)

符号解析：

- 垂直的长长的虚线叫生命线
- 生命线上的矩形叫激活点，也就是系统会发生执行某些操作
- 系统本身也可以自激活，也就是那些拐弯的箭头
- 同步消息：实线实心箭头
- 异步消息：实线非实心箭头
- 返回：虚线非实心箭头
- alt 框：可能发生，也可能不发生的情况，类似 if
