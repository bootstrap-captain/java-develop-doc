- *开源的分布式事件流平台(Event Streaming Platform)*
- *高性能数据管道，流分析，数据集成，关键任务应用*
- *大数据场景一般采用kafka作为消息队列：缓存/削峰，解耦，异步通信*

------

# 基础架构

## 1. 消费模式

### 1.1 点对点-Queue

- Consumer主动拉取数据，消息收到后清除

![image-20220906163748988](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220906163748988.png)

### 1.2 发布订阅-Topic

- 多个Consumer相互独立，都可以获取到数据
- Consumer消费数据后，不会删除数据，默认有消息过期时间
- Kafak只支持Topic模式

![image-20240226152702131](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240226152702131.png)

## 2. Kafka架构

### 2.1 Kafka Cluster + Zookeeper

```bash
# 高并发
- 一个topic的消息，根据分区策略，存储在不同server上对应的不同partition

# 高性能
- 配合partition设计，consumer通过consumer group，组内每个consumer并行消费不同partition的消息

# 高可用
- 为每个partition在其他节点引入副本，提供leader-follower机制，防止单点故障
```

![image-20240322113304756](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240322113304756.png)

```bash
# 1. leader-follower机制
- producer和consumer: 只和leader节点通信
- leader获取数据后，会同步到follower节点
- leader挂掉后，follower有条件会成为新的leader

# 2. consumer group
- 消费组：同一个消费组内，不同的消费端去连接不同的partition，增大消费能力
- 同一个消费组内，不同的消费端不能连接同一个partition，否则就重复消费
- 同一个消费组内，一个消费端可以去消费不同partition

# 3. zookeeper
- 记录server节点状态
- 记录leader-follower相关信息
- kafka-2.8.0前，必须配合zk使用， 2.8.0后，去zk化
```

## 3. Java客户端

```xml
  <dependency>
      <groupId>org.apache.kafka</groupId>
      <artifactId>kafka-clients</artifactId>
      <version>3.7.0</version>
  </dependency>
```



# Producer

- APP客户端，调用Kafka的API，向Kafka Cluster端发送数据

![image-20220906165828046](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220906165828046.png)

## 1. Interceptors

- 拦截器：拦截消息，进行一些过滤，可以自定义，一般不使用

## 2. Serializer

- 序列化器：跨节点通讯，不用java自带的序列化， 采用轻量级序列化，避免序列化信息太多引发数据过大
- 对应的消费消息时候，也有反序列化工具
- 对key和value来进行序列化

![image-20240322121859157](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240322121859157.png)

![序列化](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/%E5%BA%8F%E5%88%97%E5%8C%96.png)

## 3.  Partitioner

- 分区器：决定数据应该存放在哪个分区

![image-20240322123515926](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240322123515926.png)

### 3.1 分区优点

#### 数据均衡

- 一个topic的所有数据通过分区规则，分别存在在不同的server上对应的partition
- 合理控制分区规则，可以达到数据均衡分布

#### 高并发

-  producer: 可以以分区为单位发送数据
- consumer: 可以以分区为单位消费数据

### 3.2 分区策略

- 分区数：0，1，2等， 根据broker节点向zookeeper注册时间和数目来判断
- 假如存在三个broker，那么有效分区就是0，1，2

#### DefaultPartitioner

- 默认分区策略

```bash
- 指定分区：    如果指定分区，则使用
- 按照key：    如果不指定分区，但是存在key，则用key的hash对分区数取模 （比如key的hash为5，分区数量为3，则就存在2号分区内）
- Stick Partition： 若不指定分区，不存在key，则使用
                      1.随机选择一个分区，并尽可能一直使用该分区
                      2.等到该分区的batch已满(batch.size)或已经完成(linger.ms)，则再随机选一个分区(和上一次分区不同)

# 2. 自定义分区器： 
#    2.1. 实现对应的package org.apache.kafka.clients.producer.Partitioner接口
#    2.2. 重写partition()方法
#    2.3  写入Producer的config中
- 比如根据key，不同数据库的数据，用表名做为key，存放在不同分区
- 过滤脏数据
```

## 4. 其他

```bash

# 2. RecordAccumulator: 缓存队列 
# 两个条件只要达到一个就会被拉取
- batch.size: 数据以批次的形式进行发送
              当数据积累到batch.size后，sender才会拉取数据，默认 16k
- linger.ms:  如果数据迟迟没有达到batch size， 等到linger.ms时间后，sender也会将数据拉取
              单位为ms，默认0ms，即无延迟发送，但效率较低       
            
# 3. Sender-Thread
- NetworkClient: 负责拉取数据发往kafka server
- InflightRequests: 每个broker节点最多缓存5个请求，如果发送没有进行ack，则不允许继续发送
- 发送失败，会进行重试，默认为int的最大次数

# 4. Selector
- 将数据发送到对应的broker的对应partition
- 集群收取到数据后，会进行leader-follower之间同步， 再进行应答
- 应答成功后，会将数据的request从Sender中删除，将对应的消息从Dqueue中删除
- 如果应答失败，则触发重试机制，会继续将Request进行重试， 重试默认次数为int的最大值

# 5. ack
- 0: 生产者发送过来的数据，不需要数据落盘就应答
- 1: 生产者发送过来的数据，Leader收到数据后应答
- -1(all): 生产者发送过来的数据， Leader收到，follower同步完成，再应答
```

## 2. 发送

- 异步发送： 消息发送到缓冲区后，并不关心后续的处理
- 同步发送： 数据发送到对应缓冲队列后，必须要等数据全部落盘到Kafka Cluster中并进行应答，才算发送成功

```bash
# 异步发送
public Future<RecordMetadata> send(ProducerRecord<K, V> record)

# 带回调函数的发送
public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback)

# 同步发送
producer.send(new ProducerRecord(topicName, partition, key, value)).get();

# key, value
- key和value可以为任意类型的数据
- key可以不存在，value必须存在

# memory pool
- Dequeue从memory pool中申请内存
- 数据发送成功后，Dequeue的内存再还回到memory pool中
```

![](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220909110349719.png)

# Produce-性能

- 参数可以在配置文件中调节作为全局配置， 也可以在代码端调节作为局部配置

## 1 吞吐量

- batch.size和linger.ms满足其中一个, sender-thread 就可以从RecordAccumulattor 发送到cluster

```bash
# 实时性 vs 高性能
- batch.size: 批次大小，默认为16k
- linger.ms: 等待时间， 单位为ms，默认0ms

# 数据进行压缩，有不同的压缩格式： gzip, snappy, lz4, zstd, snappy用的较多
- compression.type: 压缩 snappy

# 缓冲区大小：假如对应partition较多 扩大缓冲区
RecordAccumulator: 默认为32M
```

![image-20220906171442070](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220906171442070.png)

## 2. 可靠性

### 2.1 ACK

![image-20220909155828011](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220909155828011.png)

```bash
# 0： 一般很少使用
- producer发送数据，不需要等数据落盘应答

- producer发送完，leader还未落盘，leader挂了，数据压根没有发送过去

# 1： 普通日志，允许丢部分数据
- producer发送数据，leader收到数据落盘并应答

- producer发送完，leader收到数据落盘并应答
- leader还未完成同步，挂了，其他follower成为新的leader后，并没有之前数据

# -1， all： 金融类场景
- 生产者发送过来的数据，leader和所有的follower都收到后，才进行答复
```

### 2.2 动态ISR

- ACK为-1时，假如一个follower因为网络问题，迟迟不能应答，ACK动作一直不能完成
- 分区副本： 指的是leader和follower的集合

```bash
# ISR:  in-sync replcaition set
- Leader维护了一个 和Leader保持同步的Follower+Leader集合      (Leader:0, ISR: 0,1,2)

- 如果Follwer长时间未向leader发送通信请求或同步数据，该Follower将被踢出ISR， (Leader:0, ISR: 0,1)

# 参数
- replica.lag.time.max.ms:      默认为30s  
```

### 2.3 可靠性

- 可靠性保证： 数据一定能发送成功
- 建立在ACK=-1的基础上

#### 分区副本=1

- 就是只有一个Leader，不包含Follower了
- Leader挂了后，就不能获取数据了

![image-20240311112546371](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240311112546371.png)

#### ISR中应答的最小副本数量=1

```bash
# min.insync.replicas=1      默认为1
- 就不考虑leader向follower同步数据问题
- 1指的就是leader                         leader：0，   isr：0
- 就和ACK=1时候一样了
```

![image-20240311112624604](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240311112624604.png)

#### 可靠方案

- 必须全部条件满足
- 数据最少发送一次

```bash
- ACK=-1 
- 分区副本 >= 2 
- ISR中应答的最小副本数量 >= 2        #  默认为1
```

## 3. 重复性

### 3.1 数据重复

![image-20240311113827271](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240311113827271.png)

- 假如leader收到数据，向follower同步完成后，在应答瞬间，leader挂了, ack失败 
- 其他follower成为leader，继续接收上次的数据，就会产生重复的消息
- 概率较小，但是依然可能发生

### 3.2 幂等性-单分区单会话

- producer不论向broker发送多少次重复数据，broker端都只会持久化一次，保证不重复
- 开启幂等：enable.idempotence:true     默认true    false关闭

```bash
# Producer在发送数据的时候，会生成<PID + Partition + SeqNumber>，作为消息的主键
# Kafak Server端会缓存这个主键，来进行消息的去重， 如果数据重复，就会在内存中将数据删除，不会进行落盘

# PID: ProducerID
- 在Producer初始化时分配，作为每个Producer会话的唯一标识
- 每创建一个新的Producer, 就会分配一个新的PID

# Sequence Number
- Producer发送的每条消息（更准确地说是每一个消息批次，即ProducerBatch）都会带有此序列号
- 单调递增，从0开始
- 属于Producer的属性

# Partition
- 分区

# 只能保证： 单分区单会话，不可重复性
```

![image-20240311174529894](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240311174529894.png)

### 3.3 事务

- 开启事务，必须开启幂等性
- TODO



## 4.有序性

###  4.1 单分区

- broker端，最多会缓存producer发过来的最近5个request批次的元数据，可以保证该批数据是有序的
- 数据落盘后并ack后，会从缓存队列中删除

```bash
#  1. broker接受到request1和request2，落盘后进行ack
#  2. request3发送后，假如本次发送时间较长，broker迟迟没有接受到该request
#  3. 后续的request4和request5发送过来后， 通过序列号发现前面还有待落盘的request3， 则request4和request5暂时保存在内存中
#  4. request3完成落盘后，request4和request5再完成

#   开启幂等性
enable.idempotence=true                   # 需要用到其中的自增的sequence number来进行request元数据的排序
max.in.flight.requests.per.connection=5   # 需要设置小于等于5， 每个链接中发送的数据数量

# 未开启幂等性
# 该值为1时，才能保证幂等性
max.in.flight.requests.per.connection=1
```

![image-20220907091648873](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220907091648873.png)

### 4.2 跨分区

- 多分区，分区与分区间数据无序
- 如果要实现跨分区有序，可以发送数据时指定key，然后在consumer端进行数据重排，效率低

### 4.3 绝对有序

- 如果需要绝对有序，则建议发送消息时候用单分区

```bash
# 使用单分区
- 发送数据时候，指定分区
- 单分区的数据，kafka的幂等性保证其有序

# 使用单一生产者
- 如果多个producer一起向某个分区发送数据，则依然可能乱序
```

## 5. 调优

```bash
# bootstrap.servers
- value： 118.31.237.198:9092,120.55.75.185:9092,118.178.93.223:9092
- 服务器集群

# key.serializer
- key的序列化
- org.apache.kafka.common.serialization.StringSerializer
- 支持其他的序列化方式

# value.serializer
- value的序列化
- org.apache.kafka.common.serialization.StringSerializer
- 支持其他的序列化方式

# buffer.memory
- 缓冲区大小
- 默认值： 33554432(32M)

# linger.ms
- 多少s后发送
- 默认值： 0， 无延迟发送
- 建议：5-100ms

# batch.size
- 批次发送多少大小数据发送
- 默认：16384(16k)

# ack
- 应答机制
- 0，1，-1

# max.in.flight.requests.per.connection
- 确保有序
- 必须和幂等配合
- 默认值：5
- 值范围：0-5

# retries
- 消息发送出现错误时，重试次数
- 默认值：int的最大值

# retry.backoff.ms
- 两次重试之间的时间间隔
- 默认： 100ms ，一般不要修改

# enable.idempotence
- 开启幂等性
- 默认值： true

# compression.type
- 数据压缩方式
- 默认值： none
```

# Broker Server

## 1. 分区副本

- 防止单节点故障，提高数据可靠性

```bash
# 1. 副本数量
- 默认为1个，生产环境一般2个(一个leader，一个follower)
- 太多副本： 进一步提高数据可靠性。 但会增加磁盘存储空间，增加网络数据传输，降低效率
- 一般副本数量为Kafka集群的server数量-1

#  producer和consumer都是和leader进行通讯

# 2. 副本信息  AR = ISR + OSR
   # AR (Assigned Replicas) 
   - 分区中所有的副本， 包含leader和follower
   
   # ISR(In-sync Replcaition Set)
   - 和leader保持同步的follwer集合，包括leader + follower
   - 如果某follower长时间未向leader发送通信请求或同步数据，该follower就会被踢出ISR
   - replica.lag.time.max.ms:30000     默认30s
   
   - 如果leader挂了，则会从follower中重新选出新的follower
        
   # OSR
   - follower和副本同步时，延迟过大的副本集合
```

- 副本数量为3的架构

![image-20220910143019647](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910143019647.png)

## 2. 文件存储

### 2.1 存储方式

- Topic是逻辑概念，Partition是物理概念

![](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910110525667.png)

#### 存储位置

- 配置文件中配置data存储的位置
- 每个topic的每个partition对应一个文件目录，包含对应的消息的文件

```bash
# 1. /kafka/data/citi-0 ， 包含leader的数据和其他partition的follower的数据
- 因此会出现比如 citi-0    citi-1两个目录， 是因为其中一个是leader数据，另外一个是其他partition的follower数据

# 2. segment：一个topic+partitionNo 中的数据，会再次切分为多个segment，每个segment大小为1G
# /usr/local/kafka/kafka_2.12-3.2.1/config/server.properties
- log.segment.bytes=1073741824
```

#### 存储内容

```bash
# 每个segment的文件内容如下， 同时引入分片和索引机制
00000000000000000000.log：            # 具体的消息， 序列化后的数据
00000000000000000000.index ：         # 文件数据的索引
00000000000000000000.timeindex：      # 时间戳索引文件，用来删除数据，默认保留7天
leader-epoch-checkpoint:              # 是不是leader
partition.metadata:                   # 元数据信息

# 新的segment： 会根据绝对offset来作为文件命名规则
- 00000000000000004096.log
- 00000000000000004096.index
- 00000000000000004096.timeindex
```

#### 稀疏索引

- 通过不同的index，能够快速定位到消息
- index采用稀疏索引，大约每往.log文件中写入4k数据，才会往index里面写入一条索引

```bash
log.index.interval.bytes:4kb
```

#### 数据查找

![image-20240315210103367](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240315210103367.png)

```bash
# 1. 根据offset，定位segment文件
# 2. 找到小于等于目标offset的最大offset对应的索引项
# 3. 定位到log文件
# 4. 向下遍历，找到目标Record
```

#### 数据添加

- 产生的新数据，不断追加到该.log文件末尾
- 速度较快

### 2.2 清除策略

```bash
# 默认时间： 默认数据保存7天，到期后通过清除策略处理
# 配置以下不同方式，优先级从低到高
# /usr/local/kafka/kafka_2.12-3.2.1/config/server.properties
log.rentention.hours:
log.rentention.minutes:
log.rentention.ms:

# 负责检查周期，默认5min
log.rentention.check.interval.ms:  
```

#### DELETE策略

- log.cleanup.policy=delete
- 默认选择

```bash
# 基于时间： 
- 默认打开，通过segment中所有记录中的 最大时间戳 作为该文件时间戳
- 一个segment中包含过期的和不过期的，则选取最大的，就不会删除

log.rentention.hours:


# 基于大小： 默认关闭，表示无穷大，永不过期
#                 超过设置的所有日志总大小，删除最早的segment
#                 放置数据超过服务器最大硬盘，删除最早的segment
log.rentention.bytes: 
```

#### COMPACT策略

- log.cleanup.policy = compact
- 默认关闭

```bash
# 针对相同key的不同value，只保留最后一个版本
- 适合场景： 消息的key是用户ID，value是用户资料

- 压缩后的offset可能是不连续的，比如没有6，当从这个offset开始消费时，将会拿到比这个offset大的offset对应的消息，即为7
```

![image-20220910151147907](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910151147907.png)

## 3. 高效读写

### 3.1 分布式集群

- 分布式集群，分区技术，producer和conusmer并行度高

### 3.2 稀疏索引

- 读数据采用稀疏索引，可以快速定位到要消费的数据

### 3.3 顺序写磁盘

- producer产生的数据，在写入到log文件中时，追加到文件末端，为顺序写
- 官网数据： 同样的磁盘，顺序写能达到600M/s,  随机写只有 100k/s

### 3.4 页缓存和零拷贝

 **非零拷贝**

![image-20220910152331731](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910152331731.png)

**零拷贝**

- kafka采用零拷贝和页缓存，重度依赖操作系统提供的PageCache功能
- 不需将数据加载到kafka broker VM中，kafka broker不需data process，而是交给producer和consumer

```bash
# 页缓存
- 操作系统提供的功能
- 写操作时，只是将数据写入到PageCache
- 读操作时，先从PageCache中找，再去磁盘中找
- 尽可能多的把多的空闲内存当作了磁盘缓存来用
- NIC: 网卡

# 零拷贝
- kafka server端不对数据做任何操作
- 具体的操作都交给对应的producer和consumer
```

![image-20220910152933842](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910152933842.png)

# Consumer

```bash
# pull模式: kafka采取的模式
- consumer主动从broker中拉取数据    
- consumer根据自身的消费消息的速率，进行适当的拉取
- 若kafka没有数据，可能陷入死循环

# push模式
- broker向consumer推送消息
- broker决定推送消息的速率，难以满足不同consumer
```

## 1. 消费原理

![image-20220910153752707](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910153752707.png)

## 2. consumer组

```bash
# consumer组：
- 一个consumer可以消费多个partition中数据
- 消费者组中，不同consumer，可分别消费不同partition中数据
- 消费者组中，多个consumer，不能同时消费同一个partition(引发重复)
- 消费者组中，consumer超过了partition数，则一部分consumer处于空闲状态

# tips
- 引入主要目的是为了快速消费不同partition
- 一个consumer组包含多个consumer，多个consumer要采用相同的groupId

group.id="citi-erick"
```

### 2.1 消费组初始化

```bash
# coordinator: 辅助实现consumer组的初始化和分区配置
- 每个broker节点都有对应的coordinator
- 目标coordinator节点 = hashCode(groupId) % 50 (_consumer_offsets的分区数量，可以调整)
- 目标coordinator：进行consumer组的初始化和分区配置
- 消费者组选出目标coordinator后，与其通信进行消息处理
```

![image-20220910112933763](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910112933763.png)

### 2.2 消费过程

![image-20220907140144643](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220907140144643.png)

```bash
# 数据大小和时间，满足一个即可

# 每批次拉取的最小数据
fetch.min.byte=1k

# 未达到大小时，超时就会依然拉取
fetch.max.wait.ms=500ms

# 每批次拉取的最大数据量
fetch.max.bytes=50m

# parseRecord:   数据的反序列化
# Interceptors:  拦截器(可以用来做数据的统计)
```

### 2.3 再平衡

- 三个参数都是在consumer端进行配置

```bash
# 心跳机制
- 每个consumer都会向coordinator保持心跳
- 默认3s
- heartbeat.interval.ms=3000         

# 再平衡
- 再平衡会影响Kafka的性能

    # 超时心跳
    - 超时未发送心跳，该consumer就会被移除，并触发再平衡
    - 默认45s
    - session.timuout.ms=45000
          # 45s内，某个consumer挂掉，则该consumer的任务交给某个consumer去消费
          # 45s后，consumer依然没有恢复，该consumer就会被移除该消费者组，并且重新制定消费计划
    
    # consumer处理消息时间长
    - consumer处理消息时间过长, 也会触发再平衡 (会导致重复消费)  
    - 默认5min
    - max.poll.interval.ms = 5 min
         # 会将该consumer踢出consumer group，并把任务重新交给其他的consumer去处理，并重新制定消费计划
         # 有心跳，但是被强制踢出
```

## 3. 分区分配策略

- consumer-leader在指定消费策略时，消费者组中多个consumer，一个topic多个partition，到底哪个consumer来消费哪个partition
- partition.assignment.strategy：可以在代代码端配置

### 3.1 Range

- 针对单独一个topic而言
- 默认分区分配策略

```bash
# 分区规则: 
- 对一个topic，按照分区序号进行排序，并对consumer按照字母排序
- 分区： 0，1，2，3，4，5，6          consumer： c0, c1, c2
- partition/consumer 来决定每个consumer应该消费几个分区，如果除不尽，则前面consumer多消费1个分区

# 缺点： 数据倾斜
- 如果只是针对一个topic，则c0consumer多消费一个分区影响不大
- 如果是多个topic都采取这种策略，则c0压力过大，引发数据倾斜
```

![image-20220910113823683](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910113823683.png)

### 3.2 RoundRobin

```bash
# 分区规则： 针对集群中所有topic
- 把所有的topic的partition(topic + partition)，按照hashcode进行排序， 再对consumer进行排序
- 轮询分区策略： 通过轮询算法分配给每个consumer
```

![image-20220910163624443](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910163624443.png)

### 3.3 Sticky

```bash
# 黏性分区
- 首先会尽量均衡的放置分区到consumer上面
- 不同于range，会采用随机的方式，将对应的topic的partition分配给不同的consumer
```

##  4. offset

- consumer消费一个topic的partition时，假如consumer突然挂掉，下次consumer重启时需要知道从哪里开始继续消费
- offset维护在kafka的系统主题中： _consumer_offsets

![image-20220907140510556](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220907140510556.png)

###  4.1 存储位置

```bash
# 存放位置
- offset信息存放在kafka broker的 名为_consumer_offsets的topic中
- offset信息是通过k-v的方式存储
- key： groupId + topic + partition                value: 当前offset值
- 每隔一段时间，kafka内部会对该topic进行压缩， 即groupId + topic + partition  保留最新数据

# _consumer_offsets:  该topic默认不能被消费
- exclude.internal.topics=false,  默认为true
- 可修改 config/consumer.properties中添加上面属性
- 也可consumer端代码中配置参数
```

###  4.2 自动offset

- kafka自动存在offset功能
- 每次提交完毕后，才会继续消费下一批数据

```bash
enable.auto.commit:true               # 是否开启自动提交offset功能， 默认为true
auto.commit.interval.ms:5000          # 自动提交offseet的时间间隔，默认是5s

# 1. 每次消费后，consumer不用提交，继续消费后面的数据
# 2. 5s后，kafka自动将该consumer上一个的offset进行提交
```

![image-20240316164053305](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240316164053305.png)

###  4.3 手动offset

- 自动提交offset十分简单，但是是基于时间提交的，kafka提供更精准的手动提交的功能

```bash
# 同步提交---commitSync
- 将本次消费的一批数据的offset提交
- 阻塞当前线程，一直到提交成功，才会进行下一批次的消费
- 并且会自动失败重试(不可控因素导致，提交失败)

# 异步提交---commitAsync, 一般用的比较多
- 将本次提交的一批数据的offset提交
- 可能出现提交失败问题

# 具体操作代码
- ConsumerConfig中配置关闭自动提交参数
- 消费完后进行commitAsync或commitSync
```

### 4.4 指定offset消费

```bash
# 包含earliest, latest, none

# earliest：   
- 当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费

# latest：     
- 当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据

# none：      
- 如果没找到消费者组的先前偏移量， 则向consumer抛出异常

auto.offset.reset=latest
# 也可以指定offset或者指定时间进行消费
```

##  5. 可靠性

### 5.1 重复消费

- 自动提交offset引发

```bash
- 如果提交了一次offset，2s后consumer挂了
- 再次重启consumer，则从上次提交的offset继续消费，导致重复消费
```

![image-20240316173345390](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240316173345390.png)

### 5.2 漏消费

- 手动提交offset引发

```bash
# 异步手动提交
- 当offset被提交时，数据还在consumer端正在处理(比如落库)，consumer突然挂了
- 再次重启consumer，则之前的数据丢失
```

### 5.3 消费者事务

- 如果要确保精确收到一次
- 需要kafka消费端，将消费过程和提交offset过程做原子绑定，比如和mysql对应

### 5.4 数据积压

- topic中数据默认是保存7天
- 如果consumer能力不足，可能导致数据丢失

```bash
- 提高consumer个数和consumer集群
- 提高consumer每批次拉取数据个数
- 提高每批次拉取数据的内存上限
```

## 6. 调优

```bash
# bootstrap.servers
- value： 118.31.237.198:9092,120.55.75.185:9092,118.178.93.223:9092
- 服务器集群

# key.deserializer
- key的反序列化
- org.apache.kafka.common.serialization.StringDeserializer

# value.deserializer
- value的反序列化
- org.apache.kafka.common.serialization.StringDeserializer

# group.id
- 消费者组的名称
- 任何消费者必须要有消费者组

# enable.auto.commit
- 自动offset的提交，消费者会自动周期性向服务器提交偏移量
- 默认值： true

# auto.commit.interval.ms
- enable.auto.commit如果为true，该值定义消费者偏移量向kafka的提交频率
- 默认值： 5000(5s)

# auto.offset.reset
- 包含earliest, latest, none
- 默认值： latest

# heartbeat.interval.ms
- 消费者和coordinator之间的心跳时间
- 必须小于session.timeout.ms, 不该高于session.timeout.ms的1/3
- 不建议修改
- 默认值： 3000(3s)

# session.timeout.ms
- 消费者和coordinator连接超时时间
- 超时则该消费者组被移除，并触发消费者再平衡
- 默认值: 45000(45s)

# max.poll.interval.ms
- 消费者处理消息的最大时长
- 超时则该消费者被移除，并触发消费者再平衡
- 默认值：300000(5min)

# fetch.max.bytes
- 消费者获取服务器端一批消息最大的字节数
- 默认值：52428800(50M)

# fetch.min.bytes
- 消费者获取服务器端一批消息最小的字节数
- 默认值：1(1k)

# fetch.max.wait.ms
- 消费者最多等待时间，就会去拉取数据
- 默认值： 500ms
```

