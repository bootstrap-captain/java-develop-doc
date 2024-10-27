# 入门

## 1. 基本理念

### 1.1 JAVA：跨平台语言

- .java文件被编译成.class文件后，可在不同操作系统上的JVM执行

![image-20221110114507987](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221110114507987.png)

### 1.2 JVM：跨语言的平台

- 不同语言通过各自编译器编译源文件(.java, .kotlin, etc)
- 编译时遵循JVM规范，得到的.class文件就可以在JVM上执行
- JVM只关心.class文件的格式，并不关心其对应的源文件是什么

![image-20221110115907781](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221110115907781.png)

### 1.3 多语言混合编程

```bash
# Java字节码
- 由.java文件，经过java编译器编译后的.class文件
- 也可是其他多种语言编译后的，能够在JVM平台上执行的
- 执行的对应的字节码格式是一致的
- 以上统称为JVM字节码

1. JVM和java语言没有必然的联系，  JVM只和特定的二进制文件 .class文件格式关联
2. .class文件包含了JVM的指令集和符号表，还有其他辅助信息

# 多语言混合编程
- 特定领域的语言去解决特定领域的问题
- 软件的不同层面(MVC) 可以交给不同的语言去编写
- 最终所有的语言都会被编译成JVM可以识别的  .class文件，从而在同一个JVM中使用
- 各种语言的交互不存在困难，就和使用自己的原生API一样，因为最终都是在一个JVM中去执行的
```

## 2. JVM

### 2.1 虚拟机(Virtual Machine)

- 虚拟的计算机，一款软件，用来执行一系列虚拟计算机指令

```bash
# 系统虚拟机
- 对物理计算机的仿真，提供了一个可运行完整操作系统的软件平台
- 比如VMware

# 程序虚拟机
- 专门执行单个计算机程序而设计
- 比如Java虚拟机，在java虚拟机中执行的指令： java字节码指令/其他语言的字节码指令

两种虚拟机上运行的软件，都被限制于虚拟机提供的资源
```

### 2.2 Java虚拟机

- Java Virtual Machine，二进制字节码的运行环境

```bash
# 作用
- 装载字节码到其内部
- 解释/编译为对应平台上的机器指令执行(windows，linux，mac)

#  优缺点： 自动管理可能带来意想不到的问题
- 一次编译，处处运行
- 自动内存管理
- 自动垃圾回收功能
```

### 2.3 JVM的位置

- JVM和硬件并没有直接的交互

![image-20221110153140386](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221110153140386.png)

## 3. JVM整体架构

### 3.1 简图

- HotSpot虚拟机是目前使用最广泛的一个虚拟机

```bash
# 类装载子系统
- 将 .class文件加载到内存中，生成对应的Class对象

# 方法区和堆内存
- 多个线程共享

# Java栈， 本地方法栈， 程序计数器
- 每个线程独有

# 执行引擎
- 将热点代码进行编译

# 垃圾回收器
- 负责回收垃圾

1. 前端编译器： .java文件编译成.class文件
2. 后端编译器： 执行引擎将热点代码进行编译，将字节码指令转换为操作系统的指令
```

![image-20221120103323627](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221120103323627.png)

### 3.2 Java执行流程

```bash
1. java前端编译器：  .java文件编译为  .class文件， 会将一些错误在编译器展现出来(前端编译错误)
2. 翻译字节码：将字节码指令编译为系统可识别的指令
3. JIT编译器： 将字节码指令中的一些热点代码抽取出来，翻译成系统可识别的的二进制内容，缓存到方法区，提高性能
```

![image-20221120110055058](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221120110055058.png)

```bash
# 解释器
- 将字节码指令逐行编译成本地机器指令执行，效率较慢

# JIT编译器： 后端编译器
- 将热点字节码抽取出来，比如for循环中的代码。编译后缓存在方法区内部，相对较快

# 区别
- 解释器就像步行，很快可以起步上路。    如果只用解释器，速度慢，但是可以立刻运行
- JIT就像公交车，速度很快，但是要等。   如果只用JIT，虚拟机启动后预热时间长，但运行后就可以很快
- 一般的JVM都是解释器+JIT编译器
```

![image-20221120112442650](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221120112442650.png)

## 4. JVM生命周期

### 4.1 启动

- 通过引导类加载器(boostrap class loader)创建一个初始类(initial class)来完成
- 初始类由虚拟机的具体实现来具体指定

```bash
1. 比如通过main方法开启，是由初始类来做一些前期的准备工作(main所在的类及父类，以及其他的很多类)
2. 准备工作做好后，main方法所在类以及其父类，都会依次被加载进来
```

### 4.2 执行

- 一个运行中的Java虚拟机有一个清晰的任务：执行Java程序
- 程序开始执行JVM才会运行，程序结束时JVM停止
- 执行Java程序时，就是开启了一个JVM的进程
- 可以通过jps来进行查看当前jvm的进程

### 4.3 退出

- 程序正常执行结束
- 程序在执行过程中遇到了异常或错误而异常终止
- 操作系统出现错误而导致VM进程终止
- 某线程调用Runtime类或System类的exit方法，或Runtime类的halt方法，并且JAVA安全管理器也允许这次exit或halt

```bash
# native
- 方法被native修饰，表示去调c语言了，本地方法
# Runtime类： 
- 对应运行时数据区域
- 单例对象，一个jvm进程只有一个
```

```java
package com.nike.erick.paypal;

public class Demo01 {
    /*一个类只会包含一个*/
    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        runtime.halt(1);
        runtime.exit(1);
        /*内存大小*/
        long maxMemory = runtime.maxMemory();
        System.out.println(maxMemory);
        /*java版本： 11.0.10+9-LTS*/
        Runtime.Version version = Runtime.version();
        System.out.println(version);
    }
}
```

## 5. 虚拟机历程

### 5.1 Sun Classic VM

```bash
1996年Java 1.0 版本，Sun公司的第一款商用JVM, JDK1.4 时候已经淘汰

1. 内部只提供解释器
2. 如果使用JIT， 就需用外挂。一旦使用JIT， JIT就会接管虚拟机的执行系统，解释器就不再执行
```

### 5.2 Exact VM

```bash
# JDK 1.2 版本，sun提供了该虚拟机

Exact Memory Management
1.  虚拟机可以知道内存中某个位置的数据具体类型是什么
2.  具备现代高性能虚拟机的雏形
      2.1 热点代码探测
      2.2 编译器和解释器混合工作模式

- 英雄气短，很快被Hotspot虚拟机代替
```

### 5.3 HotSpot

```bash
# JDK 1.3-1.8 的默认虚拟机，占有绝对的市场地位，称霸武林, 后面JVM也是基于HotSpot
热点代码探测技术： hotspot
1. 通过计数器找到最具编译价值代码，触发JIT即时编译
2. 编译器和解释器协同工作，平衡程序响应时间和最佳执行性能
```

### 5.4 JRockit

```bash
# 专注于服务端应用, 注重响应时间而不是程序启动时间

1. 不关注程序启动速度，因此内部不包含解析器实现，全部代码依靠JIT编译并优化后实现
2. JRockit JVM 是世界上最快的JVM
    2.1 提供了MissonControl服务套件，用来监控，管理和分析生产环境中的应用程序的工具
3. Oracle收购后， 在1.8 版本中，在  Hotspot 基础上移植了JRockit的优秀特性
```

### 5.5 J9

```bash
IBM Technology for Java Virtual Machine , IT4J, J9
1. 市场定位和HotSpot接近
2. 广泛应用于IBM的各种 Java产品
3. 最具影响力的三大商用虚拟机
```

### 5.6 其他虚拟机

```bash
IBM Technology for Java Virtual Machine , IT4J, J9
1. 市场定位和HotSpot接近
2. 广泛应用于IBM的各种 Java产品
3. 最具影响力的三大商用虚拟机
```

## 6. JVM架构

![image-20221201121513580](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221201121513580.png)

# Runtime Data Area

## 0. 架构

### 内存

- 内存是非常重要的系统资源，是硬盘和CPU的中间仓库和桥梁，承载着操作系统和应用程序的应用时
- JVM内存布局规定了java在运行过程中内存申请，分配，管理的策略，保证JVM的高效稳定运行
- 不同的JVM对于内存的划分和管理机制存在部分差异

![image-20230217165912935](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230217165912935.png)

### JVM架构

- 一个Runtime Data Area, 对应一个JVM

![image-20221204155102274](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221204155102274.png)

```bash
# 线程
- 每个java线程，都和操作系统的本地线程直接映射
- 当一个java线程准备好后，此时一个操作系统的本地线程也会创建， 当java线程终止后，本地线程也会回收
- 操作系统负责将线程的安排调度到任何一个可用的cpu上
- 一旦本地线程初始化成功，就会调用java线程中的run()
```

## 1. PC Register

- Program Counter Register：线程在切换和重新加载时，保存和读取代码的执行进度
- 一个线程中包含一个PC Register, 记录代码指令执行行行数，属于线程私有
- 通过PC Register, 保存和读取代码执行进度，用来进行线程切换
- 存储空间比较小，运行速度最快，不存在OOM

![image-20230217172644015](/Users/shuzhan/Library/Application Support/typora-user-images/image-20230217172644015.png)

## 2. Native Method Stack

### 2.1 Native Method

```bash
# 1. 一个native method，就是java调用非java代码的接口，比如c，c++
   融合不同的编程语言为java所用，最初是为了融合c和c++

#  2.如何定义
  2.1 不提供实现，实现是由非java语言实现
  2.2 native可以和其他关键字组合，但是不能和abstract组合
  2.3 比如Object 中的  public final native Class<?> getClass();
  
# 3. 为什么要用
  3.1 某些操作，java实现起来不方便
  3.2 某些操作，Java实现性能较差
  
# 4. 应用场景
  4.1 java应用要和java外面环境交互，比如操作系统，硬件信息
      本地方法提供一个简介的接口，开发无需关注java应用外的繁琐的细节
      
  4.2 与操作系统： jvm不是一个完整的操作系统，因此jvm要和操作系统交互
  
  4.3 sun java： jvm的组件，解释器就是用c来实现
  
# 5. 趋势
随着java的发展，本地接口越来越少，除非要和硬件交互
```

### 2.2 Native Library

- 将一些本地方法，保存在本地库中

### 2.3 Native Method Stack

```bash
# JVM管理java方法的调用， 本地方法栈管理本地方法的调用

1. 本地方法栈，线程私有
2. 内存大小： 固定或可动态扩展 (内存溢出和java虚拟机栈类似)
3. 具体流程： Native Method Stack 登记 native方法，
            Execution Engine执行时加载本地方法
            
 # 当线程调用本地方法时，该线程不再受JVM限制，该线程和虚拟机拥有相同的权限
 - 本地方法可以通过本地方法接口来获取虚拟机内存的Runtime Data Area
 - 本地方法可以直接使用本地处理器中的寄存器
 - 本地方法可以从本地内存的堆内存中分配任意数量的内存
 
 并不是所有的JVM都支持本地方法
 HotSpot JVM将本地方法栈和虚拟机栈合二为一
```

## 3. Stack

- stack： 运行时的单位，解决程序如何执行，如何处理数据
- heap：存储的单位，数据存在哪儿，如何存

### 3.1 介绍

```bash
# JVM Stack
- 每个线程在创建时，都会创建一个虚拟机栈
- 栈内部保存一个个的栈帧(stack frame)，对应java方法的一次次调用
- 一个线程内部只会有一个活动栈帧
- 线程私有，生命周期和线程一致

# 作用
- 主管java程序的运行，保存方法的局部变量(8种基本数据类型，引用类型的的引用地址)，部分结果，并参与方法的调用和返回

# 特点
- 快速有效的分配存储方式，访问速度仅次于PC Register
- JVM对java栈操作：方法入栈和出栈, FILO
- 不存在垃圾回收
- 可能出现OOM(不恰当的递归方法)
```

```java
package com.erick.service;

public class Test05 {
    public static void main(String[] args) {
        /*StackOverflowError*/
        main(args);
    }
}
```

### 3.2 大小设置

```bash
# java栈的大小
# 1. 固定大小
- 默认的，每个线程的栈内存在线程创建的时候独立选定
- 如果线程请求的栈容量超过了最大容量，则StackOverFlow
- VM Options:  -Xss: 设置栈内存为256k，单个线程的栈内存大小

# default value
Linux/x64 (64-bit): 1024 KB
macOS (64-bit): 1024 KB
Windows: The default value depends on virtual memory

# 2. 动态分配： 可能引发OutOfMemoryError
- 尝试扩展的时候无法申请到足够内存
- 创建新线程时没有足够的内存
```

![image-20221204161859917](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221204161859917.png)

### 3.3 存储单位

- 栈帧是一个内存模块，是一个数据集，维系着方法执行过程中的各种数据信息

```bash
- Current Frame, Current Method,  Current Class
- 执行引擎运行的所有字节码指令， 只针对当前栈帧进行创建
- 如果在一个方法中调用了其他方法，对应新的栈帧就会被创建出来，成为新的当前帧
- 可以通过debug的方式来观察方法和栈帧的一一对应
```

```bash
# 数据共享
- 不同线程中包含的栈帧是不允许存在相互引用， 不能在一个栈帧中引用另外一个线程的栈帧
- 但是可以共享主存中的数据

# 方法调用
- A方法调用B方法，B方法返回后，B栈帧会传递B方法的执行结果给A栈帧
- 同时虚拟机会丢弃B栈帧，使得A栈帧重新成为当前栈帧

# 生命周期
- 正常方法返回(return指令)，当前栈帧结束
- 抛出异常，底层栈帧弹出，同时不断向上抛出异常，直到顶端，然后抛出异常，jvm 挂掉
```

![image-20230219153438795](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230219153438795.png)

### 3.4 局部变量表

#### 基本介绍

- Local Variable，本地变量表，局部变量数组
- 数字数组来存储：方法参数，方法体内部的局部变量(基本数据类型，对象引用)，方法返回值
- 在线程的栈上的栈帧中，不存在数据安全问题
- 容量大小：在编译期间确定下来，并保存在方法的Code属性的 maximun local variables中，方法运行期间不会改变局部变量的大小
- 局部变量表中的变量只在当前方法调用中有效。当方法调用结束后，随着方法栈帧的销毁，局部变量表也会随之销毁

```bash
# 局部变量表---局部变量和方法返回值
- 局部变量表中的基本存储单元： slot插槽

# 32位数据类型(基本数据类型，引用数据类型的引用)
- 32位以内的类型只占用一个slot
- byte，short，char在存储前被转换为int
- boolean也会被转换为int，0表示false，非0表是true

# 64位数据类型：long，double
- 占两个slot

# slot插槽
- 实例方法被调用时候，方法参数和局部变量会按照顺序复制到变量表中每个slot上
- slot插槽中存放局部变量的具体的值，JVM会根据slot提供的索引，从数组中取出对应的slot中存放的值
- 假如某个数据占用了2个slot，访问时候使用起始索引。
- 当前栈是实例方法或者构造方法， slot0索引位置存放this对象引用
- 当前方法是static方法，slot0索引位置存放方法参数
- slot插槽可以重复利用（销毁的slot可以被后面的复用）
```

#### 案例1

```bash
# 静态方法slot： 不存储this，所以在static方法中不能使用成员变量和成员方法
- 索引0:   Date引用，1个slot
- 索引1:   List引用，1个slot
- 索引2:   double，2个slot
- 索引4:   flag，1个slot
- 局部变量表最大长度在编译期间已经确定，长度为5
```

![image-20230219181907932](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230219181907932.png)

#### 案例2

```bash
# 实例方法
- 索引0: this对象引用，1个slot，从而可以使用实例的其他方法或者变量
- 索引1: String引用类型，1个slot
- 索引2: double， 2个slot
- 索引4: int，1个slot， 假如调用thirdMethod(10)的时候不赋值给返回值，则slot也不会保存
- 索引5: int，1个slot
```

![image-20230219183344526](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230219183344526.png)

```bash
# 成员变量：使用前都会经历默认初始化赋值
- 类变量： linking的prepare阶段：给类变量默认赋值；               initialization阶段： 给类变量定义的值
- 实例变量：随着对象的创建，会在堆空间中分配实例变量空间，并进行默认初始化

# 局部变量： 
- 使用前，必须要进行显式赋值，否则编译不通过
- 相当于对应的slot中没有存放变量的名称和值

# 栈帧中，与性能调优关系最为密切的部分就是局部变量表
- 局部变量表会占据栈帧的大部分空间 
- 局部变量表中的变量也是重要的垃圾回收根节点，只要被局部变量表中直接或间接引用的对象都不会被回收
```

### 3.5 操作数栈

#### 基本介绍

- Operand Stack，表达式栈(Expression Stack)
- 栈实现： 数组和链表
- 每一个独立栈帧除了包含局部变量表，还包含一个操作数栈

```bash
# 作用： 
# 1.方法执行过程中，根据字节码指令，往栈中写入数据或提取数据
- 一些字节码将值压入操作数栈，一些字节码从操作数栈中取出数据，使用完成后再把结果入栈
- 复制，交换，求和
- jvm的解释引擎是基于操作数栈来 2+5 来翻译为操作系统可以执行的流

# 2.保存计算过程的中间结果，同时作为计算结果变量的 临时存储空间

# 3.是JVM执行引擎的一个工作区
- 方法开始执行时，新的栈帧也会创建出来，这个方法的操作数栈 是空的，但是会先创建出来

# 4. 每个操作数栈都会有一个明确的栈深度用于存储数组，最大深度在编译期就定义好了
- 深度保存在方法的Code属性中，为max_stack的值
- 操作数栈任何一个元素都可以是任意java类型
- 32的类型占1个深度， 64的类型占2个深度

# 5.操作数栈只能通过标准的push和pop操作来完成一次数据访问
- 通过数组来实现，但只允许从数组尾部添加获取数据
```

#### 案例1

![image-20230219220907185](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230219220907185.png)

```bash
# 指令： byte，short，boolean转还为int类型
- bipush： 如果长度是byte，则为bipush（byte int push）
- sipush： 如果长度大于byte，则为sipush (short int push)
- iconst_0: boolean值，iconst_0表示false，iconst_1表示true

# 返回值
- return: 结束方法
- ireturn： 结束方法，返回int类型

# bipush 15
- byte类型，转换为int， 向操作数栈push进15
# istore_1
- 从操作数栈中取出数据，并存放在索引为1的局部变量表中
# bipush 20
- 将int类型的数据push到操作数栈中
# istore_2
- 从操作数栈中取出数据，并存储到索引为2的局部变量表中
# iload_1
- 从局部变量表索引为1的位置，取出数据
# iload_2
- 从局部变量表索引为2的位置，取出数据
# iadd
- 依靠执行引擎，相加后得到结果
# istore_3
- 将执行结果存储在局部变量表中索引为3的位置
# return   ireturn
- 结束方法： 返回值是int时候，则ireturn
```

#### 案列2

```bash
# 如果被调用的方法有返回值，则返回值会被压入到当前栈帧的操作数栈中
- 取出的时候通过a_load_0,  取出被调用方法的操作数栈中的栈底的数据
```

![image-20230219223804403](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230219223804403.png)

### 3.6 动态链接

- Dynnamic Linking
- 作用：指向运行时常量池中，该栈帧所属方法的引用
- 字节码指令中的     #符号引用，无限套娃

```bash
# Java源文件被编译成class字节码后
- 所有的变量和方法引用，都作为符号引用（Symbolic Reference）保存在class文件的常量池中
- 描述一个方法调用了另外的其他方法，就是通过常量池中指向方法的符号引用来表示
- 动态链接的作用，就是为了将这些符号引用，转换为调用方法的直接引用

# 多线程共享，多对象共享，节省内存
- 不用这个行不行：  行！
- 在栈帧中直接保存对象的引用地址来获取堆中的对象
- 每个线程的每个方法都要保存地址引用
- 其实就是添加了一个方法区的常量池缓冲层
```
#### 方法绑定机制

- JVM中，将符号引用转换为调用方法的直接引用与方法的绑定机制有关

```bash
# 静态链接（早期绑定）
# 非虚方法：静态方法，私有方法，final方法，实例构造器，父类方法
- 当一个字节码文件被装载进JVM时，如果被调用的目标方法在编译期可知，且运行时间保持不变
- 调用方法的符号引用转换为直接引用称为静态链接

# 动态链接 （晚期绑定）
# 虚方法：其他方法
# 表示为final的方法，就会禁止动态链接
- 被调用的方法在编译期间无法确定下来
- 只能够在程序运行期将调用方法的符号引用转换为直接引用，称做动态链接

# 调用指令
- invokestatic: 调用静态方法，解析阶段确定唯一方法版本
- invokespecial：调用 <init>方法，私有方法，父类方法，解析阶段确定唯一方法版本

- invokevirtual: 调用所有虚方法，不包含final方法
- invokeinterface：调用接口方法

- invokedynamic: 动态解析出需要调用的方法，然后执行
```

### 3.7 方法返回值

```bash
1. 存放调用用该方法的pc寄存器的值
      1.1 并非返回的具体的值， 具体的返回值放到操作数栈里面去了
      1.2 以便方法结束后，能知道下一个指令去哪里执行
      1.3 将pc寄存器里面的值，交给执行引擎，让他去执行后续的操作，即继续执行调用该方法的方法

2. 方法结束： 方法退出后，都返回到该方法被调用的位置
   2.1 正常执行完成
     - 调用者的pc寄存器的值作为返回值，即调用该方法的指令的下一条指令的地址

   2.2 出现没有处理的异常，非正常退出
    - 返回地址要通过异常表来确定，栈帧中一般不会保存这部分信息

3. 方法正常调用完后，具体需要哪一个返回指令
   3.1 根据方法返回值的实际数据类型而定：
   3.2 ireturn：boolean,byte,char,short,int
   3.3 lreturn: long
   3.4 freturn: float
   3.5 dreturn: double
   3.6 areurn: 引用数据类型 
   3.7 return: void， 构造器 ， 类的构造器
```

### 3.8 栈面试题

```bash
1. 举例栈溢出的情况？(StackOverFlowError)

   不恰当的方法递归调用
   jvm的栈的大小默认是固定的，因此如果多个不当的递归调用，就会引发栈溢出
   jvm的栈的大小如果是动态的，多个递归， 就会引发OutOfMemory

2. 调整栈大小，就能保证不出现溢出吗？

   不能，不恰当的递归，最终依然会导致溢出，不过只是时间先后的问题

3. 分配的栈内存越大越好吗？
   
   机器牛逼的话，同比放大的情况下，越大越好，
   不然的话，栈内存过大，其他内存就小了

4. 垃圾回收是否会涉及到虚拟机栈

   不会，栈帧内存随着方法的弹栈就会被销毁，随着线程的结束会销毁该栈

5. 方法中定义的局部变量是否是线程安全的

  5.1 基本数据类型是安全的
  5.2 引用类型数据可能会发生逃逸
```

## 4 Heap

- 栈管运行，堆管内存

### 4.1 基本概念

```bash
# 一一对应
java进程 --- jvm实例 --- Runtime类 --- 堆空间

# 堆区
1. jvm启动后就会被创建，大小就确定
2. jvm中最大的内存空间，大小可以调节
3. 物理上可以不连续的内存空间，但在逻辑上应该视为连续
4. 所有的线程共享

# 基本特点
1. 所有的对象实例和数组 都在堆上分配内存。栈帧中只保存引用，引用指向对象或者数组在堆中位置
2. 方法结束后，堆中对象不会马上被移除，仅在垃圾收集的时候才会被移除
3. 堆是GC执行的重点区域
```

### 4.2 空间大小

- 堆区存储java对象实例，堆大小在JVM启动的时候就已经设定好了
- 包含新生代，养老区，元空间(Meta Space)

```bash
# 最小内存        -Xms:256k            -X: jvm的参数       ms: memory start 
# 最大内存        -Xmx: 1028k                             mx：memory max
- 表示堆区的起始内存(新生代+老年代)
- Survivor-0和Survivor-1只会有一个真正存放，但是内存还是会计算两份

# OutOfMemoryError
- 堆区的内存大小超过了-Xmx指定的最大内存时，OutOfMemoryError: Java heap space

# 最佳实践     -Xms和-Xmx配置相同的值！     -Xms1000M -Xmx1000M
- java垃圾回收机制处理完堆区后，不需要重新分隔计算堆区的大小，提高性能
- 避免不必要的扩缩容，增大系统压力

# 默认大小
初始内存大小：  物理电脑内存大小/64
最大内存大小：  物理电脑内存大小/4

# 打印GC简单信息或者详细信息 
-XX:+PrintGCDetails
```

```java
public class Test07 {
    public static void main(String[] args) {
        //  初始内存
        long initialMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        //  最大内存
        long maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024;

        System.out.println("初始JVM堆内存：" + initialMemory + "M");
        System.out.println("最大JVM堆内存：" + maxMemory + "M");

        System.out.println("本地物理内存：" + initialMemory * 64 / 1024 + "G");
        System.out.println("本地物理内存：" + maxMemory / 1024 * 4 + "G");
    }
}
```

### 4.3 新生代和老年代

```bash
# 存储在JVM中的java对象被划分为两类
- 生命周期短的瞬时对象，创建和消亡都很快
- 生命周期比较长，极端情况下和JVM的生命周期一样(Runtime类)

# JVM堆区划分
- 年轻代/新生代/Young Genration Space：                Eden， Survivor0和Survivor1（from区和to区）
- 老年代/Tenure Generation Space: 
- 元数据区/Meta Space:
```

#### 内存大小

- 默认配置
- 一般不去修改，如果JVM中的很多对象存活时间很长，则考虑修改
- 大多数的java对象都是在Eden中被new出来，同时也会在Eden中被销毁

![image-20230221125108024](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230221125108024.png)

```bash
# 新生代和老年代比例调整： VM Option
-XX:NewRatio=4:  新生代占1，老年代占用4

# 查看比例
jps                                  : 得到端口号
jinfo -flag NewRatio 40402           : 得到比例 

# 新生代按照指定数字分配
-Xmn100M                     显示指定，以这个为准，一般不设置

# 新生代中Eden:From:To      VM Option
-XX:SurvivorRatio=12                 :调整为 12:1:1

# 查看比例
jps                                  : 得到端口号
jinfo -flag SurvivorRatio 40402      : 查看比例 
```

#### 对象分配过程

```bash
1.1 新对象在Eden创建，满了(可达性算法)则触发    Minor GC(Young GC)
1.2 回收垃圾对象， 可用对象复制到 S0中，并加上年龄计数器

2.1 第二次Eden满后，继续触发 Minor GC
2.2 检查Eden中存活对象和S0的存活对象放到S1中，s0的age加1。（谁空放谁）

3. from区到to区时，如果版本号达到15(阈值)，则会Promotion, 放在老年代 
   -XX:MaxTenuringThreshold=15:   手动设置阈值
   假如Survivor满了，同时没有达到阈值，也会放在老年代

# 什么时候触发Minor GC
- Eden满
- Major GC会自动触发一次Minor GC
- Survivor满不会触发minor gc, 但是每次Minor GC都会触发一次Survivor的GC
```

![image-20230221161050720](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230221161050720.png)

### 4.4 常见GC

- JVM在GC时，并非每次都对新生代，老年代，元空间一起回收
- 大部分回收都是在新生代
- 垃圾回收时，会有STW,  导致用户线程的暂停

```bash
HotSpot VM包含两种收集

# 部分收集(Partial GC)： 不是完整收集整个JAVA堆中的垃圾
- Minor GC:  收集新生代（Eden, S0, S1）
- Major GC:  收集老年代
                   1. CMS GC  :  单独收集老年代

# 整堆收集(Full GC): 收集整个java堆和方法区
```

#### Minor GC

- 当年轻代Eden空间不足时，就会触发Minor GC, S0/S1满时不会触发
- Java对象大多朝生夕死， Minor GC非常频繁， 回收速度比较快
- Minor GC会引发STW, 暂停其他用户线程

#### Major GC

- 发生在老年代
- Major GC触发时，一般至少触发一次Minor GC。 先尝试Minor GC, 如果空间还是不足，则触发Major GC
- Major GC 的速度比Minor GC慢10倍以上， STW更长
- Major GC后，内存还不足，就 OOM

#### 堆分代思想

```bash
# 优点 ：优化GC 性能
1. 如果没有分代，那所有对象都在一块， gc 时候需要所有区域扫描
2. 分代后
   - 朝生夕死的对象，存放在新生代
   - 生命周期长的对象，存放在老年代
```

![image-20230221162224219](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230221162224219.png)

#### 内存分配策略

- 优先分配到Eden
- 大对象直接分配到老年代，尽量避免程序中出现过多的大对象
- 长期存活的对象分配到老年代
- 动态对象年龄判断：如果Survivor区中相同年龄的所有对象的总和大于Survivor空间的一半，年龄大于或等于该年龄的所有对象，直接进入老年代，不用等到MaxTenuringThreshold中要求的年龄

### 4.5 TLAB

- Thread Local Allocation Buffer

```bash
# 引入原因
- 堆区是多个线程共享，任何线程都可以访问堆区中共享数据
- 对象实例的创建在JVM中非常频繁，并发环境从堆区中划分内存空间，是线程不安全的。   堆区内存作为共享的数据
- 为避免多个线程操作同一地址，在分配内存时候，需要进行加锁机制。 从而影响分配内存的速度

# TLAB: 从内存分配角度，不是垃圾收集角度
- 对Eden区继续进行划分， JVM为每个线程分配私有缓存区域
- 多线程同时分配内存时， 使用TLAB可以避免一系列的非线程安全问题， 同时提高内存分配的吞吐量
- 快速分配策略

#  分配策略
- 每个线程分配资源的时候，先分配在Eden的属于该线程的TLAB中，如果已经满了，再分配在Eden剩下的区域中
- 保证线程安全，在分配的时候不用对内存资源加锁，提高性能
- JVM将TLAB作为内存分配的首选
- 一旦TLAB内存耗尽，JVM就会尝试通过使用加锁机制来确保数据操作的原子性，从而直接在Eden分配内存

# 参数调整：  默认TLAB比较小，仅占整个Eden区域的1%
# 1. 开启：默认开启
-  -XX:UseTLAB   --- 开启TLAB
-  -XX:-UseTLAB  --- 关闭TLAB

# 2 查看     -XX:+UseTLAB        -XX:-UseTLAB
- jps
- jinfo -flag UseTLAB pid

#  查看所有的参数的默认初始值
-XX:+PrintFlagsInitial          
#  查看所有参数的修改后的值
-XX:+PrintFlagsFinal   

# 打印GC信息
-  -Xlog:gc 
-  -XX:+PrintGCDetails
```

![image-20230222215943867](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230222215943867.png)

### 4.6 栈上分配

```bash
# 堆存储对象不是唯一选择
- 随着JIT编译器的法阵和逃逸技术分析逐渐成熟，产生了栈上分配和标量替换优化技术

# 逃逸分析
- 如果经过(Escape Analysis)发现，一个对象并没有逃逸出方法，那么就可能被优化成栈上分配
- 当一个对象在方法中被定义，并且对象只在方法内部使用，则认为没有发生逃逸
- 当一个对象在方法中被定义，它被外部方法所引用，则认为发生逃逸
- 没有发生逃逸的对象，则可以被分配到栈上，随着方法执行的结束，栈空间就会被移除

#  逃逸设置
- JDK7后，HotSpot默认开启逃逸分析

# 开启和关闭
#   -XX:+DoEscapeAnalysis    -XX:-DoEscapeAnalysis
- 开发中能使用局部变量的，就不要在方法外定义

# 栈上分配优点
- 栈上内存不用进行垃圾回收，不会频繁触发STW
- 栈上分配，不用考虑线程安全问题，内存分配较快; 堆上分配除了TLAB外，需要进行加锁分配内存
```

#### 逃逸案例

```java
package com.erick;

public class Demo01 {

    /*会发生逃逸*/
    public static StringBuffer createStringBuffer() {
        StringBuffer sb = new StringBuffer();
        sb.append("erick");
        sb.append("tom");
        return sb;
    }

    /*不会发生逃逸*/
    public static String createString() {
        StringBuffer sb = new StringBuffer();
        sb.append("erick");
        sb.append("tom");
        return sb.toString();
    }
}
```

```bash
#  逃逸
- 方法返回了变量的引用，就可能在其他方法被使用，因此是在堆上分配内存，交给垃圾回收器进行GC

# 不逃逸
- 对象只在方法内部使用，且其他方法不会获取到该对象的引用，就可以在栈上分配，从而通过栈的出入来进行GC
```

#### 栈上分配案例

```java
package com.erick;

public class Demo02 {

    /**
     * 开启逃逸栈上分配耗时： 3ms
     * 关闭逃逸栈上分配耗时： 330ms
     * @param args
     */
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        for (int i = 0; i < 1000000000; i++) {
            create();
        }
        long end = System.currentTimeMillis();
        System.out.println("time consumed:" + (end - start));
    }

    public static void create() {
        Student student = new Student();
    }
}
```

## 5.方法区

### 5.1 基本介绍

- 所有的方法区在逻辑上是属于堆的一部分，但一些JVM的实现不会对方法区进行GC
- HotSpot: 方法区/元空间/metaspace/非堆/Non-Heap: 目的就是和堆分开
- 方法区可以看作是一块独立于Java堆的内存空间

```bash
# 1. 各个线程共享的内存区域
- JVM启动时，方法区创建，物理内存可以不连续，逻辑内存必须连续
- 方法区大小：可以固定大小或可扩展
- 方法区大小决定了系统可以保存多少个类，可能出现OOM: java.lang.OutOfMemoryError: Metaspace
   - 系统加载过多的第三方jar包
   - Tocmat部署的工程太多
   - 代码中大量动态生成的反射类
- 释放： 关闭JVM则结束

# 2. 演变历史
- JDK7以前，方法区叫做永久代，  JDK8以后，方法区叫做元空间
- 方法区可以类比为接口， 永久代和元空间可以类比为实现类 
- 永久代使用的是JVM的内存， 元空间使用的是本地内存
- 永久代和元空间二者不只是名字变了，内部结构也调整了
- 如果方法区不满足新的内存分配需求，将抛出OOM异常
```

### 5.2 大小调整

```bash
#  jdk8
-XX:MetaspaceSize      元数据区域   
-XX:MaxMetaspaceSize   元数据区域最大,   默认-1， 表示无穷大(依赖本地内存)
-XX:MetaspaceSize=100m
-XX:MaxMetaspaceSize=100m

# 如何查看
jps
jinfo -flag MetaspaceSize pid

# 介绍
- 如果不指定大小，默认情况下，元数据空间作为方法区时，虚拟机会耗尽所有的可用系统内存
- 如果元数据区发生溢出，虚拟机一样会抛出 OutOfMemoryError: Metaspace

# -XX:MetalspaceSize:
- 设置初始的元空间大小
- 64位的服务器端JVM， 默认是21M， 又叫 《初始的高水位线》
   1. 一旦达到这个水位线，Full GC就会被触发，并卸载没用的类以及对应的类加载器
       - 然后高水位线将会被重置
       - 高水位线的值取决于GC后释放了多少元空间
   2. 如果释放的空间不多，那么在不超过MaxMetaspaceSize时，适当提高该值
       如果释放空间过多，则适当降低该值

- 如果初始化的高水位线设置过低，那么高水位频繁调整，发生多次Full GC
- 为了避免频发GC, 一般设置高水位线为一个相对较高的值
```

### 5.3 内部结构

- 存储虚拟机加载的类型信息，常量，静态变量，JIT编译后的代码缓存

```bash
# 1. 类型信息：           要加载的class, interface, enum, annotation，要存储以下类型信息
# 创建的对象中会包含：到该对象类型数据的指针，对象实例数据
# 所以类型信息的下面所有的信息可以通过反射获取到
- 类的全限定类名
- 类的直接父类的全限定类型（interface和Object类没有父类）
- 类的修饰符(public, abstract, final)
- 类直接接口的一个有序列表

# 2. 域(Field)信息:       保存类型的所有域信息
- 域名称，类型，修饰符

# 3. Method信息： 这些信息以模版的形式保存在方法区，方法运行的时候会入栈
- 方法名称，返回值，参数的数量和类型，修饰符
- 方法的字节码，操作数栈，局部变量表及大小
- 异常表

# 4. non-final 类变量
- 静态变量和类关联在一起，随着类的加载而加载，是类数据的一部分
- 类变量被类的所有实例共享，即使没有类实例也可以访问
#   static final全局变量
- 被声明为final的类变量的处理方式：在编译时候就已经会被分配

# 5. 常量池 
- 一个有效的字节码文件除了包含类的版本信息，字段，方法，接口等描述信息
- 常量池表(Constant Pool Table), 包含各种字面量，对类型，域和方法的符号引用
```

### 5.4 常量池

#### 字节码常量池

- 一个字节码会包含一个常量池表：对各种字面量，对类型，域，方法的符号引用
- 常量池，可以看作是一张表，虚拟机指令，根据这张常量表找到要执行的类名，方法名，参数类型，字面量等类型
```bash
# 为什么需要常量池：保证java字节码文件的完整性
- 一个java源文件中的类，接口编译后产生的字节码文件，需要数据支持，数据很大，不能直接存在字节码中
- 可以通过符号引用存储到常量池中，字节码中只需要保存指向运行时常量池的引用，然后后面动态链接到 《运行时常量池》
- 数据复用

# 常量池存储的数据类型
1. 数量值
2. 字符串值
3. 类引用
4. 字段引用
5. 方法引用
```
![image-20230225221439326](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230225221439326.png)

#### 运行时常量池

```bash
 # Runtime Constant Pool:  是方法区的一部分
- 1.1 常量池表是Class文件的一部分， 存放编译期生成的各种字面量和符号引用
- 1.2 加载类到虚拟机后的方法区后，就会创建对应的运行时常量
- 1.3 根据字节码常量池，真实的创建对象，并从引用变为真实创建对象

- 2. 每个加载进来的类或接口， 都会有一个单独的运行时常量池。 池中的数据项就和数组一样，通过索引来访问

- 3.1 运行时常量池包含： 编译期的数值字面量，运行期解析后获取到的方法或者字段引用
- 3.2 运行时常量池相比Class文件常量池： 具备动态性  
      String.intern(): 如果运行时常量池不包含，则创建一个放进去

- 4. 常量池如果超过了方法区提供的最大值，则OOM
```

### 5.5 方法区演进

- HotSpot才有永久代。 BEA JRockit, IBM J9等，是不存在永久代概念的
- 如何实现方法区是属于虚拟机实现细节，不受Java虚拟机规范管束，并不要求统一

```bash
# Hotspot 中方法区变化
- JDK1.6及以前：  有永久代，静态变量存放在永久代
- JDK1.7:        有永久代，但字符串常量池，静态变量移除，保存在堆中
- JDK1.8及以后：   无永久代，类型信息，字段，方法，常量保存在本地内存的元空间。  但字符串常量池，静态变量仍在堆

# 永久代为什么被元空间替代
- JRockit不包含永久代
- 为永久代设置空间大小是很难确定的：比如某些场景下，如果动态加载的类过多，则容易Full GC--->OOM
- 对永久代进行调优比较困难（元空间的话，就只需要依赖本地内存即可）
```

#### StringTable调整

- jdk7将StringTable放到了堆空间中
- jdk7以前，StringTable是放在永久代。永久代回收效率低，在full gc时候才会触发。full gc是老年代的空间不足，永久代不足才触发。这就导致StringTable回收效率不高
- 开发中会有大量的字符串被创建，回收效率低，导致永久代内存不足。放到堆中，能及时回收内存

## 6. 对象实例化

### 6.1 对象创建步骤

```bash
# 1.判断对象对应的类是否被加载，链接和初始化
虚拟机遇到一条new指令，首先去检查这个指令的参数能否在Metaspace的常量池中定位到一个类的符号引用，并且检查这个符号引用
代表的类是否已经被加载，解析和初始化，即判断类元信息是否存在

如果没有，通过双亲委派模式，使用当前类加载器以ClassLoader + 包名 + 类名为key进行查找对应的.class文件。如果没有找到文件
则抛出ClassNotFoundException, 如果找到，则进行类加载，并生成对应的Class类对象

# 2. 为对象分配内存
首先计算对象占用空间大小，接着在堆中划分一块内存给新对象。  如果实例成员变量是引用变量，仅分配引用变量空间即可，4个字节

如果内存规整， 则通过指针碰撞来进行堆内存的划分和分配
如果内存不规整，则虚拟机需要维护一个列表，然后通过空闲列表来进行分配

# 3. 处理并发安全问题
采用CAS失败重试，区域加锁保证更新的原子性
每个线程预先设置一块TLAB， 从而保证部分数据的快速分配

# 4. 初始化分配到的空间
- 所有属性设置默认值，保证对象实例字段在不赋值的时候可以直接使用，即默认初始化
- 默认初始化---

# 5. 设置对象的对象头

# 6. 执行init方法，调用构造器来进行初始化
从java程序角度来看，初始化才正式开始。初始化成员变量，执行实例化代码块，调用类的构造器，并把堆内对象地址赋值给引用变量
--->> 显示初始化--->代码块中初始化->构造器中初始化
```

### 6.2 对象内存布局

![image-20230228172514505](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230228172514505.png)

![image-20230228203756505](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230228203756505.png)

# Execution Engine

## 1. 编程语言

### 1.1 机器指令码

- 采用二进制0和1编码方式表示的指令，一开始就是直接用这种方式来编程
- 容易被计算机识别，但和人类语言差别太大，编程容易出错
- 编写的程序，cpu直接读取运行，因此执行速度最快
- 与cpu密切相关，不同种类的cpu所对应的机器指令不同

### 1.2 机器指令

- 机器指令码是由0和1组成的，不方便，因此发明了机器指令
- 把机器码中特定的0和1的序列，简化成对应的指令，如mov，inc，可读性提高

### 1.3 机制指令集

- 不同的硬件平台，各自支持的指令是有不同的。因此每个平台所支持的指令，称为对应平台的指令集
- x86指令集，ARM指令集

### 1.4 汇编语言

- 指令的可读性还是很差，引入汇编语言
- 用助计符代替机器指令的操作码，用地址符号和标号代替指令或操作数的地址
- 通过汇编语言转换成机器指令

### 1.5 高级语言

- java, c++, python, js等
- 计算机执行高级语言的程序时，需要把程序解释和编译成对应平台的机器指令码
- 该过程就叫解释程序或编译程序

## 2. JVM执行引擎

- 输入：字节码二进制流
- 输出：本地操作系统的机器指令码

![image-20230302112019567](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230302112019567.png)

### 2.1 字节码

- 字节码是一种中间状态(中间码)的二进制代码(文件)，它比机器指令码更加抽象，需要编译器转换后才能称为机制指令码
- 字节码和对应平台的执行引擎结合，从而实现跨平台

### 2.2 执行引擎：解释器

- 解释执行
- 将字节码中的文件内容翻译为对应平台的本地机器指令码执行
- 一条字节码指令被解释执行完成后，再根据PC寄存器中记录的下一条需要被执行的字节码指令执行解释操作
- JVM启动后，解释器先发挥作用，省去许多不必要的编译时间，随着程序运行时间的提升，JIT才会逐渐发挥作用，提高程序运行

### 2.3 执行引擎：JIT编译器

- 即时编译技术：JIT,  Just In Time
- 编译执行：直接编译成机器码
- 虚拟机为了提高执行效率，将二进制字节码，直接编译成机器指令码，进行缓存后再直接执行

```bash
# HotSpot VM
 - 目前市面上高性能虚拟机的代表之一
 - 采用解释器和即使编译器并存的架构
 - 相互协作，各自取长补短
 - 依靠热点探测功能，基于计数器的热点探测
 - HotSpot为每个方法建立两个不同类型的计数器
 
 # 方法调用计数器
 - 统计方法的调用次数，   server模式下默认1500次，client模式下是10000次
 - XX:ComileThreshold:  指定
 
 # 回边计数器
- 统计循环体执行的循环次数

# 热度衰减
- 如果不做任何设置，方法调用计数器统计的并不是方法被调用的绝对次数，而是一个相对的执行频率
- 一段时间内方法调用的次数，如果还不满足让它提交给即时编译器，则这个方法的调用计数器减少一半
- XX:-UseCounterDecay 关闭衰减， 这样只要系统运行时间足够长，绝大部份方法都会被JIT即时编译器变为机器指令
- XX: CounterHalfLifeTime:     设置半衰周期的时间
```

![image-20230302115349026](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230302115349026.png)

### 2.4 设置

```bash
# 默认情况: 混合模式： mixed mode
# 查看： java -version 
openjdk version "17.0.3" 2022-04-19 LTS
OpenJDK Runtime Environment Corretto-17.0.3.6.1 (build 17.0.3+6-LTS)
OpenJDK 64-Bit Server VM Corretto-17.0.3.6.1 (build 17.0.3+6-LTS, mixed mode, sharing)

# 纯解释器：   -Xint

# 纯编译器：   -Xcomp

# 混合：      -Xmixed
```

```bash
# Hotspot VM中包含两个JIT编译器

# Client Compiler:  C1编译器
- 对字节码进行简单和可靠的优化，耗时短。以便达到更快的编译速度
- 方法内联
- 去虚拟化
- 冗余消除

# Server Compiler: C2编译器， 默认
- 进行耗时较长的优化，以及激进优化。但优化后的代码执行效率更高
- 标量替换
- 栈上分配
- 同步消除，清除同步操作，通常指synchronized
```

# String

## 1. 基本特性

```bash
# String 字符串 
 String name = "erick";      String name = new String("erick");
 
# final修饰的，不可被继承

# 实现了Serializable接口， 表示字符串是支持序列化的

# 实现了Comparable接口， 表示字符串是可以比较大小的

# jdk8以前内部定义了final char[] value 来存储字符串数据，   jdk8以后该为byte[]
- 字符串是堆空间的主要组成部分之一
- 1个char占两个字节
      - 大多数char都是Lation-1字符，这种类型的char只需要一个字节就可以存储，造成了空间浪费
- 1个byte只占1个字节
```

## 2. 不可变性

```bash
# 以下情况，需要重写指定内存区域值，不能使用原有的value来进行赋值
 - 当对字符串重新赋值后               （重新创建新的数组）
 - 当对现有的字符串进行拼接操作时      （类似数组的不可变长度）
 - 调用String.replace()等方法时
 
# 通过字面量的方式(区别于new)给一个字符串赋值时，此时的字符串值声明在字符串常量池中 
```

## 3. 不可重复性

- 字符串常量池中不会存储相同内容的字符串
- String的String Pool是一个固定大小的HashTable
- jdk17中长度默认是65536，最小设置值是1009
- -XX:StringTableSize=100000 来调节 StringTable的长度， 过小JVM就会禁止
- 空间换时间，设置长度大点会相对比较好

![image-20230302191118957](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230302191118957.png)

- 如果相同的字符串进行加载，如果在常量池中已经定义好了，则不会继续定义

## 4. 字符串拼接

- 常量与常量的拼接结果，放在常量池，原理是编译期优化
- 常量池中不会存在相同内容的常量
- 只要其中有一个是变量，结果就是在堆中。变量拼接的原理是StringBuilder
- 如果拼接的结果调用intern(), 则主动的将常量池中还没有的字符串对象放入池中，并返回对象地址

### 4.1 案列1

```java
// 源码
@Test
public void test01() {
    String s1 = "a" + "b" + "c";   //编译期优化
    String s2 = "abc";             // abc字符串直接放在常量池中

    System.out.println(s1 == s2);
    System.out.println(s1.equals(s2));
}

// class文件
@Test
public void test01() {
    String s1 = "abc";
    String s2 = "abc";
    System.out.println(s1 == s2);
    System.out.println(s1.equals(s2));
}
```

### 4.2 案例2

- java8中，字符串的拼接是通过StringBuilder的append来进行的

![image-20230305110016449](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230305110016449.png)

- 如果左右还是常量，则就是编译期优化
- final修饰类，方法，基本数据类型，引用数据类型时，能使用final的时候建议用上

![image-20230305110131504](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230305110131504.png)

### 4.3 案列3

- 字符串拼接的时候 ，如果拼接的多了，尽可能使用StringBuilder，而不要用String的拼接
- 因为String的每次拼接，都是创建了一个新的StringBuilder对象

```java
@Test
public void test03() {
    long start01 = System.currentTimeMillis();
    String name = "a";
    for (int i = 0; i < 100000; i++) {
        name += "a";   // 每次循环都很会创建一个新的StringBuilder, 调用其toString的时候，也会创建了一个新的String
    }
    long end01 = System.currentTimeMillis();
    System.out.println(end01 - start01); // 916

   // 如果基本确定要添加的字符串长度，可以使用带参的构造器，这样就会避免内部的自动扩容机制
    long start02 = System.currentTimeMillis();
    StringBuilder info = new StringBuilder("a");
    for (int i = 0; i < 100000; i++) {
       info.append("a");
    }
    long end02 = System.currentTimeMillis();
    System.out.println(end02 - start02); // 1
}
```

## 5. Intern()

```bash
# 字符串调用 intern(), 尝试将这个字符串对象放入池子中
#      如果常量池中包含该字符串，则不会放入，返回已有的串池中的对象的地址
#      如果常量池中不包含该字符串，则会把对象的引用地址值复制一份，放入字符串池子中，并返回串池中的引用地址
public native String intern();

# 如何保证一个String一定指向的是常量池子中的字符串
- String s = "name";             # 字面量声明
- String info = xxx.intern();    # 不管xxx是何种方式的String, 只要调用了intern,则一定指向池子中的地址
```

### 面试题1

- new String("ab"); 创建了几个对象

```bash
- 在堆中创建了一个String对象
- 如果对应的“ab”不在常量池中，则会再创建一个"ab" 对象放在池子中，如果常量池中有了，则不再创建新的
```

### 面试题2

- String name = new String("erick") + new String("lucy");

```bash
 0 new #18 <java/lang/StringBuilder>                  # 对象1: 遇见+，就会先去创建一个StringBuilder对象
 3 dup
 4 invokespecial #20 <java/lang/StringBuilder.<init> : ()V>
 7 new #10 <java/lang/String>                         # 对象2: 第一个 String对象
10 dup
11 ldc #7 <erick>                                     # 对象3: ldc，代表常量池中的"erick"对象
13 invokespecial #15 <java/lang/String.<init> : (Ljava/lang/String;)V>
16 invokevirtual #21 <java/lang/StringBuilder.append : (Ljava/lang/String;)Ljava/lang/StringBuilder;>
19 new #10 <java/lang/String>                         # 对象4: 第二个 String对象
22 dup 
23 ldc #25 <lucy>                                     # 对象5: ldc，代表常量池中的"lucy"对象
25 invokespecial #15 <java/lang/String.<init> : (Ljava/lang/String;)V>
28 invokevirtual #21 <java/lang/StringBuilder.append : (Ljava/lang/String;)Ljava/lang/StringBuilder;>
31 invokevirtual #27 <java/lang/StringBuilder.toString : ()Ljava/lang/String;> 
                                       # 对象6: toString()的时候，会创建一个新的String, 但是ericklucy不进池子
34 astore_1
35 return
```

### 面试题3

- 常量池中存放的不一定是字符串字面量，也可能是一个地址值
- 为了节省空间，因为new String("sz")已经有了，并且不会往常量池中放，因此常量池中通过intern方法后，就会把地址值放过去
- s1==s2， 如果去掉intern，则二者就不会相等

![image-20230305155655129](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230305155655129.png)

面试题4

```java
String s1 = new String("s") + new String("z");
String s2 = "sz";
String s3 = s1.intern();

System.out.println(s1 == s2); // false
System.out.println(s2 == s3); // true
```

```java
String s1 = new String("s") + new String("z");
String s2 = s1.intern();

System.out.println(s2 == "sz"); // true
System.out.println(s1 == "sz"); // true
```

# GC入门

## 1. Garbage

### 1.1 定义

- 垃圾：在程序运行中，没有任何指针指向的对象，就是需要被回收的垃圾

```bash
# 清理内存
- 如果不及时对内存中垃圾进行清理，垃圾所占用内存空间会一致保留到应用程序结束，被保留的空间无法被其他对象使用，甚至导致内存溢出

# 碎片整理
- 除了释放没用的对象，同时可以清除内存里面的记录碎片。碎片整理将所用的堆内存移到堆的一端，以便JVM将整理出的内存分配给新对象

# 业务需求
- 应用程序逐渐变大，必须要有GC。 经常GC引发的STW会影响业务，因此不断对GC进行优化
```

### 1.2 JavaGC

```bash
# 自动内存管理
- 无需开发人员手动参与内存的分配和回收，降低内存泄漏和内存溢出的风险
- 自动内存管理机制，无需关注
- GC发生在堆和方法区上

# 缺点
- 自动内存管理是黑匣子，不能过分依赖
- 遇见OOM后，必须要根据错误异常一致快速定位问题和解决
- 出现内存溢出，内存泄漏后，当垃圾收集成为系统的瓶颈时，必须对自动化的技术实施监控和调优
```

## 2. 内存泄漏/内存溢出

### 2.1 内存溢出

- Out Of Memory: OOM

```bash
# OOM: 没有空闲内存，并且垃圾回收器回收完后，依然无法提供更多内存
- 应用程序占用的内存增长速度快，快速占用了JVM的所有内存
- 进行一次独占式的 Full GC 操作后，回收大量内存

# 原因
- Java虚拟机的堆内存设置不够
- 代码中创建了大量大对象，并且长时间不能被垃圾收集器收集，存在被引用
```

### 2.2 内存泄漏

- Memory Leak

```bash
# 严格意义
- 只有对象不会再被程序用到了，但是GC又不能回收他们，才叫做内存泄漏

# 实际情况
- 意外导致对象的生命周期变得很长甚至导致OOM，也可以叫做内存泄漏
- 比如类的static属性，所以应该尽可能少的使用static属性

# 危害
- 内存泄漏可能会逐步消耗可用内存空间，直到耗尽所有内存，最终出现 OutOfMemory

# example: 
# 1. 单例模式
- 单例的生命周期和应用程序是一样长的，如果单例程序中持有对外部对象的引用，那么这个外部对象是不可能被回收的，则会导致内存泄漏的产生

# 2. 变量的声明尽可能作用范围小一点

# 3. 一些提供close的资源未关闭导致内存泄漏
- 比如socket，io，datasource等的链接，如果不手动close， 则是不能回收的
```

## 3. System.gc()

```bash
# System.gc()   Runtime.getRuntime().gc()， 会触发  Full GC

- 同时对老年代和新生代进行回收，尝试释放被丢弃对象占用的内存
- 免责声明， 无法保证堆垃圾收集器的调用， 可以通过-XX:+PrintGC 查看
- 一般情况下，垃圾回收自动进行，无须手动触发
- 特殊情况下，可以显示调用来做基准测试
```

## 4. Stop The World

- Stop-The-World: 在GC发生过程中，产生应用程序会停顿；
- 停顿产生时整个应用程序线程都会被暂停，没有任何响应，有点卡死的感觉
- 被STW 中断的应用程序，会在完成GC 后恢复，频繁中断会让用户感觉多个卡顿，所以需要减少STW的产生

```bash
# 可达性分析算法中枚举根节点会导致所有java执行线程停顿
- 分析工作必须在一个能确保一致性的快照中进行
- 一致性指整个分析期间，整个执行系统看起来像是被冻结在某个时间点上
- 如果出现分析过程中对象引用关系还在不断变化，则分析结果的准确性无法保证

# 特点
1. STW和哪款GC无关，所有的GC 都有这个事件
2. 只能说垃圾回收器越来越优秀，回收效率越来越高，尽可能的缩短了STW
3. STW 是JVM在后台自动发起和自动完成的。在用户不可见的情况下，把用户正常的工作线程全部停下来
4. 开发中不要用System.gc(), 会导致STW的发生

# safe region
- 假如线程处在block或sleep状态，无法响应jvm的中断请求，所以额外需要safe region来进行
```

## 5. 安全点和安全区域

- 安全点 (Safepoint)：程序执行时，并非在所有地方都能停顿下来开始GC,只有在特定位置才可以
- 安全区域： 在一段代码片段中，对象的引用关系不会发生变化，在这个区域中的任何位置开始GC都是安全的
- 可以把Safe Region看作是扩展了的 SafePoint

```bash
# 数量
- 如果太少，则等待GC的时间太长，就会导致oom
- 如果太多，则频繁的gc，导致运行时的性能问题

# 安全点的选择
- 大部分指令的执行时间都非常短暂
- 通常会根据 “是否具有让程序长时间执行的特征”为标准
- 比如选择一些执行时间较长的指令作为  Safe Point, 比如方法调用，循环跳转，异常跳转
```

# 垃圾回收算法

- 哪些是垃圾？标记算法
- 怎么清除？清除算法

## 1. 标记算法

- 堆里存放着几乎所有的Java对象实例，GC前，首先需要区分出内存中存活对象和已死亡对象
- 只有被标记死亡的对象，GC才会在执行时，释放掉其所占用的内存空间
- 当一个对象已经不再被任何的存活对象继续引用时，判断为已经死亡

### 1.1 引用计数算法

#### 基本概念

- Reference Counting

```bash
# 每个对象保存一个整型的引用计数器属性，用于记录对象被引用的情况
- 对于一个对象A，只要有任何一个对象引用了A， 则A的引用计数器加1
- 当引用失效时，引用计数器减1
- 对象A的引用计数器的值为0，则表示对象A不可能再被使用，就可以回收

# 优点
- 实现简单，垃圾对象方便辨识
- 判定效率高，回收没有延迟

# 缺点：
- 需要单独的字段存储计数器，增加存储空间的开销
- 每次赋值需要更新计数器，伴随着加法和减法，增加了时间开销
- 致命缺陷： 无法处理循环引用的问题

# 最终java的垃圾回收器中没有使用该算法，因为很难处理循环引用的问题
# Python实际是上支持引用计数算法，对于循环引用通过手动解除或使用弱引用
```

#### 循环引用

```java
package com.nike.erick.d07;

// -XX:+PrintGCDetails
public class Demo02 {
    public static void main(String[] args) {
        ErickService service01 = new ErickService();
        ErickService service02 = new ErickService();

        service01.reference = a
        service02.reference = service01;

        service01 = null;
        service02 = null;

         System.gc();
    }
}

class ErickService {
    // 占位符，只是代表对象中的大的对象
    private int[] arr = new int[5 * 1024 * 1024];

    public Object reference;
}
```

![image-20230306201539659](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230306201539659.png)

```bash
# 调用GC前
Heap
 PSYoungGen      total 76288K, used 46203K [0x000000076ab00000, 0x0000000770000000, 0x00000007c0000000)
  eden space 65536K, 70% used [0x000000076ab00000,0x000000076d81ed58,0x000000076eb00000)
  from space 10752K, 0% used [0x000000076f580000,0x000000076f580000,0x0000000770000000)
  to   space 10752K, 0% used [0x000000076eb00000,0x000000076eb00000,0x000000076f580000)
 ParOldGen       total 175104K, used 0K [0x00000006c0000000, 0x00000006cab00000, 0x000000076ab00000)
  object space 175104K, 0% used [0x00000006c0000000,0x00000006c0000000,0x00000006cab00000)
 Metaspace       used 3181K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 352K, capacity 388K, committed 512K, reserved 1048576K
  
# 调用GC后
[GC (System.gc()) [PSYoungGen: 44892K->624K(76288K)] 44892K->624K(251392K), 0.0025427 secs] [Times: user=0.01 sys=0.00, real=0.01 secs] 
[Full GC (System.gc()) [PSYoungGen: 624K->0K(76288K)] [ParOldGen: 0K->387K(175104K)] 624K->387K(251392K), [Metaspace: 3173K->3173K(1056768K)], 0.0031131 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 
Heap
 PSYoungGen      total 76288K, used 1909K [0x000000076ab00000, 0x0000000770000000, 0x00000007c0000000)
  eden space 65536K, 2% used [0x000000076ab00000,0x000000076acdd440,0x000000076eb00000)
  from space 10752K, 0% used [0x000000076eb00000,0x000000076eb00000,0x000000076f580000)
  to   space 10752K, 0% used [0x000000076f580000,0x000000076f580000,0x0000000770000000)
 ParOldGen       total 175104K, used 387K [0x00000006c0000000, 0x00000006cab00000, 0x000000076ab00000)
  object space 175104K, 0% used [0x00000006c0000000,0x00000006c0060ca8,0x00000006cab00000)
 Metaspace       used 3181K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 352K, capacity 388K, committed 512K, reserved 1048576K
  
  # 可以明显看到，调用GC后，eden space 从 70% 回收到2%，证明Java没有使用引用计数器算法进行垃圾标记
```

### 1.2 可达性算法

```bash
# 概念
- 根搜索算法，追踪性垃圾收集器
- 简单，执行高效，能有效解决引用计数算法中的循环引用问题，防止内存泄漏的发生
- Java中选择的就是可达性算法

# GC Roots: 一组必须活跃的引用
- 以GC Roots为起始点，从上到下的方式， 搜索被根对象集合所链接的目标对象是否可达
- 内存中的存活对象都会被根对象集合直接或间接的链接， 搜索走过的路径 《引用链》
- 目标对象没有任何引用链相连，则是不可达的，对象已死
- 只有被根对象集合直接或间接链接的对象才是存活对象
```

![image-20230306204905907](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230306204905907.png)

#### 根元素

- 如果一个指针，指向了堆内存的对象，但是该指针又不存在堆中，那么该指针就是一个Root

```bash
# 虚拟机栈中：                           各个线程被调用的方法中使用的参数，局部变量中的引用等
# 本地方法栈：                           引用的对象
# 方法区中：                             类静态属性引用的对象
# 方法区中：                             常量引用的对象，比如String常量池
# 所有被同步锁synchronized持有的对象
# Java虚拟机中内部的引用：                 基本数据对应的Class对象，常驻的异常对象(NP, OOM)，系统类加载器
```

- 同时，如果考虑比如新生代的垃圾回收，那么其他堆空间的对象，也必须作为GC Root的一部分，从而判断
  新生代的对象是否是垃圾
- 如果要使用可达性分析算法来判断内存是否可回收，分析工作必须在一个能保障一致性的快照中进行
- 因此GC必须 Stop The World 的一个重要原因，枚举根节点时必须要停顿

## 2. 清除算法

### 2.1 标记-清除算法

- Mark-Sweep

```bash
# 1. STW
- 首先暂停用户线程

# 2. 标记
- Collector从引用根节点开始遍历，标记所有《被引用的对象》，不是垃圾对象。并在对象的Header中记录为可达对象

# 3. 清除
- Collector对堆内存从头到尾进行线性的遍历，如果发现某个对象在Header中没有标记为可达对象则将其回收
```

![image-20230306221806290](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230306221806290.png)

```bash
# 优点
- 实现比较简单

# 缺点
- 效率不算高
- 进行GC的时候，需要STW,导致用户体验差
- 清理出来的空闲内存是不连续的，产生内存碎片，可能部分大对象存不下。并且需要维护一个空闲列表

# 何为清除？延迟清理，空闲列表
- 并不是真的清空，而是把垃圾对象地址保存在空闲列表中
- 下次有新对象需要加载时，判断被垃圾对象占用的位置空间是否足够
- 如果足够，就清理并存放，如果不够则OOM
```

### 2.2 复制算法

-  Coping：将活着的内存空间分为两块， 每次只使用其中一块

```bash
1. 垃圾回收时，将正在使用的内存中的存活对象复制到未被使用的内存快中
2. 清除正在使用的内存快中的所有对象
3. 交换两个内存的角色，最后完成垃圾回收
```

![image-20230306223008966](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230306223008966.png)

```bash
# 优点
- 没有标记和清除过程，实现简单，运行高效
- 复制过去以后保证空间的连续性，不会出现 内存碎片 的问题

# 缺点
- 需要两倍的内存空间

# 局限性
- 如果系统中的存活对象很多，复制算法需要不断进行大量的复制操作
- 比较适合于朝生夕死的区域，比如Survivor的from和to区
```

### 2.3 标记-压缩算法

- Mark-Compact

![image-20230308140412687](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230308140412687.png)

```bash
# mark
- 从根节点开始，标记所有被引用的对象

# compact
- 将所有存活的对象压缩到内存的一端，按顺序排放

- 相当于mark-sweep后再进行一次碎片整理， 可以叫mark-sweep-compact算法

# 缺点
- 效率相比复制算法要低
- 移动对象时，如果对象被其他对象引用，则还需要调整引用的地址
- 移动过程中，需要全程暂停用户应用程序，即STW

# 优点
- 不会存在内存碎片问题
- 给新对象分配内存时，jvm只需要一个内存的起始地址即可(指针碰撞)，相比维护空闲列表更好
```

### 比较

|          | Mark-Sweep   | Mark-Compact   | Copying                   |
| -------- | ------------ | -------------- | ------------------------- |
| 速度     | 中等         | 最慢           | 最快                      |
| 空间开销 | 少(堆积碎片) | 少(不堆积碎片) | 存货对象的2倍(不堆积碎片) |
| 移动对象 | 否           | 是             | 是                        |

## 3. 收集思想

### 3.1 分代收集

- 每种算法都有优缺点，如何选择一个最合适的算法？
- Java堆分为新生代和老年代，不同对象生命周期不一样，可以采取不同的收集方式

```bash
# 分代收集(Generational Collecting)算法
- 几乎所有GC都是采用这个思想

# 年轻代
- 区域相对老年代小，对象生命周期短，存活率低，回收频繁
- 使用复制算法， 同时使用hotspot的两个survivor的设计来缓解内存问题

# 老年代
- 区域较大，对象生命周期长，存活率高，回收不会很频繁
- 可以使用标记-清除算法    或   标记-压缩算法
```

### 3.2 增量收集算法

- 垃圾回收过程中，程序处于STW状体，如果垃圾回收时间长，则会导致应用程序的长时间卡顿

```bash
# 优点： 减少STW的持续时间， 低延迟
- 让垃圾收集线程和应用线程交替执行
- 每次垃圾收集线程只收集一小片区域的内存空间，然后切换到应用程序线程
- 反复执行，直到垃圾收集完成

- 总体来说， 增量收集算法的基础仍然是 标记-清除和复制算法
- 但是允许垃圾收集线程以分阶段的方式完成标记，清理或复制工作

# 缺点：造成系统吞吐量的下降
- 垃圾回收，间断性执行了应用程序，所以减少了系统的停顿时间
- 但是线程切换和上下文转换的消耗，使得垃圾回收的总体成本上升，造成系统吞吐量的下降
```

### 3.3 分区算法

- Region

```bash
- 相同条件下，堆空间越大，一次GC时间越长，STW也就越大
- 将一块大的堆内存区域分割为多个小块，根据预期的STW，每次合理回收若干小区间
- 分区算法将整个堆空间划分为多个连续的不同的小区间 region
- 每个小区间都独立使用，独立回收

# 类似于微服务的理念
```

# Reference

- 当内存空间还足够时，则能保留在内存中。如果内存在GC后还是紧张，则可以抛弃的对象类型
- 上述不同的引用，均指的是在引用还存在的前提下，垃圾回收器是否会进行回收

## 1. 强引用

- Strong Reference

```bash
# 类似Object obj = new Object()类型的引用
- 强引用的对象是可触及的，可以通过指针访问的。只要指针没有断开，则永远不会被回收
- 强引用是内存泄漏的主要原因
```

## 2. 软引用

- Soft Reference
- 引用存在的情况下，内存不足才会回收

```bash
# 用来描述一些还有用，但并非必须的对象
- 软引用关联的对象，在系统将要发生oom的时候，会把软引用的对象，列入回收范围(Reference Queue)内进行
- 第一次回收不可触达的对象，第二次回收软引用的对象
- 如果这次回收还没有足够的内存，才会抛出内存溢出

# 作用：高速缓存
- 如果还有空闲内存，则可以暂时保留缓存
- 如果内存不足时，则再去清理
- 保证了既能使用JVM内部的缓存，也不会过分影响内存

# 实现
- java.lang.ref.SoftReference<Object>类
```

```java
package com.erick.day02;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.ref.SoftReference;

public class Demo03 {
    public static void main(String[] args) {
        Student student = new Student("erick", 11);
        SoftReference<Student> softReference = new SoftReference<>(student);

        // 断开强引用
        student = null;

        // 依然可以通过软引用获取到对象
        System.out.println("before" + softReference.get()); // 能取到值

        try {
            // 让JVM觉得资源紧张, OOM之前就会对软引用进行回收
            // 同时堆内存设置为10M: -Xms10M -Xmx10M
            // 新生代老年代默认1：2， 那么直接放在堆空间就会OOM
            int[] arr = new int[1024 * 1024 * 7];
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.out.println("after:" + softReference.get()); // null
        }
    }
}

@Data
@AllArgsConstructor
class Student {
    private String name;
    private int age;
}
```

## 3.  弱引用

- weak reference
- 发现即回收
- 描述那些非必须对象，只被弱引用关联的对象，只能生存到下一次GC前
- 垃圾回收线程的优先级比较低，因此弱引用的对象可能存活很长一段时间
- WeakHashMap：缓存一些数据，只有GC时候才会去清理

```bash
# 软引用 vs 弱引用
- 弱引用相比软引用，更容易被发现和回收，速度更快(因为软引用回收时候，需要判断内存够不够的算法)
- 都可以存放一些缓存数据，如果内存够，则就会使用jvm内部的缓存数据，加速系统。只在内存不够的时候才会回收
```

```java
package com.erick.day02;

import java.lang.ref.WeakReference;

public class Demo04 {
    public static void main(String[] args) {
        Object obj = new int[1024];

        WeakReference<Object> weakReference = new WeakReference<>(obj);

        // 将强引用断开
        obj = null;

        System.out.println("Before GC:" + weakReference.get());
        System.gc();
        System.out.println("After GC:" + weakReference.get());
    }
}
```

## 4. 虚引用

- Phantom Reference
- 是所有引用类型中最弱的一个
- 一个对象是否有虚引用的存在，完全不会决定对象的生命周期。如果一个对象仅仅持有虚引用，那么它和没有引用几乎是一样的，随时都可能被垃圾回收器回收
- 它不能单独使用，也无法通过虚引用来获取被引用的对象。当试图通过虚引用的get方法获取对象时，总是null
- 目的：跟踪垃圾回收过程。能在这个对象被收集器回收时收到一个系统通知

# GC基本概念

## 1.性能指标

```bash
# 不可能三角

# 吞吐量
- 运行用户代码的时间/(程序运行时间+内存回收的时间)

# 暂停时间
- 执行垃圾收集时，程序的工作线程被暂停的时间

# 内存占用
- Java堆区所占的内存大小
```

![image-20230311213339764](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230311213339764.png)

- 高吞吐量：就不能清理多次，每次清理时间较长，用户会感觉卡顿，但是系统的总可用时间较长
- 低延迟：每次少清理一些，多清理几次，用户体验好，系统的总可用时间较短
- 清理多次的话，涉及到线程的切换，吞吐量就会下降
- 高吞吐量和低延迟，二者是有矛盾的，只能解决其中一个

## 1.2 GC分类

- 不同垃圾收集器，是处理堆区的不同地方

![image-20230311220000947](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230311220000947.png)

```bash
# 1. 查看默认的垃圾回收器:    -XX:+PrintCommandLineFlags
# jdk8： 默认使用ParallelGC,  新生代使用ParNewGC，老年代自动使用Parallel Old GC
-XX:InitialHeapSize=268435456 -XX:MaxHeapSize=4294967296 -XX:+PrintCommandLineFlags -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseParallelGC 

# 2. 使用命令行: jinfo -flag 相关垃圾回收器参数 进程ID
jinfo -flag UseParallelGC 59431         # -XX:+UseParallelGC,       +代表使用了
jinfo -flag UseParallelOldGC 59431      # -XX:+UseParallelOldGC
jinfo -flag UseG1GC 59438               # -XX:-UseG1GC              -代表没使用
```

# GC分类

## 1. Serial/Serial Old

- 最基本，最早的垃圾收集器，用于单核CPU
- 新生代： Serial 收集器采用复制算法，串行回收，STW
- 老年代：Serial Old采用标记-压缩算法，串行回收，STW

![image-20230312185647667](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230312185647667.png)

```bash
# 单线程
- 只会使用一个CPU或者一条收集线程去完成收集工作，同时必须暂停其他所有的工作线程，直到收集结束

# 优点
- 简单高效： 对于限定单cpu的情况下，Serial 收集器没有线程交互的开销，收集效率高

# 指定： -XX:+UseSerialGC
- HotSpot虚拟机中，指定后新生代使用Serial GC, 老年代使用Serial Old GC
- 不管哪个版本都可以

# 缺点
- 现在已经基本不会使用了
- 特别是在web应用中，完全STW，给用户体验不好
```

## 2. ParNew

- ParNew收集器是Serial收集器的多线程版本
- 新生代的一种垃圾回收器
- 除了并行回收外，和Serial收集器基本没任何区别，并且使用了很多相同的代码
- ParNew是很多JVM运行在Server模式下的默认的新生代垃圾收集器

![image-20230312191415883](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230312191415883.png)

```bash
# 新生代
- 回收次数频繁，使用并行ParNew高效

# 老年代
- 回收次数少，使用串行方式节省资源，(CPU并行需要切换线程，串行可省却切换线程的资源)

# 开启: 
-  -XX:+UseParNewGC       # 指定年轻代， 不影响老年代
-  -XX:+ParallelThreads   # 限制线程数量，默认开启和CPU相同的线程数
```

## 3. Parallel Scavenge

- 吞吐量优先

```bash
# ParNew vs Parallel Scavenge
- 二者都是回收年轻代，采用复制算法，并行回收，STW

# Parallel Scavenge
# 1. 自适应调节策略
- Parallel Scavenge根据JVM运行情况，可自动调节内存分配，达到高吞吐量

# 2. 适应场景
- 后台运算，不需要与用户太多交互的场景
- 批量处理，订单处理，工资支付，科学计算等场景
- 如果需要与用户交互，则需要低延迟优先

# 3 jdk8
- 默认使用 Parallel Scavenge GC 和 Parallel Old GC
```

![image-20230312213942060](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230312213942060.png)

```bash
# 1. 开启，  设置其中一个，另一个也会被开启(互相激活)
- -XX:+UseParallelGC          # 手动指定年轻代使用Parallel Scavenge
- -XX:+UseParallelOldGC       # 手动指定老年代使用并行垃圾回收器

# 2. GC线程数量
- -XX:ParallelGCThreads       # 设置年轻代并行收集器的线程数，最好与CPU数量相等
# 默认情况
- 当CPU数量小于8个，则thread数量为cpu数量
- 当CPU数量大于8个，threads = 3+(5*cpu)/8

# 3. 吞吐量优先
- -XX:MaxGCPauseMillis        # 设置垃圾收集器最大STW时间，  ms
                              # JVM尽可能会把停顿时间控制在范围内
                              # JVM调整堆内存大小，堆小，STW短，但是频率就会提升，总体的吞吐量就会下降
                              # 谨慎使用
                          
- -XX:GCTimeRatio             # 垃圾收集时间/(总运行时间)
                              # 0-100， 默认值99， 垃圾回收时间不超过1%
                              # 与上个参数具有一定矛盾性
                              
- -XX:+UseAdaptiveSizePolicy  # 开启自使用策略
                              # 默认开启
                              # 年轻代大小，Eden和Survivor比例，晋升老年代的对象年龄参数会自动调整
                              # 达到在堆大小，吞吐量和停顿时间之间的平衡点
                              # 手动调优比较难时候，指定虚拟机的最大堆，目标吞吐量，停顿时间，使用自适应，让JVM自己调优
```

## 4.  CMS

- Concurrent-Mark-Sweep,老年代收集器
- HotSpot中，真正意义上的并发收集器，实现了让垃圾收集线程和用户线程同时工作
- 缩短STW: 重点是缩短STW，以达到更好的和用户的交互
- 标记-清除算法， 并行，STW
- ParNew/CMS,  Serial/CMS, 不能和Parallel Scavenge搭配

## 5. G1

- 区域化分代式
- 业务越来越复杂，没有GC就不能保证应用程序正常执行，经常造成的STW跟不上实际需求
- Jdk7 引入的 Garbage First， JDK1.7开始启用，是JDK9以后的默认垃圾回收器
- 官方定义：延迟可控的情况下，获得尽可能高的吞吐量

```bash
# 并行回收器，分而治之
- 堆内存分割为很多不相关的区域(Region), 物理上不连续。 使用不同的Region表示Eden, Survivor0, Survivor1，老年代
- 有计划的避免在整个Java堆中进行全区域的gc.G1跟踪各个Region里面垃圾堆积的价值大小(回收所获的空间大小及回收时间)
- 在后台维护一个优先列表，每次根据允许的收集时间，优先回收价值最大的Region
- 侧重于回收垃圾最大量的Region, 所以取名为Region First(垃圾优先)

# 使用场景： 新生代及老年代
- 针对配置多核CPU及大容量的机器，以极高概率满足GC停顿时间的同时，兼具高吞吐量的性能
- 面向服务端应用，针对具有大内存，多处理器的机器(在普通大小的堆中表现一般)
- 需要低GC延迟，又具有比较大的堆内存的应用程序

# 启用
- -XX:+UseG1GC
```

```bash
# 1. 优点
# 并行与并发
- 并行： G1在回收期间，拥有多个GC线程同时工作，有效利用多核计算能力。 此时用户STW
- 并发： G1可以和应用程序交互执行，部分GC工作和应用程序同时执行。不会在整个回收阶段完全阻塞应用程序
- 线程性质转换： G1可以采用应用线程来承担后台运行的GC工作，当JVM的GC线程处理速度慢时， jvm 会调用应用线程来帮助GC线程

# 分代收集
- G1依然属于分代型垃圾回收器，区分年轻代和老年代，年轻代依然有Eden和Survivor区。但从堆的角度来看，不会要求每个部分都是连续的，
  也不再坚持固定大小和固定数量

- 将堆空间分为若干个区域(Region)， Region包含了逻辑上的年轻代和老年代
- 兼顾年轻代和老年代

# 空间整合
- G1将内存划分为一个个的Region,内存回收是以region作为基本单位
- Region之间是复制算法，整体实际可看作是标记-压缩算法。两种都可以避免内存碎片，有助于程序长时间运行
- 分配大对象时不会因为无法找到连续内存而提前触发下一次GC, 尤其当Java堆非常大的时候， G1优势更加明显

# 可预测的停顿时间模型(soft-real-time)
- 分区回收，可以有效控制STW
- 跟踪region的回收价值，优先回收价值较大的region

# 2.缺点
- 为了垃圾收集产生的内存占用(Footprint), 程序运行的额外执行负载(Overload),相比CMS要高
- 小内存上，CMS比G1性能好，    大内存上，G1性能更好。    平衡点在6-8G
```

```bash
# 参数设置
- -XX:+UseG1GC                                  # 指定G1作为垃圾收集器
- -XX:G1HeapRegionSize                          # 设置每个region的大小，默认是堆内存的1/2000
                                                # 值是2的幂，范围是1MB-32MB,目标是根据值来，来划分约2048个region
                                                
- -XX:MaxGCPauseMills                           # 设置期望达到的最大GC停顿时间(会尽力实现，但不保证)， 默认200ms
- -XX:ParallelGCThread                          # 设置STW工作线程数的值，最大设置为8
- -XX:ConcGCThreads                             # 设置并发标记的线程数，最好设置为上面的 1/4 左右
- -XX:InitiatingHeapOccupancyPercent            # 设置触发GC周期的Java堆占用率阈值。默认是45，超过就会触发GC

#  简化JVM调优： 简单三步设置即可
- 开启G1垃圾收集器
- 设置堆的最大内存
- 设置最大的停顿时间
```

### 5.1 Region

- 将整个Java堆划分为大约2048个大小相同的独立Region
- 每个Region块大小根据堆空间的实际大小而定，整体是1MB-32MB之间，且为2的N次幂
- 所有的Region大小相同，且在JVM生命周期内不会被改变
- 一个region可能属于Eden, Survivor，Old内存区域，但是一个region在某个时间只能属于一个角色
- G1增加了一个新的内存区域，Humongous区域，主要用来存储大对象。如果超过1.5个region，就放在H

![image-20230313142143464](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230313142143464.png)

### 5.2 记忆集

- Remembered Set
- 一个Region中的对象，可能被其他任意Region中对象引用，判断对象存活时，是否需要扫描整个Java堆才能确保？这个问题在其他的分代收集器中也存在，不过在G1多region的环境下更加突出。如果回收新生代也必须扫描老年代，就会极大降低MinorGC效率

```bash
# 解决方案：为每个Region分配一个记忆集，扫描的时候只需要通过对应region的记忆集来扫描需要的region
- 每次 reference类型数据写操作时，都会产生一个Write Barrier暂时中断操作
- 检查将要写入的引用指向的对象是否和该Reference类型数据在不同的region(其他垃圾收集器：检查老年代对象是否引用了新生代对象)
- 如果不同，通过CardTable把相关引用信息记录到引用指向对象所在的Region对应的Remembered Set中
- 当进行垃圾回收时，在GC根节点的枚举范围假如Remembered Set, 就可以保证不进行全局扫描，也不会有遗漏
```

![image-20230313145912287](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230313145912287.png)

### 5.3 回收过程

![image-20230313143925922](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230313143925922.png)

#### 年轻代GC

- 只会回收Eden和Survivor区
- JVM启动时，G1准备好Eden区，程序在运行过程中不断创建对象到Eden区，当Eden区域空间耗尽时，G1启动一次年轻代垃圾回收
- 暂停所有应用程序线程，启动多线程执行年轻代回收
- G1创建回收集(Collection Set), 也就是被回收的内存分段的集合，包含Eden和Survivor
- 将年轻代区间存活对象移动到Survivor区间或者老年代

```bash
# 1. 扫描根
- 根是指static变量指向的对象，正在执行的方法调用链上的局部变量等。 根引用连同Rset记录的外部引用作为扫描存活镀锡爱过你的入口

# 2. 更新RSet
- 处理dirty card queue中的card，来更新Rset(跨region引用时，是更新dirty card queue，只有GC时，才会更新Rset)
- 处理完成后，RSet可以准确反映老年代对所在的内存分段中对象的引用

# 3. 处理RSet
- 识别被老年代对象所指向的EDen中的对象，这些对象被认为是存活的对象

# 4. 复制对象
- 对象树被遍历
- Eden存活对象被复制到Survivor区中共给你空的内存片段
- Survivor区域中存活对象如果年龄不到阈值，则年龄加1，达到阈值会被复制到Old区中
- 如果Survivor空间不够，则Eden的数据直接晋升老年代

# 5. 处理引用
- 处理软弱虚等其他引用。 最终Eden清空， GC停止，目标内存中的对象都是连续的
```

#### 并发标记过程

- 当堆内存使用达到阈值(默认45%)，开始老年代并发标记过程

```bash
# 1.初始标记阶段
- 标记从根节点直接可达的对象。 同时STW,并且会触发一次年轻代GC

# 2. 根区域扫描(Root Region Scan)
- 扫描Survivor区直接可达的老年代对象区域对象，并标记被引用的对象。该过程必须在young gc前完成

# 3. 并发标记(Concurrent Marking)
- 在整个堆中进行并发标记(和应用程序并发执行)
- 在并发标记阶段，若发现区域对象中的所有对象都是垃圾，则该区域会被立即回收
- 同时，在并发标记过程中，会计算每个区域中的对象活性(区域中存活对象的比例)

# 4. 再次标记(Remark)
- 由于应用程序持续进行，需要修正上一次的标记结果。 是STW的
- G1采用了比CMS更快的初始快照算法， snapshot-at-the-beginning(SATB)

# 5. 独占清理(cleanup, STW)
- 计算各个区域的存活对象和GC回收比例，并进行排序，识别可以混合回收的区域，为下阶段做铺垫， 是STW
- 并不会实际上去做垃圾的收集

# 6. 并发清理阶段
- 识别并清理完全空闲的区域
```

#### 混合回收

- 当越来越多的对象晋升到老年代，为了避免堆内存被耗尽， JVM会触发混合的垃圾收集器
- Mixed  GC
- 回收整个Young Region，一部分Old Region(不是整个Old Region)

```bash
# 部分回收
- 并发标记结束后，老年代中百分百为垃圾的内存分段已经被回收， 部分为垃圾的内存分段已经被计算出来
- 默认情况下，这些老年代的内存分段会分8次(可以通过 -XX:G1MixedGCCountTarrget设置)被回收

# 混合回收的回收集(Collection Set)，具体回收算法和年轻代一样
- 八分之一的老年代内存分段
- Eden 区内存分段
- Survivor区内存分段

# 老年代
- 老年代的内存分段默认为8次，G1会优先回收垃圾多的内存分段
- 垃圾占内存分段比例越高的，越会被先回收
- 阈值：决定内存分段是否会被回收  -XX:G1MixedGCLiveThresholdPercent, 默认为65%， 垃圾占内存分段比例达到65%才会被回收
```

#### Full GC

```bash
# G1的初衷是避免 Full GC的出现
- 如果上述方法不能正常工作， G1会停止应用程序的执行，使用单线程的内存回收算法进行垃圾回收
- 性能会非常差，STW较长

# 发生情况
- 堆内存太小，当G1在复制存活对象的时候没有空的内存分段可以用，就会回退到full gc, 可以通过增大内存解决
```

# GC总结

## 1. 总结

![image-20230313190547444](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230313190547444.png)

## 2. GC日志

```bash
# -verbose:gc                          输出gc日志
# gc信息： 分配失败，分配前的空间的和后面的，耗时
[GC (Allocation Failure)  1192260K->856K(1326080K), 0.0004063 secs]



# -XX:+PrintGC                        输出gc日志
[GC (Allocation Failure)  65372K->640K(251392K), 0.0026733 secs]

# -XX:+PrintGCDetails                 输出gc的详细日志
[GC (Allocation Failure) [PSYoungGen: 1386750K->64K(1396736K)] 1387186K->500K(1571840K), 0.0004135 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
Heap
 PSYoungGen      total 1396736K, used 417560K [0x000000076ab00000, 0x00000007c0000000, 0x00000007c0000000)
  eden space 1395712K, 29% used [0x000000076ab00000,0x00000007842b62c0,0x00000007bfe00000)
  from space 1024K, 6% used [0x00000007bff00000,0x00000007bff10000,0x00000007c0000000)
  to   space 1024K, 0% used [0x00000007bfe00000,0x00000007bfe00000,0x00000007bff00000)
 ParOldGen       total 175104K, used 436K [0x00000006c0000000, 0x00000006cab00000, 0x000000076ab00000)
  object space 175104K, 0% used [0x00000006c0000000,0x00000006c006d080,0x00000006cab00000)
 Metaspace       used 3184K, capacity 4496K, committed 4864K, reserved 1056768K
  class space    used 352K, capacity 388K, committed 512K, reserved 1048576K



# -XX:+PrintGCDetails -XX:+PrintGCDateStamps        带时间日期
2023-03-13T19:55:44.314-0800: [GC (Allocation Failure) [PSYoungGen: 65372K->640K(76288K)] 65372K->640K(251392K), 0.0024256 secs] [Times: user=0.01 sys=0.00, real=0.00 secs] 


# -XX:+PrintGCDetails -XX:+PrintGCTimeStamps       带时间日期: 虚拟机启动后的秒数
0.144: [GC (Allocation Failure) [PSYoungGen: 65372K->640K(76288K)] 65372K->640K(251392K), 0.0034842 secs] [Times: user=0.00 sys=0.00, real=0.01 secs] 


# -XX:+PrintHeapAtGC                  # 在进行gc的前后打印出堆中的信息
- -Xloggc: ../logs/gc.log             # 日志文件的输出路径



```

# 类加载

- 基本数据类型是JVM预先定义好的，引用数据类型则需要进行类的加载

## 1. 加载

- 将Java类的字节码文件加载到JVM内存中，并在内存中构建出Java类的原型-类模版对象

```bash
# 类模版对象
- Java类在JVM中内存的一个快照
- JVM从字节码文件中解析出的常量池，类字段，类方法信息存储到类模版中， 在运行期间获取Java类的任意信息
- 反射：基于这一基础，来调用java方法和java字段

# 加载流程
- 通过类的全名，获取类的二进制数据流
- 解析类的二进制数据流为方法区内的数据结构(jdk1.8前是在永久代，jdk1.8及以后在元空间)
- 创建java.lang.Class类的实例，表示该类型。作为方法区这个类的各种数据的访问入口(存放在堆中)
```

## 2. 类加载器

- 类加载器子系统：只负责从文件系统或网络中加载Class文件，class文件在开头有特定的文件标识

```bash
- 加载的类信息，存储在方法区的内存空间
 1. 类Klass信息
 2. 存放运行时常量的信息(字符串字面量和数字常量)
 
# class file
- 存在于本地硬盘上，类似设计师画在纸上的模版
- 只负责class文件的加载，至于是否可以运行，交给Execution Engine决定
- 模版在执行时，会加载到JVM中，根据模版，创造出n个一摸一样的实例
- class file通过二进制流的方式加载到JVM中，称为DNA元数据模版，存放在方法区
- .class --> jvm -->元数据模版： 类装载器就是快递员的角色
```

![image-20221201153940070](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221201153940070.png)

# 加载过程

## 1. Loading

### 1.1 文件来源

```bash
- 从本地文件系统直接加载
- 通过网络获取
- 从ZIP包，JAR包，WAR包获取
- 运行时生成：动态代理
- 从其他文件生成：jsp
- 从加密文件中获取，防止Class文件被反编译的保护，在读取时候需要解密
```

### 1.2 过程

- 通过类的全限定类名，获取该类的二进制字节流
- 方法区：将字节流所代表的静态存储结构转换为的运行时数据结构(元数据)
- 堆内存：生成代表该类的java.lang.Class对象，作为方法区这个类的各种数据的访问入口

```java
package com.erick.service;

import java.lang.reflect.Field;

public class Test01 {
    public static void main(String[] args) {
        /*方式一：通过类对象*/
        Class<ErickStudent> studentClass = ErickStudent.class;

        /*方式二：通过类全名获取*/
        try {
            Class clazz = Class.forName("com.erick.service.ErickStudent");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        /*方式三：通过具体实例获取*/
        ErickStudent student = new ErickStudent();

        // 获取public属性
        Field[] fields = studentClass.getFields();
        for (Field field : fields) {
            // 获取属性名
            System.out.println(field.getName());
        }
    }
}

class ErickStudent {
    public String name;
    public String address;
}
```

## 2.Linking

### 2.1 Verification

- 确保class文件的字节流中包含信息符合当前虚拟机要求，保证被加载类的正确性，不会危害虚拟机自身安全
- 文件格式校验，语义检查，字节码校验，符号引用校验
- Verification虽然拖慢了加载速度，但是避免了在字节码运行时还需要进行各种检查

```bash
# 格式校验
- 魔数：    CA FE BA BE: 通过《二进制文件查看器》，发现所有的 .class文件都会以这个开头（魔数）
- 版本号：  主版本和副版本号是否在当前JVM的支持范围
- 数据：    每一个项是否都拥有正确的长度

# 语义检查：  进行字节码的检查
- 是否所有的类都有父类的存在(Java中除了Object,其他类都应该有父类)
- 是否一些被定义为final的方法或类被重写或继承了
- 非抽象类是否实现了所有的抽象方法和接口方法

# 字节码验证： 最为复杂的一个过程。通过对字节码流的分析，判断字节码是否可以被正确执行
- 在字节码的执行过程中，是否会跳转到一条不存在的指令
- 函数的调用是否传递了正确类型的参数
- 变量的赋值是否给了正确的数据类型

# 符号引用验证： 在resolve才会去做
- Class文件在其常量池中通过字符串记录自己将要使用的其他类或方法
- 在验证阶段，JVM检查这些类或方法确实是存在的，并且当前类是有权限访问这些数据
- 如果一个需要使用的类无法在系统中找到，则会抛出NoClassDefFoundError
- 如果一个方法无法找到，则会抛出NoSuchMethodError
```

### 2.2 Preparation

```bash
# 为类的静态变量分配内存(方法区-元空间)，并将其初始化为默认值
- static修饰的field：         为其分配内存(方法区)，并且设置默认初始值，即零值

# static + final修饰的
- 在准备阶段就会进行显示的赋值(方法区)

# String如果是用字面量的方式定义一个字符串静态常量，也是在准备阶段显示赋值

# 实例变量 vs 类变量
- 不会为实例变量分配初始化
- 类成员变量会分配在方法区中
- 实例变量会随着对象一起分配到Java堆中
```

### 2.3 Resolve

- 常量池的符号引用转换为直接引用
- 比如一个.class文件，在准备的时候，需要对其父类也要做一些初始化的准备工作 

## 3. Initialization

- 类构造器: \<clinit>方法
- 类的field的显示初始化
- 到这个环节，才会真正去执行类的Java代码

### 3.1 \<clinit>方法

```bash
# 包含： 类成员变量的赋值动作  静态代码块中的语句
- 不需要定义，是javac编译器自动收集类中的信息，并进行合并
- 执行顺序：从源文件由上而下
- 如果不包含上面两种数据，则不会有<clinit>()
- 如果有父类，JVM保证子类的<clinit>()在执行前，父类的<clinit>先执行
```

![](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221201204614499.png)

### 3.2 不包含\<clint>场景

```bash
# 非静态的字段，不管是否进行显示赋值                  
- public int num=1;

# 静态的字段，没有显示赋值 
- public static int num;

# static+final修饰的字段
     # 基本数据类型
     - 是在准备阶段就会显示赋值       public static final num = 1;
     - 是在<clinit>中赋值           public statifc final num = new Random().nextInt(10);
     
     # 引用数据类型
     - 在<clint>赋值： public static final Interger a = Integer.valueOf(100);
                      public static  Interger a = Integer.valueOf(100)
                      
     # String数据类型     
     - 在准备阶段赋值(字面量)        public static final String str = "hello";    
     - 在<clint>                  public static final String str = new String("hello");
```

### 3.3  线程安全

```bash
# 类加载天然线程安全
- <clinit>是用来加载类成员变量的，因此只需要加载一次，并在方法区进行缓存
- JVM确保一个类的<clinit>()在多线程时被同步加锁

# 尽可能少用static
- 因为<clint>是带锁线程安全的，所以如果一个类中static很多，则会造成其他多个线程阻塞
- 如果多个线程都需要用到这个类，则都会触发加载这个类，从而包含 initialization的加锁阻塞
```

```java
package com.erick.service;

public class Test03 {
    public static void main(String[] args) {
        new Thread(() -> {
            Erick erick = new Erick();
        }).start();

        new Thread(() -> {
            Erick erick = new Erick();
        }).start();
    }
}

class Erick {
    static {
        if (true) {
            System.out.println("开始初始化该类");
            while (true){

            }
        }
    }
}
```

### 3.4 类的构造器\<init>方法

```bash
类的构造器和<clinit>()不是一回事，类的构造器是JVM视角下的<init>方法
```

- 无参构造器

![image-20221201205743770](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221201205743770.png)

- 有参构造器

![image-20221201210013874](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221201210013874.png)

# 类的使用

## 1. 主动使用

- 会导致类的初始化，去调用类对应的\<clinit>

### 1.1 创建类的实例

- new关键字，反射，克隆，反序列化

### 1.2 调用类的静态方法

- 即使用了字节码invokestatic指令

```java
package com.erick.day01;

import org.junit.Test;

public class Demo06 {

    @Test
    public void test() {
        OrderService.say();
    }

}

class OrderService {
    static {
        System.out.println("order service is loaded");
    }

    public static void say() {
        System.out.println("hello");
    }
}
```

### 1.3 使用类，接口的静态字段

- 使用getstatic或putstatic指令(访问变量，赋值变量)

```java
package com.erick.day01;

import org.junit.Test;

import java.util.Random;

public class Demo06 {

    @Test
    public void test() {
        System.out.println(OrderService.str01);
    }

}

class OrderService {
    static {
        System.out.println("order service is loaded");
    }

    public static int num = 0;  // 调用会导致初始化

    public static final int num01 = 1; // 不会导致初始化，因为在编译期就已经确定了

    public static final int num02 = new Random().nextInt(10); // 会导致初始化

    public static final String str = "hello";  // 不会初始化

    public static final String str01 = String.valueOf("hello");  // 会初始化
}
```

### 1.4 使用java.lang.reflect包中的方法反射类的方法

- Class.forName("com.citi.erick.OrderService")

### 1.5 子类触发父类

- 当初始化子类时，如果发现其父类还没有进行过初始化，则需要先触发其父类的初始化
- 因为需要使用子类，子类可能要使用到父类的一些属性，所以一定要确保父类也执行了初始化

```bash
# 接口
- 当JVM初始化一个类时，并不会先初始化它所实现的接口
- 在初始化一个接口时，并不会先初始化它的父接口
```

### 1.6 接口包含default方法

- 如果一个接口定义了default方法，那么直接实现或者间接实现该接口的类的初始化，会导致接口先被初始化

```java
package com.erick.day01;

import org.junit.Test;

public class Demo07 {

    @Test
    public void test() {
        Man.drinkBeer();
    }

}

class Man implements People {
    public static void drinkBeer() {
        System.out.println("man drink beer");
    }
}

interface People {

    Thread thead = new Thread() {
        {
            System.out.println("People interface is loaded");
        }
    };

    default void say() {
        System.out.println("say hell");
    }
}
```

## 2. 被动使用

- 类的被动使用，即不会进行类的初始化操作，即不会调用\<clinit>
- 没有进行初始化，不意味着没有加载

### 2.1 静态字段

- 当访问一个静态字段时候，只有真正声明这个字段的类才会被初始化
- 当通过子类引用父类的静态变量，不会导致子类初始化

```java
package com.erick.day01;

import org.junit.Test;

public class Demo08 {

    @Test
    public void test() {
        System.out.println(Son.parentName);
    }

}


class Son extends Parent {
    static {
        System.out.println("son is loaded");
    }

}

class Parent {

    static {
        System.out.println("parent is loaded");
    }

    public static String parentName = "parent";
}
```

### 2.2 通过数组定义类引用

- 通过数组定义类引用，不会触发此类的初始化
- 只是开辟了空间，还没实际进行对象的分配

```java
package com.erick.day01;

import org.junit.Test;

public class Demo09 {

    @Test
    public void test() {
        Service[] services = new Service[10];
    }
}

class Service {
    static {
        System.out.println("service is loaded");
    }
}
```

### 2.3 常量触发

- 获取常量不会触发此类或接口的初始化，因为常量在链接阶段就已经被显示赋值了

### 2.4 loadClass

- 调用ClassLoader类的loadClass方法加载一个类时， 不是类的主动使用，不会导致类的初始化

```java
package com.erick.day01;

import org.junit.Test;

public class Demo09 {

    @Test
    public void test() throws ClassNotFoundException {
        Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass("com.erick.day01.Service");
    }
}

class Service {
    static {
        System.out.println("service is loaded");
    }
}
```

# 类的卸载

## 1. 类实例，类Class对象，类加载器

- 一个类加载器可以加载很多个类，并会保存当前类加载器加载了哪些类
- 一个类何时结束生命周期，取决于它的Class对象何时结束生命周期

![image-20230325134605672](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230325134605672.png)

## 2. 何时回收

```bash
# 类卸载的三个条件
- 该类所有的实例都已经被回收
- 加载该类的类加载器已经被回收。 基本很难
- 该类对应的java.lang.Class对象没有在任何地方被引用，无法在任何地方通过反射访问该类的方法

# 满足上面三个条件后，仍然很难被收， 类基本上很少被卸载
- 方法区的GC本身就比较少 
```

# 类加载器

- 所有的Class都是由ClassLoader进行加载的
- ClassLoader负责将各种方式获取的Class二进制数据加载到JVM内部，转换为一个与目标类对应的java.lang.Class对象实例
- ClassLoader只负责类的加载，JVM内部进行链接，初始化， Excution Engine负责类的代码是否可以执行

## 1. 分类

### 1.1 Bootstrap Class Loader

- 引导类类加载器

```bash
# 实现
- 用c，c++实现的，没有父类加载器，不继承ClassLoader, 嵌套在JVM中

# 作用
- 加载系统的核心类库， JAVA_HOME/jre/lib/rt.jar 或 sun.boot.class.path路径下的内容
- 处于安全考虑，只加载包名为java, javax, sun开头的类
- 比如JVM自身的类(String)
- 加载其他的类加载器(类加载器也是java对象)，同时指定为他们的父类加载器
- 一个jvm只有一个，打印时候为null
```

```java
    @Test
    public void test02() {
        URL[] urLs = Launcher.getBootstrapClassPath().getURLs();
        for (URL element : urLs) {
            System.out.println(element.toExternalForm());
        }
    }

/*
file:/Library/Java/JavaVirtualMachines/jdk1.8.0_361.jdk/Contents/Home/jre/lib/resources.jar
file:/Library/Java/JavaVirtualMachines/jdk1.8.0_361.jdk/Contents/Home/jre/lib/rt.jar
file:/Library/Java/JavaVirtualMachines/jdk1.8.0_361.jdk/Contents/Home/jre/lib/jsse.jar
file:/Library/Java/JavaVirtualMachines/jdk1.8.0_361.jdk/Contents/Home/jre/lib/jce.jar
file:/Library/Java/JavaVirtualMachines/jdk1.8.0_361.jdk/Contents/Home/jre/lib/charsets.jar
file:/Library/Java/JavaVirtualMachines/jdk1.8.0_361.jdk/Contents/Home/jre/lib/jfr.jar
file:/Library/Java/JavaVirtualMachines/jdk1.8.0_361.jdk/Contents/Home/jre/classes
*/
```

### 1.2 User-Defined ClassLoader

```bash
- 派生于抽象类ClassLoader的类加载器
- 使用java语言实现，但并不是等同于程序猿自定义的类加载器
- 父类加载器为Bootstrap Class Loader(父类定义：儿子类加载时由父类加载器加载，通过getParent)
```

#### Extension ClassLoader

```bash
- 从java指定目录下加载jar包(不是核心库，用户文件如果放在该目录下，也会被该类加载)
- 从java.ext.dirs系统属性所指定的目录中加载类库，或从jdk的安装目录 jre/lib/ext子目录加载
- java语言编写，sun.misc.Launcher$ExtClassLoader@533ddba
- 派生于ClassLoader类
- 父类加载器为bootstrap classloader
```

#### AppClassLoader

```bash
# 作用
- 程序中默认的类加载器(用户写的类)
- 负载加载环境变量classpath和系统属性

# 其他
- ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader()获取到类加载器
- sun.misc.Launcher$AppClassLoader@18b4aac2
- 一个JVM中只会有一个
- 父类为扩展类加载器  
```

### 1.3 用户自定义类加载器

- 日常开发中，基本都是通过上面三种类加载器相互配合执行，必要时可以自定义类加载器

```bash
- 隔离加载类：不同框架使用的同名，但是不同的源码的类
- 扩展加载源：比如从磁盘中读取一个类的class文件
- 修改类的加载方式
- 防止源码泄漏：比如编译时候加密了，那么加载的时候就需要解密
```

```java
package com.erick.service;

public class ErickClassLoader extends ClassLoader{

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 1. 读取字节码字节流
        // 2.  字节流转换为Class对象
        return super.findClass(name);
    }
}
```



## 2. 获取

### 2.1 相同Class对象

```bash
# 1. JVM两个Class对象相同的判断标准
- 类的全限定类名是一样的
- 加载这个类的ClassLoader必须是同一个(哪怕在同一个JVM中，同一个Class文件，也是可以被不同类加载器加载的)

# 2. 类加载器的引用
- JVM必须知道一个类型是由bootstrap class loader还是user defined classloader来加载的
- 如果一个类型是由user defined classloader来加载，jvm会将这个类加载器的一个引用作为类型信息的一部分保存在方法区
- 解析一个类型到另一个类型的引用时，jvm要确保这两个类型的类加载器是相同的
```

### 2.2 ClassLoader获取

```bash
# 数组的类加载器
- 数组的Class对象，是Java运行时JVM根据需要创建，对应的ClassLoader是对应数组元素的类加载器
- 数组元素：引用类型，根据对应的引用类型的ClassLoader
- 数组元素：基本数据类型， 不需要类加载器，因为基本数据类型已经由JVM预先定义了
```

```java
package com.erick.day03;

public class Demo01 {
    public static void main(String[] args) throws ClassNotFoundException {

        ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
        System.out.println(appClassLoader); // sun.misc.Launcher$AppClassLoader@18b4aac2

        ClassLoader extClassLoader = appClassLoader.getParent();
        System.out.println(extClassLoader); // sun.misc.Launcher$ExtClassLoader@6d6f6e28

        ClassLoader bootstrapClassLoader = extClassLoader.getParent();
        System.out.println(bootstrapClassLoader); // null

        ClassLoader appClassLoader01 = Class.forName("com.erick.day03.Demo01").getClassLoader();
        System.out.println(appClassLoader01);

        ClassLoader bootstrapClassLoader01 = Class.forName("java.lang.String").getClassLoader();
        System.out.println(bootstrapClassLoader01); // sun.misc.Launcher$AppClassLoader@18b4aac2

        // 
        String[] arr = new String[10];
        ClassLoader classLoader = arr.getClass().getClassLoader();
        System.out.println(classLoader); // null
    }
}
```

# 双亲委派机制

## 3.1 基本原理

- JVM对Class文件采用按需加载的方式，懒加载，将其class文件加载到内存生成class对象
- 加载时使用双亲委派机制，即把请求交给父类处理（任务委派机制）

![image-20230327113338001](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230327113338001.png)

## 3.2 优点 

```bash
# 优点
# 1. 避免类的重复加载 
- 确保一个类是全局唯一性的。   因为判断一个Class是不是唯一，需要通过类的全限定类名和对应的类加载器

# 2. 沙箱安全保护机制
- 保护程序安全，防止核心API被随意修改
```

## 3.3 缺点

- 通常情况下，bootstrapclassloader的类加载核心类库，包含一些重要的系统接口。而在appClassLoader中去加载实现该接口的一些应用类。应用类访问系统类没什么问题，但是系统类访问应用类就会出现问题
- 比如在系统类中提供了一个接口，该接口需要在应用类中实现。该接口还绑定一个工厂方法，用于创建该接口的实例。接口和工厂方法都是启动类加载器中， 则出现工厂类无法创建由引用类加载器加载的应用实例的问题

## 3.4 打破双亲委派

- 基于以上问题，JVM规范中，并没有明确要求类加载器的加载机制一定要使用双亲委派模型，只是建议使用这种方式而已
- Thread.currentContextClassLoader(), 上层的实现类如果要加载下面的类，则通过这个获取到底层的类加载器来加载
- 第三次：由于用户对程序动态性的追求导致：代码热替换，模块热部署



# Class文件

## 1. javac

- HotSpot并没有强制要求前端编译器使用javac
- 全量编译
- IDEA默认使用javac来进行编译

```bash
# 前端编译器： javac 路径
/Library/Java/JavaVirtualMachines/jdk1.8.0_361.jdk/Contents/Home/bin/javac

/Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home/bin/javac

/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/javac

# 其他类型编译器
- AspectJ编译器  ajc
- Eclipse的EJC编译器

# 效果
- 前端编译器并不会直接涉及编译优化方面的技术，基本几种编译的Class文件都差不多，具体的编译优化细节交给HotSpot的JIT
```

## 2. 基本定义

### 2.1 Class文件

- 源代码经过编译器编译后，生成一个或者多个字节码文件(内部类，其他类)
- 字节码是一种二进制类文件，内容是JVM的指令

### 2.2 字节码指令

```bash
# 操作码(opcode)
- 一个字节长度的，代表着某种特定操作含义的操作码

# 操作数(operand)
- 零至多个代表此操作所需参数的操作数

# 许多指令不包含操作数，只有一个操作码
```

### 2.3 解读字节码方式

```bash
#  方式一: javap                 
- javap -v Demo01.class 
- javap -v Demo01.class >demo.txt      # 导出到txt文件中

/Library/Java/JavaVirtualMachines/jdk1.8.0_361.jdk/Contents/Home/bin/javap

/Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home/bin/javap

/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/javap

# 方式二：使用记事本： Hex Fiend来进行查看

# 方式三： IDEA中jclasslib插件 / jclasslib客户端
https://github.com/ingokegel/jclasslib/releases
```

## 3. Class文件结构

- 任何一个Class文件都对应着唯一一个类或接口的定义信息
- Class文件可能通过磁盘，网络，反射等来传输，是一组以8个字节为基本单位的二进制流
- Class文件结构不像XML等具有分割符。所以其中数据项，无论是字节顺序还是数量，都严格限定，目的为了缩小Class文件大小

### 3.1 魔数

- Magic Number: 魔数，每个Class文件开头的4个字节的无符号整数
- 作用：确定这个文件是否为一个被虚拟机接受的有效合法的Class文件。使用魔数而不是扩展名来识别，主要是基于安全方法的考虑，因为文件名可以随意改动
- 基本其他的文件格式，也都会有类似的魔数来进行格式校验 
- 固定值： CA FE BA BE, 如果一个Class文件不以  CA FE BA BE开始，则虚拟机在校验的时候就会直接抛出错

### 3.2 版本号

- Class文件的版本号，4个字节， 第5和第6个字节代表的是编译的副版本号(minor_version)，7和8代表的是编译的主版本号
- 主版本和副版本共同构成了class文件的格式版本号
- 比如1.8的的jdk，对应的class 版本号就是 00 00 00 34(前面是副版本，后面是16进制的主版本，对应着十进制的52)
- jdk的版本从45开始，后面每个大版本的版本号，自动向上加1

```bash
- 不同版本的java编译器编译的class文件对以的版本是不同的
- 高版本JVM可以执行低版本的编译的结果
- 低版本的JVM不可以运行高版本的编译         
# java.lang.UnsupportedClassVersionError:    Unsopported major.minro version 52.0
```

| 主版本（十进制） | 副版本(10进制) | 编译器版本 |
| ---------------- | -------------- | ---------- |
| 45               | 3              | 1.1        |
| 46               | 0              | 1.2        |
| 47               | 0              | 1.3        |
| 48               | 0              | 1.4        |
| 49               | 0              | 1.5        |
| 50               | 0              | 1.6        |
| 51               | 0              | 1.7        |
| 52               | 0              | 1.8        |
| 53               | 0              | 1.9        |
| 54               | 0              | 1.10       |
| 55               | 0              | 1.11       |

### 3.3 常量池

- Class文件中内容最丰富的区域，对于Class文件中的字段和方法解析至关重要
- 常量池是Class文件的基石

```bash
# 常量池计数器
- constant_pool_count              # 2个字节
- 常量池的数量不固定，需要2个字节来表示常量池容量计数值
- constant_pool_count=1， 表示常量池中没有常量项，比常量池中实际存放个数多1

# 常量池表: 存放字面量(Literal)和符号引用(Symbol References)
# 存放字面量(Literal)
- 文本字符串            String name = "erick";
- 声明为final的常量值    final int age = 10;

# 符号引用(Symbol References)
- 类和接口的全限定类名      com/nike/erick;   # 加;是为了不和其他的字段混淆
- 字段的名称和描述符       
- 方法的名称和描述符
     # 名称：即为方法或者字段的名称
     # 描述符号： 字段的数据类型，方法的参数列表(数量，类型，顺序)和返回值
```

| 标注符 | 含义                              |
| ------ | --------------------------------- |
| B      | 基本数据类型byte                  |
| C      | 基本数据类型char                  |
| D      | 基本数据类型double                |
| F      | 基本数据类型float                 |
| I      | 基本数据类型int                   |
| J      | 基本数据类型long                  |
| S      | 基本数据类型short                 |
| Z      | 基本数据类型boolean               |
| V      | Void类型                          |
| L      | 对象类型， 比如Ljava/lang/object  |
| [      | 数组类型， 比如 double[ ] 就是 [D |

## 4.javap

### 4.1 javac

```bash
# javac xxx.java
- 编译后的class文件不会带局部变量表的信息

# javac -g xxx.java
- 编译后的class文件会带局部变量表的信息

# IDEA编译的java文件会自动带 局部变量表的信息

# 结果检查: 反解析得到对应的字节码文件
- javap -v xxx.class >xxx.txt
```

### 4.2 javap

```bash
# javap -version Demo.class >1.txt
- 版本信息，显示当前javap所在jdk的版本信息，不是class在哪个jdk下生成的

17.0.6
Compiled from "Demo.java"
public class com.erick.day02.Demo {
  public com.erick.day02.Demo();
  public int add();
}
```

```bash
# javap -public Demo.class >1.txt
# javap -protected Demo.class >1.txt
# javap -p Demo.class >1.txt       javap -private Demo.class >1.txt    
# 默认权限：默认default
- 仅显示指定权限以上的类和成员

Compiled from "Demo.java"
public class com.erick.day02.Demo {
  public com.erick.day02.Demo();
  public int add();
}
```

```bash
# javap -sysinfo Demo.class >1.txt
- 显示类的系统信息(路径，大小，日期，MD5散列，源文件名)

Classfile /Users/shuzhan/Documents/workspace-java/jvm/src/main/java/com/erick/day02/Demo.class
  Last modified Mar 20, 2023; size 460 bytes
  SHA-256 checksum 035c3d99d7a5af2217db2dff4893868876b1678e47aa8f7b3efaf2ae8c128b6b
  Compiled from "Demo.java"
public class com.erick.day02.Demo {
  protected java.lang.String name;
  public com.erick.day02.Demo();
  public int add();
}
```

```bash
# javap -constants Demo.class >1.txt
- 显示静态的最终常量

Compiled from "Demo.java"
public class com.erick.day02.Demo {
  public static final int num = 1;
  protected java.lang.String name;
  public com.erick.day02.Demo();
  public static int add();
}
```

```bash
# javap -s Demo.class >1.txt

Compiled from "Demo.java"
public class com.erick.day02.Demo {
  public static final int num;
    descriptor: I
  protected java.lang.String name;
    descriptor: Ljava/lang/String;
  public com.erick.day02.Demo();
    descriptor: ()V       # 

  public static int add();
    descriptor: ()I      # 返回值int
}

```

```bash
# javap -l Demo.class >1.txt     # 输出行号和本地变量表

Compiled from "Demo.java"
public class com.erick.day02.Demo {
  public static final int num;

  protected java.lang.String name;

  public com.erick.day02.Demo();
    LineNumberTable:
      line 3: 0
      line 7: 4
    LocalVariableTable:
      Start  Length  Slot  Name   Signature
          0      11     0  this   Lcom/erick/day02/Demo;

  public static int add();
    LineNumberTable:
      line 10: 0
}
```

```bash
# javap -c Demo.class >1.txt        # 代码的反编译

Compiled from "Demo.java"
public class com.erick.day02.Demo {
  public static final int num;

  protected java.lang.String name;

  public com.erick.day02.Demo();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: aload_0
       5: ldc           #7                  // String erick
       7: putfield      #9                  // Field name:Ljava/lang/String;
      10: return

  public static int add();
    Code:
       0: iconst_1
       1: ireturn
}
```

```bash
# javap -v Demo.class >1.txt          # 详细信息，不包含私有的方法和field
# javap -verbose Demo.class >1.txt    # 
# javap -v -p Demo.class >1.txt       # 最全的信息


Classfile /Users/shuzhan/Documents/workspace-java/jvm/src/main/java/com/erick/day02/Demo.class
  Last modified Mar 20, 2023; size 435 bytes
  SHA-256 checksum a2c5181073c8aa394022198434ce86d95750af21a59852f12804215eb3a3b191
  Compiled from "Demo.java"
public class com.erick.day02.Demo
  minor version: 0
  major version: 61
  flags: (0x0021) ACC_PUBLIC, ACC_SUPER
  this_class: #10                         // com/erick/day02/Demo
  super_class: #2                         // java/lang/Object
  interfaces: 0, fields: 2, methods: 2, attributes: 1
Constant pool:
   #1 = Methodref          #2.#3          // java/lang/Object."<init>":()V
   #2 = Class              #4             // java/lang/Object
   #3 = NameAndType        #5:#6          // "<init>":()V
   #4 = Utf8               java/lang/Object
   #5 = Utf8               <init>
   #6 = Utf8               ()V
   #7 = String             #8             // erick
   #8 = Utf8               erick
   #9 = Fieldref           #10.#11        // com/erick/day02/Demo.name:Ljava/lang/String;
  #10 = Class              #12            // com/erick/day02/Demo
  #11 = NameAndType        #13:#14        // name:Ljava/lang/String;
  #12 = Utf8               com/erick/day02/Demo
  #13 = Utf8               name
  #14 = Utf8               Ljava/lang/String;
  #15 = Utf8               num
  #16 = Utf8               I
  #17 = Utf8               ConstantValue
  #18 = Integer            1
  #19 = Utf8               Code
  #20 = Utf8               LineNumberTable
  #21 = Utf8               LocalVariableTable
  #22 = Utf8               this
  #23 = Utf8               Lcom/erick/day02/Demo;
  #24 = Utf8               add
  #25 = Utf8               ()I
  #26 = Utf8               SourceFile
  #27 = Utf8               Demo.java
{

###################### field集合的信息 ############################
  public static final int num;
    descriptor: I
    flags: (0x0019) ACC_PUBLIC, ACC_STATIC, ACC_FINAL
    ConstantValue: int 1

  protected java.lang.String name;
    descriptor: Ljava/lang/String;
    flags: (0x0004) ACC_PROTECTED


###################### 方法表集合的信息 ############################

############### 构造器方法 #########
  public com.erick.day02.Demo();
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
     # stack： 方法的操作数栈深度
     # locals： 局部变量表长度
     # args_size:  方法接受参数的长度
      stack=2, locals=1, args_size=1
     
     # 偏移量，操作码      操作数
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: aload_0
         5: ldc           #7                  // String erick
         7: putfield      #9                  // Field name:Ljava/lang/String;
        10: return
      
      # 行号表： 指名字节码指令的偏移量和java程序源码中代码行号的一一对应   
      LineNumberTable:
        line 3: 0
        line 7: 4
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0      11     0  this   Lcom/erick/day02/Demo;
            
############### 成员static方法 ，通过IDEA中就是对应的 <init> #########
  public static int add();
    descriptor: ()I
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=1, locals=0, args_size=0
         0: iconst_1
         1: ireturn
      LineNumberTable:
        line 10: 0
}
SourceFile: "Demo.java"
```

# 字节码指令集

- 一个字节长度的，代表着某种特定操作含义的数字(操作码，Opcode)
- 跟随其后的0-多个代表此操作所需的参数(操作数，Operands)

```bash
do {
  自动计算PC寄存器的值加1
  根据PC寄存器的指示位置，从字节码流中取出操作码
  if(字节码存在操作数) 从字节码流中取出操作数
  执行操作码所定义的操作
} while(字节码长度>0)
```

## 1. 基本存储指令

```bash
# 助记符
aload ， 对应具体的二进制流

# 对于大部分与数据类型相关的字节码指令，他们的操作码助记符都有特殊的字符来表明专门为哪一类数据类型服务
i       int
l       long
s       short
b       byte
c       char
f       float
d       double
a       reference

# 也有一些指令的助记符，没有明确的指明操作类型的字母
- 如arraylength，没有代表数据类型，但操作数永远只能是一个数组类型的对象

# 另外的一些指令，如无条件跳转指令 goto，则是和数据类型无关的

# 大部分的指令都没有支持整数类型byte，char，short，boolean等
- 编译器会在编译器或运行期，将byte和short类型的数据转换为int
- boolean和char会转换为int
- 数组处理的时候同样的逻辑
```

### 1.1 加载与存储指令

- 将数据从栈帧的局部变量表和操作数栈之间来回传递

```bash
# 节省内存
- iload_0 :  只有操作码，没有操作数，代表将局部变量表中索引为0的位置上的数据压入到操作数栈中
- 基本只会定义前面几个
- iload 4: 将局部变量表中索引为4的位置上的数据压入到操作数栈中
```

```bash
# 压栈指令：将给定的局部变量表中的数据压入操作数栈
# 将第n个局部变量压入到操作数栈

- xload_<n> :   x为i，l，f，d，a，  n为0-3
- xload n   ：  n为4以上， 超过4以上，就需要使用操作码+操作数的方式 
```

![image-20230324112459879](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230324112459879.png)

### 1.2 常量入栈指令

- 将常数压入操作数栈，根据数据类型和入栈内容不同，分为const，push，ldc

#### int类型

- 操作数栈的栈深为1，因为int类型，
- 每次操作，先把常量进行入栈。然后对该常量出栈，并存储到局部变量表指定索引的位置

![image-20230324115637487](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230324115637487.png)

#### 其他类型

- 如果压入的是long或double类型的，则是ldc2_w

![image-20230324121643624](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230324121643624.png)

### 1.3 出栈转入局部变量表

- 将操作数栈中栈顶元素弹出后，装入局部变量表的指定位置，用于给局部变量赋值

```bash
# 和load类似： 将栈顶元素弹出，并压缩索引为n的局部变量表中
# 局部变量表是存储的具体的值 
- xstore           x为i，l，f，d，a
- xstore_n         n为0-3
```

## 2. 算数运算符

- 对两个操作数栈上的值进行某种特定运算，并把结果重新压入操作数栈
- 整型和浮点型的运算
- boolean, byte, char, short在实际运算的时候，都会转换为int来进行

![image-20230324151248326](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230324151248326.png)

#### i++ vs ++i

![image-20230324155243368](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230324155243368.png)

#### 比较指令

- dcmpg，dcmpl，fdcmpg，fdcmpl，lcmp
- fcmpg和fcmpl都会从栈中弹出两个操作数，并将他们做比较。栈顶元素为v2，栈顶顺位的第二个元素为v1，如果 v1=v2，则压入0；如果v1>v2，则压入1；如果v1<v2，则压入-1.

## 3. 创建对象和数组

- 创建类实例： new
- 创建数组： newarray(基本数据类型数组)，anewarray(引用类型数组), multianewarray(多维数组)

![image-20230324165628029](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230324165628029.png)

![image-20230324170404426](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230324170404426.png)

## 4. 字段访问

- 访问类字段：get static, putstatic
- 访问类实例字段：getfield, putfield
- 包含一个操作数，为指向常量池的Fieldref索引，获取指定对象或值，并将其压入操作数栈中

![image-20230324171448637](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230324171448637.png)

![image-20230324172156286](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230324172156286.png)

## 5. 方法调用

```bash
# invokeinterface
- 调用接口方法。一般是和多态相关，通过接口方法来调用。在运行时以搜索由特定对象所实现的这个接口方法，并找出合适的方法进行调用

# invokespecial
- 调用一些需要特殊处理的实例方法
- 实例初始化方法(构造器)，   私有方法，      父类方法(super.)
- 都是不能被重写的方法，静态类型绑定，不会在调用时进行动态派发

# invokestatic
- 调用命名类中的类方法，静态绑定的
- 如果既是static，又是private，则优先用static

# invkevirtual
- 调用对象的实例方法，用的最多
- 支持动态分配
```

## 6. 方法返回指令

```bash
#  方法调用结束后，需要进行返回
- ireturn         返回值是boolean，byte, char, short, int
- lreturn         long
- freturn         float
- dreturn         double
- areturn         reference
- return          void, 实例初始化方法

# 作用
- 将当前函数操作数栈的栈顶层元素弹出，并将这个元素压入调用者函数的操作数栈中
- 所有当前函数操作数栈中的其他元素都会被丢弃
- 最后，丢弃当前方法的整个栈帧，恢复调用者的栈帧，并将控制权交给调用者

# 同步方法
- 在return时， 还会包含一个隐含的  monitorexit指令，退出临界区
```
