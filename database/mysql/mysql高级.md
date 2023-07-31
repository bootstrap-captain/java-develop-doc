# 安装

- 环境：CentOS7.6，MySQL 8.0.27
- [MySQL社区版](https://downloads.mysql.com/archives/community/)

![image-20230731163537301](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230731163537301.png)

## 1. 上传文件

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

## 2. CentOS7检查MySQL依赖

```bash
# 检查/tmp临时目录权限(必不可少)
- mysql在安装过程中，会通过mysql用户在/tmp目录下新建 tmp_db文件，所以请给/tmp较大权限
cd /
chmod -R 777 /tmp

# 检查存在libaio依赖
# 存在时候的结果： libaio-0.3.109-13.el7.x86_64
rpm -qa | grep libaio

# 检查存在net-tools依赖
# 存在时候的结果： net-tools-2.0-0.25.20131004git.el7.x86_64
rpm -qa | grep net-tools
```

## 3. 安装

- 是严格按照下面的rpm顺序来安装

```shell
cd opt/erick_mysql/
rpm -ivh mysql mysql-community-common-8.0.27-1.el7.x86_64.rpm

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
```

## 4. 检查安装情况

```bash
# mysql  Ver 8.0.27 for Linux on x86_64 (MySQL Community Server - GPL)
mysql --version

# mysqladmin  Ver 8.0.27 for Linux on x86_64 (MySQL Community Server - GPL)
mysqladmin --version
```

## 5. 服务的初始化

- 为了保证数据库目录与文件的所有者为mysql登录用户，如果是以root身份运行mysql服务，需要执行命令初始化

```bash
# 默认为root用户生成一个密码并将该密码标记为过期，登录后需要设置一个新的密码
# 生成的临时密码会保存在 /var/log/mysqld.log
mysqld --initialize --user=mysql
cat /var/log/mysqld.log   # Ge>kRZqFT1k2

# 启动mysql
systemctl start mysqld

# 查看是否启动
systemctl status mysqld

# 查看虚拟机启动后，mysql是否自启动：  enabled代表是
systemctl list-unit-files | grep mysqld.service

# 如果不是，设置为自启动
systemctl enable mysqld.service

# 关闭自启动
systemctl disable mysqld.service
```

## 6. 登录

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

