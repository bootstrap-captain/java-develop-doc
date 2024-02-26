- *开源的分布式事件流平台(Event Streaming Platform)*
- *高性能数据管道，流分析，数据集成，关键任务应用*
- *大数据场景一般采用kafka作为消息队列：缓存/削峰，解耦，异步通信*

------

# 基本介绍

## 1. 消费模式

### 1.1 点对点-Queue

- Consumer主动拉取数据，消息收到后清除

![image-20220906163748988](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220906163748988.png)

### 1.2 发布订阅-Topic

- 多个Consumer相互独立，都可以获取到数据
- Consumer消费数据后，不会删除数据，默认有消息过期时间

![image-20240226152702131](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240226152702131.png)

## 2. 基础架构

- 高并发：一个topic的消息，根据分区策略，存储在不同server上不同partition
- 高性能：配合partition设计，consumer通过consumer group，组内每个consumer并行消费不同partition的消息
- 高可用：为每个partition在其他节点引入副本，提供leader-follower机制，防止单点故障

![image-20220906164905245](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220906164905245.png)

```bash
# 1. leader-follower机制
- producer和consumer都是连接leader节点，不会和follower节点通信
- leader获取数据后，会同步到follower节点
- leader挂掉后，follower有条件会成为新的leader

# 2. consumer
- 消费组：同一个消费组内，不同的消费端去连接不同的partition，增大消费能力
- 同一个消费组内，不同的消费端不能连接同一个partition，否则造成消息重复消费
- 同一个消费组内，一个消费端可以去消费不同partition

# 3. zookeeper
- 记录server节点状态
- 记录leader-follower相关信息
- kafka-2.8.0前，必须配合zk使用， 2.8.0后，去zk化
```



# 安装-3.2.1

- 集群版本：1台zookeeper，3台kafka组成集群

## 1. 启动zookeeper

```bash
# 1. 在服务器上47.93.216.235安装zookeeper    8G内存
docker pull zookeeper:3.8
docker run --name zookeeper -p 2181:2181 --restart always -d 1530b1a1b069 
```

## 2. Kafka集群安装

- 相同步骤，部署三台kafka，但保证对应的broker.id不同即可，本文分别为111， 222， 333

### 2.1 安装

- [官网下载](https://kafka.apache.org/downloads)
- kafka的broker端是用scala写的
- 2.12/2.13代表的是scala的版本， 后面的3.2.1代表的是kafka版本

![image-20220909075507491](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220909075507491.png)

### 2.2 上传

- Kafka的运行需要有java环境，选择安装java17
- 阿里云服务器     39.106.78.92,      39.105.127.52,   39.105.26.134

```bash
# 第一台服务器
cd usr/local
mkdir kafka

# 1. 上传文件并解压
put put /Users/shuzhan/Desktop/kafka_2.12-3.2.1.tgz /usr/local/kafka
tar -zxvf kafka_2.12-3.2.1.tgz
```

```bash
# 目录解释
LICENSE 
NOTICE  
bin               # kafka执行时候的启动脚本
config            # kafka对应的参数
libs              # kafka内部运行所需要的jar包
licenses     
site-docs
```

### 2.3 修改配置文件

- /usr/local/kafka/kafka_2.12-3.2.1/config
- server.properties： 该台kafka server端对应的参数

```bash
# The id of the broker. This must be set to a unique integer for each broker.
# 该kafka在整个集群中的身份唯一表示
broker.id=111

# A comma separated list of directories under which to store log files
# 存储具体的kafka的message的地方，为自建目录
log.dirs=/usr/local/kafka/data

# 外部访问时候的ip和端口，对应的ip为本机的ip
advertised.listeners=PLAINTEXT://120.77.156.53:9092

# zookeeper的配置：zookeeper的ip和端口
zookeeper.connect=120.79.28.20:2181
```

### 2.4 环境变量

```bash
vim /etc/profile

export KAFKA_HOME=/usr/local/kafka/kafka_2.12-3.2.1
export PATH=$PATH:$KAFKA_HOME/bin

# 刷新环境变量
source /etc/profile
```

### 2.5 kafka启动

```bash
# 1. 启动
# 进入对应目录
/usr/local/kafka/kafka_2.12-3.2.1

# kafka-server-start.sh： 启动的脚本
# -daemon： 后台启动
# config/server.properties: 按照指定的配置文件启动
bin/kafka-server-start.sh -daemon config/server.properties

# 查看启动进程, kafka也是java进程
jps
```

# Producer

## 1. 原理

![image-20220906165828046](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220906165828046.png)

```bash
# 1. producer
- Interceptors-拦截器： 拦截消息，进行一些过滤，可以自定义，一般不使用
- Serializer-序列化器：  跨节点通讯，不用java自带的序列化， 采用轻量级序列化，避免序列化信息太多引发数据过大
- Partitioner-分区器：  决定数据应该存放在哪个分区

# 2. RecordAccumulator: 缓存队列 
- batch.size: 数据会以批次的形式进行发送
              当数据积累到batch.size后，sender才会拉取数据，默认 16k
- linger.ms:  如果数据迟迟没有达到batch size， 等到linger.ms的时间后，sender也会将数据拉取
              单位为ms，默认是0ms，即无延迟发送，但是效率较低       
            
# 3. Sender-Thread
- NetworkClient: 负责拉取数据发往kafka server
- InflightRequests: 每个broker节点最多缓存5个请求，如果发送没有进行ack，则不允许继续发送
- 发送失败，会进行重试，默认为int的最大次数

# 4. Selector
- Selector将数据发送到对应的broker的对应partition
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
```

![](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220909110349719.png)

## 3. 分区

### 3.1 优点

```bash
# 1. 合理使用存储资源
- 合理存储资源：一个topic的所有数据分partition存储，每个partition在一个broker上存储
             可以把海量数据按照分区切割成一块一块的数据存储在多台broker上
             
- 合理控制分区的任务，可以实现负载均衡

# 2. 提高并行度
- 生产者可以以分区为单位发送数据
- consumer可以以分区为单位消费数据
```

![image-20220909150030293](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220909150030293.png)

### 3.2 分区策略

- 分区数：0，1，2等， 根据broker节点向zookeeper注册时间和数目来判断
- 假如存在三个broker，那么有效分区就是0，1，2

```bash
# 1. 默认分区策略: package org.apache.kafka.clients.producer.internals.DefaultPartitioner
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



## 4. 性能调优

- 参数可以在配置文件中调节作为全局配置， 也可以在代码端调节作为局部配置

### 4.1 吞吐量

```bash
# batch.size和linger.ms满足其中一个, sender-thread 就可以从RecordAccumulattor 发送到cluster

# 实时性 vs 高性能
- batch.size: 批次大小，默认为16k
- linger.ms: 等待时间， 单位为ms，默认0ms

# 数据进行压缩，有不同的压缩格式： gzip, snappy, lz4, zstd, snappy用的较多
- compression.type: 压缩 snappy

# 缓冲区大小：假如对应partition较多 扩大缓冲区
RecordAccumulator: 默认为32M
```

![image-20220906171442070](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220906171442070.png)

### 4.2 可靠性

#### ACK

![image-20220909155828011](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220909155828011.png)

```bash
# 0： 一般很少使用
- producer发送数据，不需要等数据落盘应答

- producer发送完，leader还未落盘，leader挂了，数据压根没有发送过去

# 1： 普通日志，允许丢部分数据
- producer发送数据，leader收到数据后应答

- producer发送完，leader收到数据落盘并应答
- leader还未完成同步，挂了，其他follower成为新的leader后，并没有之前数据

# -1， all： 金融类场景
- 生产者发送过来的数据，leader和所有的follower都收到后，才进行答复

- producer发送完，leader收到数据落盘，且其他follower同步完成
```

#### 动态ISR

- ACK为-1时，假如一个follower因为网络问题，迟迟不能应答，ACK动作一直不能完成

```bash
# IST:  in-sync replcaition set
- leader维护了一个 和leader保持同步的Follower+Leader集合(leader:0, isr: 0,1,2)
- 如果follwer长时间未向leader发送通信请求或同步数据，该follower将被踢出ISR， (leader:0, isr: 0,1)
- replica.lag.time.max.ms:      默认为30s  
```

#### 可靠性

- 分区副本： 指的是leader和follower的集合

```bash
# 可靠性保证： 数据一定能发送成功
- ACK级别为-1  +  分区副本数大于等于2    +  ISR里应答的最小副本数量大于等于2
- 分区副本： 包含leader和follower
```

```bash
# ack=-1时， 数据重复问题，概率较低
- 假如leader收到数据，向follower同步完成后，在应答瞬间，leader挂了, ack失败 
- 其他follower成为leader，继续接受上次的数据，就会产生重复的消息
```

### 4.3 重复性

#### a. 幂等性

- producer不论向broker发送多少次重复数据，broker端都只会持久化一次，保证不重复

```bash
# 根据 <PID + Partition+SeqNumber>:  如果数据重复，就会在内存中将数据删除，不会进行落盘
# 只能保证： 单分区单会话，不可重复性
- PID: ProducerID, producer每重启一次，都会分配一个新的PID
- Partition: 分区
- Sequence Number: 单调递增，从0开始,  属于kafka对应的broker的指定topic的属性

# 开启幂等
- enable.idempotence:true     默认为true    false关闭

# 分区问题
- 一般情况下，同一个value的数据，经过分区器处理后，就只会进入某个指定分区
```

![image-20220910102409381](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910102409381.png)

#### b. 事务

- 开启事务，必须开启幂等

### 4.4 有序性

####  单分区

```bash
# 1.   1.x版本之前，保证单分区内有效
max.in.flight.requests.per.connection=1

# 2.   1.x版本之后，
#      未开启幂等性
max.in.flight.requests.per.connection=1

#      开启幂等性
enable.idempotence=true                   # 需要用到其中的自增的sequence number来进行重排
max.in.flight.requests.per.connection=5   # 需要设置小于等于5， 每个链接中发送的数据数量
```

```bash
# broker端，最多 会缓存producer发过来的最近5个request元数据，可以保证该批数据是有序的
#  1. broker接受到request1和request2，落盘后进行ack
#  2. request3发送后，假如本次发送时间较长，broker迟迟没有接受到该request
#  3. 后续的request4和request5发送过来后， 通过序列号发现前面还有待落盘的request3， 则request4和request5暂时保存在内存中
#  4. request3完成落盘后，request4和request5再完成
```

![image-20220907091648873](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220907091648873.png)

#### 跨分区

- 多分区，分区与分区间数据无序
- 如果要实现跨分区有序，可以发送数据时指定key，然后在consumer端进行数据重排，效率低

## 5. 参数调优

| Param                                 | DESC                                                        | Default Value | Other                                                        |
| ------------------------------------- | ----------------------------------------------------------- | ------------- | ------------------------------------------------------------ |
| buffer.memory                         | RecordAccumulator缓冲区大小                                 | 32M           | 如果分区数过多，可以适当增大                                 |
| batch.size                            | 缓冲区一批数据最大值                                        | 16k           | 适当增大该值，可以提高吞吐量，但如果设置过大，则会导致数据传输延迟增加 |
| linger.ms                             | 如果数据迟迟未达到batch.size, 等待linger.ms之后就会发送数据 | 0ms           | 生产环境一般建议为5-100ms之间                                |
| compression.type                      | 生产者发送的所有数据的压缩方式                              | none          | none, gzip, snappy, lz4, zstd                                |
| ack                                   | 消息发送成功验证                                            |               | 生产环境一般为all                                            |
|                                       |                                                             |               |                                                              |
| enable.idempotence                    | 开启幂等性                                                  | true          | 以下三个参数可以保证单分区消息的有序                         |
| max.in.flight.requests.per.connection | 允许最多没有返回ack的次数                                   | 5             | 开启幂等,要保证该值是1-5之间                                 |
| retries                               | 当消息发送到broker server端，出现错误的时候，系统会自动重发 | int的最大值   | 设置了重试，还想保证有序性，max.in.flight.requests.per.connection要为1 |





# Broker Server

## 1. 分区副本

- 防止单节点故障，提高数据可靠性

```bash
# 1. 副本数量
- 默认副本1个，生产环境一般2个(一个leader，一个follower)
- 太多副本： 可以进一步提高数据可靠性。   但是会增加磁盘存储空间，增加网络数据传输，降低效率

- producer和consumer都是从leader获取数据

# 2. 副本信息  AR = ISR + OSR
- AR(Assigned Replicas):    分区中所有的副本， 包含leader和follower
- ISR:  和leader保持同步的follwer集合，包括leader+follower
        如果某follower长时间未向leader发送通信请求或同步数据，该follower就会被踢出ISR

replica.lag.time.max.ms:30000     默认30s
        
 - OSR: follower和副本同步时，延迟过大的副本集合
```

- 副本数量为3的架构

![image-20220910143019647](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910143019647.png)

## 2. 文件存储

### 2.1 存储方式

- Topic是逻辑概念，Partition是物理概念

![](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910110525667.png)

```bash
# 1. 存储位置： 配置文件中配置data存储的位置
- 每个topic的每个partition对应一个文件目录，包含对应的消息的文件
- log： 实际存储数据，并不是log目录名，目录名为： topicName--partition no

# log:  包含leader的数据和其他partition的follower的数据，
# 因此会出现比如 nike-0    nike-1两个目录， 是因为其中一个是leader数据，另外一个是其他partition的follower数据

# 2. segment：一个log中的数据，会再次切分为多个segment，每个segment大小为1G
# 每个segment的对应的数字，其实就是方便索引
# 每个segment的文件内容如下， 同时引入分片和索引机制

00000000000000000000.log：            # 具体的消息， 序列化后的数据
00000000000000000000.index ：         # 文件数据的索引
00000000000000000000.timeindex：      # 时间戳索引文件，用来删除数据，默认保留7天
leader-epoch-checkpoint:              # 是不是leader
partition.metadata:                   # 元数据信息

# 3. 通过不同的index，能够快速定位到消息
- index采用稀疏索引，大约每往.log文件中写入4k数据，才会往index里面写入一条索引
log.index.interval.bytes:4kb

# 4. 数据添加方式
- 产生的新数据，不断追加到该.log文件末尾
```

### 2.2 清除策略

```bash
# 默认时间： 默认数据保存7天，到期后通过清除策略处理
# 配置以下不同方式，优先级从低到高
log.rentention.hours:
log.rentention.minutes:
log.rentention.ms:

# 负责检查周期，默认5min
log.rentention.check.interval.ms:  
```

**delete策略**

```bash
log.cleanup.policy=delete

#    1.1 基于时间： 默认打开，通过segment中所有记录中的最大时间戳作为该文件时间戳
log.rentention.hours:
#    1.2 基于大小： 默认关闭，表示无穷大，永不过期
#                 超过设置的所有日志总大小，删除最早的segment
#                 放置数据超过服务器最大硬盘，删除最早的segment
log.rentention.bytes: 
```

**compact策略**

- 针对相同key的不同value，只保留最后一个版本
- 适合场景： 消息的key是用户ID，value是用户资料
- 压缩后的offset可能是不连续的，比如没有6，当从这个offset开始消费时，将会拿到比这个offset大的offset对应的消息，即为7

```bash
log.cleanup.policy = compact
```

![image-20220910151147907](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910151147907.png)

## 3. 高效读写

```bash
# 1. 分布式集群： 
- 分布式集群，分区技术，producer和conusmer并行度高

# 2. 稀疏索引
- 读数据采用稀疏索引，可以快速定位到要消费的数据

# 3. 顺序写磁盘
- producer产生的数据，在写入到log文件中时，追加到文件末端，为顺序写
- 官网数据： 同样的磁盘，顺序写能达到600M/s,  随机写只有 100k/s

# 4. 页缓存和零拷贝
```

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
# 1 consumer组：
- 一个consumer可以消费多个partition中数据
- 一个consumer group中的的不同consumer，可以分别消费不同partition中数据
- 一个consumer group 的多个consumer，不能同时消费同一个partition(引发重复)
- 一个consumer group 的consumer超过了partition数，则一部分consumer处于空闲状态

# tips
- 引入主要目的是为了快速消费不同partition
- 一个consumer组包含多个consumer，多个consumer要采用相同的groupId

group.id="citi-erick"
```



### 2.1 消费者组初始化

![image-20220910112933763](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910112933763.png)

```bash
# coordinator: 辅助实现consumer组的初始化和分区配置
- 每个broker节点都会有对应的coordinator
- 会由其中的一个coordinator来进行consumer组的初始化和分区配置
- coordinator节点选择 = groupId的hashCode % 50 (_consumer_offsets的分区数量，可以调整)

# 心跳机制
- 每个consumer都会向coordinator保持心跳(默认3s)
# 再平衡： 会影响kafka性能
- 1.超时未发送心跳，该consumer就会被移除，并触发再平衡                       session.timuout.ms = 45 s
- 2.consumer处理消息时间过长, 也会触发再平衡 (会导致重复消费)                 max.poll.interval.ms = 5 min

# consumer节点故障：session.timuout.ms = 45 s
- 45s内，如果某个consumer挂掉，则该consumer的任务交给某个consumer去消费
- 45s后，consumer依然没有恢复，该consumer就会被移除该消费者组，并且重新制定消费计划
```

### 2.2 消费流程

![image-20220907140144643](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220907140144643.png)

```bash
# 每批次拉取的最小数据
fetch.min.byte=1k

# 未达到大小时，超时就会依然拉取
fetch.max.wait.ms=500ms

# 每批次拉取的最大数据量
fetch.max.bytes=50m
```



### 2.3 分区消费策略

- consumer-leader在指定消费策略时，一个consumer group中多个consumer，一个topic多个partition，到底哪个consumer来消费哪个partition
- partition.assignment.strategy：可以通过配置文件或者代码端

#### Range

- 针对单独一个topic而言

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

#### RoundRobin

```bash
# 分区规则： 针对集群中所有topic
- 把所有的topic的partition(topic + partition)，按照hashcode进行排序， 再对consumer进行排序
- 轮询分区策略： 通过轮询算法分配给每个consumer
```

![image-20220910163624443](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910163624443.png)

#### Sticky

```bash
# 黏性分区
- 首先会尽量均衡的放置分区到consumer上面
- 不同于range，会采用随机的方式，将对应的topic的partition分配给不同的consumer
```

##  3. offset

- consumer消费一个topic的partition时，假如consumer突然挂掉，下次consumer重启时需要知道从哪里开始继续消费
- offset维护在kafka的系统主题中： _consumer_offsets

![image-20220907140510556](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220907140510556.png)

###  3.1 存储位置

```bash
# 存放位置
- offset信息存放在kafka broker的 名为_consumer_offsets的topic中
- offset信息是通过k-v的方式存储
- key： groupId + topic + partition                value: 当前offset值
- 每隔一段时间，kafka内部会对该topic进行压缩， 即groupId + topic + partition  保留最新数据

# _consumer_offsets:  该topic默认不能被消费
- 可修改 config/consumer.properties中添加 exclude.internal.topics = false 来消费该主题， 默认为true
- 也可代码中consumer端配置参数

exclude.internal.topics = false
```

###  3.2 自动offset

- kafka自动存在offset功能
- 每次提交完毕后，才会继续消费下一批数据

```bash
enable.auto.commit:true               # 是否开启自动提交offset功能， 默认为true
auto.commit.interval.ms:5000          # 自动提交offseet的时间间隔，默认是5s
```

###  3.3 手动offset

- 自动提交offset十分简单，但是是基于时间提交的，kafka提供更精准的手动提交的功能
- offset提交值为什么是不连续的？

```bash
# 同步提交---commitSync
- 将本次提交的一批数据的offset提交
- 阻塞当前线程，一直到提交成功，才会进行下一批次的消费
- 并且会自动失败重试(不可控因素导致，提交失败)

# 异步提交---commitAsync, 一般用的比较多
- 将本次提交的一批数据的offset提交
- 可能出现提交失败问题

# 具体操作代码
- ConsumerConfig中配置关闭自动提交参数
- 消费完后进行commitAsync或commitSync
```

### 3.4 指定offset消费

```bash
# 包含earliest, latest, none

# earliest：   当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费
# latest：     当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据
# none：       如果没找到消费者组的先前偏移量， 则向consumer抛出异常

auto.offset.reset=latest
# 也可以指定offset或者指定时间进行消费
```

##  4. 可靠性

### 4.1 漏消费和重复消费

```bash
# 重复消费
- 自动提交offset引发
- 如果提交了一次offset，2s后consumer挂了
- 再次重启consumer，则从上次提交的offset继续消费，导致重复消费

# 漏消费
- 手动提交offset引发
- 当offset被提交时，数据还在consumer端正在处理，consumer突然挂了
- 再次重启consumer，则之前的数据丢失
```

### 4.2 消费者事务

- 如果要确保精确收到一次
- 需要kafka消费端，将消费过程和提交offset过程做原子绑定，比如和mysql对应

### 4.3 数据积压

- topic中数据默认是保存7天
- 如果consumer能力不足，可能导致数据丢失

```bash
- 提高consumer个数和consumer集群
- 提高consumer每批次拉取数据个数
- 提高每批次拉取数据的内存上限
```

## 5. 参数调优



| Param                   | Desc                                                         | Default Value | Other                                                        |
| ----------------------- | ------------------------------------------------------------ | ------------- | ------------------------------------------------------------ |
| heartbeat.interval.ms   | consumer和coordinator心跳时间                                | 3s            | 必须小于session.timeout.ms, 也不应该高于session.timeout.ms的1/3 |
| session.timeout.ms      | consumer和coordinator心跳超时，consumer 被移除               | 45s           |                                                              |
| max.pull.interval.ms    | 消费消息处理时间的最大值， 超时则触发再平衡                  | 5min          |                                                              |
| fetch.min.bytes         | 拉取数据时，每次达到该值就可以拉取                           | 1k            | 数据从 server端，拉取到consumer端队列中                      |
| fetch.max.wait.ms       | 拉取数据时，如果没达到fetch.min.bytes， 但是等待时间到达，则依然会拉取 | 500ms         |                                                              |
| fetch.max.bytes         | 一次拉取数据时，最大能拉取的数据                             | 50M           |                                                              |
| max.poll.records        | consumer具体每次处理的消息条数                               | 500           |                                                              |
|                         |                                                              |               |                                                              |
| enable.auto.commit      | offset的自动提交                                             | true          | 生产环境一定要设置为手动提交                                 |
| auto.commit.interval.ms | offset的自动提交的时间间隔                                   | 5s            |                                                              |
| auto.offet.reset        | consumer宕机重启后从哪里消费                                 | latest        | earliest: 自动重置偏移量为最早<br />latest: 自动重置偏移量为最新 |

# Kafka-Monitor

- Kafka-Eagle, [官网下载](https://www.kafka-eagle.org/)，对应的二进制文本

## 1. 下载

```bash
# 阿里云服务器安装kafka eagle:  39.108.99.248
- Kafka-eagle依赖需要用到jdk环境， 安装前需要先安装JDK, 并配置好环境变量
- MYSQL： kafka-eagle的数据信息需要保存在mysql中
- 阿里云服务器选用2核8G, 内存过小可能跑不起来

# 安装
cd usr/local/
mkdir kafka-eagle
put /Users/shuzhan/Desktop/kafka-eagle-bin-3.0.1.tar.gz /usr/local/kafka-eagle

tar -zxvf kafka-eagle-bin-3.0.1.tar.gz 
tar -zxvf efak-web-3.0.1-bin.tar.gz 

# 最终解压后的文件为 efak-web-3.0.1
/usr/local/kafka-eagle/efak-web-3.0.1

```

## 2. 修改配置

- 修改conf/system.config.properties文件参数

```bash
# 1 zookeepr对应的集群和端口号， 可以监控多个cluster
efak.zk.cluster.alias=cluster1           
cluster1.zk.list=120.79.28.20:2181

# 2. offset信息是保存在kafak中的，将第二行注掉
cluster1.efak.offset.storage=kafka
# cluster2.efak.offset.storage=zk

# 3. 监控界面的数据，要保存在数据库中，因此必须提供mysql信息
# 数据库名称：必须是ke
efak.driver=com.mysql.cj.jdbc.Driver
efak.url=jdbc:mysql://39.108.99.248:3307/ke?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
efak.username=root
efak.password=123456
```

```bash
# 1. 配置环境变量
vim etc/profile
export KE_HOME=/usr/local/kafka-eagle/efak-web-3.0.1
export PATH=$PATH:$KE_HOME/bin

# 2. 刷新环境变量
source /etc/profile

# 3. 启动
./ke.sh start

# 查看是否启动
jps

# 4. 访问： 默认用户名和密码是： admin   123456
http://39.108.99.248:8048


# 如果出现异常问题，可以通过下面的错误日志来查看错误
/usr/local/kafka-eagle/efak-web-3.0.1/logs
```

 
