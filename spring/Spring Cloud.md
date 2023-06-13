# 入门简介

## 简介

- 分布式微服务架构的一站式解决方案，是多种微服务架构落地技术的集合体，微服务全家桶
- [s pringcloud和springboot版本](https://start.spring.io/actuator/info)

```bash
# springcloud：(GA: general avaliability，官方推荐版本)
2021.0.3

# springboot
2.6.8
```

![image-20220826165741290](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220826165741290.png)

## 父工程pom

- 只包含pom，不包含任何其他任何东西

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.citi</groupId>
    <artifactId>poc-parent</artifactId>
    <version>1.0-SNAPSHOT</version>
    <modules>
        <module>payment-service</module>
    </modules>
    <packaging>pom</packaging>
    <name>poc-parent</name>

    <!--统一jar包版本-->
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <spring-boot>2.6.8</spring-boot>
        <spring-cloud>2021.0.3</spring-cloud>
    </properties>

    <!--父模块的依赖管理： 子模块不需要再写版本号
        实际并不会引入依赖-->

    <!--dependencyManagement:
      1. 通常会用项目的父pom中使用，所有子项目不会写明版本号，maven会沿着父子层次向上走，直到找到一个
         拥有dependencyManagement元素的项目，并使用其中的版本号
      2. 只是负责版本管理，不会实际导入依赖
      3. 子项目可以自定义版本号-->
    <dependencyManagement>
        <dependencies>
            <!--springboot-->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!--spring cloud-->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

## 子工程pom

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>poc-parent</artifactId>
        <groupId>com.citi</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>payment-service</artifactId>

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
</project>
```

## Common工程

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>poc-parent</artifactId>
        <groupId>com.citi</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>erick-common</artifactId>

    <dependencies>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.8.5</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>
</project>
```

- Maven install到本地仓库
- 在其他项目中引入该common工程

# Eureka注册中心

## 1. 服务直接调用

- 直接调用外部API, 一般通过传统的RPC调用，也就是目标API的IP，PORT， URL进行调用

### 1.1 服务调用方

```java
package com.citi.order.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    /*boot 2后的版本，已经不内置该bean*/
    private RestTemplate restTemplate = new RestTemplate();

    private static final String PAYMENT_URL = "http://localhost:9100/user/name";

    @GetMapping("/info")
    public Map<String, Object> getOrderInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("time", new Date());
        result.put("name", getUserName());
        return result;
    }

    private String getUserName() {
        String name = restTemplate.getForObject(PAYMENT_URL, String.class);
        return name;
    }
}
```

### 1.2 服务被调用方

```java
package com.citi.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserAccount {

    @GetMapping("/name")
    public String getName() {
        return "erick";
    }
}
```

## 2. 服务治理

```bash
# 问题
- 传统 RPC 管理比较复杂，因此需要引入服务治理
- 包含服务注册，服务发现，服务调用，负载均衡

# 注册中心理念
- 集群式服务注册中心
- 服务提供者： 注册，心跳(默认30s)，断开
- 服务消费者： 注册，获取注册信息，通过注册中心调用
```

![image-20220828000304488](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220828000304488.png)

## 3. 单机版Eureka

### 3.1 Server端

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>poc-parent</artifactId>
        <groupId>com.citi</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>eureka-first</artifactId>
    
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
        <!--版本受spring cloud管理-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    </dependencies>
</project>
```

```yaml
server:
  port: 7001

eureka:
  client:
    register-with-eureka: false  # 是否将自己注册到注册中心：server端不需要
    fetch-registry: false        # 是否检索注册中心的服务： server端不需要
```

```bash
# 主启动类： org.springframework.cloud.netflix.eureka.server.EnableEurekaServer
@EnableEurekaServer

# 可以在本地host文件配置  127.0.0.1做多个映射
http://127.0.0.1:7001/
http://localhost:7001/
http://firsteureka:7001/
http://secondeureka:7001/
http://thirdeureka:7001/
```

### 3.2 Client端

```xml
<!--版本受spring cloud管理-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

```yaml
server:
  port: 9000

spring:
  application:
    name: citi-order  # 微服务在注册中心名字，大写

eureka:
  instance:
    # 微服务在注册中心名字:   优先级高于 spring.application.name
    #                      一般会直接用spring.application.name
    appname: ${spring.application.name}

  client:
    register-with-eureka: true   # 服务注册到eureka
    fetch-registry: true         # 抓取注册中心的其他服务
    service-url:
      defaultZone: http://localhost:7001/eureka
```

```bash
# 主启动类加
@EnableEurekaClient
```

- DS Replicas: 默认将注册中心本身注册在自己，同时显示默认域名localhost

![image-20220826191507121](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220826191507121.png)

## 4. 集群版Eureka

### 4.1 Server端

- 为避免单节点故障问题，互相注册，互相守望

```bash
# 修改本地host配置文件，为了方便域名解析
127.0.0.1	      localhost
127.0.0.1       first-eureka
127.0.0.1       second-eureka
127.0.0.1       third-eureka
```

```yaml
server:
  port: 7001

eureka:
  instance:
    hostname: first-eureka    # 会通过host文件，将该域名和ip进行解析，并显示为eureka server的一个名字
                              # 下面注册的时候，也是通过对应的域名来进行注册的

  client:
    register-with-eureka: false # 是否将自己注册到注册中心：server端不需要
    fetch-registry: false # 是否检索注册中心的服务： server端不需要
    service-url:
      defaultZone: http://second-eureka:7002/eureka,http://third-eureka:7003/eureka

  server:
    enable-self-preservation: false # 默认为true
```

```yaml
server:
  port: 7002

eureka:
  instance:
    hostname: second-eureka  

  client:
    register-with-eureka: false # 是否将自己注册到注册中心：server端不需要
    fetch-registry: false # 是否检索注册中心的服务： server端不需要
    service-url:
      defaultZone: http://first-eureka:7001/eureka,http://third-eureka:7003/eureka
  server:
    enable-self-preservation: false # 默认为true
```

```yaml
server:
  port: 7003

eureka:

  instance:
    hostname: third-eureka
  client:
    register-with-eureka: false # 是否将自己注册到注册中心：server端不需要
    fetch-registry: false # 是否检索注册中心的服务： server端不需要
    service-url:
      defaultZone: http://first-eureka:7001/eureka,http://second-eureka:7002/eureka
  server:
    enable-self-preservation: false # 默认为true
```

![image-20220828005130647](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220828005130647.png)

### 4.2  Client端

```yaml
server:
  port: 9000

spring:
  application:
    name: citi-order

eureka:
  instance:
    appname: ${spring.application.name}

  client:
    register-with-eureka: true   # 服务注册到eureka
    fetch-registry: true         # 抓取注册中心的其他服务，集群版本必须设置为true才能配合ribbon使用
    service-url:
      defaultZone: http://localhost:7001/eureka,http://localhost:7002/eureka,http://localhost:7003/eureka
```

## 5. 服务调用

### 5.1 基本使用

- 对目标api，用服务名替换ip和port，并利用spring-cloud-loadbalancer实现负载均衡目的

```java
package com.citi.order.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Config {

   // @LoadBalnaced是springcloud的，适用于所有的注册中心
    @LoadBalanced
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
```

```java
package com.citi.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    // 协议：微服务名：具体映射路径
    private static final String PAYMENT_URL = "http://CITI-PAYMENT/user/name";

    @GetMapping("/info")
    public Map<String, Object> getOrderInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("time", new Date());
        result.put("name", getUserName());
        return result;
    }

    private String getUserName() {
        String name = restTemplate.getForObject(PAYMENT_URL, String.class);
        return name;
    }
}
```

### 5.2 负载均衡

- 被调用方的服务启动两份，通过负载均衡来达到调用

## 6. actutor信息完善

- 一般通过点击status中的服务名，可以知道具体服务的ip和端口
- pom中必须添加actutor依赖

```yaml
# client端
eureka:
  instance:
    appname: ${spring.application.name}
    instance-id: payment-first    # 一个服务部署多份时，对应的不同的instance
    prefer-ip-address: true       # 鼠标移动到对应的服务上，就会有对应的ip和端口显示
```

![image-20220828011142802](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220828011142802.png)

## 7. 服务监控

```bash
# 主启动类： import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
# 从而在服务中，可以获取到注册中心当前注册服务的信息
# 是springcloud的，适用于所有的注册中心
@EnableDiscoveryClient

# 在服务中，可以发现当前注册中心的所有服务， 注册中心包含： eureka,consul,nacos等
```

```java
package com.citi.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/monitor")
public class MonitorController {

    /*获取时， 不区分大小写*/
    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/services")
    public List<String> getAllServiceName() {
        List<String> services = discoveryClient.getServices();
        /*发现链接的注册中心的所有服务名
         * 对应的名字是： 服务的名字， spring.application.name*/
        return services;
    }

    @GetMapping("/instances")
    public List<ServiceInstance> getInstanceByName() {
        String serviceName = "CITI-PAYMENT";
        /*根据服务注册名： 获取到该服务的多个实例对象*/
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        return instances;
    }
}
```

## 8. 自我保护机制

### 8.1 机理

- 宁可保留错误的服务信息，不会滥杀无辜

```bash
1. Client定期向Server端发送心跳包
2. 但一定时间内，因为网络原因，Server没有收到心跳包（假死），Server并不会立刻将该Client删除
3. Server节点在短时间内丢失过多Client时，该节点就会进入自我保护模式

# api调用：如果某个client确实挂了，那么在调用失败时，触发重试机制
# 负载均衡到该client的其他节点
```

![image-20220826230406489](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220826230406489.png)

```yaml
# server端， 自我保护机制开启的时候，配置心跳和属性
eureka:
  instance:
    appname: ${spring.application.name}
    instance-id: payment-first
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 2     # 客户端多久发送心跳的时间间隔，默认为30s
    lease-expiration-duration-in-seconds: 3  # server端在收到最后一次心跳等待的时间上限，默认为90s，超时就删除
```

### 8.2 禁用

```bash
 # server端关闭
 server:
    enable-self-preservation: false # 默认为false
```

![image-20220826230816734](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220826230816734.png)



# Config配置中心

- 改动方便： 多微服务共享的信息抽取出来统一配置，改动方便
- 数据安全： 微服务的数据源信息直接写在application.yaml里面，数据可能暴露

## 1. 理念

```bash
# Remote Repository
- github， gitee类的仓库，对敏感信息和公共信息进行配置

# Config Server
- 能够直接获取到github仓库类的信息， 获取到信息并同步到本地缓存

#  Client Server
- 从Config Server获取相关数据

# 步骤
- 修改remote repository的数据信息， config server拉取数据
- 重启 Client Server， 会先去config server的本地访问，然后再获取远程，为服务配置生效
```

## 2. 搭建

### 2.1 Config Server端

```xml
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
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
  </dependency>
</dependencies>
```

```bash
# 主启动类： import org.springframework.cloud.config.server.EnableConfigServer;
@EnableConfigServer
```

![](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220826234401794.png)

**读取文件**

- github的文件必须是以yaml，yml，properties格式
- 文件格式应该遵循 {filename}-{profile}.yaml
- 可以通过下面几种方式来验证Config Server和 github是否已经对接
- 每次在git上改完，访问下面的地址都可以立即生效

```bash
# 下面两种方式获取到的数据格式类似application.yaml的文件
# 方式一： ip：端口/分支/文件名，如果文件不存在，则读取到的为{} 
- http://localhost:3344/main/datasource-prod.yaml

# 方式二：ip：端口/文件名 ，默认读取main分支
- http://localhost:3344/datasource-prod.yaml

# 方式三：ip：端口/文件名/profile/分支，读取到的为json字符串
- http://localhost:3344/datasource/prod/main
```

### 2.2 Config Client 端

```xml
 <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-config</artifactId>
 </dependency>
 <!--springcloud 2020版本默认把bootstrap文件禁用，如果需要用，则需要加上-->
 <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-bootstrap</artifactId>
 </dependency>
```

#### a. bootstrap.yaml

- bootstrap.yaml文件负责加载配置中心的相关信息
- bootstrap.yaml文件和application.yaml数据共同起作用，bootstrap.yaml优先级高

```yaml
spring:
  cloud:
    config:
      label: main                    # git对应的仓库分之
      name: datasource               # 文件名
      profile: prod                  # 文件名后缀的profile
      uri: 'http://localhost:3344'   # 去config server工程间接获取
```

#### b. GitHub源文件

```yaml
username: erick.shu
password: 123456
address: shanxi
age: 21
```

#### c. 测试

```java
package com.citi.order.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/config")
public class CloudConfigController {

    @Value("${username}")
    private String userName;

    @Value("${password}")
    private String password;

    @Value("${address}")
    private String address;

    @Value("${age}")
    private int age;

    @GetMapping("/info")
    public Map<String, Object> getConfigInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("username", userName);
        info.put("password", password);
        info.put("address", address);
        info.put("age", age);
        return info;
    }
}
```

### 2.3 Dynamic Setting

#### a. 问题

```bash
# 问题
- github上信息修改后，config server能直链github，信息可以及时生效
- config client是链接的config server，不能直接生效

# 解决
- 每次修改github信息后，重启所有的业务服务，即 config client
```

#### b. 动态刷新

- 在需要动态更新的client server端，需要如下配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

```bash
# @RefreshScope：   org.springframework.cloud.context.config.annotation.RefreshScope
- 加到类上，需要动态配置的属性，不启动微服务也能动态刷新
- 凡是需要动态刷新的类，都需要进行这个注解
```

```java
package com.citi.order.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RefreshScope
@RestController
@RequestMapping("/config")
public class CloudConfigController {

    @Value("${username}")
    private String userName;

    @Value("${password}")
    private String password;

    @Value("${address}")
    private String address;

    @Value("${age}")
    private int age;

    @GetMapping("/info")
    public Map<String, Object> getConfigInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("username", userName);
        info.put("password", password);
        info.put("address", address);
        info.put("age", age);
        return info;
    }
}
```

- 每次修改配置文件后，对每个业务微服务，都通过ip+端口进行刷新
- 每次远程配置文件修改后，都需要手动的去执行刷新脚本

```bash
# 通过postman或者shell脚本刷新
curl -X POST "http://localhost:9001/actuator/refresh"


#  返回结果： config.client.version 和 修改的属性名
[
    "config.client.version",
    "address"
]
```

# Bus总线

## 1. 理念

- 每次修改config文件，需要手动刷新config client的配置，比较麻烦
- 假如client server端比较多，执行难度较大

# Open Feign服务调用

## 1. 理念

- OpenFeign实现服务间调用，是Feign的升级版本
- 利用RestTemplate对http请求进行了封装，形成了一套模版化的调用方法，服务注册中心+模版接口+直接调用
- OpenFeign集成了spring cloud loadbalancer，调用时候默认用轮循作为负载均衡机制
- 服务调用方和服务调用方都需要入驻注册中心，通过服务名来进行调用时自动会有负载均衡

## 2. 服务被调用方

```java
@RestController
@RequestMapping("/user")
public class UserAccount {

    @GetMapping("/name")
    public String getName() {
        return "erick";
    }
}
```

## 3.  服务调用方

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```bash
# 主启动类： org.springframework.cloud.openfeign.EnableFeignClients
@EnableFeignClients
```

```java
package com.citi.order.openfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

// 封装了RestTemplate以及负载均衡
@Service
@FeignClient(value = "citi-payment") // 被调用方在服务注册中心名
public interface PaymentFeignInter {

    /**
     * 1. 查找注册中心的citi-payment的服务,大小写均可以
     * 2. 根据上面的服务名，结合下面的具体接口的URL，定位到具体调用的url
     * 3. 调用的时候会采取负载均衡
     * 4. 完全按照对应其他服务的接口来写
     */
    @GetMapping("/user/name")
    String getName();
}
```

```java
package com.citi.order.controller;

import com.citi.order.openfeign.PaymentFeignInter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feign")
public class OrderRemoteController {

    @Autowired
    private PaymentFeignInter paymentFeign;

    @GetMapping("/info")
    public String getRemoteService() {
        return paymentFeign.getName();
    }
}
```

## 4. OpenFeign调用超时

```yaml
# A去调B, Feign的配置都是在A中
feign:
  client:
    config:
      default:
        connect-timeout: 5000 # 默认1s调用不到就返回错误信息
        read-timeout: 5000

      # default： 代表全局配置，可以根据服务注册名局部配置
      # 注册中心的名字： 大小写敏感
      CITI-PAYMENT:
        connect-timeout: 1000
        read-timeout: 1000
```

# Hystrix-服务降级与熔断

- netflix公司，目前停止更新，但设计理念优秀，出道即巅峰
- 服务降级，服务熔断，服务监控

## 1. 引入

```bash
# 服务雪崩
- 微服务调用链中，若某个调用环节一直超时占用
- 被调用方服务接口有一定的最大并发数，当前线程得不到释放，就会引发该服务的线程数目迅速占满，从而冲垮服务器

# 超时原因
1. 异常超时：   被调用的服务响应时间过长(网速拥堵，服务器宕机)
2. 程序错误：   被调用的服务内部出错
3. 正常超时：   被调用的服务响应时间太长(可能由于业务复杂引起)，调用方不愿继续等待
```

## 2.  服务降级

```bash
# Hystrix既可以放在consumer，也可以放在provider方，一般是用在provider
1. 服务不可用： 被调用方程序运行异常，网络卡顿，业务时间过长
2. 服务降级：   被调用方服务不可用时，应该提供一个兜底方案
```

```xml
<!-- 新版本springboot和spring cloud不再管理hystrix,需要手动指定版本号-->
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
  <version>2.2.10.RELEASE</version>
</dependency>
```

### 2.1  方法降级

#### a.Provider

```java
package com.citi.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/hystrix")
public class HystrixController {

    @GetMapping("/normal")
    public String getContent() {
        return "hello, I am result";
    }

    @GetMapping("/error")
    public String getError() {
        throw new RuntimeException("unexpected error");
    }

    @GetMapping("/timeout")
    public String getTimeOut() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
        return "I am awake";
    }
}
```

#### b.consumer

```bash
# 主启动类上加： 开启hystrix服务降级及熔断的功能
# @EnableHystrix继承了@EnableCricuitBreaker
# 因为hystrix已经不再被springcloud收录，已经开始过期
- EnableHystrix:                     org.springframework.cloud.netflix.hystrix.EnableHystrix;
- EnableCircuitBreaker:              org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
```

```java

package com.citi.order.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/consumer")
@RestController
public class HystrixTestController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String NORMAL_URL = "http://citi-payment/hystrix/normal";

    private static final String ERROR_URL = "http://citi-payment/hystrix/error";

    private static final String TIMEOUT_URL = "http://citi-payment/hystrix/timeout";

    @GetMapping("normal")
    public String testNormal() {
        return restTemplate.getForObject(NORMAL_URL, String.class);
    }

    /*被调用方发生异常后，调用方提供的兜底方案*/
    @HystrixCommand(fallbackMethod = "providerError")
    @GetMapping("error")
    public String testError() {
        return restTemplate.getForObject(ERROR_URL, String.class);
    }

    /*调用方自己的异常，也会进行兜底处理*/
    @HystrixCommand(fallbackMethod = "providerError")
    @GetMapping("/error/self")
    public String testErrorSle() {
        int i = 1 / 0;
        return "hello";
    }

    @HystrixCommand(fallbackMethod = "providerTimeout",
            commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")})
    @GetMapping("timeout")
    public String testTimeOut() {
        return restTemplate.getForObject(TIMEOUT_URL, String.class);
    }

    /*兜底方案*/
   /**服务降级的方法必须和处理的接口的参数一致*/
    private String providerError() {
        return "provider error happens";
    }

    private String providerTimeout() {
        return "provider timeout";
    }
}
```

### 2.2 全局配置

- 不可能为每一个方法都提供一个服务降级处理的方法
- 为类中所有方法配置全局服务降级的方法，某些方法可以单独自定义

```java
package com.citi.order.controller;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@DefaultProperties(defaultFallback = "globalErrorHandler") //配置该类的全局变量的异常处理
@RequestMapping("/global")
@RestController
public class HystrixGlobalController {

    @Autowired
    private RestTemplate restTemplate;

    private static final String NORMAL_URL = "http://citi-payment/hystrix/normal";

    private static final String ERROR_URL = "http://citi-payment/hystrix/error";

    private static final String TIMEOUT_URL = "http://citi-payment/hystrix/timeout";

    @GetMapping("normal")
    public String testNormal() {
        return restTemplate.getForObject(NORMAL_URL, String.class);
    }

    /*需要被全局异常处理的： 加上 @HystrixCommand即可*/
    @HystrixCommand
    @GetMapping("error")
    public String testError() {
        return restTemplate.getForObject(ERROR_URL, String.class);
    }

    @HystrixCommand
    @GetMapping("/error/self")
    public String testErrorSle() {
        int i = 1 / 0;
        return "hello";
    }

    /* 个别方法需要单独特殊处理的，在该方法上再添加具体的配置 */
    @HystrixCommand(fallbackMethod = "providerTimeout",
            commandProperties = {@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")})
    @GetMapping("timeout")
    public String testTimeOut() {
        return restTemplate.getForObject(TIMEOUT_URL, String.class);
    }

    /*兜底方案*/
    private String providerTimeout() {
        return "provider timeout";
    }

    /*全局异常处理*/
    private String globalErrorHandler() {
        return "unexpected error";
    }
}
```

## 3. 服务熔断

```bash
1. 在指定时间窗口期内及请求次数，如果服务失败率达到60%，则开启服务熔断；  Closed
2. 再次用正确的参数来进行，断路器依然为Closed，直接进行服务降级
3. 慢慢的将断路器更换为  Half Closed，并尝试来调用当前服务
4. 如果服务恢复了，则变为Open，如果依然是失败的，则继续恢复为Closed
```

```java
@RequestMapping("/breaker")
@RestController
public class ConsumerCircuitController {

    @HystrixCommand(fallbackMethod = "backupPlan", commandProperties = {@HystrixProperty(name = "circuitBreaker.enabled", value = "true"), // 是否开启断路器
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"), // 请求次数
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),// 时间窗口期
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60"), // 失败率达到多少后开启
    })
    @GetMapping("/info")
    public String getInfo(Integer id) {
        if (id < 0) {
            throw new RuntimeException("id不能为负数");
        }
        return new Date() + " ===== " + id + " 正常访问";
    }

    public String backupPlan(Integer id) {
        return new Date() + " ===== " + id + " 服务降级";
    }
}


```

## 4. 服务监控

### 4.1 DashBoard服务

- 单独搭建一个微服务，用来观察指定服务的降级，熔断情况等

```xml
<!--1. web模块的starter和健康检查的starter-->
<!--hystrix的dashboard仪表盘-->
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
  <version>2.2.10.RELEASE</version>
</dependency>
```

```yaml
server:
  port: 7000
# 不配置如下可能出现如下错误：
# Origin parameter: http://localhost:8080/actuator/hystrix.stream is not in the allowed list of proxy host names.
# If it should be allowed add it to hystrix.dashboard.proxyStreamAllowList.
hystrix:
  dashboard:
    proxy-stream-allow-list: localhost
    # 允许监控的ip，不同版本可能不同
```

```bash
# 主启动类:  org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
@EnableHystrixDashboard
# 访问地址
http://localhost:7000/hystrix
```

![image-20220827112242442](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220827112242442.png)

### 4.2 微服务上监控

```
# 监控： pom中必须带有下面的依赖
spring-boot-starter-actuator
```

```java
package com.citi;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableFeignClients
@EnableCircuitBreaker
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class);
    }

    /**
     * 如果不添加下面的bean，监控时候就会链接不到该微服务
     * 报错信息： Unable to connect to Command Metric Stream.
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean getServlet() {
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
}
```

### 4.3 数据观测

```bash
1. 启动DashBoard和业务微服务，页面输入 微服务 ip, 端口        http://localhost:8001/hystrix.stream
2. 随机访问微服务业务断，不断发生服务降级及熔断等多种情况
3. 页面的信息是实时变化的，可以统计当前该服务的访问情况
```

![image-20220827112839006](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220827112839006.png)



# Sleuth链路监控

