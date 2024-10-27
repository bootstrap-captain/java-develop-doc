# 入门简介

## 1. 简介

### 1.1 NoSQL

- 非关系型数据库： Not Only SQL, 运行在内存中
- 关系型数据库：运行在磁盘中，读写速度慢

### 1.2 Redis

- 2009年，Remote Dictionary Server
- 开源，C编写, K-V存储
- 核心命令的执行：单线程保证安全
- 官网数据： 写8.1万/s，读11万/s

## 2. 基本特性

### 2.1 多数据库

- 一个redis实例默认包含16个数据库(0 -15)，默认使用0号数据库
- 该数据在redis.conf 中配置，可以自定义修改

```bash
# 切换数据库
SELECT 1; SELECT 15; 
# 查看当前数据库 k-v 对数
DBSIZE
# 剪切数据: 将0号数据库的一个数据剪切到1号数据库
SELECT 0 ;  MOVE key 1;
```

### 2.2 基本指令

```bash
# 验证某个key是否存在，存在-1 不存在-0
EXISTS keyname
# 给某个key设置过期时间,到期后删除该key
EXPIRE keyname seconds
# 查看当前key剩余过期时间秒数, 过期后为-2,  永不过期返回 -1
TTL keyname   
# 查看所有的key，可以自定义后面的pattern
# 千万不要在生产环境主节点用，阻塞其他指令
KEYS *
# 查看key对应的value类型
type keyname
# 删除 当前数据库 数据
flushdb
# 删除 所有数据库 数据
flushall
```

### 2.3 事务

- 伪事务
- 一组事务执行完毕后，本次事务结束。如果需要再用，需重新开启

#### 提交

```bash
# 定义两个数据
SET age 10; SET name shuzhan

# 开启事务,返回值 OK
MULTI

# 操作数据: 执行后并不立即执行，而是将所有的操作放在一个队列中, 按照放入的顺序执行
# 返回值 ：QUEUED
INCR age;   INCR name;

# 提交事务：
# 提交后age可以执行成功，name执行失败，因此是伪事务
# 1) (integer) 11
# 2) (error) ERR value is not an integer or out of range
EXEC
```

#### 回滚

```bash
SET age 10; SET name shuzhan
MUTLI
INCR age;   INCR name;

# 回滚事务： 返回值：OK， 回滚后上面两条都不会执行
DISCARD
```

#### 编译时异常

- 编译型错误，事务中所有操作都不会执行
- 比如控制台指令写错

```bash
SET age 10 
SET name shuzhan
multi
incra age

# ERR unknown command `incra`, with args beginning with: `name`,
incr name

# EXECABORT Transaction discarded because of previous errors.
# age 也不会增加
EXEC
```

#### 运行时异常

- 运行时异常，错误的命令不能执行，其他命令可以正常执行

```bash
SET age 1
SET name shuzhan
MULTI
INCR age
INCR name
EXEC
# 错误的指令没执行，其他的指令可以正常执行
# 1) (integer) 2
# 2) (error) ERR value is not an integer or out of range
```

### 3. 消息的订阅与发布

- 打开不同的SSH客户端，如客户端A和客户端B，同时链接目标Redis

#### 订阅者

```bash
# 持续等待： 
# 消息的订阅: 可以订阅多个频道，
subscribe channel1 channel2 channel3
# 可以订阅一定格式的频道, 
psubscribe channel*;

# 发布者一旦提供消息，就会在消费者控制台出现如下
# 1) "message"。  // 
# 2) "cctv1"。    // 频道
# 3) "caonima"    // 具体消息

# 退出客户端及订阅模式
ctrl + c
```

#### 发布者

```bash
# 向一个频道发布消息，返回值为消费这条消息的订阅者个数
publish channel content
```

# 数据类型

- key永远是string
- 数据类型指的是value
- 添加值时，如果不带过期参数，默认永不过期

## 1. String

- 包含普通字符串，整数，浮点类型
- 底层都以字节数组存储，但编码方式不同，字符串最大内存不能超过512M

```bash
# 1. 添加/获取/删除   key
SET key value   
GET key
DEL key

# key存在，在value后追加value     key不存在，新建一个key=value
APPEND key value
# 获取字符串长度， 若key不存在，返回0
STRLEN key

# 2. 字符串截取， 0 ～ -1 代表截取全部
GETRANGE key start end
# 字符串替换, 从第几位开始，将后面的数据按位替换为value, 
# 例子： set name abcdefg,   setrange name 1 xx;    axxdefg
SETRANGE key offset value

# 3. 进阶操作
#    4.1 两次set同一个key， 都返回OK, 覆盖操作，默认永不过期
#    4.2 setex:  (set + expire)带过期时间的操作
#    4.3 setnx:  (set if not exist) 返回值1或0
SETEX key seconds value   # seconds为0的时候，表示永不过期
SETNX key value

# 4. 批量操作, 效率更高,  具有事务属性，一个k-v失败，当前操作回滚
#    4.1 mset [k-v]: 批量无条件添加
#    4.2 msetnx [k-v]： 批量不存在再添加， 返回值为1或者0                       
MSET k1 v1 k2 v2 k2 k3 v3
MSETNX k1 v1 k2 v2 k3 v3
MGET k1 k2 k3

# 5.  getset： 获取原来的值并返回，同时修改这个值
#     6.1 原k不存在时，返回nil
#     6.2 原k存在时，返回原来的值
GETSET key value
```

- redis操作是原子性，采用单线程处理所有业务，命令是依次执行的，无需考虑并发影响
- 用自增/自减作为唯一id的生成

```bash
# 1. integer值运算
#   1.1 必须用在整数上，否则 ERR value is not an integer or out of range
#   1.2 返回操作后的key的值
#   1.3 如果不存在key，则新建并初始化为0
#   1.4 自增的值可以为负数
#   1.5 自增存在最大值： java中long的最大值
# 自增自减
INCR key
DECR key
# key增加value
INCRBY key value
DECRBY key value

# 2. 小数形式
INCRBYFLOAT key value
```

```bash
# key允许有多个单词形成层级结构，可以用 ：隔开
项目名:业务名:类型:ID

# value： 比如对象，可以将对象转换为json数据串
```

## 2. List

- 双向链表，元素有序重复
- 在左边和右边插入效率高，中间插入相对慢，查询速度一般

```bash
# 1. 增加: 返回当前key所包含的元素的个数，   RPUSH    LPUSH
LPUSH key element
LPUSH key element1 element2

# 2. 移除并返回：返回移除的元素,      LPOP   RPOP  BLPOP  BRPOP
LPOP key count
BLPOP key timout_second  # 和LPOP类似，在没有元素时指定等待时间，而不是直接返回nil，阻塞队列

# 3. 获取 
LRANGE key 0 -1        # 获取所有元素
LINDEX key index       # 获取指定索引元素
LLEN key               # 获取长度     
LREM key count value   # 移除指定个数指定值,返回移除的个数，精确匹配
LTRIM key start end    # 将原list截取指定区间

# 4. 复杂操作
RPOPLPUSH source dest    # source右弹一个，左压到dest的list; only supported
LSET key index value     # 给指定下标赋值
LINSERT key BEFORE/AFTER pivot value  # 在pivot元素前或后插入一个值
```

## 3. Set

- 无序，不重复，查找快
- 支持交集，并集，差集等

```bash
# 1. 增加         
SADD key element1 element2 element3

# 2. 移除
SREM key element1 element2 element3

# 3.查看
SMEMBERS key              # 查看所有元素
SISMEMBER key element     # 判断set中是否包含某个元素
SCARD key                 # 获取某个集合的长度

SRANDMEMBER key           # 随机获取集合中的一个元素, 可以带个数
SRANDMEMBER key count

SPOP key                  # 随机弹出一个元素，返回弹出来的元素
SPOP key count

SMOVE src dest value     # 将src中value元素剪切到dest中

SINTER key1 key2 key3    # 交集
SDIFF key1 key2 key3     # 差集
SUNION key1 key2 key3    # 并集  
```

## 4. Hash

- value为Map

```bash
# 1. 添加获取 以H开头
HSET key field1 value1 field2 value2 field3 value3    # 返回插入的map的k-v数
HGET key field
HGETALL key       # 获取到所有的对应的k-v

# 2.获取
HLEN key           # key的 k-v对数
HEXISTS key field  # key的指定field是否存在  
HKEYS key          # 获取大key的所有小key
HVALS key          # 获取大key的所有value

# 3. 删除
HDEL key field1, field2, field3

# 4. 增加: map的某个属性增加多少 ，返回增加后的数据
#    减少： 设置numbers为负数即可
HINCRBY key field numbers

# 5 . add if not exist: 返回添加的field的个数
HSETNX key field value
```

## 5. SortedSet

- 有序，不重复，查询速度快
- 增加了权重标识
- 应用：排行榜数据

```bash
# 1. 查询
ZADD key score1 member1 score2 member2    # 返回插入的element个数, member 重复就覆盖
ZRANGE key 0 -1                           # 查询所有的元素
ZCOUNT key min max                        # score在这个区间的所有member
ZSCORE key member                         # 获取指定元素的score
ZCARD key                                 # member的个数
ZRANK key member                          # 获取指定member的排名
ZRANDMEMBER key count                     # 随机获取
ZRANDMEMBER key count WITHSCORES

# 2. 删除
ZREM key member1, member2                 # 移除对应的member

# 3.排序
ZRANGEBYSCORE key -inf +inf               # 按照score排序，最小到最大
ZRANGEBYSCORE key -inf +inf WITHSCORES
ZREVRANGE key 0 -1 WITHSCORES             # 按照分数从大到小
```

## 6. Geospatial

- 存放经纬度，地理位置信息
- 应用：附近的人，打车等

```bash
# 1. 添加. (纬度，经度，名称)
GEOADD key longitude1 latitud1 member1 longitude2 latitud2 member2

# 2. 查询经纬度
GEOPOS key member1 member2

# 3. 显示两个地方的直线距离
GEODIST key member1 member2 km

# 4. 某个坐标半径内的地方
GEORADIUS key longitude1 latitud1 number km 

# 5. 某个元素方圆多少的其他元素
GEORADIUSBYMEMBER key member number km
```

## 7. HyperLogLog

- 集合中，不重复元素总个数
- 应用：基数统计，网站访问量(同一个用户访问多次，只能算一个)
- 总数统计存在一定的误差性，适用于对精确度要求不高的场景

```bash
# 1. 传统实现： 可以将用户的id保存在set里面起到去重效果，
#             如果保存数据太多，内存扛不住，
#             而且目的不是保存元素，只是计数

# 2. Redis:   保存 2^64个不同的数据，只需要固定的12kb内存
#             具有0.81%的误差
PFADD key element1 element2 element3  # 返回1为成功
PFCOUNT key                           # 统计不重复元素的总个数

PFCOUNT key1 key2                     # 两个集合合并起来， 统计不重复元素个数 
PFMERGE dest src1 src2 src3           # 将src的合并到dest， src的不变
```

## 8. Bitmap

- 用0和1表述数据，如果一个数据只有两个状态，则可以用这种数据类型
- 利用bit的数据格式，通过二进制的方法来保存数据

```bash
# 1. 为key在不同的day中设置 0 或者1 ，返回值为0
SETBIT key 2 0     # 在数据的第二位上给0，
SETBIT key 12 1    # 在数据的第12位上给1，前面的全部用0来补

# 2. 获取具体哪天的value
GETBIT key 2

# 3. 统计状态为1的数据
BITCOUNT key [start end] 
```

# Java客户端

- [Redis官方推荐客户端](https://redis.io/docs/clients/#java)

## 1. Jedis

- 以Redis指令命名作为方法名称，学习成本低，简单实用
- Jedis实例是线程不安全的，多线程环境下需要基于连接池使用
- [Jedis github](https://github.com/redis/jedis)

## 2. Lettuce

- 基于Netty实现，支持同步，异步和响应式编程，并且是线程安全的
- 支持Redis的哨兵模式，集群模式和管道模式
- [Luttuce github](https://github.com/lettuce-io/lettuce-core)

## 3. Redisson

- 基于Redis实现的分布式，可伸缩的Java数据结构集合
- 包含了如Map, Queue, Lock, Semaphore, AtomicLong等强大功能
- [Redisson github](https://github.com/redisson/redisson)

## 4. SpringBoot集成

- RedisTemplate, 底层使用的是lettuce

![image-20230329114721111](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329114721111.png)

# 缓存问题

## 1. 缓存穿透

- 客户端请求的数据，在缓存和数据库中都不存在，这样缓存永远不会生效，所有请求都会打到数据库
- 恶意请求：不存在的数据，大量请求到达数据库，最终导致数据库崩溃，进而引发服务血崩

```bash
# 解决方案一：  缓存空对象
- 如果缓存和数据库中都查不到，则在缓存中，缓存空对象null，并设置TTL
- 优点：实现简单，维护方便
- 缺点：额外的缓存内存消耗

# 解决方案二：布隆过滤器
- 内存占用少，没有多余的key
- 实现复杂，存在误判可能

# 其他方案
- 做好数据的基础格式校验，不合规的查询直接在代码层面拦截，不会进行查询
- 同时增强redis的id复杂度，避免被恶意猜测并绕过代码层面拦截
```

![image-20230329122721698](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329122721698.png)

## 2. 缓存击穿

- 热点key问题： **高并发访问**，**缓存构建业务复杂**的key突然失效，无数请求访问会在瞬间打到数据库

```bash
# 解决方案一：      热点数据永不过期
- 这样就不会存在key过期的问题
- 缺点：需要使用额外的内存消耗

# 解决方案二：      互斥锁        redis分布式锁(分布式系统)或java锁(单体系统) 
- 如果key因为某种原因就是失效了
- 优点： 没有额外的内存消耗，保证一致性，实现简单
- 缺点： 线程需要等待，性能受影响； 可能存在死锁风险

# 解决方案三：      互斥锁+逻辑过期    
- 优点： 线程无需等待，性能较好
- 缺点： 不保证一致性，有额外内存消耗，实现复杂
```

![image-20230329124132278](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329124132278.png)

## 3. 缓存雪崩

- 同一时间内，大量的缓存key同时失效，大量请求都进入数据库，导致数据库崩溃
- Redis服务宕机

```bash
# 解决方案
- 数据预热：   预先加载可能访问到的数据到Redis中
- 不同TTL:    给不同的key的TTL添加随机值
- Redis集群： 异地多活，提高服务可用性，宕机后可快速切换
- 限流降级：   给缓存业务添加降级限流策略
- 多级缓存：   业务添加多级缓存
```

# 持久化

- RDBMS：数据存储在磁盘
- NORDBMS：数据存储在内存中，读取速度快，但重启redis后数据会丢失
- Redis 支持两种持久化方式，RDB 和 AOF， 配置都是在redis.conf文件

## 1. RDB-Redis Database Backup

### 1.1 原理

- 快照形式，定期将内存中的全量数据集写入文件
- 持久化的文件为二进制文件，在data/dump.rdb文件；

```bash
rdbcompression yes   # 是否压缩，建议不开启，压缩消耗cpu，磁盘空间不值钱
dbfilename dump.rdb  # 默认的rdb文件名称，可以自定义
dir ./               # 文件保存的路径为当前目录
```

### 1.2 触发方式

```bash
# save-手动触发： Redis主进程来执行
- 命令行操作完数据后，执行 save 指令, 执行完成后若存在老的rdb文件，覆盖
- 阻塞IO: 由Redis主进程来执行，会阻塞所有命令
- Redis客户端很多，因此save不可取

# bgsave-手动触发： Redis子进程来执行
- 异步IO: redis会在后台异步进行快照操作，同时还可以响应客户端请求
- Redis会fork一个子进程(非线程)来进行文件的备份操作，延时操作
- Redis内部所有RDB都是 bgsave

# 配置-自动触发： 自动触发通过redis.conf实现
# 如果满足下面三种任意一个条件，则内部触发bgsave
# 1. 3600 秒内如果至少有 1 个 key 的值变化；
# 2. 300 秒内如果至少有 100 个 key 的值变化；
# 3. 60 秒内如果至少有 10000 个 key 的值变化；
save 3600 1
save 300 100
save 60 10000
```

### 1.3 数据恢复

- 重启redis，自动加载dump.rdb中数据，并将其恢复到内存中
- 数据容灾，会将redis中的dump.rdb文件在另外的服务器单独放一份

### 1.4 原理

- bgsave开始时，主进程fork得到子进程，子进程共享主内存的数据

```bash
# fork用的是 copy-on-write技术
- 主进程执行读操作时，访问共享内存
- 主进程执行写操作时，则会拷贝一份数据，执行写操作
```

![image-20230329154644799](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329154644799.png)

```bash
# 优势
- RDB文件紧凑，全量备份，适合用于进行备份和灾难恢复
- RDB 在恢复大数据集时的速度比 AOF 的恢复速度要快

# 劣势
- 定期持久化，如果redis意外宕机，则上次持久化后所做的数据会丢失
- 快照持久化需要时间，这段时间内修改的数据可能丢失
- bgsave会将内存中的数据扩充一倍来进行持久化，导致内存性能的2倍扩大
```

## 2. AOF-Append Only File

### 2.1 机理

```bash
# 默认关闭AOF
- 将redis收到的写命令(非读)，通过write函数追加到AOF文件中
- 文件是data/appendonly.aof,  只能追加，不能修改
- redis启动后会读取该文件，按照所有指令重新在内存中构建数据
```

### 2.2 配置

```bash
# 开启
appendonly yes                                 # 开启aof, 默认不开启,开启后立刻就会先生成持久化文件
appendfilename  "myredis_appendonly.aof"       # 默认为appendonly.aof，可以进行修改
appendfsync everysec                           # 同步策略： always， everysec， no，  默认为everysec

# 同步策略
always                    # 每次数据变更会被立即记录到文件，性能差，但数据较完整
everysec                  # 异步操作, redis突然宕机，那这一秒前写入内存的数据会丢失
no                        # 写命令执行完后，先放入AOF缓存区， 由操作系统控制何时写磁盘，不可控
```

### 2.3 文件重写

- 多次数据变更，aof文件增大。 一个数据的多次修改，操作次数可只算做一次，文件重写来减少文件大小
- 异步执行： 读取内存中数据，保存在新临时文件中，替换原来aof文件

```bash
# 1. 触发机制一： 配置文件
#     1.1 redis会记录上次重写aof的的文件大小，达到阈值后，进行重写
#     1.2 默认是当前aof大小是上次aof的一倍时且大于64mb时候，进行重写

# 阈值设定
auto-aof-rewrite-percentage 100       # AOF文件比上次文件增长多少百分比才触发重写
auto-aof-rewrite-min-size 64mb        # AOF文件体积最小多大以上才触发重写

# 2.  触发机制二： 手动触发
bgrewriteaof
```

## 3. RDB vs AOF

- 生产中二者结合使用

|              | RDB                                      | AOF                                              |
| ------------ | ---------------------------------------- | ------------------------------------------------ |
| 持久化方式   | 定时对整个内存做快照                     | 记录每一条执行的命令                             |
| 数据完整性   | 不完整，两次备份之间会丢失               | 相对完整，取决于刷盘策略                         |
| 文件大小     | 会有压缩，文件体积小                     | 记录命令，文件体积大                             |
| 宕机恢复速率 | 很快                                     | 慢                                               |
| 恢复优先级   | 低，因为数据完整性不如AOF                | 高，因为数据更加完整                             |
| 系统资源占用 | 高，大量消耗cpu和内存消耗                | 低，主要是磁盘IO, 但AOF重写时会占用大量CPU和内存 |
| 使用场景     | 可以容忍数分钟内的数据丢失，追求启动速度 | 对数据安全性要求较高                             |

# 双写一致

- Redis中数据和数据库中查询的数据不一致

## 1. 低一致性：淘汰机制 

- 使用Redis内存淘汰机制或超时剔除
- 查询redis，如果redis中有则使用。 如果redis中查询不到，则查询数据库，并更新redis

|          | DESC                                                         | 一致性 | 维护和成本 |
| -------- | ------------------------------------------------------------ | ------ | ---------- |
| 内存淘汰 | Redis内存淘汰机制，内存不足时淘汰部分数据，下次查询不到时更新缓存 | 差     | 无         |
| 超时剔除 | 缓存数据TTL， 到期后自动删除，下次查不到时更新缓存           | 一般   | 低         |

## 2. 高一致性：主动更新

- 修改数据库时，更新缓存

| 更新方式 | 描述                                   | 缺点             |
| -------- | -------------------------------------- | ---------------- |
| 更新缓存 | 每次更新数据库时都需要更新缓存         | 无效写操作比较多 |
| 删除缓存 | 更新数据库时删除缓存，查询时再更新缓存 | 较好，建议采用   |

```bash
# 原子性：保证缓存和数据库的操作同时成功或失败
- 单体系统：   缓存和数据库操作放在同一个事务中
- 分布式系统： 利用TCC分布式事务方案
```

## 3. 删除缓存顺序

- 两个数据请求，一个是查请求，一个是写请求
- 更新顺序： 针对的是写请求

### 3.1 删除缓存->更新数据库

- 会因为网络问题导致不一致性

![image-20230329120800814](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329120800814.png)

### 3.2 更新数据库->删除缓存

```bash
# 不一致的情况必须同时满足两个苛刻的条件, 但是可能性较低
- 异常条件一： 首先缓存因为某种原因缓存失效了
- 异常条件二： 线程一写缓存的时间 > 线程二更新数据库+删除缓存的时间

# 兜底方案：
- 即使一旦发生了，再通过ttl来进行保底
```

![image-20230329121442846](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329121442846.png)

# 分布式锁

## 1. 理念

### 1.1 Java锁

- 集群模式下，synchronized只能保证单个JVM内部的线程互斥，不能保证跨JVM的互斥

![image-20230329155717221](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329155717221.png)

### 1.2 分布式锁

- 提高锁的作用域

```bash
# 分布式锁特点
1. 多进程可见：     必须多个jvm都可访问到该锁资源
2. 互斥：          锁资源必须是互斥
3. 高可用：        锁的稳定性
4. 高性能：        加锁本来就会降低系统性能，如何保证
5. 安全性：        锁假如无法释放怎么办
```

![image-20230329160134829](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329160134829.png)

## 2. Redis分布式锁

### 2.1 基础版本

- Redis的工作线程是单线程访问，保证一定只有一个线程来获取锁

```bash
# 场景一： 假如锁释放失败怎么办？
1. 获取:     SETNX k v
2. 执行业务
3. 释放锁    DEL k

# 场景二：
1. 获取锁，并添加过期时间    SET K V EX 10 NX
2. 执行业务
3. 释放锁
```

```java
/***
 * 基础版本的Redis分布式锁
 */
public class RedisBasicLock {
    private static final String LOCK_NAME = "nike:order:2022-09-01";
    private static final String LOCK_VALUE = "1";
    private static long keyTTLSeconds = 5;

    /**
     * 方式一：假如锁释放失败，则该锁永远无法释放
     */
    public static void firstLock() {
        Jedis jedis = getJedis();
        long result = jedis.setnx(LOCK_NAME, LOCK_VALUE);
        if (1 == result) {
            business();
        } else {
            System.out.println("get lock failure");
        }
        jedis.del(LOCK_NAME);
    }

    /**
     * 方式二： 超时机制保证锁一定能够释放
     */
    public static void secondLock() {
        Jedis jedis = getJedis();
        String result = jedis.set(LOCK_NAME, LOCK_VALUE, SetParams.setParams().nx().ex(keyTTLSeconds));
        if ("OK".equalsIgnoreCase(result)) {
            business();
        } else {
            System.out.println("get lock failure");
        }
        jedis.del(LOCK_NAME);
    }

    public static void business() {
        System.out.println(Thread.currentThread().getName() + " executing business");
    }

    public static void main(String[] args) {
        new Thread(() -> RedisBasicLock.firstLock()).start();
        new Thread(() -> RedisBasicLock.firstLock()).start();
    }
}
```

### 2.2 增强版本

- 上面分布式锁存在的问题： 误删，删已失效

![image-20230329160706259](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329160706259.png)

```bash
# 解决方法一：设置超时时间远大于业务执行时间，但是会带来性能问题

# 解决方法二：删除锁的时候要判断，是不是自己的，如果是再删除    UUID

1. 其中key可以用业务名称来表示
2. value 用uuid加线程标识
   2.1 删除锁时，先通过value来判断锁是不是自己线程的
   2.2 如果是，则删除，如果不是，就不要删除
```

```java
package com.erick.lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.UUID;

import static com.erick.redis.JedisConnectionFactory.getJedis;

public class RedisSecondLock {
    private static final String LOCK_NAME = "nike:order:2022-09-01";

    private static long keyTTLSeconds = 5;

    private static String getLockValue() {
        return Thread.currentThread().getId() + UUID.randomUUID().toString();
    }

    private static void executeBusiness() {
        System.out.println("Business execution.....");
    }

    private static void firstMethod(String lockKey, String lockValue) {
        Jedis redis = getJedis();
        String result = redis.set(lockKey, lockValue, SetParams.setParams().nx().ex(keyTTLSeconds));

        if ("OK".equalsIgnoreCase(result)) {
            executeBusiness();
            String presentValue = redis.get(lockKey);
            /*判断是否是自己的，是自己的再删除*/
            if (lockValue.equalsIgnoreCase(presentValue)) {
                redis.del(lockKey);
                System.out.println("lock deleted");
            }
        } else {
            System.out.println("Can not get lock");
        }
    }

    public static void main(String[] args) {
        new Thread(() -> firstMethod(LOCK_NAME, getLockValue())).start();
    }
}
```

### 2.3 LUA脚本

- 上面方案存在的问题：依然可能存在原子性问题
- 判断锁是否能释放，和锁真正释放的代码中间，假如存在full gc，那么就会依然出现问题
- 判断锁是否该释放和释放锁，应该做成一个原子性动作， 但是redis的事务机制不是强一致性

![image-20230329161221837](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329161221837.png)

- Redis提供了Lua脚本功能，在一个脚本中编写多条Redis命令，确保多条命令的原子性

```bash
# 1. Redis内部函数
redis.call('命令名称','key','其他参数', ......)

# 2. 执行脚本 EVAL "redis.call()"
# 无参数， 0代表脚本的key的参数个数
EVAL "return redis.call('set','name','erick')" 0

# 3. 带参数
EVAL "return redis.call('set',KEYS[1],ARGV[1])" 1 age 20
KEYS[1]:  redis的key值个数
ARGV[1]:  redis的value的值个数
1:        具体包含几个key
age：     实际传递的key值
20:       实际传递的value值
```

- 获取流程

```lua
-- 获取锁中的线程标识，动态传递参数
local lockValue = redis.call('get',KEYS[1])

-- 比较线程标示与锁中的是否一致
if (ARGV[1] == lockValue) then
   -- 释放锁
    redis.call('del',KEYS[1])
    return 1
else
    return 0
end
```

```java
package com.erick.lock;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;

import static com.erick.redis.JedisConnectionFactory.getJedis;

public class RedisLuaLock {
    private static final String LOCK_NAME = "nike:order:2022-09-01";

    private static long keyTTLSeconds = 5;

    private static String getLockValue() {
        return Thread.currentThread().getId() + UUID.randomUUID().toString();
    }

    private static void executeBusiness() {
        System.out.println("Business execution.....");
    }

    private static void firstMethod(String lockKey, String lockValue) {
        Jedis redis = getJedis();
        String result = redis.set(lockKey, lockValue, SetParams.setParams().nx().ex(keyTTLSeconds));

        if ("OK".equalsIgnoreCase(result)) {
            System.out.println("获取锁成功");
            executeBusiness();
            if (deleteLockWithLua(lockKey, lockValue)) {
                System.out.println("释放锁成功");
            }
        }
    }

    private static boolean deleteLockWithLua(String lockKey, String lockValue) {
        String luaScript = """
                local keyName = redis.call('get',KEYS[1])
                if (ARGV[1] == keyName) then
                    redis.call('del',KEYS[1])
                    return 1
                else
                    return 0
                end       
                """;
        Jedis jedis = getJedis();
        /*加载脚本*/
        String script = jedis.scriptLoad(luaScript);
        /*执行脚本*/
        Object result = jedis.evalsha(script, Collections.singletonList(lockKey), Collections.singletonList(lockValue));
        return result.equals(1L);
    }

    public static void main(String[] args) {
        new Thread(() -> firstMethod(LOCK_NAME, getLockValue())).start();
    }
}
```

### 2.4 存在的问题

- 某些业务场景，需要对锁有更高的要求
- 极端情况下出现的问题

![image-20230329161517201](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329161517201.png)

## 3.Redisson

- 一个用来进行分布式锁的工具jar

```java
package com.erick.redisson;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class RedissonDemo {

    private static final String LOCK_KEY = "nike:order:2022.08.14";

    private static RedissonClient getRedissonClient() {
        Config config = new Config();
        /*单节点*/
        config.useSingleServer().setAddress("redis://60.205.229.31:6381");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

    public static void main(String[] args) {
        new Thread(() -> executeWithLock()).start();
        new Thread(() -> executeWithLock()).start();
    }

    /*基本使用*/
    public static void executeWithLock() {
        RedissonClient redissonClient = getRedissonClient();
        /*获取锁： RLock extends Lock*/
        RLock lock = redissonClient.getLock(LOCK_KEY);

        /**
         *  可重入锁：
         *    参数一： waitTime：默认为-1，失败了立刻返回
         *    参数二： leaseTime: 默认锁释放时间为30s*/
        if (lock.tryLock()) {
            System.out.println(Thread.currentThread().getName() + "获取到锁");
            try {
                business();
            } catch (Exception e) {
                System.out.println("业务执行失败");
            } finally {
                lock.unlock();
            }
        } else {
            System.out.println("获取锁失败");
        }
    }

    public void executeWithLockRetry() {
        RedissonClient redissonClient = getRedissonClient();
        RLock lock = redissonClient.getLock(LOCK_KEY);
        /**
         * 参数一： 最长等待时间，超时则不再等待， 会触发重试等待
         * 参数二： 锁自动释放时间
         * 参数三： 时间单位
         */
        boolean hasLock = false;
        try {
            hasLock = lock.tryLock(6, 20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (hasLock) {
            try {
                business();
            } catch (Exception e) {
                System.out.println("业务发生异常");
            } finally {
                lock.unlock();
            }
        }
    }

    private static void business() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("执行业务");
    }
}
```

### 3.1 可重入

- Redission的锁具有可重入性

#### 不可重入锁

![image-20230329162251309](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329162251309.png)

#### 锁重入原理

- 存储的键值对用Hash结构来保存
- 为了保证多条命令的原子性，必须采取lua脚本来做

![image-20230329162423220](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329162423220.png)

#### LUA脚本

![image-20230329163131401](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329163131401.png)

![image-20230329163157062](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329163157062.png)

### 3.2 重试机制

- 通过等待时间结合，发布以及订阅模式来实现
- 不会立即触发重试机制，而是订阅当前锁的使用者发布的消息

### 3.3 锁超时释放

- 业务执行期间，不断有定时任务去更新过期时间
- 业务执行完毕后，取消定时任务

### 3.4 主从一致->联锁

- 主从一致性问题：由于主节点宕机，但是主节点的数据还未向从节点传输，引发的锁失效问题

![image-20230329163359012](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329163359012.png)

- 解决方案：设立多个redis作为主节点
- 只有每个都获取成功的时候，才会去执行。只要有一组正常的主从，就可以保证锁不会失效

![image-20230329163706012](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329163706012.png)

```java
package com.erick.redis;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class Test04 {
    public static void main(String[] args) {
        businessWithLock();
    }

    private static void businessWithLock() {
        String lockKey = "BUSINESS";
        RedissonClient firstClient = redissonClient01();
        RedissonClient secondClient = redissonClient02();
        RedissonClient thirdClient = redissonClient03();

        RLock firstLock = firstClient.getLock(lockKey);
        RLock secondLock = secondClient.getLock(lockKey);
        RLock thirdLock = thirdClient.getLock(lockKey);

        /*获取到多把锁*/
        RLock multiLock = firstClient.getMultiLock(firstLock, secondLock, thirdLock);

        boolean hasLock = multiLock.tryLock();
        try{
            if (hasLock) {
                business();
            } else {
                System.out.println("未获取到锁，业务没有执行");
            }
        }finally {
            multiLock.unlock();
        }
    }

    private static void business() {
        System.out.println("执行业务");
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*Redis的配置类*/
    private static RedissonClient redissonClient01() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://60.205.229.31:6379");
        return Redisson.create(config);
    }

    private static RedissonClient redissonClient02() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://60.205.229.31:6380");
        return Redisson.create(config);
    }

    private static RedissonClient redissonClient03() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://60.205.229.31:6381");
        return Redisson.create(config);
    }
}

```

# 多级缓存

## 1. 传统缓存

- 请求经过tomcat处理，tomcat的并发能力低于redis，tomcat的性能成为系统的瓶颈
- redis缓存失效时，会对数据库产生冲击

![image-20230329164116102](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329164116102.png)

## 2. 多级缓存

- 利用请求处理每个环节，分别添加缓存，减轻tomcat压力，提升服务性能

![image-20230329164256344](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230329164256344.png)

## 3. JVM进程缓存

- 缓存是存储在内存中，数据读取速度较快，能大量减少对数据库的访问，减少数据库压力

```bash
# 分布式缓存，如redis
 - 优点： 存储容量大，可靠性好，可以在集群中共享
 - 缺点： 访问缓存有网络开销
 - 场景： 缓存数据量大，可靠性高，需要在集群中共享的数据

# 进程本地缓存， 如HashMap， GuavaCache, Caffeine
- 优点：读取本地内存，没有网络开销，速度更快
- 缺点：存储容量有限，可靠性低(如重启后丢失)，无法在集群中共享
- 场景：性能要求高，缓存数据量少
```

### 3.1 Java本身

- jvm内部的数组，集合等都可以作为缓存

### 3.2 caffineCache

- Caffeine是一个基于java8开发的，提供了近乎最佳命中率的高性能的本地缓存库
- 目前spring内部的缓存用的就是这个

```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
    <version>3.1.1</version>
</dependency>
```

```bash
# 缓存清除策略
- 基于容量： 设置缓存的数量上限

- 基于时间： 设置缓存的有效时间

- 基于引用： 设置缓存为软引用或弱引用， 利用GC来回收数据。 性能较差，不建议使用

# 清除方式
- 默认情况下，当一个缓存元素过期时，caffeine并不会立刻将其清理和驱逐
- 在一个读操作或写操作后，或者在空闲时间完成对失效数据的驱逐
```

```java
package com.erick.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

public final class CacheUtils {
    private CacheUtils() {
    }

    private static final int TTL_SECS = 2;
    private static final int MAX_PAIRS = 10;

    private static Cache<String, String> cacheWithTTL;
    private static Cache<String, String> cacheWitMaxPairs;

    static {
        /*过期策略： 写完60s后过期*/
        cacheWithTTL = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(TTL_SECS)).build();

        /*过期策略： 超过多少对后删除*/
        cacheWitMaxPairs = Caffeine.newBuilder().maximumSize(MAX_PAIRS).build();
    }

    /**
     * 从缓存中获取，如果获取不到，则从DB查
     *
     * @param key
     * @return
     */
    public static String getKeyFromTTLCache(String key) {
        return cacheWithTTL.get(key, value -> getFromDB());
    }

    public static String getKeyFromMaxCache(String key) {
        return cacheWitMaxPairs.get(key, value -> getFromDB());
    }

    public static void putToTTLCache(String key, String value) {
        cacheWithTTL.put(key, value);
    }

    public static void putToMaxCache(String key, String value) {
        cacheWitMaxPairs.put(key, value);
    }

    private static String getFromDB() {
        System.out.println("查询数据库");
        return "mock_data";
    }

    public static void main(String[] args) {
        CacheUtils.putToTTLCache("name", "shuzhan");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(CacheUtils.getKeyFromTTLCache("name"));
    }
}
```

# BigKey

## 1. 脚本灌数据

```bash
# 灌数据： 100W数据

# 1. 在linux的任意目录下，使用来进行数据写入到文本中
for((i=1;i<=1000*10000;i++)); do echo "set key-$i value-$i" >> /tmp/redisTest.txt; done;

# 2. 数据灌完后，查看数据
more redisTest.txt

# 3 通过redis提供的管道 --pipe命令插入500w大批量数据
# 3.1 打开Linux上的cli
redis-cli -h 127.0.0.1 -p 6379 -a 123456
redis-cli -h 127.0.0.1 -p 6379 -a 123456 --pipe     # 再添加一个管道指令

# 3.2  将上个指令的结果，灌到下个指令中去
cat /tmp/redisTest.txt | redis-cli -h 127.0.0.1 -p 6379 -a 123456 --pipe
```

## 2. 指令安全

### 2.1 keys *

- 生产中在数据量大的时候，禁止使用 keys *, flushdb, flushall等
- 可以通过配置的方式禁用掉不相关的指令，也可以参考官方的[Redis|ACL](https://redis.io/docs/management/security/acl/)

```bash
#  1. keys *
- 指令的执行顺序原子性。 在keys *执行的时候，其他指令都在等待执行
- 使用keys *的时， 会导致Redis服务卡顿，所有读写Redis的其他的指令都会被延后甚至会超时报错
- 可能引起缓存雪崩甚至数据库宕机
- TODO:  服务雪崩待验证

# 使用阿里云 1-cpu 2-g内存， 100w数据
# 花费的时间，随着阿里云的网络和配置变化
- 花费时间： (157.90s)

# 2. flushdb, flush all
- 指令执行时间比较短，很快就删除了所有的数据 
```

### 2.2 Scan

```bash
# 常用指令： SCAN        SSCAN     HSCAN      ZSCAN
# SCAN CURSOR [MATCH pattern] [COUNT n]
- 是一个基于游标的迭代器，每次被调用后，都会向用户返回一个新的游标
- 用户在下次迭代的时候需要使用这个有表作为SCAN命令的游标参数，从而来延续之前的迭代过程
- SCAN 0 MATCH * COUNT 4

# 返回结果：包含两个元素的数组
- 第一个元素是用于进行下一次迭代的新游标
- 第二个元素是一个数组，包含了所有被迭代的元素
- 如果新游标返回0，表示迭代已经结束
```

## 3. BigKey

- 指的是对应的Value多大

### 3.1 定义

|         type          |  definition   |                            delete                            |
| :-------------------: | :-----------: | :----------------------------------------------------------: |
|        string         |     >10KB     |                     可以使用del直接删除                      |
| hash, list, set, zset | 元素个数>5000 | 使用scan来进行渐进式删除，最终删除该大key<br />同时要防止BigKey的过期自动删除造成的阻塞 |

```bash
# BigKey的危害
- 内存不均，集群迁移困难
- 超时删除，bigkey的阻塞从中做梗
- 网络流量阻塞，程序可能超时

# 产生场景
- 日积月累的情况：  某明星的粉丝，报表类的长期累积
- 瞬时的高峰：     某些场合比如双11的突然增大
```

### 3.2 定位

- redis-cli --bigkeys -a 123456

```bash
# result: 给出每种数据结构的Top 1 bigkey, 同时给出每种数据类型的k-v个数+平均值
# 不足：如果想查询大于10kb的所有key，只能借助memory usage
Sampled 10000001 keys in the keyspace!
Total key length in bytes is 108888901 (avg len 10.89)

Biggest string found '"key-10000000"' has 14 bytes

0 lists with 0 items (00.00% of keys, avg size 0.00)
0 hashs with 0 fields (00.00% of keys, avg size 0.00)
10000001 strings with 128888905 bytes (100.00% of keys, avg size 12.89)
0 streams with 0 entries (00.00% of keys, avg size 0.00)
0 sets with 0 members (00.00% of keys, avg size 0.00)
0 zsets with 0 members (00.00% of keys, avg size 0.00)

#  memory usage keyname
- 可以统计出该key所占用的内存大小，单位为kb 
```

### 3.3 lazy freezing

```bash
# 阻塞式
- 默认删除命令 DEL, REMOVE ,FLUSHDB, FLUSHALL 是阻塞式的
- 命令在执行的时候，redis-server不再处理其他指令

# 非阻塞式, 进行适当的修改
lazyfree-lazy-eviction no
lazyfree-lazy-expire no
lazyfree-lazy-server-del no
replica-lazy-flush no
lazyfree-lazy-user-del no
```
