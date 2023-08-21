# 安装单机版

- 环境：CentOS7.6，MySQL 8.0.27
- [MySQL社区版](https://downloads.mysql.com/archives/community/)

![image-20230731163537301](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230731163537301.png)

### 上传文件

```bash
# mac本地, 将文件上传到linux服务器的/opt目录下
put /Users/shuzhan/Desktop/mysql-8.0.27-1.el7.x86_64.rpm-bundle.tar /opt

# 解压对应的文件得到多个文件,标注1的是需要的
tar -xvf mysql-8.0.27-1.el7.x86_64.rpm-bundle.tar

mysql-community-client-8.0.27-1.el7.x86_64.rpm            # 1
mysql-community-client-plugins-8.0.27-1.el7.x86_64.rpm    # 1
mysql-community-common-8.0.27-1.el7.x86_64.rpm            # 1
mysql-community-devel-8.0.27-1.el7.x86_64.rpm
mysql-community-embedded-compat-8.0.27-1.el7.x86_64.rpm
mysql-community-libs-8.0.27-1.el7.x86_64.rpm              # 1
mysql-community-libs-compat-8.0.27-1.el7.x86_64.rpm
mysql-community-server-8.0.27-1.el7.x86_64.rpm            # 1 
mysql-community-test-8.0.27-1.el7.x86_64.rpm

# 在当前目录下，创建目录
mkdir erick_mysql

# 将上面标注了1的文件，都拷贝到erick_mysql下面
cp mysql-community-c* erick_mysql/
cp mysql-community-libs-8.0.27-1.el7.x86_64.rpm erick_mysql
cp mysql-community-server-8.0.27-1.el7.x86_64.rpm erick_mysql
```

### CentOS7检查MySQL依赖

```bash
# 检查/tmp临时目录权限(必不可少)
- mysql在安装过程中，会通过mysql用户在/tmp目录下新建 tmp_db文件，所以请给/tmp较大权限
cd /
chmod -R 777 /tmp

# 检查存在libaio依赖, 存在net-tools依赖
# 存在时候的结果： libaio-0.3.109-13.el7.x86_64,     net-tools-2.0-0.25.20131004git.el7.x86_64
rpm -qa | grep libaio 
rpm -qa | grep net-tools

# 不存在的话安装
yum install -y libaio
```

### 安装

- 严格按照下面rpm顺序

```shell
cd opt/erick_mysql/
rpm -ivh mysql-community-common-8.0.27-1.el7.x86_64.rpm

rpm -ivh mysql-community-client-plugins-8.0.27-1.el7.x86_64.rpm

# 执行时候会
# warning: mysql-community-libs-8.0.27-1.el7.x86_64.rpm: Header V3 DSA/SHA256 Signature, key ID 5072e1f5:  # # NOKEY
# error: Failed dependencies:
# 	mariadb-libs is obsoleted by mysql-community-libs-8.0.27-1.el7.x86_64
# 先删除之前安装的lib
yum remove mysql-libs
rpm -ivh mysql-community-libs-8.0.27-1.el7.x86_64.rpm

rpm -ivh mysql-community-client-8.0.27-1.el7.x86_64.rpm

rpm -ivh mysql-community-server-8.0.27-1.el7.x86_64.rpm

# 安装完毕后，检查安装是否成功
# mysql  Ver 8.0.27 for Linux on x86_64 (MySQL Community Server - GPL)
mysql --version
mysqladmin --version
```

### 服务初始化

- 为了保证数据库目录与文件的所有者为mysql登录用户，如果是以root身份运行mysql服务，需要执行命令初始化

```bash
# 默认为root用户生成一个密码并将该密码标记为过期，登录后需要设置一个新的密码
# 生成的临时密码会保存在 /var/log/mysqld.log
mysqld --initialize --user=mysql
cat /var/log/mysqld.log   # wbil#RC>u6wQ          Z:>FM2>EcseK

# 启动mysql
systemctl start mysqld
systemctl stop mysqld
systemctl restart mysqld

# 查看是否启动
systemctl status mysqld

# 查看虚拟机启动后，mysql是否自启动：  enabled代表是
systemctl list-unit-files | grep mysqld.service

# 如果不是，设置为自启动
systemctl enable mysqld.service

# 关闭自启动
systemctl disable mysqld.service
```

### 登录

```bash
# 输入密码
mysql -u root -p

# 执行任意sql，则报错
ERROR 1820 (HY000): You must reset your password using ALTER USER statement before executing this statement.

# 先修改密码
alter user 'root'@'localhost' identified by '123456';

# 退出mysql，重新使用新密码登录
show databases;
```

### 远程连接

```sql
# 默认的root用户，只能是localhost来访问
USE mysql;
select host, user from user;

# 修改一下
UPDATE user SET host='%' WHERE user='root';

# 一定要刷新一下
FLUSH privileges;
```

### 密码安全强度-插件

- MySQL8.0之前，使用validate_password插件检测，验证账户密码强度，保障账户的安全性
- 通过安装/启用插件：会注册到元数据，也就是mysql.plugin表中，mysql重启后插件不会失效

```sql
mysql> INSTALL PLUGIN validate_password SONAME 'validate_password.so';

# 就会报错： ERROR 1819 (HY000): Your password does not satisfy the current policy requirements
ALTER USER 'root'@'%' IDENTIFIED BY 'abc1234';

ALTER USER 'root'@'%' IDENTIFIED BY 'Abc_1234';
```

#### 1 查看具体要求

```sql
SHOW VARIABLES LIKE 'validate_password%';

Variable_name                         | Value  |
+--------------------------------------+--------+
| validate_password_check_user_name    | ON     |
| validate_password_dictionary_file    |        |
| validate_password_length             | 8      |
| validate_password_mixed_case_count   | 1      |
| validate_password_number_count       | 1      |
| validate_password_policy             | MEDIUM |
| validate_password_special_char_count | 1      |
```

| 选项                                 | 默认值 | 描述                                                         |
| ------------------------------------ | ------ | ------------------------------------------------------------ |
| validate_password_check_user_name    | ON     | 设置为on时，表示能将密码设置为当前用户名                     |
| validate_password_dictionary_file    |        | 用于检查密码的字典文件的路径名，默认为空                     |
| validate_password_length             | 8      | 密码的最小长度                                               |
| validate_password_mixed_case_count   | 1      | 密码策略是medium或者strong，要求密码具有的小写和大写的最小数量 |
| validate_password_number_count       | 1      | 密码必须包含的数字个数                                       |
| validate_password_policy             | MEDIUM | 0：low，只检查长度<br>1：medium，检查长度，数字，大小写，特殊字符<br>2：strong，检查长度，数字，大小写，特殊字符，字典文件 |
| validate_password_special_char_count | 1      | 特殊字符的个数                                               |

#### 2 修改安全策略

```SQL
# 修改安全策略
SET GLOBAL validate_password_policy=LOW/MEDIUM/STRONG;
SET GLOBAL validate_password_policy=0/1/2;

# 修改密码长度
SET GLOBAL validate_password_length=6;
```

#### 3 删除插件

```
mysql> UNINSTALL PLUGIN validate_password;
```

#  Bin Log

- binary log：二进制日志文件，变更日志(update log)
- 记录数据库所有执行过的<font color=orange>DDL</font>和<font color=orange>DML</font>等数据库更新时间的sql，但是不包含任何查询语句(select, show等)
- 以<font color=orange>事件形式</font>记录并保存在<font color=orange>二进制文件</font>，通过bin log，可以再现数据库更新操作的全过程

## 1. 应用场景

- <font color=orange>数据备份，主备，主主，主从</font>都是需要bin log来实现

```bash
# 数据恢复
- mysql意外停止，通过bin log查看用户执行的更新操作，对数据库服务器文件做的修改，然后根据bin log来恢复数据库服务器

# 数据复制
- 由于日志的延续性和失效性，master将它的二进制日志传递给slave来达到master-slave数据一致性的目的
```

## 2. 日志参数

### 2.1 查看默认配置

```bash
# 查看默认配置
SHOW VARIABLES LIKE '%log_bin%';

log_bin,                             ON                                # 开启状态
log_bin_basename,                    /var/lib/mysql/binlog             # 二进制文件系列的存放路径：binlog.000001
log_bin_index,                       /var/lib/mysql/binlog.index       # 二进制文件系列的对应索引
log_bin_trust_function_creators      OFF                               # 主从复制时候，不信任函数比如 now()
log_bin_use_v1_row_events            OFF
sql_log_bin                          ON                                # 变更操作的sql，记录下来
```

### 2.2 参数设置

```bash
# log-bin
- 文件的具体路径/文件名，也可以是单独文件名，就选用上面参数
- 对应的新的log_bin和log_bin_index就是用这个名字
# binlog_expire_logs_seconds
- 二进制文件保留时长：单位为秒，默认30天
# max_binlog_size
- 单个二进制文件大小，如果超过此变量，执行切换动作，重新创建xxx.0002
- 最大和默认值是1GB，但不能严格控制binlog的大小，如果一个binlog比较靠近最大值而又遇到一个比较大的事务时，为了保证
  事务的完整性，可能不做切换日志的动作

[mysqld]
log-bin=shuzhan-binlog               
binlog_expire_logs_seconds=36000  
max_binlog_size=100M
```

### 2.3 查看日志

- mysql创建二进制文件时，先创建一个以filename.index文件，再创建一个以filename.000001的日志文件
- mysql<font color=orange>重启一次</font>，以.000001后缀的文件就会增加一个，并且后缀名按1递增(xxx.000001,xxx.000002,xxx.000003)
- 日志文件(系列)的个数与服务器启动的次数相同
- 如果日志长度超过了max_binlog_size，就会创建一个新的日志文件

```sql
# 查看当前日志文件
SHOW BINARY LOGS;
shuzhan-binlog.000001   156    No

# 二进制文件，官方提供工具来查看，但是没必要
- 生产中误删的数据，可以通过bin log来进行恢复
- 根据时间，或者根据sql来
```

### 2.4 删除日志

```sql
# 新增几个日志
FLUSH LOGS;

# 手动删除-按照文件
PURGE MASTER|BINARY LOGS TO '指定文件名';

PURGE MASTER LOGS TO '/var/lib/mysql/shuzhan-binlog.000003';       # 删除003之前的
```

## 3.  日志机制

### 3.1 写入机制

- 事务执行过程中，先把日志写到<font color=orange>binlog cache</font>，事务提交时，再把binlog cache写到binlog文件中
- 一个事务的binlog不能被拆开，无论这个事务多大，也要确保一次性写入
- 系统会给每个线程分配一个块内存作为binlog cache

![image-20230815140345873](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230815140345873.png)

```bash
# write
- 将日志写入到OS的page cache中，并没有把数据持久化到磁盘，速度较快

# fsync
- 将数据从page cache持久化到磁盘

# write和fsync的时机：sync_binlog
SHOW VARIABLES LIKE 'sync_binlog';

# 0
- 每次事务提交，只write，由OS决定什么时候执行fsync
- 性能提升，但是OS宕机，page cache里面的page cache会丢失

# 默认：1
- 每次事务提交，都会执行write和fsync
- 性能比较差 

# 折衷方案： N  N>1
- 每次提交事务都write
- 累积N个事务后，进行fsync
- 出现IO瓶颈后，将sync_binlog调大一点。但是风险高，丢失数据的概率大
```

### 3.2 binlog vs redolog

- redolog属于<font color=orange>物理日志</font>，记录内容是'在某个数据页做了什么样的修改'，属于InnoDB存储引擎层产生的
- binlog属于<font color=orange>逻辑日志</font>，记录内容是语句的原始逻辑，'修改某一行的某个字段'，属于MySQL Server层

### 3.3 两阶段提交

## 4. 中继日志

- Relay log: 只在主从服务器架构的<font color=orange>从服务器上</font>存在
- 从机为了和主服务器保持一致，要把主服务器上的binary log读取到，写入到<font color=orange>从机本地的日志文件</font>
- 从机读取中继日志，完成主从服务器的数据同步
- 每次开启一次同步，就会产生一个文件

```bash
# relay log
- 默认保存在从服务器的数据目录下
- 文件名格式：    从服务器名-relay-bin.0000001, 从服务器名-relay-bin.index
```

# 主从复制

## 1. 作用

- master：主机，写入数据，写库
- slave：从机，读取数据，读库
- master更新数据后，自动将数据复制到slave上，客户端读取数据时，从slave中去读取

![image-20230815143701995](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230815143701995.png)

### 1.1 读写分离

- 主从复制的方式同步数据，再通过读写分离提高数据库并发处理能力

```bash
# 优点
- 实现高并发访问，读取更加顺畅
- 减少锁表影响： 主库写数据时加的写锁，不会影响从库进行select读取
```

### 1.2 数据备份

- 主从复制，数据实现<font color=orange>热备份机制</font>，主库正常运行时候进行的备份，不会影响到主服务

### 1.3 高可用

- 当master出现故障时，可以快速切换到slave，保证服务正常运行

## 2. 原理

- 每个slave只有一个master
- 每个slave只能有一个唯一的服务器ID
- 每个master可以有多个slave

```bash
# 二进制日志转储线程： binlog dump thread
- 主库线程。当从库线程连接时，主库将二进制日志文件发送给从库
- 当主库读取事件(event)时，会在binlog上加锁，读取完成后，释放锁

# 从库I/O线程
- 连接到主库，向主库发送请求更新binlog
- 从库的I/O线程读取主库的二进制日志转储线程发送的binlog更新部分，并且拷贝到本地的中继日志(relay log)

# 从库SQL线程
- 读取从库中的中继日志，并且执行日志中的事件，将从库中的数据和主库保持同步
```

![image-20230815142107587](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230815142107587.png)

![image-20230821103259829](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230821103259829.png)

## 3. 一主一从安装

![image-20230821103655267](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230821103655267.png)

- 按照单机版本步骤，分别搭建两个两个mysql

```bash
# 修改 /var/lib/mysql/auto.cnf，主/从的server-uuid不能一样,一般也不会一样
[auto]
server-uuid=xxxxx
```

### 3.1 主-读写

- 所有配置都是在<font color=orange>[mysqld]</font>结点下，且都是小写字母
- 修改完成后，重启生效

```properties
# /etc/my.conf
# 必须
# 主服务器唯一id
server-id=1
# 启用二进制日志，指名路径, 可以全路径
log-bin=erick-master-binlog


# 可选
# 0：默认，读写     1：只读
read-only=0

# 日志文件保留时长，单位为s
binlog_expire_logs_seconds=6000

# 控制单个二进制日志文件大小，最大值和默认值是1GB
max_binlog_size=500M

# 不要复制的数据库
binlog-ignore-db=test

# 需要复制的数据库，默认全部记录
binlog-do-db=xxxxx

# binlog格式
binlog_format=STATEMENT
```

### 3.2 从-只读

```properties
# 必须
# 主服务器唯一id
server-id=2

# 可选
# 指明中继日志的前缀
relay-log=erick-slave-relay
```

### 3.3 建立连接

```sql
# 主
CREATE USER 'erick-slave'@'%' IDENTIFIED BY 'erick_@0507';

GRANT REPLICATION SLAVE ON *.* TO 'erick-slave'@'%';

# 必须执行,否则从机会报错
ALTER USER 'erick-slave'@'%' IDENTIFIED WITH mysql_native_password BY 'erick_@0507';
FLUSH PRIVILEGES;

SHOW MASTER STATUS;

+----------------------------+----------+--------------+------------------+-------------------+
| File                       | Position | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set |
+----------------------------+----------+--------------+------------------+-------------------+
| erick-master-binlog.000001 |     1022 |              |                  |                   |
+----------------------------+----------+--------------+------------------+-------------------+
```

```sql
# 从
# 下面的部分信息，通过上面的SHOW MASTER STATUS来获取
CHANGE MASTER TO 
MASTER_HOST='124.221.202.112', 
MASTER_USER='erick-slave',
MASTER_PASSWORD='erick_@0507',
MASTER_LOG_FILE='erick-master-binlog.000001',
MASTER_LOG_POS=4166;

# 开始同步
START SLAVE;

# 检验 :                Slave_IO_Runnniing=yes  Slave_SQL_Rruning=yes
SHOW SLAVE STATUS\G;
```

```sql
# 从
# 断开同步, 然后重复上面的步骤，再次从指定位置进行同步
STOP SLAVE;
```



## 4. binlog格式设置

- binlog_format=xxx

### 4.1 STATEMENT

- 基于SQL语句的复制， statement-based replication，SBR
- 默认的同步格式

```bash
# 优点
- 历史悠久，技术成熟
- 不需要记录每一行的变化，减少了binlog的日志量，文件较少
- binlog包含了所有数据库更改信息，可以根据这个来审核数据库的安全情况
- binlog可以用于实时的还原，而不仅仅用于复制
- 主从mysql版本可以不一样，从服务器版本可以比主服务器版本高(向下兼容)

# 缺点
- 不是所有的UPDATE语句都能被复制，尤其是包含不确定操作时，如 UUID()， NOW()，导致主从数据不一样
- 对于一些复杂的sql,从服务器上的耗费资源情况比较严重
- 数据表必须主从完全一致，否则会出错
```

### 4.2 ROW

- 不记录每条sql语句的上下文信息，仅记录那条数据被修改了，修改成什么样了
- Row-based Replication

```bash
# 优点
- 任何情况下都可以被复制，对于复制来说，安全可靠
- 多数情况下，从服务器上的表如果有主键，复制速度快
- 从服务器上采用多线程来执行复制

# 缺点
- binlog比较大
- 复杂的回滚时，包含大量数据
- 无法看到binlog中都复制了什么sql
```

### 4.3 MIXED

- 结合statement和row，mysql会自动进行选择

## 5. 数据一致性

### 5.1 异步复制

![image-20230821173007496](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230821173007496.png)

### 5.2 半同步复制

- 只要有一个从库进行了相应，就可以返回给客户端
- rpl_semi_sync_master_wait_for_slave_count=1： 在主库中设置

![image-20230821173228562](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230821173228562.png)

### 5.3 组复制

- 基于paxos协议，超过一半的响应，就算是响应了

![image-20230821173420723](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230821173420723.png)
