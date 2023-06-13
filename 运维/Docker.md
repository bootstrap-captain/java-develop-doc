# Dockerfile

- 用来构建docker镜像的文本文件，是由一条条构建镜像所需要的指令和参数构成的脚本文件

## 1. 简介

### 1.1 基础知识

- 每条保留字指令必须为大写字母，后面要至少跟随一个参数
- 指令按照从上到下，顺序执行
- \# 表示注释
- 每条指令都会创建一个新的镜像层并对镜像进行提交

### 1.2 构建流程

- docker从基础镜像运行一个容器
- 执行一条指令并对容器做出修改
- 执行类似docker commit的操作提交一个新的镜像层
- docker再基于刚提交的镜像运行一个新容器
- 执行dockerfile中的下一条指令直到所有指令执行完毕

### 1.3 角色

- Dockerfile：软件的原材料， 面向开发
- Docker镜像： 软件的交付品，交付标准
- Docker容器：软件镜像的运行台，部署与运维

## 2. 保留字

```bash
# 基础镜像：当前新镜像是基于哪个镜像的，指定一个已经存在的镜像作为模板
# 第一条必须是FROM
FROM

# 镜像维护者的姓名和邮箱地址
MAINTAINER

# 等同于在终端操作的shell命令，容器构建时需要运行的命令
# 两种格式： shell格式和exec格式
# RUN 是在docker build时运行
RUN<命令行命令>

# 当前容器对外暴露出的端口
EXPOSE

# 指定在创建容器后，终端默认登录的进来工作目录，一个落脚点
WORKDIR

# 指定该镜像是以什么样的用户去执行，默认为root，权限相关
USER

# 配置环境变量
# 可以在后续的任何RUN指令中使用，
ENV

# 将宿主机目录下的文件拷贝进镜像，且会自动处理URL和解压tarr压缩包
ADD

# 将宿主机目录下的文件拷贝进镜像
COPY

# 容器数据卷，用于数据保存和持久化工作
VOLUME

# 指定容器启动后要做的事情
# Dockerfile中可以有多个CMD指令，但只有最后一个生效
# CMD会被docker run之后的参数替换， 为用户提供了自定义参数
CMD

# 指定容器启动时需要的命令
# 类似CMD，但ENTRYPOINT不会被docker run后面的命令覆盖
# 可变参数时： CMD就是给ENTRYPOINT传参
ENTRYPOINT
```

# Docker微服务

## 1. 本地服务打包

### 1.1 服务依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.erick</groupId>
    <artifactId>guli-mall</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <spring-boot>2.7.3</spring-boot>
    </properties>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.3</version>
    </parent>


    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>

    <build>
        <!-- ......用于扫描 dao 文件下的mapper 文件................. start -->
        <resources>
            <!-- 编译 src/main/java 目录下的 mapper 文件 -->
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.xml</include>
                </includes>
                <filtering>false</filtering>
            </resource>

            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*</include>
                </includes>
            </resource>
            <resource>
                <directory>src/main/resources</directory>
                <includes>
                    <include>**/*</include>
                </includes>
            </resource>
        </resources>
        <!-- ......用于扫描 dao 文件下的mapper 文件................. end -->

        <plugins>
            <!--maven package的插件-->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <!--添加配置跳过测试-->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.1</version>
                <configuration>
                    <skipTests>true</skipTests>
                </configuration>
            </plugin>
            <!--添加配置跳过测试-->
        </plugins>
    </build>

</project>
```

### 1.2  maven打包

- 打包得到对应的jar包
- 打包后后，通过java -jar 来验证是否可以本地正常启动
- 默认端口为8080

![image-20220831170740333](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220831170740333.png)

### 1.3 Dockerfile

```bash
# 具体的版本信息，可以从dockerhub镜像上找
# 基础镜像是该jdk,将该jdk安装在docker中
FROM openjdk:17.0-jdk

# 将该jar包文件，拷贝到容器中，并更名
# 是因为后续会将Dockerfile和对应java包放在服务器同一个目录下
COPY guli-mall-1.0-SNAPSHOT.jar guli-erick.jar

# 按照java -jar guli-erick.jar来运行
ENTRYPOINT ["java","-jar","guli-erick.jar"]
```

## 2. 服务器构建

### 2.1 上传文件

- 将对应的jar包和Dockerfile上传到阿里云服务器的同一个目录下

```bash
 # mac的sftp功能
 - put source target   # 上传
 - get source target   # 下载
```

### 2.2 构建镜像

```bash
# 进入到阿里云服务器Dockerfile和jar包的目录
# nike-guli:     对应的docker image的名字
# 1.0:           版本号
# .  :           不能缺少, 代表在当前目录下
docker build -t nike-guli:1.0 .
```

![image-20220831181610088](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220831181610088.png)

### 2.3 运行容器

```bash
# 容器里面启动的是8080端口，9090是对外访问的端口
docker run --name balance-service -d -p 9090:8080 51b6019f5c15

# 访问路径
http://60.205.229.31:9090/user/date
```

# Docker-Compose

## 1. 简介

- Docker官方的开源项目，负责实现对Docker容器集群的快速编排
- 管理多个Docker容器，组成一个应用。同时要注意各个组件的启动顺序
- 定义一个docker.compose.yaml， 写好多个容器之间的调用关系。通过一个命令，同时启动/关闭这些容器

## 2. 常用命令

```bash
docker-compose -version               # 查看版本

docker-compose up                     # 启动所有docker-compose服务
docke-compose up -d                   # 启动所有docker-compose服务并后台运行
dokcer-compose down                   # 停止并删除容器，网络，卷，镜像
docker-compose restart/start/stop     # 重启/启动/停止
```

