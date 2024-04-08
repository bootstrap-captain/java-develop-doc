# CAS-无锁并发

- CAS需要voliate支持

## 1. 线程不安全

- 可以通过 悲观锁-synchronized，来实现线程间的互斥，实现线程安全
- 也可以通过无锁并发CAS实现

```java
package com.erick.multithread.d04;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Demo01 {
    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        BankService service = new BankService();
        for (int i = 0; i < 20; i++) {
            Thread thread = new Thread(() -> service.drawMoney());
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.join();
        }
        log.info("money={}", service.total);
    }
}

@Slf4j
class BankService {
    public int total = 10;

    public void drawMoney() {
        if (total <= 0) {
            log.info("no money left");
            return;
        }

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        total--;
        log.info("get money");
    }
}
```

## 2.  乐观锁-CAS 

- 不加锁实现共享资源的保护
- Compare And Set
- JDK提供了对应的CAS类来实现不加锁

### 2.1 API

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
@Slf4j
class BankService {
    public AtomicInteger total = new AtomicInteger(10);

    public void drawMoney() {
        while (true) {
            int value = total.get(); // 获取最新值
            if (value <= 0) {
                log.info("no money left");
                return;
            }

            Sleep.sleep(1);

            /*参数一：原来的值
             * 参数二：变换后的值
             * 原来的值，一旦被其他线程修改了，是一个volatile修饰的，本线程立刻就会获取到，CAS就会失败，
             * 就会继续下次循环*/
            boolean result = total.compareAndSet(value, value - 1);
            if (result) {
                break;
            }
            log.info("CAS failure");
        }
    }
}
```

### 2.2 原理

- CAS 必须和 volatile结合使用
- get()方法获取到的是类的value，被volatile修饰。其他线程修改该变量后，会立刻同步到主存中，方便其他线程的cas操作
- compareAndSet内部，是通过系统的 lock cmpxchg(x86架构)实现的，也是一种锁

```bash
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

### 3.2 自增/自减

- 如果没有判读条件，那么多个线程访问同一个资源，也是线程安全的

```java
package com.erick.multithread.d04;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Demo01 {
    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        BankService service = new BankService();
        for (int i = 0; i < 1000000; i++) {
            Thread thread = new Thread(() -> service.drawMoneyFirst());
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.join();
        }
        log.info("money={}", service.total.get()); // 结果=0
    }
}

@Slf4j
class BankService {

    /**
     * 内部维护了一个private volatile int value;
     * 无参构造为0
     */
    public AtomicInteger total = new AtomicInteger(1000000);

    /*每次取一块钱*/
    public void drawMoneyFirst() {
        total.getAndDecrement();
    }
}
```

### 3.2 自定义操作

- 如果要控制超额支出问题，必须自定义CAS的while true

#### 方式1 - 普通运算

- 手写while true循环

```java
@Slf4j
class BankService {

    public AtomicInteger total = new AtomicInteger(50);

    /*每次取指定钱，带条件判断*/
    public void drawMoneyFirst(int bucks) {
        while (true) {
            int value = total.get();
            if (value < bucks) {
                log.info("no money left");
                break;
            }

            Sleep.sleep(1);

            boolean result = total.compareAndSet(value, value - bucks);
            if (result) {
                break;
            }
            log.info("CAS failure");
        }
    }
}
```

#### 方式2 - 函数式接口

```java
@Slf4j
class BankService {

    public AtomicInteger total = new AtomicInteger(50);

    public void drawMoneyFirst() {
        process(total, new Calculate());
    }

    private void process(AtomicInteger count, IntUnaryOperator operator) {
        while (true) {
            int currentValue = count.get();
            if (currentValue <= 0) {
                log.info("zero money");
                break;
            }
            int afterValue = operator.applyAsInt(currentValue);
            if (afterValue <= 0) {
                log.info("not enough money");
                break;
            }
            if (count.compareAndSet(currentValue, afterValue)) {
                log.info("success");
                break;
            }
            log.info("CAS failure");
        }
    }
}

class Calculate implements IntUnaryOperator {

    @Override
    public int applyAsInt(int operand) {
        /*可能涉及到复杂运算*/
        int afterValue = operand - 6;
        return afterValue;
    }
}
```

## 4. 原子引用

- 基本数据类型满足不了的一些其他计算场景，就要用到原子引用
- 用法和上面原子整数类似

```bash
# 比如大数BigDecimal， 商业计算

# 比如其他引用类型，如String
```

### 4.1 AtomicReference

```java
@Slf4j
class AccountService {
    public AtomicReference<BigDecimal> account = new AtomicReference<>(new BigDecimal("100"));

    public void drawMoney(BigDecimal bucks) {
        while (true) {
            BigDecimal prev = account.get();
            if (prev.compareTo(new BigDecimal("0")) <= 0) {
                log.info("no money left");
                break;
            }
            BigDecimal next = prev.subtract(bucks);
            if (next.compareTo(new BigDecimal(0)) <= 0) {
                log.info("not enough money");
                break;
            }
            if (account.compareAndSet(prev, next)) {
                log.info("success");
                break;
            }
            log.info("cas failure");
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
package com.erick.multithread.d04;

import com.erick.multithread.Sleep;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class Demo03 {
    public static void main(String[] args) throws InterruptedException {
        JackService jackService = new JackService();
        Thread firstThread = new Thread(() -> jackService.job());
        Thread secondThread = new Thread(() -> jackService.work());
        firstThread.start();
        secondThread.start();
        firstThread.join();
        secondThread.join();
        log.info(jackService.ref.get());
    }
}

@Slf4j
class JackService {
    public AtomicReference<String> ref = new AtomicReference<>("A");

    // 第一次就会交换成功
    public void job() {
        while (true) {
            String prev = ref.get();
            Sleep.sleep(1);
            if (ref.compareAndSet(prev, "B")) {
                log.info("success");
                break;
            }
            log.info("cas failure");
        }
    }

    public void work() {
        ref.compareAndSet("A", "B");
        ref.compareAndSet("B", "A");
    }
}
```

#### ABA解决

- 具体的值和版本号(版本号或者时间戳)
- 只要其他线程动过了共享变量(通过值和版本号)，就算cas失败
- 可以通过版本号，得到该值前前后后被改动了多少次

```java
@Slf4j
class JackService {
    /*初试版本号可以设置为0 */
    public AtomicStampedReference<String> ref = new AtomicStampedReference<>("A", 0);

    // 第一次会交换成功
    public void job() {
        while (true) {

            int prevStamp = ref.getStamp();
            String prevValue = ref.getReference();
            Sleep.sleep(1);
            boolean result = ref.compareAndSet(prevValue, "B", prevStamp, prevStamp + 1);
            if (result) {
                log.info("success");
                break;
            }
            log.info("cas failure");
        }
    }

    public void work() {
        ref.compareAndSet("A", "B", 0, 1);
        ref.compareAndSet("B", "A", 1, 2);
    }
}
```

### 4.3 AtomicMarkableReference

- 线程a在执行CAS操作时，其他线程反复修改数据，但是a线程只关心最终的结果是否变化了
- ABABA场景

```java
@Slf4j
class JackService {

    private boolean initialFlag = true;
    public AtomicMarkableReference<String> ref = new AtomicMarkableReference<>("A", true);

    public void job() {
        while (true) {
            String prevValue = ref.getReference();

            Sleep.sleep(1);
            boolean result = ref.compareAndSet(prevValue, "B", true, false);
            if (result) {
                log.info("success");
                break;
            }
            log.info("cas failure");
        }
    }

    public void work() {
        ref.compareAndSet("A", "B", true, false);
        ref.compareAndSet("B", "A", false, true);
        ref.compareAndSet("A", "B", true, false);
        // ref.compareAndSet("B", "A", false, true);
    }
}
```

## 5. 原子数组

- 上述原子整数和原子引用，只是针对一个对象的
- 原子数组，可以存放上面的数组

### 5.1 不安全

- 可以将数组中的每个元素单独拿出来，通过原子整数来解决

```java
package com.erick.multithread.d04;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

public class Demo04 {
    public static void main(String[] args) throws InterruptedException {
        BankingService service = new BankingService();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(() -> service.change());
            thread.start();
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.join();
        }

        service.log();
    }
}

@Slf4j
class BankingService {
    private int[] accounts = new int[2];

    public void change() {
        for (int i = 0; i < 1000; i++) {
            accounts[0] = accounts[0] - 1;
            accounts[1] = accounts[1] + 1;
        }
    }

    public void log() {
        log.info("arr-0={}", accounts[0]);
        log.info("arr-1={}", accounts[1]);
    }
}
```

### 5.2 原子数组

- AtomicIntegerArray
- AtomicLongArray
- AtomicReferenceArray

```java
@Slf4j
class BankingService {
    private AtomicIntegerArray accounts = new AtomicIntegerArray(2);

    public void change() {
        for (int i = 0; i < 1000; i++) {
            /*参数一： 索引
             * 参数二： 改变的值
              也可以自定义while true循环
             */
            accounts.addAndGet(0, 2);
            accounts.addAndGet(1, -2);
        }
    }

    public void log() {
        log.info("arr-0={}", accounts.get(0));
        log.info("arr-1={}", accounts.get(1));
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

