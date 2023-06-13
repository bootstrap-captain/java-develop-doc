# Junit 5

```bash
# Junit: Junit Platform,      Junit Jupiter,      Junit Vintage
- Junit Platform:     JVM上启动测试框架的基础，不仅支持Junit自制的测试引擎， 其他测试引擎也可以接入
- Junit Jupiter:      Junit5 核心， 内部包含测试引擎，用于在Junit Platform 上运行
- Junit Vintage:      提供了兼容Junit3， Junit4的测试引擎
```

## 1. 入门案例

### 1.1 依赖

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter-api</artifactId>
  <version>5.9.0</version>
  <scope>test</scope>
</dependency>
```

![image-20220831120413871](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220831120413871.png)

### 1.2 被测试类

```java
package com.erick.service;

public class AwsService {
    public String getTopicName(String topicName) {
        return topicName.toUpperCase();
    }
}
```

### 1.3 测试类

```java
package com.erick.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class AwsServiceTest {

    private AwsService awsService;

    @BeforeEach
    public void init() {
        /*类的初始化，在init里面做*/
        awsService = new AwsService();
    }

    @Test
    public void testGetTopicName() {
        String mallTopic = awsService.getTopicName("mall_topic");
        // 断言
        Assertions.assertEquals("MALL_TOPIC", mallTopic);
    }

    @AfterEach
    public void close() {
        System.out.println(new Date());
    }
}
```

### 断言机制

-  org.junit.jupiter.api.Assertions提供的断言机制

```bash
# 相等
- Assertions.assertEquals(Object expected, Object actual)        
- Assertions.assertNotEquals(Object unexpected, Object actual)   
- Assertions. assertNotEquals(Object unexpected, Object actual, String message)   # 不相等时候的报错信息

# 为空
- Assertions.assertNull(Object actual)           
- Assertions.assertNotNull(Object actual)      

# 同一个对象
- Assertions.assertSame(Object expected, Object actual)
- Assertions.assertNotSame(Object unexpected, Object actual)

# true or false
- Assertions.assertTrue(boolean condition)
- Assertions.assertFalse(boolean condition)

# 数组元素顺序是否相同，可以为数组的任意类型
- Assertions.assertArrayEquals(boolean[] expected, boolean[] actual)

# 组合断言， 所有断言都成功才算成功
- Assertions.assertAll(String heading, Executable... executables)

# 异常断言
- Assertions.assertThrows(Class<T> expectedType, Executable executable)
```

### 常见注解

```bash
# @BeforeAll   @AfterAll  
- 该类中的所有测试方法运行前或者后运行
- 必须是static修饰

# @BeforeEach  @AfterEach
- 每个测试方法运行前或者后面运行

# @Disabled
- 禁用掉(跳过)某个测试方法

# @Timeout(value = 2, unit = TimeUnit.SECONDS)
- 测试方法是否超时： 如果超时则报错
```

## 2. 案例精进

### 2.1 被测试类

```java
package com.erick.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class AwsService {

    public static final String TOPIC_NAME = "topic_name";
    public static final String TOPIC_REGION = "topic_region";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public String getTopicName(String topicName) {
        return topicName.toUpperCase();
    }

    public Map<String, Object> getSnsInfo(String topicName) {
        Map<String, Object> info = new HashMap<>();
        info.put(TOPIC_NAME, topicName);
        info.put(TOPIC_REGION, "us-east-1");
        return info;
    }

    public void callApi() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> isLegalCredential(String userName) {
        Map<String, Object> info = new HashMap<>();
        if (userName.equalsIgnoreCase("root")) {
            info.put(USERNAME, "root");
            info.put(PASSWORD, "123456");
            return info;
        } else {
            throw new RuntimeException("Illegal Credential");
        }
    }
}
```

### 2.2 测试类

```java
package com.erick.service;

import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.erick.service.AwsService.TOPIC_NAME;
import static com.erick.service.AwsService.TOPIC_REGION;

public class AwsServiceTest {

    private AwsService awsService;

    /**
     * BeforeAll 和 BeforeEach的区别：
     * BeforeAll： 初始化一些整个类都会使用到的资源，且各个方法对该资源使用不会冲突
     * BeforeEach： 初始化一些，各个方法使用时候，可能会冲突的资源
     */

    /*整个类的测试，只会执行一次*/
    @BeforeAll
    public static void startAwsServiceTest() {
        System.out.println("begin AwsServiceTest");
    }

    @BeforeEach
    public void init() {
        /*类的初始化，在init里面做*/
        awsService = new AwsService();
    }

    /*单个断言*/
    @Test
    public void testGetTopicName() {
        String mallTopic = awsService.getTopicName("mall_topic");
        Assertions.assertEquals("MALL_TOPIC", mallTopic);
    }

    /*组合断言*/
    @Test
    public void testGetSnsInfo() {
        Map<String, Object> snsInfo = awsService.getSnsInfo("mock");
        Assertions.assertAll("sns-info",
                () -> Assertions.assertEquals(2, snsInfo.size()),
                () -> Assertions.assertEquals("mock", snsInfo.get(TOPIC_NAME)),
                () -> Assertions.assertEquals("us-east-1", snsInfo.get(TOPIC_REGION))
        );
    }

    /*超时断言*/
    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    public void testCallApi() {
        awsService.callApi();
    }

    /*异常断言*/
    @Test
    public void testIsLegalCredential() {
        Assertions.assertThrows(RuntimeException.class,
                () -> awsService.isLegalCredential("hehe"));
    }

    @Test
    @Disabled
    public void hello() {
        System.out.println("hello");
    }

    @AfterEach
    public void close() {
        System.out.println(new Date());
    }

    /*整个类的测试，只会执行一次*/
    @AfterAll
    public static void endAwsServiceTest() {
        System.out.println("end the AwsServiceTest");
    }
}
```

# Mockito

## 1. 用途

```bash
# 场景
- 场景一： A类中方法去调用了外部的一些Client或者Connection，不希望去真实的调用Client和Connection
- 场景二： A类中注入B类，同时A类方法调用B类方法，测试时不希望B类的代码去实际运行的，因为B类的代码已经自测
- 场景三： A类中方法a调用方法b，b方法已经测试过了，因此在测试a方法时跳过b方法

# 场景二：
- 依赖注入可以通过setter或者constructer来执行

# 单纯使用junit不能满足上述需求，因此引入了Mockito来增强Junit 5
```

```xml
<!--mockito core： 核心依赖-->
<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-core</artifactId>
  <version>4.7.0</version>
  <scope>test</scope>
</dependency>

<!-- junit 5 -->
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter-api</artifactId>
  <version>5.9.0</version>
  <scope>test</scope>
</dependency>

<!--mockito-inline: 支持静态方法的mock-->
<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-inline</artifactId>
  <version>4.7.0</version>
  <scope>test</scope>
</dependency>
```

![image-20220831120825863](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220831120825863.png)

## 2. Mock

```bash
# 作用
- 对象执行方法时候，不会走实际的调用，    默认返回结果为该方法返回值的'空值'
- 可以通过打桩的方式，让该对象执行某些方法时，返回指定的结果

# 饮用方式: 两种方式只是不同的写法， 依赖注入可以选择setter或者constructer
- 可以通过@Mock注解加MockitoAnnotations.openMocks(this)使用
- 可以直接Mockito.mock(SnsService.class);
```



### 2.1 setter+@mock

```java
package com.erick.service;

import java.util.HashMap;
import java.util.Map;

import static com.erick.constant.ErickConstant.TOPIC_NAME;
import static com.erick.constant.ErickConstant.TOPIC_REGION;

public class SnsService {

    public Map<String, Object> getSnsTopic(String topicName) {
        Map<String, Object> info = new HashMap<>();
        info.put(TOPIC_NAME, topicName);
        info.put(TOPIC_REGION, "us-east-1");
        return info;
    }
}
```

```java
package com.erick.service;

import lombok.Setter;

import java.util.Map;

public class AwsService {

    @Setter
    private SnsService snsService;

    public Map<String, Object> getSnsInfo(String topicName) {
        int a = 2;
        int b = 3;
        return snsService.getSnsTopic(topicName);
    }
}
```

```java
package com.erick.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

public class AwsServiceTest {

    private AwsService awsService;

    /*mock的B类对象*/
    @Mock
    private SnsService snsService;

    @BeforeEach
    public void init() {
        /*结合@Mock使用，在测试类中打开mock功能，否则报错*/
        MockitoAnnotations.openMocks(this);

        awsService = new AwsService();
        awsService.setSnsService(snsService);
    }

    /**
     * 被mock的对象，默认返回结果为该方法返回值的'空值'
     * 基础数据类型： 0
     * 引用数据类型： List   空list，但是不是null
     */
    @Test
    public void testGetSnsInfo01() {
        Map<String, Object> mockInfo = awsService.getSnsInfo("mock");
        Assertions.assertEquals(0,mockInfo.size());
    }

    /**
     * 被mock的对象，可以通过打桩的方式，让该对象执行某些方法时，返回指定的结果
     */
    @Test
    public void testGetSnsInfo02() {
        Map<String, Object> result = new HashMap<>();
        result.put("k1","v1");
        result.put("k2","v2");
        result.put("k3","v3");
        Mockito.when(snsService.getSnsTopic(Mockito.any())).thenReturn(result);
        Map<String, Object> mockInfo = awsService.getSnsInfo("mock");
        Assertions.assertEquals(3, mockInfo.size());
    }
}
```

### 2.2 consturct+mock

```
Mockito.mock(SnsService.class);
```

```java
package com.erick.service;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class AwsService {
    private SnsService snsService;

    public Map<String, Object> getSnsInfo(String topicName) {
        int a = 2;
        int b = 3;
        return snsService.getSnsTopic(topicName);
    }
}
```

```java
package com.erick.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class AwsServiceTest {

    private AwsService awsService;

    private SnsService snsService;

    @BeforeEach
    public void init() {
        snsService = Mockito.mock(SnsService.class);
        awsService = new AwsService(snsService);
    }

    @Test
    public void testGetSnsInfo01() {
        Map<String, Object> mockInfo = awsService.getSnsInfo("mock");
        Assertions.assertEquals(0, mockInfo.size());
    }
    
    @Test
    public void testGetSnsInfo02() {
        Map<String, Object> result = new HashMap<>();
        result.put("k1", "v1");
        result.put("k2", "v2");
        result.put("k3", "v3");
        Mockito.when(snsService.getSnsTopic(Mockito.any())).thenReturn(result);
        Map<String, Object> mockInfo = awsService.getSnsInfo("mock");
        Assertions.assertEquals(3, mockInfo.size());
    }
}
```

## 3. Spy

```bash
# 用法
- 被spy的测试对象， 如果不打桩，默认走实际方法调用
- 如果打桩，就会跳过打桩的方法，mock一个假的结果

# 饮用方式： 只是书写的两种方式，并无区别
- Mockito.spy()

- @Spy + MockitoAnnotations.openMocks(this);

# 这种打桩方法，如果本类中被打桩的方法是私有方法，则无法实现， 可以改为protected来实现
```

### 3.1 Mockito.spy()

```java
package com.erick.service;

import java.util.ArrayList;
import java.util.List;

public class AccountService {

    public String getName() {
        return "erick";
    }

    /*调用其他的方法，其他的方法就可以被spy*/
    public List<String> getInfo() {
        List<String> basicInfo = getBasicInfo();
        basicInfo.add("password");
        return basicInfo;
    }

    public List<String> getBasicInfo() {
        List<String> basicInfo = new ArrayList<>();
        basicInfo.add("name");
        basicInfo.add("address");
        return basicInfo;
    }
}
```

```java
package com.erick.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class AccountServiceTest {
    private AccountService accountServiceSpy;


    @BeforeEach
    public void init() {
        accountServiceSpy = Mockito.spy(new AccountService());
    }

    /*直接调用方法: 走真实的业务逻辑*/
    @Test
    public void testGetName() {
        String name = accountServiceSpy.getName();
        Assertions.assertEquals("erick", name);
    }

    @Test
    public void testGetInfo() {
        /*spy： 打桩后，走虚拟的业务逻辑*/
        Mockito.doReturn(new ArrayList<>()).when(accountServiceSpy).getBasicInfo();
        List<String> result = accountServiceSpy.getInfo();
        Assertions.assertEquals(1, result.size());
    }
}
```

### 3.2 @Spy

```java
package com.erick.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

public class AccountServiceTest {
    @Spy
    private AccountService accountServiceSpy = new AccountService();

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    /*直接调用方法: 走真实的业务逻辑*/
    @Test
    public void testGetName() {
        String name = accountServiceSpy.getName();
        Assertions.assertEquals("erick", name);
    }

    @Test
    public void testGetInfo() {
        /*spy： 打桩后，走虚拟的业务逻辑*/
        Mockito.doReturn(new ArrayList<>()).when(accountServiceSpy).getBasicInfo();
        List<String> result = accountServiceSpy.getInfo();
        Assertions.assertEquals(1, result.size());
    }
}
```

## 4. 静态方法

- mockito core 3.4以上，支持对静态方法的测试
- 如果一个方法里面调用了静态方法，可以对静态方法进行mock结果，验证可以通过正常断言或者静态方法被调用次数

### 4.1 静态方法

```java
package com.erick.mall.utils;

import java.util.List;

public class ErickUtils {

    /*无返回值，无参数*/
    public static void jump() {
        System.out.println("I am jumping");
    }

    /*有返回值，无参数*/
    public static String reportName() {
        return "Erick";
    }

    /*有参数无返回值*/
    public static void store(String userName, List<String> info) {
        info.add(userName);
    }

    /*有参数有返回值*/
    public static String getSecretKey(String keyName) {
        return keyName.toUpperCase();
    }

    /*有参数有返回值*/
    public static String getKey(String key) {
        return key.toUpperCase();
    }
}
```

### 4.2 被测试类

```java
package com.erick.mall.service;

import com.erick.mall.utils.ErickUtils;

import java.util.ArrayList;
import java.util.List;

public class CarService {

    public String getName() {
        return "erick";
    }

    public void jump() {
        ErickUtils.jump();
    }

    /*无返回值无参数*/
    public String reportColor() {
        return ErickUtils.reportName();
    }

    /*有参数无返回值*/
    public void storeInfo(String userName, String address) {
        List<String> info = new ArrayList<>();
        info.add(address);
        ErickUtils.store(userName, info);
    }

    /*有参数有返回值*/
    public String getKey(String key) {
        return ErickUtils.getKey(key);
    }

    /*根据入参决定是否调用util类*/
    public void judgeByParam(boolean flag) {
        if (flag) {
            ErickUtils.jump();
        }
    }
}
```

### 4.3 测试方法

```java
package com.erick.mall.service;

import com.erick.mall.utils.ErickUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class CarServiceTest {
    private CarService carService;

    @BeforeEach
    void init() {
        carService = new CarService();
    }

    @Test
    public void testGetName() {
        String name = carService.getName();
        Assertions.assertEquals("erick", name);
    }

    @Test
    public void testJump() {
        try (MockedStatic<ErickUtils> mockErickUtils = Mockito.mockStatic(ErickUtils.class)) {
            /*无参数无返回值*/
            mockErickUtils.when(ErickUtils::jump).then(invocationOnMock -> null);
            /*调用服务*/
            carService.jump();
            mockErickUtils.verify(ErickUtils::jump, Mockito.times(1));
        }
    }

    @Test
    public void testReport() {
        try (MockedStatic<ErickUtils> mockErickUtils = Mockito.mockStatic(ErickUtils.class)) {
            /*无参数有返回值*/
            mockErickUtils.when(ErickUtils::reportName).thenReturn("mock");
            // 调用服务
            String color = carService.reportColor();
            Assertions.assertEquals("mock", color);
            
            // 可以验证静态方法的调用次数 
            mockErickUtils.verify(ErickUtils::reportName, Mockito.times(1));
        }
    }

    @Test
    public void testStoreInfo() {
        try (MockedStatic<ErickUtils> mockErickUtils = Mockito.mockStatic(ErickUtils.class)) {
            /*有参数无返回值*/
            mockErickUtils.when(() -> ErickUtils.store(Mockito.any(), Mockito.any())).then(invocationOnMock -> null);
            // 调用服务
            carService.storeInfo("mock", "mock");
            /*验证服务被调用*/
            mockErickUtils.verify(() -> ErickUtils.store(Mockito.any(), Mockito.any()), Mockito.times(1));
        }
    }

    @Test
    public void testGetKey() {
        try (MockedStatic<ErickUtils> mockErickUtils = Mockito.mockStatic(ErickUtils.class)) {
            /*有参数有返回值*/
            mockErickUtils.when(() -> ErickUtils.getKey(Mockito.any())).thenReturn("HAHA");
            // 调用服务
            String result = carService.getKey("nice");
            /*验证服务被调用*/
            Assertions.assertEquals("HAHA", result);
            mockErickUtils.verify(() -> ErickUtils.getKey(Mockito.any()), Mockito.times(1));
        }
    }

    @Test
    public void testJudgeByParam() {
        try (MockedStatic<ErickUtils> mockErickUtils = Mockito.mockStatic(ErickUtils.class)) {
            mockErickUtils.when(ErickUtils::jump).then(invocationOnMock -> null);
            // 调用服务
            carService.judgeByParam(false);
            /*验证服务没被调用*/
            mockErickUtils.verifyNoInteractions();
        }
    }
}

```

# SpringBoot-Mockito

- Springboot 2.2.0 版本引入junit 5作为单元测试默认库

```bash
# 测试：
- Junit 5作为测试框架,  Mockito作为Mock和打桩, mockito-inline作为静态方法的支持
```

```xml
<dependencies>
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.erick</groupId>
    <artifactId>springboot-mockito</artifactId>
    <version>1.0-SNAPSHOT</version>
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>

    <dependencies>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version>2.7.3</version>
        </dependency>

        <!-- spring-boot-starter： 继承了junit5， mockito -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <version>2.7.3</version>
            <scope>test</scope>
        </dependency>

        <!--支持静态方法-->
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-inline</artifactId>
            <version>4.7.0</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.24</version>
        </dependency>
    </dependencies>

</project>
```

![image-20220830232336183](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220830232336183.png)

## 1. 入门案例

```bash
# 方式一： 不启动主启动类的测试
# 激活Mockito的注解
# org.junit.jupiter.api.extension.ExtendWith;
@ExtendWith(SpringExtension.class)  

# 将该类的对应的Spring的bean放在容器中
# org.springframework.test.context.ContextConfiguration;
@ContextConfiguration(classes = AwsService.class) 

# 将被测试类注入到测试中
@Autowired

# 将被测试类的依赖类，通过DI注入到测试类中
# org.springframework.boot.test.mock.mockito.MockBean;
 @MockBean
 
 
 # 方式二： 直接在被测试类上加， 这样就会加载主启动类，同时完成测试
 # 使用spring容器功能， 检验项目是否能够启动， 时间稍微长
 @SpringBootTest
```



```java
package com.erick.service;

import com.erick.constant.ErickConstant;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SnsService {

    public Map<String, Object> getSnsTopic(String topicName) {
        Map<String, Object> info = new HashMap<>();
        info.put(ErickConstant.TOPIC_NAME, topicName);
        info.put(ErickConstant.TOPIC_REGION, "us-east-1");
        return info;
    }
}
```

```java
package com.erick.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AwsService {

    @Autowired
    private SnsService snsService;

    public Map<String, Object> getSnsInfo(String topicName) {
        int a = 2;
        int b = 3;
        return snsService.getSnsTopic(topicName);
    }
}
```

```java
package com.erick.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)  // 开启spring容器功能
@ContextConfiguration(classes = AwsService.class) // 将该类的对应的Spring的bean放在容器中
public class AwsServiceTest {

    @Autowired
    private AwsService awsService;

    /*mock的B类的依赖，同时注入到上面的测试类中*/
    @MockBean
    private SnsService snsService;

    @Test
    public void testGetSnsInfo() {
        Mockito.when(snsService.getSnsTopic(Mockito.any())).thenReturn(new HashMap<>());
        Map<String, Object> snsInfo = awsService.getSnsInfo("mock");
        Assertions.assertEquals(0, snsInfo.size());
    }
}
```

