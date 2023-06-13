# 多线程简介

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
- 单核cpu下，线程是串行
- 任务调度器： 操作系统组件，将cpu的时间片（windows下为15ms）分给不同的线程使用
- cpu在线程间的切换非常快，感觉就是各个线程是同时运行的
- 微观串行，宏观并行

- concurrent：cpu核心同一个时间应对多个线程的能力
```

### 2.2 并行

```bash
- 多核cpu下，每个核都可以调度运行线程，这个时候线程就是并行的

- parrel： 同一个时间内，cpu真正去执行多个线程的能力
- 很多时候，并发和并行是同时存在的
```

![image-20230428164109578](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230428164109578.png)

## 3. 应用场景

```bash
# 异步调用
- 异步方法：不需立刻返回结果
- 耗时业务：不阻塞主线程的业务

# 提升效率
- 任务拆分：耗时业务拆分，多任务间互不依赖
- 多核cpu： 任务拆分，多线程分别执行，这样就会分到更多的cpu
- 单核cpu： 没必要拆分，串行执行，并有上下文切换的损失
```

## 4. 创建线程

- 启动JVM(main方法)，即开启一个JVM进程
- JVM进程内包含一个主线程，主线程可以派生出多个其他线程。同时还有一些守护线程，如垃圾回收线程
- 主线程，守护线程，派生线程，cpu随机分配时间片，交替随机执行 

### 4.1. 继承Thread类

- 继承 Thread类，重写run()，start()启动线程

```java
package com.erick.multithread.d01;

public class Demo01 {
    public static void main(String[] args) {
        Thread erickThread = new ErickThread();
        erickThread.start();
    }
}

// 直接继承
class ErickThread extends Thread {
    @Override
    public void run() {
        System.out.println("hello");
    }
}
```

```java
package com.erick.multithread.d01;
// 匿名内部类
public class Demo02 {
    public static void main(String[] args) {
        Thread erickThread = new Thread() {
            @Override
            public void run() {
                System.out.println("hello");
            }
        };
        erickThread.start();
    }
}
```

### 4.2 实现Runnable接口

- 实现Runnable接口，重写Runnable的run方法，并将该对象作为Thread构造方法的参数传递

```bash
# Runnable优点 
- 线程和任务分开了，更加灵活
- 容易和线程池相关的API结合
- 让任务脱离了继承体系，更加灵活，避免单继承
```

```java
// 实现Runnable接口
package com.erick.multithread.d01;

public class Demo03 {
    public static void main(String[] args) {
        Thread thread = new Thread(new MyThread());
        thread.start();
    }
}

class MyThread implements Runnable {
    @Override
    public void run() {
        System.out.println("hello");
    }
}
```

```java
// 匿名内部类
package com.erick.multithread.d01;

public class Demo04 {
    public static void main(String[] args) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("hello");
            }
        });

        thread.start();
    }
}
```

### Runnable/Thread

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

### 4.3 FutureTask接口

- 可以获取任务执行结果的一个接口

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
package com.nike.erick.d01;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class Demo06 {
    public static void main(String[] args) {

        FutureTask<String> futureTask = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                System.out.println("slave-thread running");
                TimeUnit.SECONDS.sleep(2);
                return "success from erick";
            }
        });

        Thread thread = new Thread(futureTask, "erick-thread");
        thread.start();

        try {
            /*获取结果的时候，会将主线程阻塞*/
            System.out.println("slave-thread result： " + futureTask.get());
            System.out.println("slave-thread result: " + futureTask.isCancelled());
            System.out.println("slave-thread result: " + futureTask.isDone());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Main Thread ending");
    }
}
```

## 5. 进程查看

### 5.1. Linux

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

### 5.2 Java

```bash
# JPS
- 查看java进程

# JConsole
- Java内置，检测java线程的图形化界面工具
- 位于jdk/bin/目录下
- 可以用来检测死锁
```

## 6. 守护线程

- 守护线程： 只要其他非守护线程运行结束了，即使守护线程的代码没执行完，也会强制退出。如垃圾回收器

```java
package com.erick.multithread.d01;

public class Demo05 {
    public static void main(String[] args) throws InterruptedException {
        Thread demonThread = new Thread() {
            @Override
            public void run() {
                System.out.println("demon start");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("demon end");
            }
        };
        demonThread.setDaemon(true);
        demonThread.start();
        Thread.sleep(4000);
        System.out.println("main end");
    }
}
```

## 7. Thread Context Switch

- cpu不再执行当前线程，转而执行另一个线程代码
- Thread Context Switch时，操作系统保存当前线程的状态，并恢复另一个线程的状态
- Thread Context Switch频繁发生会影响性能

```bash
- 线程cpu时间片用完
- 垃圾回收：                 # STW
- 有更高优先级的线程需要运行
- 线程自己调用了sleep，yield，wait，join，park，synchronized，lock等方法
```

# 常见方法

## 1. start/run

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

## 2. sleep/yield

### 2.1 sleep

- 不释放线程锁

```bash
# public static native void sleep(long millis) throws InterruptedException
- 线程放弃cpu，从RUNNABLE 进入 TIMED_WAITING状态
- 睡眠中的线程可以自己去打断自己的休眠
- 睡眠结束后，会变为RUNNABLE，并不会立即执行，而是等待cpu时间片的分配
```

```java
package com.erick.multithread;

import java.util.concurrent.TimeUnit;

public class Demo06 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    
                }
                System.out.println("thread end");
            }
        };
        System.out.println(thread.getState()); // NEW
        thread.start();
        System.out.println(thread.getState()); // RUNNABLE
        TimeUnit.SECONDS.sleep(2);
        System.out.println(thread.getState()); // TIMED_WAITING
        thread.interrupt(); // 自己打断自己的睡眠
    }
}
```

### 2.2 yield

```bash
# public static native void yield()

- 线程让出cpu资源，让其他高优先级的线程先去执行
- 让线程从RUNNING状态转换为RUNNABLE状态
- 假如其他线程不用cpu，那么cpu又会分配时间片到当前线程，可能压根就没停下
```

### 2.3 priority

- 只是一个CPU参考值，高优先级的会被更多的分到时间资源

```java
package com.erick.multithread.d01;

import static java.lang.Thread.MAX_PRIORITY;
import static java.lang.Thread.MIN_PRIORITY;

public class Demo07 {
    public static void main(String[] args) {
        Thread firstThread = new Thread() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    i++;
                    System.out.println("T1:" + i);
                }
            }
        };

        Thread secondThread = new Thread() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    i++;
                    System.out.println("-------------->> T2:" + i);
                }
            }
        };

        firstThread.setPriority(MIN_PRIORITY);
        secondThread.setPriority(MAX_PRIORITY);
        firstThread.start();
        secondThread.start();
    }
}
```

### 2.4 sleep应用

- 程序一直执行，浪费cpu资源
- 程序间歇性休眠，让出cpu资源， 避免空转

```java
package com.erick.multithread.d01;

import java.util.concurrent.TimeUnit;

public class Demo08 {
    public void method01() {
        while (true) {
            System.out.println("do...");
        }
    }

    public void method02() throws InterruptedException {
        while (true) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println("do..");
        }
    }
}
```

## 3. join 

- 同步等待其他线程的结果调用

### 3.1. join()

```bash
# public final void join() throws InterruptedException
- xxx.join()，在调用的地方，等待xxx执行结束后再去运行当前线程
```

```java
package com.erick.multithread.d02;

import java.util.concurrent.TimeUnit;

public class Demo01 {
    static int number = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread firstThread = new Thread() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                number = 5;
            }
        };

        Thread secondThread = new Thread() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                number = 10;
            }
        };

        System.out.println(number); // 0;

        firstThread.start();
        secondThread.start();
        long start = System.currentTimeMillis();
        /**
         * 1. 两个线程同时插队，以相同优先级执行
         * 2. 所以一共等待时间为3s
         */
        firstThread.join();
        secondThread.join();
        System.out.println(number); // 10
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
```

### 3.2 join(long millis)

```bash
# public final synchronized void join(long millis) throws InterruptedException
- 有时效等待：最多等待多少ms， 0 代表等待到任务结束
- 假如线程join的等待时间超过了实际执行时间，执行完后就可以不用继续等了
```

```java
package com.erick.multithread.d02;

import java.util.concurrent.TimeUnit;

public class Demo02 {
    static int number = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                number = 10;
            }
        };
        thread.start();
        thread.join(1000);
        System.out.println(number);
    }
}
```

## 4. interrupt

```bash
# 打断线程，谁调用打断谁
- public void interrupt()

# 判断线程是否被打断 : 默认为false, 不会清除打断标记
- public boolean isInterrupted()
       
# 判断线程是否被打断:  会将线程打断标记置为false
- public static boolean interrupted()
```

### 4.1 打断阻塞线程

- 打断线程，抛出nterruptedException，将打断标记从true重置为false（需要一点缓冲时间）
- 如sleep，join，wait的线程

```java
package com.erick.multithread.d02;

import java.util.concurrent.TimeUnit;

public class Demo03 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    // 被打断后就会抛出InterruptedException进入catch
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        thread.start();
        System.out.println(thread.isInterrupted()); // false
        TimeUnit.SECONDS.sleep(2);
        thread.interrupt();
        // 如果不是true，则可以稍微留点缓冲时间
        System.out.println(thread.isInterrupted()); // true
    }
}
```

### 4. 2 打断正常线程

 - 正常线程运行时，收到打断，继续正常运行, 但打断信号变为true

```java
package com.erick.multithread.d02;

import java.util.concurrent.TimeUnit;

public class Demo04 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    System.out.println("hello");
                }
            }
        };

        thread.start();
        TimeUnit.SECONDS.sleep(1);
        thread.interrupt();
    }
}
```

- 可以通过打断标记来进行普通线程的打断控制

```java
package com.erick.multithread.d02;

import java.util.concurrent.TimeUnit;

public class Demo05 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    System.out.println("hello");
                }
            }
        };
        thread.start();
        TimeUnit.SECONDS.sleep(2);
        thread.interrupt();
    }
}
```

### 4.3 Two Phase Termination

- 终止正常执行的线程：留下料理后事的机会
- 两阶段：正常线程阶段和阻塞线程阶段
- 业务场景：定时任务线程，如果被打断，则中止任务

```java
class ScheduleTask implements Runnable {

    @Override
    public void run() {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                clear();
                break;
            }
            
            interval();
            doJob();
        }
    }

    /*阶段一： 业务代码: 可能被打断*/
    public void doJob() {
        System.out.println("working");
    }

    /*阶段二： 等待间隔: 可能被打断*/
    public void interval() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            // 被打断时，打断标记被重制为false，需要调用该方法将打断标记置为true
            Thread.interrupted();
            throw new RuntimeException(e);
        }
    }

    public void clear() {
        System.out.println("ending task");
    }
}
```

### 4.4  打断park线程

- LockSupport:   public static void park() 
- park当前的线程: 线程一直生存，直到被打断才会继续往下执行
- 被打断后，打断标记就会变为true，就不能二次park了

#### 单次park

```java
package com.nike.erick.d02;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Demo07 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("thread start running....");

                // 将当前线程停下来
                LockSupport.park();
                System.out.println("after first park...");
            }
        });

        thread.start();
        TimeUnit.SECONDS.sleep(2);
        // 打断后就会继续执行
        thread.interrupt();
    }
}
```

#### 多次park

```java
package com.nike.erick.d02;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class Demo08 {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("slave thread running");
                LockSupport.park();
                System.out.println("after first park...");

                // 获取当前线程的打断标记，同时将打断标记清除，即为 false
                System.out.println("打断标记：" + Thread.interrupted());

                LockSupport.park(); // 再次park
                System.out.println("after second park...");
            }
        });

        thread.start();
        TimeUnit.SECONDS.sleep(1);
        thread.interrupt();
        TimeUnit.SECONDS.sleep(3);
        thread.interrupt();
    }
}
```

## 5. 其他方法

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
- cpu的时间片只会分给可运行状态的线程，阻塞状态的需要其本身阻塞结束

![image-20220925095436125](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220925095436125.png)

### NEW 

- new 出一个Thread的对象时，线程并没有创建，只是一个普通的对象
- 调用start，new 状态进入runnable状态

### RUNNABLE

- 调用start，jvm中创建新线程，但并不立即执行，只是处于就绪状态
- 等待cpu分配时间片，轮到它时，才真正执行

### RUNNING

- 一旦cpu调度到了该线程，该线程真正执行

```bash
# 该状态的线程可以转换为其他状态
- 进入BLOCK：        sleep， wait，阻塞IO如网络数据读写， 获取某个锁资源
- 进入RUNNABLE：     cpu轮询到其他线程，线程主动yield放弃cpu
```

### BLOCK

```bash
# 该状态的线程可以转换为其他状态
- 进入RUNNABLE：      线程阻塞结束，完成了指定时间的休眠
                     wait中的线程被其他线程notify/notifyall
                     获取到了锁资源
```

### TERMINATED

-  线程正常结束
-  线程运行出错意外结束
-  jvm crash,导致所有的线程都结束

## 2. JAVA层面

- 根据Thread类中内部State枚举，分为六种状态

```
NEW     RUNNABLE    BLOCKED    WAITING    TIMED_WAITING   TERMINATED
```



# 线程安全

## 1. 不安全

### 1.1 共享变量的写

- 多线程对共享变量的并发修改

```java
package com.erick.multithread.d03;

import lombok.Getter;

public class Demo01 {
    public static void main(String[] args) throws InterruptedException {
        ErickService service = new ErickService();

        Thread first = new Thread(() -> service.add());
        Thread second = new Thread(() -> service.minus());
        first.start();
        second.start();
        first.join();
        second.join();
        System.out.println(service.getNumber());
    }
}

class ErickService {
    @Getter
    private int number;

    public void add(){
        for (int i = 0; i < 1000000; i++) {
            number++;
        }
    }

    public void minus(){
        for (int i = 0; i < 1000000; i++) {
            number--;
        }
    }
}
```

### 1.2 原因

#### 字节码

- 线程拥有自己的栈内存，读数据时会堆主内存中拿，写完后会将数据写回堆内存
- i++在实际执行的时候，是一系列指令，一系列指令就会导致指令交错

![image-20230428164030859](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230428164030859.png)

#### 指令交错

- 存在于多线程之间
- 线程上下文切换，引发不同线程内指令交错，最终导致上述操作结果不会为0

![image-20230428164425388](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230428164425388.png)

#### 不安全

- 线程不安全： 只会存在于多个线程共享的资源
- 临界区：  对共享资源的多线程读写操作的代码块
- 竞态条件： 多个线程在在临界区内，由于代码的指令交错，对共享变量资源的争抢

```java
多线程  读    共享资源  没问题
多线程  读写  共享资源  可能线程不安全(指令交错)
```

## 2. synchronized

### 2.1 同步代码块

- 对象锁：只要为同一个对象，为任意对象(除基本类型）
- 对象锁：必须用final修饰，这样保证对象的引用不可变，从而确保是同一把锁

```bash
- 阻塞式的，解决多线程访问共享资源引发的不安全的解决方案
- 可在不同代码粒度进行控制
- 保证了《临界区代码的原子性(字节码)》，不会因为线程的上下文切换而被打断
- 必须保证对对同一个对象加锁(Integer.value(0))

# 原理
1. a线程获取锁，执行代码，并执行临界区代码
2. b线程尝试获取锁，无法获取锁资源，被block，进入  《等待队列》，同时进入上下文切换
3. a线程执行完毕后，释放锁资源。唤醒其他线程，进行cpu的争夺
```

```java
class ErickService {
    @Getter
    private int number;
    
    private final Object lock = new Object();

    public void add(){
        /*可以在不同粒度进行加锁， 锁对象可以为任意对象，比如lock
          一般会使用this作为锁
        * */
        synchronized (this){
            for (int i = 0; i < 1000000; i++) {
                number++;
            }
        }
    }

    public void minus(){
        synchronized (this){
            for (int i = 0; i < 1000000; i++) {
                number++;
            }
        }
    }
}
```

### 2.2 同步方法

- 如果多个线程对共享变量读写，但是部分线程没有加锁保护，依然线程不安全

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

### 2.3 犹豫模式

- 一个线程发现另一个线程已经做了某事，那么该线程就无需再做
-  犹豫模式： synchronized对doJob()加锁

```java
package com.erick.multithread.d03;

import com.erick.multithread.util.Sleep;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Demo02 {
    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        BalkingService service = new BalkingService();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> service.doJob());
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println(service.getJobTimes());
    }
}

class BalkingService {
    @Getter
    private int jobTimes;

    private boolean isJobDone = false;

    /*可以通过同步方法或同步代码块来解决*/
    public void doJob() {
        if (isJobDone) {
            return;
        }
        Sleep.sleep(1);
        jobTimes++;
        isJobDone = true;
    }
}
```



## 3. 线程安全场景

### 3.1 成员变量&静态成员变量

```bash
1. 没被多线程共享：        则线程安全
2. 被多线程共享：
     2.1 如果只读，   则线程安全
     2.2 如果有读写， 则可能发生线程不安全问题
```

### 3.2 局部变量

#### 线程安全

- 每个线程方法都会创建单独的栈内存，局部变量保存在自己当前方法的栈桢内
- 局部变量线程私有

```bash
- 基础数据类型时： 安全
- 引用类型时：    可能不安全
  - 如果该对象没有逃离方法的作用访问，则线程安全
  - 如果该对象逃离方法的作用范围，则可能线程不安全 《引用逃离》
 
# 避免局部变量线程安全类变为不安全类： 不要让一个类的方法被重写
- final修饰类禁止继承，或对可能引起安全的方法加private
```

#### 引用逃离

- 一个类不是final类，那么就可能被继承
- 被继承的时候发生方法覆盖，覆盖方法如果创建新的线程，就可能发生局部变量不安全
- 本质：一个类被一个线程使用时候，类中的方法被不正确的覆盖，从而又启动了新的线程

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

### 3.3 线程安全类

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





# Java锁

## 1. 对象头

- 以32位的 JVM 为例，Java对象的对象头都包含如下信息

```bash
# 组成
Mark Word:      锁信息，hashcode，垃圾回收器标志
Klass Word:     指针，包含当前对象的Class对象的地址

# 普通对象对象头，占用8个字节，64位
 Mark Word(32 bits)        Klass Word(32 bits)

# 数组对象对象头， 占用12个字节， 96位      包含额外的4个字节用来保存数组长度
                  Object Header (96 bits)
 Mark Word(32 bits)       Klass Word(32 bits)      Array Length (32 bits)


- 01/00/11：      代表是否加锁
- age：           垃圾回收器标记
```

![image-20220926112525895](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220926112525895.png)

## 2. Monitor

### 2.1  monitor

```bash
# 操作系统 提供，又叫监视器或管程，可包含多个不同的Monitor
# 包含三部分
   1. Owner：      保存当前获取到java锁的，线程指针
   2. EntryList:   保存被java锁阻塞的，线程指针
   3. WaitSet:     保存被java锁等待的，线程指针

# synchronized 的java对象，该对象会被关联到一个monitor监视器，java对象头的Mark Word就被设置为 monitor监视器的地址
- synchronized修饰的java对象， 重量级锁，不公平锁
```

![image-20220926113801439](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220926113801439.png)

### 2.2 竞争步骤

- java对象被synchronized修饰后
- 当它获取到对象锁的时候，该对象就会被关联到monitor，java对象的Mark Word就会变为  **prt_to_heavyweight_monitor**， 即保存monitor的地址，同时mark word中的01转变为10

```bash
1. thread-1 通过synchronized获取到一个obj对象
  1.1 obj对象头信息(hashcode,age等)变为prt_to_heavyweight_monitor(30 bit)(monitor指针)
  1.2 obj对象头的锁状态变为 10（重量级锁）
  1.3 根据monitor指针，找到monitor，将Owner设置为thread-1
   
2. thread-2 过来后，检查obj锁对象头
   2.1 发现该obj对象头的Mark Word的锁状态已经是重量级锁
   2.2 根据Mark Word中锁的地址检查当前Owner已经有其他线程了
   2.3 thread-2进入到EntryList，进行Block
  
3. thread-1 执行完临界区代码后，
      3.1 monitor的Owner进行清空
      3.2 将owner中的当前线程的owner和obj对象头中的monitor地址再次交换
      3.3 monitor唤醒EntryList中其他线程
      3.4 其他在 EntryList 中等待的线程， 再次竞争对象锁，再次设置monitor的Owner

- synchronized(obj)，就会有一个monitor监管该对象
- 同步代码块发生异常时，将锁释放
- synchronized(obj), 必须关联到同一个obj，不然就不会指向同一个monitor
```

![image-20220926120606596](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220926120606596.png)

## 3. 常见锁

### 3.1 轻量级锁

- 锁对象虽被多个线程都来获取，但访问时间错开，不存在竞争
- 轻量级锁对使用者 是透明的， 语法：syncronized
- 当存在其他线程竞争的时候，自动升级为重量级锁

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

### 3.3 锁膨胀

- 在尝试轻量级加锁时，cas无法成功
- 可能因为：其他线程为此对象加上了轻量级锁(有竞争)，这时进行锁膨胀，锁变为重量级锁
- 轻量级锁没有阻塞机制，重量级锁有阻塞机制

![image-20220928205845097](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220928205845097.png)

```bash
# 加锁
- thread-0轻量级锁加锁成功
- 当thread-1进行轻量级加锁时，thread-0已经为该对象加了轻量级锁，对应的java object是00
- thread-1轻量级加锁失败，进入了锁膨胀流程

# 锁膨胀
- 为Object对象申请monitor锁，并让Object的mark word 指向重量级锁地址, 同时变为10(重量级锁)
- 然后自己进入monitor的EntryList 进行 Block

# 解锁
- 当Thread-0 退出同步块时，使用cas将Mark Word的值恢复给对象头，失败进入重量级解锁流程
- 按照Monitor地址找到Monitor，设置Owner为null，唤醒EntryList中BLOCKED线程
```

### 3.4 自旋优化-重量级锁

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

## 4. 锁消除

```bash
- Java的 JIT, 对热点代码进行优化
- 逃逸分析： JVM  是根据锁对象是否可以发生逃逸分析来判断
- JVM默认开启锁消除机制
- Java中锁消除默认是打开的，会根据代码中锁关联的对象是否能够逃逸决定是否优化
- 关闭锁消除： java -XX: -EliminateLocks -jar demo.jar
```

```java
package com.erick.multithread.d02;

public class Demo07 {
    public static void main(String[] args) {
        LockEscapeAnalysis analysis = new LockEscapeAnalysis();
        /*jdk8结果：  3, 1373, 1096
          jdk11结果： 1, 101, 1785*/
        System.out.println(analysis.firstMethod());  // 1
        System.out.println(analysis.secondMethod()); // 101
        System.out.println(analysis.thirdMethod());  // 1785
    }
}

class LockEscapeAnalysis {

    public long firstMethod() {
        int number = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            number++;
        }
        return System.currentTimeMillis() - start;
    }

    /*锁消除： 锁是一个局部变量，且局部变量不会发生逃逸，因此会被优化掉*/
    public long secondMethod() {
        int number = 0;
        long start = System.currentTimeMillis();
        Object lock = new Object();
        for (int i = 0; i < 100000000; i++) {
            synchronized (lock) {
                number++;
            }
        }
        return System.currentTimeMillis() - start;
    }


    /*在当前线程调用方法的时候，又开启了新的线程来争抢这把锁,因此不会锁消除*/
    public long thirdMethod() {
        int number = 0;
        Object lock = new Object();
        new Thread(() -> {
            synchronized (lock) {
                System.out.println("hello");
            }
        }).start();

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            synchronized (lock) {
                number++;
            }
        }
        return System.currentTimeMillis() - start;
    }
}
```

## 5. 多锁

- 如果不同方法访问的不是同一个共享资源，则尽可能提供多把锁，否则会降低并发度
- 共享同一个资源的方法，才让他们去使用同一把锁

```java
package com.erick.multithread.d02;

import com.erick.multithread.util.Sleep;

import java.util.ArrayList;
import java.util.List;

public class Demo08 {
    public static void main(String[] args) throws InterruptedException {
        BigRoom bigRoom = new BigRoom();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Thread firstKind = new Thread(() -> bigRoom.firstJob());
            Thread secondKind = new Thread(() -> bigRoom.secondJob());
            threads.add(firstKind);
            threads.add(secondKind);
            firstKind.start();
            secondKind.start();
        }

        long start = System.currentTimeMillis();
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println(System.currentTimeMillis() - start); // 6s
    }
}

class BigRoom {
    private int firstNumber = 0;
    private int secondNumber = 0;

    public void firstJob() {
        synchronized (this) {
            Sleep.sleep(1);
            firstNumber++;
        }
    }

    public void secondJob() {
        synchronized (this) {
            Sleep.sleep(1);
            firstNumber++;
        }
    }
}
```

```java
package com.erick.multithread.d02;

import com.erick.multithread.util.Sleep;

import java.util.ArrayList;
import java.util.List;

public class Demo08 {
    public static void main(String[] args) throws InterruptedException {
        BigRoom bigRoom = new BigRoom();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Thread firstKind = new Thread(() -> bigRoom.firstJob());
            Thread secondKind = new Thread(() -> bigRoom.secondJob());
            threads.add(firstKind);
            threads.add(secondKind);
            firstKind.start();
            secondKind.start();
        }

        long start = System.currentTimeMillis();
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println(System.currentTimeMillis() - start); // 3s
    }
}

class BigRoom {
    private int firstNumber = 0;
    private int secondNumber = 0;

    private Object firstLock = new Object();

    private Object secondLock = new Object();

    public void firstJob() {
        synchronized (firstLock) {
            Sleep.sleep(1);
            firstNumber++;
        }
    }

    public void secondJob() {
        synchronized (secondLock) {
            Sleep.sleep(1);
            firstNumber++;
        }
    }
}
```

## 6. 死锁

- 如果多把锁，被一个方法同时使用了，可能造成死锁

```bash
- 线程一：持有a锁，等待b锁
- 线程二：持有b锁，等待a锁
- 互相等待引发的死锁问题
- 哲学家就餐问题
- 定位死锁: 可以借助jconsole来定位死锁
- 解决方法： 按照相同顺序加锁就可以，但可能引发饥饿问题
```

```java
package com.dreamer.multithread.day04;

import java.util.concurrent.TimeUnit;

public class Demo02 {
    public static void main(String[] args) {
        BigRoom room = new BigRoom();
        new Thread(() -> room.sleepAndWork()).start();
        new Thread(() -> room.workAndSleep()).start();
    }
}

class BigRoom {

    private final Object sleepLock = new Object();
    private final Object workLock = new Object();

    // 互相持有对方的锁
    public void sleepAndWork() {
        synchronized (sleepLock) {
            consumeTime();
            synchronized (workLock) {
                System.out.println("睡醒---工作啦");
            }
        }
    }

    public void workAndSleep() {
        synchronized (workLock) {
            consumeTime();
            synchronized (sleepLock) {
                System.out.println("工作后--要睡觉啦¬");
            }
        }
    }

    private void consumeTime() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

```

## 7. 饥饿锁

- 某个线程因为优先级太低，一直得不到cpu的执行

## 8. 活锁

- 两个线程中互相改变对方结束的条件，导致两个线程一直运行下去
- 可能会结束，但是二者可能会交替进行

```java
package com.erick.multithread.d03;

public class Demo03 {
    static int number = 0;

    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                while (number < 50) {
                    number++;
                    System.out.println("+++++++" + number);
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                while (number > 0) {
                    number--;
                    System.out.println("            ---------" + number);
                }
            }
        }.start();
    }
}
```

# Wait/Notify

- 线程在加锁执行时，发现条件不满足，则进行wait，同时释放锁
- 条件满足后，通过notify来唤醒，同时继续竞争锁

## 1. 基本使用

### 1.1 API

- Object类的方法，必须成为锁的owner时候才能使用
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

### 1.2 原理

- Owner线程发现条件不满足，调用当前锁的wait，进入WaitSet变为WAITING状态
- wait释放当前锁资源

```bash
1. BLOCK和WAITING的线程都处于阻塞状态，不占用cpu
2. BLOCK线程会在Owner线程释放锁时唤醒
3. WAITING线程会在Owner线程调用notify时唤醒，但唤醒后只是进入EntryList重新竞争锁
```

![image-20220929091827634](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220929091827634.png)

```java
package com.erick.multithread.d03;

import java.util.concurrent.TimeUnit;

public class Demo04 {
    public static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        new Thread() {
            @Override
            public void run() {
                synchronized (lock) {
                    System.out.println("first coming");
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("first ending");
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                synchronized (lock) {
                    System.out.println("second coming");
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("second ending");
                }
            }
        }.start();

        TimeUnit.SECONDS.sleep(2);
        synchronized (lock){
            lock.notifyAll();
        }
    }
}
```

- 要wait或者notify，必须先获取到锁资源

```java
package com.nike.erick.d02;

public class Demo02 {
    private static Object lock = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                /*不能直接wait，要先获取到锁资源
                * IllegalMonitorStateException */
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
```

### 1.3  wait vs sleep

```bash
1. Wait 是Object的方法，调用lock的方法                     Sleep 是Thread 的静态方法
2. Wait 必须和synchronized结合使用                        Sleep 不需要
3. Wait 会放弃当前线程的锁资源                             Sleep 不会释放锁（如果工作时候带锁）
4. 都会让出cpu资源，状态都是Timed-Waiting
```

## 2. 虚假唤醒解决

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
package com.erick.multithread.d03;

import java.util.concurrent.TimeUnit;

public class Demo05 {

    private static final Object lock = new Object();
    private static boolean hasCigarette = false;

    public static void main(String[] args) throws InterruptedException {
        new Thread() {
            @Override
            public void run() {
                synchronized (lock) {
                    while (!hasCigarette) {
                        System.out.println("没有烟，休息一下");
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("烟来了，干活");
                }
            }
        }.start();

        for (int i = 0; i < 5; i++) {
            new Thread(){
                @Override
                public void run() {
                    synchronized (lock){
                        System.out.println(Thread.currentThread().getName()  + "干活");
                    }
                }
            }.start();
        }

        TimeUnit.SECONDS.sleep(2);
        synchronized (lock){
            hasCigarette = true;
            System.out.println("烟来了");
            lock.notify();
        }
    }
}
```

## 3. 保护性暂停

- 一个线程等待另一个线程的一个执行结果，即同步模式

```bash
- 一个结果需要从一个线程传递到另一个线程，让两个线程关联同一个GuardedObject
- JDK中， join的实现，Future的实现，就是采用保护性暂停
```

![image-20220929120241558](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220929120241558.png)

```java
package com.erick.multithread.d03;

import com.erick.multithread.util.Sleep;

import java.util.concurrent.TimeUnit;

public class Demo04 {
    public static void main(String[] args) {
        GuardObject object = new GuardObject();
        new Thread(() -> {
            Object result = object.getResult(TimeUnit.SECONDS, 1L);
            System.out.println(result);
        }).start();

        Sleep.sleep(3);
        new Thread(() -> object.setResult()).start();
    }
}

class GuardObject {
    private Object result;

    public synchronized void setResult() {
        Sleep.sleep(1); // 传输结果前的准备工作
        result = new Object();
        this.notify();
    }

    /*无限等待*/
    public synchronized Object getResult() {
        while (result == null) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /*超时等待，获取不到结果就返回了*/
    public synchronized Object getResult(TimeUnit unit, long timeout) {
        long start = System.currentTimeMillis();
        long passedTime = 0; /*经历了多长时间*/
        timeout = unit.toMillis(timeout);
        while (result == null) {
            long leftTime = timeout - passedTime;
            if (leftTime <= 0) {
                return null; /*如果剩余时间小于0了，则直接返回*/
            }
            try {
                this.wait(leftTime);/*动态等待*/
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            /*被虚假唤醒时候*/
            passedTime = System.currentTimeMillis() - start;
        }
        return result;
    }
}
```

## 4. 消息队列

- 结果从一类线程不断传递到其他类线程，使用消息队列(生产者消费者)
- 多个生产者及消费者， 阻塞队列， 异步消费
- 消息队列，先入先得，有容量限制，满时不再添加消息，空时不再消费消息
- JDK中各种**阻塞队列**，就是用的这种方式

![image-20220929180221221](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220929180221221.png)


```java
package com.erick.multithread.d03;

import java.util.LinkedList;

class MessageBroker<T> {
    private int capacity;

    private LinkedList<T> blockingQueue = new LinkedList<>();

    public MessageBroker(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void sendMessage(T data) throws InterruptedException {
        while (blockingQueue.size() >= capacity) {
            System.out.println("队列满，wait");
            this.wait();
        }
        blockingQueue.add(data);
        this.notifyAll();
    }

    public synchronized T consumeMessage() throws InterruptedException {
        while (blockingQueue.size() == 0) {
            System.out.println("队列空，wait");
            this.wait();
        }
        T data = blockingQueue.removeLast();
        this.notifyAll();
        return data;
    }
}
```

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
package com.erick.multithread.d05;

import lombok.Data;

import java.util.concurrent.TimeUnit;

public class Demo01 {
    public static void main(String[] args) throws InterruptedException {
        // 主存的数据
        ErickService erickService = new ErickService();
        new Thread(() -> erickService.deal(), "t1").start();
        TimeUnit.SECONDS.sleep(2); // 必须缓存一段时间，让JIT做从主存到工作内存的缓存优化
        new Thread(() -> erickService.setFlag(false), "t2").start();
    }
}

@Data
class ErickService {
    private boolean flag = true;

    public void deal() {
        while (flag) {
           
        }
    }
}
```

```bash
# 初始状态
- 初始状态，变量flag被加载到主存中
- t1线程从主存中读取到了flag
- t2线程要频繁从主存中读取数据,经过多次的循环后，JIT进行优化
- JIT编译器会将flag的值缓存到当前线程的栈帧工作内存中的高速缓存中(栈内存)，减少对主存的访问，提高性能
 
 # 2秒后
- 主存中的数据被t2线程修改了，但是t1线程还是读取自己工作内存的数据，并不会去主存中去拿
```

![image-20230430170254485](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230430170254485.png)

### 1.2 解决方案-volatile

- 轻量级锁

```bash
- 修饰成员变量或者静态成员变量
- 线程获取该变量时，不会从自己的工作内存中去读取，每次都是去主存中读取
- 牺牲了性能，保证了一个线程改变主存中某个值时，对于其他线程不可见的问题
```

```java
package com.erick.multithread.d05;

import lombok.Data;

import java.util.concurrent.TimeUnit;

public class Demo01 {
    public static void main(String[] args) throws InterruptedException {
        // 主存的数据
        ErickService erickService = new ErickService();
        new Thread(() -> erickService.deal(), "t1").start();
        TimeUnit.SECONDS.sleep(2);
        new Thread(() -> erickService.setFlag(false), "t2").start();
    }
}

@Data
class ErickService {
    private volatile boolean flag = true;

    public void deal() {
        while (flag) {
        }
    }
}
```

### 1.3 解决方案-synchronized

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
package com.erick.multithread.d05;

import lombok.Data;

import java.util.concurrent.TimeUnit;

public class Demo01 {
    public static void main(String[] args) throws InterruptedException {
        // 主存的数据
        ErickService erickService = new ErickService();
        new Thread(() -> erickService.deal(), "t1").start();
        TimeUnit.SECONDS.sleep(2);
        new Thread(() -> erickService.setFlag(false), "t2").start();
    }
}

@Data
class ErickService {
    private boolean flag = true;

    public void deal() {
        while (true) {
            synchronized (this) {
                if (!flag) {
                    break;
                }
            }
        }
    }
}
```



## 2. 有序性-指令重排

### 2.1 计组思想

```bash
- 每条指令划分为    取指令 --- 指令译码 --- 执行指令 --- 内存访问 --- 数据回写
- 计组思想 并不能提高单个指令的执行时间，但是变相的提高了吞吐量
```

![image-20230430145005362](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230430145005362.png)

![image-20230430145021608](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230430145021608.png)


### 2.2  指令重排

- 一行代码对应字节码可能分为若干个指令
- JVM在不影响性能的情况下，会对代码对应的字节码指令执行顺序进行重排

```bash
# 可以重排的场景：重排序，不会对结果产生影响
int a = 1；
int b = 2;
System.out.println(a+b);

# 不能重排的场景：不可重排，重排后就会发生错误
int a = 10;
int b = a-1;
```

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

## 3. 原子性-指令交错

- 多线程访问共享资源，指令交错引发的原子性问题
- volatile：不保证
- synchronized：能解决

## 4. 读写屏障

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

|                        | synchronized             | volatile       |
| ---------------------- | ------------------------ | -------------- |
| 锁级别                 | 重量级锁                 | 轻量级锁       |
| 可见性                 | 可解决                   | 可解决         |
| 原子性(多线程指令交错) | 可解决                   | 不保证         |
| 有序性(指令重排)       | 可以重排，但不会出错     | 可以禁止重排   |
| 场景                   | 一个线程修改，其他线程读 | 多线程并发修改 |

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

# CAS

## 1. 线程不安全

- 可以通过 悲观锁-synchronized，来实现线程间的互斥，实现线程安全
- 也可以通过无锁并发CAS实现

```java
package com.erick.multithread.d03;

import com.erick.multithread.util.Sleep;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Demo05 {
    public static void main(String[] args) throws InterruptedException {
        BankService service = new BankService();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Thread thread = new Thread(() -> service.drawMoney());
            threads.add(thread);
            thread.start();
        }
        for (Thread thread: threads){
            thread.join();
        }
        System.out.println(service.getTotalMoney());
    }
}

class BankService {
    @Getter
    private int totalMoney = 10;

    public void drawMoney() {
        if (totalMoney <= 0) {
            System.out.println("no money left");
            return;
        }
        Sleep.sleep(1);
        totalMoney--;
    }
}
```

## 2.  乐观锁-CAS 

- 不加锁实现共享资源的保护
- Compare And Set
- JDK提供了对应的CAS类来实现不加锁

### 2.1 CAS原生API

```bash
AtomicInteger

private volatile int value;

# 构造方法
public AtomicInteger(int initialValue) {
    value = initialValue;
}

# 1. 获取最新值
public final int get();

# 2. 比较，交换
public final boolean compareAndSet(int expectedValue, int newValue)
```

```java
package com.erick.multithread.d03;

import com.erick.multithread.util.Sleep;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo06 {
    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        SafeBankService service = new SafeBankService();
        for (int i = 0; i < 15; i++) {
            Thread thread = new Thread(() -> service.drawMoney());
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println(service.getTotalMoney().get());
    }
}

class SafeBankService {
    @Getter
    private AtomicInteger totalMoney = new AtomicInteger(10);

    public void drawMoney() {
        while (true) {
            int value = totalMoney.get();
            if (value <= 0) {
                System.out.println("no money left");
                return;
            }
            Sleep.sleep(1);
            // 参数一：原来的值。  参数二：变化后的值
            // 原来的值，一旦被其他参数修改，是volatile修饰的，本次修改就会失败，继续下次循环
            boolean result = totalMoney.compareAndSet(value, value - 1);
            if (result) {
                break;
            }
        }
    }
}
```

### 2.2 CAS原理

- CAS 必须和 volatile结合使用
- get()方法获取到的是类的value，被volatile修饰。其他线程修改该变量后，会立刻同步到主存中，方便其他线程的cas操作

```bash
# compareAndSet内部，是通过系统的 lock cmpxchg(x86架构)实现的，也是一种锁

    public final boolean compareAndSet(int expect, int update) {
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }
    
```

![image-20230501162303777](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230501162303777.png)

### 2.3 CAS vs synchronized

```bash
# 1. 解决方式
- CAS:             无锁并发(基于内存可见性，不加锁)，无阻塞并发(不会阻塞，上下文切换少)
- synchronized:    悲观锁，阻塞并发

# 2. 上下文切换
- CAS即使重试失败，线程一直高速运行
- synchronized会让线程在没有获取到锁时，进入EntryList，同时发生上下文切换，进入阻塞，影响性能

# 3. cpu核心
- CAS时，线程一直在运行，如果cpu不够多且当前时间片用完，虽然不会进入阻塞，但依然会发生上下文切换，从而进入可运行状态
- 最好是线程数少于cpu的核心数目

# 4. 乐观锁适应场景
- 如果竞争激烈，重试机制必然频发触发，反而性能会收到影响
```

## 3. 原子整数

- 能够保证修改数据的结果，是线程安全的包装类型

```bash
# 功能类似
java.util.concurrent.atomic.AtomicInteger
java.util.concurrent.atomic.AtomicLong
java.util.concurrent.atomic.AtomicBoolean
```

### 3.1 常用方法

- AtomicInteger的下面方法，都是原子性的，利用了CAS思想，简化代码

```bash
# 1. 自增，自减等操作： 可以基于compareAndSet来实现
# 自增并返回最新结果: 最新结果可能不一定是自增后的值，比如方法执行前值是2，自增只保证增加了1，但是最新结果可能是14
public final int incrementAndGet();
# 获取最新结果并自增: 
public final int getAndIncrement();

public final int decrementAndGet();
public final int getAndDecrement();
public final int getAndAdd(int delta);
public final int addAndGet(int delta);
public final int getAndSet(int newValue);

# 2. 复杂操作，自定义操作： 自定义实现cas，要配合while(true)来使用
public final boolean compareAndSet(int expect, int update);

# 3. 自定义操作的函数式接口，也是基于compareAndSet实现
public final int updateAndGet(IntUnaryOperator updateFunction);
public final int getAndUpdate(IntUnaryOperator updateFunction);
```

### 3.2 手写自定义操作

```java
package com.erick.multithread.d07;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

public class Demo01 {
    public static void main(String[] args) throws InterruptedException {
        ErickBank erickBank = new ErickBank();
        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Thread thread = new Thread(() -> erickBank.drawMoneyThird());
            threadList.add(thread);
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
        System.out.println("leftover:" + erickBank.getAccount().get());
    }
}

@Data
class ErickBank {
    /**
     * 内部维护了一个private volatile int value;
     * 无参构造为0
     */
    private AtomicInteger account = new AtomicInteger(100);

    /**
     * 方式一：CAS自定义操作
     * 每次取4块: 基于CAS
     */
    public void drawMoneyFirst() {
        int value = account.get();
        while (true) {
            if (value < 4) {
                System.out.println("no money left");
                return;
            }
            int nextValue = value - 4;
            if (account.compareAndSet(value, nextValue)) {
                break;
            }
        }
    }

    /**
     * 方式二：函数式接口
     * 让方法能够自定义操作
     */
    public void drawMoneySecond() {
        erickUpdateAndGet(account, operand -> operand - 4);
    }

    /**
     * int applyAsInt(int operand); 自定义接口， 传递一个int，返回一个int
     *
     * @param atomicInteger
     * @param operator
     */
    private void erickUpdateAndGet(AtomicInteger atomicInteger, IntUnaryOperator operator) {
        while (true) {
            int currentValue = atomicInteger.get();
            // 判断必须写在循环里面，不然可能超额支出
            if (currentValue < 4) {
                System.out.println("no money left");
                return;
            }
            // 具体要怎么变化的值，需要调用方自己去定义
            int nextValue = operator.applyAsInt(currentValue);
            if (atomicInteger.compareAndSet(currentValue, nextValue)) {
                break;
            }
        }
    }

    /**
     * 方式三：直接调用api: 如果要控制超额支出问题，必须自定义CAS的while true
     */
    public void drawMoneyThird() {
        account.updateAndGet(new IntUnaryOperator() {
            @Override
            public int applyAsInt(int operand) {
                if (operand < 0) {
                    System.out.println("no money left");
                }
                return operand - 4;
            }
        });
    }
}
```

## 4. 原子引用

- 基本数据类型满足不了的一些其他计算场景，就要用到原子引用
- 用法和上面原子整数类似

```bash
# 比如大数BigDecimal， 商业计算

# 比如其他引用类型
```

### 4.1 AtomicReference

```java
package com.erick.multithread.d4;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

public class AccountService01 {
    @Getter
    private AtomicReference<BigDecimal> balance = new AtomicReference<>(new BigDecimal("1000"));

    public void withDraw(BigDecimal count) {
        while (true) {
            if (balance.get().compareTo(new BigDecimal("0")) <= 0) {
                break;
            }
            BigDecimal previous = balance.get();
            BigDecimal next = previous.subtract(count);
            boolean isChanged = balance.compareAndSet(previous, next);
            if (isChanged) {
                break;
            }
        }
    }
}
```

### 4.2 AtomicStampedReference

#### ABA场景

- 线程-1在CAS中要将A->B，线程-2在此期间进行A->B->A
- 线程-1的CAS会成功，但是对比的值，其实已经被改过
- 一般情况下，ABA并不会影响具体的业务

```java
package com.erick.multithread.d07;

import java.util.concurrent.atomic.AtomicReference;

public class Demo02 {
    public static void main(String[] args) {
        JackService service = new JackService();
        service.changeName();
    }
}

class JackService {
    private AtomicReference<String> people = new AtomicReference<>("A");

    public void changeName() {
        while (true) {
            String currentName = people.get();
            String nextName = "B";

            /**
             * 捣乱线程
             */
            Thread damageThread = new Thread(() -> damageThread());
            damageThread.start();
            try {
                damageThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (people.compareAndSet(currentName, nextName)) {
                System.out.println("name changed: " + true);
                break;
            }
        }
    }

    private void damageThread() {
        people.compareAndSet("A", "B");
        people.compareAndSet("B", "A");
    }

}
```

#### ABA解决

- 具体的值和版本号(版本号或者时间戳)
- 只要其他线程动过了共享变量(通过值和版本号)，就算cas失败
- 可以通过版本号，得到该值前前后后被改动了多少次

```java
package com.erick.multithread.d07;

import java.util.concurrent.atomic.AtomicStampedReference;

public class Demo02 {
    public static void main(String[] args) {
        JackService service = new JackService();
        service.changeName();
    }
}

class JackService {
    /*初始版本可以设置为 0 */
    private AtomicStampedReference<String> people = new AtomicStampedReference<>("A", 0);

    public void changeName() {
        while (true) {
            String currentName = people.getReference();
            int currentStamp = people.getStamp();
            String nextName = "B";
            int nextStamp = currentStamp + 1;

            /**
             * 捣乱线程
             */
            Thread damageThread = new Thread(() -> damageThread());
            damageThread.start();
            try {
                damageThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (people.compareAndSet(currentName, nextName, currentStamp, nextStamp)) {
                System.out.println("name changed: " + true);
                break;
            }else {
                // 第一次就会失败
                System.out.println("name changed: " + false);
            }
        }
    }

    private void damageThread() {
        people.compareAndSet("A", "B", 0, 1);
        people.compareAndSet("B", "A", 1, 2);
    }
}
```

### 4.3 AtomicMarkableReference

- 线程a在执行CAS操作时，其他线程反复修改数据，但是a线程只关心最终的结果是否变化了
- ABABA场景

```java
package com.erick.multithread.d07;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class Demo02 {
    public static void main(String[] args) {
        JackService service = new JackService();
        service.changeName();
    }
}

class JackService {

    private AtomicMarkableReference<String> people = new AtomicMarkableReference<>("A", true);

    public void changeName() {
        while (true) {
            String currentName = people.getReference();
            String nextName = "B";

            /**
             * 捣乱线程
             */
            Thread damageThread = new Thread(() -> damageThread());
            damageThread.start();
            try {
                damageThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (people.compareAndSet(currentName, nextName, true, false)) {
                System.out.println("name changed: " + true);
                break;
            } else {
                System.out.println("name changed: " + false);
            }
        }
    }

    private void damageThread() {
        people.compareAndSet("A", "B", true, false);
        people.compareAndSet("B", "A", false, true);
    }
}
```

## 5. 原子数组

- 上述原子整数和原子引用，只是针对一个对象的
- 原子数组，可以存放上面的数组

### 5.1 不安全

- 可以将数组中的每个元素单独拿出来，通过原子整数来解决

```java
package com.erick.multithread.d06;

import java.util.ArrayList;
import java.util.List;

public class Demo05 {
    public static void main(String[] args) throws InterruptedException {
        int[] arr = new int[2];

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 200000; j++) {
                    arr[0] = arr[0] + 2;
                    arr[1] = arr[0] - 2;
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println(arr[0]);
        System.out.println(arr[1]);
    }
}
```

### 5.2 原子数组

- AtomicIntegerArray
- AtomicLongArray
- AtomicReferenceArray

```java
package com.erick.multithread.d06;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Demo06 {
    public static void main(String[] args) throws InterruptedException {

        AtomicIntegerArray arr = new AtomicIntegerArray(2);

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 200000; j++) {
                    /*参数一： 数组索引
                     * 参数二： 增加的值*/
                    arr.addAndGet(0, 2);
                    arr.addAndGet(1, -2);
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println(arr.get(0));
        System.out.println(arr.get(1));
    }
}
```

## 6. 原子Filed更新器

- 用来原子更新对象中的字段，该字段必须和volatile结合使用

```bash
- AtomicReferenceFieldUpdater      # 引用类型字段
- AtomicLongFieldUpdater           # Long类型字段
- AtomicIntegerFieldUpdater        # Integer类型字段
```

```java
package com.erick.multithread.d06;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class Demo07 {
    public static void main(String[] args) throws InterruptedException {
        StudentService service = new StudentService();
        Student student = new Student();
        new Thread(() -> service.updateAddress(student)).start();
        TimeUnit.SECONDS.sleep(1);
        new Thread(() -> student.address = "haha").start();
    }
}

class StudentService {

    private static AtomicReferenceFieldUpdater<Student, String> address = AtomicReferenceFieldUpdater.newUpdater(Student.class, String.class, "address");

    public void updateAddress(Student student) {
        while (true) {
            String currentAddress = address.get(student);
            String updateAddress = "lucy";
            prepare(); // 模拟在此期间已经被改了一次
            boolean isChanged = address.compareAndSet(student, currentAddress, updateAddress);
            System.out.println(isChanged);
            if (isChanged) {
                break;
            }
        }
    }

    private void prepare() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}

class Student {
    /*字段必须是公共的,操作的是属性，不是get或set方法   
     * Class com.erick.multithread.d06.StudentService can not access a member of
     * class com.erick.multithread.d06.Student with modifiers "private volatile"*/
    public volatile String address;
}
```

## 7. 原子累加器

### 7.1 基本使用

- JDK 8 以后提供了专门的做累加的类，用来提高性能
- 任务拆分的理念

```bash
# 原理： 在有竞争的时候，设置多个累加单元， Thread-0 累加 Cell[0], Thread-1累加Cell[1]
#       累加结束后，将结果进行汇总，这样他们在累加时操作的不同的Cell变量，因此减少了CAS重试失败，从而提高性能
- LongAdder        ----          AtomicLong
- DoubleAdder
```

```java
package com.erick.multithread.d06;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Demo08 {
    public static void main(String[] args) {
        AdderService adderService = new AdderService();
        System.out.println(adderService.firstAdd());  // 364 ms
        System.out.println(adderService.secondAdd()); // 76 ms
        System.out.println(adderService.getFirstNum()); // 5000000
        System.out.println(adderService.getSecondNum()); // 5000000
    }
}

@Data
class AdderService {
    private AtomicLong firstNum = new AtomicLong();

    private LongAdder secondNum = new LongAdder();

    public long firstAdd() {
        long start = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 1000000; j++) {
                    firstNum.incrementAndGet();
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return System.currentTimeMillis() - start;
    }

    public long secondAdd() {
        long start = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                for (int j = 0; j < 1000000; j++) {
                    secondNum.increment();
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return System.currentTimeMillis() - start;
    }
}
```

## 8. Unsafe类

- CAS方法中：用于操作线程和内存的一个java类

### 8.1 源码获取

- 并不是说线程不安全，只是说不建议开发人员使用

```java
package sun.misc

public final class Unsafe {

    static {
        Reflection.registerMethodsToFilter(Unsafe.class, Set.of("getUnsafe"));
    }

    private Unsafe() {}

    private static final Unsafe theUnsafe = new Unsafe();
    private static final jdk.internal.misc.Unsafe theInternalUnsafe = jdk.internal.misc.Unsafe.getUnsafe();
```

- 私有的成员方法，只能通过反射获取该类

```java
package com.erick.multithread.d5;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Demo07 {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);

        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        System.out.println(unsafe);
    }
}
```

### 8.2 修改属性

```java
package com.erick.multithread.d5;

import lombok.Data;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Demo07 {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);

        Unsafe unsafe = (Unsafe) theUnsafe.get(null);

        Teacher teacher = new Teacher();
        /*获取field的偏移量*/
        long idOffset = unsafe.objectFieldOffset(Teacher.class.getDeclaredField("id"));
        long nameOffset = unsafe.objectFieldOffset(Teacher.class.getDeclaredField("name"));

        /*执行cas操作*/
        boolean isIdChanged = unsafe.compareAndSwapInt(teacher, idOffset, 0, 1);
        boolean isNameChanged = unsafe.compareAndSwapObject(teacher, nameOffset, null, "erick");

        /*验证*/
        System.out.println(teacher);
        System.out.println(isIdChanged);
        System.out.println(isNameChanged);
    }
}

@Data
class Teacher {
    volatile int id;
    volatile String name;
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

# 线程池

## 1. 基本介绍

### 1.1 引入原因

```bash
1. 一个任务过来，一个线程去做。如果每次过来都临时创建新线程，性能低且比较耗费内存
2. 线程数多于cpu核心，线程切换，要保存原来线程的状态，运行现在的线程，势必会更加耗费资源
   线程数少于cpu核心，不能很好的利用cpiu的性能
   
3. 充分利用已有线程，去处理原来的任务
```

### 1.2. 线程池组件

```bash
1. 消费者(线程池)：                 保存一定数量线程来处理任务
2. 生产者：                        客户端源源不断产生的新任务
3. 阻塞队列(blocking queue)：      平衡消费者和生产者之间，用来保存任务 的一个等待队列

- 生产任务速度较快，多余的任务要等
- 生产任务速度慢，那么线程池中存活的线程等
```


![image-20221012105847884](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221012105847884.png)

## 2. 自定义线程池

```bash
# 消费者超时：
- 如果线程池的核心线程消费，从阻塞队列中拉取时，超时后则kill当前核心线程

# 生产者超时：拒绝策略
- 当核心线程都在工作时，阻塞队列中已满，生产者线程提供的任务就会进行等待
- 让任务生产者自己决定该如何执行

       - 死等
       - 带超时等待
       - 让调用者放弃执行任务
       - 让调用者抛出异常
       - 让调用者自己执行任务
```

### 2.1 阻塞队列

```java
package com.erick.multithread.d01;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BlockQueue {

    /*阻塞队列的大小*/
    private int capacity;

    public BlockQueue(int capacity) {
        this.capacity = capacity;
    }

    /*保存任务对象*/
    private LinkedList<Runnable> queue = new LinkedList<>();

    private ReentrantLock lock = new ReentrantLock();

    private Condition consumerRoom = lock.newCondition();

    private Condition producerRoom = lock.newCondition();

    /*添加任务：持续等待*/
    public boolean addTask(Runnable task) {
        try {
            lock.lock();
            while (queue.size() >= capacity) {
                try {
                    System.out.println(Thread.currentThread().getName() + "等待(队列满)");
                    producerRoom.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            queue.addFirst(task);
            consumerRoom.signalAll();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /*添加任务，过时不候*/
    public boolean addTask(Runnable task, long timeout, TimeUnit unit) {
        if (timeout <= 0 || unit == null) {
            return addTask(task);
        }

        try {
            lock.lock();
            long nanos = unit.toNanos(timeout);
            while (queue.size() == capacity) {
                if (nanos < 0) {
                    return false;
                }
                try {
                    nanos = consumerRoom.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            queue.addFirst(task);
            consumerRoom.signalAll();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /*生产者添加任务*/
    public boolean tryAddTask(RejectPolicy rejectPolicy, Runnable task) {
        try {
            lock.lock();
            if (queue.size() == capacity) {
                /*操作的权利，下方给对应的producer*/
                return rejectPolicy.reject(this, task);
            }
            queue.addFirst(task);
            consumerRoom.signalAll();
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 线程池中的WorkerThread线程获取blockingqueue中任务时，即使阻塞队列中没有任务，也会一直死等，WorkerThread并不会结束
     */
    public Runnable getTask() {
        try {
            lock.lock();
            while (queue.size() == 0) {
                System.out.println(Thread.currentThread().getName() + "等待(队列空)");
                try {
                    consumerRoom.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Runnable task = queue.removeLast();
            producerRoom.signalAll();
            return task;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 线程池中的WorkerThread线程获取blockingqueue中任务时，超时则会kill掉池子中的当前Thread
     *
     * @param unit
     * @param timeout
     * @return
     */
    public Runnable getTask(long timeout, TimeUnit unit) {
        if (timeout <= 0 || unit == null) {
            return getTask();
        }

        try {
            lock.lock();
            long nanos = unit.toNanos(timeout);
            while (queue.size() == 0) {
                if (nanos < 0) {
                    return null;
                }
                try {
                    nanos = consumerRoom.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Runnable task = queue.removeLast();
            producerRoom.signalAll();
            System.out.println("end");
            return task;
        } finally {
            lock.unlock();
        }
    }

    public int getSize() {
        try {
            lock.lock();
            return queue.size();
        } finally {
            lock.unlock();
        }
    }
}
```

### 2.2 线程池

```java
package com.erick.multithread.d01;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ErickThreadPool {

    /*线程池中的最大的核心线程数*/
    private int coreThreadSize;

    /*线程池子中的线程，等待获取的任务的时候，如果超时，则线程kill掉*/
    private long timeout;
    private TimeUnit timeUnit;

    private BlockQueue blockQueue;

    private RejectPolicy rejectPolicy;

    public ErickThreadPool(int coreThreadSize, int blockQueueSize) {
        this(coreThreadSize, blockQueueSize, 0L, null, new DefaultWaitForeverPolicy());
    }

    public ErickThreadPool(int coreThreadSize, int blockQueueSize, long timeout, TimeUnit timeUnit, RejectPolicy rejectPolicy) {
        this.coreThreadSize = coreThreadSize;
        blockQueue = new BlockQueue(blockQueueSize);
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.rejectPolicy = rejectPolicy;
    }

    private final Set<WorkerThread> pool = new HashSet<>();

    /**
     * 任务执行：
     * 1. 如果核心线程数<池子中线程数量，则创建新的线程
     * 2. 如果核心线程数=池子中线程数量，则先去阻塞队列进行等待
     *
     * @param task
     */
    public synchronized void executeTask(Runnable task) {
        if (pool.size() < coreThreadSize) {
            WorkerThread workerThread = new WorkerThread(task);
            pool.add(workerThread);
            System.out.println("crate new thread");
            workerThread.start();
        } else {
            rejectPolicy.reject(blockQueue, task);
        }
    }

    /*线程池中具体干活的线程*/
    class WorkerThread extends Thread {
        /*传入的任务对象*/
        private Runnable task;

        public WorkerThread(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            while (task != null || (task = blockQueue.getTask(timeout, timeUnit)) != null) {
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    task = null;
                }
            }
            /*任务执行完毕后，将该线程从池子中移除*/
            synchronized (pool) {
                System.out.println("thread destroyed");
                pool.remove(this);
            }
        }
    }
}
```

### 2.3 拒绝策略

```java
package com.erick.multithread.d01;

import java.util.concurrent.TimeUnit;

/*阻塞队列已满，生产者对应的拒绝策略*/
public interface RejectPolicy {
    boolean reject(BlockQueue blockQueue, Runnable task);
}

/*生产者死等*/
class DefaultWaitForeverPolicy implements RejectPolicy {

    @Override
    public boolean reject(BlockQueue blockQueue, Runnable task) {
        return blockQueue.addTask(task);
    }
}

/*生产者超时等待*/
class WaitWithTimeoutPolicy implements RejectPolicy {

    private TimeUnit timeUnit;
    private long timeout;

    public WaitWithTimeoutPolicy(TimeUnit timeUnit, long timeout) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }


    @Override
    public boolean reject(BlockQueue blockQueue, Runnable task) {
        return blockQueue.addTask(task, timeout, timeUnit);
    }
}

/*生产者放弃任务*/
class ProducerGiveUpPolicy implements RejectPolicy {

    @Override
    public boolean reject(BlockQueue blockQueue, Runnable task) {
        System.out.println("task give up");
        return false;
    }
}

/*生产者自己执行*/
class ProducerExecutePolicy implements RejectPolicy {

    @Override
    public boolean reject(BlockQueue blockQueue, Runnable task) {
        new Thread(task).start();
        return false;
    }
}

/*生产者异常：后续其他线程的不会再进来了*/
class ProducerExceptionPolicy implements RejectPolicy {

    @Override
    public boolean reject(BlockQueue blockQueue, Runnable task) {
        throw new RuntimeException("core thread is working, queue is full");
    }
}
```

### 2.4 测试代码

```java
package com.erick.multithread.d01;

import com.erick.multithread.util.Sleep;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TestCode {
    public static void main(String[] args) {
        ErickThreadPool pool = new ErickThreadPool(1, 1, 4, TimeUnit.SECONDS, new ProducerExceptionPolicy());

        for (int i = 0; i < 3; i++) {
            pool.executeTask(new Runnable() {
                @Override
                public void run() {
                    System.out.println("----------hello---------" + new Date());
                }
            });
            Sleep.sleep(3);
        }
    }
}
```



## 3. JDK线程池

### 3.1 类图

![image-20221018074250288](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221018074250288.png)

### 3.2 线程状态

- ThreadPoolExecutor 使用int的高3位来表示线程池状态，低29位表示线程数量

![image-20221018074517365](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221018074517365.png)

### 3.3 线程数量

- 过小，导致cpu资源不能充分利用，浪费性能
- 过大，线程上下文切换浪费性能，每个线程也要占用内存导致占用内存过多

#### CPU密集型

- 如果线程的任务主要是和cpu资源打交道，比如大数据运算，称为CPU密集型
- 线程数量： 核心数+1
- +1: 保证某线程由于某些原因(操作系统方面)导致暂停时，额外线程可以启动，不浪费CPU资源

#### IO密集型

- IO操作，RPC调用，数据库访问时，CPU是空闲的，称为IO密集型
- 更加常见： IO操作，远程RPC调用，数据库操作
- 线程数 = 核数 * 期望cpu利用率 *  (CPU计算时间 + CPU等待时间) / CPU 计算时间

![image-20221018104629282](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221018104629282.png)



## 4. ThreadPoolExecutor

### 4.1 构造方法

```java
int corePoolSize:                     // 核心线程数
int maximumPoolSize：                 // 最大线程数
long keepAliveTime：                  // 救急线程数执行任务完后存活时间
TimeUnit unit：                       // 救急线程数执行任务完后存活时间
BlockingQueue<Runnable> workQueue：   // 阻塞队列
ThreadFactory threadFactory：         // 线程生产工厂，为线程起名字
RejectedExecutionHandler handler：    // 拒绝策略 

public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) {
    if (corePoolSize < 0 ||
        maximumPoolSize <= 0 ||
        maximumPoolSize < corePoolSize ||
        keepAliveTime < 0)
        throw new IllegalArgumentException();
    if (workQueue == null || threadFactory == null || handler == null)
        throw new NullPointerException();
    this.acc = System.getSecurityManager() == null ?
            null :
            AccessController.getContext();
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.workQueue = workQueue;
    this.keepAliveTime = unit.toNanos(keepAliveTime);
    this.threadFactory = threadFactory;
    this.handler = handler;
}
```

### 4.2 核心线程和救急线程

```bash
# 核心线程
- 执行完任务后，会继续保留在线程池中

# 救急线程：
- 如果阻塞队列已满，并且没有空余的核心线程。那么会创建救急线程来执行任务
- 任务执行完毕后，这个线程就会被销毁(临时工)
- 必须是有界阻塞，如果是无界队列，则不需要创建救急线程
```

### 4.3 拒绝策略

- 核心线程满负荷，阻塞队列(有界队列)已满，无空余救急线程，才会执行拒绝策略，JDK 提供了4种拒绝策略

```bash
- RejectedExecutionHandler     # 拒绝策略接口
- AbortPolicy:                 # 调用者抛出RejectedExecutionException,  默认策略
- CallerRunsPolicy:            # 调用者运行任务
- DiscardPolicy:               # 放弃本次任务
- DiscardOldestPolicy:         # 放弃阻塞队列中最早的任务，本任务取而代之

# 第三方框架的技术实现
- Dubbo: 在抛出异常之前，记录日志，并dump线程栈信息，方便定位问题
- Netty: 创建一个新的线程来执行任务
- ActiveMQ: 带超时等待(60s)， 尝试放入阻塞队列
```

![image-20221018075430425](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221018075430425.png)

## 5. Executors

- 默认的构造方法来创建线程池，参数过多，JDK提供了工厂方法，来创建线程池

### 5.1 newFixedThreadPool

- 固定大小
- 核心线程数 = 最大线程数，救急线程数为0
- 阻塞队列：无界，可以存放任意数量的任务

```bash
# 应用场景
任务量已知，但是线程执行时间较长
执行任务后，线程并不会结束
```

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }
```

```java
package com.erick.multithread.d02;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo01 {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(1, new ThreadFactory() {
            private AtomicInteger poolNumber = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "erick-pool" + poolNumber.getAndIncrement());
            }
        });

        for (int i = 0; i < 10; i++) {
            pool.execute(() -> System.out.println(Thread.currentThread().getName() + ":hello"));
        }
    }
}
```

### 5.2 newCachedThreadPool

- 核心线程数为0, 最大线程数为Integer的无限大
- 全部是救急线程，等待时间是60s，60s后就会消亡
- SynchronousQueue: 没有容量，没有线程来取的时候是放不进去的
- 整个线程池数会随着任务数目增长，1分钟后没有其他活动会消亡

```bash
# 应用场景
1. 时间较短的线程
2. 数量大，任务执行时间长，会造成  OutOfMmeory问题
```

```java
 public static ExecutorService newCachedThreadPool() {
     return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                   60L, TimeUnit.SECONDS,
                                   new SynchronousQueue<Runnable>());
 }
```

### 5.3.  newSingleThreadExecutor

- 线程池大小始终为1个，不能改变线程数
- 相比自定义一个线程来执行，线程池可以保证前面任务的失败，不会影响到后续任务

```bash
# 1. 和自定义线程的区别
自定义线程：  执行多个任务时，一个出错，后续都能不能执行了
单线程池：    一个任务失败后，会结束出错线程。重新new一个线程来执行下面的任务

# 2. 执行顺序
单线程池： 保证所有任务都是串行

# 3. 和newFixedThreadPool的区别
newFixedThreadPool:          初始化后，还可以修改线程大小
newSingleThreadExecutor:     不可以修改
```

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}
```

```java
package com.nike.erick.d07;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo01 {
    public static void main(String[] args) {
        method03();
    }

    private static void method01() {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        /*pool-1-thread-1   pool-1-thread-2  pool-1-thread-1*/
        pool.execute(() -> System.out.println(Thread.currentThread().getName() + " working"));
        pool.execute(() -> System.out.println(Thread.currentThread().getName() + " working"));
        pool.execute(() -> System.out.println(Thread.currentThread().getName() + " working"));
    }

    private static void method02() {
        ExecutorService pool = Executors.newCachedThreadPool();
        /*pool-1-thread-1  pool-1-thread-2  pool-1-thread-3*/
        pool.execute(() -> System.out.println(Thread.currentThread().getName() + " working"));
        pool.execute(() -> System.out.println(Thread.currentThread().getName() + " working"));
        pool.execute(() -> System.out.println(Thread.currentThread().getName() + " working"));
    }

    private static void method03() {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        /*第一个任务执行失败后，后续任务会继续执行*/
        pool.execute(() -> {
            int i = 1 / 0;
            System.out.println(Thread.currentThread().getName() + " running");
        });

        pool.execute(() -> {
            System.out.println(Thread.currentThread().getName() + " running");
        });
    }
}
```

### 5.4 newScheduledThreadPool

```bash
  public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
      return new ScheduledThreadPoolExecutor(corePoolSize);
  }
```

#### 延时执行

```java
package com.erick.multithread.d02;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Demo02 {
    public static void main(String[] args) {
        method03();
    }

    /*两个核心线程：分别延时1s和3s后执行*/
    public static void method01() {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);
        pool.schedule(() -> System.out.println(Thread.currentThread().getName() + new Date()), 1, TimeUnit.SECONDS);
        pool.schedule(() -> System.out.println(Thread.currentThread().getName() + new Date()), 3, TimeUnit.SECONDS);
    }

    /*一个核心线程：串行执行*/
    public static void method02() {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        // 相差2s
        pool.schedule(() -> System.out.println(Thread.currentThread().getName() + new Date()), 1, TimeUnit.SECONDS);
        pool.schedule(() -> System.out.println(Thread.currentThread().getName() + new Date()), 3, TimeUnit.SECONDS);
    }

    /*一个核心线程：其中一个任务出错时，不影响后面的*/
    public static void method03() {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
        // 相差2s
        pool.schedule(() -> System.out.println(Thread.currentThread().getName() + new Date() + (1 / 0)), 1, TimeUnit.SECONDS);
        pool.schedule(() -> System.out.println(Thread.currentThread().getName() + new Date()), 3, TimeUnit.SECONDS);
    }
}
```

#### 定时执行

```bash
# scheduleAtFixedRate
- 如果任务的执行时间大于时间间隔，就会紧接着立刻执行

public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                              long initialDelay,
                                              long period,
                                              TimeUnit unit);

# scheduleWithFixedDelay
- 上一个任务执行完毕后，再延迟一定的时间才会执行

public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                 long initialDelay,
                                                 long delay,
                                                 TimeUnit unit);
```

```java
package com.dreamer.multithread.day09;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Demo07 {
    public static void main(String[] args) {
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(2);

        // 定时执行任务
        pool.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("task is running");
            }
        }, 3, 2, TimeUnit.SECONDS);
        //  初始延时，   任务间隔时间，    任务间隔时间单位
    }
}
```

## 6. 提交任务

```bash
# 1. execute
void execute(Runnable command);

# 2. submit: 可以从 Future 对象中获取一些执行任务的最终结果
Future<?> submit(Runnable task);
<T> Future<T> submit(Runnable task, T result);
<T> Future<T> submit(Callable<T> task);

# 3. invokeAll: 执行集合中的所有的任务
<T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
    throws InterruptedException;
    
# 4. invokeAny: 集合中之要有一个任务执行完毕，其他任务就不再执行
 <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException;
```

```java
package com.nike.erick.d07;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Demo02 {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        method05(pool);
    }

    /* void execute(Runnable command) */
    public static void method01(ExecutorService pool) {
        pool.execute(() -> System.out.println(Thread.currentThread().getName() + " running"));
    }

    /*  <T> Future<T> submit(Runnable task, T result)
     * Future<?> submit(Runnable task) */
    public static void method02(ExecutorService pool) throws InterruptedException {
        Future<?> result = pool.submit(new Thread(() -> System.out.println(Thread.currentThread().getName() + " running")));
        TimeUnit.SECONDS.sleep(1);
        System.out.println(result.isDone());
        System.out.println(result.isCancelled());
    }

    /*
     * <T> Future<T> submit(Callable<T> task)*/
    public static void method03(ExecutorService pool) throws InterruptedException, ExecutionException {
        Future<String> submit = pool.submit(() -> "success");
        TimeUnit.SECONDS.sleep(1);
        System.out.println(submit.isDone());
        System.out.println(submit.isCancelled());
        System.out.println(submit.get()); // 返回结果是success
    }

    /* <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException;*/
    public static void method04(ExecutorService pool) throws InterruptedException {
        Collection tasks = new ArrayList();
        for (int i = 0; i < 10; i++) {
            int round = i;
            tasks.add((Callable) () -> {
                System.out.println(Thread.currentThread().getName() + " running");
                return "success:" + round;
            });
        }
        List results = pool.invokeAll(tasks);

        TimeUnit.SECONDS.sleep(1);
        System.out.println(results);
    }

    /*
     *     <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException;
     * */
    public static void method05(ExecutorService pool) throws InterruptedException, ExecutionException {
        ExecutorService service = Executors.newFixedThreadPool(1);
        Collection<Callable<String>> tasks = new ArrayList<>();

        tasks.add(() -> {
            System.out.println("first task");
            TimeUnit.SECONDS.sleep(1);
            return "success";
        });

        tasks.add(() -> {
            System.out.println("second task");
            TimeUnit.SECONDS.sleep(2);
            return "success";
        });


        tasks.add(() -> {
            System.out.println("third task");
            TimeUnit.SECONDS.sleep(3);
            return "success";
        });
        // 任何一个任务执行完后，就会返回结果
        String result = pool.invokeAny(tasks);
        System.out.println(result);
    }
}
```

## 7. 异常处理

```bash
# 不处理
- 任务执行过程中，业务中的异常并不会抛出

# 任务执行者处理

# 线程池处理
```

```java
package com.erick.multithread.d02;

import com.erick.multithread.util.Sleep;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Demo03 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        method04();
    }


    /*会抛出异常，但线程不会终止*/
    public static void method01() {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.execute(() -> {
            int i = 1 / 0;
        });
    }

    /*不处理异常*/
    public static void method02() {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(() -> {
            int i = 1 / 0;
        });
    }

    /*调用者自己处理*/
    public static void method03() {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(() -> {
            try {
                int i = 1 / 0;
            } catch (Exception e) {
                System.out.println(e);
            }
        });
    }

    /*线程池处理*/
    public static void method04() throws ExecutionException, InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<?> result = pool.submit(() -> {
            int i = 1 / 0;
        });
        Sleep.sleep(1);
         // 获取结果的时候，就可以把线程执行任务过程中的异常报出来
        System.out.println(result.get());
    }
}
```



## 8. 关闭线程池

```bash
# shutdown:      void shutdown();
- 将线程池的状态改变为SHUTDOWN状态
- 不会接受新任务，已经提交的任务不会停止
- 不会阻塞调用线程的执行

# shutdownNow:    List<Runnable> shutdownNow();
-  不会接受新任务
-  没执行的任务会打断
-  将等待队列中的任务返回
```

```java
package com.erick.multithread.d02;

import com.erick.multithread.util.Sleep;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo01 {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.execute(() -> {
            System.out.println("first");
            Sleep.sleep(2);
        });

        pool.execute(() -> {
            System.out.println("second");
            Sleep.sleep(2);
        });

        pool.execute(() -> {
            System.out.println("third");
            Sleep.sleep(2);
        });
        pool.shutdown();//任务都会执行完毕
        System.out.println("main ending"); // 不阻塞调用线程的执行
    }
}
```

```java
package com.erick.multithread.d02;

import com.erick.multithread.util.Sleep;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Demo01 {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.execute(() -> {
            System.out.println("first");
            Sleep.sleep(2);
        });

        pool.execute(() -> {
            System.out.println("second");
            Sleep.sleep(2);
        });

        pool.execute(() -> {
            System.out.println("third");
            Sleep.sleep(2);
        });
        List<Runnable> leftOver = pool.shutdownNow();
        System.out.println(leftOver.size());// 剩余任务为2，正在执行的任务被打断
        System.out.println("main ending"); // 不阻塞调用线程的执行
    }
}
```

## 9. 异步模式-工作线程

### 9.1 Worker Thread 

- 让有限的工作线程来轮流异步处理无限多的任务
- 不同的任务类型应该使用不同的线程池

### 9.2 饥饿-解决

- 单个固定大小线程池，处理不同类型的任务时，会有饥饿现象

```bash
- 假设线程池包含2个线程
- 一个线程负责洗菜，一个线程负责做菜， 必须先洗好菜，才能开始做菜(同步等待结果)
- 假设只有一个项目，刚好2个线程都可以运行
- 假设有两个菜，那么两个线程都去要做菜，但没线程做菜了，就会出现线程饥饿

# 解决方式：
- 增加线程池的线程数量，但是不能从根本解决问题
- 不同的任务类型，使用不同的线程池
```

```java
package com.erick.multithread.d02;

import java.util.concurrent.*;

public class Demo04 {
    public static void main(String[] args) {
        CookingService service = new CookingService();
        service.normalService();
    }
}

class CookingService {

    /*正常情况*/
    public void normalService() {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        handle(pool, pool);
    }

    /*饥饿情况*/
    public void hungryService() {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 10; i++) {
            handle(pool, pool);
        }
    }


    /*不同任务交给不同的线程池来做*/
    public void workerThread() {
        ExecutorService washPool = Executors.newFixedThreadPool(2);
        ExecutorService cookPool = Executors.newFixedThreadPool(1);
        for (int i = 0; i < 10; i++) {
            handle(washPool, cookPool);
        }
    }

    private void handle(ExecutorService cookPool, ExecutorService washPool) {
        cookPool.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("准备");
                /*必须先洗菜*/
                Future<String> frontEnd = washPool.submit(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        System.out.println("洗好了");
                        return "干净的菜";
                    }
                });

                try {
                    String result = frontEnd.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }

                /*做菜线程*/
                System.out.println("水煮白菜");
            }
        });
    }
}
```
