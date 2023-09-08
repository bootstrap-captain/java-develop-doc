# 字符集

## 1. 默认字符集

- 字符集表示一个字符所用的最大字节长度

```sql
SHOW VARIABLES LIKE 'character%';

character_set_client,        utf8mb4
character_set_connection,    utf8mb4
character_set_database,      utf8mb4            # 具体的database
character_set_filesystem,    binary
character_set_results,       utf8mb3
character_set_server,        utf8mb4             # server端
character_set_system,        utf8mb3 
character_sets_dir,          /usr/share/mysql-8.0/charsets/
```

## 2.各级别字符集

- CHARACTER SET/：从上到下，如果不显式指明当前层的字符集，则使用上一层的字符集
- COLLATE： 比较规则，类似上述法则

### 2.1 服务器级别

- <font color=orange>character_set_server</font> ： mysql服务器级别的字符集

### 2.2 数据库级别

- <font color=orange>character_set_database</font>： 当前数据库的字符集

```SQL
# 创建时指明
CREATE DATABASE db_test2 CHARACTER SET utf8mb4;

# 修改字符集
ALTER DATABASE db_test2 CHARACTER SET utf8mb3;

# 查看当前数据库字符集
SHOW CREATE DATABASE db_test2;
```

### 2.3 表级别

- 可以在创建或者修改表的时候指定表的字符集和比较规则

```sql
# 创建时候
CREATE TABLE book1
(
    name VARCHAR(10)
) CHARACTER SET utf8mb4;

#修改
ALTER TABLE book1 CHARACTER SET utf8mb3;

# 查看
SHOW CREATE TABLE book1;
```

### 2.4 列级别

```sql
# 创建
CREATE TABLE book2
(
    address VARCHAR(10),
    name    VARCHAR(10) CHARACTER SET gbk
) CHARACTER SET utf8mb4;

# 修改
ALTER TABLE book2
    MODIFY name VARCHAR(20) CHARACTER SET utf8mb3;
```

## 3. 常见字符集

### 3.1 字符集/比较规则

- mysql一共支持41种字符集

```sql
# 查看mysql中支持的字符集合以及其对应的比较规则
SHOW CHARACTER SET;

# Default collation
- 当前字符集种默认的比较规则

# Maxen
- 该字符集表示一个字符最多需要几个字节
```

![image-20230801152529764](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230801152529764.png)

| 后缀 | 英文解释           | 描述             |
| ---- | ------------------ | ---------------- |
| _ai  | accent insensitive | 不区分重音       |
| _as  | accent sensitive   | 区分重音         |
| _ci  | case insensitive   | 不区分大小写     |
| _cs  | case sensitive     | 区分大小写       |
| _bin | binary             | 以二进制方式比较 |

```SQL
# 查看某个字符集支持的比较规则
SHOW COLLATION LIKE 'utf8%';

SHOW COLLATION LIKE 'gbk%';

# 查看服务器的字符集及比较规则
SHOW VARIABLES LIKE '%_server';

# 查看数据库的字符集及比较规则
SHOW VARIABLES LIKE '%_database';
```

### 3.2 utf8和utf8mb4

- mysql中，utf8是utf8mb3的别名

| 字符集  |                                                              |
| ------- | ------------------------------------------------------------ |
| utf8    | 一个字符需要1-4个字节来表示，a，b，c用1字节，汉字用3字节     |
| utf8mb4 | 正宗的utf8字符集，使用1-4个字节表示字符，可以存储一些emoji表情 |
| utf8mb3 | 阉割过的utf8字符集，只使用1-3个字节表示字符                  |

# 系统变量

- 系统定义的，属于<font color=orange>服务器</font>层面。启动MySQL服务器实例期间，为服务器内存中的系统变量赋值
- 定义了MySQL服务器实例的属性，特征，要么是<font color=orange>编译MySQL时参数的默认值</font>，要么是<font color=orange>配置文件</font>(my.conf)中的参数值
- [官网系统变量](https://dev.mysql.com/doc/refman/8.0/en/server-system-variable-reference.html)
- 如果一个变量，既是全局系统变量，也是会话系统变量，则修改时候一定要声明，到底是改的什么范围

![image-20230807105100648](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230807105100648.png)

## 1. 全局系统变量

- <font color=orange>global</font>：在MySQL初始化启动后，所有的全局系统变量就已经加载好了
- set进行修改，针对所有会话都有效，但是服务器重启后，修改失效
- <font color=orange>静态变量</font>：特殊的全局系统变量，在MySQL服务实例运行期间，它们的值不能使用set动态修改。如innodb_buffer_pool_instances

```sql
# 查看所有全局变量
SHOW GLOBAL VARIABLES;

# 查看满足条件的
SHOW GLOBAL VARIABLES LIKE '%标识%';

# 查看指定系统变量: @@是标记系统变量
SELECT @@GLOBAL.变量名;

# 先查找会话系统变量，如果不存在，则查找全局系统变量
SELECT @@变量名;
```

## 2. 会话系统变量

- <font color=orange>session</font>：会话连接后，会有默认值，也可以去设置这些值
- 如果不写，默认会话级别
- 只在当前会话有效，如果修改了某个会话系统变量的值，不会影响其他会话

```sql
# 查看所有会话变量, SESSION可以省略
SHOW SESSION VARIABLES;
SHOW VARIABLES;

# 查看满足条件的
SHOW SESSION VARIABLES LIKE '%标识%';

# 查看指定系统变量
SELECT @@SESSION.变量名;
```

## 3. 修改系统变量

### 3.1 my.conf文件

- 修改方式是永久的
- 必须重启MySQL服务器才能生效

### 3.2 set修改

```SQL
# 全局系统变量：服务器重启后失效
SET @@GLOBAL.变量名=变量值;
SET GLOBAL 变量名=变量值;

# 会话系统变量：不影响其他会话，会话关闭后失效
SET @@SESSION.变量名=变量值;
SET SESSION 变量名=变量值;
```

### 3.3 推荐方案

- 先设置GLOABL，保证修改时候，不影响MYSQL服务
- 再修改my.conf文件，保证后续重启后，就直接使用配置文件

# 文件目录

## 1. 配置文件

-  数据库的启动配置文件
- <font color=orange>/etc/my.conf</font>： 一般修改后，需要重启MySQL服务器，配置文件才会生效

## 2. 库/表目录

- MySQL启动后，会加载某些目录下的文件到内存中，后续产生的数据也会存储到某些目录
- /var/lib/mysql

```sql
# 该文件目录保存在系统变量中
SHOW VARIABLES LIKE  'datadir';
/var/lib/mysql

# 数据库某个库
- /var/lib/mysql/库名/表名

# InnoDB： 表结构和表数据，都放在.idb文件中 
- /var/lib/mysql/库名/book1.ibd

# MyISAM存储引擎
- book.MYD(MYData)：   数据信息文件，存储数据信息
- book.MYI(MYIndex)：  存放索引信息文件
- book_372.sdi：       描述表结构文件，字段长度等
```

```bash
# ibd文件解析
- Oracle提供了一个应用程序 ibd2sdi, MySQL8.0自带的
# 进入到指定数据库下，解析对应表的ibd文件，得到对应的.ibd文件的txt文件
- ibd2sdi --dump-file=cat.txt cat1.ibd
```

# 用户管理

- root用户拥有最高权限，可以用root用户登录后，创建其他的用户

## 1. User

- 用户的表存放在mysql.user表中，该表的主键是 host+user

```sql
# 1. 查看当前server的用户名和密码
SELECT host, user FROM mysql.user;


# 2. 创建新用户： 默认创建的用户没有任何权限
-- 默认host是 %， 代表任何IP都可以访问
CREATE USER 'erick' IDENTIFIED BY 'abcd1234';
-- 创建时候尽可能限制用户的登录主机，一般是限制成指定IP或者内网IP段
CREATE USER 'lucy'@'%' IDENTIFIED BY 'abcd1234';
-- 只允许localhost来访问
CREATE USER 'lucy'@'localhost' IDENTIF IED BY 'abcd1234';


# 3. 修改用户名
-- 必须拥有DROP USER权限，才可以删除
UPDATE mysql.user SET user='tom' WHERE user='lucy' and host='%';
FLUSH PRIVILEGES;

# 4. 删除用户
-- 不建议使用delete语句来删除对应的mysql.user表，因为其他的一些权限信息等，可能没有删除干净
-- 默认删除 host为%的用户
DROP USER 'lucy';


# 5. 修改用户名密码
# 当前用户修改当前用户
ALTER USER USER() IDENTIFIED BY 'NIKE_#_984';  -- 方式1
SET PASSWORD='NIKE_#_989';                     -- 方式1


# 修改其他用户密码：必须拥有对应的权限
ALTER USER 'tom'@'%' IDENTIFIED BY 'abcd_1234';  -- 方式1
SET PASSWORD FOR 'tom'@'%'='abcd_1235';          -- 方式1
```

## 2. 密码管理

### 2.1 过期策略

#### 手动过期

```sql
# root设置过期
ALTER USER 'tom' PASSWORD EXPIRE ;

# tom用户可以用过期密码登录，但是不能执行查询,必须先修改密码
# ERROR 1820 (HY000): You must reset your password using ALTER USER statement before executing this statement.
```

#### 自动过期-全局

- default_password_lifetime：系统变量建立全局密码过期策略
- 默认是0，表示永不过期
- 取值：正整数N，n天后过期

```sql
# 修改环境变量
SET PERSIST default_password_lifetime=180;
SHOW VARIABLES LIKE 'default_password_lifetime';

# 配置my.conf文件
[mysqld]
default_password_lifetime=180
```

### 2.2 密码安全强度-插件

- MySQL8.0之前，使用validate_password插件检测，验证账户密码强度，保障账户的安全性
- 通过安装/启用插件：会注册到元数据，也就是mysql.plugin表中，mysql重启后插件不会失效

```sql
mysql> INSTALL PLUGIN validate_password SONAME 'validate_password.so';

# 就会报错： ERROR 1819 (HY000): Your password does not satisfy the current policy requirements
ALTER USER 'root'@'%' IDENTIFIED BY 'abc1234';

ALTER USER 'root'@'%' IDENTIFIED BY 'Abc_1234';
```

#### 查看具体要求

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

#### 修改安全策略

```SQL
# 修改安全策略
SET GLOBAL validate_password_policy=LOW/MEDIUM/STRONG;
SET GLOBAL validate_password_policy=0/1/2;

# 修改密码长度
SET GLOBAL validate_password_length=6;
```

#### 删除插件

```
mysql> UNINSTALL PLUGIN validate_password;
```

# 权限管理

```sql
# 查看数据库都有哪些权限
SHOW PRIVILEGES;

# 新用户
- 新创建的用户，一般权力基本没有
```

## 1. 用户权限

### 1.1 权限操作

```sql
# 查看当前用户权限
SHOW GRANTS;
SHOW GRANTS FOR CURRENT_USER;
SHOW GRANTS FOR CURRENT_USER();

# 查看某用户的权限: 查看操作也需要权限
SHOW GRANTS FOR 'tom'@'%';
```

```SQL
# 将某个库下的指定权限赋予给指定用户
GRANT SELECT, UPDATE ON db_test1.* TO 'lucy'@'%';

# 将所有的权限都给指定用户
# 先执行下面的，否则会报错
GRANT SYSTEM_USER ON *.* to 'root'@'%';
FLUSH PRIVILEGES ;

GRANT ALL PRIVILEGES ON *.* TO 'tom'@'%';

# 同一个用户多次给权限，权限叠加而非权限覆盖
```

```sql
# 权限回收
# 用户重新登录后才有效
REVOKE UPDATE ON db_test1.* FROM 'lucy'@'%';

REVOKE ALL PRIVILEGES ON *.* FROM 'tom'@'%';
```

### 1.2 权限表

**mysql.user**

```SQL
# host:               当前允许的ip访问
# user：              用户名
# xxx_priv：          当前用户对所有表的权限是Y还是N
# ssl_xxx：           证书认证相关的
# max_connections：   当前用户每小时的最大连接次数
# max_questions：     当前用户每小时的最大查询次数
# max_updates：       当前用户每小时的最大更新次数
# authentication_string： 加密后的密码
SELECT host, user,  Insert_priv, Update_priv,ssl_type, max_connections, max_questions, max_updates, authentication_string  FROM mysql.user
```

**mysql.db**

- 某个用户对某个表的具体的权限

``` SQL
# host + user + db 作为联合主键
SELECT host , user, db, Select_priv FROM mysql.db;

# 不记录root用户或者拥有所有库权限的用户
```

- 还有其他更细粒度的表

## 2. 角色管理

- 角色是权限的集合，可以为角色添加或移除权限
- 用户可以被赋予角色，同时就会被授予角色所包含的权限
- 方便管理拥有相同权限的用户

```sql
# 创建角色
# host_name不加时，默认是%
CREATE ROLE 'manager'@'%', 'admin'@'%';

# 给角色赋权
# 创建的角色，默认没有任何权限
GRANT SELECT ON db_test1.book1 TO 'manager';
GRANT ALL PRIVILEGES ON *.* TO 'admin';

# 查看某个角色权限 
SHOW GRANTS FOR 'manager'@'%';

# 回收某个角色权限
 REVOKE SELECT ON db_test1.book1 FROM 'manager';
 
 # 删除角色
 DROP ROLE 'manager'@'%';
 
 
 # 2. 将角色给用户
 
 # 将admin这个role给tom这个角色
GRANT 'admin'@'%' TO 'tom'@'%';

# 查看当前user被赋予的role的角色(激活的角色)
SELECT CURRENT_ROLE();

# 默认:一个role给一个角色后，该角色对应的role默认暂不开启，要使用，必须先开启
# 方式一：显示开启
# 将admin的角色，对tom用户激活
# 用户必须重新登录，才能看到赋予的角色
# 如果要将admin这个role给其他用户，也要在对其他用户执行同样操作
SET DEFAULT ROLE 'admin'@'%'  TO 'tom'@'%';

# 方式二：修改参数
# 创建的role只要给了对应的user，对应的user就会直接继承该role的所有权限
SHOW VARIABLES LIKE 'activate_all_roles_on_login';
SET GLOBAL activate_all_roles_on_login=ON;


# 3. 从用户身上撤销角色: 将admin角色从lucy用户撤销
REVOKE 'admin'@'%' FROM 'lucy'@'%';
```

# 逻辑架构

## 1. 逻辑架构

- MySQL是C/S架构，即Client/Server，服务端是mysqld
- 客户端进程向服务器进程发送一个SQL，服务器进程处理后，向客户端进程发送处理结果

![image-20230806145924556](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230806145924556.png)

### 1.1 Connectors

- SDK(Native C, JDBC，PHP)，或者客户端工具(Navicat, DataGrip)等
- 不同语言中与SQL的交互，和MySQL建立<font color=orange>TCP连接</font>，按照其定义好的协议进行交互

### 1.2 连接层

#### 连接

![image-20230806150347795](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230806150347795.png)

- 客户端访问MySQL服务器前，首先需要建立<font color=orange>TCP</font>连接
- 经过三次握手建立连接成功后，对TCP传输过来的账户密码进行身份认证，权限获取

```bash
# 用户密码不对
- 会收到Access denied for user错误，客户端程序结束

# 用户密码正确
- 会从比权限表查出账户拥有的权限和与连接关联，之后的权限逻辑判断，都将依赖与此时读取的权限
```

#### TCP连接池

- 一个系统会和MySQL服务端进行多个连接，多个系统会和MySQL服务端连接
- TCP无限创建和频繁创建和销毁带来的资源耗尽，带来性能下降问题
- MySQL服务器中有专门的<font color=orange>TCP连接池</font>限制连接数，采用<font color=orange>长连接模式复用TCP连接</font>，解决上述问题
- 同时，一个TCP连接，也会被分配一个线程池来处理

### 1.3 服务层

#### SQL Interface

- 接收用户的SQL命令，并且返回用户需要查询的结果。比如SELECT * FROM table_name，就是调用SQL的Interface
- MySQL支持DML，DDL，存储过程，视图，触发器，自定义函数等多种SQL语言接口

#### Parser

- 对SQL语句进行<font color=orange>语法解析</font>， <font color=orange>语义解析</font>。将SQL分解成数据结果，并将这个结果传递到后续步骤
- 创建<font color=orange>语法树</font>，并根据其<font color=orange>验证该客户端是否具有执行该查询的权限</font>

#### Optimizer

- 一条查询可以有很多中执行方式，最后都返回相同的结果，优化器负责找到最好的执行计划
- 查询前，会根据语法树，确定一个<font color=orange>执行计划</font>
- 表明<font color=orange>使用哪些索引</font>，表之间的连接顺序如何。最终按照该步骤得到的执行假话，来查询结果

#### Cache&Buffer

- Query Cache用来缓存一条SELECT语句的执行结果。查询时候，如果命中缓存，则直接从缓存中返回结果
- 该查询缓存：<font color=orange>在不同客户端之间共享</font>
- MySQL5.7.20开始，不推荐使用查询缓存，并在MySQL8.0中删除

```bash
- 之前执行过的语句及结果，会通过k-v方式存储在查询缓存中
- key：该条SQL字符串(比较的是字符串，命中率很低)
- value：SQL的查询结果


# 命中率低：不是同一个SQL的场景
- 空格，注释，大小写不同
- 查询中包含系统函数，如NOW()等
- 缓存失效：缓存会检测涉及到的每个表，如果表结构或表数据发生写操作，就会失效

# 查询缓存弊大于利，缓存命中率低
```

### 1.4 引擎层

- <font color=orange>插件式存储引擎</font>：将查询处理和其他的系统任务以及数据的存储提取分离，支持自定义存储引擎
- 负责<font color=orange>数据的存储和提取，对物理服务器级别维护的底层数据执行操作</font>
- 服务器通过API和存储引擎进行通信，不同的存储引擎的功能不同

### 1.5 存储层

- 所有的数据，如表，索引等都存储在物理磁盘上

## 2. SQL执行流程

- 可以通过SQL提供的服务，查看一个SQL的具体每个步骤的执行
- 通过profiling来查看一个SQL的执行所需要的资源情况

![image-20230806154545674](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230806154545674.png)

```sql
# 1. 查看： 默认情况是关闭的,不记录sql的执行计划
SELECT @@profiling;                  # 0

SHOW VARIABLES LIKE 'profiling';     # off

# 修改为开启
SET profiling=1;


# 2. 查看执行计划
SELECT * FROM book1;

SHOW PROFILES ;                # 查看当前所有的查询操作, 得到queryId
SHOW PROFILE FOR QUERY 1 ;     # 查看当前所有的查询操作
```

```bash
+--------------------------------+----------+
| Status                         | Duration |
+--------------------------------+----------+
| starting                       | 0.000092 |
| Executing hook on transaction  | 0.000009 |
| starting                       | 0.000013 |
| checking permissions           | 0.000009 |
| Opening tables                 | 0.000040 |
| init                           | 0.000007 |
| System lock                    | 0.000011 |
| optimizing                     | 0.000006 |
| statistics                     | 0.000014 |
| preparing                      | 0.000017 |
| executing                      | 0.000054 |
| end                            | 0.000006 |
| query end                      | 0.000005 |
| waiting for handler commit     | 0.000010 |
| closing tables                 | 0.000009 |
| freeing items                  | 0.000021 |
| cleaning up                    | 0.000015 |
+--------------------------------+----------+
```

## 3. Buffer Pool

- InnoDB存储引擎是以页为单位来管理存储空间的，写操作本质上都是在对数据页进行操作
- 磁盘I/O消耗时间比较多，而在内存中进行操作，效率会提高很多
- DBMS申请<font color=orange>占用内存作为数据缓冲池</font>，在真正访问页之前，需要把磁盘上的页缓存到内存中的<font color=orange>Buffer Pool</font>之后才能访问
- 内存中的操作，相比磁盘I/O的性能提示巨大。买手机内存很重要，把磁盘中的数据加载到内存

### 作用

- 数据页存储在物理磁盘上，当需要访问某个页中的数据时，<font color=orange>会把完整的页数据加载到内存中</font>，读写完毕后，不着急把<font color=orange>该页对应的内存释放</font>，而是将其缓存起来，下次如果再请求该页数据，就可以节省磁盘IO开销

### 缓存原则

- <font color=orange>位置 * 频次</font>
- 位置决定效率，提供缓冲池就是为了在内存中可以直接访问数据
- 频次决定优先级。假如磁盘有200g，内存只有16g，缓冲池只占内存中的2g，就无法将所有数据都加载到缓冲池中
- 优先对使用频次高的热数据进行加载

### 缓冲池预读

- 局部性原理
- 假如使用了一些数据，则<font color=orange>大概率还会使用它周围页的数据</font>，采用预读机制提前加载，可以减少未来可能的磁盘IO操作

### 更新机制

- 如果对数据库中的记录进行修改后，首先修改缓冲池中页对应的记录信息，然后数据库会以<font color=orange>一定的频率</font>刷新到磁盘上
- 不是每次发生更新操作，都会立刻进行磁盘回写
- 采用<font color=orange>checkpoint的机制</font>将数据回写到磁盘上，提升数据库的整体性能

```bash
# 脏页-缓冲池中
- 缓冲池中被修改过的页，与磁盘上的数据页不一致

# 缓冲池不够用
- 需要释放掉一些不常用的页，就可以强行采用checkpoint的方式
- 将缓冲池中的脏页进行刷盘到磁盘，并从缓冲池中释放该页
```

### 大小设置

```bash
# InnoDB
# 默认是128MB:  134217728
SHOW VARIABLES LIKE 'innodb_buffer_pool_size';

# 系统变量
SET global innodb_buffer_pool_size =  134217728;
SET global innodb_buffer_pool_size =  268435456;

# 配置文件
[server]
innodb_buffer_pool_size =  268435456
```

### 多Buffer Pool实例

- Buffer Pool本质是向操作系统申请一块<font color=orange>连续的内存空间</font>，多线程环境下，访问Buffer Pool的数据都需要<font color=orange>加锁</font>处理
- Buffer Pool特别大，并且多线程并发访问高时，性能就会下降
- 把Buffer Pool <font color=orange>拆分成若干个小的Buffer Pool</font>，每个都是一个单独的实例。独立的管理各个表，提高并发能力

```sql
# InnoDB: 默认是1
SHOW VARIABLES LIKE 'innodb_buffer_pool_instances';

# 静态变量： 只能在配置文件中修改
[server]
innodb_buffer_pool_instances = 2

# Buffer Pool Instance不是越多越好
- 分别管理Buffer Pool也是需要性能开销的
- 当innodb_buffer_pool_size小于一个1G时候，innodb_buffer_pool_instances设置无效

# 每个BufferPool的实际内存
- innodb_buffer_pool_size/innodb_buffer_pool_instances
```

```bash
[server] 
innodb_buffer_pool_size = 2147483648      # 2G
innodb_buffer_pool_instances = 2          # 2实例 
```

## 4. 存储引擎

- <font color=orange>存储引擎就是表的类型</font>，以前存储引擎叫做<font color=orange>表处理器</font>
- 接收上层传下来的指令，对表中的数据进行提取或写入操作

```sql
# 查看当前版本支持的存储引擎
SHOW ENGINES;

# 修改: 全局系统变量
SET DEFAULT_STORAGE_ENGINE = MyISAM;
# 修改：配置文件
default_storage_engine=MyISAM
```

![image-20230807113933141](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230807113933141.png)

```sql
# 库级别
- 不能指定

# 表级别: 如果不指定，就会使用全局系统变量中的
CREATE TABLE goods
(
    id INT
) ENGINE = MyISAM;

# 查看
SHOW CREATE TABLE goods;

# 修改表的存储引擎
ALTER TABLE goods ENGINE = InnoDB;
```

### InnoDB vs MyISAM

- 除非有特别的原因，需要使用其他的存储引擎，否则应该优先考虑InnoDB存储引擎

|            | InnoDB | MyISAM |
| :--------- | :----- | :----- |
| 版本支持   | 默认   |        |
| 支持事务   | Y      | N      |
| 分布式事务 | Y      | N      |
| 外键       | Y      | N      |

