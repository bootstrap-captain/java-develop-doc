# AQS

- AbstractQueuedSynchronizer：抽象类，阻塞式锁和相关的同步器工具的框架

## 1. AbstractQueuedSynchronizer

### 1.1 state

- 表示资源的状态(独占模式， 共享模式)
- 子类需要定义如何维护这个状态，控制如何获取锁和释放锁
- 独占模式：只有一个线程能够访问资源
- 共享模式：允许多个线程访问资源，但对线程的最大数有限制

```java
private volatile int state;

protected final int getState() {
     return state;
}

protected final void setState(int newState) {
     state = newState;
}

// 乐观锁机制设置state状态，防止多个线程来修改state
protected final boolean compareAndSetState(int expect, int update) {
    return U.compareAndSetInt(this, STATE, expect, update);
}
```

### 1.2 阻塞队列

- 提供了基于FIFO的等待队列，类似于Monitor中的EntryList

### 1.3 等待队列

- 条件变量来实现等待，唤醒机制， 支持多个条件变量，类似于Monitor的WaitSet

## 2. 自定义AQS锁

- 用AQS写一个不可重入锁

```bash
# 子类继承Lock接口
# 并实现下面的方法： 默认抛出UnsupportedOperationException
- tryAcquire
- tryRealease
- tryAcquiredShared
- tryReleaseShared
- isHeldExclusively
```

### 2.1 同步器类

- 继承AbstractQueuedSynchronizer，重写某些方法

```java
package com.erick.multithread.aqs;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;

public class ErickSync extends AbstractQueuedSynchronizer {

    /*尝试加锁： 修改AbstractQueuedSynchronizer的state属性
     * 1. 修改state属性
     * 2. 设置当前线程为Owner中的，类似Monitor的Owner*/
    @Override
    protected boolean tryAcquire(int arg) {
        /*修改AbstractQueuedSynchronizer的state属性
         * 0: 未加锁
         * 1: 加锁*/
        if (compareAndSetState(0, 1)) {
            // 设置当前线程为Owner的主人
            // AbstractOwnableSynchronizer方法中的
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        return false;
    }

    /*尝试解锁*/
    @Override
    protected boolean tryRelease(int arg) {
        setState(0); // 不用担心其他线程竞争
        setExclusiveOwnerThread(null); // 清空owner中的线程
        return true;
    }

    /*是否持有当前独占锁*/
    @Override
    protected boolean isHeldExclusively() {
        return getExclusiveOwnerThread() == Thread.currentThread();
    }

    /*AbstractQueuedSynchronizer类中维护的属性*/
    public Condition newCondition() {
        return new ConditionObject();
    }
}
```

### 2.2 自定义锁

```java
package com.erick.multithread.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/*实现Lock接口*/
@Slf4j
public class ErickLock implements Lock {

    private static final long serialVersionUID = 7373984872572414699L;

    /*其实就是调用同步器来进行加锁解锁*/
    private ErickSync erickSync = new ErickSync();

    /*加锁：不成功则会进入类似EntryList的阻塞队列*/
    @Override
    public void lock() {
        erickSync.acquire(1); // 参数暂时没用
    }

    /*可打断的加锁*/
    @Override
    public void lockInterruptibly() throws InterruptedException {
        erickSync.acquireInterruptibly(1);
    }

    /*尝试加锁： 只尝试一次*/
    @Override
    public boolean tryLock() {
        return erickSync.tryAcquire(1);
    }

    /*尝试加锁带超时：只尝试一次*/
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return erickSync.tryAcquireNanos(1, unit.toNanos(time));
    }

    /*释放锁：并唤醒阻塞的线程*/
    @Override
    public void unlock() {
        erickSync.release(1);
    }

    /*创建条件变量*/
    @Override
    public Condition newCondition() {
        return erickSync.newCondition();
    }
}
```

### 2.3 测试代码

```java
package com.erick.multithread.aqs;

import com.erick.multithread.Sleep;
import lombok.extern.slf4j.Slf4j;

public class Demo01 {
    public static void main(String[] args) {
        LockTestService testService = new LockTestService();

        new Thread(() -> testService.work()).start();
        new Thread(() -> testService.work()).start();
    }
}

@Slf4j
class LockTestService {
    private ErickLock lock = new ErickLock();

    public void work() {
        try {
            lock.lock();
            log.info("{} will sleep", Thread.currentThread().getName());
            Sleep.sleep(2);
        } finally {
            log.info("{} unlock", Thread.currentThread().getName());
            lock.unlock();
        }
    }
}
```

# ReentryLock

- 是AQS的一种实现

## 1. 可重入锁

- 可重入锁：一个线程已经获取锁，因为该线程是该锁主人，跨方法获取该锁时，依然成功
- 不可重入：同一线程跨方法获取就会造成死锁，部分锁就是这种情况

### 1.1 重入

```java
package com.erick.multithread.aqs;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

public class Demo02 {
    public static void main(String[] args) {
        RetryTestService testService = new RetryTestService();

        /**
         * 111-加锁
         * 222-加锁
         * 222-解锁
         * 111-解锁
         */
        new Thread(() -> testService.first()).start();
    }
}

@Slf4j
class RetryTestService {
    private ReentrantLock lock = new ReentrantLock();

    public void first() {
        try {
            lock.lock();
            log.info("111-加锁");

            second();

        } finally {
            /*unlock必须放在finally中，保证锁一定可以释放*/
            lock.unlock();
            log.info("111-解锁");
        }
    }

    public void second() {
        try {
            lock.lock();
            log.info("222-加锁");
        } finally {
            lock.unlock();
            log.info("222-解锁");
        }
    }
}
```

### 1.2 原理

## 2. 可打断锁

```bash
# 不可打断锁
- 如：尝试获取synchronized锁的线程，如果获取不到锁，则会在对应Monitor的EntryList中一直死等

# 可打断锁
- 避免一个线程一直等待当前要获取的锁
- 被动的方式避免死锁

#  lock.lockInterruptibly();
- 线程没有获取到锁，进入类似EntryList中等待
- 这种等待可以被打断
```

### 2.1 打断

```java
package com.erick.multithread.aqs;

import com.erick.multithread.Sleep;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

public class Demo03 {
    public static void main(String[] args) {
        InterruptLockService service = new InterruptLockService();
        Thread firstThread = new Thread(() -> service.work());
        firstThread.start();

        Sleep.sleep(1);

        /*线程2在1s后打断自己获取锁的阻塞状态*/
        Thread secondThread = new Thread(() -> service.work());
        secondThread.start();
        secondThread.interrupt();
    }
}

@Slf4j
class InterruptLockService {
    private ReentrantLock lock = new ReentrantLock();

    public void work() {
        try {
            try {
                lock.lockInterruptibly();
                log.info("{} 加锁", Thread.currentThread().getName());

                log.info("doing business");
                Sleep.sleep(5);

            } catch (InterruptedException e) {
                log.error("{} 锁打断", Thread.currentThread().getName());
                throw new RuntimeException(e);
            }
        } finally {
            lock.unlock();
            log.info("{} 释放锁", Thread.currentThread().getName());
        }
    }
}
```

### 2.2 原理



## 3. 公平锁

```bash
# 1. 不公平锁
- synchronized锁
- 一个线程持有锁时，其他线程进入锁的 EntryList
- 获的锁的线程任务执行完后，Owner清空，EntryList中的线程随机挑选一个给锁，而不是按照进入的顺序先到先得

# 2. 公平锁： 通过ReentranLock实现
- public ReentrantLock(boolean fair) 
- 默认是非公平锁，传参为true，公平锁
```

### 3.1 公平

```java
package com.erick.multithread.aqs;

import com.erick.multithread.Sleep;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

public class Demo04 {
    public static void main(String[] args) {
        FairLockService fairLockService = new FairLockService();
        for (int i = 0; i < 10; i++) {
            String name = i + "号线程---";
            new Thread(() -> fairLockService.work(), name).start();
        }
    }
}

@Slf4j
class FairLockService {
    private ReentrantLock lock = new ReentrantLock(true);

    public void work() {
        try {
            lock.lock();
            log.info("{} 加锁", Thread.currentThread().getName());
            Sleep.sleep(2);
        } finally {
            log.info("{} 释放锁", Thread.currentThread().getName());
            lock.unlock();
        }
    }
}
```

### 3.2 原理



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

- 类似WaitSet， 但是有可以有多个，可以和wait/notify更好的结合
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
package com.erick.multithread.aqs;

import com.erick.multithread.Sleep;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Demo05 {
    public static void main(String[] args) {
        MultiWaitService service = new MultiWaitService();
        for (int i = 0; i < 3; i++) {
            new Thread(() -> service.boyWork(), "BOY").start();
            new Thread(() -> service.girlWork(), "GIRL").start();
        }

        Sleep.sleep(2);

        new Thread(() -> service.deliverCigarette()).start();
        Sleep.sleep(2);
        new Thread(() -> service.deliverDinner()).start();

    }
}

@Slf4j
class MultiWaitService {
    private ReentrantLock lock = new ReentrantLock();
    private Condition boyRoom = lock.newCondition();
    private Condition girlRoom = lock.newCondition();

    private boolean hasCigarette = false;
    private boolean hasDinner = false;

    public void boyWork() {
        try {
            lock.lock();
            while (!hasCigarette) {
                try {
                    log.info("{} 没烟，男人休息", Thread.currentThread().getName());
                    boyRoom.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("{} 烟来了，男人干活", Thread.currentThread().getName());
            }
        } finally {
            lock.unlock();
        }
    }

    public void girlWork() {
        try {
            lock.lock();
            while (!hasDinner) {
                try {
                    log.info("{} 没饭，女人休息", Thread.currentThread().getName());
                    girlRoom.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("{} 没饭，女人干活", Thread.currentThread().getName());
            }
        } finally {
            lock.unlock();
        }
    }

    public void deliverCigarette() {
        try {
            lock.lock();
            log.info("======================烟来了");
            hasCigarette = true;
            boyRoom.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void deliverDinner() {
        try {
            lock.lock();
            log.info("======================饭来了");
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

## 5.4  打断park线程

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

## 
