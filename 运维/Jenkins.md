# Jenkins安装

- Jenkins是一个war包，因此服务器需要先安装JDK

- Jenkins需要用Maven进行打包，所以服务器需要先下载Maven

- Jenkins需要用到git拉代码，因此需要先下载Git

- CICD: continuous integration, continuous deploy 

## 1.  下载

- [官网war包](https://www.jenkins.io/download/)
- version：2.361.2

```bash
# 1. 安装环境
- Centos 7.6

# 2. 将对应war包上传到Linux,
put /Users/shuzhan/Desktop/jenkins.war /usr/local
```

## 2. 启动

```bash
# 3. 后台启动Jenkins
#  & 表示后台启动     nohup 是centos的后台启动的指令
# >jenkins.log： 在命令执行的目录下，生成jenkins.log文件，并将启动日志写入
# >  >> 文件写入流的两种方式    > 覆盖              >> 追加
#  2>&1     2表示错误输出，1表示标准输出      错误输出和标准输出都写进来
# 如果不写输出日志，则命令会卡住，按回车后会将日志默认写入到nohup.log中
nohup java -jar jenkins.war --httpPort=8081 >jenkins.log 2>&1 &

# jps查看是否启动成功
jps
```



```bash
#  如果出现下面错误，是因为缺少字体的原因
yum install fontconfig
fc-cache --force
```

![image-20220901014435411](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220901014435411.png)

## 3. 访问

```bash
# 通过浏览器访问, 第一次进入需要admin的密码
http://119.23.242.170:8081/

# 根据红字提示查看密码
# 输入密码，等待响应，第一次加载时间会比较慢

# 其他安装
- 按照提示安装指定插件
- 创建admin用户
```

![image-20220901015413967](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220901015413967.png)

## 4. Maven集成

### 4.1  Maven Integration插件

- 项目通过识别pom文件，用maven来打包成jar包

![image-20220831224136831](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220831224136831.png)

### 4.2 配置Maven路径

- Dashboard---Global Tool Configuration
- 手动配置Maven路径,  Maven为本地服务器安装的Maven
- 如果识别不到，系统会自动提示

![image-20220901030331370](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220901030331370.png)

# Maven Item 构建

## 1. 项目构建

-  构建对应的Maven项目--- New Item

![image-20221022130726718](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221022130726718.png)

```bash
# 构建完成后，会在下面创建目录
# code example:  是项目的名称，也是对应下面包名,可以改名
# job下存放不同的item,  包含 builds, config.xml
# 存放的比如构建结构，下次build的number，本次build的相关参数等

/root/.jenkins/jobs/code example
```

## 2. 构建信息

### 2.1 General

![image-20221022130921999](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221022130921999.png)

### 2.2 Source Code

#### 开源无需密钥

```bash
# Repository URL: 会通过添加的git的地址，来测试是否拉取代码
# 可以集成gitee，gitlab，github等代码托管仓库
# 如果是开源的项目，不用配置对应的credentials
- 可以通过http或者ssh的方式来拉取代码，就和本地IDEA拉取代码一个套路

# Branch
- 该item需要执行的哪个branch，根据自己的github分支来决定
```

![image-20221022131012951](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221022131012951.png)

#### 普通用户密码凭证

- 适用于私人repo，通过提供对应的git仓库的用户名和密码来拉取代码
- 拉取代码：http的方式，https://gitee.com/daydreamer9451/boot-cloud-poc.git

![image-20221025163132806](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221025163132806.png)

![image-20221025163207851](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221025163207851.png)

#### SSH私钥凭证

```bash
# 进入Jenkins服务器，已经安装了git，生成公私钥对
ssh-keygen -t rsa

# 在github上公钥地方添加 id_rsa.pub对应的内容

# 在jenkins上添加私钥的内容 id_rsa
- 用户名为生成公私钥的时候的root账户
```

![image-20221025164804585](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221025164804585.png)

![image-20221025164945057](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221025164945057.png)

### 2.3 Pre Steps

- 在构建jar包前可以做的工作

![image-20220831230008051](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220831230008051.png)

### 2.4 Build

- 需要在Jenkins服务器上安装Maven, 从而使Jenkins拉取的代码能够通过Maven解析
- 注意对应pom.xml的路径: 对应项目所需要识别的路径
- 是以对应的git url打开后的路径作为根路径识别pom，如果pom存在嵌套，则更改即可
- 如果是聚合项目，则只要提供父项目的pom即可

![image-20220831230534172](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220831230534172.png)

### 2.5  Post Steps

- 可以把构建完成后的jar包，发送到对应的服务器上，并运行

![image-20220831233324117](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220831233324117.png)

### 2.6 打Jar包

- 会将项目从git拉取过来，然后用maven进行编译，同时下载对应的依赖，就类似于本地用maven编译一样，目录结构也是和本地类似

![image-20220901030835564](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220901030835564.png)

![image-20220901030908851](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220901030908851.png)

```bash
# 最终打包好的jar文件, 在上面目录中
/root/.jenkins/workspace/code-example/code-example/target/code-example-1.0-SNAPSHOT.jar

# Jenkins目录： 下面包含git中所有的文件： src,编译完成后的target, pom.xml， 等其他还有的目录
/root/.jenkins/workspace/code-example/code-example
```

#### 异常： 找不到pom

```bash
# 进入到//root/.jenkins/workspace/code-example/code-example目录下查看通过git拉取下来的代码,然后修改对应的pom路径
ERROR: No such file /root/.jenkins/workspace/code-example pom.xml
```



# Jenkins发布Jar包

- 准备一台阿里云服务器作为测试环境安装jdk17

## 1.  SSH发布Jar包

### 1.1 Publish Over SSH插件

- 在Jenkins上安装Publish Over SSH， 从而可以通过SSH将jar包发送到测试环境服务器上

### 1.2 新建SSH-Server

- 可以通过用户名密码来链接，也可以通过其他证书类的链接
- 新建后，可以通过test connection来验证链接是否成功
-  Remote Directory: 默认发送文件过去是到root目录下

![image-20220901170612004](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220901170612004.png)

### 1.3 Item配置-Post Steps

- 打包完成后的后置操作，通过SSH发送文件到指定服务器

```bash
# 下面的目录： 是Jenkins打包的默认目录: 
# code-example： 是根据job名来定的，一旦第一次构建完成， 后续job更名，但是该目录名不会变化
/root/.jenkins/workspace/code-example/code-example/target/code-example-1.0-SNAPSHOT.jar


# source files:  会将target/code-example-1.0-SNAPSHOT.jarr发送过去
- 以workspace作为根目录，可以通过匹配方式来获取到指定的文件
**/code-example*.jar
#  Remove Prefix: 默认前缀是workspace的项目名下的目录结构： 比如上面就是 code-example/target
code-example/target
 
# Remote directroy: 默认发送到测试服务器的root目录, 可以在对应的ssh-server中配置
# 如果没有erick，则会去创建这个目录
- erick
- 传输过去的目录路径： /root/erick

# Exec command:
- 文件发送完成后，执行的对应操作

# 最终发送到测试服务器的目录
/root/erick/code-example-1.0-SNAPSHOT.jar
```

![image-20220904113014050](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220904113014050.png)

## 2. Jar包运行

### 2.1 测试服务器运行

```bash
# 1.直接启动， 但是关闭terminal或者 ctrl+c就会终止进程
java -jar code-example-1.0-SNAPSHOT.jar

# 2.后台启动      & 表示在后台执行
nohup java -jar /root/erick/code-example-1.0-SNAPSHOT.jar &

# 执行上面指令，会卡住，按回车才会退出，并把日志输出到nohup.out中
# 会在执行该指令的目录下，创建一个新的日志文件 nohup.out
nohup: ignoring input and appending output to 'nohup.out'


## 3. 杀死进程
# jps 查看java进程： 第一个是实际运行的java的项目，第二个是jps查询时候的进程
3552 Jps
3538 code-example-1.0-SNAPSHOT.jar

# 杀死进程
kill -9 3538
```

### 2.2 Jenkins中执行

```bash
# 如果通过下面指令在 Jenkins中执行
# 因为要回车，所以Jenkins会认为任务还在执行， 就会任务一直没完成，最终导致任务超时
nohup java -jar /root/erick/*.jar &   

# 指定日志文件写入，就不会卡住，
nohup java -jar /root/erick/*.jar  >code-example.log 2>&1 &

#  对应的脚本： 必须加上上面一行，否则shell命令不会执行，可能是获得权限: . /etc/profile 
. /etc/profile 
nohup java -jar /root/erick/*.jar  >code-example.log 2>&1 &
```

## ![image-20221023140018841](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221023140018841.png)3. 运行前清理

- 通过shell脚本启动的服务，要先杀死掉之前的应用，再启动新的应用，否则就会引发端口冲突问题

### 3.1 控制台查看进程

```bash
# 1.查看java进程
ps -ef | grep code-example-1.0-SNAPSHOT

# 包含两个进程： 一个是运行的account对应的java进程， 一个是用jps查看的进程
root     26599     1  6 13:59 ?        00:00:08 java -jar /root/erick/code-example-1.0-SNAPSHOT.jar
root     26688 24420  0 14:02 pts/0    00:00:00 grep --color=auto code-example-1.0-SNAPSHOT

# 2. 两种过滤手段
# 2.1 过滤掉grep的进程: -v代表过滤
ps -ef | grep code-example | grep -v grep
# 2.2  筛选出java和account的进程
ps -ef | grep code-example | grep 'java -jar'

root     26599     1  4 13:59 ?        00:00:08 java -jar /root/erick/code-example-1.0-SNAPSHOT.jar

# 3. 获取端口： awk， 可以将对应的第二个字符获取，也就是30634， 游标是从1开始
ps -ef | grep code-example | grep 'java -jar' | awk '{printf $2}'

3659[root@dayDreamer erick]#
```

### 3.2 Shell脚本入门

```bash
# 在/root/erick下创建 clear.sh
touch clear.sh

#  编写对应的脚本，授权 测试
chmod 777 clear.sh
./clear.sh 
```

```bash
# 一般都会写，不写也可以
#!/bin/bash

# 将一个信息，输出到对应的文本中
echo "hello word">1.txt
```

```bash
#!/bin/bash
# delete jar files
rm -rf *.jar

# get the first arguments passed
projectname=$1

# get java pid
pid=`ps -ef | grep $projectname | grep 'java -jar' | awk '{printf $2}'`

if [ -z $pid ];
    then 
         echo "$projectname not started"
    else
         kill -9 $pid
         echo "$projectname stoping..."
fi
```

### 3.3 Jenkins触发-Pre Steps

- 在构建Jar包之前，传输文件之前，先去执行的操作
- 也是通过Publish Over SSH插件来完成

```bash
./etc/profile
cd /root/erick                # 需要先进入到这个目录，才能删除对应下面的文件， 但是其中的ps进程不进入也是可以的
./clear.sh code-example       # code-example是传入的项目名称
```

![image-20221023141556065](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221023141556065.png)

# Jenkins Pipeline

## 1. 简介

- 使用Pipeline， 可以将上面所有的UI操作，转换为代码，就想docker的dockerfile一样
- Jenkinsfile一般作为源代码提交到git仓库

```bash
# pipeline组成
- pipeline： 整条流水线
- agent： 指定执行器
- stages： 所有阶段
- stage： 某一阶段，可以有多个
- steps： 阶段内的每一步，可执行命令
```

## 2. 基本构建

### 2.1 Pipeline

![image-20220904142742980](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220904142742980.png)

### 2.2 脚本直接构建

- 可以通过直接在consule里面写pipeline的脚本

```bash
# 可以从外面定义一些常量
pipeline {
    # 具体交给哪个Jenkins节点执行， any表示由Jenkins自主分配
    agent any
    
    stages {
     # 环节中的一个环节
        stage('get code from git') {
            # 其中的一个步骤
            steps {
                echo 'code get successfully'
            }
        }
        stage('execute build') {
            steps {
                echo 'build successfully'
            }
        }        
    }
}
```

- 可以看到每个步骤的执行时间，每个步骤的对应的log等
- 可以对指定的stage运行，比去拉取代码拉取到了，就不需要再执行了

![image-20220904143654963](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220904143654963.png)

![image-20220904143905809](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220904143905809.png)

![image-20220904144026077](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220904144026077.png)

### 2.3. Blue Ocean

- 提供了一个更加漂亮的Pipeline构建的Web界面
- 安装Blue Ocean 插件

![image-20220904144238599](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220904144238599.png)

![image-20220904144441227](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220904144441227.png)

## 3. Pipeline脚本构建

### 3.1 代码生成器

- 下拉选项，是根据装的插件来决定的， 使用Pipeline Syntax来快速生成代码

![image-20220904144856154](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220904144856154.png)

![image-20220904144911431](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220904144911431.png)

### 3.2 代码拉取

- 利用代码生成器生成

```bash
# 对应的pipeline的脚本
git branch: 'main', url: 'https://gitee.com/daydreamer9451/nike-shoe.git'
```

![image-20220904145125543](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220904145125543.png)

### 3.3  Maven构建

```bash
pipeline {
    agent any

   # 定义maven配置的名字，具体要用哪个maven，在jenkins中已经声明过
    tools {
        maven "Linux-Maven-Jenkins"
    }
    
    stages {
        stage('Archive Source Code') {
            steps {
               git branch: 'main', url: 'https://gitee.com/daydreamer9451/nike-shoe.git'
               echo "code pull finished"
            }
        }
        
        stage('Build Jar By Maven') {
            steps {
               sh """
               cd code-example
               mvn clean package
               """
            }
        }        
    }
}
```

```bash
# 目录结构问题：
1. Maven build的时候，默认是去/root/.jenkins/workspace/code-example-pipeline找的
  因此要去执行的时候，必须先进入到cd code-example找到对应的pom文件
```



### 3.4 SSH-Pubish

- 发送测试服务器，启动服务，前期清理工作
- 根据之前发送jar包的方式，在web界面操作，通过代码生成器帮我们来做

### 3.5 最终脚本

```bash

  pipeline {
    agent any

    tools {
        maven "Linux-Maven-Jenkins"
    }
    
    stages {
        stage('Get Source Code') {
            steps {
               git branch: 'develop', url: 'https://gitee.com/daydreamer9451/erick-code-example.git'
               echo "code pull finished"
            }
        }
        
        stage('Build Jar By Maven') {
            steps {
               sh """
               cd code-example
               mvn clean package
               """
            }
        }     
        
        stage('Clean Job'){
            steps{
                sshPublisher(publishers: [sshPublisherDesc(configName: 'dev-env', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''./etc/profile
cd /root/erick               
./clear.sh code-example      ''', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '', remoteDirectorySDF: false, removePrefix: '', sourceFiles: '')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false)])
                
            }
        }
        
        stage('Send Jar File'){
            steps {
                sshPublisher(publishers: [sshPublisherDesc(configName: 'dev-env', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '', remoteDirectorySDF: false, removePrefix: 'code-example/target', sourceFiles: '**/code-example*.jar')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false)])
            }
        }
        
        stage('Start Project'){
            steps{
                sshPublisher(publishers: [sshPublisherDesc(configName: 'dev-env', transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand: '''. /etc/profile 
nohup java -jar /root/erick/*.jar  >code-example.log 2>&1 &''', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '', remoteDirectorySDF: false, removePrefix: '', sourceFiles: '')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false)])
            }
        }
    }
}
```

![image-20221023145403519](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221023145403519.png)

## 4. Pipeline-Jenkinsfile构建

- 在代码中创建Jenkinsfile文件，上面的脚本都写在里面

![image-20220904155828617](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220904155828617.png)

![image-20221023150430142](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221023150430142.png)

# Jenkins发布Docker

## 1. DEV-环境

- 安装Docker环境

## 2. Jenkins

### 2.1  构建前清理工作

```bash
. /etc/profile 
rm -rf *                             # 删除account下的jar包和dockerfile
docker stop nike-shoe             # 停止容器
docker rm nike-shoe             # 删除容器
docker rmi nike-shoe-image        # 删除image

. /etc/profile 
rm -rf *  
docker stop nike-shoe             
docker rm nike-shoe             
docker rmi nike-shoe-image       
```

![image-20220903222550119](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220903222550119.png)

### 2.2 构建工作

Dockerfile发送和构建

```bash
# 在同一个发送Jar包的SSH下面，发送Dockerfile， 并构建镜像，然后启动
. /etc/profile 
docker build -t nike-shoe-image .
docker run --name nike-shoe -d -p 9000:9000 nike-shoe-image
```

![image-20220904002326567](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220904002326567.png)