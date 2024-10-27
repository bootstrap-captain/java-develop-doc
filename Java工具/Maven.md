# 入门

## 1. 引入原因

### 1.1 Dependencies Management

```bash
# jar包规模
- 项目开发，需在项目中逐个加入jar包，麻烦
- 通过maven拉取jar包，并最终打包到对应项目的发布jar包中

# jar包来源
- 手动添加，需下载一个个jar包，可能存在奇奇怪怪的问题
- maven拉取jar包时，方便快捷规范

# jar包之间依赖关系
- 手动添加，数量庞大，彼此见存在错综复杂的依赖关系，人力不能手动解决，jar包间冲突
- 让maven来管理冲突
```

### 1.2 Build Management

```bash
# 传统开发
- 将单个.java文件通过 javac 编译成 .class文件
- 执行 测试代码
- 将代码打包上传到服务器部署
- 测试功能
   如此步骤，遇到bug，重复进行

# Maven
 - 自动化进行上述的所有的 批量编译，打包，部署
 - jar包依赖管理
```

## 2. 介绍

- Apache维护的，java开发的，为Java项目提供的工具。安装maven前必须先安装java
- Build Management:  上传到代码仓库的是java源代码，需要Maven进行代码拉取，清理原来class文件，重新compile，test，package，install，deploy
- Dependencies Management:  jar包下载，jar包依赖，jar包冲突

### 2.1 下载

- [官网下载](https://maven.apache.org/download.cgi)，目前最先版本用3.8.6
- Mac/Linux选用对应的tar.gz文件，windows选用对应的bin.zip文件
- 核心配置文件： maven/conf/setting.xml

![image-20221027092722683](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221027092722683.png)

```xml
<!--本地repo: 默认有一个路径，更改其依赖下载的最终路径-->
<localRepository>/Users/shuzhan/Documents/repo</localRepository>
```

#### 镜像配置

- 默认去中央仓库拉取jar包，需要对中央仓库配置一个镜像仓库
- 3.8.6版本的maven，对http类型的中央仓库进行了block，因此需要先删除掉用来的mirror

```xml
<mirrors>
    <!-- mirror
     | Specifies a repository mirror site to use instead of a given repository. The repository that
     | this mirror serves has an ID that matches the mirrorOf element of this mirror. IDs are used
     | for inheritance and direct lookup purposes, and must be unique across the set of mirrors.
     |
    <mirror>
      <id>mirrorId</id>
      <mirrorOf>repositoryId</mirrorOf>
      <name>Human Readable Name for this Mirror.</name>
      <url>http://my.repository.com/repo/path</url>
    </mirror>
     -->
    <mirror>
      <id>maven-default-http-blocker</id>
      <mirrorOf>external:http:*</mirrorOf>
      <name>Pseudo repository to mirror external repositories initially using HTTP.</name>
      <url>http://0.0.0.0/</url>
      <blocked>true</blocked>
    </mirror>
  </mirrors>
```

```xml
  <mirrors>
    <!--阿里的镜像地址-->
    <mirror>
      <id>alimaven</id>
      <name>aliyun maven</name>
      <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
```

#### IDEA中配置

- 项目中注意全局配置和项目配置
- Maven配置为刚才下载的maven路径，settings file使用刚才配置的文件，也可以自定义另外的setting文件

**当前项目**

![image-20221027095843614](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221027095843614.png)

**全局配置**

![image-20221027102134631](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221027102134631.png)

**导入多个项目**

![image-20221028113734951](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221028113734951.png)

![image-20221028113849072](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221028113849072.png)

### 2.2 坐标

- groupId: 域名反写，一般是公司或者组织的ID，一般会再加上项目
- artifactId: 项目中对应的某个工程名称
- version：项目版本号
- 使用唯一坐标，让maven能够找到对应的jar包，统一帮忙打包

```xml
 <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>1.18.22</version>
      <scope>provided</scope>
  </dependency>
```

### 2.4 目录结构

- Maven工程必须严格遵守约定的目录结构，否则无法运行
- 这种目录结构是在超级目录里面配置的(相当于所有POM的父POM)

#### 编译前

![image-20221027111950093](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221027111950093.png)

#### 编译后

![image-20221027120920581](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221027120920581.png)

### 2.5 常用命令

- 生命周期： compile, test, package, install, 执行后面命令时，会执行前面的
- 一般都是先clean再去执行后面的动作

```bash
# Jenkins服务器上
- 拉取到对应的java源码后，必须先进入到 pom.xml所在的目录，才能执行对应的  mvn -***

# 如果没进入到对应的目录
The goal you specified requires a project to execute but there is no pom in this directory
```

```bash
# mvn clean
- 删除target目录

# mvn compile
- 将src下面的业务代码和测试代码从 .java文件编译成 .class文件

# mvn test-compile
- 将测试代码进行编译，放在test-classes中

# mvn test
- 编译src下的业务代码和测试代码，并且执行test，并在surefire-reports中生成测试报告

# mvn package
- 执行上面所有步骤，并打包成预先定义好的jar包： artifactId-version-SNAPSHOT.jar
- 内容不会包含测试代码

# mvn install
- 执行上面所有步骤，并打包存入到本地仓库
- 将本项目的pom文件，重命名为jar包相同的，并存入同样的目录中

# mvn deploy
- 执行上面的步骤，并将对应的jar包上传到maven的linux私服上

# 可以使用组合命令
mvn clean install
```

### 2.6 生命周期

- default构建生命周期，clean不包含在这个生命周期内
- 每个指令执行的时候，都会执行前面所有的指令

![image-20221028114319577](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221028114319577.png)

## 3. 插件

- lifecycle：定义的是生命周期，是一个逻辑上的概念
- plugins：定义的是执行对应生命周期时候的具体操作底层

![image-20221029102155960](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221029102155960.png)

## 4. 仓库

- 存储资源，包含各种jar包
- 搜索时，在本地repo中，通过groupId/artifactId/version逐层目录去查找

```bash
# maven仓库会保存三类jar
- 安装到仓库的个人/公司项目

- 第三方包和框架包

- maven插件
```

```bash
# 中央仓库
- 由maven团队维护
- 默认是去国外网站拿
- 一般会配置中央仓库的镜像，建议不要配置多个镜像和中央仓库混用，可能获取到的jar包不一样

# 私服
- 公司内部的maven仓库,私服会从中央仓库(或中央仓库的镜像)拿，获取后并保存到私服
- 加快本地拉取jar包的速度
- 部分自主研发的产品具有知识产权（不应该上传到中央仓库）
  
# 本地仓库
- 本地local的仓库，拉取jar包后会保存到本地的repo中
```

## 5. pom.xml

### 5.1 基本标签

- Project Object Model: 项目对象模型
- maven项目必须包含一个pom.xml文件，从而获得整个项目的信息

```xml
<!--当前pom所采用的标签结构，从Maven2开始就固定是4.0.0-->
<modelVersion>4.0.0</modelVersion>

<!--jar： 默认，说明是Java工程
    war： 说明是war工程
    pom： 说明这个工程是用来管理其他工程的工程    
-->
<packaging>jar</packaging>

<!--在构建过程中读取源码时使用的字符集-->
<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
```

### 5.2 super pom

- 所有的pom.xml如果没有父pom，则都会使用maven本身提供的super pom，类似与Java的Object类
- 超级pom路径： apache-maven-3.8.6/lib/maven-model-builder-3.8.6.jar/   org/apache/maven/model/pom-4.0.0.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<!-- START SNIPPET: superpom -->
<project>
  <modelVersion>4.0.0</modelVersion>

  <repositories>
    <repository>
      <id>central</id>
      <name>Central Repository</name>
      <url>https://repo.maven.apache.org/maven2</url>
      <layout>default</layout>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
    </repository>
  </repositories>

  <pluginRepositories>
    <pluginRepository>
      <id>central</id>
      <name>Central Repository</name>
      <url>https://repo.maven.apache.org/maven2</url>
      <layout>default</layout>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
      <releases>
        <updatePolicy>never</updatePolicy>
      </releases>
    </pluginRepository>
  </pluginRepositories>

  <build>
    <directory>${project.basedir}/target</directory>
    <outputDirectory>${project.build.directory}/classes</outputDirectory>
    <finalName>${project.artifactId}-${project.version}</finalName>
    <testOutputDirectory>${project.build.directory}/test-classes</testOutputDirectory>
    <sourceDirectory>${project.basedir}/src/main/java</sourceDirectory>
    <scriptSourceDirectory>${project.basedir}/src/main/scripts</scriptSourceDirectory>
    <testSourceDirectory>${project.basedir}/src/test/java</testSourceDirectory>
    <resources>
      <resource>
        <directory>${project.basedir}/src/main/resources</directory>
      </resource>
    </resources>
    <testResources>
      <testResource>
        <directory>${project.basedir}/src/test/resources</directory>
      </testResource>
    </testResources>
    <pluginManagement>
      <!-- NOTE: These plugins will be removed from future versions of the super POM -->
      <!-- They are kept for the moment as they are very unlikely to conflict with lifecycle mappings (MNG-4453) -->
      <plugins>
        <plugin>
          <artifactId>maven-antrun-plugin</artifactId>
          <version>1.3</version>
        </plugin>
        <plugin>
          <artifactId>maven-assembly-plugin</artifactId>
          <version>2.2-beta-5</version>
        </plugin>
        <plugin>
          <artifactId>maven-dependency-plugin</artifactId>
          <version>2.8</version>
        </plugin>
        <plugin>
          <artifactId>maven-release-plugin</artifactId>
          <version>2.5.3</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>

  <reporting>
    <outputDirectory>${project.build.directory}/site</outputDirectory>
  </reporting>

  <profiles>
    <!-- NOTE: The release profile will be removed from future versions of the super POM -->
    <profile>
      <id>release-profile</id>

      <activation>
        <property>
          <name>performRelease</name>
          <value>true</value>
        </property>
      </activation>

      <build>
        <plugins>
          <plugin>
            <inherited>true</inherited>
            <artifactId>maven-source-plugin</artifactId>
            <executions>
              <execution>
                <id>attach-sources</id>
                <goals>
                  <goal>jar-no-fork</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
          <plugin>
            <inherited>true</inherited>
            <artifactId>maven-javadoc-plugin</artifactId>
            <executions>
              <execution>
                <id>attach-javadocs</id>
                <goals>
                  <goal>jar</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
          <plugin>
            <inherited>true</inherited>
            <artifactId>maven-deploy-plugin</artifactId>
            <configuration>
              <updateReleaseInfo>true</updateReleaseInfo>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>
  </profiles>

</project>
<!-- END SNIPPET: superpom -->
```

### 5.3 Effective Pom

- 一个项目的pom.xml，经过超级pom，父pom，当前pom，根据一定的优先级组合后，形成的最终的pom

# 依赖管理

## 1. scope

- 依赖的jar默认情况下，在任何地方都可以使用，可以通过scope标签来设定其作用范围

| scope            | 主代码是否能用 | 测试代码是否能用 | 打包是否包含该jar包                   | 示例                |
| ---------------- | -------------- | ---------------- | ------------------------------------- | ------------------- |
| complie(default) | Y              | Y                | Y                                     | log4j               |
| test             | N              | Y                | N(测试依赖的jar包不进入最终项目jar包) | junit               |
| provided         | Y              | Y                | N                                     | servant-api, lombok |
| import           |                |                  |                                       | 解决依赖单继承问题  |

```bash
# provided：
- 场景一：开发时候需要用该jar包，但是部署时，服务器已经有了，所以打包的时候不能带，不然可能冲突，比如jakarta.servlet-api
- 场景二：开发时候的注解，然后让编译时候进行动态生成好的 .class文件。jar运行时候已经不需要该依赖了，比如 lombok
```

## 2. 本地项目依赖传递

### 2.1 依赖传递

- 自己本地的多个项目(不管是不是父子项目)可互相依赖
- 依赖传递时候，必须install才可以(IDEA中不install时，编译不会出现问题，但运行时候就会出错。因此线上的必须先install)

![image-20221028093120625](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221028093120625.png)

### 2.2 仅传递compile

- A依赖B, B中不是compile的依赖不能传递到A
- B打包的时候，只有compile的会进行打包，所以才能传递 

![image-20221028094213087](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221028094213087.png)

## 3. version conflict

- 同一个项目中使用了同一个jar包的不同版本，导致冲突，使用的时候可能出现问题

```bash
# 抛异常
- java.lang.ClassNotFoundException:   编译过程找不到类
- java.lang.NoClassDefFoundError：    运行过程找不到类
- java.lang.LinkageError:             不同类加载器分别加载的多个类有相同的全限定类名

# 抛异常
- java.lang.NoSuchMethodError:  程序找不到预期的方法，多见于通过反射调用方法

# 没报错但结果不对
- 两个jar包中的类分别实现了同一个接口，但是因为没有注意命名规范，两个不同的类刚好是同一个名字
```

![](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230423141604705.png)

### 3.1 Maven仲裁机制

- Maven本身提供了仲裁机制，决定到底该采用哪个版本

#### a.最短路径

- 路径优先：依赖中出现相同的资源，资源层级越深，优先级越低
- 最终选择lombok2.0

![image-20221030195638273](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221030195638273.png)

#### b.路径相同，先声明优先

- 一个项目依赖其他几个项目，其他几个项目中都包含某个jar包，那么引入其他几个项目，先引用的生效

![image-20221030170215889](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221030170215889.png)

#### c.覆盖优先

- 同一个文件中配置了一个资源的不同版本，后配置的覆盖前面配置的

![image-20221030165348830](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221030165348830.png)

### 3.2 手动解决

- 多个三方包引入的不同版本，版本仲裁会指定一个版本的jar。

```bash
# 仲裁的最终结果：
- JVM在加载一个jar包的时候，如果加载了其中一个jar包，其他不同版本的该jar就不会再次加载

# 低版本： 
- 低版本可能存在安全漏洞，因此需要指定一个高版本

# 高版本：
- 高版本中可能删除了一些类，导致某个三方包运行不起来。 ClassNotFound Error, MethodNotFound

# 因此，在经过maven仲裁机制过后的某个jar版本，如果项目运行不起来，就需要手动改变该jar的版本
```

![image-20221030203750528](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221030203750528.png)

#### a.exclude

- 可以借助Maven Helper插件帮我们快速排除依赖

![image-20221030203856638](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221030203856638.png)

#### b.版本锁定

- 若某个包被很多个框架引入，那么exclude时，就需要一个个去排除，影响代码美观性能，因此可以在本工程中使用版本锁定
- **锁定版本法可以打破依赖传递的原则，优先级为最高**
- 也可以在父工程中来控制版本锁定
- 若父工程和子工程同时进行了版本锁定，子项目优先级高

![image-20230423131605590](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230423131605590.png)

- 版本锁定只针对传递的依赖，直接依赖如果指定版本，则版本锁定失效

![image-20230423131700641](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230423131700641.png)

### 3.3 依赖隐藏

- optional属性：B工程可以隐藏自己的依赖，不让A工程看到，不传递该依赖
- 默认为false，表示可以传递，true代表不传递
- 被依赖方解决依赖冲突

![image-20221028094740815](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221028094740815.png)

# 父子工程

- 父工程打包为pom，不写业务代码，只提供版本管理机制对应的pom.xml
- 子工程打包是jar
- 打包的时候，只需要在父工程一键install即可

```bash
# 父子项目最大优点
- 可以直接执行父工程的maven命令，父工程会自动根据项目模式，找到对应的其他子工程
- 执行maven命令时，比如package，只需在父工程中进行，聚合项目会自动对该父工程下所有项目进行一键package(父项目是自己的)
- 如果父项目是公司级别的其他项目，则该优点就失效了，只能进行版本控制
- 版本锁定也可以通过DM引入其他的对应的DM来进行管理，优先级比parent

# 尽量使用parent类型引入pom
- 这样可以使用到 properties
- DM可以进行依赖传递
```

## 1. 单继承-parent

- 一个子工程只能包含一个parent工程
- 子工程的version和groupId如果和父工程一样，则可以省略
- 父工程version一处修改，各个子工程全部生效，子工程可单独定义version，优先级比父工程高
- 子工程要使用时，无需定义对应version，只需引入GA坐标，然后再通过版本仲裁来确定引入的依赖的深层依赖的version

![image-20221028110938184](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221028110938184.png)

## 2. 多继承-DM

- 解决pom的parent单继承问题
- import必须作用于pom类型的dependency，必须放在dependencyManagement中
- DM和parent同时存在时候，DM优先级高

### 2.1 单体项目-import

- 单体项目依赖多个parent
- 可以将父项目通过import来引入，也可作为DM来引入

#### a. 多pom相同依赖

- 多个pom级中如果包含相同依赖，声明优先，不管路径深度
- 嵌套pom-声明优先

![image-20230423150224529](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230423150224529.png)

![image-20230423152552841](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230423152552841.png)

#### b. 一级import：显式优先

![image-20230423150757244](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230423150757244.png)

### 2.2 父子项目-parent

- 在父工程中通过import的方式导入多个其他父工程，然后让子项目去依赖
- 如果父工程和子工程都定义了一个pom类型的父工程，则选用子项目的版本
- 父工程可以不断向上追溯

![image-20221030163207215](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221030163207215.png)

## 3. 项目抽离

- 可以对父工程下各个子工程需要的公共的服务，抽取成common三方包，install到本地仓库，并上传到私服
- 然后让各个子模块去依赖

## 4. 标签属性

- 标签属性： 可以在父工程中定义，也可以在子工程中定义
- 标签在引用时候，只能存在于子父工程，不能存在于通过DependencyManagement来使用

### 4.1 自定义标签

- 可以自己为标签取名字，然后在工程中通过${}来进行获取对应的值

![image-20221028111835450](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20221028111835450.png)

# Build

## 1. 基本设置

```bash
# finalName
- 默认打包后的文件名是： artifactId-version.jar
- 可以通过该标签个性化名字
```

## 2. 微服务打包

- maven的直接打包，不能生成直接可以运行的微服务jar包

```bash
# 微服务jar包
- 当前微服务本身jar包
- 当前微服务所依赖的jar包
- 内置可以直接运行的tomcat
- jar包可以通过 java -jar方式直接启动

# 添加对应的plugin，最终会生成两个jar
erick-customization.jar                # fat jar，即可以直接运行的jar
erick-customization.jar.original       # 去掉 .original, 就是maven默认的打包后的jar
```

![image-20230424132142527](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230424132142527.png)

```xml
<build>
        <plugins>
            <!--如果不加该插件，则只包含当前服务的代码-->

            <!--满足微服务打包的插件
            版本信息： 1. 如果parent是springboot的版本，则不配置可以直接使用
                      2. 也可以自定义
                      3. 不能直接通过DM来进行传递，只能通过parent来传递-->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>3.0.5</version>
            </plugin>
        </plugins>

        <!--自定义打包后的名字: erick-customization.jar -->
        <finalName>erick-customization</finalName>
    </build>
```



# Nexus私服

## 1. 安装

- Nexus的运行需要Java环境，先在Linux上安装Java(3.52.0.01要求安装Java8环境)

```bash
# 上传
put /Users/shuzhan/Desktop/nexus-3.52.0-01-unix.tar.gz /usr/local 
tar -zxvf nexus-3.52.0-01-unix.tar.gz

# 启动
cd /usr/local/nexus-3.52.0-01/bin
./nexus run       # 前台启动，能够实时查看日志，但是退出命令行就会终止
./nexus start     # 后台启动(推荐)，但不能看到日志
./nexus status    # 查看状态

# 参数设置
# 1. 参数一：内存: nexus-3.52.0-01/bin/nexus.vmoptions
- nexus中默认是2g
    # 修改前
    -Xms2703m
    -Xmx2703m
    -XX:MaxDirectMemorySize=2703m

    # 修改后
    -Xms528m
    -Xmx528m
    -XX:MaxDirectMemorySize=1024m
    
# 2. 参数二：浏览器端口：/nexus-3.52.0-01/etc/nexus-default.properties
- 默认端口： 8081
- application-port=8081

# error:  Detected execution as "root" user.  This is NOT recommended!
# 需要去/bin/nexus中修改配置,
- vi环境下可以使用/run_as_root搜索
- run_as_root=true   改为  run_as_root=false
```

## 2. 访问

```bash
# 1. 访问url
- http://60.205.229.31:8081/

# 2. 默认用户名和密码， 可以进行密码修改
- user：     admin
- password： 根据页面提示，在对应的linux的目录中去找password
# Disable anonymous access： # 选择禁用匿名访问
```

### 2.1 添加用户

- 添加一个用户： user:erick     password:19920507, 赋予admin权限

![image-20230424182949968](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230424182949968.png)

## 3. 仓库类型

- 拉取的jar包会保存在maven-central和maven-public中，我们项目中配置public的mirror即可

```bash
# maven-central
- Nexus对Maven中央仓库的代理，默认是取中央仓库拉的
- 设置镜像仓库，设置成阿里云的

# maven-public
- Nexus默认创建的，供开发下载使用的仓库
- 实际下载的jar包就会存在这里

# maven-release
- Nexus默认创建，供开发部署自己的jar，要求版本以release结尾

# maven-snapshots
- Nexus默认创建，供开发部署自己的jar，要求版本以snapshots结尾

# proxy
- 某个远程仓库的代理

# group
- 通过Nexus获取的第三方jar包

# hosted
- 本团队开发人员部署到Nexus的jar包
```

![image-20230424174044569](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230424174044569.png)

![image-20230424180729155](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230424180729155.png)

![image-20230424182322668](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230424182322668.png)

## 4. 公共jar包-本地拉取配置

- 配置setting.xml文件拉去公共的jar包
- 去对应的mirror拉代码时候，根据url找到对应的私服仓库，根据id找到对应的username和password来进行权限认证访问

```xml
  <servers>
    <server>
      <!--server的ID必须和对应的私服的mirror的ID相同-->
      <id>erick-nexus</id>
      <username>erick</username>
      <password>19920507</password>
    </server>
  </servers>

  <mirrors>
    <!--自己搭建的私服的repo-->
    <mirror>
      <id>erick-nexus</id>
      <name>erick-nexus</name>
      <url>http://60.205.229.31:8081/repository/maven-public/</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
```

## 5. 项目上传发布

- 通过IDEA来执行maven deploy

### member

- 如果配置了maven-public的member，则member添加jar包时，同时也会在**maven-public**存一份。如果取消了maven-public的member，则会立刻删除掉对应的jar包

![image-20230424221731984](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230424221731984.png)

### pom.xml

![image-20230424225135087](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230424225135087.png)

### 5.1  SNAPSHOTS

- pom中对应项目的version只要包含 SNAPSHOT都会发布到maven-snapshots仓库
- SNAPSHOTS发布时候，会自动加上时间戳作为备份

```bash
# Deployment policy: Allow redeploy
- 同一个jar包相同版本可以部署多次
```

![image-20230424214231603](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230424214231603.png)

### 5.2 RELEASES

- version如果不包含SNAPSHOT字符，都会自动发布到maven-releases仓库
- 建议release的jar包带上RELEASE，比如1.0.0-RELEASE

```bash
# Deploy Policy: Disable redeploy,  不允许deploy相同版本的jar包
status: 400 Repository does not allow updating assets: maven-releases
```

![image-20230424223749793](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230424223749793.png)



## 6. 项目拉取

### 6.1 maven-public

- 如果SNAPSHOT和RELEASE的jar包存在于maven-public，则直接可以去maven-public拉

#### setting.xml

```bash
# 在服务器上的maven和本地的maven配置，作为全局配置，项目pom.xml可以不加任何配置上面配置的已经生效
- 缺点： 强制配置setting.xml
- 优点： 较为安全，不会出现user和password暴露在pom.xml中
```

```xml
<servers>
    <server>
      <!--server的ID必须和对应的私服的mirror的ID相同-->
      <id>erick-nexus</id>
      <username>erick</username>
      <password>19920507</password>
    </server>
  </servers>

  <mirrors>
    <!--自己搭建的私服的repo-->
    <mirror>
      <id>erick-nexus</id>
      <name>erick-nexus</name>
      <url>http://60.205.229.31:8081/repository/maven-public/</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
```

```bash
# 引用时用原来项目的版本号，不能通过时间戳对应的版本来拉
- 会自动拉取maven-public中最新时间戳的jar包
- 如果jar包更新了，本地重新reimport即可，不用删除本地repo的包

        <dependency>
            <groupId>com.erick.daydreamer</groupId>
            <artifactId>erick-haha</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```

### 6.2 maven-xxx

- 如果maven-public没有对应的SNAPSHOT和RELEASE的member，则不能从maven-public拉，只能从对应的仓库去找

**setting.xml**

```xml
   <?xml version="1.0" encoding="UTF-8"?>

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<!--
 | This is the configuration file for Maven. It can be specified at two levels:
 |
 |  1. User Level. This settings.xml file provides configuration for a single user,
 |                 and is normally provided in ${user.home}/.m2/settings.xml.
 |
 |                 NOTE: This location can be overridden with the CLI option:
 |
 |                 -s /path/to/user/settings.xml
 |
 |  2. Global Level. This settings.xml file provides configuration for all Maven
 |                 users on a machine (assuming they're all using the same Maven
 |                 installation). It's normally provided in
 |                 ${maven.conf}/settings.xml.
 |
 |                 NOTE: This location can be overridden with the CLI option:
 |
 |                 -gs /path/to/global/settings.xml
 |
 | The sections in this sample file are intended to give you a running start at
 | getting the most out of your Maven installation. Where appropriate, the default
 | values (values used when the setting is not specified) are provided.
 |
 |-->
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">

    <localRepository>/Users/shuzhan/Documents/repo</localRepository>

    <pluginGroups></pluginGroups>

    <proxies></proxies>

    <mirrors>
        <!--自己搭建的私服的repo，可以配置多个私服-->
        <mirror>
            <id>erick-nexus</id>
            <name>erick-nexus</name>
            <url>http://60.205.229.31:8081/repository/maven-public/</url>
            <mirrorOf>central</mirrorOf>
        </mirror>
    </mirrors>

    <servers>
        <server>
            <!--server的ID必须和对应的私服的mirror的ID相同-->
            <id>erick-nexus</id>
            <username>erick</username>
            <password>19920507</password>
        </server>
        <server>
            <!--snapshot的server-->
            <id>erick-nexus-public</id>
            <username>erick</username>
            <password>19920507</password>
        </server>

        <server>
            <!--snapshot的server-->
            <id>erick-nexus-snapshot</id>
            <username>erick</username>
            <password>19920507</password>
        </server>

        <server>
            <!--release的server-->
            <id>erick-nexus-release</id>
            <username>erick</username>
            <password>19920507</password>
        </server>
    </servers>

    <profiles>
        <profile>
            <!--id随意,后面要通过该id激活该profile-->
            <id>erick-repo</id>
            <!--有几个repo，就有几个repo的server验证信息
               1. repo的id不能重复
               2. 拉的时候遍历几个repo去拉-->
            <repositories>
                <!--配置snapshots的拉取权限-->
                <repository>
                    <!--id要和对应的server对应-->
                    <id>erick-nexus-snapshot</id>
                    <name>erick-snapshot-repo</name>
                    <url>http://60.205.229.31:8081/repository/maven-snapshots/</url>
                    <snapshots>
                        <enabled>true</enabled>
                        <updatePolicy>always</updatePolicy>
                    </snapshots>
                </repository>
                <!--配置release的拉取权限-->
                <repository>
                    <!--id要和对应的server对应-->
                    <id>erick-nexus-release</id>
                    <name>erick-release-repo</name>
                    <url>http://60.205.229.31:8081/repository/maven-releases/</url>
                    <releases>
                        <enabled>true</enabled>
                    </releases>
                </repository>
            </repositories>
        </profile>
    </profiles>

    <activeProfiles>
        <!--激活对应的profile-->
        <activeProfile>erick-repo</activeProfile>
    </activeProfiles>

</settings>
```

# 版本信息

```bash
# SNAPSHOT
- 快照版本
- 每个模块进行的临时版本(测试版本)

# RELEASE 
- 发布版本
- 稳定版本

# 工程版本号: 主版本  次版本  增量版本   里程碑版本
- 主版本：     项目重大架构的变更
- 次版本：     较大的功能增加和变化， 或者全面系统的修复漏洞
- 增量版本：   重大漏洞的修复
- 里程碑版本：  版本内部。同下一个正式版本相比，相对来说不是很稳定，有待更多测试

5.1.9 RELEASE
```
