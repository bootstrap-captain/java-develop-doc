# AQS

## 1. 简介

- AbstractQueuedSynchronizer，是阻塞式锁和相关的同步器工具的框架

```bash
# state属性： 表示资源的状态(独占模式， 共享模式)， 子类需要定义如何维护这个状态，控制如何获取锁和释放锁
- 独占模式：只有一个线程能够访问资源。      共享模式：允许多个线程访问资源，但对线程的最大数有限制
- getState: 获取state属性
- setState: 设置state属性
- compareAndSetState: 乐观锁机制设置state状态

# 提供了机遇FIFO的等待队列，类似于Monitor中的EntryList

# 条件变量来实现等待，唤醒机制， 支持多个条件变量，类似于Monitor的WaitSet

# 子类主要实现下面的方法： 默认抛出UnsupportedOperationException
- tryAcquire
- tryRealease
- tryAcquiredShared
- tryReleaseShared
- isHeldExclusively
```

## 2. 自定义AQS锁

- 用AQS写一个不可重入锁

```java
package com.erick.multithread.d7;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Demo03 {
    public static void main(String[] args) {
        ErickLock erickLock = new ErickLock();

        new Thread(() -> {
            erickLock.lock();
            try {
                System.out.println("first-thread locking....");
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                System.out.println("first-thread unlocking...");
                erickLock.unlock();
            }
        }).start();

        new Thread(() -> {
            erickLock.lock();
            try {
                System.out.println("second-thread locking....");
            } finally {
                System.out.println("second-thread unlocking...");
                erickLock.unlock();
            }
        }).start();
    }
}

class ErickLock implements Lock {

    /*同步器类， 独占锁*/
    class ErickSync extends AbstractQueuedSynchronizer {

        /*尝试获取锁*/
        @Override
        protected boolean tryAcquire(int arg) {
            /*将state属性从0未加锁 改为 1加锁*/
            if (compareAndSetState(0, 1)) {
                // 加上了锁，并设置owner未当前线程
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            /*设置Owner为null，并设置锁的状态为0*/
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        /*是否是独占锁*/
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        /*等待队列*/
        public Condition newCondition() {
            return new ConditionObject();
        }
    }

    private ErickSync erickSync = new ErickSync();

    // 加锁，不成功会放入等待队列
    @Override
    public void lock() {
        erickSync.acquire(1);
    }

    /*加锁，可打断*/
    @Override
    public void lockInterruptibly() throws InterruptedException {
        erickSync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return erickSync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return erickSync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        erickSync.release(1);
    }

    @Override
    public Condition newCondition() {
        return erickSync.newCondition();
    }
}
```

# ReentryLock

## 1. 可重入锁

- 可重入锁：一个线程已经获取锁，因为该线程是该锁主人，跨方法获取该锁时，依然成功
- 不可重入：同一线程跨方法获取就会造成死锁，部分锁就是这种情况

```java
package com.erick.multithread.d04;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class Demo01 {

    private ReentrantLock lock = new ReentrantLock();

    @Test
    void testMethod() {
        firstMethod();
    }

    public void firstMethod() {
        try {
            lock.lock();
            log.info("first method get lock");
            secondMethod();
        } finally {
            /*unlock必须放在finally中，保证锁一定可以释放*/
            lock.unlock();
        }
    }

    public void secondMethod() {
        try {
            lock.lock();
            log.info("second method get lock");
        } finally {
            lock.unlock();
        }
    }
}
```

## 2. 可打断锁

- 被动的方式： 避免一个线程一直等待当前要获取的锁

```bash
- 没有其他线程争夺锁，则正常执行
- 有竞争时，线程就会进入EntryList，但是可以被打断
- 其他线程先获取锁，执行一段时间后，等待获取锁的线程 《打断等待》
```

#### 无竞争

```java
package com.erick.multithread.d2;

import java.util.concurrent.locks.ReentrantLock;

public class Demo02 {
    private static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {

        new Thread(() -> {
            /*第一个try表示可以被打断*/
            try{
                lock.lockInterruptibly();
            } catch (InterruptedException e) {
                System.out.println("锁被打断了");
                throw new RuntimeException(e);
            }
            /*第二个try表示释放锁*/
            try{
                doBusiness();
            }finally {
                lock.unlock();
            }
        }).start();
    }

    private static void doBusiness(){
        System.out.println("do business logic");
    }
}
```

#### 有竞争

```java
package com.erick.multithread.d2;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Demo03 {
    private static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {

        Thread firstThread = new Thread(() -> {
            /*获取锁的时候，可以被打断，就结束当前等待过程*/
            try {
                System.out.println("first-thread 开始等待锁");
                lock.lockInterruptibly();
            } catch (InterruptedException e) {
                System.out.println("first-thread 锁被打断");
                throw new RuntimeException(e);
            }

            try {
                doBusiness();
            } finally {
                lock.unlock();
            }
        });

        Thread secondThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock();
                    /*不会释放锁*/
                    sleep(2);
                    firstThread.interrupt();
                    System.out.println("second-thread完成业务");
                } finally {
                    lock.unlock();
                }
            }
        });

        secondThread.start();
        sleep(1);
        firstThread.start();
    }

    private static void doBusiness() {
        System.out.println("do business logic");
    }

    private static void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

## 3 公平锁

```bash
# 1. 不公平锁
- 一个线程持有锁时，其他线程进入锁的 EntryList
- 线程释放锁时，其他线程一拥而上，而不是按照进入的顺序先到先得

# 2. 公平锁： 通过ReentranLock实现
- public ReentrantLock(boolean fair) 
- 默认是非公平锁，传参为true，公平锁
```

```java
package com.erick.multithread.d04;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Demo02 {
    private static ReentrantLock lock = new ReentrantLock(true);

    public static void main(String[] args) {
        new Thread("main-thread") {
            @Override
            public void run() {
                try {
                    lock.lock();
                    System.out.println(Thread.currentThread().getName() + " get lock");
                    rest(5);
                } finally {
                    lock.unlock();
                }
            }
        }.start();

        rest(2);

        for (int i = 0; i < 5; i++) {
            new Thread("t" + i) {
                @Override
                public void run() {
                    try {
                        lock.lock();
                        System.out.println(Thread.currentThread().getName() + " get lock");
                    } finally {
                        lock.unlock();
                    }
                }
            }.start();
        }
    }


    private static void rest(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

## 4. 超时锁

- 避免死锁： 主动方式来避免一个线程一直等待锁资源，带来的死锁问题

#### 不带超时

- 正常获取到锁

```java
package com.erick.multithread.d2;

import java.util.concurrent.locks.ReentrantLock;

public class Demo05 {
    private static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        new Thread(() -> {
            boolean hasLock = lock.tryLock();
            if (!hasLock) {
                System.out.println("没有获取到锁，放弃");
                return;
            }

            try{
                executeBusiness();
            }finally {
                lock.unlock();
            }
        }).start();
    }

    private static void executeBusiness(){
        System.out.println("do business logic");
    }
}
```

- 最终没获取到锁

```java
package com.erick.multithread.d2;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Demo06 {
    private static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        new Thread(() -> {
            try{
                lock.lock();
                sleep(5);
            }finally {
                lock.unlock();
            }
        }).start();

        new Thread(() -> {
            boolean hasLock = lock.tryLock();
            if (!hasLock){
                System.out.println("没有获取到锁，放弃等待");
                return;
            }

            try{
                executeBusiness();
            }finally {
                lock.unlock();
            }
        }).start();
    }

    private static void executeBusiness(){
        System.out.println("do business logic");
    }

    private static void sleep(int seconds){
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

#### 带超时

- 等待过程中，如果获取到了锁，则执行正常的业务流程
- 等待一段时间后，如果没有获取到锁，则中断获取锁的竞争
- 等待过程中，依然可以被打断

```bash
ReentrantLock        public boolean tryLock(long timeout, TimeUnit unit)
                             throws InterruptedException
```

```java
package com.nike.erick.d03;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Demo06 {

    private static ReentrantLock reentrantLock = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {

        Thread firstThread = new Thread(() -> {
            reentrantLock.lock();
            try {
                TimeUnit.SECONDS.sleep(2);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                reentrantLock.unlock();
            }
        });

        Thread secondThread = new Thread(() -> {
            try {
                /*最长等待3s*/
                boolean hasLock = reentrantLock.tryLock(3, TimeUnit.SECONDS);
                if (!hasLock) {
                    System.out.println("没有获取到锁");
                    return;
                }

                try {
                    System.out.println("Execution Business...");
                } finally {
                    reentrantLock.unlock();
                }

            } catch (InterruptedException e) {
                System.out.println("等待锁过程中被打断了。。。");
                e.printStackTrace();
            }
        });

        firstThread.start();
        TimeUnit.SECONDS.sleep(1);
        secondThread.start();
        secondThread.interrupt();
    }
}
```

## 5. 条件变量

- 多WaitSet， 可以和wait/notify更好的结合
- 将等待的不同WaitSet分类，然后指定WaitSet来进行唤醒

```bash
# 1. 创建一个等待的队列
- public Condition newCondition()

# 2. 将一个线程在某个队列中进行等待
Condition        public final void await() throws InterruptedException

# 3. 去某个队列中唤醒等待的线程
Condition        public final void signal()
                 public final void signalAll()
```

```java
package com.erick.multithread.d04;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demo03 {

    private static ReentrantLock lock = new ReentrantLock();

    private static Condition boyRoom = lock.newCondition();
    private static Condition girlRoom = lock.newCondition();

    private static boolean hasCigarette = false;

    private static boolean hasDinner = false;

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            int number = i;
            new Thread("boy " + i) {
                @Override
                public void run() {
                    lock.lock();
                    while (!hasCigarette) {
                        try {
                            System.out.println("没烟，休息" + number);
                            boyRoom.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        System.out.println("有烟，干活" + number);
                    } finally {
                        lock.unlock();
                    }
                }
            }.start();
        }

        for (int i = 0; i < 5; i++) {
            int number = i;
            new Thread("girl " + i) {
                @Override
                public void run() {
                    lock.lock();
                    while (!hasDinner) {
                        try {
                            System.out.println("没外卖，休息" + number);
                            girlRoom.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    try {
                        System.out.println("有外卖，干活" + number);
                    } finally {
                        lock.unlock();
                    }
                }
            }.start();
        }

        TimeUnit.SECONDS.sleep(2);
        try {
            lock.lock();
            hasCigarette = true;
            boyRoom.signalAll();
        } finally {
            lock.unlock();
        }

        TimeUnit.SECONDS.sleep(2);
        try {
            lock.lock();
            hasDinner = true;
            girlRoom.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
```

## 6. ReentrantLock *vs* Synchronized

```bash
可重入性：   都支持
可打断性：   Synchronized锁不能被打断                 ReentrantLock可以被打断，防止死锁
超时性：     Synchronized锁获取时候会一直等待          ReentrantLock支持超时等待
公平性：     Synchronized的EntryList是不公平         ReentrantLock(true)可以是公平锁
条件变量：   Synchronized的WaitSet只有一个            ReentrantLock支持不同的WaitSet
```

# 固定顺序输出

## 1. 先2后1

- 线程2运行完毕后，线程1再开始运行

### 1.1  synchronized

- synchronized + wait + notify

```java
package com.erick.multithread.d04;

import java.util.concurrent.TimeUnit;

public class Demo04 {

    private static Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        new Thread("t1") {
            @Override
            public void run() {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(Thread.currentThread().getName() + " running");
                }
            }
        }.start();

        TimeUnit.SECONDS.sleep(2);

        new Thread("t2") {
            @Override
            public void run() {
                synchronized (lock) {
                    System.out.println(Thread.currentThread().getName() + " running");
                    lock.notify();
                }
            }
        }.start();
    }
}
```

### 1.2 reentrylock

- reentrylock + await + signal

```java
package com.erick.multithread.d04;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demo05 {
    private static ReentrantLock lock = new ReentrantLock(true);

    private static Condition room = lock.newCondition();

    public static void main(String[] args) {
        new Thread("t1") {
            @Override
            public void run() {
                lock.lock();
                try {
                    room.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread().getName() + " running");
                lock.unlock();
            }
        }.start();

        new Thread("t2") {
            @Override
            public void run() {
                lock.lock();
                System.out.println(Thread.currentThread().getName() + " running");
                room.signal();
                lock.unlock();
            }
        }.start();
    }
}
```

### 1.3 park + unpark

```java
package com.erick.multithread.d3;

import java.util.concurrent.locks.LockSupport;

public class Demo03 {
    public static void main(String[] args) {
        Thread firstThread = new Thread(() -> {
            LockSupport.park();
            System.out.println("线程一运行完毕");
        });

        firstThread.start();

        new Thread(() -> {
            System.out.println("线程二运行完毕");
            LockSupport.unpark(firstThread);
        }).start();
    }
}
```

## 2. 交替输出字符

- 五个线程交替输出abcde

### 2.1  synchronized

- synchronized + wait + notify

```java
package com.erick.multithread.d04;

public class Demo06 {

    private static final Object lock = new Object();

    private static String outPutChar = "a";

    public static void main(String[] args) {
        new Thread(() -> outPut("a", "b")).start();
        new Thread(() -> outPut("b", "c")).start();
        new Thread(() -> outPut("c", "d")).start();
        new Thread(() -> outPut("d", "e")).start();
        new Thread(() -> outPut("e", "a")).start();
    }

    private static void outPut(String threadChar, String nextThreadChar) {
        while (true) {
            synchronized (lock) {
                while (threadChar != outPutChar) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.println(outPutChar);
                outPutChar = nextThreadChar;
                lock.notifyAll();
            }
        }
    }
}
```

### 2.2 reentrylock

- reentrylock + await + signal
- 也可以利用多Condition的方式来避免虚假唤醒

```java
package com.erick.multithread.d04;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demo07 {
    private static ReentrantLock lock = new ReentrantLock();

    private static Condition room = lock.newCondition();

    private static String outputChar = "a";

    public static void main(String[] args) {
        new Thread(() -> output("a", "b")).start();
        new Thread(() -> output("b", "c")).start();
        new Thread(() -> output("c", "d")).start();
        new Thread(() -> output("d", "e")).start();
        new Thread(() -> output("e", "a")).start();
    }

    private static void output(String threadChar, String nextThreadChar) {
        while (true) {
            lock.lock();
            while (threadChar != outputChar) {
                try {
                    room.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(threadChar);
            outputChar = nextThreadChar;
            room.signalAll();
            lock.unlock();
        }
    }
}
```

### 2.3 park + unpark

```java
package com.erick.multithread.d3;

import java.util.concurrent.locks.LockSupport;

public class Demo06 {
    private static String baseChar = "a";
    private static Thread firstThread;
    private static Thread secondThread;
    private static Thread thirdThread;
    private static Thread fourthThread;
    private static Thread fifthThread;

    public static void main(String[] args) {
        firstThread = new Thread(() -> printCharacter("a", "b", secondThread));
        secondThread = new Thread(() -> printCharacter("b", "c", thirdThread));
        thirdThread = new Thread(() -> printCharacter("c", "d", fourthThread));
        fourthThread = new Thread(() -> printCharacter("d", "e", fifthThread));
        fifthThread = new Thread(() -> printCharacter("e", "a", firstThread));

        firstThread.start();
        secondThread.start();
        thirdThread.start();
        fourthThread.start();
        fifthThread.start();
    }

    private static void printCharacter(String printChar, String targetChar, Thread nextThread) {
        while (true){
            if (baseChar != printChar) {
                LockSupport.park();
            }
            System.out.println(Thread.currentThread().getName() + ":" + printChar);
            baseChar = targetChar;
            LockSupport.unpark(nextThread);
        }
    }
}
```



## 3. 交替输出奇偶数

- 两个线程，交替输出奇偶数

### 3.1  synchronized

- synchronized + wait + notify

```java
package com.erick.multithread.d04;

import java.util.concurrent.TimeUnit;

public class Demo08 {
    private static final Object lock = new Object();
    private static int number = 1;

    public static void main(String[] args) {
        new Thread(() -> output(), "t1-").start();
        new Thread(() -> output(), "t2-").start();
    }

    private static void output() {
        synchronized (lock) {
            while (true) {
                rest();
                System.out.println(Thread.currentThread().getName() + number);
                number++;
                lock.notify();
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static void rest() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### 3.2 reentrylock

- reentrylock + await + signal

```java
package com.erick.multithread.d04;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demo09 {

    private static int number = 0;
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition room = lock.newCondition();

    public static void main(String[] args) {
        new Thread(() -> output(), "t1-").start();
        new Thread(() -> output(), "t2-").start();
    }

    private static void output() {
        while (true) {
            rest();
            lock.lock();
            System.out.println(Thread.currentThread().getName() + number);
            number++;
            room.signal();
            try {
                room.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void rest() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### 3.3 park + unpark

```java
package com.erick.multithread.d3;

import java.util.concurrent.locks.LockSupport;

public class Demo09 {
    private static int number = 0;

    private static Thread firstThread;
    private static Thread secondThread;

    public static void main(String[] args) {
        firstThread = new Thread(() -> printNum(secondThread));
        secondThread = new Thread(() -> printNum(firstThread));

        firstThread.start();
        secondThread.start();

        // 触发
        LockSupport.unpark(firstThread);
    }

    private static void printNum(Thread nextThread) {
        for (int i = 0; i < 10; i++) {
            LockSupport.park();
            System.out.println(Thread.currentThread().getName() + ":" + number);
            number++;
            LockSupport.unpark(nextThread);
        }
    }
}
```

# 集合

