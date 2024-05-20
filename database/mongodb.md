# 基本介绍

## 1. 简介

- 开源，高性能，无模式的文档型数据库
- NoSQL数据库的一种，是最像关系型数据库的NoSQL
- 数据类型： BSON, 类似JSON

|        | MYSQL    | MongoDB    | MongoDB example           |
| ------ | -------- | ---------- | ------------------------- |
| 数据库 | database | database   |                           |
| 表     | table    | collection |                           |
| 行     | row      | document   | {"name":"Erick","age",26} |
| 行     | column   | field      |                           |
| 索引   | index    | index      |                           |
| 表连接 |          | NA         | 通过嵌入文档来代替表连接  |
| 主键   |          | _id        | 自动生成                  |

## 2. 场景

- 数据量大
- 读写频繁
- 数据价值低，对事务要求不高

# 基本指令

- 区分大小写

## 1. Database

```bash
# 查看有权限的数据库
show dbs;
show databases;

# 创建数据库: 如果不存在，则创建新，并切换到这个db中
# 暂时存放在内存中，还没做持久化， 因此这个时候查看不到
# 插入一条数据库后，就会持久化
use erickdb;

# 查看当前在哪个库
db;

# 删除数据库： 删除已经持久化的数据库
db.dropDatabase();
```

```bash
# 本身自带的库

# admin
- root数据库，负责权限管理的

# local
- 数据永远不会被复制
- 用来存储仅限于本地单台服务器的数据

# config
- 设置了mongo的分片的时候，config数据库在内部使用， 用于保存分片的相关信息
```

## 2. Collection

- 类似于mysql的表

```bash
# 创建
# 显示创建 ： 创建一个名字为 ‘good’的collection
db.createCollection('good');

# 查看表
show collections;
show tables;

# 删除表 : 删除‘good’表
db.good.drop();
```

## 3. Document

- 插入一条文档后，该文档所在的库，才会持久化到硬盘上

```bash
# 插入单个: 会按照长度最长的filed来组建
# ‘good’可以不存在于databse里面，执行插入的时候，会自动创建一个collection
db.good.insertOne({"name":"erick","age":20});
db.good.insertOne({"name":"lucy","age":18,"address":"changsha"});

# 插入多条：一旦其中一个失败，将会终止插入，但是之前插入的不会回滚
db.good.insertMany([{"name":"jack"},{"name":"tom","age":10}]);

# 查找全部
db.good.find();

# 查看某个: 属性匹配
db.good.find({"name":"jack"});
db.good.findOne({"name":"jack"})     # 只查看其中一条

# 也可以比较，模糊，in等

# 查看时候，显示address和name字段，_id是自带的
db.good.find({"name":"lucy"},{"address":1, "name":1});

# 分页
db.good.find().limit(3).skip(1);

# 排序: 1为升序， -1为降序
db.good.find().sort({"age":1});
```

```bash
# 更新: 参数一：匹配条件        参数二：更新其中的字段
db.good.updateOne({"name":"erick"},{$set:{"address":"xian"}});

db.good.updateMany({"name":"lucy"},{$set:{"address":"beijig"}});

# 删除
db.good.deleteOne({"name":"erick"});
db.good.deleteMany({"name":"erick"});
```

```bash
# 统计
db.good.countDocuments();
```

# 索引

```bash
# 查看当前索引： 默认_id是索引
db.student.getIndexes();

# name：索引的字段
# v：版本号
# key：具体的那个字段，1代表升序，-1代表降序
[
  {
    "key": {
      "_id": 1
    },
    "name": "_id_",
    "v": 2
  }
]

# 创建索引
db.student.createIndex({name:1});              # 单个索引
db.student.createIndex({name:1,hobby:-1});     # 多个索引

# 删除索引
db.student.dropIndex({name:1});    # 具体指定索引类型
db.student.dropIndex('name_1');    # 根据索引名称
db.student.dropIndexes();          # 删除所有自定义的index，不会删除_id

```



# Java

- springdata

```yaml
spring:
  data:
    mongodb:
      host: 39.105.210.163
      port: 27017
      database: erickdb
      username: erickroot
      password: '123456'               # 如果密码纯数字，则必须加''
      authentication-database: admin   # 必须配置，不然就认证失败
```

