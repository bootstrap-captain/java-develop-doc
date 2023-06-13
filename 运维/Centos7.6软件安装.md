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
