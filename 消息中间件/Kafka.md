- *开源的分布式事件流平台(Event Streaming Platform)*
- *高性能数据管道，流分析，数据集成，关键任务应用*
- *大数据场景一般采用kafka作为消息队列：削峰，解耦，异步通信*

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
# zookeeper
- 记录server节点状态
- 记录leader-follower相关信息
- kafka-2.8.0前，必须配合zk使用， 2.8.0后，去zk化
```

## 3. 副本

- 防止单节点故障，提高数据可靠性
- 副本数量：leader+follower

```bash
# 1. leader-follower机制
- producer和consumer: 只和msg所在的leader节点通信
- leader获取数据后，会同步到follower节点
- leader挂掉后，follower有条件会成为新的leader

# 2. 副本数量
- 太多副本： 进一步提高数据可靠性。 但会增加磁盘存储空间，增加网络数据传输，降低效率
- 标准数 = Kafka集群的server数量-1

# 3. 副本信息  AR = ISR + OSR
   # AR (Assigned Replicas) 
   - 分区中所有的副本， 包含leader和follower
   
   # ISR(In-sync Replcaition Set)
   - 和leader保持同步的follwer集合，包括leader + follower
   - 如果某follower长时间未向leader发送通信请求或同步数据，该follower就会被踢出ISR
   - replica.lag.time.max.ms:30000     默认30s
        
   # OSR
   - follower和leader同步时，延迟过大的副本集合
```

- 副本数量为3的数据架构

![image-20240323103027573](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240323103027573.png)

# Producer

- 客户端，调用Kafka的API，向Kafka Cluster端发送数据

![image-20241110104513323](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20241110104513323.png)

## 1. Interceptors

- 拦截器：拦截消息，进行一些过滤，可在Java项目中自定义，一般不用
- 外部应用(Flume, Flume也会自己带拦截器)作为数据源，导入时候可以通过拦截器进行过滤

## 2. Serializer

- 序列化器：跨节点通讯，不用java自带的序列化， 采用轻量级序列化，避免序列化信息太多引发数据过大
- 后续消费消息时，也需要配置反序列化工具
- 对key和value来进行序列化

```bash
# key, value
- key： msg的一个名字，  value：msg的具体的内容
- key和value可以为任意类型的数据
- key可以不存在，value必须存在
```

![image-20240322121859157](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240322121859157.png)

## 3.  Partitioner

- 分区器：决定数据应该存放在哪个分区

![image-20241110104909347](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20241110104909347.png)

### 优点

```bash
# 数据均衡
- 数据可以存放在集群不同的broker上
- brokers内存配置相同，---> 负载均衡
- brokers内存配置不同，---> 指定负载
    - 自定义分区，使不同类型数据，存放在不同的partion上
    - 比如根据key，不同数据库的数据，用表名做为key，存放在不同分区
    - 过滤脏数据

# 高并发
- producer：和单个broker通信 vs 和多个broker通信，效率提升
- consumer：可以以分区为单位消费数据
```

### 策略

- 分区数：0，1，2等， 根据broker节点向zookeeper注册时间和数目来判断
- 假如存在三个broker，kafaka自动生成有效分区：0，1，2

```bash
# RoundRobinPartitioner： 默认分区策略
- 尽可能让数据均衡的分布在多个不同的borker上

# 2. 自定义分区器： 
#    2.1. 实现 package org.apache.kafka.clients.producer.Partitioner 接口
#    2.2. 重写partition()方法
#    2.3  写入Producer的config中
```

## 4. RecordAccumulator

- 分区器生成好的数据，先在JVM本地的DQueue中进行缓存，后续发往对应分区
- 对应的Topic，有几个分区，就会有几个DQueue

![image-20241110110545861](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20241110110545861.png)

### trigger

- batch.size和linger.ms满足其中一个, sender-thread 就可以从RecordAccumulattor 发送到cluster

```bash
# batch.size: 
          - 数据以批次的形式进行发送
          - 当数据积累到batch.size后，sender才会拉取数据
          - 默认 16k
# linger.ms
          - 如果数据迟迟没有达到batch size， 等到linger.ms时间后，sender也会将数据当作一个批次，进行拉取
          - 默认0ms，即无延迟发送，但效率较低   
```

### concurrency

```bash
# 实时性 vs 高性能
- batch.size 和 linger.ms
          
# memory pool
- Dequeue从memory pool中申请内存
- 数据发送成功后，Dequeue的内存再还回到memory pool中
- 假如partition较多 扩大缓冲区

# 数据压缩，不同压缩格式： gzip, snappy, lz4, zstd,           snappy用的较多
- compression.type: 压缩 snappy
- 确保发送方和消费方使用同一种压缩算法
```

![image-20220906171442070](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220906171442070.png)

## 5. Sender-Thread

- 将RecordAccumulator中的数据，发送到Kafka的cluster中

```bash
# 1. Sender-Thread
- NetworkClient: 负责拉取数据发往kafka server

# 2. Selector
- 将数据发送到对应的broker的对应partition
```

## 6. ACK

- Kafka cluster收到数据后，会进行leader-follower之间同步， 再进行应答

![image-20220909155828011](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220909155828011.png)

```bash
# 参数： acks 
# 0： 基本不用
- leader收到数据即ack，不用等数据落盘，即将数据的Request进行删除
       # 问题
       - producer发送完，leader还未落盘，leader挂了，数据就没有发送过去

# 1： 普通日志，允许丢部分数据
- leader收到数据，并落盘后，再ack
      # 问题
      - producer发送完，leader收到数据落盘并ack
      - leader还未完成同步，挂了，其他follower成为新的leader后，并没有之前数据

# -1， all： 金融类场景
- leader和所有的follower都收到并落盘后，才进行ACK
      # 问题
      - 假如一个follower因为网络问题，迟迟不能应答，ACK动作一直不能完成： 动态ISR
```

### 6.1 可靠性

- 数据最少发送一次(可能重复发送)

```bash
# min.insync.replicas = 1        ISR中应答的最小副本数量 默认为1
- ACK = -1 
- 分区副本 >= 2 
- ISR中应答的最小副本数量 >= 2        #  默认为1
```

### 6.2 重复发送

- 基于上面可靠性，可能发生producer重复发送数据问题

```bash
- 假如leader收到数据，向follower同步完成后，在应答瞬间，leader挂了, ack失败 
- 其他follower成为leader，继续接收上次的数据，就会产生重复的消息
- 概率较小，但是依然可能发生
```

![image-20240311113827271](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240311113827271.png)

## 7. 消息重复

### 幂等性-单分区单会话

- 一条数据，producer不论向broker发送多少次，broker端都只会持久化一次

```bash
# enable.idempotence:true         默认true 

# Producer发送数据时
- 生成<PID + Partition + SeqNumber>，作为消息的主键
# Kafak Server端
- 缓存这个主键，来进行消息去重， 如果数据重复，就会在内存中将数据删除，不会进行落盘
- 解决上面应答失败，造成的消息重复问题

     # PID: ProducerID
       - producer的唯一标识
       - Kafka重启后，对应的ProducerId就会重新分配(单会话)
       - kafka挂了重启后，app重连后，消息发送依然可能重复

    # Sequence Number
      - Producer发送的每批次消息都会带有此序列号
      - 单调递增，从0开始
      - 属于Producer的属性

    # Partition
      - 分区
```

### 事务

- 开启事务，必须开启幂等性
- 保证多分区多会话

## 8.有序性

###  8.1 单分区

- broker端，最多会缓存producer发过来的最近5个request批次的元数据，可以保证该批数据是有序的
- 数据落盘后并ack后，会从缓存队列中删除

```bash
#  1. broker接受到request1和request2，落盘后进行ack
#  2. request3发送后，假如本次发送时间较长，broker迟迟没有接受到该request
#  3. 后续request4和request5发送过来后， 通过序列号发现前面还有待落盘的request3，则request4和request5暂时保存在内存中
#  4. request3完成落盘后，request4和request5再完成

#   开启幂等性
enable.idempotence=true                   # 需要用到其中的自增的sequence number来进行request元数据的排序
max.in.flight.requests.per.connection=5   # 需要设置小于等于5， 每个网络链接中最大可同时发送的   数据批次

# 未开启幂等性
# 该值为1时，才能保证幂等性
max.in.flight.requests.per.connection=1


# max.in.flight.requests.per.connection=5
- 假如其中两个发送失败，进行了retry，就会一直占据两个链接(retry机制)
- 假如其中5个发送失败，并且一直进行retry，消息发送就直接被block了

# max.in.flight.requests.per.connection=1
- 假如一条消息一直发送失败，则就会block后续所有的数据发送

# 重试机制
- 发送失败，会进行重试
- 参数 ： retries
- 默认为int的最大次数
```

![image-20220907091648873](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220907091648873.png)

### 8.2 跨分区

- 多分区，分区与分区间数据无序
- 如果要实现跨分区有序，可以发送数据时指定key，然后在consumer端进行数据重排，效率低

### 8.3 绝对有序

- 如果需要绝对有序，则建议发送消息时候用单分区

```bash
# 单分区
- 发送数据时候，指定分区

# 单一生产者
- 如果多个producer一起向某个分区发送数据，则依然可能乱序
```

## 9. 调优

- 上面参数，在项目启动后，可以通过日志查看到对应的参数

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

# Consumer

```bash
# pull: kafka采取的模式
- consumer主动从broker中拉取数据    
- consumer根据自身的消费消息的速率，进行适当的拉取
- 若kafka没有数据，consumer需要不断询问，消费消费者资源

# push
- broker向consumer推送消息
- broker决定推送消息的速率，难以满足不同consumer
```

## 1. 消费原理

![image-20220910153752707](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910153752707.png)

## 2. consumer组

- consumer group，通过groupId来区分，不同consumer group，是独立的消费者
- consumer group中可包含多个consumer

```bash
# 同一消费者组
- 同一个partition，只能被一个consumer消费(引发重复,kafka直接杜绝了这个)
- consumer数量超过了partition数，则一部分consumer处于idle状态

# tips
- 引入主要目的是为了快速消费不同partition

group.id="citi-erick"
```

### 2.1 consumer group initialization

- coordinator：辅助实现consumer组的初始化和分区配置
- 每个broker节点都有对应的coordinator

```bash
# consumer_offses的分区数量： 默认为50

# 目标coordinator节点 = hashCode(groupId) % 50 (_consumer_offsets的分区数量，可以调整)
      - hashCode(groupId)%50 = 1, 则对应的分区数为1
      - consumer_offsets的1号分区上，对应的broker刚好为1
      - 则选取broker-1的coordinator作为目标
```

![image-20241125104654879](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20241125104654879.png)

### 2.2 rebalance

- 三个参数都是在consumer端进行配置
- 再平衡会影响Kafka的性能

```bash
# heartbeat
- 每个consumer都会向coordinator保持心跳
- 默认3s
- heartbeat.interval.ms=3000         

# 再平衡
    # 超时心跳
    - 超时未发送心跳，该consumer就会被移除，并触发再平衡
    - 默认45s
    - session.timuout.ms=45000
          # 45s内，某个consumer挂掉，则该consumer的任务交给某个consumer去消费
          # 45s后，consumer依然没有恢复，该consumer就会被移除该消费者组，触发rebanlance
    
    # consumer处理消息时间长
    - consumer处理消息时间过长, 触发rebanlance (会导致重复消费)  
    - 默认5min
    - max.poll.interval.ms = 5 min
         # 将该consumer踢出consumer group，并把任务重新交给其他的consumer去处理，并重新制定消费计划
         # 有心跳，但是被强制踢出
```

### 2.3 消费过程

![image-20220907140144643](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220907140144643.png)

```bash
# 实时性 vs 效率： 二者满足其一即可
    # 每批次拉取的最小数据：只有大于20k时，才回拉数据
    fetch.min.byte=20k

    # 未达到大小时，超时就会依然拉取
    fetch.max.wait.ms=500ms

# 每批次拉取的最大数据量
fetch.max.bytes=50m

# parseRecord:   数据的反序列化
# Interceptors:  拦截器(可以用来做数据的统计)
```

## 3. 分区分配策略

- consumer-leader在制定消费策略时，哪个consumer来消费哪个partition
- partition.assignment.strategy：可以在代码端配置

### Range

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

### RoundRobin

```bash
# 分区规则： 针对集群中所有topic
- 把所有的topic的partition(topic + partition)，按照hashcode进行排序， 再对consumer进行排序
- 轮询分区策略： 通过轮询算法分配给每个consumer
```

![image-20220910163624443](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910163624443.png)

##  4. offset

- consumer消费topic的partition时，假如consumer挂掉，下次consumer重启时需要知道从哪里开始继续消费
- offset维护在kafka的系统主题中： _consumer_offsets

![image-20220907140510556](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220907140510556.png)

###  4.1 存储位置

```bash
# 存放位置： kafka broker
- offset信息存放在。 ‘_consumer_offsets’  topic中

 # k-v
- key： groupId + topic + partition                value: 当前offset值
- 每隔一段时间，kafka内部会对该topic进行压缩， 即groupId + topic + partition  保留最新数据

# _consumer_offsets:  该topic默认不能被消费
- exclude.internal.topics=false,  默认为true
- 可修改 config/consumer.properties中添加上面属性
- 也可consumer端代码中配置参数
```

###  4.2 提交方式

####  自动offset--重复消费

- kafka自动存在offset功能
- 每次提交完毕后，才会继续消费下一批数据

```bash
# enable.auto.commit:true               
- 是否开启自动提交offset功能， 默认为true
# auto.commit.interval.ms:5000          
- 自动提交offseet的时间间隔，默认是5s

# 1. 每次消费后，consumer不用提交，继续消费后面的数据
# 2. 5s后，kafka自动将该consumer上一个的offset进行提交
```

![image-20240316164053305](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240316164053305.png)

- 重复消费： 自动提交offset引发

```bash
- 如果提交了一次offset，2s后consumer挂了
- 再次重启consumer，则从上次提交的offset继续消费，导致重复消费
```

![image-20240316173345390](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240316173345390.png)

####  手动offset--消息丢失

- 自动提交offset十分简单，但是是基于时间提交的，kafka提供更精准的手动提交的功能

```bash
# 同步提交---commitSync
- 将本次消费的一批数据的offset提交
- 阻塞当前线程，一直到提交成功，才会进行下一批次的消费
- 并且会自动失败重试(不可控因素导致，提交失败)

# 异步提交---commitAsync,    用的比较多
- 将本次提交的一批数据的offset提交
- 可能出现提交失败问题

# 具体操作代码
- ConsumerConfig中配置关闭自动提交参数
- 消费完后进行commitAsync或commitSync
```

- 异步手动提交：消息丢失

```bash
# 异步手动提交
- 当offset被提交时，数据还在consumer端正在处理(比如落库)，consumer突然挂了
- 再次重启consumer，则之前的数据丢失
```

### 4.3 指定offset消费

```bash
# 包含earliest, latest, none

# earliest：   
- 当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费

# latest：  默认值
- 当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据

# none：      
- 如果没找到消费者组的先前偏移量， 则向consumer抛出异常

auto.offset.reset=latest
# 也可以指定offset或者指定时间进行消费
```

## 5. 调优

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

# Broker Server

## 1. 文件存储

- Topic是逻辑概念，Partition是物理概念

![](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220910110525667.png)

### 存储位置

- 配置文件中配置data存储的位置
- 每个topic的每个partition对应一个文件目录，包含对应的消息的文件

```bash
# 1. /kafka/data/citi-0 ， 包含leader的数据和其他partition的follower的数据
- 因此会出现比如 citi-0    citi-1两个目录， 是因为其中一个是leader数据，另外一个是其他partition的follower数据

# 2. segment：一个topic+partitionNo 中的数据，会再次切分为多个segment，每个segment大小为1G
# server.properties
- log.segment.bytes=1073741824
```

### 存储内容

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

### 稀疏索引

- 通过不同的index，能够快速定位到消息
- index采用稀疏索引，大约每往.log文件中写入4k数据，才会往index里面写入一条索引

```bash
log.index.interval.bytes:4kb
```

### 数据查找

![image-20240315210103367](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20240315210103367.png)

```bash
# 1. 根据offset，定位segment文件
# 2. 找到小于等于目标offset的最大offset对应的索引项
# 3. 定位到log文件
# 4. 向下遍历，找到目标Record
```

### 数据添加

- 产生的新数据，不断追加到该.log文件末尾
- 速度较快

## 2.清除策略

- 如果consumer能力不足，则可能数据丢失

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

### DELETE

- log.cleanup.policy=delete
- 默认开启

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

### COMPACT

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

