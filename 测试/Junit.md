# Junit 5

```bash
# Junit: Junit Platform,      Junit Jupiter,      Junit Vintage
- Junit Platform:     JVM上启动测试框架的基础，不仅支持Junit自制的测试引擎， 其他测试引擎也可以接入
- Junit Jupiter:      Junit5 核心， 内部包含测试引擎，用于在Junit Platform 上运行
- Junit Vintage:      提供了兼容Junit3， Junit4的测试引擎
```

## 1. 依赖

- 切记Junit5和Junit4别混用

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter-api</artifactId>
  <version>5.9.0</version>
  <scope>test</scope>
</dependency>
```

![image-20220831120413871](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220831120413871.png)

## 2. 案例

### SRC

```java
package com.erick.demo01;

public class AwsService {
    public String getTopicName(String topicName) {
        return topicName.toUpperCase();
    }
}
```

### UT

```java
package com.erick.demo01;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AwsServiceTest {
    private AwsService service;

    @BeforeEach
    public void init() {
        /*类的初始化，在init里面做*/
        service = new AwsService();
    }

    @Test
    public void getTopicNameTest() {
        String topicName = "erick";
        String result = service.getTopicName(topicName);
        // 断言
        Assertions.assertEquals(result, "ERICK");
    }

    @AfterEach
    public void close() {
        System.out.println("test finish");
    }
}
```

## 3. 断言机制

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

## 4. 常见注解

```bash
# @BeforeAll   @AfterAll  
- 该类中的所有测试方法运行前或者后运行
- 必须是static修饰

# @Disabled
- 禁用掉(跳过)某个测试方法

# @Timeout(value = 2, unit = TimeUnit.SECONDS)
- 测试方法是否超时： 如果超时则报错
```

## 5. 案例精进

### 5.1 BeforeAll vs BeforeEach

- BeforeAll: 作用在静态方法上，初始化一些整个类都会使用到的资源，且各个方法对该资源使用不会冲突
- BeforeEach： 初始化一些，各个方法使用时候，可能会冲突的资源

#### SRC

```java
package com.erick.demo02;

public class BrandService {

    public String getName(String name){
        return name.toUpperCase();
    }

    public String getInfo(String name){
        return name.toLowerCase();
    }
}
```

#### UT

```java
package com.erick.demo02;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BrandServiceTest {

    private BrandService;
    @BeforeAll
    public static void once() {
        System.out.println("beforeAll只会加载一次");
    }

    @BeforeEach
    public void init(){
        service = new BrandService();
        System.out.println("coming");
    }

    @Test
    public void getNameTest(){
        String name = "erick";
        String result = service.getName(name);
        Assertions.assertEquals(result,"ERICK");
    }

    @Test
    public void getInfoTest(){
        String info = "BEIJING";
        String result = service.getInfo(info);
        Assertions.assertEquals(result,"beijing");
    }
}
```

### 5.2 组合断言

#### SRC

```java
package com.erick.demo03;

import java.util.HashMap;
import java.util.Map;

public class SnsService {
    public Map<String, String> getSnsInfo(String name, String region) {
        Map<String, String> info = new HashMap<>();
        info.put("name", name);
        info.put("region", region);
        return info;
    }
}
```

#### UT

- 组合断言，可以拆分成单个断言

```java
package com.erick.demo03;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class SnsServiceTest {
    private SnsService service = new SnsService();

    @Test
    public void getSnsInfoTest() {
        String name = "aws";
        String region = "us-east";
        Map<String, String> info = service.getSnsInfo(name, region);
        Assertions.assertAll("组合测试",
                () -> Assertions.assertEquals(2, info.size()),
                () -> Assertions.assertEquals("aws", info.get("name")),
                () -> Assertions.assertEquals("us-east", info.get("region")));
    }
}
```

### 5.3 超时断言

#### SRC

```java
package com.erick.demo04;

public class HeavyService {

    public void heavyWork() {
        System.out.println("coming");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("out");
    }
}
```

#### UT

```java
package com.erick.demo04;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

public class HeavyServiceTest {

    private HeavyService service = new HeavyService();

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    public void heavyTest() {
        service.heavyWork();
    }
}
```

### 5.4 异常断言

#### SRC

```java
package com.erick.demo05;

import java.util.NoSuchElementException;

public class MessageService {

    public int validateId(int id) {
        if (id == 1) {
            throw new IllegalArgumentException();
        }

        if (id == 2) {
            throw new NoSuchElementException();
        }

        if (id == 3) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return id;
    }
}
```

#### UT

```java
package com.erick.demo05;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

public class MessageServiceTest {

    private MessageService service = new MessageService();

    @Test
    public void correctIdTest() {
        int i = service.validateId(5);
        Assertions.assertEquals(i, 5);
    }

    @Test
    public void illegalIdTest() {
        int id = 1;
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.validateId(id));
    }

    @Test
    public void noSuchIdTest() {
        int id = 2;
        Assertions.assertThrows(NoSuchElementException.class,
                () -> service.validateId(id));
    }

    @Test
    public void outOfIndexdTest() {
        int id = 3;
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> service.validateId(id));
    }
}
```

# Mockito-@Mock

```bash
# 场景
- 场景一： A类方法 ---> 外部Client或者Connection，不希望去真实的调用Client和Connection
- 场景二： A类注入B类，A类方法 ---> B类方法，(不希望B类代码实际运行，因为B类的代码已经自测)

- B类可以是本项目其他的类，也可以是一些第三方jar包里面的类
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
```

## 1. 零值处理

- 被Mock的对象，执行方法时候，不会走实际的调用，默认返回结果为该方法返回值的'空值'

### setter注入

#### SRC

```java
package com.erick.demo06;

public class AService {

    private BService bService;

    public void setbService(BService bService) {
        this.bService = bService;
    }

    public String getInfo() {
        // 调用B类,空值就是null
        String bServiceName = bService.getBServiceName();
        return "A-Service" + bServiceName;
    }
}
```

```java
package com.erick.demo06;

public class BService {
    public String getBServiceName() {
        System.out.println("B-service");
        return "BService"; 
    }
}
```

#### UT

```java
package com.erick.demo06;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AServiceTest {

    private AService aService;

    @Mock
    private BService bService;


    @BeforeEach
    public void init() {
        // 代表本类开启了Mockito功能
        MockitoAnnotations.openMocks(this);
        aService = new AService();
        aService.setbService(bService); // 依赖注入
    }

    @Test
    public void getInfoTest() {
        String info = aService.getInfo();
        Assertions.assertEquals(info, "A-Servicenull");
    }
}
```

### constructor注入

#### SRC

```java
package com.erick.demo07;

public class AService {

    private final BService bService;

    public AService(BService bService) {
        this.bService = bService;
    }

    public int getAge() {
        // 调用B类,空值就是0
        int age = bService.getAccount();
        return age;
    }
}
```

```java
package com.erick.demo07;

public class BService {
    public int getAccount() {
        System.out.println("B-service");
        return 10;
    }
}
```

#### UT

```java
package com.erick.demo07;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AServiceTest {

    private AService aService;

    @Mock
    private BService bService;


    @BeforeEach
    public void init() {
        // 代表本类开启了Mockito功能
        MockitoAnnotations.openMocks(this);
        aService = new AService(bService); // constructor注入
    }

    @Test
    public void getInfoTest() {
        int age = aService.getAge();
        Assertions.assertEquals(age, 0);
    }
}
```

### new注入

- A类中调用B，但是BService是在方法里面创建的，这种就没办法测
- 因此，一般第三方的Service，尽可能<font color=orange>成员变量</font>，或者通过<font color=orange>方法参数</font>传递进来

#### SRC

```java
package com.erick.demo08;

public class PhoneService {

    public long process(OuterApi api) {
        System.out.println("coming");
        OuterApi api = new OuterApi();
        long costTime = api.callApi();
        System.out.println("ending");
        return costTime;
    }
}
```

```java
package com.erick.demo08;

import java.util.concurrent.TimeUnit;

public class OuterApi {

    public long callApi() {
        long start = System.currentTimeMillis();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return System.currentTimeMillis() - start;
    }
}
```

#### UT

```java
package com.erick.demo08;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PhoneServiceTest {

    private PhoneService phoneService;

    @Mock
    private OuterApi outerApi;

    @BeforeEach
    public void init() {
        phoneService = new PhoneService();
        MockitoAnnotations.openMocks(this);
        //outerApi根本没有和phoneService发生关系
    }

    @Test
    public void process() {
        long process = phoneService.process();
    }
}
```

## 2. 打桩处理

- 被Mock的对象，执行方法时候，不会走实际的调用，可以给指定返回值
- 比如调用的B类中获得json，后面还要处理该json
- 同样支持上面两种注入方式
- Mockito.when(bService.getNumber()).thenReturn(3);

### SRC

```java
package com.erick.demo09;

public class AService {

    private BService bService;

    public AService(BService bService) {
        this.bService = bService;
    }

    public int getTotalNumber() {
        int number = bService.getNumber();
        return number * 5;
    }
}
```

```java
package com.erick.demo09;

public class BService {

    public int getNumber() {
        return 10;
    }
}
```

### UT

```java
package com.erick.demo09;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AServiceTest {

    private AService aService;

    @Mock
    private BService bService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        aService = new AService(bService);// 依赖注入
    }

    @Test
    public void testTotalNumber() {
        // 打桩处理
        Mockito.when(bService.getNumber()).thenReturn(3);
        int number = aService.getTotalNumber();
        Assertions.assertEquals(15, number);
    }
}
```

## ❤️换个写法

### SRC

```java
package com.lucy.demo01;

public class AService {

    private BService bService;

    public AService(BService bService) {
        this.bService = bService;
    }

    public String getTag() {
        return bService.getPhoneName() + "test";
    }
}
```

```java
package com.lucy.demo01;

public class BService {

    public String getPhoneName() {
       return "xiaomi";
    }
}
```

### UT

```java
package com.lucy.demo01;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AServiceTest {

    private AService aService;

    private BService bService;

    @BeforeEach
    public void init() {
        bService = Mockito.mock(BService.class); // mock 对象
        aService = new AService(bService);
    }

    @Test
    public void getPhoneTest() {
        // 1. 打桩
        Mockito.when(bService.getPhoneName()).thenReturn("apple");
        // 2. 调用
        String phoneNumber = aService.getTag();
        Assertions.assertEquals(phoneNumber,"apple"+ "test");
    }
}
```

# Mockito-静态方法

- 场景：A类方法-->B类方法，如果B类方法是静态的，那就玩不转了
- mockito core 3.4以上，支持对静态方法的测试
- 如果一个方法里面调用了静态方法，可以对静态方法进行mock结果，验证可以通过正常断言或者静态方法被调用次数
- 但是打桩的类，就不是代理类了，但是功能还是正常的

```xml
<!--mockito-inline: 支持静态方法的mock-->
<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-inline</artifactId>
  <version>4.7.0</version>
  <scope>test</scope>
</dependency>
```

## 无参-无返回值

- 验证调用次数

### SRC

```java
public class ErickUtil {
    public static void jump() {
        System.out.println("I am jumping");
    }
}
```

```java
public class CarService {
    public void carJump() {
        ErickUtil.jump();
        ErickUtil.jump();
    }
}
```

### UT

```java

public class CarServiceTest {

    private CarService carService = new CarService();

    @Test
    public void testCarJump() {
        // mock static method
        try (MockedStatic<ErickUtil> mockErickUtil = Mockito.mockStatic(ErickUtil.class)) {
            // 跳过static的方法调用,不要激活静态方法
            mockErickUtil.when(ErickUtil::jump).then(invocationOnMock -> null);
            // 调用服务
            carService.carJump();
            // 验证调用次数
            mockErickUtil.verify(ErickUtil::jump, Mockito.times(2));
        }
    }
}
```

## 无参-有返回值

### SRC

```java
public class ErickUtil {
    public static String getName() {
        return "Erick";
    }
}
```

```java
public class CarService {
    public String reportColor() {
        String name = ErickUtil.getName();
        return name + "RED";
    }
}
```

### UT

```java

public class CarServiceTest {

    private CarService carService = new CarService();

    @Test
    public void testCarJump() {
        // mock static method
        try (MockedStatic<ErickUtil> mockErickUtil = Mockito.mockStatic(ErickUtil.class)) {
            //打桩
            mockErickUtil.when(ErickUtil::getName).thenReturn("LUCY");
            // 调用服务
            String color = carService.reportColor();
            // 验证调用结果
            Assertions.assertEquals(color, "LUCYRED");
        }
    }
}
```

## 有参-无返回值

### SRC

```java
public class ErickUtil {
    public static void store(String userName, List<String> info) {
        info.add(userName);
    }
}
```

```java
public class CarService {
    public void storeInfo(String userName, String address) {
        List<String> info = new ArrayList<>();
        info.add(address);
        info.add("123");
        ErickUtil.store(userName, info);
    }
}
```

### UT

```java
public class CarServiceTest {

    private CarService carService = new CarService();

    @Test
    public void testCarJump() {
        // mock static method
        try (MockedStatic<ErickUtil> mockErickUtil = Mockito.mockStatic(ErickUtil.class)) {
            //打桩: 传递任何参数时，都不激活静态方法
            mockErickUtil.when(() -> ErickUtil.store(Mockito.any(), Mockito.any())).then(invocationOnMock -> null);
            // 调用服务
            carService.storeInfo("mockName", "mockAddress");

            // 验证调用次数
            mockErickUtil.verify(() -> ErickUtil.store(Mockito.any(), Mockito.any()), Mockito.times(1));
        }
    }
}
```

## 有参-有返回值

### SRC

```java
public class ErickUtil {
    public static String encrypt(String keyName) {
        return keyName.toUpperCase();
    }
}
```

```java
public class CarService {
    public String processKey(String key) {
        return ErickUtil.encrypt(key) + "123";
    }
}
```

### UT

```java
public class CarServiceTest {

    private CarService carService = new CarService();

    @Test
    public void testCarJump() {
        // mock static method
        try (MockedStatic<ErickUtil> mockErickUtil = Mockito.mockStatic(ErickUtil.class)) {
            //打桩: 传递任何参数时，都不激活静态方法
            mockErickUtil.when(() -> ErickUtil.encrypt(Mockito.any())).thenReturn("HAHA");
            // 调用服务
            String key = carService.processKey("MOCK");

            // 验证结果
            Assertions.assertEquals(key, "HAHA123");

            // 验证调用次数
            mockErickUtil.verify(() -> ErickUtil.encrypt(Mockito.any()), Mockito.times(1));
        }
    }
}
```



















# Mockito-@Spy

```bash
# 场景：
- A类中:方法a ---> 方法b， b方法已经测试过了，因此在测试a方法时跳过b方法

# 用法
- 被spy的测试对象，默认走实际方法调用
- 如果打桩，就会跳过打桩的方法，mock一个假的结果

# 饮用方式： 只是书写的两种方式，并无区别
- Mockito.spy()

- @Spy + MockitoAnnotations.openMocks(this);

# 这种打桩方法，如果本类中被打桩的方法是私有方法，则无法实现， 可以改为protected来实现
```

## 1. @Spy

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

