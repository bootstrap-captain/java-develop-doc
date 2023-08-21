# 事务

- InnoDB支持事务和分布式事务，MyISAM不支持事务
- 一组逻辑操作单元，使数据从一种状态变换到另一种状态
- 处理原则：保证所有事务都作为<font color=orange>一个工作单元</font>来执行，即使出现故障，都不能改变这种执行方式

## ACID特性

### 1. Atomicity

- 事务是一个不可分割的工作单位，要么全部提交，要么全部失败会滚，没有中间状态

### 2. Consistency

- 数据从一个<font color=orange>合法状态</font>变换到另一个<font color=orange>合法状态</font>，和具体的业务相关
- <font color=orange>合法状态</font>：是和具体的业务相关的，满足<font color=orange>预定的约束</font>。满足这个状态，数据就是一致的，不满足就不是

```bash
# 例子一：不是一致的，因为余额不能小于0
- A账户有200，B账户有0
- A账户转给B账户300
- 则A剩余-100， B账户有300

# 例子二：不是一致的，因为默认A+B=200
- A账户有200，B账户有0
- A账户转给B账户50， 但B没收到
- 则A剩余150， B账户有0

# 例子三：不是一致的，因为姓名唯一
- 将表中 姓名 字段设置为 唯一约束
- 如果修改表中数据，如果姓名不唯一，则破坏了事务的一致性
```

### 3. Isolation

- 一个事务的执行，不能被其他事务干扰
- 一个事务的内部操作时，对应的数据，对<font color=orange>并发</font>的其他事务是隔离的，并发执行的各个事务之间，不能互相干扰

```bash
# A账户有200，B账户有0。 A往B转两次，每次转50。两次操作在两个事务中执行
# 如果无法保证隔离性，则会出现下面情况
```

![image-20230807142654691](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230807142654691.png)

### 4. Duration

- 一个事务一旦被提交，它对数据库中数据的改变就是永久性的，接下来的其他操作和数据库故障，不应该对其有影响
- 持久性是通过<font color=orange>事务日志</font>来保证的。包含<font color=orange>Redo日志</font>和<font color=orange>Undo日志</font>

## 使用事务

### 1. 隐式事务

- AUTOCOMMIT

```sql
# 默认ON
# 每个DML都是一个独立的事务，都是自动提交
SHOW VARIABLES LIKE 'autocommit';

INSERT INTO book1 (name) VALUE ('CCCC');
```

```sql
# 关闭后，多个DML就都会被归结到一个事务中，需要手动提交
SET AUTOCOMMIT=FALSE;

# 不加COMMIT，则无法生效
INSERT INTO book1 (name) VALUE ('AAAAA');

INSERT INTO book1 (name) VALUE ('BBBBB');

COMMIT;
```

### 2. 显式使用

- 和AUTOCOMMIT无关

```sql
# 1. 开启事务
START TRANSACTION READ ONLY;       # 开启一个只读的事务，不能进行写
START TRANSACTION READ WRITE;      # 开启一个可读可写的事务
START TRANSACTION;                 # 默认可读可写     
BEGIN;                             # 开启可读可写

# 2. 一系列DML操作: 也可以是一条语句

# 3. 结束状态
# 提交
COMMIT;

# 回滚到上一次事务开始点
ROLLBACK;
```

```sql
# 带SAVEPOINT: 可以会滚到某个状态
START TRANSACTION;

INSERT INTO book1 (name) VALUE ('ERICK');

INSERT INTO book1 (name) VALUE ('LUCY');

SAVEPOINT first;

INSERT INTO book1 (name) VALUE ('Nancy');

ROLLBACK TO SAVEPOINT first; # 则'Nancy'不会被添加

COMMIT;
```

```sql
START TRANSACTION;

INSERT INTO book1 (name) VALUE ('apple');

INSERT INTO book1 (name) VALUE ('peach');

SAVEPOINT first;

INSERT INTO book1 (name) VALUE ('melon');

RELEASE SAVEPOINT first; #删除某个SAVEPOINT

COMMIT;
```

### 3. 不受AUTOCOMMIT影响

```SQL
# 数据定义语言(Data Definition Language) DDL
- 数据库，表，视图，存储过程等
- 当使用CREATE, ALTER, DROP 等去修改数据库对象时，就会自动提交当前事务

# 修改mysql库中的信息， 如mysql.user表
- ALTER USER, CREATE USER, DROP USER, GRANT, RENAME USER, REVOKE, SET PASSWORD等操作

# 事务控制
BEGIN;
....

BEGIN; # 又开启了一个事务，则会自动提交上一个事务

# 关于锁定的语句: 自动提交前面语句的事务
LOCK TABLES, UNLOCK TABLES 
```

## 隔离级别

```sql
CREATE TABLE student
(
    studentno    INT,
    name  VARCHAR(20),
    class VARCHAR(20),
    PRIMARY KEY (studentno)
);

INSERT INTO student (studentno, name, class) VALUE (1, 'erick', '一班');
```

### 1. 并发问题

- 访问相同数据的事务，如果<font color=orange>并发执行</font>，可能会有一些问题

#### 脏写(Dirty Write)

- 对于两个事务Session-A和Session-B，如果B修改了<font color=orange>未提交</font>事务A<font color=orange>修改过</font>的数据，就是脏写

![image-20230807152316885](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230807152316885.png)

#### 脏读(Dirty Read)

- A读取了已经被B更新，但B还没提交的字段。如果B回滚，A读取的数据就是临时且无效的

![image-20230807152629320](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230807152629320.png)

#### 不可重复读(Non Repeated Read)

- 事务B中，隐式提交了几次数据，导致事务A中，每次读取的结果都不一样

![image-20230807153729181](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230807153729181.png)

#### 幻读(Phantom)

- Session-A在一个事务中，读取到了Session-B<font color=orange>新增</font>的数据，也就是<font color=orange>幻影记录</font>
- 如果B是删除了数据，则不算幻读

![image-20230807153932214](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230807153932214.png)

### 2. 隔离级别

- 上面四种并发问题严重性：脏写>脏读>不可重复读>幻读
- 隔离级别越高，并发越低；隔离级别越低，并发越高
- 四种隔离级别，都能解决脏写的问题，因为脏写不能容忍
- MySQL默认： REPEATABLE READ

![image-20230807155057395](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230807155057395.png)

```sql
# 默认： REPEATABLE-READ
SHOW VARIABLES LIKE 'transaction_isolation';

# 修改：READ UNCOMMITTED | READ COMMITTED | REPEATABLE READ | SERIALIZABLE
SET GLOBAL|SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

SET GLOBAL|SESSION TRANSACTION_ISOLATION='';

# 修改配置文件
```

数据准备

```sql
CREATE TABLE account
(
    id      INT,
    name    VARCHAR(20),
    balance INT,
    PRIMARY KEY (id)
);

INSERT INTO account (id, name, balance)
VALUES (1, '张三', 100),
       (2, '李四', 0);
```

#### READ UNCOMMITTED-脏读

- 会话1还没提交的数据，被会话2读取到了

```SQL
SET GLOBAL TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
```

```sql
# 会话1
BEGIN;

UPDATE account
SET balance=200
WHERE id = 1;

#  如果rollback了，则会话2读取到了临时且无效的数据
```

```SQL
# 会话2： 读取到了balance=200
SELECT *
FROM account
WHERE id = 1;
```

#### READ COMMITTED-不可重复读

- 能够解决上面脏读
- 但是会有不可重复读

```SQL
SET GLOBAL TRANSACTION ISOLATION LEVEL READ COMMITTED;
```

```sql
#  会话1: 隐式的提交了几次数据
UPDATE account
SET balance=balance - 50
WHERE id = 1;


UPDATE account
SET balance=balance - 50
WHERE id = 1;
```

```SQL
# 会话2： 在一个会话中，读取的值不同
BEGIN;

SELECT *
FROM account
WHERE id = 1;

SELECT *
FROM account
WHERE id = 1;
```

#### REPEATABLE READ-幻读

- MySQL默认级别
- 能解决上面的不可重复读的问题
- 但是存在幻读问题

```sql
SET GLOBAL TRANSACTION ISOLATION LEVEL REPEATABLE READ;
```

```SQL
# 会话1: 先开启
BEGIN;

# 读取不到，这个时候开启会话2
SELECT *
FROM account
WHERE id = 3;

# 依然读取不到：没读取到
SELECT *
FROM account
WHERE id = 3;

# 用这个可以证明：读取到了
INSERT INTO account (id, name, balance) VALUE (3, 'lucy', 200);

ROLLBACK ;
```

```sql
#  会话2：
INSERT INTO account (id, name, balance) VALUE (3, 'lucy', 200);
```

#### SERIALIZABLE

- 通过添加锁的方式，使得多个事务之前，完全是串行执行，并发度较低

# 事务日志

- 事务的隔离型是由<font color=orange>锁机制</font>实现的
- 事务的原子性，一致性和持久性由事务的redo日志和undo日志来保证

## REDO LOG

- <font color=orange>重做日志</font>：提供再写入操作，恢复提交事务修改的页操作，保证事务的<font color=orange>持久性</font>
- 存储引擎InnoDB生成的日志，记录<font color=orange>物理级别</font>上的页修改操作，比如页号xx，偏移量是yyy，写入了zzz数据
- 存储引擎以<font color=orange>页为单位</font>管理存储空间。访问页时，先读<font color=orange>磁盘上</font>的页到内存中的<font color=orange>Buffer Pool</font>。所有变更都<font color=orange>先更新Buffer Pool</font>中数据，然后Buffer poll中的<font color=orange>脏页</font>会以一定的频率被刷入磁盘文件<font color=orange>(checkpoint机制)</font>

### 1. 引入原因

- 一个事务提交后(业务成功)，数据先在Buffer Pool中写，然后等待checkpoint刷盘完成持久化
- 假如Buffer Poll中已经有数据了，但还未刷盘。此时服务器宕机，Buffer Poll中数据丢失，最终没有完成持久化

![image-20230813102504050](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230813102504050.png)

```bash
# 暴力解决方案
- 事务提交完毕后，立刻把该事务所修改的页面刷盘

# 缺点一： 修改量和刷盘量工作 不成比例
- 仅仅修改了某个页面中的一个字节，刷盘时需要把一个完整的页从内存中刷新到磁盘中
- 一个页面是16kb，只修改一个字节就要刷新16kb的数据到磁盘上，性能消耗

# 缺点二：随机IO刷新慢
- 一个事务可能包含很多个语句，牵涉到修改多个页面
- 这些页面可能并不相邻，刷盘时候进行随机IO，比顺序IO要慢
```

### 2. redo log

- InnoDB引擎：REDO LOG是<font color=orange>Write Ahead Logging， WAL技术</font>
- 记录内容：某个事务将某个表中<font color=orange>第10页</font>中偏移量为<font color=orange>100</font>的字节值从<font color=orange>1</font>改为<font color=orange>2</font>，存储表空间ID, 页号，偏移量，更新的值
- 先写日志，再写磁盘，只有日志( redo log buffer 或 redo log file)写入成功，才算事务提交成功
- 如果发生宕机且Buffer Pool数据未刷盘到磁盘时，通过redo log来恢复，保证事务的持久性

![image-20230813105442456](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230813105442456.png)

```bash
# 1 事务开启后，先将原始数据从磁盘中读到Buffer Pool中，修改数据的内存拷贝
# 2. 每次执行一个sql，就会写入一个记录到redo log buffer，记录的是数据被修改后的值
# 3. 将redo log buffer的数据写入到redo log file(追加写)
# 4. 定期将buffer pool中的数据刷新到磁盘文件中

# 优点： 
- redo 日志降低了刷盘频率
- redo日志占用的空间非常小
```

### 3 刷盘策略

- redo log bufffer刷盘到redo log file中的频率

```bash
# page cache
- OS层面的，为提高文件写入效率的一个优化，真正的写入会交给OS来决定(如page cache足够大了)

#  策略: 0,1,2
SHOW VARIABLES LIKE 'innodb_flush_log_at_trx_commit';
```

#### 0值

```bash
# innodb_flush_log_at_trx_commit=0
- 每次事务提交，不进行刷盘操作
- InnoDB有一个后台线程，每隔1s，就把redo log buffer中的内容写入到page cache，并调用刷盘操作

# 特点
- 会丢失上一次刷盘距离mysql宕机之间的数据
```

![image-20230813112837831](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230813112837831.png)

#### 1(默认值)

```bash
# innodb_flush_log_at_trx_commit=1
- 每次事务提交时，将redo log buffer中数据写入page cache中，并立刻调用OS，将数据同步到redo log file中
- 假如事务持续时间长，则还没提交的事务的执行sql操作，也会被InnoDB后台线程(0值场景)进行同步

# 特点
- 只要事务提交成功(刷盘redo log file成功才算成功)，则redo log记录一定在硬盘中，不会有任何数据丢失
- 事务执行期间mysql挂了，虽然redo log buffer数据丢了，但是事务并没有提交，所以满足持久性
```

![image-20230813111828299](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230813111828299.png)

#### 2值

```bash
# innodb_flush_log_at_trx_commit=2
- 每次事务提交时，只把redo log buffer内容写入到page cache，不进行同步，由OS决定什么时候同步到redo log file中

# 特点
- 如果数据从redo log buffer更新到了page cache中了，OS挂了，则丢失page cache中的数据
```

![image-20230813112207156](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230813112207156.png)

### 4. redo log buffer

#### 4.1 mini-transaction

- MySQL把底层页面中的一次原子访问的过程称为一个mini-transaction(mtr)

```bash
# 场景：向某个索引对应的B+树中插入一条记录的过程就是一个mtr
- 一个mtr会包含一组redo日志(B+树中插入一条记录，可能会涉及到多个页面的变化)
- redo日志就是记录的具体哪个页面的哪个位置的数据发生了什么样的变化
- 进行崩溃恢复时，这一组redo日志作为一个不可分割的整体
```

![image-20230813121013975](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230813121013975.png)

#### 4.2 redo log buffer结构

- 保存在内存中，比较容易丢失
- Mysql服务启动后，向OS申请一大片<font color=orange>连续内存空间</font>，并被划分为若干个连续的<font color=orange>redo log block</font>，一个block是<font color=orange>512字节</font>
- 开启事务后，每次执行一条sql语句，就会写入若干个redo log buffer记录

![image-20230813103555500](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230813103555500.png)

```sql
# redo log buffer, 默认16M,最大4096M，最小1M
SHOW VARIABLES LIKE 'innodb_log_buffer_size';
```

#### 4.3 写入过程

- <font color=orange>顺序写</font>，先往前面的block中写，当该block的空闲空间用完后，再往下一个block中写
- 每个mtr运行过程中，产生的日志会暂存到一个地方，当该mtr结束时，将该mtr的redo日志全部复制到log buffer中

![image-20230813152823659](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230813152823659.png)

- 一个mtr执行过程，会产生若干条redo日志，这些redo log是一个不可分割的组。同一事务中的多个mtr，可以不连续
- 多个事务可以并发执行：t1代表事务1，t2代表事务2

![image-20230813153632841](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230813153632841.png)

### 5. redo log file

- 保存在磁盘上的，持久化文件

#### 5.1 基本参数

```sql
# 存放路径：        默认数据目录相对路径： /var/lib/mysql
SHOW VARIABLES LIKE 'innodb_log_group_home_dir';         # 默认值：   ./

# 文件个数
# 默认是2个文件， 先把文件内存空间大小开辟好
SHOW VARIABLES LIKE 'innodb_log_files_in_group';

# 单个文件大小：默认48M
# 最大值是512G， 指整个redo log 系列文件之和， 即innodb_log_files_in_group*innodb_log_file_size
SHOW VARIABLES LIKE 'innodb_log_file_size';


# 默认的文件
-rw-r----- 1 mysql mysql 50331648 Aug  8 09:27 ib_logfile0
-rw-r----- 1 mysql mysql 50331648 Aug  1 13:47 ib_logfile1
```

#### 5.2 checkpoint机制

- <font color=orange>已同步到.ibd文件的数据</font>或者<font color=orange>rollback后的事务</font>，应该从redo log file中删除掉
- <font color=orange>write pos</font>：当前记录的位置，一边写一边后移
- <font color=orange>checkpoint</font>：当前要擦除的位置，也是往后移动

![image-20230813105442456](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230813105442456.png)

```bash
- 每次刷盘redo log buffer中记录到日志文件组中，write pos的位置就往后移动更新
- 服务器正常：  执行6步骤时，清空加载过的redo log buffer中记录，checkpoint向后移动
- 服务器宕机后： 从redo log file加载数据，然后一系列最终进入xxx.idb文件，checkpoint向后移动

# 如果write pos追上checkpoint，则表示redo log file已经满了，mysql会停下，将checkpoint来推进一下
```

![image-20230813155641835](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230813155641835.png)

## UNDO LOG

- 保证事务的原子性。在事务的<font color=orange>写数据</font>的前置操作：先写入一个undo log

# 事务锁

- 数据库中，除传统的计算资源(CPU, RAM, I/O)的争用意外，<font color=orange>数据</font>也是一种供多个用户共享的资源
- 锁机制：为MySQL各个隔离级别提供了保证

## 1. 并发事务访问相同记录

### 1.1 读-读

- 并发事务相继读取相同的记录，读取操作本身不会对记录产生任何影响，并不会引发问题

### 1.2 写-写

- 并发事务相继对相同的记录做出改动
- 可能发生脏写问题，任何一种隔离级别都不允许这种情况发生。
- 多个未提交事务相继对一条记录做改动时，需要让它们<font color=orange>排队执行</font>，这个排队的过程其实就是通过<font color=orange>锁</font>来实现
- 锁：内存中的结构

```bash
# 1. 一开始时，记录不会和任何的锁结构来进行关联

# 2. 一个事务要对记录做改动时，先看内存中有没有与这条记录关联的锁结构
- 如果没有，则在内存中生成一个锁结构和该条记录关联

# 3. 新的事务过来，也想修改改条记录，
- 发现有一个锁与之关联了，则也生成一个锁结构和该记录关联，is_waiting状态设置为true，同时等待

# 4. 占用行记录的事务执行完毕后，释放当前事务的锁结构，同时检查行记录数据的其他锁对应的事务，然后去争抢
```

![image-20230813181957498](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230813181957498.png)

### 1.3 读-写

- 可能发生<font color=orange>脏读</font>，<font color=orange>不可重复读</font>，<font color=orange>幻读</font>
- 利用加锁或者MVCC来解决

## 2. 锁分类-数据操作

### 2.1 读锁

- Shared Lock,   S锁，针对同一个数据，多个事务的读操作可以同时进行而不互相影响

### 2.2 写锁

- Exclusive Lock，X锁，当前写操作没完成时，阻断其他写锁和读锁

|      | X锁    | S锁    |
| ---- | ------ | ------ |
| X锁  | 不兼容 | 不兼容 |
| S锁  | 不兼容 | 兼容   |

### 锁定读

- 即可加共享锁，也可加排他锁

```sql
SELECT ... FOR SHARE;               # 共享锁
SELECT ... LOCK IN SHARE MODE;      # 共享锁

SELECT ... FOR UPDATE;             # 排他锁
```

```SQL
# 两个共享锁: 不会阻塞

# 会话一：
SELECT * FROM account FOR SHARE;

# 会话二：
SELECT * FROM account FOR SHARE;
```

```sql
# 一个共享锁，一个排他锁： 会阻塞

# 会话一：必须事务结束后，会话二才会获取到记录
BEGIN;

SELECT * FROM account FOR UPDATE;

COMMIT;

# 会话二：
SELECT * FROM account FOR SHARE;
```

```sql
# 两个排他锁： 会阻塞
# 会话一：必须事务结束后，会话二才会获取到记录
BEGIN;

SELECT * FROM account FOR UPDATE;

COMMIT;

# 会话二：
BEGIN;

SELECT * FROM account FOR UPDATE;

COMMIT;
```

```sql
# 8.0新特性

# 1. 如果会话中包含一个排他锁，当前会话，超时等待时间
SHOW VARIABLES LIKE 'innodb_lock_wait_timeout';

# 2. NOWAIT: 如果获取不到锁，则立刻报错返回

# 3. SKIP LOCK： 也会立刻返回，只是返回的结果中不包含被锁定的行


# 会话一：
BEGIN;

SELECT * FROM account FOR UPDATE;

COMMIT;

# 会话二：
BEGIN;

# [HY000][3572] Statement aborted because lock(s) could not be acquired immediately and NOWAIT is set.
SELECT * FROM account FOR UPDATE NOWAIT;

COMMIT;
```

## 3. 锁分类-粒度

- 为了尽可能提高数据库的并发度，每次锁定的数据范围越小越好
- 理论上，每次只操作当前操作的数据的方案能得到最大的并发度，但是管理锁是很耗资源的事情(涉及获取，检查，释放等动作)
- 数据库系统需要在<font color=orange>并发性能</font>和<font color=orange>系统性能</font>两方面进行平衡

### 3.1 表锁

#### 表锁

- 锁定整个表，是MySQL中最基本的锁策略，<font color=orange>不依赖与存储引擎</font>，表锁是<font color=orange>开销最小</font>的策略，粒度最大
- 表级锁一次会将整个表锁定，所以可以很好的<font color=orange>避免死锁</font>，但是<font color=orange>并发性低</font>
- 一般不建议对InnoDB添加表锁，因为InnoDB支持行锁

```sql
# 以MyISAM为例，当然InnoDB也是可以的
CREATE TABLE dog
(
    id   INT,
    name VARCHAR(20),
    PRIMARY KEY (id)
) ENGINE = MyISAM;

# 查看当前都有哪些表带锁
SHOW OPEN TABLES WHERE in_use>0;
```

```sql
# 基本语法
LOCK TABLES dog READ;        # 加读锁
LOCK TABLES dog WRITE;       # 加写锁

UNLOCK TABLES;               # 释放锁，不用指定哪个表
```

| 锁类型 | 当前会话可读 | 当前会话可写 | 当前会话可操作其他表 | 其他会话可读 | 其他会话可写 |
| ------ | ------------ | ------------ | -------------------- | ------------ | ------------ |
| 读锁   | Y            | N            | N                    | Y            | N，等        |
| 写锁   | Y            | Y            | N                    | N，等        | N，等        |

#### 意向锁

- InnoDB支持<font color=orange>多粒度锁</font>，允许<font color=orange>行级锁和表级锁共存</font>

```bash
# 解决的问题： 一个事务要加表级锁时，需要全部遍历所有记录，确保每一行都没有锁
- 事务t1对A-表中某个行加上了锁
- 事务t2要对A-表中添加 表级别 的x锁或s锁，首先要确定该表上是否有其他锁(表锁/行锁)
- 事务t2必须遍历A-表中所有行，才能确保是否有锁，比较耗费性能
```

- 事务给某一行数据加上了<font color=orange>排他锁</font>，数据库会<font color=orange>自动</font>给更大一级的空间，比如数据页或数据表加上<font color=orange>意向锁</font>(锁标识)，告诉其他事务这个数据页或数据表已经有其他人上过排他锁了
- 由InnoDB存储引擎<font color=orange>自己维护的</font>，在为数据行加共享/排他锁之前，会先在表级别添加意向锁

```sql
CREATE TABLE dog
(
    id   INT,
    name VARCHAR(20),
    PRIMARY KEY (id)
);

# 会话一: 为某个行加锁， 则存储引擎会自动给该表加上一个意向排他锁
# 只有commit后，x锁才会释放，会话二才会开始执行
BEGIN;

SELECT *
FROM dog
WHERE id = 1 FOR
UPDATE;

COMMIT;

# 会话二：锁定某个表
LOCK TABLES dog READ;
```

|        | 意向共享锁IS | 意向排他锁IX |
| ------ | ------------ | ------------ |
| 共享锁 | 兼容         | 互斥         |
| 排他锁 | 互斥         | 互斥         |

- 意向锁之间是互相兼容的，因为只是一个锁标识，多个行记录，可能都会添加对应的意向锁标识

|              | 意向共享锁IS | 意向排他锁IX |
| ------------ | ------------ | ------------ |
| 意向共享锁IS | 兼容         | 兼容         |
| 意向排他锁IX | 兼容         | 兼容         |

#### 元数据锁

- meta data lock: MDL锁，保证读写的正确性
- <font color=orange>当对一个表做写操作时，加DML读锁；当对表结构变更操作时，加DML写锁</font>
- 不需要显示使用，在访问一个表的时候会被自动加上
- 多个读锁之间不互斥，读写锁之间，写锁之间是互斥的

### 3.2 行锁

- InnoDB支持，MyISAM不支持
- 优点：锁粒度小，发生锁冲突概率低，并发度高
- 缺点：锁开销比较大，容易出现死锁

#### 记录锁

- 当一个事务获取了一条记录的s锁时，其他事务可以继续获取该记录的s锁，但不可以继续获取该记录的x记录锁
- 当一个事务获取了一条记录的x锁时，其他事务不能获取该记录的x和s锁

#### 间隙锁

- Gap Locks
- MySQL在REPETATED READ下，是可以解决幻读问题
- 间隙锁仅仅是为了防止插入幻影记录而提出来的

```sql
# 数据多行假如为1，3，6，9，10的主键

# 事务一：
可以为主键为5的(数据不存在)，加一个间隙锁
 SELECT * FROM table WHERE id=14 LOCK IN SHARE MODE;       # 会把10～正无穷的记录锁住，不允许其他事务插入
```

#### 临键锁

- Next-Key Locks：LOCK_ORDINARY，是记录锁和间隙锁的合体
- 既想锁住某条记录，又想阻止其他事务在该记录前边的间隙插入新记录

#### 插入意向锁

- Insert Intention Locks

### 3.3 页锁

- 在页的粒度上进行锁定
- 页锁的开销介于表锁和行锁之间，会出现死锁
- 每个层级的锁数量是有限制的，当某个层级的锁数量超过了阈值，就会进行锁升级

## 4. 锁分类-态度

### 4.1 乐观锁

```sql

```

### 4.2 悲观锁

```sql
SELECT ... FOR UPDATE;
```

