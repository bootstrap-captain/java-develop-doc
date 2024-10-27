# 简介

## 1. 基本概念

- <font color=orange>持久化(Persistence)：</font>把数据保存到可掉电式存储设备中，将内存中数据固化到硬盘上
- <font color=orange>持久化介质：</font>关系型数据库，非关系性数据库，磁盘文件，XML文件
- <font color=orange>DB：</font>Database, 数据结构化存储仓库，保存数据
- <font color=orange>DBMS：</font>Database Management System，数据库管理系统。（txt文件-notepad软件）
- <font color=orange>RDBMS：</font>Relational Database Management System，关系型数据管理系统
- <font color=orange>SQL：</font>Structured Query Language, 结构化查询语言, 用来和数据库通信

```bash
# RDBMS
- 数据结构归结为二维表格形式
- 包含Database, Table, Row, Column
- 不同Table对应实体，并且不同Table可以进行联查(关系模型)
- 支持复杂查询，事务

# 非RDBMS
- 可以看成RDBMS的精简版，基于k-v存储数据，不需要经过SQL层的解析，性能高
- k-v类型数据库:  Redis
- 文档类型数据库:  mongodb, aws dynomoDB, 存储xml或json
- 图形类型数据库: 
- 搜索引擎数据库： es
```

## 2. SQL

- Structured Query Language: 使用关系型模型的数据库应用语言，与数据库直接交互
- 美国国家标准局(ANSI)制定，其中<font color=orange>SQL92</font>和<font color=orange>SQL99</font>影响深远
- SQL类似普通话，不同的数据库厂商类似各地方言

### 2.1 分类

```bash
# DDL: Data Definition Languages
- 数据定义语言
- CREATE   ALTER   DROP   RENAME  TRUNCATE
- 定义不同的库，表，视图，索引等数据库对象

# DML:Data Manipulation Language
- 数据操作语言
- INSERT   DELETE   UPDATE  SELECT(重要)
- 添加，删除，更新，查询数据库记录
- SELECT 是SQL的基础，最为重要

# DCL: Data Control Language
- 数据控制语言
- GRANT,  REVOKE, COMMIT, ROLLBACK,  SAVEPOINT
- 定义库，表，字段，用户的访问权限和安全级别
```

### 2.2 规则

- SQL结束用 ；， 同一个SQL必要时候可以换行
- 同一行输入多条指令，用  ； 隔开即可，多条sql可以一起执行

```sql
# \G 作为sql语句结束符，也可以使用 \G来结束，查询结果垂直显示
--  如果输入的语句比较短，则 \G没有什么意义，如果较长，则方便查看；
--   只在mysql自带的cli中生效
SELECT user(),now(),version()\G

# \c : 输入前面的较长的sql，如果不想执行了，则 \c 来取消当前sql的执行
SELECT now(),version(),user()\c

SELECT now();SELECT version();SELECT user();
```

```bash
SELECT now();   # 当前mysql的时间
SELECT user();  # 当前账户的用户名，返回当前用户@服务端的内网ip   root@117.35.135.138  
SELECT version(); # 
```

# 数据类型

```sql
整数类型：        TINYINT    SMALLINT  MEDIUMINT   INT    INTEGER   BIGINT
浮点类型：        FLOAT      DOUBLE
定点数类型：      DECIMAL
位类型：          BIT
日期类型：        YEAR  TIME    DATE   DATETIME    TIMESTAMP
文本字符串类型：   CHAR  VARCHAR TINYTEXT  TEXT  MEDIUMTEXT  LONGTEXT
枚举类型：        ENUM
集合类型          SET
二进制字符串：     BINARY   VARBINARY  TINYBLOB   BLOB   MEDIUMBLOB     LONGBLOB
JSON类型：       JSON对象    JSON数组
```

## 1. 整数类型

- 无符号类型：加上UNSIGNED
- 一个字节8位
- 建议使用大点的数据，空间换系统稳定

![img](https://cdn.nlark.com/yuque/0/2022/png/26882770/1653059227660-165272c2-5e12-493c-af5c-5d19d1e58e3e.png)

```sql
-- 应用场景
TINYINT:          一般用于枚举类型
SMALLINT:         较小范围的统计数据
MEDIUMINT:        较大整数的计算
INT INTEGER：     取值范围足够大，一般就用这个
BIGINT:           只有处理特别巨大的整数才会用到
```

## 2. 浮点类型

| 类型   | desc         | 字节 | 取值范围 |
| ------ | ------------ | ---- | -------- |
| float  | 单精度浮点数 | 4    | 小       |
| double | 双精度浮点数 | 8    | 大       |

```sql
# UNSIGNED: 并不会改变正数数据的范围，因为符号位也占用空间，因此没必要显示声明UNSIGNED 
# double(m,n), float(m,n), 
- m代表总的数字长度：  则整数长度为m-n，小数长度为n
- 如果存入的小数位的精度超过了指定的精度，小数位会四舍五入
- 如果整数位超了，则报错

# 可能存在精确度丢失的问题
```

## 3. 定点类型

```bash
# DECIMAL(5,2): -999.99~999.99
-- 底层是用字符串存储
-- 不指定的默认的是（10，0)
-- 超过精度的话，进行四舍五入
-- 金融类的推荐用Decimal,一分钱都不能差
```

## 4. 日期时间

- 添加格式为字符串或者数字，日期函数，会自动转换
- <font color=orange>DATETIME</font>: 插入的数据和读取的数据一样， 建议使用
- TIMESTAMP: 会根据时区变化， 底层存储的是毫秒数，查询日期时间差比较快

```bash
# 日期类型
- 开发中建议使用DATETIME

# 注册时间，商品发布等
- 建议使用 时间戳， 因为DATETIME不方便计算
- SELECT UNIX_TIMESTAMP();
```

![img](https://cdn.nlark.com/yuque/0/2022/png/26882770/1653063414547-dd2c129f-65a0-4621-9963-788b5f6e4974.png) 

## 5. 文本类型

![img](https://cdn.nlark.com/yuque/0/2022/png/26882770/1653065021718-15f76e1c-919e-4069-96e0-708a1c239bf9.png)

### 5.1char/varchar

| 字符串类型 | 特点     | 长度范围字节 | 实际存储空间字符 | 空间 | 时间 | 场景                 |
| ---------- | -------- | ------------ | ---------------- | ---- | ---- | -------------------- |
| CHAR(M)    | 固定长度 | 0--255       | m                | 浪费 | 高   | 存储不大，速度要求高 |
| VARCHAR(M) | 可变长度 | 0～65535     | 实际长度+1       | 节省 | 低   | 非char的情况         |

```bash
# char
- 需预定义长度，默认1个字符
- 数据如果比声明的长度小，则在右侧填充空格达到指定长度
- 检索时，char类型会自动去除尾部空格
- 门牌号，uuid，变更频繁的column

# varchar
- 必须指定长度
- 插入时，会根据实际占用长度，一个字节来保存字符， 因此为 实际长度+1
- 检索时，保留数据尾部的空格


# 因为影响性能的主要因素是存储总量，推荐varchar
```

### 5.2 text

- 主要用来存储大文本，比如评论，博客记录等
- 频繁使用的表，不建议使用text，可以把该字段单独分表出去

### 5.3 enum/set

- 插入时候只能插入一个值

```sql
ADD season ENUM ('春', '夏', '秋', '冬') null;
ADD season SET ('春', '夏', '秋', '冬') null;
```

## 6. 二进制字符串

- 存储图片，音频等数据
- 一般都是用S3/OSS等存储资源，mysql中保留对应的url

![img](https://cdn.nlark.com/yuque/0/2022/png/26882770/1653098685735-d7788dcd-3b3b-4483-9021-d2e94ac47e25.png)

![img](https://cdn.nlark.com/yuque/0/2022/png/26882770/1653098878471-44b03851-62b2-47a0-a1c7-f4c6def68273.png)

## 开发建议

```bash
# 整数类型，就用int

# 小数类型是DECIMAL，禁止使用float和double
- 如果超过范围，可以将decimal拆成整数和小数分开存储

# 如果字段是非负数，必须是UNSIGNED

# 日期时间类型： datatime

# 字符串类型
- 如果为定长，则使用char
- 如果为可变，则用varchar，长度不要超过5000，否则就使用text，并独立出一张表，用主键来对应
```

# 库表操作

## 1. 库

```sql
SHOW DATABASES;          -- 查看当前用户账户下所有库
SELECT DATABASE();       -- 查看当前使用的库，  一般当前为null
USE lucycat;             -- 切换库, 每次开启新的会话，当前库是null

-- 创建库
-- 默认为utf8mb4，判重是用库名来判断， 数据库名不能改
CREATE DATABASE  shuzhan_db;
CREATE DATABASE IF NOT EXISTS shuzhan_db;

-- 查看库信息
SHOW CREATE DATABASE shuzhan_db;

-- 删除库
DROP DATABASE  my_db;
DROP DATABASE IF EXISTS my_db;

-- 字符集
CREATE DATABASE my_db CHARACTER SET 'utf8mb4';        -- 创建时
ALTER DATABASE shuzhan_db CHARACTER SET 'utf8mb4';    -- 修改库的字符集
```

## 2. 表

- 表操作，要么库名.表名，要么在sql脚本前面指定用那个库

### 2.1 创建

```sql
-- 1. 列出当前数据库所有表
SHOW TABLES;
SHOW TABLES  FROM shuzhan_db;

-- 2. 创建普通表
CREATE TABLE people (name char(20),weight INT,sex ENUM('F','M'));
CREATE TABLE IF NOT EXISTS  people (name char(20),weight INT,sex ENUM('F','M'));

CREATE TABLE `people` (
  `name` char(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `weight` int(11) DEFAULT NULL,
  `sex` enum('F','M') COLLATE utf8mb4_unicode_ci DEFAULT NULL
) CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2.2 描述

```sql
-- 1. 描述一个表
DESC/DESCRIBE/EXPLAIN people;
SHOW COLUMNS/FIELDS FROM people;

DESC/DESCRIBE/EXPLAIN president '%name';       -- 只描述带有name的字段
SHOW COLUMNS/FIELDS FROM president LIKE '%name';  

-- 2.  查看行数据的详细信息
SHOW FULL FIELDS/COLUMNS FROM people;

-- 3. 查看表
SHOW CREATE TABLE people;
```

### 2.3 修改

```sql
-- 1. 添加field
ALTER TABLE employee ADD 'status' boolean;                         # 默认添加到最后
ALTER TABLE employee ADD home varchar(50) FIRST;
ALTER TABLE employee ADD hobby varchar(50) AFTER home;

-- 2. 修改field
ALTER TABLE employee MODIFY home VARCHAR(100);
ALTER TABLE employee MODIFY status BOOLEAN DEFAULT 1;

-- 3. 重命名field
ALTER TABLE employee CHANGE 'status' is_enabled BOOLEAN;
ALTER TABLE employee CHANGE home myhome VARCHAR(200);                   # 重命名字段名，并且修改长度
 
-- 4. 删除field
ALTER TABLE employee DROP is_enabled;
ALTER TABLE employee DROP COLUMN is_enabled;
  
-- 5.重命名table
RENAME TABLE employee TO employees;
ALTER TABLE  employees RENAME TO employee;
 
-- 6. 修改table存储引擎
ALTER TABLE people ENGINE = MyISAM;
```

### 2.4 删除

```sql
-- 1. 删除表： 删除表结构和表数据，释放表空间
--    不能回滚
DROP TABLE cnip;
DROP TABLE cnip,phone;
DROP TABLE IF EXISTS cnip;

-- 2. 清空表: 表数据清除，表结构保留
TRUNCATE TABLE people;     
DELETE FROM people;        

# TRUNCATE:   
- 速度较快，且使用的系统和事务日志资源少
- 不支持rollback， 类似其他DDL
- 无事务且不触发TRIGGER，有可能造成事故，因此不建议使用
- DDL执行完后，必然就会自动提交

# DELETE: 
- 可以部分删除
- 可以利用事务进行rollback
```

### 2.5 复制

```sql
# 复制时候，SELECT 结构可以比较复杂，同时也可以对其中的字段其别名，作为新表的字段名
-- 1. 复制表结构和数据
CREATE TABLE us_phone AS SELECT * FROM phone;

-- 2. 原表的部分复制
CREATE TABLE us_apple_phone AS SELECT * FROM phone WHERE brand = 'Apple';  

-- 3. 原表的结构复制
CREATE TABLE us_apple_phone AS SELECT * FROM phone WHERE 1=2; 
CREATE TABLE cn_phone LIKE phone;
```

### 2.6 分区表

- 大数据量表，读写操作效率低，将一个大表的行数据，分配到不同的表

```sql
-- 1.按照时间分区
-- 在var/lib/mysql/库名 目录下，会创建 phone#P#p1.ibd类似的数据保存数据文件
CREATE TABLE phone (id INT,name CHAR(20),created_time DATETIME NOT NULL)

PARTITION BY RANGE(YEAR(created_time))

(PARTITION p0 VALUES LESS THAN (2010),
PARTITION p1 VALUES LESS THAN (2011),
PARTITION p2 VALUES LESS THAN (2012),
PARTITION p3 VALUES LESS THAN (2013),
PARTITION pmax VALUES LESS THAN MAXVALUE);

--2. 如果后续数据都超过了2013，则可以对pmax分区进行再分区
ALTER TABLE phone REORGANIZE PARTITION pmax
INTO(
PARTITION p4 VALUES LESS THAN(2014),
PARTITION p5 VALUES LESS THAN(2015),
PARTITION pmax VALUES LESS THAN MAXVALUE
)
```

## 3. 行

### 3.1 插入

```sql
-- 全列插入：必须包含所有的列数据。不推荐这种方式
-- 批量快
INSERT INTO president VALUES (1,"shu","zhan");
INSERT INTO president VALUES (2,"wu","wang"),(3,"si","li");

-- 指定列名插入， 对于自增的键，如果赋值就取赋值的，不赋值则取默认的
INSERT INTO president (last_name,first_name,id) VALUES ("八","王",10);
INSERT INTO president (last_name,first_name,id) VALUES ("六","赵",5),("七","田",6);

-- set赋值方法：主键就没有给值，采取默认的即可
INSERT INTO president SET first_name = "朱", last_name = "院长";

-- 查询结果插入数据
INSERT INTO phone(name, created_time)
SELECT name, created_time
FROM phone;
```

### 3.2 删除/更新

```sql
-- 1. 删除: 删除所有数据
DELETE FROM cnip;

-- 指定删除： 一般删除时，必须加where子句，避免误删
DELETE FROM cnip WHERE name = 'zhangsan';


-- 2. 更新操作: 更新所有数据的name
UPDATE cnip SET name = 'sz';
-- 指定更新：更新时候，一般必须加where子句，避免误更新
UPDATE cnip SET name = 'sz' WHERE id = 4;
-- 指定更新多列数据
UPDATE cnip SET name = 'sz', color = 'red' WHERE id = 4;
-- 更新某列的值为NULL
UPDATE cnip SET name = NULL where id = 4;
```

# 单表查询

## 1.基本查询

### 1.1 基本/别名/去重/拼接/着重号/合并

```sql
-- 1. 基本使用
SELECT * FROM cnip; 
SELECT name,age FROM cnip;
SELECT 100;  SELECT 'mick';                         -- 常量,可以给每行添加一个对应的字段    
SELECT (2+2);                                       -- 表达式
SELECT 1+1 FROM DUAL;                               -- 伪表
SELECT VERSION();                                   -- 函数
SELECT * FROM cnip_db.cnip WHERE cnip.name = '李明'; -- 全限定表名，列名


-- 2.别名：
-- 改变结果集中字段显示名称；    多表联查，重复字段进行区分
SELECT VERSION() AS version;              -- 建议加AS
SELECT VERSION() version;                 -- 可以不写
SELECT VERSION() 'my version';            -- 别名中存在空格，用或“”

-- 列的别名，只能在order by后使用
SELECT *, salary * 10 AS allsalary FROM book1 ORDER BY allsalary;  # 正常
SELECT *, salary * 10 AS allsalary FROM book1 WHERE allsalary > 10;      # 报错


-- 3. 去重：select后单独使用
SELECT DISTINCT name FROM phone;           -- 获取不重复的某个字段的value
SELECT DISTINCT name,age FROM cnip;        -- 用于后面多列，即行中name和age都重复了，才去重
# SELECT name,DISTINCT age FROM cnip;        -- error：因为表中数据对不上了


-- 4. 拼接： 不能用 + ，     sql中 + 只能当作运算符
SELECT CONCAT('姓名:',first_name,'---',last_name) AS name FROM president;

-- 5. 着重号： 如果表，字段名和mysql关键字重复了，可以使用着重号
SELECT `order` FROM people;

-- 6. UNION   UNION ALL
-- UNION 会进行去重， 但是去重操作会耗费时间
-- UNION ALL: 存在重复的数据，但效率高，推荐
SELECT * FROM cnip where name='erick'

UNION

SELECT * FROM cnip where name='tom';
```

### 1.2. 计算运算符

```sql
--       +  -  *  /(DIV)  %(MOD)
SELECT 100 + 99;
SELECT '100' + 99;  -- 隐式转换：尝试将字符转换为数字并做运算，如果转换不成功则将字符视为0
SELECT 'dfa' + 99;
SELECT 100 DIV 20;   -- 5
```

## 2. 比较运算符

### 2.1 基本比较

```sql
##  = 返回的是0或者1     {！=,   < ,   >,    <=,   >= } 

-- 两边都是数字，则比较大小，
SELECT 1 = 1, 1 = 2；

-- 一个数字，一个字符串：字符串隐式转换，转换不成功则字符串看作0
SELECT 1 = '1', 1 = 'a', 0 = 'a';    # 1,0,1

-- 两边都是字符串，则按照ANSI的比较规则
SELECT 'A' = 'A', 'Ab' = 'AB', 'a' = 'b'; -- 1 1 0
```

### 2.2 NULL值

```sql
-- 1. 只要有NULL参与运算，则结果都是null
SELECT 1 = NULL, NULL = NULL, 12*NULL;
SELECT * FROM cnip WHERE name=null;             # 不会返回一条记录, 因为只返回比较结果为1的数据


-- 2. <=>:  安全等于，null不参与时和=没区别， 为null而生
-- 两边都是null时为1，只有一个null时为0
SELECT NULL <=> NULL, 1 <=> NULL;
SELECT * FROM cnip WHERE name<=>NULL;           -- 会返回对应name为null的记录


-- 3. 非空判断
-- IS NULL         IS NOT NULL      和<=>相同
SELECT * FROM phone WHERE name IS NULL;
SELECT * FROM phone WHERE name IS NOT NULL;


-- ***** null参与运算：  null不等同于0，‘’，‘null’
SELECT null + 10;   --只要其中一个为null，则结果为null

-- 可以通过流程函数来控制: 如果为null，则转换为0
SELECT IFNULL(salary,0) * 10
```

### 2.3 高级比较

```sql
-- 匹配时： 字符的一定要用‘’,     数值类型，不用

-- 1. 按照字典来比较
-- LEAST()     GREATEST()   
SELECT LEAST('A', 'B', 'F', 'Z', 'G');
SELECT GREATEST('A', 'B', 'F', 'Z', 'G');
SELECT LEAST('erick', 'edjk', 'adf');                              -- 首先按照第一个字母比较，以此类推
SELECT LEAST(LENGTH('erick'), LENGTH('dfg'), LENGTH('dgadfaf'));   -- 比较长度


-- 2. 范围查找
-- BETWEEN(下限) AND(上限)      NOT BETWEEN AND
SELECT * FROM phone WHERE age BETWEEN 20 and 100;        --  包含临界值
SELECT * FROM phone WHERE age NOT BETWEEN 32 AND 100;


-- 3. 离散查找
-- IN     NOT IN
SELECT * FROM phone WHERE name in ('tony','jack','lucy');


-- 4. 模糊查询
-- % 代表0-n个字符，  - 只匹配一个字符；
-- 不能匹配null值
-- 英文匹配时候不区分大小写；
SELECT * FROM cnip WHERE content LIKE '%zte%';
SELECT * FROM cnip WHERE content LIKE 'z%e';
SELECT * FROM cnip WHERE content LIKE '_te';
SELECT * FROM cnip WHERE content LIKE '__te';
SELECT * FROM phone WHERE  name like '%\_%';   -- 转义符号   \  查询包含 _ 的 字符
SELECT * FROM phone WHERE name like '%^_%' ESCAPE '^';   -- 自定义转义符号为 ^
```

## 3. 逻辑运算符

```sql
-- 逻辑运算符
--   OR ｜｜     AND &&      NOT !
SELECT * FROM people WHERE name = 'erick' || name = 'eri';
SELECT * FROM people WHERE name = 'erick' AND name = 'eri';
SELECT * FROM book1 WHERE NOT id=1;
```

## 4. 排序

```sql
# 不显示指定时，按照数据添加顺序
SELECT * FROM people;
	
-- 默认升序/ASC   DESC/降序
SELECT * FROM people ORDER BY name DESC;

-- 多字段(二级排序)： 先按第一个字段排序，第一个字段相同再按第二个排序
SELECT * FROM president ORDER BY first_name DESC, last_name DESC;

-- 区分null值的排序： 某个字段含null值的排序
-- 先按照该字段的null和非null排序，再按照字段具体内容排序
SELECT * FROM president ORDER BY IF(address IS NULL,0,1) DESC, address DESC;
```

## 5.  分页

```sql
-- 前10条
SELECT * FROM cnip LIMIT 10;

-- 跳过前面5个，检索10个
SELECT * FROM cnip LIMIT 5,10;

-- 跳过前面5个，检索2个
SELECT * FROM cnip LIMIT 10 OFFSET 5;
```

# 多表联查

## 1. 笛卡尔积

- 两个集合X和Y的全排列组合
- A表包含n条数据，B表包含m条数据，则笛卡尔积为n*m条数据

```sql
SELECT * FROM people,phone;                     # A,B
SELECT * FROM people CROSS JOIN phone;          # A CROSS JOIN B
SELECT * FROM people INNER JOIN phone;          # A INNER JOIN B
SELECT * FROM people JOIN phone;                # A JOIN B

-- 加上表别名：表中的字段加上表别名，避免不同表字段重复，同时sql优化提升效率
SELECT peo.name,peo.`order`, pho.name FROM people AS peo,phone AS pho;
```

## 2. 连接条件

- 两个表在笛卡尔积的条件上，增加一个条件

### 2.1 等值链接/非等值链接

```sql
-- 1 等值链接，  笛卡尔积的各种join都适用
SELECT * FROM student stu,president pre WHERE stu.id = pre.id;

-- 2. 非等值链接
SELECT emp.first_name, sal.grade
FROM employee AS emp,
     salary_level AS sal
WHERE emp.salary > sal.low_level
  AND emp.salary < sal.high_level;
```

### 2.2 自链接/非自链接

```sql
-- 1. 自链接
SELECT emp.employ_id, emp.first_name, manage.employ_id, manage.first_name
FROM employee AS emp,
     employee AS manage
WHERE emp.manager_id = manage.employ_id;

-- 2. 非自链接
```

## 3. 内连接/外连接

- SQL99 标准
- 采用(A表)   JOIN (B表)  ON 

### 3.1 内连接

- 合并具有同一列的两个以上的表的行，结果集中不包含一个表与另一个表不匹配的行
- 笛卡尔积加上对应的条件过滤得到的数据

```sql
# 条件过滤
-- sql 92标准：  where
SELECT * FROM student stu,president pre WHERE stu.id = pre.id;

-- sql 99标准：   on
SELECT * FROM employee JOIN salary_level ON employ_id=salary_level.high_level;
SELECT * FROM employee INNER JOIN salary_level ON employ_id=salary_level.high_level;
```

### 3.2 外连接

- 除了应该有的结果集，还包含不匹配的比如null的结果集
- 一般情况下，考虑到连接部分的字段可能为null，因此查询所有的记录时，就应该直接使用外连接
- <font color=orange>sql92不支持外连接，sql99支持</font>

#### LEFT OUTER JOIN

- 在笛卡尔积的基础上，进行条件过滤后
- 再加上左表中剩下的数据，右边数据用null表示

```sql
-- A LEFT OUTTER JOIN B           A LEFT JOIN B
-- OUTTER可以省略
SELECT * FROM student stu LEFT JOIN president pre on stu.id = pre.id;
SELECT * FROM student stu LEFT OUTTER JOIN president pre on stu.id = pre.id;
```

![img](https://img-blog.csdnimg.cn/20200816173507406.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzM3NDU3OA==,size_16,color_FFFFFF,t_70#pic_center)

#### RIGHT OUTER JOIN

```sql
-- A RIGHT OUTTER JOIN B           A RIGHT JOIN B
-- OUTTER可以省略
SELECT * FROM student stu RIGHT JOIN president pre on stu.id = pre.id;
SELECT * FROM student stu RIGHT OUTTER JOIN president pre on stu.id = pre.id
```

![img](https://img-blog.csdnimg.cn/20200816173812906.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzM3NDU3OA==,size_16,color_FFFFFF,t_70#pic_center)

#### FULL OUTER JOIN

- 满外链接
- 再加上左表中剩下的数据，右边数据用null表示
- 再加上右表中剩下的数据，左边数据用null表示 
- mysql不支持这种写法，但是可以通过UNION来实现

```sql
SELECT * FROM student stu LEFT JOIN president pre on stu.id = pre.id

UNION

SELECT * FROM student stu RIGHT JOIN president pre on stu.id = pre.id;
```

![img](https://img-blog.csdnimg.cn/20200816174323369.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzM3NDU3OA==,size_16,color_FFFFFF,t_70#pic_center)

## 4. SQL99新特性

### 4.1 USING子句

```sql
-- 如果两个表中的on条件的列名相同，则可以用using来简化sql
SELECT * FROM student stu RIGHT JOIN president pre USING(id);
```

### 4.2  NATURAL

- 查询两个表中**所有相同**的字段，然后进行等值连接
- 两个表中只有一列的表字段相同，则用NATURAL  JOIN简化sql
- 相同的列只会展示一个

```sql
-- 左连接，右连接，内连接
-- NATURAL LEFT JOIN, NATURAL RIGHT JOIN, NATURAL  JOIN

SELECT * FROM student stu NATURAL LEFT JOIN president pre;
```

![img](https://img-blog.csdnimg.cn/20200816180642864.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzM3NDU3OA==,size_16,color_FFFFFF,t_70#pic_center)

# 单行函数

## 1. 数值函数

| 函数                    | DESC                                                         |
| ----------------------- | ------------------------------------------------------------ |
| ABS(1)                  | 绝对值                                                       |
| SIGN(X)                 | 返回x的符号，正数返回1，负数返回-1，0返回0                   |
| PI()                    | 圆周率                                                       |
| CEIL(1.5)  CEILING(1.5) | 向上取整  >= 该参数的最小整数                                |
| FLOOR(1.5)              | 向下取整                                                     |
| LEAST(e1,e2,e3)         | 返回最小的                                                   |
| GREATEST(e1,e2,e3)      | 返回最大的                                                   |
| MOD(x,y)                | 返回x除以y后的余数                                           |
| RAND()                  | 0-1 的随机数,可以乘法的到对应的值                            |
| RAND(X)                 | 0-1 的随机数，如果X因子一样，产生的随机数相同(伪随机，可以以时间戳为种子) |
| ROUND(1.5)              | 四舍五入                                                     |
| ROUND(5.32, 1)          | 四舍五入，保留1位小数 5.3                                    |
| TRUNCATE(1.345,2)       | 取小数点后几位  1.34                                         |
| SQRT(2)                 | 返回平方根，x值为负，返回NULL                                |

## 2. 字符串函数

| 函数                               | DESC                                |
| ---------------------------------- | ----------------------------------- |
| LENGTH(name)                       | 字节长度                            |
| CONCAT(name,'====',brand)          | 字符拼接                            |
| CONCAT_WS('--','beijing','shanxi') | 指定拼接符号拼接                    |
| REPLACE('aaaa','a','bv')           | 用bv替换a                           |
| UPPER(name)                        | 大写转换                            |
| LOWER(name)                        | 小写转换                            |
| LEFT(str,3)， RIGHT(str,2)         | 取字符串左边3个, 最多取当前完整字符 |
| TRIM()   LTRIM().  RTRIM()         | 去除空格                            |

```sql
SELECT CONCAT( LOWER(name), '===',UPPER(brand)) from phone; -- 函数嵌套
SELECT LENGTH('hello'),LENGTH('我们') FROM DUAL;             -- 5, 6, utf8中，1个英文1个字节，1个汉字3个字节
SELECT CONCAT_WS('--','beijing','shanxi') FROM dual;        -- beijing--shanxi
```

## 3. 日期函数

### 3.1 简单日期

| 函数                                                         | Value               | DESC         |
| ------------------------------------------------------------ | ------------------- | ------------ |
| CURDATE()   /   CURRENT_DATE()                               | 2023-08-28          |              |
| CURTIME()   /   CURRENT_TIME()                               | 16:50:01            |              |
| NOW() / SYSDATE() /  CURRENT_TIMESTAMP() /  LOCALTIME() / LOCALTIMESTAMP() | 2023-08-28 16:52:02 |              |
| UTC_DATE()                                                   | 2023-08-28          | 世界标准时间 |
| UTC_TIME()                                                   | 09:36:29            |              |
| UTC_TIMESTAMP()                                              | 2023-08-28 09:37:08 |              |

### 3.2 常见日期

```sql
-- 1. 获取年月日周天
YEAR(NOW()), MONTH(NOW()),  DAY(NOW());  -- 年月日,参数date
MONTHNAME(NOW()),WEEKDAY(NOW()),QUARTER(NOW()); 
WEEK(NOW()),WEEKDAY(NOW()),WEEKOFYEAR(NOW());
DAYOFMONTH(NOW()),DAYOFYEAR(NOW()),DAYOFWEEK(NOW());

HOUR(CURRENT_TIME), MINUTE(CURRENT_TIME),SECOND(CURRENT_TIME); -- 时分秒，参数time


-- 2. 日期操作函数 EXTRACT(type from date)
SELECT EXTRACT(SECOND FROM NOW());
SELECT EXTRACT(MINUTE_MICROSECOND FROM NOW());

-- 3. 日期与时间戳转换
SELECT UNIX_TIMESTAMP();        -- 以UNIX时间戳的形式返回当前时间 1652841099
SELECT UNIX_TIMESTAMP(NOW());   -- 将Data转换为UNIX时间戳
SELECT UNIX_TIMESTAMP('2021-09-01: 10:24');

SELECT FROM_UNIXTIME(1652841243); -- 将UNIX时间戳转换普通格式时间 2022-05-18 10:34:03

-- 4. 时间和秒转换
SELECT TIME_TO_SEC(CURRENT_TIME); -- time参数， 小时*3600+分钟*60
SELECT SEC_TO_TIME(39421);        -- seconds参数


-- 5. 修改时间
SELECT DATE_ADD(NOW(), INTERVAL 1 YEAR);   -- 加的具体的值可为负数
SELECT ADDDATE(NOW(), INTERVAL 1 YEAR);
SELECT DATE_SUB(NOW(), INTERVAL 1 MONTH);
SELECT SUBDATE(NOW(), INTERVAL 1 MONTH);
SELECT DATE_ADD(NOW(), INTERVAL '1_1' YEAR_MONTH ); -- 同时加年和月
SELECT DATEDIFF(NOW(),SUBDATE(NOW(),INTERVAL 1 YEAR)); -- 天数
```

### 3.3 字符串和日期转换

#### 指定格式转换

```sql
-- 日期转换为字符串，按照指定格式
DATE_FORMAT(date, fmt);
SELECT DATE_FORMAT(NOW(),'%Y-%m-%d');    -- 2022-05-18
SELECT DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s'); -- 2022-05-18 11:25:11

-- 字符串转为日期
-- 字符串和对应的格式需要匹配，不然会报错
STR_TO_DATE(str, fmt)
SELECT STR_TO_DATE('2021-09-05 12:38:16', '%Y-%m-%d %H:%i:%s');
```

```sql
%Y -- 4位数年份
%y -- 2位数年份
%M -- 月名表示月份 January
%m -- 两位数表示月份 01，02
%b -- 缩写的月名  Jan Feb
%c -- 数字表示月份  1，2，11

%D -- 英文后缀月中的天数  1st
%d -- 2位数表示月中的天数 01，22
%e -- 数字表示月中天数。 1，2，3

%H -- 2位数字表示小时   24小时制
%h -- 2位数表示小时,12小时制
%k -- 数字形式的小时， 1，12

%i -- 2位数字表示分钟 00，01
%S %s -- 2位数字表示秒
```

#### 嵌套转换

- 上面的具体的格式，比较难记，可以利用mysql提供的嵌套函数来实现

```sql
SELECT STR_TO_DATE('2021-09-05 12:38:16', GET_FORMAT(DATETIME ,'ISO'));
```

![image-20230828174930198](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230828174930198.png)

## 4. 流程控制函数

### 4.1 三元运算符

```sql
-- IF(condition,result1, result2)
-- 条件中可以用 = <= >=  is null等判断
SELECT IF( 1=1,'positive','negative' ) as result;  


-- IFNULL(value,0): 如果为空，则给0值，否则就用原来的值
SELECT IFNULL(name,0) FROM phone;
```

### 4.2 CASE WHEN

```sql
-- 类似switch，是用在from之前的，对于某些字段进行计算
SELECT *,
       CASE brand
           WHEN 'Apple' THEN price * 0.9
           WHEN 'Huawei' THEN price * 0.8
           WHEN 'XiaoMi' THEN price * 0.7
           WHEN 'Oppo' THEN price * 0.6
           ELSE price
           END 
           AS discount_price
FROM phone;

-- 2. 类似多重if，  适用于区间比较。  case 后没有字段
SELECT *,
       CASE
           WHEN price >= 5000 THEN 'LEVEL-A'
           WHEN price >= 4000 THEN 'LEVEL-B'
           WHEN price >= 3000 THEN 'LEVEL-C'
           WHEN price >= 2000 THEN 'LEVEL-D'
           WHEN price >= 1000 THEN 'LEVEL-E'
           ELSE 'LEVEL-LOW'
           END 
           AS product_level
FROM phone;
```

# 聚合函数

- 聚合函数不能嵌套使用，必须使用子查询

## 1. 常用函数

- 聚合函数统计时，都会忽略null值。分子和分母都会忽略该行

```sql
-- SUM()  AVG()：               参数必须是数值类型 

-- MAX()  MIN()：               参数为任何类型

SELECT MAX(age) from phone;
SELECT SUM(DISTINCT(brand)) FROM phone;  -- 和distinct搭配
```

```sql
-- COUNT():                    参数为任何类型

-- COUNT(*)    COUNT(field)    COUNT(1) 
-- COUNT(*)，COUNT(1)：    没区别，统计不为null的行数目， 一行中只要一个字段不为null，总数就会计算该行
-- COUNT(field):           如果某个字段不为null，就统计

-- 性能： 
INNODB:    COUNT(*)=COUNT(1) > COUNT(filed)
MYISAM:    三个都一样， 引擎内部有计数器来维护行数
```

## 2.  GROUP BY

- 只能结合聚合函数使用
- select中的非聚合函数的字段，一定要出现在group by后面

```sql
-- 单列分组
SELECT AVG(salary), SUM(salary), nation
FROM employee
GROUP BY nation;

-- 多列分组: 先按照a字段分组，再根据b字段分组，统计最小组的数据
-- 字段前后无区别,因为from前面的是聚合函数
SELECT nation, address, SUM(salary)
FROM employee
GROUP BY nation, address;
```

## 3. HAVING

### 3.1 基本使用

- 过滤条件中如果使用了聚合函数，则必须使用HAVING来替换WHERE

```sql
-- HAVING不能单独使用，必须结合GROUP BY
-- HAVING必须声明在GROUP BY后面
SELECT nation, MAX(salary)
FROM employee
GROUP BY nation
HAVING MAX(salary) > 20000;
```

### 3.2 HAVING/WHERE

- 过滤条件中有聚合函数的，则过滤条件必须声明在HAVING中
- 过滤条件不是聚合函数的，则过滤条件声明在HAVING和WHERE都可，但是建议声明在WHERE中

```sql
-- 推荐，where, 实现效率高
SELECT address, MAX(salary)
FROM employee
WHERE address = '上海'
GROUP BY address
HAVING MAX(salary) > 10;

SELECT address, MAX(salary)
FROM employee
GROUP BY address
HAVING MAX(salary) > 10
   AND address = '上海';  
```

## 4. 执行过程

```sql
-- 书写顺序： 
SELECT>>>FROM>>>WHERE>>>GROUP BY>>>HAVING>>>ORDER BY>>>LIMIT

-- 执行顺序：
FROM-> WHERE->GROUP BY->HAVING->SELECT字段->DINSTINCT->ORDER BY ->LIMIT

-- 尽可能每次执行，都最多的去过滤掉数据
```

# 子查询

- 一个查询语句嵌套在另一个查询语句内部的查询
- 子查询先执行，主查询后执行。子查询结果要传递给主查询
- 单行子查询对应单行子查询，多行子查询对应多行子查询

## 1. 单行/多行

### 1.1 单行子查询

- 单行子查询，返回的就是一个单字段值

```sql
# 单行操作符： 通过 =    >    >=     <    <=      <>(不等于)来比较

# 先写子查询，再写外查询
SELECT name, salary
FROM employee
WHERE salary > (SELECT salary FROM employee WHERE name = 'tom');


# 可以包含多个子查询
SELECT id, name, salary
FROM employee
WHERE salary > (SELECT salary FROM employee WHERE name = 'lucy')
  AND id > (SELECT id FROM employee WHERE name = 'jack');
  

# 配合聚合函数
SELECT id, name, salary
FROM employee
WHERE salary > (SELECT MIN(salary) FROM employee)

# 空值问题
-- 子查询如果没有记录，则外查询匹配时，也就没有对应的数据
-- 假如没有tom这个人，则比较运算符比较时，条件永远都是false
SELECT name, salary
FROM employee
WHERE salary > (SELECT salary FROM employee WHERE name = 'tom');


-- FROM中也可以跟子查询
-- 聚合函数不能嵌套
SELECT *
FROM employee
WHERE salary > (SELECT MIN(avg_salary)
                FROM (SELECT AVG(salary) AS avg_salary
                      FROM employee
                      GROUP BY id) AS temp_table)
```

### 1.2 多行子查询

```sql
-- 多行子查询： 内查询返回多条数据
-- IN: 列表中任意一个
-- ANY: 需要和单行操作符一起使用，和字查询返回的某一个值比较
-- ALL/SOME: 需要和单行操作符一起使用，和字查询返回的所有值比较

SELECT *
FROM employee
WHERE salary in (SELECT salary FROM employee);

-- 比任一的都小就可以查
SELECT *
FROM employee
WHERE salary < ANY (SELECT salary FROM employee WHERE address = '上海');

-- 比所有的小才行
SELECT *
FROM employee
WHERE salary < ALL (SELECT salary FROM employee WHERE address = '上海');
```

## 2. 相关/不相关

# 约束

```sql
-- 查看
SELECT * FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_NAME='employee';

# 单列约束 vs 多列约束
# 列级约束：  将约束声明在对应字段的后面
# 表级约束：  在表中所有字段声明后，在后面声明约束，  一般表级约束的目的就是为了给多列加
```

| 约束类型       | 表/列约束 | 单/多列约束 | 个数     | 约束名             | 索引关联 | Other                |
| -------------- | --------- | ----------- | -------- | ------------------ | -------- | -------------------- |
| NOT NULL       | 列级约束  | 单列        | 多个     | NA                 | NA       |                      |
| UNIQUE         | 表/列约束 | 单列/多列   | 多个     | 默认列名，可自定义 | 唯一索引 | 允许列值为多个null   |
| PRIMARY KEY    | 单列/多列 | 单列/多列   | 一个     | PRIMARY            | 主键索引 | 非空+唯一约束        |
| AUTO_INCREMENT | 单列/多列 | 单列/多列   | 最多一个 |                    |          | 约束的必须是整数类型 |
| CHECK          |           |             |          |                    |          |                      |
| DEFAULT        |           |             |          |                    |          |                      |

## 1. NOT NULL

- 所有类型的值都可是NULL, 包括int，float等数据类型
- 空字符串‘’和0不等于null
- 该约束不会出现在information_schema.TABLE_CONSTRAINTS中, 可以通过DESC来查看

```sql
# 1.创建表时
CREATE TABLE erick (name VARCHAR(20) NOT NULL);
 
# 2. 创建表后添加新字段
ALTER TABLE erick
    ADD COLUMN address VARCHAR(20) NOT NULL;

# 3. 创建表后给原字段
ALTER TABLE erick
    MODIFY address VARCHAR(40) NOT NULL;
    
# 4. 删除某个字段的约束
ALTER TABLE erick
    MODIFY address VARCHAR(40) NULL;
```

## 2. UNIQUE

```sql
# 1.创建表时： 可以指定约束名， 可以不指定
CREATE TABLE erick (name VARCHAR(20) UNIQUE);       # 列约束

CREATE TABLE lucy (id int, name VARCHAR(20),
CONSTRAINT uk_name UNIQUE(name));                   # 表级约束，可以指定约束名uk_name，也可以不指定
    
# 2. 创建表后添加   
ALTER TABLE lucy
    ADD CONSTRAINT uk_name UNIQUE (name);           # 表级约束

ALTER TABLE lucy
    ADD COLUMN address VARCHAR(20) UNIQUE;          # 列级约束

ALTER TABLE lucy
    MODIFY address VARCHAR(20) UNIQUE;             # 列级约束
   
   
# 3. 多行数据的约束: 如果不指定约束名，则和复合约束的第一个列名一样
CREATE TABLE user
(
    id       INT,
    name     VARCHAR(20),
    password VARCHAR(20),
    CONSTRAINT uk_user_name UNIQUE (name, password)
);

# 4. 删除：只能通过删除对应的索引
ALTER TABLE user
    DROP INDEX uk_user_name;
```

## 3. PRIMARY KEY

- 主键约束多列时，每个列都不允许为null

```sql
# 1. 创建表时
CREATE TABLE my_erick
(
    id   INT PRIMARY KEY,
    name varchar(20)
);                                   # 列级约束

CREATE TABLE my_erick
(
    id   INT ,
    name varchar(20),
    CONSTRAINT PRIMARY KEY(id)
);                                  # 表级约束

# 2. 创建表后
ALTER TABLE people
    MODIFY id int PRIMARY KEY;

# 3. 复合主键
CREATE TABLE my_erick_tb
(
    id   INT,
    name varchar(20),
    PRIMARY KEY (id, name)
)

# 4. 删除主键约束
ALTER TABLE my_erick_tb
    DROP PRIMARY KEY;
```

## 4. AUTO_INCREMENT

- 必须作用在唯一或主键上
- 主键声明有自增，添加数据时，不要给主键赋值

```sql
-- 必须作用在唯一或主键上
CREATE TABLE erick
(
    id   INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20)
);

-- 修改
ALTER TABLE erick
    MODIFY id int AUTO_INCREMENT;
    
-- 删除
ALTER TABLE erick
    MODIFY id int;
    
-- 自增变量持久化
删除一条数据后(如id=10)，新增的数据，会沿用之前的值，下一个值就是11，即使重启服务器也是
```

## 5. CHECK

```sql
-- check约束
CREATE table erick_shu
(
    id     int PRIMARY KEY,
    name   varchar(10),
    salary int CHECK ( salary > 200 )
);
```

## 6. DEFAULT

- 建表时，加not null defalut‘’或default 0 ，不想让表中出现null值
- null不好比较，同时效率不高，影响索引效果

```sql
CREATE table erick_shu
(
    id     int PRIMARY KEY,
    name   varchar(10) DEFAULT 'ERICK',
    salary int CHECK ( salary > 200 )
);

-- 修改时添加
ALTER TABLE erick_shu
    MODIFY name VARCHAR(20) DEFAULT 'LUCY';

-- 删除
ALTER TABLE erick_shu
    MODIFY name VARCHAR(20);
```
