# 安装JDK17/8

## 1. 下载

- [官网下载](https://www.oracle.com/java/technologies/downloads/#java17)

![image-20220901003504136](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220901003504136.png)

## 2. 安装

```bash
# 在阿里云服务器上
cd usr/local
mkdir java17

#  使用mac的sftp上传文件到
put /Users/shuzhan/Desktop/jdk-17_linux-x64_bin.tar.gz /usr/local/java17

# 解压文件
tar -zxvf jdk-17_linux-x64_bin.tar.gz 
```

## 3. 配置环境变量

```bash
vim /etc/profile

# 找到export PATH USER LOGNAME MAIL HOSTNAME HISTSIZE HISTCONTROL，在下面写上：

#set java environment
export JAVA_HOME=/usr/local/java17/jdk-17.0.4.1
export CLASSPATH=.:$JAVA_HOME/lib
export PATH=$PATH:$JAVA_HOME/bin:$JAVA_HOME/jre/bin
```

![image-20220901004521435](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220901004521435.png)

## 4. 刷新环境变量

```bash
# 刷新环境变量
source /etc/profile

java -version

java version "17.0.4.1" 2022-08-18 LTS
Java(TM) SE Runtime Environment (build 17.0.4.1+1-LTS-2)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.4.1+1-LTS-2, mixed mode, sharing)

# 如果切换了java 版本
- 需要重新打开terminal，不然可能出现查看java -version可能没改过来
```

# MYSQL

## 1. 单机版

- 环境：CentOS7.6，MySQL 8.0.27
- [MySQL社区版](https://downloads.mysql.com/archives/community/)

![image-20230731163537301](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230731163537301.png)

### 1.1 上传文件

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

### 1.2 CentOS7检查MySQL依赖

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

### 1.3 安装

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

### 1.4 服务初始化

- 为了保证数据库目录与文件的所有者为mysql登录用户，如果是以root身份运行mysql服务，需要执行命令初始化

```bash
# 默认为root用户生成一个密码并将该密码标记为过期，登录后需要设置一个新的密码
# 生成的临时密码会保存在 /var/log/mysqld.log
mysqld --initialize --user=mysql
cat /var/log/mysqld.log   # -CaBeiZa>1)k

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

### 1.5 登录

```bash
# 输入密码
mysql -u root -p

# 执行任意sql，则报错
ERROR 1820 (HY000): You must reset your password using ALTER USER statement before executing this statement.

# 先修改密码
alter user 'root'@'localhost' identified by 'erick_123456';

# 退出mysql，重新使用新密码登录
show databases;
```

### 1.6 远程连接

```sql
# 默认的root用户，只能是localhost来访问
USE mysql;
select host, user from user;

# 修改一下
UPDATE user SET host='%' WHERE user='root';

# 一定要刷新一下
FLUSH privileges;
```

# 安装**Maven3.6.3**

## 1. 下载

- [官网下载](https://archive.apache.org/dist/maven/maven-3/3.6.3/binaries/)

## 2. 上传安装

- Maven依赖Java环境，因此必须先下载Java并配置环境变量

```bash
# 上传到usr/local目录下并解压
put /Users/shuzhan/Desktop/apache-maven-3.6.3-bin.tar.gz /usr/local
tar -zxvf apache-maven-3.6.3-bin.tar.gz

# 查看版本， 出现下面代表安装成功
/usr/local/apache-maven-3.6.3/bin/mvn -v


Maven home: /usr/local/apache-maven-3.6.3
Java version: 17.0.4.1, vendor: Oracle Corporation, runtime: /usr/local/java17/jdk-17.0.4.1
Default locale: en_US, platform encoding: ANSI_X3.4-1968
OS name: "linux", version: "3.10.0-957.21.3.el7.x86_64", arch: "amd64", family: "unix"
```

## 3. 镜像配置

-  修改maven的对应的setting.xml

```
<mirror>
      <id>alimaven</id>
      <name>aliyun maven</name>
      <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
    <mirrorOf>central</mirrorOf>
 </mirror>
```

#  Git安装

```bash
yum install git

git --version

git version 1.8.3.1
```
