# 简介

## 1. 进程/线程

```bash
# 进程
- 由指令和数据组成，指令要运行，数据要读写，必须将指令加载到CPU，数据加载到内存。指令运行过程中还需要用到磁盘，网络等IO设备
- 进程用来加载指令，管理内存，管理IO
- 一个程序被运行，从磁盘加载这个程序的代码到内存，就是开启了一个进程
- 进程可以视为一个程序的实例
- 大部分程序可以运行多个实例进程，也有的程序只能启动一个实例进程

# 线程
- 一个进程内部包含1-n个线程
- 一个线程就是一个指令流，将指令流中的一条条指令以一定的顺序交给CPU执行
- JVM中，进程作为资源分配的最小单元，线程作为最小调度单元

# 对比
- 进程基本上相互独立                                    线程存在于进程内，是进程的一个子集
- 进程拥有共享的资源，如内存空间，供其内部的线程共享
- 进程间通信比较复杂： 同一计算机的进程通信为IPC(Inter-process communication)， 
                    不同计算机之间的进程通信，需要通过网络，并遵守共同的协议，如HTTP
- 线程通信比较简单，因为它们共享进程内的内存，如多个线程可以访问同一个共享变量
- 线程更轻量，线程上下文切换成本比进程低
```

## 2. 并行/并发

### 2.1 并发

```bash
- concurrent：cpu核心同一个时间应对多个线程的能力

- 单核cpu下，线程是串行
- 任务调度器： 操作系统组件，将cpu的时间片（windows下为15ms）分给不同的线程使用
- cpu在线程间的切换非常快，感觉就是各个线程是同时运行的
- 微观串行，宏观并行
```

### 2.2 并行

```bash
- parrel： 同一个时间内，cpu真正去执行多个线程的能力

- 多核cpu下，每个核都可以调度运行线程，这个时候线程就是并行的
```

- 很多时候，并发和并行是同时存在的

![image-20230428164109578](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230428164109578.png)

### 2.3 应用场景

```bash
# 异步调用
- 异步方法：  不需立刻返回结果
- 耗时业务：  不阻塞主线程的业务

# 提升效率
- 任务拆分：  耗时业务拆分，多任务间互不依赖
- 多核cpu：  任务拆分，多线程分别执行，这样就会分到更多的cpu
- 单核cpu：  没必要拆分，串行执行，并有上下文切换的损失。  但是能够在不同任务之间切换
```

## 3. 进程查看

### 3.1. Linux

```bash
# process status
ps -ef
ps -fe               # 进程查看

ps -ef|grep java
ps -fe|grep java     # 管道运算符， 查看java进程(包含java进程和查看的grep进程)

kill pid             # 杀死指定进程
top                  # 动态查看当前进程的占用cpu和mem情况。ctrl+c 退出

top -H -P (pid)      # 查看某个进程内部的所有线程的cpu使用
                     # -H : 表示查看线程
```

### 3.2 Java

```bash
# JPS
- 查看java进程

# JConsole
- Java内置，检测java线程的图形化界面工具
- 位于jdk/bin/目录下
- 可以用来检测死锁
```

## 4. 线程切换

- Thread Context Switch
- cpu不再执行当前线程，转而执行另一个线程代码
- 线程切换时，操作系统保存当前线程的状态，并恢复另一个线程的状态
- 线程切换频繁发生会影响性能

```bash
- 线程cpu时间片用完
- 垃圾回收：                 # STW
- 有更高优先级的线程需要运行
- 线程自己调用了sleep，yield，wait，join，park，synchronized，lock等方法
```

# 基本方法

- 使用logback和lombok作为日志打印

```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
    </dependency>

    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.4.14</version>
    </dependency>
</dependencies>
```

## 1. 创建线程

- 启动JVM(main方法)，即开启一个JVM进程
- JVM进程内包含一个主线程，主线程可以派生出多个其他线程。同时还有一些守护线程，如垃圾回收线程
- 主线程，守护线程，派生线程，cpu随机分配时间片，交替随机执行 

### 1.1 继承Thread类

- 继承 Thread类，重写run()，start()启动线程

```java
package com.citi;
// 新类
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Demo01 {
    public static void main(String[] args) {
        ErickThread thread = new ErickThread();
        thread.setName("t-1");
        thread.start();
        log.info("main-running");
    }
}

@Slf4j
class ErickThread extends Thread {
    @Override
    public void run() {
        log.info("slave-running");
    }
}
```

```java
import lombok.extern.slf4j.Slf4j;

// 匿名内部类
@Slf4j
public class Demo02 {
    public static void main(String[] args) {
       Thread thread = new Thread("t-1"){
           @Override
           public void run() {
               log.info("slave-running");
           }
       };

       thread.start();
       log.info("main-running");
    }
}
```

### 1.2 实现Runnable接口

- 实现Runnable接口，重写Runnable的run方法，并将该对象作为Thread构造方法的参数传递

```bash
# Runnable优点 
- 线程和任务分开了，更加灵活
- 容易和线程池相关的API结合
- 让任务脱离了继承体系，更加灵活，避免单继承
```

```java
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Demo03 {
    public static void main(String[] args) {
        Thread thread = new Thread(new ErickTask());
        thread.setName("t-1");
        thread.start();
        log.info("main-thread");
    }
}

@Slf4j
class ErickTask implements Runnable {

    @Override
    public void run() {
        log.info("slave-thread");
    }
}
```

```java
// 匿名内部类
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Demo04 {
    public static void main(String[] args) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                log.info("slave-threa");
            }
        }, "t-1");

        thread.start();
        log.info("main-thread");
    }
}
```

### 😎 Runnable/Thread

```bash
# 策略模式
- 实际执行是调用的Runnable接口的run方法
- 因为将Runnable实现类传递到了Thread的构造参数里面
```

- Runnable接口

```java
// @FunctionalInterface修饰的接口，可以用lambda来创建
@FunctionalInterface
public interface Runnable {
    public abstract void run();
}
```

- Thread 类

```java
public class Thread implements Runnable {

    private Runnable target;
    
   // 如果是Runnable接口，则重写的是接口中的run方法
    public Thread(Runnable target) {
        this(null, target, "Thread-" + nextThreadNum(), 0);
    }
    // 如果是继承Thread,则重新的是这个run方法
    @Override
    public void run() {
        if (target != null) {
            target.run();
        }
    }
}
```

### 1.3 FutureTask类

- 可以获取任务执行结果的一个接口
- 实现Callable接口，重写Callable接口的call方法，并将该对象作为FutureTask类的构造参数传递
- FutureTask类，作为Thread构造方法的参数传递

```bash
# FutureTask 继承关系
class FutureTask<V> implements RunnableFuture<V>
interface RunnableFuture<V> extends Runnable, Future<V>
interface Future<V> :
                       boolean cancel()
                       boolean isCancelled()
                       boolean isDone()
                       V get()
                       V get(long timeout, TimeUnit unit)

# Callable 接口实现类
interface Callable<V>
```

```java
package com.citi;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo05 {
    public static void main(String[] args) {
        FutureTask<String> task = new FutureTask<>(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        log.info("slave-thread-running");
                        TimeUnit.SECONDS.sleep(2);
                        return "erick-result";
                    }
                }
        );

        Thread thread = new Thread(task, "t-1");
        thread.start();

        /*获取结果时候，会将主线程阻塞*/
        try {
            String result = task.get();
            log.info("result={}", result);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        boolean cancelled = task.isCancelled();
        boolean done = task.isDone();
        log.info("cancelled={}, done={}", cancelled, done);

        log.info("main thread ending");
    }
}
```

## 2. start/run

```bash
# public synchronized void start()
# public void run()

1. start() :  
       - 线程从new状态转换为runnable状态，等待cpu分片从而有机会转换到running状态
       - 在主线程之外，再开启了一个线程
       - 已经start的线程，不能再次start  “IllegalThreadStateException”
       
2. run():    
      - 线程内部调用start后实际执行的一个普通方法
      - 如果线程直接调用run() ，只是在主线程内，调用了一个普通的方法
```

## 3. sleep/yield

### 3.1 sleep

- 放弃cpu时间片，不释放线程锁

```bash
# public static native void sleep(long millis) throws InterruptedException
# Thread类的静态方法
- 线程放弃cpu，从RUNNABLE 进入 TIMED_WAITING状态
- 睡眠中的线程可以自己去打断自己的休眠
- 睡眠结束后，会变为RUNNABLE，并不会立即执行，而是等待cpu时间片的分配
```

```java
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo06 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                   log.error("thread interrupt");
                }
                log.info("thread end");
            }
        };
        log.info("{}", thread.getState()); // NEW
        thread.start();
        log.info("{}", thread.getState()); // RUNNABLE
        TimeUnit.SECONDS.sleep(2);
        log.info("{}", thread.getState()); // TIMED_WAITING
        thread.interrupt(); // 自己打断自己的睡眠
    }
}
```

### 3.2 yield

```bash
# public static native void yield()

- 线程让出cpu资源，让其他高优先级的线程先去执行
- 让线程从RUNNING状态转换为RUNNABLE状态
- 假如其他线程不用cpu，那么cpu又会分配时间片到当前线程，可能压根就没停下
```

### 3.3 priority

- 只是一个CPU参考值，高优先级的会被更多的分到时间资源

```java
import lombok.extern.slf4j.Slf4j;

import static java.lang.Thread.MAX_PRIORITY;
import static java.lang.Thread.MIN_PRIORITY;

@Slf4j
public class Demo07 {
    public static void main(String[] args) {
        Thread firstThread = new Thread(() -> {
            int i = 0;
            while (true) {
                i++;
                log.info("t-1 num={}", i);
            }
        });

        Thread secondThread = new Thread(() -> {
            int i = 0;
            while (true) {
                i++;
                log.info(">>>>>>>>>>>>>>>>>>>>>>>   t-2 num={}", i);
            }
        });

        firstThread.setPriority(MIN_PRIORITY);
        secondThread.setPriority(MAX_PRIORITY);
        firstThread.start();
        secondThread.start();
    }
}
```

### 3.4 sleep应用

- 程序一直执行，浪费cpu资源
- 程序间歇性休眠，让出cpu资源， 避免空转

```java
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo08 {
    public void method01() {
        while (true) {
            log.info("do...");
        }
    }

    public void method02() throws InterruptedException {
        while (true) {
            TimeUnit.SECONDS.sleep(1);
            log.info("do..");
        }
    }
}
```

## 4. join 

- 同步等待其他线程的结果调用

### 4.1. join()

```bash
# public final void join() throws InterruptedException
- xxx.join()，在调用的地方，等待xxx执行结束后再去运行当前线程
```

```java
package com.citi.d02;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo01 {
    static int number = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread firstThread = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            number = 5;
        });

        Thread secondThread = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            number = 10;
        });

        log.info("{}", number); // 0;

        firstThread.start();
        secondThread.start();
        long start = System.currentTimeMillis();
        /**
         * 1. 两个线程同时插队，以相同优先级执行
         * 2. 所以一共等待时间为3s
         */
        firstThread.join();
        secondThread.join();
        log.info("{}", number); // 10
        log.info("{}", System.currentTimeMillis() - start);
    }
}
```

### 4.2 join(long millis)

```bash
# public final synchronized void join(long millis) throws InterruptedException
- 有时效等待：最多等待多少ms， 0 代表等待到线程任务结束
- 假如线程join的等待时间超过了实际执行时间，执行完后就可以不用继续等了
```

```java
package com.citi.d02;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo02 {
    static int number = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            number = 10;
        });
        thread.start();
        thread.join(1000);
        log.info("{}", number);
    }
}
```

## 5. interrupt

```bash
# 打断线程，谁调用打断谁
- public void interrupt()

# 判断线程是否被打断 : 默认为false, 不会清除打断标记
- public boolean isInterrupted()
       
# 判断线程是否被打断:  会将线程打断标记重置为true
- public static boolean interrupted()
```

### 5.1 打断阻塞线程

- 打断阻塞的线程，抛出nterruptedException，将打断标记从true重制为false（需要一点缓冲时间）
- 如sleep，join，wait的线程

```java
package com.citi.d02;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo03 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            log.info("slave sleep");
            try {
                TimeUnit.SECONDS.sleep(10);
                // 被打断后就会抛出InterruptedException进入catch
            } catch (InterruptedException e) {
                log.error("interrupted");
            }
        });

        thread.start();
        TimeUnit.SECONDS.sleep(1);
        log.info("{}", thread.isInterrupted());    // false
        TimeUnit.SECONDS.sleep(2);
        thread.interrupt();
        // 如果不是true，则可以稍微留点缓冲时间
        TimeUnit.SECONDS.sleep(1);
        log.info("{}", thread.isInterrupted());     // false
    }
}
```

### 5.2 打断正常线程

 - 正常线程运行时，收到打断，继续正常运行, 但打断信号变为true

```java
package com.citi.d02;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo04 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            while (true) {
                log.info("hello");
            }
        });

        thread.start();
        TimeUnit.SECONDS.sleep(1);
        thread.interrupt();
    }
}
```

- 可以通过打断标记来进行普通线程的打断控制

```java
package com.erick.multithread.d02;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo05 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                log.info("hello");
            }
        });
        thread.start();
        TimeUnit.SECONDS.sleep(2);
        thread.interrupt();
    }
}
```

### 😎 Two Phase Termination模式

- 终止正常执行的线程：留下料理后事的机会
- 两阶段：正常线程阶段和阻塞线程阶段
- 业务场景：定时任务线程，如果被打断，则中止任务

```java
package com.erick.multithread.d02;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

public class Demo06 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new ScheduleTask());
        thread.start();
        TimeUnit.SECONDS.sleep(4);
        thread.interrupt();
    }
}

@Slf4j
class ScheduleTask implements Runnable {

    @Override
    public void run() {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                log.info("coming");
                clear();
                break;
            }
            interval();
            job();
        }
    }

    /*阶段一： 业务代码: 可能被打断*/
    private void job() {
        log.info("doing heavy job");
    }

    /*阶段二： 等待间隔: 可能被打断*/
    private void interval() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // 被打断时，打断标记被重制为false，需要调用该方法将打断标记置为true
            Thread.currentThread().interrupt();
            log.error("thread interrupted");
        }
    }

    private void clear() {
        log.info("clear");
    }
}
```

## 6. 守护线程

- 普通线程：只有当所有线程结束后，JVM才会退出
- 守护线程： 只要其他非守护线程运行结束了，即使守护线程的代码没执行完，也会强制退出。如垃圾回收器

```java
package com.erick.multithread.d02;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Demo02 {
    public static void main(String[] args) throws InterruptedException {
        Thread demonThread = new Thread(() -> {
            log.info("demon start");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("demon end");
        });
        demonThread.setDaemon(true);
        demonThread.start();
        Thread.sleep(2000);
        log.info("main end");
    }
}
```

## 7. 其他方法

| 方法描述     | 方法                                                         | 备注                                                         |
| ------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 名字         | public final synchronized void setName(String name);<br/>public final String getName(); |                                                              |
| 优先级       | public final void setPriority(int newPriority);<br/>public final int getPriority(); | - 最小为1，最大为10，默认为5<br/>- cpu执行目标线程的顺序  建议设置<br/>- 任务调度器可以忽略它进行分配资源 |
| 线程id       | public long getId();                                         | 13                                                           |
| 是否存活     | public final native boolean isAlive();                       |                                                              |
| 是否后台线程 | public final boolean isDaemon();                             |                                                              |
| 获取线程状态 | public State getState()                                      | NEW  RUNNABLE  BLOCKED    <br/>WAITING   TIMED_WAITING   TERMINATED |
| 获取当前线程 | public static native Thread currentThread()                  |                                                              |

# 生命周期

## 1. 操作系统

- 从操作系统层面来说，包含五种状态
- CPU时间片只会分给可运行状态的线程，阻塞状态的需要其本身阻塞结束

![image-20240405161947365](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240405161947365.png)

**NEW** 

- new 出一个Thread的对象时，线程并没有创建，只是一个普通的对象
- 调用start，new 状态进入runnable状态

**RUNNABLE**

- 调用start，jvm中创建新线程，但并不立即执行，只是处于就绪状态
- 等待cpu分配时间片，轮到它时，才真正执行

**RUNNING**

- 一旦cpu调度到了该线程，该线程真正执行

```bash
# 该状态的线程可以转换为其他状态
- 进入BLOCK：        sleep， wait，阻塞IO如网络数据读写， 获取某个锁资源
- 进入RUNNABLE：     cpu轮询到其他线程，线程主动yield放弃cpu
```

**BLOCK**

```bash
# 该状态的线程可以转换为其他状态
- 进入RUNNABLE：      线程阻塞结束，完成了指定时间的休眠
                     wait中的线程被其他线程notify/notifyall
                     获取到了锁资源
```

**TERMINATED**

-  线程正常结束
-  线程运行出错意外结束
-  jvm crash,导致所有的线程都结束

## 2. JAVA层面

- 根据Thread类中内部State枚举，分为六种状态

```
NEW     RUNNABLE    BLOCKED    WAITING    TIMED_WAITING   TERMINATED
```

# 线程安全

## 1. 不安全场景

- 多线程对共享变量的并发修改

```java
package com.erick.multithread.d02;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Demo03 {
    public static void main(String[] args) throws InterruptedException {
        ErickService service = new ErickService();

        Thread first = new Thread(() -> service.incur());
        Thread second = new Thread(() -> service.decr());
        first.start();
        second.start();
        first.join();
        second.join();

        log.info("{}", service.getNumber());
    }
}

class ErickService {
    @Getter
    private int number;

    public void incur() {
        for (int i = 0; i < 1000000; i++) {
            number++;
        }
    }

    public void decr() {
        for (int i = 0; i < 1000000; i++) {
            number--;
        }
    }
}
```

## 2. 不安全原因

### 字节码

- 线程拥有自己的栈内存，读数据时会堆主内存中拿，写完后会将数据写回堆内存
- i++在实际执行的时候，是一系列指令，一系列指令就会导致指令交错

![image-20230428164030859](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230428164030859.png)

### 指令交错

- 存在于多线程之间
- 线程上下文切换，引发不同线程内指令交错，最终导致上述操作结果不会为0

![image-20230428164425388](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230428164425388.png)

### 不安全

- 线程不安全： 只会存在于多个线程共享的资源
- 临界区：  对共享资源的多线程读写操作的代码块
- 竞态条件： 多个线程在在临界区内，由于代码的指令交错，对共享变量资源的争抢

```java
多线程  读    共享资源  没问题
多线程  读写  共享资源  可能线程不安全(指令交错)
```

## 3. synchronized

### 3.1 同步代码块

- 对象锁：只要为同一个对象，为任意对象(除基本类型)
- 对象锁：必须用final修饰，这样保证对象的引用不可变，从而确保是同一把锁

```bash
- 阻塞式的，解决多线程访问共享资源引发的不安全问题
- 可在不同代码粒度进行控制
- 保证了《临界区代码的原子性(字节码)》，不会因为线程的上下文切换而被打断
- 必须保证对对同一个对象加锁(Integer.value(0))

# 原理
1. a线程获取锁，执行代码，并执行临界区代码
2. b线程尝试获取锁，无法获取锁资源，被block，进入  《等待队列》，同时进入上下文切换
3. a线程执行完毕后，释放锁资源。唤醒其他线程，进行cpu的争夺
```

```java
/*
   可以在不同粒度进行加锁， 锁对象可以为任意对象，比如lock
   一般会使用this作为锁
 */
class ErickService {
    @Getter
    private int number;

    private final Object lock = new Object();
    
    public void incur() {
        for (int i = 0; i < 1000000; i++) {
            synchronized (lock) {
                number++;
            }
        }
    }

    public void decr() {
        for (int i = 0; i < 1000000; i++) {
            synchronized (lock) {
                number--;
            }
        }
    }
}
```

### 3.2 同步方法

#### 普通成员方法

- 同步成员方法和同步代码块效果一样(前提是：同步代码块的锁对象是this对象)
- 可能锁粒度不太一样
- 同步方法的锁对象是this，即当前对象

```java
@Data
class Calculator {
    private int number;

    public void incr() {
        synchronized (this) {
            for (int i = 0; i < 10000; i++) {
                number++;
            }
        }
    }
    // 同步方法
    public synchronized void decr() {
        for (int i = 0; i < 10000; i++) {
            number--;
        }
    }
}
```

#### 静态成员方法

-  锁对象：锁用的是类的字节码对象: Calculator.class

```java
@Data
class Calculator {
    private static int number;
    
    /*多个方法，只要用的是同一把锁，就可以保证线程安全*/
    public static void incr() {
        for (int i = 0; i < 10000; i++) {
            synchronized (Calculator.class) {
                number++;
            }
        }
    }

    public static synchronized void decr() {
        for (int i = 0; i < 10000; i++) {
            number--;
        }
    }
}
```

## 4. 变量安全

### 4.1 成员变量&静态成员变量

```bash
1. 没被多线程共享：        则线程安全
2. 被多线程共享：
     2.1 如果只读，   则线程安全
     2.2 如果有读写， 则可能发生线程不安全问题
```

### 4.2 局部变量

#### 线程安全

- 每个线程方法都会创建单独的栈内存，局部变量保存在自己当前线程方法的栈桢内
- 局部变量线程私有

```bash
# 基础数据类型
- 安全

# 引用类型时
- 可能不安全
   - 如果该对象没有逃离方法的作用访问，则线程安全
   - 如果该对象逃离方法的作用范围，则可能线程不安全 《引用逃离》
 
# 避免局部变量线程安全类变为不安全类： 不要让一个类的方法被重写
- final修饰类禁止继承，或对可能引起安全的方法加private
```

#### 引用逃离

- 一个类不是final类，那么就可能被继承
- 被继承的时候发生方法覆盖，覆盖方法如果创建新的线程，就可能发生局部变量不安全
- 通过final或者private来不让父类方法被重写，从而保证线程安全性

```java
// 安全
package com.erick.multithread.d03;

import java.util.List;

class SafeCounter {
    public void handle(List<String> data) {
        for (int i = 0; i < 100000; i++) {
            add(data);
            remove(data);
        }
    }

    public void add(List<String> data) {
        data.add("hello");
    }

    public void remove(List<String> data) {
        data.remove(0);
    }
}
```

```java
// 不安全
package com.erick.multithread.d03;

import java.util.List;

public class UnsafeCounter extends SafeCounter {

    @Override
    public void remove(List<String> data) {
        new Thread() {
            @Override
            public void run() {
                data.remove(0);
            }
        }.start();
    }
}
```

### 4.3 线程安全类

#### JDK类

- 多个线程同时调用他们同一个实例的方法时，线程安全
- 线程安全类中的方法的组合，不一定线程安全
- 如果要线程安全，必须将组合方法也设置成 synchronized

```bash
- String
- Integer
- StringBuffer
- Random
- Vector
- Hashtable
- java.util.concurrent包下的类
```

```java
package com.erick.multithread.d03;

import com.erick.multithread.util.Sleep;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Demo03 {
    public static void main(String[] args) throws InterruptedException {
        CollectionService service = new CollectionService();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> service.combinedMethod());
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println(service.getHashtable().size()); // 实际为6
    }
}

class CollectionService {
    @Getter
    private Hashtable<String, String> hashtable = new Hashtable<>();

    /*get和put方法是线性安全的，但组合方法不是
     * 解决方式： 组合方法也加synchronized*/
    public void combinedMethod() {
        if (hashtable.get("k1") == null) {
            Sleep.sleep(1);
            hashtable.put("k1", "---");
            hashtable.put(Thread.currentThread().getName(), "===");
        }
    }
}
```

#### 不可变类

- 类中属性都是final，不能修改
- 如 String，Integer

```bash
#  实现线程安全类的问题
- 无共享变量
- 共享变量不可变
- synchronized互斥修饰
```

# Synchronized锁

## 1. 对象头

- 以32位的 JVM 为例，堆内存中的Java对象中，对象头都包含如下信息
- Mark Word:     锁信息，hashcode，垃圾回收器标志
- Klass Word:     指针，包含当前对象的Class对象的地址

### 1.1 普通对象

![image-20240406093635621](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240406093635621.png)

### 1.2 数组对象

![image-20240406093719742](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240406093719742.png)

## 2. Mark Word

- 如果加锁用到了该对象，那么该对象头的内容，会随着加锁状态来改变Mark Word

![image-20220926112525895](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220926112525895.png)

```bash
# hashcode
- 每个java对象的hashcode

# age
- 对象的分代年龄，为垃圾回收器进行标记

# biased_lock
- 是否是偏向锁

# 锁状态： 01/00/11
- 代表是否加锁
- 01: 无锁
- 00: 轻量级锁
- 10: 重量级锁
```

## 3. 轻量级锁

- 锁对象虽被多个线程都来访问，但访问时间错开，不存在竞争
- 对使用者 是透明的， 语法：syncronized
- 如果加锁失败，则会升级会重量级锁
- 轻量级锁没有和monitor关联，不存在阻塞

### 3.1 加锁流程

#### 创建锁记录

- 每个线程的栈帧都会包含一个锁记录的结构，存储锁定对象的MarkWord

![image-20240406114156940](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240406114156940.png)

#### CAS

- 让锁记录中的Object reference指向锁对象，并尝试使用CAS替换Object中的Mark Word，将Mark Word的值存入锁记录
- CAS成功了，则表示加上了锁
- 可能CAS失败

![image-20240406114514180](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240406114514180.png)

#### 解锁

- 执行完临界代码块后，再次交换，并从当前栈帧中删除Lock Record

![image-20240406112825427](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240406112825427.png)

### 3.2 锁重入

- 一个线程，A方法调用B方法时候，两个方法都用到了相同的锁

#### 创建锁记录

![image-20240407094251770](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240407094251770.png)

#### CAS

![image-20240407094503592](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240407094503592.png)

#### 锁重入

- 在当前栈帧中，创建第二个锁记录
- 让第二个Lock Record中的Object Reference指向锁地址
- 尝试CAS时，发现该锁的Mark Word就是当前线程的另外一个Lock Record的地址
- 则第二个Lock Record会存一个null，计数器加一

![image-20240407095233862](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240407095233862.png)

#### 解锁

- 解锁时候，如果发现了null，则表示有重入，则直接清空Lock Record即可
- 直到完全CAS并删除Lock Record为止

## 4. 锁膨胀

### 4.1 轻量级锁加锁失败

- thread-0进行CAS交换时，发现Object已经是加锁状态了，因此加锁失败
- 引入monitor锁

![image-20240406164435179](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240406164435179.png)

### 4.2 monitor

- 监视器(管程)： 操作系统提供，会有多个不同的monitor

![image-20220926113801439](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220926113801439.png)

```bash
# Owner    
- 保存当前获取到java锁的，线程指针
# EntryList
- 保存被java锁阻塞的，线程指针
# WaitSet:    
- 保存被java锁等待的，线程指针
```

### 4.3 加锁流程

#### 申请monitor

- thread-0进行CAS时失败，就进行锁膨胀
- 为锁对象Object申请Monitor锁，Java锁对象的Mark Word保存monitor地址，后两位同时变为10
- 该Monitor的EntryList保存thread-0的指针，同时thread-0进入阻塞状态

![image-20240406171127826](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240406171127826.png)

#### 原CAS解锁

- Thread-1执行完临界代码块，解锁时，发现锁对象的Mark Word已经变成重量级锁的地址，进入重量级锁结束流程
- 清空monitor的Owner，并唤醒EntryList中的线程，随机唤醒一个线程，成为新的Owner

## 5. 重量级锁

- 锁一旦成为重量级锁后，就不能再降级成为轻量级锁

### 5.1 锁竞争

- 如果当前锁已经是重量级锁
- thread-0已经成为monitor的owner，其他线程过来后，就会进入monitor的EntryList来进行阻塞等待

![image-20240406172814684](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240406172814684.png)

### 5.2 解锁

- thread-0执行完毕后，会将monitor中的Owner清空，同时通知该monitor的EntryList中的线程来抢占锁(非公平锁)
- 抢占到的新的thread，成为Owner中新的主人

### 😎 自旋优化

- 只属于重量级锁

```bash
- 一个线程的重量级锁被其他线程持有时，该线程并不会直接进入阻塞
- 先本身自旋，同时查看锁资源在自旋优化期间是否能够释放   《避免阻塞时候的上下文切换》
- 若当前线程自旋成功(即此时持有锁的线程已经退出了同步块，释放了锁)，这时线程就避免了阻塞
- 若自旋失败，则进入EntryList中
```

```bash
智能自旋： 
-  自适应的: Java6之后，对象刚刚的一次自旋成功，就认为自旋成功的概率大，就会多自旋几次
            反之，就少自旋几次甚至不自旋
- java7之后不能控制是否开启自旋功能
- 自旋会占用cpu时间，单核自旋就是浪费，多核自旋才有意义
```

### 3.2 锁重入

- 锁重入： 一个线程在调用一个方法的时候，在方法调用链中，多次使用同一个对象来加锁

![image-20220928202550955](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220928202550955.png)

```bash
# 创建锁记录
- 线程在自己的工作内存内，创建栈帧，并在活动栈帧创建一个  《锁记录》  的结构
- 锁记录： lock record address: 加锁的信息，用来保存当前线程ID等信息, 同时后续会保存对像锁的Mark Word
          Object Reference:  用来保存锁对象的地址
          00: 表示轻量级锁， 01代表无锁
          
- 锁记录对象：是在JVM层面的，对用户无感知         
- Object Body: 该锁对象的成员变量

# 加锁cas-- compare and set
# cas成功
- 尝试cas交换Object中的 Mark Word和栈帧中的锁记录

# cas失败
- 情况一：锁膨胀，若其他线程持有该obj对象的轻量级锁，表明有竞争，进入锁膨胀过程，加重量级锁
- 情况二：锁重入，若本线程再次synchronized锁，再添加一个Lock Record作为重入计数
- 两种情况区分： 根据obj中保存线程的lock record地址来进行判断
- null： 表示重入了几次

# 解锁cas
- 退出synchronized代码块时，若为null的锁记录，表示有重入，这时清除锁记录（null清除）
- 退出synchronized代码块时，锁记录不为null，cas将Mark Word的值恢复给对象头
  同时obj头变为01无锁状态
- 成功则代表解锁成功； 失败说明轻量级锁进入了锁膨胀
```

# Wait/Notify

- 线程在加锁执行时，发现条件不满足，则进行wait，同时释放锁
- 条件满足后，通过notify来唤醒，同时继续竞争锁

## 1. 原理

- Owner线程发现条件不满足，调用当前锁的wait，进入WaitSet变为WAITING状态
- Wait会释放当前锁资源

```bash
1. BLOCK和WAITING的线程都处于阻塞状态，不占用cpu
2. BLOCK线程会在Owner线程释放锁时唤醒
3. WAITING线程会在Owner线程调用notify时唤醒，但唤醒后只是进入EntryList进行阻塞，待锁时候后，重新竞争锁
```

![image-20220929091827634](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220929091827634.png)

## 2. API

- Object类的方法，必须成为锁的owner时才能使用
- 调用当前锁对象的lock，notify方法

```bash
# 当前获取锁的线程进入WaitSet, 一直等待
public final void wait() throws InterruptedException

# 当前获取锁的线程只等待一定时间，然后从 WaitSet 重新进入EntryList来竞争锁资源
# 也可以被提前唤醒
public final native void wait(long timeoutMillis) throws InterruptedException

# 随便唤醒一个线程，进入到EntryList
public final native void notify()

# 唤醒所有的线程，进入到EntryList
public final native void notifyAll()
```

### 2.1 基本使用

```java
package com.erick.multithread.d02;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * first coming
 * second coming
 * inform coming
 * inform finished
 * first finished
 * second finished
 */
public class Demo01 {

    public static void main(String[] args) throws InterruptedException {
        WorkingService workingService = new WorkingService();
        new Thread(() -> workingService.firstJob()).start();
        new Thread(() -> workingService.secondJob()).start();

        TimeUnit.SECONDS.sleep(2);

        new Thread(() -> workingService.inform()).start();
    }
}

@Slf4j
class WorkingService {
    private final Object lock = new Object();

    public void firstJob() {
        synchronized (lock) {
            try {
                log.info("first coming");
                lock.wait();
                log.info("first finished");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void secondJob() {
        synchronized (lock) {
            try {
                log.info("second coming");
                lock.wait();
                log.info("second finished");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void inform() {
        synchronized (lock) {
            log.info("inform coming");
            lock.notifyAll();
            log.info("inform finished");
        }
    }
}
```

### 2.2 必须条件

- 要wait或者notify，必须先获取到锁资源，否则不允许wait或者notify

```java
public void inform() {
    // Exception in thread "Thread-2" java.lang.IllegalMonitorStateException: current thread is not owner
    log.info("inform coming");
    lock.notifyAll();
    log.info("inform finished");
}
```

### 2.3 Wait vs Sleep

```bash
- Wait 是Object的方法，调用lock的方法                     Sleep 是Thread 的静态方法
- Wait 必须和synchronized结合使用                        Sleep 不需要
- Wait 会释放当前线程的锁                                 Sleep 不会释放锁（如果工作时候带锁）

# 都会让出cpu资源，状态都是Timed-Waiting
```

## 3. 虚假唤醒

- 循环wait： 防止虚假唤醒的问题，确保线程一定是执行完毕任务后才会结束

```java
// 工作线程
synchronized(lock){
  while(条件不成立){
    lock.wait();
  }
  executeBusiness();
};

//其他线程唤醒
synchronized(lock){
  // 实现上述条件
  lock.notifyAll();
}
```

```java
package com.erick.multithread.d02;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

public class Demo04 {
    public static void main(String[] args) throws InterruptedException {
        JobService jobService = new JobService();
        new Thread(() -> jobService.manWork()).start();
        for (int i = 0; i < 5; i++) {
            new Thread(() -> jobService.childWork()).start();
        }
        TimeUnit.SECONDS.sleep(2);
        new Thread(() -> jobService.deliverCigarette()).start();
    }
}

@Slf4j
class JobService {
    private final Object lock = new Object();

    private boolean hasCigarette = false;

    public void manWork() {
        synchronized (lock) {
            while (!hasCigarette) {
                log.info("没有烟，休息会儿");
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            executeBusiness();
        }
    }

    public void deliverCigarette() {
        synchronized (lock) {
            hasCigarette = true;
            log.info("送烟来了");
            lock.notifyAll();
        }
    }

    public void childWork() {
        synchronized (lock) {
            log.info("{} child working", Thread.currentThread().getName());
        }
    }

    private void executeBusiness() {
        log.info("烟来了，干活");
    }
}
```

## 4. 保护性暂停模式

- GuardedSuspension：一个线程等待另一个线程的一个执行结果，即同步模式

```bash
- 一个结果需要从一个线程传递到另一个线程，让两个线程关联同一个GuardedObject
- JDK中， join的实现，Future的实现，就是采用保护性暂停
```

![image-20220929120241558](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220929120241558.png)

```java
package com.erick.multithread.d02;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo08 {
    public static void main(String[] args) {
        GuardObject guardObject = new GuardObject();
        new Thread(() -> {
            Object result = guardObject.getResult();
            log.info("{}", result);
        }).start();

        new Thread(() -> guardObject.setResult()).start();
    }
}

@Slf4j
class GuardObject {
    private Object result;

    public synchronized void setResult() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        result = new Object();
        log.info("result completed");
        this.notifyAll();
    }


    /*无限等待*/
    public synchronized Object getResult() {
        while (result == null) {
            log.info("waiting for result");
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /*超时等待：一定时间后，还是会返回一个数据*/
    public Object getResult(long timeout, TimeUnit timeUnit) {
        long start = System.currentTimeMillis();
        long passedTime = 0; /*经历了多长时间*/
        timeout = timeUnit.toMillis(timeout);

        while (result == null) {
            long leftTime = timeout - passedTime;
            if (leftTime <= 0) {
                return null;/*如果剩余时间小于0，则直接返回*/
            }

            try {
                this.wait(leftTime); /*动态等待*/
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            /*被虚假唤醒时*/
            passedTime = System.currentTimeMillis() - start;
        }
        return result;
    }
}
```

## 5. 消息队列模式

- 结果从A类线程不断传递到B类线程，使用消息队列(生产者消费者)
- 多个生产者及消费者， 阻塞队列， 异步消费
- 消息队列，先入先得，有容量限制，满时不再添加消息，空时不再消费消息
- JDK中各种阻塞队列，就是用的这种方式

![image-20220929180221221](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220929180221221.png)


```java
package com.erick.multithread.d02;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo09 {
    public static void main(String[] args) throws InterruptedException {
        MessageBroker broker = new MessageBroker(3);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                Object result = broker.consume();
                log.info("{}", result);
            }).start();
        }

        TimeUnit.SECONDS.sleep(3);

        for (int i = 0; i < 5; i++) {
            new Thread(() -> broker.produce("hello" + new Random().nextInt())).start();
        }
    }
}

@Slf4j
class MessageBroker {

    private int capacity;

    private LinkedList<Object> blockingQueue = new LinkedList<>();

    public MessageBroker(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void produce(Object data) {
        while (blockingQueue.size() >= capacity) {
            log.info("{}: full queue, producer wait", Thread.currentThread().getName());
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        blockingQueue.addFirst(data);
        this.notifyAll();
    }

    public synchronized Object consume() {
        while (blockingQueue.isEmpty()) {
            log.info("{}: empty result, consumer wait", Thread.currentThread().getName());
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Object result = blockingQueue.removeLast();
        this.notifyAll();
        return result;
    }
}
```

# 锁特性

## 1. 多锁

- 如果不同方法访问的不是同一个共享资源，则尽可能提供多把锁，否则会降低并发度
- 共享同一个资源的不同方法，才让他们去使用同一把锁

### 1.1 一把锁

```java
package com.erick.multithread.d03;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo01 {
    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        BigRoom bigRoom = new BigRoom();
        for (int i = 0; i < 3; i++) {
            Thread firstKindThread = new Thread(() -> bigRoom.firstJob());
            Thread secondKindThread = new Thread(() -> bigRoom.secondJob());
            firstKindThread.start();
            secondKindThread.start();
            threads.add(firstKindThread);
            threads.add(secondKindThread);
        }

        long start = System.currentTimeMillis();
        for (Thread thread : threads) {
            thread.join();
        }
        log.info("{}", System.currentTimeMillis() - start); // 3s
    }
}

class BigRoom {
    private int firstNumber = 0;
    private int secondNumber = 0;

    public void firstJob() {
        synchronized (this) {
            firstNumber++;
            sleep();
        }
    }

    public void secondJob() {
        synchronized (this) {
            secondNumber++;
            sleep();
        }
    }

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### 1.2 多把锁

```java
class BigRoom {
    private int firstNumber = 0;
    private final Object firstLock = new Object();
    private int secondNumber = 0;

    private final Object secondLock = new Object();

    public void firstJob() {
        synchronized (firstLock) {
            firstNumber++;
            sleep();
        }
    }

    public void secondJob() {
        synchronized (secondLock) {
            secondNumber++;
            sleep();
        }
    }

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

## 2. 活跃性

- 一个线程中的代码，因为某种原因，一直不能执行完毕

### 2.1 死锁

- 如果一个线程需要同时获取多把锁，就容易发生死锁

```bash
- 线程一：持有a锁，等待b锁
- 线程二：持有b锁，等待a锁
- 互相等待引发的死锁问题
- 哲学家就餐问题
- 定位死锁: 可以借助jconsole来定位死锁
- 解决方法： 按照相同顺序加锁就可以，但可能引发饥饿问题
```

```java
package com.erick.multithread.d03;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

public class Demo02 {
    public static void main(String[] args) {
        DeadLock deadLock = new DeadLock();
        new Thread(() -> deadLock.firstMethod()).start();

        new Thread(() -> deadLock.secondMethod()).start();
    }
}

@Slf4j
class DeadLock {
    private final Object firstLock = new Object();
    private final Object secondLock = new Object();

    public void firstMethod() {
        synchronized (firstLock) {
            log.info("{} first lock coming", Thread.currentThread().getName());
            sleep();
            synchronized (secondLock) {
                log.info("{} second lock coming", Thread.currentThread().getName());
            }
        }
    }

    public void secondMethod() {
        synchronized (secondLock) {
            log.info("{} second lock coming", Thread.currentThread().getName());
            sleep();
            synchronized (firstLock) {
                log.info("{} first lock coming", Thread.currentThread().getName());
            }
        }
    }

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

![image-20240407163050432](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240407163050432.png)

### 2.2 饥饿

```bash
# 场景一：
- 某些线程，因为优先级别低，一直抢占不到cpu的资源，导致一直不能运行

# 场景二：
- 在加锁情况下，有些线程，一直不能成为锁的Owner，任务无法执行完毕
```

### 2.3 活锁

- 并没有加锁
- 两个线程中互相改变对方结束的条件，导致两个线程一直运行下去
- 可能会结束，但是二者可能会交替进行

```java
package com.erick.multithread.d03;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo03 {
    static volatile int number = 10;

    public static void main(String[] args) {
        new Thread(() -> {
            while (number < 20) {
                number++;
                log.info("+++++++ {}", number);
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> {
            while (number > 0) {
                number--;
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("                         --------- {}", number);
            }
        }).start();
    }
}
```

# 内存模型

```bash
# JMM
- Java Memory Model
- 定义主存，工作内存等概念

# 主存
- 所有线程都共享的数据，比如堆内存(成员变量)，静态成员变量(方法区)
# 工作内存
- 线程私有的数据，比如局部变量(栈帧中的局部变量表) 
```

## 1. 可见性

- 一个线程对主存中数据写操作，对其他线程的读操作可见性

### 1.1  不可见性

```java
package com.erick.multithread.d03;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

public class Demo04 {
    public static void main(String[] args) throws InterruptedException {
        // 主存的数据
        ErickService service = new ErickService();
        new Thread(() -> service.first(), "t-1").start();

        // 必须缓存一段时间，让JIT做从主存到工作内存的缓存优化
        TimeUnit.SECONDS.sleep(2);

        /*2s后，t-2 线程更新了flag的值，但是t-1线程并没有停下来*/
        new Thread(() -> service.updateFlag(), "t-2").start();
    }
}

@Slf4j
class ErickService {
    private boolean flag = true;

    public void first() {
        while (flag) {
        }
    }

    public void updateFlag() {
        log.info("update the flag");
        flag = !flag;
    }
}
```

### 1.2  不可见原因

```bash
# 初始状态
- 初始状态，变量flag被加载到主存中
- t1线程从主存中读取到了flag
- t1线程要频繁从主存中读取数据,经过多次的循环后，JIT进行优化
- JIT编译器会将flag的值缓存到当前线程的栈帧工作内存中的高速缓存中(栈内存)，减少对主存的访问，提高性能
 
 # 2秒后
- 主存中的数据被t2线程修改了，但是t1线程还是读取自己工作内存的数据，并不会去主存中去拿
```

![image-20230430170254485](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230430170254485.png)

### 1.3 解决方案-volatile

- 轻量级锁

```bash
- 修饰成员变量或者静态成员变量
- 线程获取该变量时，不会从自己的工作内存中去读取，每次都是去主存中读取
- 牺牲了性能，保证了一个线程改变主存中某个值时，对于其他线程不可见的问题
```

```java
@Slf4j
class ErickService {
    private volatile boolean flag = true;

    public void first() {
        while (flag) {
        }
    }

    public void updateFlag() {
        log.info("update the flag");
        flag = !flag;
    }
}
```

### 1.4 解决方案-synchronized

- 重量级锁

```bash
# java内存模式中，synchronized规定，线程在加锁时
1. 先清空工作内存
2. 在主存中拷贝最新变量到工作内存中
3. 执行代码
4. 将更改后的共享变量的值刷新到主存中
5. 释放互斥锁
```

```java
@Slf4j
class ErickService {
    private boolean flag = true;

    public synchronized void first() {
        while (true) {
            synchronized (this) {
                if (!flag) {
                    break;
                }
            }
        }
    }

    public void updateFlag() {
        log.info("update the flag");
        flag = !flag;
    }
}
```

## 2. 原子性-指令交错

- 多线程访问共享资源，指令交错引发的原子性问题
- volatile：不保证
- synchronized：能解决

## 3. 有序性-指令重排

- JVM会在不影响正确性的前提下，可以调整语句的执行顺序
- 在单线程下没有任何问题，在多线程下可能发生问题

### 3.1 计组思想

```bash
- 每条指令划分为    取指令 --- 指令译码 --- 执行指令 --- 内存访问 --- 数据回写
- 计组思想 并不能提高单条指令的执行时间，但是变相的提高了吞吐量
- 同一时间，对不同指令的不同步骤进行处理，提高吞吐量
```

![image-20230430145005362](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230430145005362.png)

![image-20230430145021608](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230430145021608.png)


### 3.2  指令重排

- 一行代码对应字节码可能分为若干个指令
- JVM在不影响性能的情况下，会对代码对应的字节码指令执行顺序进行重排

#### 单线程

- 指令重排不会对结果产生任何影响

#### 多线程

- 因为指令重排，可能引发诡异的问题

```bash
# 场景一：正常结果：10
- 线程2执行num=2时，线程1进入flag判断，result为10

# 场景二：正常结果：2
- 线程2执行完flag，线程1才开始，result为2

# 场景三：异常：0
- 线程2在执行时候指令重排：     num = 2; flag = true;---> flag = true; num = 2; 
- 线程2执行到flag = true，线程1开始，result为0
```

```java
class OrderServer {
    private int num = 0;
    private boolean flag = false;

    private int result;

    /*线程1执行该方法*/
    public void first() {
        if (flag) {
            result = num;
        } else {
            result = 10;
        }
    }

    /*线程2执行该方法*/
    public void second() {
        num = 2;
        flag = true;
    }
}
```

## 4. 读写屏障-voliate

- Memory Barrier(Memory Fence):     底层实现是内存屏障

```bash
# 1. 写屏障
- voliate修饰的变量，会在写操作后，加上写屏障(代码块)
- 可见性 ：       在该屏障之前的所有代码的改动，同步到主存中去
- 有序性 ：       确保指令重排时，不会将写屏障之前的代码排在写屏障之后

# 2. 读屏障
- volatile修饰的变量，会在读取到该变量的前面，加上读屏障
- 可见性 ：      读屏障后面的数据，都会在主存中获取
- 有序性：       不会将读屏障之后的代码排在读屏障之前
```

### 4.1 可见性

```java
package com.erick.multithread.d05;

import java.util.concurrent.TimeUnit;

public class Demo04 {
    public static void main(String[] args) throws InterruptedException {
        TomService service = new TomService();
        new Thread(() -> service.work()).start();
        TimeUnit.SECONDS.sleep(2);
        new Thread(() -> service.changeValue()).start();
    }
}

class TomService {

    private boolean firstFlag = false;
    private volatile int age = 0;

    private boolean secondFlag = false;

    /**
     * 写屏障： age是volatile修饰，在age下面加上写屏障
     * age以及前面的firstFlag，secondFlag等写操作都会同步到主存中
     */
    public void changeValue() {
        firstFlag = true;
        secondFlag = true;
        age = 10;
        /*--------写屏障-------*/
    }


    /**
     * 读屏障： age是volatile修饰，在age上加上读屏障
     * 读屏障下面的所有变量，都是从主存中加载
     */
    public void work() {
        while (true) {
            /*--------读屏障--------*/
            int w_age = age;
            boolean w_firstFlag = firstFlag;
            boolean w_secondFlag = secondFlag;
            if (w_age == 10 && w_firstFlag && w_secondFlag) {
                break;
            }
        }
    }
}
```

### 4.2 有序性

-  单例模式: 双重加锁检查机制

```bash
# volatile作用：防止指令重排
# instance = new Singleton();   具体执行的字节码指令可以分为三步
1. 给instance分配内存空间
2. 调用构造函数来对内存空间进行初始化
3. 将构造好的对象的指针指向instance(这样对象就不为null了)

# 假如不加volatile
- 线程一执行时候，假如发生指令重排，按照132来构造，当执行到3的时候，其实对象还没初始化好
- 线程二在判断的时候，就发现instance已经不是null了，就返回了一个没有初始化完全的对象
```

```java
package com.erick.multithread.d02;

import com.erick.multithread.util.Sleep;

public class Demo05 {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> System.out.println(Singleton.getInstance())).start();
        }
    }
}

class Singleton {
    /**
     * volatile作用：防止指令重排
     * instance = new Singleton();
     */
    private volatile static Singleton instance;

    private Singleton() {
    }

    public static Singleton getInstance() {
        if (instance == null) {
            /*第一批的若干个线程都能到这里，
             * 但instance实例一旦初始化完毕，后续就不会进入这里了*/
            Sleep.sleep(2);

            synchronized (Singleton.class) {
                /*第一个线程初始化完毕后，第一批的后面所有，就不会初始化了，但是还是加锁判断的*/
                if (instance == null) {
                    instance = new Singleton();
                }
                return instance;
            }
        }

        return instance;
    }
}
```

## 5. synchronized/volatile

|                        | synchronized         | volatile                 |
| ---------------------- | -------------------- | ------------------------ |
| 锁级别                 | 重量级锁             | 轻量级锁                 |
| 可见性                 | 可解决               | 可解决                   |
| 原子性(多线程指令交错) | 可解决               | 不保证                   |
| 有序性(指令重排)       | 可以重排，但不会出错 | 可以禁止重排             |
| 场景                   | 多线程并发修改       | 一个线程修改，其他线程读 |

## 6 happen-before

- 对共享变量的读写操作，代码的可见性和有序性的总结

### 6.1 synchronized

- 线程解锁时候，对变量的写，会从工作内存同步到主内存中，其他线程加锁的读，会从主内存中取

```java
package com.dreamer.multithread.day02;

public class Demo04 {
    private static int x = 0;

    private static Object lock = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (lock) {
                x = 10;
            }
        }).start();

        new Thread(() -> {
            synchronized (lock) {
                System.out.println(x);
            }
        }).start();
    }
}
```

### 6.2 volatile

- 变量用volatile修饰，一个线程对其的写操作，对于其他线程来说是可见的
- 写屏障结合读屏障，保证是从主存中读取变量

### 6.3 先写先得

- 线程start前对变量的写操作，对该线程开始后的读操作是可见的

```java
package com.dreamer.multithread.day02;

public class Demo06 {
    private static int x = 0;

    public static void main(String[] args) {

        x = 10;

        new Thread(() -> System.out.println(x)).start();
    }
}
```

### 6.4 通知准则

- 线程结束前对变量的写操作，会将操作结果同步到主存中(join())
- 主要原因：public final synchronized void join(long millis)，加锁了

```java
package com.erick.multithread.d05;

import java.util.concurrent.TimeUnit;

public class Demo07 {

    private static boolean flag = true;

    public static void main(String[] args) throws InterruptedException {

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                flag = false;
            }
        });
        t1.start();
        t1.join(); // 其实就是t1先执行，执行完毕后，已经将最新结果同步到主存中了

        while (flag) {

        }
    }
}
```

### 6.5 打断规则

- 线程t1 打断t2前，t1对变量的写，对于其他线程得知得知t2被打断后，对变量的读可见

```java
package com.erick.multithread.d05;

import lombok.Setter;

import java.util.concurrent.TimeUnit;

public class Demo08 {
    public static void main(String[] args) {
        PhoneServer server = new PhoneServer();

        Thread t2 = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(10000);
            } catch (InterruptedException e) {
            }
        });
        t2.start();

        Thread t1 = new Thread(() -> {
            // 写的455会生效
            server.setNumber(123);
            // 2秒后打断t2的休眠线程
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            t2.interrupt();
        });
        t1.start();

        new Thread(() -> server.firstMethod(t2)).start();
    }
}

class PhoneServer {
    @Setter
    private int number = 2;

    public void firstMethod(Thread interThread) {
        while (!interThread.isInterrupted() || number == 456) {

        }
    }
}
```

### 6.6 传递性

- 其实就是volatile的读屏障和写屏障

```java
package com.erick.multithread.d05;

import java.util.concurrent.TimeUnit;

public class Demo04 {
    public static void main(String[] args) throws InterruptedException {
        TomService service = new TomService();
        new Thread(() -> service.work()).start();
        TimeUnit.SECONDS.sleep(2);
        new Thread(() -> service.changeValue()).start();
    }
}

class TomService {

    private boolean firstFlag = false;
    private volatile int age = 0;

    private boolean secondFlag = false;

    /**
     * 写屏障： age是volatile修饰，在age下面加上写屏障
     * age以及前面的firstFlag，secondFlag等写操作都会同步到主存中
     */
    public void changeValue() {
        firstFlag = true;
        secondFlag = true;
        age = 10;
        /*--------写屏障-------*/
    }


    /**
     * 读屏障： age是volatile修饰，在age上加上读屏障
     * 读屏障下面的所有变量，都是从主存中加载
     */
    public void work() {
        while (true) {
            /*--------读屏障--------*/
            int w_age = age;
            boolean w_firstFlag = firstFlag;
            boolean w_secondFlag = secondFlag;
            if (w_age == 10 && w_firstFlag && w_secondFlag) {
                break;
            }
        }
    }
}
```

# 不可变类

## 1. 日期类

### 1.1 SimpleDateFormat

#### 线程不安全

```java
package com.erick.multithread.d07;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Demo03 {
    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    Date parse = sdf.parse("2022-03-12");
                    System.out.println(parse); // 最终结果不一致，或者出现异常
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
}
```

#### 加锁解决

- 性能会受到影响

```java
package com.erick.multithread.d07;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Demo03 {
    private static Object lock = new Object();

    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    synchronized (lock) {
                        Date parse = sdf.parse("2022-03-12");
                        System.out.println(parse); 
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
}
```

### 1.2. DateTimeFormatter

- JDK8之后提供了线程安全的类

```java
package com.erick.multithread.d07;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class Demo04 {
    public static void main(String[] args) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TemporalAccessor parse = dtf.parse("2022-09-12");
                    System.out.println(parse);
                }
            }).start();
        }
    }
}
```

## 2.不可变类

- 不可变类是线程安全的
- 类中所有成员变量都是final修饰，保证不可变，保证只能读不能写
- 类是final修饰，不会因为错误的继承来重写方法，导致了可变

```bash
# String类型： 不可变类
- 里面所有的Field都是final修饰的，保证了不可变，不可被修改：  private final byte[] value;
- 类被final修饰，保证了String类不会被继承

# 数组保护性拷贝
- 数组类型也是final修饰，如果通过构造传递，实际上是创建了新的数组和对应的String [保护性拷贝]
```

## 3  享元模式

- 对于不可变类，可能需要系统频繁的创建和销毁，对于同一个对象，可以使用享元模式来进行获取和使用

### 3.1 包装类

- Long: 维护了一个静态内部类LongCache, 将一些Long对象进行缓存

```java
// 获取的时候，先从对应的Cache中去找，然后才会创建新的对象
public static Long valueOf(long l) {
    final int offset = 128;
    if (l >= -128 && l <= 127) { // will cache
        return LongCache.cache[(int)l + offset];
    }
    return new Long(l);
}


private static class LongCache {
    private LongCache(){}

    static final Long cache[] = new Long[-(-128) + 127 + 1];

    static {
        for(int i = 0; i < cache.length; i++)
            cache[i] = new Long(i - 128);
    }
}
```

```bash
# 缓存范围
Byte, Short, Long:   -128～127
Character:           0~127
Integer:             -128~127
Boolean:             TRUE和FALSE
```

### 3.2 自定义连接池

```java
package com.erick.multithread.d07;

import lombok.Data;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Demo06 {
    public static void main(String[] args) {
        ConnectionPool pool = new ConnectionPool(2);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                ErickConnection connection = pool.getConnection();
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                pool.release(connection);
            }).start();
        }
    }
}

class ConnectionPool {
    private int pooSize;

    private ErickConnection[] pool;

    /* 1: 当前Connection正在使用
     * 0: 当前Connection空闲*/
    private AtomicIntegerArray states;

    public ConnectionPool(int pooSize) {
        this.pooSize = pooSize;
        pool = new ErickConnection[pooSize];
        states = new AtomicIntegerArray(pooSize);
        for (int i = 0; i < pooSize; i++) {
            pool[i] = new ErickConnection();
        }
    }

    public ErickConnection getConnection() {
        while (true) {
            for (int i = 0; i < pooSize; i++) {
                if (states.get(i) == 0) {
                    states.compareAndSet(i, 0, 1);
                    System.out.println(Thread.currentThread().getName() + "获取连接");
                    return pool[i];
                }
            }
            /*如果没有空闲连接，当前线程进入等待: 如果不加下面，就会造成CPU资源*/
            System.out.println(Thread.currentThread().getName() + "等待连接");
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void release(ErickConnection erickConnection) {
        for (int i = 0; i < pooSize; i++) {
            if (pool[i] == erickConnection) {
                states.set(i, 0);
                System.out.println(Thread.currentThread().getName() + "释放连接");
                synchronized (this) {
                    this.notifyAll();
                }
                break;
            }
        }
    }

}

@Data
class ErickConnection {
    private String username;
    private String password;
}
```



# left

## 5. Park/Unpark

### 5.1. 基本使用

- 先park，再unpark
- park后是waiting状态，会释放锁

```bash
# 暂停当前线程
java.util.concurrent.locks.LockSupport

# 在哪个线程中使用，就暂停哪个线程
public static void park()

# 恢复一个线程
public static void unpark(Thread thread)
```

```java
package com.dreamer.multithread.day04;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Demo01 {
    public static void main(String[] args) throws InterruptedException {
        Thread slaveThread = new Thread("slave-thread") {
            @Override
            public void run() {
                System.out.println("prepare for PARK....");
                LockSupport.park();
                System.out.println("PARK ended");
            }
        };
        slaveThread.start();

        TimeUnit.SECONDS.sleep(2);

        LockSupport.unpark(slaveThread);
    }
}
```

### 5.2. 先unpark后park

- 先unpark，再park，线程就不会停下来了

```java
package com.dreamer.multithread.day04;

import java.util.concurrent.locks.LockSupport;

public class Demo01 {
    public static void main(String[] args) {
        Thread slaveThread = new Thread("slave-thread") {
            @Override
            public void run() {
                LockSupport.unpark(Thread.currentThread());
                System.out.println("prepare for PARK....");
                LockSupport.park();
                System.out.println("PARK ended");
            }
        };
        slaveThread.start();
    }
}
```

### 5.3 wait/park

```bash
# 二者都会使线程进入waitset等待，都会释放锁

wait/notify是Object的方法                    park/unpark是LockSupport
wait/notify 必须和synchronized结合使用        park/unpark不必
wait/notify 顺序不能颠倒                      park/unpark可以颠倒
wait/notify 只能随机唤醒一个或者全部唤醒         park/unpark可以指定一个线程唤醒
```

