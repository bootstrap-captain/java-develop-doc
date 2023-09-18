- 索引：<font color=orange>存储引擎</font>快速找到数据记录的一种<font color=orange>数据结构</font>。<font color=orange>排好序的数据结构</font>，满足特定查找算法。这些数据结构以某种方式指向数据，这样就可以在这些数据结构的基础上实现<font color=orange>高级查找算法</font>，从而减少磁盘IO，提升查找效率
- <font color=orange>索引是在存储引擎中实现的</font>：每种存储引擎支持的索引不同，每个表的<font color=orange>最大索引数</font>和<font color=orange>最大索引长度</font>不同
- 类似书本，通过目录找到对应文章的页码，快速定位到需要的文章
- MySQL: 首先查看查询条件是否命中某个索引，符合则<font color=orange>通过索引查找</font>相关数据，否则<font color=orange>全表扫描</font>

# InnoDB索引

```sql
# 精确匹配的查找例子
SELECT [列名] FROM [表名] WHERE 列名=xxx;
```

## 1. 不加索引

- 数据页：mysql底层存储数据的最小物理单位
- 表中记录少，所有记录存放在一个页。表中记录多时，会有多个页
- 主键：数据存储时候，默认是按照主键来进行排序的

### 1.1 一个页

- 主键查找： 在<font color=orange>页目录</font>中使用<font color=orange>二分法</font>快速定位到对应的<font color=orange>槽</font>，然后再遍历该槽对应的分组中的记录
- 其他列查找：数据页并没有对应非主键列的页目录，只能从<font color="orange">最小记录依次遍历</font>单链表中的每条记录，判断每条记录是否满足搜索条件，因为可能多列符合条件

### 1.2 多个页

- 主键列：首先定位到查找的记录所在的页然后从所在页中，根据单页的方式查找对应的记录
- 非主键列：可能需要遍历所有数据页，比如搜索条件可能对应多个行记录

## 2. 索引推演

- <font color=orange><strong>record_type</strong></font>: 记录的类型。0-普通记录，2-最小记录，3-最大记录
- <font color=orange><strong>next_record</strong></font>: 下一条记录的地址相对于本条记录的地址偏移量
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
- 单页内：单向链表，记录按照主键值大小串联
- 页之间：双向链表，物理上不连续，逻辑上连续
- 页分裂：一个页存满时，或者插入新数据时候，受索引列排列影响，可能会存在记录移动

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

![image-20230910095630532](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230910095630532.png)

### 2.2 数据页-目录项页

- 数据页(16kb)<font color=orange>可能是物理上不连续</font>，在多个页中进行查找时，需要从首页遍历
- 要从多个页中根据主键值<font color=orange>快速定位某些记录所在的页</font>，需要对页再做个<font color=orange>目录</font>

```bash
# 目录项页和普通页根据record_type来区分

# 每个页对应一个目录项，每个目录项包含两个部分
-key：      页的用户记录中最小的主键值
-page_no：  页号

# 主键查找数据过程
- 先根据数据页-目录项页，查看当前查找的主键，在具体的哪个数据页
- 然后进入具体的数据页，通过单页的二分法查找到数据

# IO只有两次或者多次(定位目录页可能需要多次)
```

![image-20230910095757954](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230910095757954.png)

### 2.3 目录项页-目录项页

- 假如顶层目录项页只有一个，查找一个数据的时候，只需要三次与磁盘之间的IO

![image-20230719102801562](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230719102801562.png)

## 3. B+树

- 叶子节点：存放真实的数据，页和页之间通过<font color=orange>双向链表</font>连接，页内的数据通过<font color=orange>单向链表</font>连接
- 非叶子结点：目录项构成的结点，不存放数据

![image-20230719104046875](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230719104046875.png)

```bash
# B+树通常不会超过4层
- 原因一：数据体量
- 原因二：树的层次越低，内存和磁盘的IO次数就越少
         数据查找时候，每经过一层，就是一次IO，因为加载数据的时候，是把一个完整的页加载到内存中

# 数据体量，一个页的默认大小一般是16kb
假设叶子结点一个页可以存放100条用户记录，非叶子结点一个页可以存放1000条记录(因为非叶子结点数据项比较少)
- 1层：   100条
- 2层：   1000*100条
- 3层：   1000*1000*100条
```

## 4. 索引物理类型

- 索引按照物理存储实现方式，分为聚簇索引和非聚簇索引

### 4.1 聚簇索引

- 不是一种单独的索引类型，而是一种<font color=orange>数据存储方式</font>，即所有用户记录存储在叶子结点
- <font color=orange>索引即数据，数据即索引</font>

![image-20230719110202647](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230719110202647.png)

#### 特点

```bash
# 特点一： 按主键值大小排序
- 页内：记录按照 主键大小顺序 排成一个单向链表
- 多页：多页间根据页中用户记录的主键大小排序称一个双向链表

# 特点二：叶子结点存储完整用户记录
- 完整的用户记录，指该记录中存储了所有列的值(包括隐藏列)

# 拥有上述两种特点的B+树称为聚簇索引
- 该聚簇索引不需要在MySQL中显示的使用INDEX语句创建，
- InnoDB存储引擎会 自动 为我们创建聚簇索引，同时也是构建数据
```

#### 优点

- <font color=orange>数据访问更快</font>，索引和数据保存在同一个B+树中，因此比非聚簇索引更快
- 对于主键的<font color=orange>排序查找</font>和<font color=orange>范围查找</font>速度非常快
- 范围查询时，数据都是紧密相连，数据库不用从多个数据块中提取数据，<font color=orange>节省大量IO操作</font>

#### 缺点

- <font color=orange>插入速度严重依赖插入顺序</font>：按照主键的顺序插入是最快的，否则出现页分裂，严重影响性能。一般会定义一个<font color=orange>自增的ID列为主键</font>
- <font color=orange>更新主键的代价很高</font>：会导致更新的行移动以及伴随的页分裂，一般<font color=orange>主键为不可更新</font>
- <font color=orange>二级索引访问需要两次索引查找</font>：第一次找到主键值，第二次根据主键值找到行数据

#### 限制

- InnoDB支持聚簇索引，MyISAM不支持聚簇索引
- 数据物理存储排序方式只能有一种，所以每个MySQL表<font color=orange>只能有一个聚簇索引</font>。一般情况就是该表的主键
- 如果没有定义主键，InnoDB会选择<font color=orange>非空的唯一索引</font>来代替。如果没有这样的索引，InnoDB会隐式的定义一个主键作为聚簇索引
- 为了充分利用聚簇索引的聚簇特性，InnoDB表的主键列尽可能<font color=orange>用有序的顺序ID</font>，而不建议用无序的id，比如UUID，MD5， HASH， 字符串列作为主键无法保证数据的顺序增长

### 4.2 非聚簇索引

- 辅助索引，非聚簇索引，二级索引
- 聚簇索引只能是搜索条件是<font color=orange>主键值</font>时才能发挥作用。如果要以别的列作为搜索条件时，就需要建立非聚簇索引
- <font color=orange>多建几个B+树</font>，不同的B+树采用不同的排列方式

#### 单列

- <font color=orange>搜索列</font>：对搜索列建立索引，搜索列按照升序排列。<font color=orange>按照哪个字段建立索引，哪个字段的就是升序</font>
- <font color=orange>主键列</font>：叶子结点中，搜索列和主键列进行一一对应
- <font color=orange>叶子结点</font>： 只包含搜索列和主键列，不包含其他字段(避免数据冗余以及维护冗余数据的消耗)。搜索时需要通过两个B+树查找
- <font color=orange>回表</font>： 通过搜索列查找时，先遍历搜索列的非聚簇索引构造的B+树，找到对应的匹配记录的主键，然后通过主键结果，<font color=orange>回表</font>到主键构成的聚簇索引的B+树
- 一个InnoDB表中，<font color=orange>包含一个聚簇索引和若干个非聚簇索引</font>

![image-20230723103929286](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230723103929286.png)

#### 联合索引

- 多个列组成一个索引
- 假如通过c2和c3两个列作为联合索引c2_c3，<font color=orange>先按照c2升序排列，c2相同则再通过c3排列</font>
- 叶子结点中搜索列包含联合列：<font color=orange>两个以上的列</font>
- 非叶子结点中的key页包含两个以上的列作为最小值记录

![image-20230723112133238](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230723112133238.png)

## 5. 索引准则

### 5.1 根页面位置不变

- <font color=orange>B+树构建是从上而下</font>
- B+树索引的根结点一旦产生，便不再移动

```bash
# 构建过程
1. 索引创建后，表中无数据时，B+树索引对应的根结点既没有用户记录，也没有目录项记录
2. 插入用户记录后，用户记录存储到根结点，直到根结点的可用空间用完时
3. 继续插入数据，先将根结点的所有记录复制到一个新页，然后对新页进行页分裂，得到一个新的页来存放新增用户记录
4. 同时根结点升级为存储目录项记录的页

# 优点
- 根结点的页号会被记录到某个地方B
- 存储引擎需要用到这个索引时，会从固定的地方取出根结点页号，从而来访问这个索引
```

### 5.2 非叶子节点记录唯一

- 针对<font color=orange>非聚簇索引</font>的非叶子结点的key

![image-20230724100936764](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724100936764.png)

- 解决方案：确保非叶子结点的key是唯一的，<font color=orange>搜索列+主键</font>

![image-20230724101634034](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724101634034.png)

### 5.3 单页面最少2条记录

- 一个数据页最少可以存放两条记录，然后通过目录的多次磁盘IO(取决于B+树的深度)获取到真实存储的用户记录的数据页

## 6. 优缺点

### 6.1 优点

- 提高数据检索的效率，降低数据库的<font color=orange>磁盘IO成本</font>
- 唯一索引：保证数据库表中每一行的<font color=orange>数据唯一性</font>
- 在使用分组和排序子句进行数据查询时，可以显著<font color=orange>减少查询中分组和排序的时间</font>，降低CPU消耗

### 6.2 缺点

**空间**

- 每建立一个索引，都要为其构建一颗B+树，每颗B+树的每个结点都是一个数据页(16KB)，会占用大量空间
- 如果有大量的索引，索引文件就可能比数据文件更快达到最大文件尺寸

**时间**

- 每次<font color=orange>写操作</font>时，都要去修改各个索引的B+树： B+树每层结点页都是按照索引列的值<font color=orange>从小到大排序，并组成双向链表</font>，页内记录<font color=orange>是从小到大组成单列向链表</font>，写操作可能会带来<font color=orange>记录移位，页面分裂，页面回收</font>等维护操作
- 并且随着数据量的增加，所耗费的时间也会增加

```bash
# 索引可以提高查询的速度，但会影响插入记录的速度
- 最好的办法是先删除表中的索引，然后插入数据，插入完成后再创建索引
```

# MyISAM索引

```bash
# boy_shoes.MYD:         存储数据
# boy_shoes.MYI：        存储索引
# boy_shoes_363.sdi：    表定义，表结构的文件
```

## 1. 用户记录

- .MYD文件：具体的用户数据和对应的地址值
- 数据存放时，按照数据添加顺序进行排列，不会进行再次排序

## 2. 索引文件

- .MYI文件：对应搜索列的索引文件
- 叶子结点的data域存放的是<font color=orange>数据记录的地址值</font>
- 索引结构依然是<font color=orange>B+树</font>， 但是所有的索引都是<font color=orange>非聚簇索引</font>， <font color=orange>索引数据相分离</font>
- 每次添加一个新的索引，就是新增一个.MYI文件

![image-20230724112145413](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724112145413.png)

## 3. InnoDB vs MyISAM

- InnoDB包含一个聚簇索引和多个非聚簇索引，根据主键值对聚簇索引进行<font color=orange>一次查找</font>就能找到对应记录
- MyISAM全部都是二级索引，根据索引查找时，需要先遍历对应索引的B+树，然后进行<font color=orange>回表操作</font>到数据文件

|                        | InnoDB                                                       | MyISAM                                                       |
| ---------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 索引类型               | 包含一个聚簇索引和多个非聚簇索引，根据主键值对聚簇索引进行<font color=orange>一次查找</font>就能找到对应记录 | 全部都是二级索引，根据索引查找时，需要先遍历对应索引的B+树，然后进行<font color=orange>回表操作</font>到数据文件 |
| 索引存储               | 聚簇索引，<font color=orange>索引即数据，数据即索引</font><br>非聚簇索引，<font color=orange>索引数据分离</font> | 索引数据分离                                                 |
| **非聚簇索引**叶子结点 | data域存放<font color=orange>对应记录的主键值</font>         | <font color=orange>记录的地址值</font>                       |
| **非聚簇索引**回表操作 | 先获取主键，再去聚簇索引找，慢                               | 用地址偏移量去找，快                                         |
| 主键                   | 必须有主键                                                   | 可以没有                                                     |



# InnoDB数据

## 1. 页-磁盘与内存交互基本单位

- 数据存储划分为若干页个，页默认大小是<font color=orange>16KB</font>
- 页是磁盘和内存交互的<font color=orange>基本单位</font>，一次IO最少从磁盘中读取16KB内容到内存，或最少把16KB内容刷新到磁盘
- <font color=orange>不论读一行或多行，都是将这些行所在的页进行加载，数据库I/O的操作最小单位是页</font>
- 记录是按照行来存储的，但是读取并不是以行为单位，否则一次读取(一次I/O)只能处理一行数据，效率会非常低
- 多个页<font color=orange>可以不在物理结构上连续</font>，只要通过<font color=orange>双向链表</font>相关联即可
- 单页中的用户记录，按照主键值从小到大顺序组成一个<font color=orange>单向链表</font>

```bash
# 查询页大小： 16384
SHOW VARIABLES LIKE '%innodb_page_size%';

# 页目录
- 单页中，存储的所有记录会生成一个当前页的 页目录
- 页目录中会包含多个 slot槽

# 单页查找
- 通过页目录中使用 二分法 快速定位到对应的 槽
- 再遍历该槽对应分组中的记录即可快速找到指定记录
```

![image-20230724142441676](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724142441676.png)

## 2. 页结构

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

| <font color=orange>文件头</font> | 空间(字节) | 描述                                                         |
| -------------------------------- | ---------- | ------------------------------------------------------------ |
| FIL_PAGE_OFFSET                  | 4          | 页在系统中唯一的编号，方便InnoDB<font color=orange>唯一定位</font>一个页(页号) |
| FIL_PAGE_TYPE                    | 2          | 当前页的类型                                                 |
| FIL_PAGE_PREV                    | 4          | 当前页的上一页(双向链表)                                     |
| FILE_PAGE_NEXT                   | 4          | 当前页的下一页(双向链表)                                     |
| FILE_PAGE_SPACE_OR_CHECKSUM      | 4          | 校验和                                                       |
| FILE_PAGE_LSN                    | 8          | 页面被最后修改时对应的日志序列位置，也是来校验文件完整性的   |

| <font color=orange>文件尾</font> | 空间(字节) | 描述                                                       |
| -------------------------------- | ---------- | ---------------------------------------------------------- |
| FILE_PAGE_SPACE_OR_CHECKSUM      | 4          | 校验和                                                     |
| FILE_PAGE_LSN                    | 4          | 页面被最后修改时对应的日志序列位置，也是来校验文件完整性的 |

#### FIL_PAGE_TYPE

- 表示当前页是什么类型的数据页

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
# 1. 概念
- 对于一个很长的字节串来说，通过某种算法得到一个比较短的值来代表，该较短的值就是校验和
- 比较两个很长的字节串时，先比较其检验和。如果校验和都不一样，则两个长的字节串肯定不一样，节省不等于时候的时间损耗

# 文件头文件尾都有属性：FILE_PAGE_SPACE_OR_CHECKSUM

# 2. 作用
- InnoDB将数据以页为单位，将其加载到内存中并对数据进行处理，处理完成后要再重新刷盘到磁盘中
- 刷盘时，假如同步了当前页的一半数据，数据库挂了，内存数据丢失。当前页刷盘就是不完整的
- 为检查一个页是否完整，文件头和文件尾都有一个校验值，通过比较两个值
- 如果相同，则刷盘完整。如果值不同，则刷盘不完整，需要重新进行刷盘

# 3. 具体刷盘操作
- 开始刷盘，先修改File Header中的检验和。完成刷盘后，再修改File Trailer中的校验和
- 同步成功：刷盘完成，首尾检验和相同
- 同步失败：刷盘一半时，MySQL挂了。重启后加载某个页时，首尾校验和不同，需要通过redo日志重新刷盘
- 检验方式： Hash算法
```

### 2.2 空闲空间

- 表中存储的用户记录会按照指定的<font color=orange>行格式</font>存储到User Records中
- 插入一个记录，会从Free Space中，申请一个行记录大小的空间给User Records，直到Free Space耗尽，则<font color=orange>申请新页</font>

### 2.3 用户记录

- 按照指定的<font color=orange>行格式</font>，一条条摆在User Records中，相互之间形成单链表

### 2.4 最小/大记录

- 该页中对应的索引列的最大记录和最小记录

### 2.5 页目录

- Page Directory
- 将一个页中所有的记录，<font color=orange>分成几个组</font>，包含最小记录和最大记录，但不包含已删除的的记录
- 可以在一个页中，进行二分法快速找到对应的数据

```bash
# 页内数据分组
- 第一组：当前页中最小记录所在的组，只包含一个
- 最后一组：最大记录所在的组，会有1-8条记录
- 其余组：数量在4-8之间

- 会在n_owned中保存：当前组中最后一条记录的头信息中会存储该组中一共有多少个记录
- 槽中的数据指向当前组的最大值
```

### 2.6 页面头部

## 3. 行格式

- 页中对应的数据记录(用户记录，空闲空间，最大指针和最小指针)中的具体的格式
- Mysql8.0的默认格式是COMPACT

### 3.1 COMPACT

```sql
# 指定行格式
CREATE TABLE erick
(
    c1 INT,
    c2 INT,
    c3 VARCHAR(10000),
    PRIMARY KEY (c1)
) CHARSET = ascii
  ROW_FORMAT = COMPACT;
  
# 查看
SHOW TABLE STATUS like '%table_name%'
```

#### 变长字段长度列表

- Varchar(m)， TEXT类型的字段，是变长字段类型，存储数据时需要记录这些数据占用的字节数
- 把所有变长字段的真实数据占用的字节长度存储在行记录的开头部分
- 字段声明和记录是逆序的，c1(06), c2(08), c3(10)， 存储记录 100806

#### null值列表

- 把为null的列统一管理起来，如果表中没有允许存储null的列，则该数据为空

#### 记录头(5字节)

![image-20230724180908337](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724180908337.png)

```bash
# delete_mask
- 标记当前记录是否被删除，1个二进制位               # 0～未删，1～已删
- 删除记录不立即从磁盘上移除                       # 移除该记录后，如果物理删除，其他记录需要在磁盘上重新排列，导致性能消耗
- 删除标记后，所有被删除的记录会组成 <垃圾链表>      # 垃圾链表
- 垃圾链表中的记录占用的空间，是可重用空间，后续新记录插入时，进行覆盖写操作

# min_rec_mask
- B+树的每层非叶子结点中的最小记录都会添加该标记，值为1
- 叶子结点中，值是0，表示其不是非叶子结点的最小记录

# record_type
- 0： 用户记录
- 1： B+数非叶子结点记录
- 2： 最小记录
- 3： 最大记录

# heap_no
- 当前记录在本页中的位置            # 用户记录一般是2，3，4，5.。。。。。
                                # 0-最小记录；1-最大记录
- MySQL会自动为每个页中先添加两个记录，伪记录，虚拟记录，位置最靠前

# n_owned
- 当前页中，当前组中最后一条记录的头信息中会存储该组中一共有多少个记录
- 一个页中的用户记录，会被分成若干个组

# next_record
- 当前记录的真实数据到下一条数记录的真实数据的   <地址偏移量>
- 一条记录的next_record是32，从该条记录的真实数据的地址处向后找32个字节就是下一条数据
```

#### 真实数据

- 用户的真实数据

#### 隐藏数据

```bash
# ROW_ID
- 一个表没有手动定义主键，则会选取一个UNIQUE键作为主键来构建B+数
- 如果表中没有UNIQUE键，则会使用row_id作为主键构建数据

# TRANSACTION_ID
- 事务ID
```

#### 图示

![image-20230910164428204](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230910164428204.png)

### 3.2 DYNAMIC/COMPRESSED

#### 行溢出

- 一条记录中，假如一个字段为VARCHAR(65535)，已经超过了一个页的最大值(16kb)，就会产生行溢出的问题
- 在Compact和Redutant行格式中，对于占用内存较大的列，只会存储该列的一部分数据，剩余的数据分散在其他列
- 相当于保存了部分数据和其余数据的指针
- 不同的行溢出处理方式

## 4. 页上层

![image-20230724142950974](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230724142950974.png)

### 4.1 区

- 假如以页为分配单位，那么多个页之间可能是物理不连续的
- 如果进行范围查找，加载多个页的操作属于<font color=orange>随机I/O</font>，加载比较慢
- 内存以区进行划分，该区内的所有页都是连续的，就属于<font color=orange>顺序I/O</font>，比较快

```bash
# 区(Extent)
- 一个区会分配  64个连续的页， 在文件系统中是一个连续分配的空间
- 一个区的大小是16KB*64=1MB
```

### 4.2 段

- 叶子结点和非叶子结点，保存在不同的段
- 不希望同一个段中，既有叶子结点，又有非叶子结点

```bash
# 一个索引
- 一个叶子结点段， 一个非叶子结点段

# 其他段
- 数据段，索引段，回滚段

# 段
- 一个段中包含若干个区，区与区之间物理不连续，逻辑连续
```

### 4.3 表空间(Tablespace)

- 是一个逻辑容器，表空间和段的关系是一对多
- 数据库由多个表空间构建：系统表空间，用户表空间，撤销表空间，临时表空间

# 索引使用

## 1. 分类

| 逻辑功能 | 物理实现   | 字段个数 |
| -------- | ---------- | -------- |
| 普通索引 | 聚簇索引   | 单列索引 |
| 唯一索引 | 非聚簇索引 | 联合索引 |
| 主键索引 |            |          |

```bash
# 普通索引
- 不加任何限制条件，只是为了提高查询效率
- 可以创建后任何数据类型上，其值是否唯一和非空，要由字段本身的完整性约束条件决定
- 建立索引后，可以通过索引进行查询

# 唯一索引
- 使用UNIQUE设置字段，就会自动添加唯一索引
- 该索引的值必须是唯一的，但允许有空值
- 一个表中可以有多个

# 主键索引
- 一种特殊的唯一性索引，在唯一索引的基础上增加了不为空的约束：NOT NULL + UNIQUE
- 一个表中只能包含一个主键索引：主键索引决定了数据的存储方式
```

## 2. 创建索引

### 2.1 创建表时

- <font color=orange> INDEX | KEY </font>： 同义词
- <font color=orange>index_name</font>： 索引名称，可选参数。默认是col_name为索引名
- <font color=orange>col_name</font>：需要创建索引的字段列
- <font color=orange>length</font>：可选参数，表示索引的长度，只有字符串类型的字段才能指定索引长度
- <font color=orange>ASC | DESC</font>：指定升序或者降序的索引值存储

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

### 2.2 现有表上

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

- <font color=orange>唯一索引，或者主键索引</font>
- 业务上具有唯一特性的字段：<font color=orange>即使是组合字段，也必须建成唯一索引</font>(阿里规范)
- 唯一索引影响了insert速度，但损耗可以忽略，明显提高查找速度(查找B+树，找到一条记录后，就不再查找其余的页了)

### 4.2 WHERE查询的字段

- 普通索引即可大幅提升数据查询的效率(可能搜索多个数据页，但不用扫全部页)

### 4.3 UPDATE/DELETE的WHERE条件列

- 数据按照某个条件进行查询后再进行UPDATE/DELETE， 对WHERE字段建立索引，就能大幅度提升效率
- 如果更新的字段是非索引字段，提升效率更加明显，因为不需要对非索引字段进行索引维护

### 4.4 GROUP BY/ORDER BY的字段

- 索引就是让数据按照某种顺序进行存储和检索
- 创建单列索引或者联合索引

### 4.5 DISTINCT字段

- 不加索引：全表扫描，将该字段在内存中去重
- 加索引：<font color=orange>B+树相邻去重更快</font>，同时查出的数据是<font color=orange>递增排序</font>

### 4.6 使用列的类型小的创建索引

- 类型大小：指该列的该类型表示的数据范围的大小
- 以整数为例，TINYINT, MEDIUMINT, INT, BIGINT, 占用的存储空间依次增加

```bash
# 数据类型越小
- 查询时的比较操作越快
- 索引占用的存储空间就越少，一个数据页能存放更多的数据记录，从而减少磁盘IO带来的性能损耗

# 对主键尤其重要
- 聚簇索引中会存储主键值，其他所有的二极索引的每个节点都会存储一份记录的主键值
- 主键使用更小的数据类型，就会节省更多的存储空间和更加高效的I/O
```

### 4.7 前缀索引

```bash
# 长字符串作为二级索引的缺点
- B+树的叶子结点需要存储该列的完整记录，占用较大的空间，查找索引数据页的IO会增大
- 长字符串的比较，也会耗费性能
```

```sql
# 前缀索引
- 截取字段前面的一部分内容建立索引
- 查找时虽不能精确定位到记录的位置，但是能定位到相应前缀所在的位置，然后再回表找到对应的主键，然后再来判断

# 截取多少
SELECT COUNT(DISTINCT(address)) / COUNT(*) from book;     # 查看选择度
SELECT COUNT(DISTINCT(LEFT(address，索引长度)) / COUNT(*) from book;     # 查看选择度
             
 # 阿里手册
 - 在varchar上建立索引时，必须指定索引长度，没有必要对全部字段建立索引，根据实际区分度来决定索引长度
 - 一般区分度达到90%即可
```

### 4.8 区分度高的列

- 列的基数：某一列中不重复数据的个数
- 区分度：列基数/记录行数，区分度越接近1，越适合做索引
- <font color=orange>联合索引把区分度高的列放在前面</font>

```sql
# 一般超过33%就算是比较高效的索引
SELECT COUNT(DISTINCt a)/COUNT(*) FROM t1;
```

### 4.9 使用最频繁的列，放在联合索引的左侧

- 可以较少的建立一些索引，同时，最左前缀原则，可以增加联合索引的使用率

### 4.10 多字段都要索引时，联合索引优于单值索引

### 4.11 多表Join操作注意

- 连接的表的数量尽量不要超过3张，每增加一张表就增加了一次嵌套的循环，数量级增长比较快，严重影响查询效率
- 对WHERE条件列创建索引
- 对于连接的字段创建索引，并且该字段在多张表中的类型必须一致

# 查询优化

- 物理优化：通过<font color=orange>索引</font>和<font color=orange>表连接方式</font>来进行优化
- 逻辑优化：通过<font color=orange>sql等价变换</font>提升查询效率

## 1. 索引实践

- 是否使用索引，是由优化器来进行选择
- 不是基于规则，而是基于cost开销，基于语义。
- sql语句是否使用索引，和数据库版本，数据量，数据选择度都相关

### 1.1 全值匹配最佳

- 当where后面有多个查询条件时，对多个字段建立联合索引，效率最高

```sql
EXPLAIN SELECT * FROM student WHRER age=10 AND address='xian' AND class_id='abcd';

# 创建的索引
CREATE INDEX idx_age ON student(age);
CREATE INDEX idx_age_address ON student(age,address);
CREATE INDEX idx_age_address_class_id ON student(age,address,class_id);        # 效率最高，最终会选择这个
```

### 1.2 最左前缀原则

- 联合索引中，最左优先，检索数据时，从联合索引的最左边开始匹配
- 如果左边的值不确定，那么无法使用此索引

```sql
# 联合索引
CREATE INDEX idx_age_address_class_id ON student(age,address,class_id); 

# 会使用到全部索引
EXPLAIN SELECT * FROM student WHRER age=10 AND address='xian' AND class_id='abcd';

# 不会使用到索引： 没有age字段
EXPLAIN SELECT * FROM student WHRER address='xian' AND class_id='abcd';

# 会使用索引一部分： 
EXPLAIN SELECT * FROM student WHRER age=10 AND class_id='abcd';
```

### 1.3 主键插入顺序

- 如果主键是顺序插入，但是相邻两个数据之间有空隙，则后续插入的数据可能要插入之前数据之间，就可能造成数据页的分裂，影响性能
- 插入的记录的主键值依次递增，让主键具有AUTO_INCREMENT，让存储引擎自己为表生成主键，而不是手动插入

## 2. 索引失效

### 2.1 计算，函数，类型转换

- 对where条件后的字段，如果进行计算，函数，类型转换(手动或自动)会导致索引失效

```sql
# 函数
EXPLAIN SELECT * FROM student WHRER name LIKE 'abc%';       # 正常使用索引
EXPLAIN SELECT * FROM student WHRER LEFT(name,3) = 'abc';   # 索引失效，全表遍历(mysql不知道这个函数是做什么) 

# 计算
EXPLAIN SELECT * FROM student WHRER student_no = 99;       # 正常使用索引
EXPLAIN SELECT * FROM student WHRER student_no+1 = 100;    # 索引失效，全表遍历(mysql不知道这个运算是做什么) 

# 类型转换: varchar类型字段
EXPLAIN SELECT * FROM student WHRER name='12345';       # 正常使用索引
EXPLAIN SELECT * FROM student WHRER name=12345;         # 先把name做一个隐式的类型函数
```

### 2.2 范围条件右边的列

- 根据搜索条件，先把等值匹配的列放在联合索引的前面，范围匹配的列放在联合索引的后面

```sql
CREATE INDEX idx_age_address_class_id ON student(address,age,class_id); 

# class_id对应的索引就会用不上
EXPLAIN SELECT * FROM student WHRER  address='xian' AND age>10 AND class_id='abcd';
```

### 2.3 不等于

```sql
CREATE INDEX idx_name ON student(name); 

EXPLAIN SELECT * FROM student WHRER name='erick';      # 正常使用索引
EXPLAIN SELECT * FROM student WHRER name!='erick';      # 正常使用索引    !=    <>
```

### 2.4 IS NULL

- is null可以使用索引， is not null会无法使用索引
- 最好在涉及数据表的时候，将字段设置为NOT NULL + DEFAULT约束

### 2.5 LIKE通配符以%开头

- like的通配符，如果第一个是%，索引就会失效
- 数据页搜索严禁左模糊或全模糊，如果需要就用搜索引擎如es来解决

### 2.6 OR前后存在非索引的列，索引失效

```sql
CREATE INDEX idx_name ON student(name); 

# 计划一：name=erick 可以使用索引， age=12只能全表扫，然后把两个结果union起来
# 计划二：还不如一次性扫全表
EXPLAIN SELECT * FROM student WHRER name='erick' OR age=12;
```

### 2.7 数据库和表的字符集统一使用utf8mb4

- 统一使用字符集，不同的字符集相互比较时，会先进行类型转换，从而导致索引失效

## 3. 关联查询

### 3.1 外连接

- 连接条件，一旦数据类型不同，就存在隐式转换，就可能存在索引失效

```sql
# 左外连接和右外连接可以互相转换，因此以左外连接为例
# 假设student中包含20条数据，book中包含100条数据
EXPLAIN SELECT SQL_NO_CACHE * FROM student LEFT JOIN book ON student.id=book.id;


# 1. 不加索引，
-- 从student中取一个数据，book中把100条数据全拿出来，然后一一比较；
-- 继续student的下一个数据，一共遍历20次


# 2. 为被驱动表book的id加索引
-- 从驱动表student中取一个数据，被驱动表利用索引快速找到id匹配的
--  继续student的下一个数据，一共遍历20次
```

### 3.2 内连接

- 如果表的连接条件中只能有一个字段有索引，则有索引的字段所在的表会被优化器作为被驱动表
- 如果两个表的连接条件都有索引，则小表驱动大表

```sql
# 由查询优化器来决定谁作为驱动表，谁作为被驱动表
# 驱动表一般是全表扫描，被驱动表一般是索引扫描
EXPLAIN SELECT SQL_NO_CACHE * FROM student JOIN book ON student.id=book.id;
```

### 3.3 JOIN原理

#### 驱动表/被驱动表



#### 简单嵌套循环连接



#### 索引嵌套循环连接



#### 块嵌套循环连接



#### HASH JOIN



## 4. 子查询优化

- 子查询可以一次性完成很多逻辑上需要多个步骤才能完成的SQL操作

### 4.1 效率低

- 执行子查询时候，mysql需要为内层查询的结果<font color=orange>建立一个临时表</font>，然后外层查询语句从临时表中查询记录。查询完毕后，再<font color=orange>撤销这些临时表</font>。消耗过多的CPU和IO资源，产生大量的慢查询
- 子查询的结果存储的临时表，不管是内存临时表还是磁盘临时表，都<font color=orange>不会存在索引</font>，查询性能低
- 返回结果集越大的子查询，其对查询性能的影响就越大

### 4.2 替代方案

- 用关联查询代替子查询。关联查询不需要建立临时表，速度比子查询要快

## 5. 排序优化

- MYSQL中支持两种排序方式：FileSort和INDEX排序
- Index排序：索引可以保证数据的有序性，不需要再进行排序，效率更高
- FileSor排序：一般在内存中进行排序，占用CPU较多。如果待排结果较大，会产生临时文件I/O到磁盘进行排序的情况，效率较低

```bash
# 优化建议
- order by字句中使用索引，避免全表扫描，从而带来的FileSort排序
- 无法使用INDEX时，需要对FileSort方式进行调优
```



## 6. 分组优化



## 7. 分页优化







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

- 记录MySQL中<font color=orange>相应时间超过阈值</font>的SQL，具体值运行时间超过<font color=orange>long_query_time</font>值的SQL，则会被记录到慢查询日志中
- 记录执行时间长的<font color=orange>SQL查询</font>，并针对性的优化，提高系统整体效率。当数据服务器发生阻塞，运行变慢，通过检查慢SQL日志，定位到慢查询来解决
- 默认<font color=orange>不开启慢查询日志</font>，需要手动开启。<font color=orange>除非调优需要，一般不建议启动该参数</font>，因为开启慢查询日志会对性能带来一定影响

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

# 修改阈值: 1s    默认是10s
SET long_query_time = 1;

# 建议这些配置在mysql服务器中添加
```

### 2.2 分析

- mysqldumpslow -help ;
- 能够得到具体的执行过，对应慢查询的sql，后续再用EXPLAIN来进一步分析该sql

# Explain

- 定位到慢查询的SQL后，使用EXPLIAN或者DESCRIBE工具做针对性的分析查询语句
- EXPLAIN可以查看mysql对一条sql进行优化后的具体的执行计划是什么
- Explain并没有真正执行后面的SQL语句，因此可以安全的查看执行计划

```sql
EXPLAIN / DESCRIBE sql;
```

## 1. 数据准备

```sql
# 加入十万条记录

# 表1
CREATE TABLE student
(
    id          INT AUTO_INCREMENT,
    name        VARCHAR(100),
    id_no       INT,
    address     VARCHAR(100),
    first_info  VARCHAR(100),
    second_info VARCHAR(100),
    third_info  VARCHAR(100),
    other       VARCHAR(100),
    PRIMARY KEY (id),
    INDEX name_index (name),
    UNIQUE INDEX id_no_index (id_no),
    INDEX address_index (address),
    INDEX multi_info_index (first_info, second_info, third_info)
);

# 表2
CREATE TABLE girl
(
    id          INT AUTO_INCREMENT,
    name        VARCHAR(100),
    id_no       INT,
    address     VARCHAR(100),
    first_info  VARCHAR(100),
    second_info VARCHAR(100),
    third_info  VARCHAR(100),
    other       VARCHAR(100),
    PRIMARY KEY (id),
    INDEX name_index (name),
    UNIQUE INDEX id_no_index (id_no),
    INDEX address_index (address),
    INDEX multi_info_index (first_info, second_info, third_info)
);
```

## 2. 属性值

### 2.1 table

- 具体查询的哪张表
- 一个sql中，对几个表进行查询，就会出现几个表

![image-20230911152806371](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911152806371.png)

### 2.2 id

- 在一个大的查询语句中，每个SELECT关键字都会对应一个唯一的ID
- id如果相同，可以认为是一组，从上往下执行
- 所有组中，id值越大，优先级越高，越先执行
- id每个独立的号码(不同)，表示一趟独立的查询，一个sql的查询趟数越少越好

#### 子查询优化

- 子查询涉及了另外一个表，但是唯一id只有一个，是因为mysql对子查询进行了优化

![image-20230911153409946](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911153409946.png)

```sql
# 子查询
EXPLAIN SELECT * FROM student WHERE id IN (SELECT id FROM girl);

# 子查询效率低，优先会使用多表连接的方式
EXPLAIN SELECT * FROM student WHERE id IN (SELECT id FROM girl);
```

#### UNION

- 会有三个id，因为涉及到了union去重，会创建一个临时表

![image-20230911154026034](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911154026034.png)

#### UNION ALL

- 不需要去重，所以只有两个id

![image-20230911154121333](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911154121333.png)

### 2.3 select_type

- 查询属性，确定一个小查询在大查询中扮演的角色

#### simple

- 查询语句中，不包含union或者子查询的都是 simple类型

![image-20230911154929718](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911154929718.png)

#### primary

- 包含了union，union all,  子查询的大查询，最左边的大的查询就是PRIMARY

#### union

- union的第二个查询

#### union result

- union all的结果集合

![image-20230911154952064](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911154952064.png)

![image-20230911155013425](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911155013425.png)

### 2.4 type

- mysql对某个表的执行查询时候的访问方法，比较重要

```bash
# 从上到下，性能依次降低
system --> const -- >eq_ref --> ref --> fulltext --> ref_or_null --> index_merge
unique_subquery --> index_subquery --> range --> index --> all

# sql优化，至少要达到range级别，要求是ref，最好是const
```

#### system

- 表中只有一条记录，并且该表使用的存储引擎的统计数据是精确的
- MyISAM会有一个记录，专门记录一个表中的行数

![image-20230911160053316](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911160053316.png)

- InnoDB则不会记录, 是会去表中进行全表扫描

![image-20230911160222354](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911160222354.png)

#### const

- 根据主键或者唯一二级索引列，与常数进行等值匹配时，对单表的访问方式
- 因为数据的唯一性，找到一个数据后就不会再往后面找了

![image-20230911160435784](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911160435784.png)

![image-20230911160830806](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911160830806.png)

#### eq_ref

- 连接查询时，如果被驱动表是通过主键或者唯一二级索引列等值匹配的方式进行并访问，则被驱动表的访问方式就是eq_ref

![image-20230911161406008](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911161406008.png)

#### ref

- 通过普通二级索引列等值匹配的方式进行并访问，访问方式就是ref

![image-20230911164316794](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911164316794.png)

#### ref_or_null

- 普通二级索引等值匹配时，该索引列可能为null

![image-20230911164438758](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911164438758.png)

#### index_merge

- 多个查询列，都有索引，并且是or的关系

![image-20230911164647140](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911164647140.png)

#### range

- 根据某个索引，获取区间范围的值

![image-20230911164841083](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911164841083.png)

#### all

- 全表扫描

![image-20230911165048466](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230911165048466.png)

### 2.5 possible_key

- 可能用到的索引

### 2.6 key

- 实际使用到的索引
- 查询优化器，会在possible_key中选取一个，进行查找

### 2.7 key_len

- 实际使用到的索引长度(字节数)
- 可以判断是否充分的利用了索引，数值越大越好
- 主要针对联合索引

## 3 rows

- 预估的查询出来的结果的行数

## 4. filtered

- 从对应的行数中，会有多少百分比的数据满足记录
- 数值越高越好

## 5. Extral

