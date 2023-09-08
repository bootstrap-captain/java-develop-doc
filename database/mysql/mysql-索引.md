- 索引：存储引擎快速找到数据记录的一种<font color=orange>数据结构</font>。排好序的快速查找数据结构，满足特定查找算法。这些数据结构以某种方式指向数据，这样就可以在这些数据结构的基础上实现<font color=orange>高级查找算法</font>
- <font color=orange>索引是在存储引擎中实现的</font>，每种存储引擎的索引不完全相同，每种存储引擎支持的索引类型也不同。存储引擎定义的每个表的<font color=orange>最大索引数</font>和<font color=orange>最大索引长度</font>不同
- 类似书本，通过目录找到对应文章的页码，快速定位到需要的文章
- MySQL: 首先查看查询条件是否命中某个索引，符合则<font color=orange>通过索引查找</font>相关数据，否则<font color=orange>全表扫描</font>

# InnoDB索引

```sql
# 精确匹配的查找例子
SELECT [列名] FROM [表名] WHERE 列名=xxx;
```

## 1. 不加索引

### 1.1 一个页

- 表中记录比较少时，所有的记录都存放在一个页中
- 主键查找： 在<font color=orange>页目录</font>中使用<font color=orange>二分法</font>快速定位到对应的<font color=orange>槽</font>，然后再遍历该槽对应的分组中的记录
- 其他列查找：数据页并没有对应非主键列所谓的页目录，只能从<font color="orange">最小记录</font>开始，<font color="orange">依次遍历</font>单链表中的每条记录，然后对比每条记录是否满足搜索条件

### 1.2 多个页

- 主键列：首先定位到查找的记录所在的页然后从所在页中，根据单页的方式查找对应的记录
- 非主键列：可能需要遍历所有数据页，比如搜索条件可能对应多个行记录

## 2. 索引设计

- <font color=green><strong>record_type</strong></font>: 记录的类型。0-普通记录，2-最小记录，3-最大记录
- <font color=green><strong>next_record</strong></font>: 下一条记录的地址相对于本条记录的地址偏移量
- 各个列的值：当前表中的各个列
- 其他信息：除了上述三种信息，其他的隐藏列的值以及记录的额外信息

```sql
# 建表
CREATE TABLE girl_shoes
(
    id   int primary key,
    age  int,
    name char(1)
);
```


### 2.1 数据页

- 假设每个数据页最多能存放3条记录，实际上一个数据页非常大，能够存放多个记录
- 记录按照主键值的大小，串联称一个单向链表
- 多个页之间：双向链表，物理上不连续，逻辑上连续
- 页分裂：一个页存满时，或者根据主键来进行索引时，可能会存在记录移动

```sql
# 插入3条数据： 会按照主键大小来进行插入，并重新排序
INSERT INTO girl_shoes
values (1, 14, 'a'),
       (5, 20, 'c'),
       (3, 16, 'b');

# 再插入一条数据，会存在页分裂，伴随着记录移动
INSERT INTO girl_shoes
values (4, 32, 'd')；
```

![image-20230718165528719](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230718165528719.png)

### 2.2 页之目录项页

- 数据页的<font color=orange>可能是不连续的</font>，16kb的页在物理存储上是不连续的，如果要从这么多页根据主键值<font color=orange>快速定位某些记录所在的页</font>，需要对页做个<font color=orange>目录</font>
- 目录项页和普通页根据record_type来区分
- 每个页对应一个目录项，每个目录项包含两个部分
- <font color=orange>页的用户记录中最小的主键值</font>: key
- <font color=orange>页号</font>： page_no
- 查找数据时页目录：IO只有两次或者多次(定位目录页可能需要多次)，先从目录项页中根据key找到在哪一页，然后再去加载对应的页

![image-20230718171525721](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230718171525721.png)

### 2.3 目录项页之目录项页

- 假如顶层目录项页只有一个，查找一个数据的时候，只需要三次与磁盘之间的IO

![image-20230719102801562](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230719102801562.png)

### 2.4 B+树

- 叶子节点：存放真实的数据，页和页之间通过<font color=orange>双向链表</font>连接，页内的数据通过<font color=orange>单向链表</font>连接
- 非叶子结点：目录项构成的结点，不存放数据

![image-20230719104046875](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230719104046875.png)

- B+树通常不会超过<font color=orange>4层</font>
- 原因一：数据体量
- 原因二：<font color=orange>树的层次越低，内存和磁盘的IO次数就越少</font>，数据查找时候，每经过一层，就是一次IO，因为加载数据的时候，是把一个完整的页加载到内存中

```bash
# 数据体量，一个页的默认大小一般是16kb
假设叶子结点一个页可以存放100条用户记录，非叶子结点一个页可以存放1000条记录(因为非叶子结点数据项比较少)
- 1层：   100条
- 2层：   1000*100条
- 3层：   1000*1000*100条
```

## 3. 索引物理类型

- 索引按照物理存储实现方式，可以分为聚簇索引和非聚簇索引

### 3.1 聚簇索引

- 聚簇索引不是一种单独的索引类型，而是一种<font color=dred>数据存储方式</font>，也就是所有的用户记录存储在叶子结点
- <font color=orange>索引即数据，数据即索引</font>

![image-20230719110202647](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230719110202647.png)

**按主键值大小记录和页排序**

- 页内的记录是按照<font color=orange>主键大小顺序</font>排成一个单向链表
- 各个存放用户记录的页也是根据页中用户记录的主键大小排序称一个双向链表
- 存放目录项记录的页分为不同的层次，在同一层次中的页也是根据页中目录项记录的主键大小顺序排成一个双向链表

**叶子结点存储完整用户记录**

- 完整的用户记录，指该记录中存储了所有列的值(包括隐藏列)

拥有上述两种特性的B+树成为聚簇索引，该聚簇索引不需要在MySQL中显示的使用<font color=orange>INDEX</font>语句创建，<font color=orange>InnoDB</font>存储引擎会<font color=orange>自动</font>为我们创建聚簇索引，同时也是构建数据

#### 优点

- <font color=orange>数据访问更快</font>，索引和数据保存在同一个B+树中，因此比非聚簇索引更快
- 对于主键的<font color=orange>排序查找</font>和<font color=orange>范围查找</font>速度非常快
- 范围查询时，数据都是紧密相连，数据库不用从多个数据快中提取数据，<font color=orange>节省大量IO操作</font>

#### 缺点

- <font color=orange>插入速度严重依赖插入顺序</font>，按照主键的顺序插入是最快的，否则出现页分裂，严重影响性能。因此对于InnoDB表，一般会定义一个<font color=orange>自增的ID列为主键</font>
- <font color=orange>更新主键的代价很高</font>,因为将会导致更新的行移动以及伴随的页分裂。因此对于InnoDB表，一般<font color=orange>主键为不可更新</font>
- <font color=orange>二级索引访问需要两次索引查找</font>，第一次找到主键值，第二次根据主键值找到行数据

#### 限制

- InnoDB支持聚簇索引，MyISAM不支持聚簇索引
- 数据物理存储排序方式只能有一种，所以每个MySQL表<font color=orange>只能有一个聚簇索引</font>。一般情况就是该表的主键
- 如果没有定义主键，InnoDB会选择<font color=orange>非空的唯一索引</font>来代替。如果没有这样的索引，InnoDB会隐式的定义一个主键作为聚簇索引
- 为了充分利用聚簇索引的聚簇特性，InnoDB表的主键列尽可能<font color=orange>用有序的顺序ID</font>，而不建议用无序的id，比如UUID，MD5， HASH， 字符串列作为主键无法保证数据的顺序增长

### 3.2 非聚簇索引

- 也叫做辅助索引，非聚簇索引二级索引
- 聚簇索引只能是搜索条件是<font color=orange>主键值</font>时才能发挥作用，如果要以别的列作为搜索条件时，就需要建立非聚簇索引
- <font color=orange>多建几个B+树</font>，不同的B+树采用不同的排列方式

#### 单个列

![image-20230723103929286](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230723103929286.png)

- <font color=orange>搜索列</font>：对搜索列建立索引，搜索列按照升序排列。<font color=orange>按照哪个字段建立索引，哪个字段的就是升序</font>
- <font color=orange>主键列</font>：叶子结点中，搜索列和主键列进行一一对应
- <font color=orange>叶子结点</font>： 只包含搜索列和主键列，不包含其他字段(避免数据冗余以及维护冗余数据的消耗)。搜索时需要通过两个B+树查找
- <font color=orange>回表</font>： 通过搜索列进行查找时候，先遍历搜索列的非聚簇索引构造的B+树，找到对应的匹配记录的主键，然后通过主键结果，<font color=orange>回表</font>到主键值构成的聚簇索引的B+树
- <font color=orange>一个InnoDB表中，<font color=orange>包含一个聚簇索引和若干个非聚簇索引</font>

#### 联合索引

- 多个列组成一个索引
- 假如通过c2和c3两个列作为联合索引c2_c3，<font color=orange>先按照c2升序排列，c2相同则再通过c3排列</font>
- 叶子结点中搜索列包含联合列：<font color=orange>两个以上的列</font>
- 非叶子结点中的key页包含两个以上的列作为最小值记录

![image-20230723112133238](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230723112133238.png)

## 4. 索引准则

### 4.1 根页面位置万年不变

- 聚簇索引，非聚簇索引的<font color=orange>B+树构建是从上而下</font>
- 当一个表的索引创建后。一开始表中没有数据时，每个B+树索引对应的<font color=orange>根结点既没有用户记录，也没有目录项记录</font>
- 向表中插入用户记录时，<font color=orange>用户记录存储到根结点</font>
- 根结点的<font color=orange>可用空间用完</font>。继续插入数据，<font color=orange>先将根结点的所有记录复制到一个新的页，然后对新页进行页分裂，得到一个新的页来存放新增用户记录</font>。同时<font color=orange>根结点升级为存储目录项记录的页</font>
- 一个B+树索引的根结点一旦产生，便不再移动，同时<font color=orange>根结点的页号会被记录到某个地方</font>。InnoDB存储引擎需要用到这个索引时，会从固定的地方取出根结点页号，从而来访问这个索引

### 4.2 非叶子节点中目录项记录的唯一性

- 针对<font color=orange>非聚簇索引</font>的非叶子结点的key

![image-20230724100936764](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724100936764.png)

- 解决方案：确保非叶子结点的key是唯一的，<font color=orange>搜索列+主键</font>

![image-20230724101634034](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724101634034.png)

### 4.3 一个页面最少存储2条记录

- 一个数据页最少可以存放两条记录，然后通过目录的多次磁盘IO(取决于B+树的深度)获取到真实存储的用户记录的数据页

## 5. MyISAM索引

### 5.1 索引原理

```sql
# boy_shoes.MYD:  存储数据
# boy_shoes.MYI： 存储索引
# boy_shoes_363.sdi： 表定义，表结构的文件
```

- 叶子结点的data域存放的是<font color=orange>数据记录的地址值</font>
- 索引结构依然是<font color=orange>B+树</font>， 但是所有的索引都是<font color=orange>非聚簇索引</font>， <font color=orange>索引数据相分离</font>
- 数据添加时候：<font color=orange>逐行添加不排序</font>
- 每次添加一个新的索引，就是新增一个.MYI文件

![image-20230724112145413](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724112145413.png)

### 5.2 InnoDB vs MyISAM

- InnoDB包含一个聚簇索引和多个非聚簇索引，根据主键值对聚簇索引进行<font color=orange>一次查找</font>就能找到对应记录
- MyISAM全部都是二级索引，根据索引查找时，需要先遍历对应索引的B+树，然后进行<font color=orange>回表操作</font>到数据文件

|                        | InnoDB                                                       | MyISAM                                                       |
| ---------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 索引类型               | 包含一个聚簇索引和多个非聚簇索引，根据主键值对聚簇索引进行<font color=orange>一次查找</font>就能找到对应记录 | 全部都是二级索引，根据索引查找时，需要先遍历对应索引的B+树，然后进行<font color=orange>回表操作</font>到数据文件 |
| 索引存储               | 聚簇索引，<font color=orange>索引即数据，数据即索引</font><br>非聚簇索引，<font color=orange>索引数据分离</font> | 索引数据分离                                                 |
| **非聚簇索引**叶子结点 | data域存放<font color=orange>对应记录的主键值</font>         | <font color=orange>记录的地址值</font>                       |
| **非聚簇索引**回表操作 | 先获取主键，再去聚簇索引找，慢                               | 拿地址偏移量去找，快                                         |
| 主键                   | 必须有主键                                                   | 可以没有                                                     |

## 6. 代价

### 6.1 优点

- 提高数据检索的效率，降低数据库的<font color=orange>磁盘IO成本</font>
- 唯一索引：保证数据库表中每一行的<font color=orange>数据唯一性</font>
- 在使用分组和排序子句进行数据查询时，可以显著<font color=orange>减少查询中分组和排序的时间</font>，降低CPU消耗

### 6.2 缺点

**空间**

- 每建立一个索引，都要为其构建一颗B+树，每颗B+树的每个结点都是一个数据页，一个页默认大小是16KB。会占用大量空间
- 如果有大量的索引，索引文件就可能比数据文件更快达到最大文件尺寸

**时间**

- 每次<font color=orange>写操作</font>时，都要去修改各个索引的B+树： B+树每层结点页都是按照索引列的值<font color=orange>从小到大排序，并组成双向链表</font>，页内记录<font color=orange>是从小到大组成单列向链表</font>，写操作可能会带来<font color=orange>记录移位，页面分裂，页面回收</font>等维护操作
- 并且随着数据量的增加，所耗费的时间也会增加

```bash
# 索引可以提高查询的速度，但会影响插入记录的速度
- 最好的办法是先删除表中的索引，然后插入数据，插入完成后再创建索引
```

# InnoDB数据存储

## 1. 页概念

### 1.1 磁盘与内存交互基本单位

- InnoDB将数据划分为若干页个，页默认大小是<font color=orange>16KB</font>
- 页作为磁盘和内存交互的<font color=orange>基本单位</font>，一次最少从磁盘中读取16KB的内容到内存中，一次最少把16KB内容刷新到磁盘中
- <font color=orange>数据库中，不论读一行，还是读多行，都是将这些行所在的页进行加载，数据库I/O的操作最小单位是页</font>
- 记录是按照行来存储的，但是读取并不是以行为单位，否则一次读取(一次I/O)只能处理一行数据，效率会非常低
- 一个页中可以存储多个行记录

```sql
# 查询页大小： 16384
SHOW VARIABLES LIKE '%innodb_page_size%';
```

### 1.2 页结构概述

- 多个页<font color=orange>可以不在物理结构上连续</font>，只要通过<font color=orange>双向链表</font>相关联即可
- 每个数据页中的用户记录，按照主键值从小到大顺序组成一个<font color=orange>单向链表</font>
- 每个数据页都会为存储在它里面的记录生成一个<font color=orange>页目录</font>
- 通过主键查找某个记录时，在页目录中使用<font color=orange>二分法</font>快速定位到对应的<font color=orange>槽</font>，然后再遍历该槽对应分组中的记录即可快速找到指定记录

![image-20230724142441676](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724142441676.png)

### 1.3 页上层结构

**区(Extent)**

- 一个区会分配<font color=orange>64个连续的页</font>，在文件系统中是一个连续分配的空间。
- 一个区的大小是16KB*24=1MB

**段(Segment)**

- 一个段包含若干个区，区之间<font color=orange>不要求相邻</font>
- <font color=orange>数据库的分配单位，不同类型的数据库对象以不同的段存在</font>，创建表时会创建一个表段，创建索引时会创建一个索引段

**表空间(Tablespace)**

- 是一个逻辑容器，表空间和段的关系是一对多
- 数据库由多个表空间构建：<font color=orange>系统表空间，用户表空间，撤销表空间，临时表空间</font>

![image-20230724142950974](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724142950974.png)

## 2. 页内部结构

| 名称               | 大小   | 描述                             |
| ------------------ | ------ | -------------------------------- |
| File Header        | 38字节 | 文件头，描述页的信息             |
| Page Header        | 56字节 | 页头，页的状态信息               |
| Infimum + Supremum | 26字节 | 最大和最小记录，虚拟的行记录     |
| User Records       | 不确定 | 用户记录，存储行记录内容         |
| Free Space         | 不确定 | 空闲记录，页中还没有被使用的空间 |
| Page Directory     | 不确定 | 页目录，存储用户记录的相对位置   |
| File Trailer       | 8字节  | 文件尾，校验页是否完整           |

### 2.1 文件头/文件尾

- 页文件的通用部分

| 文件头名称                  | 空间(字节) | 描述                                                         |
| --------------------------- | ---------- | ------------------------------------------------------------ |
| FIL_PAGE_OFFSET             | 4          | 页在系统中唯一的编号，方便InnoDB<font color=green>唯一定位</font>一个页 |
| FIL_PAGE_TYPE               | 2          | 当前页的类型                                                 |
| FIL_PAGE_PREV               | 4          | 当前页的上一页                                               |
| FILE_PAGE_NEXT              | 4          | 当前页的下一页                                               |
| FILE_PAGE_SPACE_OR_CHECKSUM | 4          | 校验和                                                       |
| FILE_PAGE_LSN               | 8          | 页面被最后修改时对应的日志序列位置，也是来校验文件完整性的   |

| 文件尾名称                  | 空间(字节) | 描述                                                       |
| --------------------------- | ---------- | ---------------------------------------------------------- |
| FILE_PAGE_SPACE_OR_CHECKSUM | 4          | 校验和                                                     |
| FILE_PAGE_LSN               | 4          | 页面被最后修改时对应的日志序列位置，也是来校验文件完整性的 |

#### FIL_PAGE_TYPE

| 类型名称                                    | 十六进制 | 描述               |
| ------------------------------------------- | -------- | ------------------ |
| FIL_PAGE_TYPE_ALLOCATED                     | 0x0000   | 最新分配，还没使用 |
| <font color=orange>FIL_PAGE_UNDO_LOG</font> | 0x0002   | Undo日志页         |
| FIL_PAGE_INODE                              | 0x0003   | 段信息结点         |
| <font color=orange>FIL_PAGE_TYPE_SYS</font> | 0x0006   | 系统页             |
| <font color=orange>FIL_PAGE_INDEX</font>    | 0x45BF   | 索引页，数据页     |
| 其他类型                                    |          |                    |

#### FILE_PAGE_SPACE_OR_CHECKSUM

- FILE_PAGE_SPACE_OR_CHECKSUM和FILE_PAGE_LSN都是用来进行文件的完整性校验的
- 文件头和文件尾的来进行比较

```bash
# 校验和
- 对于一个很长的字节串来说，通过某种算法得到一个比较短的值来代表，该较短的值就是校验和
- 比较两个很长的字节串时，先比较其检验和。如果校验和都不一样，则两个长的字节串肯定不一样，节省不等于时候的时间损耗

# 文件头文件尾都有属性：FILE_PAGE_SPACE_OR_CHECKSUM
```

- InnoDB将数据以页为单位，将其加载到内存中并对数据进行处理，处理完成后要再重新刷盘到磁盘中
- <font color=red>刷盘时，假如同步了当前页的一半数据，数据库挂了，内存数据丢失</font>，当前页刷盘就是不完整的
- 为检查一个页是否完整，文件头和文件尾都有一个校验值，通过比较两个值。<font color=orange>如果相同，则刷盘完整。如果值不同，则刷盘不完整，需要重新进行刷盘</font>

```bash
# 具体刷盘操作
- 当一个页在内存中被修改了，刷盘前会计算其检验和
- 开始刷盘，先修改File Header中的检验和。完成刷盘后，再修改File Trailer中的校验和
- 同步成功：刷盘完成，首尾检验和相同
- 同步失败：刷盘一半时，MySQL挂了，首尾校验和不同，需要重新刷盘
- 检验方式： Hash算法
```

### 2.2 空闲空间

- 表中存储的用户记录会按照指定的<font color=orange>行格式</font>存储到User Records中
- 每次插入一条记录，都会从Free Space中，申请一个记录大小的空间划分到User Records中，直到Free Space耗尽，则<font color=orange>申请新页</font>

### 2.3 用户记录

- 按照指定的<font color=orange>行格式</font>，一条条摆在User Records中，相互之间形成单链表

### 2.4 页目录-Page Directory

- 将一个页中所有的记录，<font color=orange>分成几个组</font>，包含最小记录和最大记录，但不包含已删除的的记录
- 第一组：当前页中最小记录所在的组，只包含一个
- 最后一组：最大记录所在的组，会有1-8条记录
- 其余组：数量在4-8之间
- 会在n_owned中保存：当前组中最后一条记录的头信息中会存储该组中一共有多少个记录
- 槽中的数据指向当前组的最大值

![image-20230724193519800](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724193519800.png)

## 3. 行格式

### 3.1 COMPACT

```sql
CREATE TABLE erick
(
    c1 INT,
    c2 INT,
    c3 VARCHAR(10000),
    PRIMARY KEY (c1)
) CHARSET = ascii
  ROW_FORMAT = COMPACT;
```

#### 记录头(5字节)

![image-20230724180908337](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724180908337.png)

**delete_mask**

- 标记当前记录是否被删除，1个二进制位。0代表没删除，1代表已删除
- 删除一条记录，<font color=orange>不立即从磁盘上移除</font>，是因为移除该记录后，其他的记录需要在磁盘上<font color=orange>重新排列，导致性能消耗</font>
- 只是进行删除标记，所有被删除的记录会组成<font color=orange>垃圾链表</font>，在这个链表中的记录占用的空间是<font color=orange>可重用空间</font>，之后如果有新记录插入的话，进行覆盖写操作

**min_rec_mask**

- B+树的每层非叶子结点中的最小记录都会添加该标记，值为1
- 叶子结点中，值是0，表示其不是非叶子结点的最小记录

**record_type**

- 0： 用户记录
- 1：B+数非叶子结点记录
- 2：最小记录
- 3：最大记录

**heap_no**

- 当前记录在本页中的位置
- 用户记录一般是2，3，4，5.。。。。。
- <font color=orange>0-最小记录；1-最大记录</font>。MySQL会自动为每个页中先添加两个记录，<font color=orange>伪记录，虚拟记录</font>，位置最靠前

**n_owned**

- 当前页中，当前组中最后一条记录的头信息中会存储该组中一共有多少个记录

**next_record**

- 当前记录的真实数据到下一条数记录的真实数据的<font color=orange>地址偏移量</font>
- 一条记录的next_record是32，意味着<font color=orange>从该条记录的真实数据的地址处向后找32个字节</font>就是下一条数据





# 索引的声明和使用

## 1. 分类

### 1.1 逻辑功能

**普通索引**

- 不加任何限制条件，只是为了提高查询效率
- 可以创建在<font color=orange>任何数据类型上</font>，其值是否唯一和非空，要由字段本身的完整性约束条件决定
- 建立索引后，可以通过索引进行查询

**唯一索引**

- 使用<font color=orange>UNIQUE</font>设置字段，就会自动添加唯一索引
- 该索引的值必须是唯一的，但允许有空值
- 一个表中<font color=orange>可以有多个</font>

**主键索引**

- 一种<font color=orange>特殊的唯一性索引</font>，在唯一索引的基础上增加了不为空的约束：NOT NULL + UNIQUE
- 一个表中只能包含一个主键索引：主键索引决定了数据的存储方式

**全文索引**

- 是目前<font color=orange>搜索引擎</font>使用的一种技术，利用<font color=orange>分词技术</font>等多种算法智能分析
- 一般是用Elastic Search来替代

### 1.2 物理实现方式

- 聚簇索引
- 非聚簇索引

### 1.3 作用字段个数

- 单列索引
- 联合索引：作用在多个字段上

## 2. 创建索引

### 2.1 创建表时创建

- <font color=orange> INDEX | KEY </font>： 同义词
- <font color=orange>index_name</font>： 索引名称，可选参数。默认是col_name为索引名
- <font color=orange>col_name</font>：需要创建索引的字段列
- <font color=orange>length</font>：可选参数，表示索引的长度，只有字符串类型的字段才能指定索引长度
- <font color=orange>ASC | DESC</font>：指定生序或者降序的索引值存储

```SQL
CREATE TABLE table_name [col_name data_type]
[UNIQUE | FULLTEXT] [INDEX | KEY] [index_name] (col_name[length])
[ASC | DESC]
```

```sql
CREATE TABLE book1
(
    id      INT,
    name    VARCHAR(100),
    comment VARCHAR(100),
    price   INT,
    # 普通索引
    INDEX name_index (name),
    # 唯一索引
    UNIQUE INDEX comment_index (comment),
    # 主键索引：名字就是PRIMARY，自定义名字不会起作用
    PRIMARY KEY (id),
    # 联合索引: 也可以为unique, 最左前缀原则
    INDEX mul_name_comment_price_index (name, comment, price)
);

# 查看索引
SHOW INDEX FROM book1;
```

### 2.2 现有表上创建

```SQL
# 普通索引
ALTER TABLE book2
    ADD INDEX name_index (name);
# 唯一索引
ALTER TABLE book2
    ADD UNIQUE INDEX comment_index (comment);
# 主键索引
ALTER TABLE book2
    ADD PRIMARY KEY (id);
# 联合索引
ALTER TABLE book2
    ADD INDEX mul_name_comment_price_index (name, comment, price);
```

```SQL
# 普通索引
CREATE INDEX name_index ON book3 (name);
# 唯一索引
CREATE UNIQUE INDEX comment_index ON book3 (comment);
# 联合索引
CREATE INDEX mul_name_comment_price_index ON book3 (name, comment, price);
# 主键索引: 不能通过这种方式
```

### 2.3 删除索引

- 删除索引时，是根据名字来匹配的，所以不需要指定索引类型，除了主键索引

```sql
# 普通索引
ALTER TABLE book2
    DROP INDEX name_index;
# 唯一索引： 不是drop unique index
ALTER TABLE book2
    DROP INDEX comment_index;
# 主键索引
ALTER TABLE book2
    DROP PRIMARY KEY;
# 联合索引
ALTER TABLE book2
    DROP INDEX mul_name_comment_price_index;
```

```sql
# 普通索引
DROP INDEX name_index ON book3;
# 唯一索引
DROP INDEX comment_index ON book3;
# 联合索引
DROP INDEX mul_name_comment_price_index ON book3;
```

```sql
# 删除某个字段时候，假如该字段有索引，对应索引也会删除
# 删除某个字段的时候，假如该字段具有联合索引，联合索引的该字段也会更新删除
ALTER TABLE book2 DROP COLUMN name;
```

## 3. MySQL8.0新特性

### 3.1 降序索引

- <font color=orange>MySQL8.0之前，索引都是升序，使用时进行反向扫描，大大降低数据库的效率(单链表逆向遍历)</font>
- 某些操作，需要对多个列进行排序，且顺序要求不一致，使用降序索 引就会避免数据库使用额外的文件排序操作，提高性能

```sql
CREATE TABLE book4
(
    id      INT,
    name    VARCHAR(100),
    comment VARCHAR(100),
    price   INT,
    INDEX name_comment_index (name ASC, comment DESC)
);
```

### 3.2 隐藏索引

```bash
# 引入场景：打算删除一个索引，看一下删除后会不会对系统有影响

# 5.7及之前
- 先显示删除索引
- 删除后发现系统错误，只能显示的再去创建删除的索引
- 如果表中数据量很大，这种操作就会消耗系统过多的资源，操作成本比较高

# 8.0-隐藏索引： 软删除
- 将待删除的索引设置为隐藏索引，查询优化器就不再使用这个索引(即使使用force index,优化器也不会使用该索引)
- 确认变为隐藏索引后，系统不受任何影响，就可以彻底删除该索引
```

- 主键不能设置为隐藏索引。当表中没有显示主键时，表中第一个唯一非空索引会成为隐式主键，也不能设置为隐藏索引
- <font color=orange>索引被隐藏时，对应的B+树仍然和正常索引一样，随着写操作进行实时更新</font>，索引如果需要被长期隐藏，那么建议将其删除，因为索引的存在会影响写性能

```sql
# 创建表时
CREATE TABLE book5
(
    id      INT,
    name    VARCHAR(100),
    comment VARCHAR(100),
    price   INT,
    INDEX name_index (name) INVISIBLE
);

# 创建表后
ALTER TABLE book5
    ADD index comment_index (comment) INVISIBLE;

CREATE INDEX price_index ON book5 (price) INVISIBLE;

# 修改索引可见性
ALTER TABLE book5
    ALTER INDEX comment_index INVISIBLE;
```

## 4. 适合加索引

### 4.1 字段数值有唯一性

- 某个字段是唯一的：创建<font color=orange>唯一索引，或者主键索引</font>
- 业务上具有唯一特性的字段：<font color=orange>即使是组合字段，也必须建成唯一聚簇索引</font>(阿里规范)
- 唯一索引影响了insert速度，但损耗可以忽略，但提高查找速度很明显(查找B+树，找到一条记录后，就不再查找其余的页了)

### 4.2 频繁作为WHERE查询条件的字段

- 创建普通索引就可以大幅度提升数据查询的效率(根据B+树的搜索，可能搜索到多个数据页，但是不用扫全部页)

### 4.3 经常GROUP BY和ORDER BY的列

- 索引就是让数据按照某种顺序进行存储和检索
- 创建单列索引或者联合索引
- 待补充 

### 4.4 UPDATE/DELETE的WHERE条件列

- 数据按照某个条件进行查询后再进行UPDATE/DELETE， 对WHERE字段建立索引，就能大幅度提升效率
- 原理：<font color=orange>先要按照WHERE条件列检索出该条记录，然后再对其进行UPDATE/DELETE</font>
- 如果更新的字段是非索引字段，提升效率更加明显，因为不需要对非索引字段进行索引维护

### 4.5 DISTINCT字段

- 不加索引：全表扫描，将该字段在内存中去重
- 加索引：<font color=orange>B+树相邻去重更快</font>，同时查出的数据是<font color=orange>递增排序</font>

### 4.6 区分度高(散列性高)的列

- 列的基数：某一列中不重复数据的个数
- 区分度：列基数/记录行数，区分度越接近1，越适合做索引
- <font color=orange>联合索引把区分度高的列放在前面</font>

```sql
# 一般超过33%就算是比较高效的索引
SELECT COUNT(DISTINCt a)/COUNT(*) FROM t1;
```

## 6. 最佳实践

### 6.1 字符串前缀创建索引

### 6.2 单表的索引数量不超过6个

- <font color=orange>空间</font>：每个索引都需要占用磁盘空间
- <font color=orange>性能</font>：被索引的字段的写操作，需要维护索引
- <font color=orange>索引评估</font>： 优化器在选择如何优化查询时，会根据统一信息，对每一个可能用到的<font color=orange>索引来进行评估</font>，以生成一个最好的执行计划，如果有多个索引都可以用于查询，会<font color=orange>增加MySQL优化器生成执行计划时间</font>，降低查询性能

### 6.3 数据量小的表不要加索引

- 如果表记录少，比如少于1000个，不需要创建索引
- 索引产生的非聚簇索引，查询时候需要回表，效率可能会更低

# 性能分析

## 1. 查看系统性能参数

```sql
SHOW GLOBAL|SESSION STATUS LIKE 'connections';
```

| 参数                 | 描述                           |
| -------------------- | ------------------------------ |
| connections          | 连接MySQL服务器的次数          |
| uptime               | MySQL服务器的上线时间          |
| slow_queries         | 慢查询次数                     |
| Innodb_rows_read     | select查询返回的行数           |
| Innodb_rows_inserted | insert操作插入的行数           |
| Innodb_rows_updated  | update操作更新的行数           |
| Innodb_rows_deleted  | delete操作删除的行数           |
| com_select           | 查询操作的次数                 |
| com_insert           | 插入操作的次数，批量插入算一次 |
| com_update           | 更新操作的次数                 |
| com_delete           | 删除操作的次数                 |

## 2. 慢查询日志

- 用来记录在MySQL中<font color=orange>相应时间超过阈值</font>的语句，具体值运行时间超过<font color=orange>long_query_time</font>值的SQL，则会被记录到慢查询日志中
- long_query_time：默认是10s
- 作用：记录执行时间特别长的<font color=orange>SQL查询</font>，并且针对性的进行优化，提高系统的整体效率。当数据服务器发生阻塞，运行变慢，通过检查慢SQL日志，定位到慢查询来解决
- MySQL默认<font color=orange>不开启慢查询日志</font>，需要手动开启。<font color=orange>除非调优需要，一般不建议启动该参数</font>，因为开启慢查询日志会对性能带来一定影响

### 2.1 开启

```SQL
# 默认是OFF
SHOW VARIABLES LIKE '%slow_query_log%';

# 打开慢查询: 同时会有慢查询日志的文件
SET GLOBAL slow_query_log=ON;

slow_query_log,ON
slow_query_log_file,/var/lib/mysql/8bc168b1a471-slow.log

# 默认阈值： 10s
SHOW VARIABLES LIKE '%long_query_time%';

# 修改阈值: 1s
SET long_query_time = 1;

# 建议这些配置在mysql服务器中添加
```

### 2.2 分析

- mysqldumpslow -help ;

```sql
SHOW STATUS LIKE '%slow_queries';
```

## 3. Explain

- 定位到慢查询的SQL后，使用EXPLIAN或者DESCRIBE工具做针对性的分析查询语句
- Explain并没有真正执行后面的SQL语句，因此可以安全的查看执行计划

