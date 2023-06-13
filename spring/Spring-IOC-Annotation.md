- 注解和xml配置功能一样，是IOC的两种实现方式而已
- 通过在类上的注解，将该类放在容器中

# Bean

## 1. 包扫描

- spring在启动时，需要扫描的包，找出spring的annotation

- 配置component-scan后，其实就是注入了一些Spring的BeanFactoryPostProcess来处理这些，识别对应包上类的注解，然后将这些类放在BeanDefinition中

```xml
<!--具体扫描的包结构中的spring的注解-->
<context:component-scan base-package="com.lucy">
    <!--排除指定的spring的注解-->
    <!--1. 如果指定的是Component，会一起排除掉对应的Component的子注解-->
    <context:exclude-filter type="annotation" expression="org.springframework.stereotype.Service"/>

    <!--2. 排除指定的类：该类的所有spring注解都不会被加载-->
    <context:exclude-filter type="assignable" expression="com.lucy.ComputerService"/>
</context:component-scan>
```

```xml
<!--具体扫描的包结构中的spring的注解
  1. use-default-filters:  默认为true，扫描所有的包，排除excluded的
  2. use-default-filters=false时候，不会扫描任何包，只扫include的包-->
<context:component-scan base-package="com.lucy" use-default-filters="false">
    <context:include-filter type="assignable" expression="com.lucy.ComputerService"/>
</context:component-scan>
```

```java
// 包含bean.xml， spring注解来进行bean创建和di，容器使用ClassPathXmlApplicationContext
ApplicationContext context = new ClassPathXmlApplicationContext("erick.xml");
```

## 2. 配置类

### 2.1 @ComponentScan

- 项目的配置包类，可以放在任何一个类上

```bash
# @ComponentScan(basePackages = "com.erick")
- 1. 完全取消xml配置文件, 并更换容器的实现类
- 2. 扫描该包下的spring的注解
- 3. 该类并不会放在spring容器中
- 替换： <context:component-scan base-package="com.erick"></context:component-scan>
```

```java
package com.erick;

import org.springframework.context.annotation.ComponentScan;

/*可以为任何一个类*/
@ComponentScan(basePackages = "com.lucy")
public class ProjectConfig {
}
```

```java
package com.erick;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Test01 {

    @Test
    void test03() {
        // 更换的新容器
        ApplicationContext context = new AnnotationConfigApplicationContext(ProjectConfig.class);
        ErickService bean = context.getBean(ErickService.class);
        System.out.println(bean);
    }
}
```

### 2.2 @PropertySource

- 项目中的一些基础属性，可以通过@Value来获取

```bash
# @PropertySource(value = "classpath:jdbc.properties")
-  <context:property-placeholder location="jdbc.properties"></context:property-placeholder>

# 1. 作用在项目的任何一个配置文件上或者容器类上
- @ComponentScan(basePackages = "com.lucy")
- @Component， @Configuration等

# 2. 也可以作用在具体的要使用该配置文件的类上
- 比较推荐，对应的配置文件的属性，IDEA会提示具体的哪个key-value被使用了
```

```java
package com.erick;

import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ToString
@PropertySource(value = "classpath:jdbc.properties")
public class ErickService {

    @Value("${erick.url}")
    private String userName;
}
```

### 2.3 @Import

```bash
# @Import
- 要和其他容器类组件结合使用，才能保证@Import生效   比如@Configuration, @Component,@ComponenetScan
- 对应的@Import导入的类，也会放在容器中
- 默认组件名字： 对应类的全类名
```

#### 场景一

- @Bean：一旦通过@Import导入这个类，该类会放在容器中，@Bean也会生效
- 情况一： 导入第三方的，当前项目包没扫的类，即使对应类上有@Configuration
- 情况二： 本项目的，没有被@Configuration修饰的@Bean

```java
package com.erick.outer;

import org.springframework.context.annotation.Bean;

/*两种情况：该配置可能是当前项目扫不到的
 *          当前项目能扫到，但是类上没有@Component等注解*/
public class OuterBean {

    @Bean
    public Dog getDog() {
        return new Dog();
    }
}
```

#### 场景二

- 本项目不能扫的，第三方的，使用spring的，@Component及其其中的DI的属性
- 如果该类中包含@Autowired等其他属性类，这些类也要被导入进来

```java
package com.erick.boot;

import com.erick.outer.CitiService;
import com.erick.outer.ICGService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

// 当前包扫描路径只扫com.ericker.boot包
@Configuration
@Import(value = {CitiService.class, ICGService.class, Dog.class})
public class ErickService {
}
```

```java
package com.erick.outer;

import org.springframework.stereotype.Service;

@Service
public class ICGService {
}
```

```java
package com.erick.outer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CitiService {
    /*该属性也可以被注入*/
    @Autowired
    private ICGService icgService;

    public void work() {
        System.out.println(icgService);
    }
}
```

```java
package com.erick.boot;

public class Dog {
}
```

#### 场景三

- 普通的Java类，不管是不是本项目的类，可以通过Import，将其放在spring容器中
- Import后，只会通过无参构造来创建对象。 如果需要带参构造来创建，则通过@Configuration+@Bean的方式

```java
package com.erick.service;

public class PhoneService {
    public PhoneService() {
        System.out.println("Phone-无参构造");
    }
    
    public PhoneService(String name) {
        System.out.println("Phone-带参构造");
    }
}
```

### 2.4 @Primary

- 存在多个相同类型的对象时候，注入的时候，优先被注入

```bash
# @Target({ElementType.TYPE, ElementType.METHOD})
- 和@Bean，@Component一起使用
```

## 3. 初始化

### 3.1 @Component

-  @Component            @Controller             @Service           @Repository 
-  应用场景：本项目中的对象放在容器中

```bash
- 四个作用一样，作用在类上，将该类放在容器中
- component为其他三个的父类， 普通组件
- 其他三个，一般用于区分三层架构
```

```java
package com.spring.ioc.annotation;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 1. 放在类上，通过类全名反射创建对象，并将其放在spring容器中
 * 2. 默认key是类名的驼峰，支持自定义
 * 3. 默认为单例，可以配置为多例
 */
@Component(value = "ordinary_bean") // 注解如果对应的属性是value，则这种属性可以不写key
public class OrdinaryBean {
}
```

### 3.2. @Configuration+@Bean

- @Configuration： 作用在类上，告诉spring这是一个配置类
- @Bean： 作用在方法上，将方法返回值放在容器中，必须作用在能被spring扫到的类里面
- 应用场景：非自定义的Bean，其他第三方，压根就没被spring管理的类
- 类中各个bean按照声明顺序加载, 如果某个bean依赖于后面声明的bean，则先加载后面的

```bash
# 注意
- 有些三方类，必须要先成为@Bean才能被项目所使用，因为spring会对一些类的Bean,进行后置处理
```

#### full模式

```bash
# @Configuration(proxyBeanMethods = true): 默认开启
- 当前类生成一个代理对象，代理对象放在容器中:     com.erick.JmsConfig$$EnhancerBySpringCGLIB$$d736e239@49cb9cb5
- 通过该配置类，去调用配置类里面 方法获得的对象，是用代理对象来调用的，是先检查容器中是否存在，保证组件单实例
- 启动较慢，但是可以保证所有注册组件之间依赖的，都是从容器中获取的
```

```java
package com.erick;

import com.erick.boot.House;
import com.erick.boot.Room;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*first room
second room
third room
first house
second house*/
@Configuration(proxyBeanMethods = true)
public class JmsConfig {

    // Bean默认的名字是方法名 
    @Bean
    public Room firstRoom() {
        System.out.println("first room");
        return new Room();
    }

    @Bean
    public Room secondRoom() {
        System.out.println("second room");
        return new Room();
    }

    @Bean
    public House getfirstHouse() {
        Room thirdRoom = getThirdRoom(); // 会从容器中去找
        System.out.println("first house");
        return new House("beijing", 10);
    }

    // 会从容器中去找，和proxyBeanMethods无关 
    // 带参数时，也是从容器中去找的：包含普通属性(@Value)和引用属性(@Autowired,@Qualifier等)
    // 方法参数时，@Qualifier可以单独使用
    @Bean
    public House getSecondHouse(@Qualifier(value = "getThirdRoom") Room room){ 
        System.out.println("second house");
        return new House("shanghai", 12);
    }

    @Bean
    public Room getThirdRoom() {
        System.out.println("third room"); // 只会打印一次
        return new Room();
    }
}
```

#### lite模式

```bash
# @Configuration(proxyBeanMethods = false)
- 当前类本身会放在容器中： com.erick.JmsConfig@1339e7aa
- 组件依赖时， 如果直接调用方法的时候， 不会从容器中去检查是否存在
- 启动比较快
```

```java
package com.erick;

import com.erick.boot.House;
import com.erick.boot.Room;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
public class JmsConfig {
    
    @Bean
    public House getFirstHouse() {
        Room thirdRoom = getRoom(); // 不会从容器中去找，直接调用方法
        System.out.println("first house");
        return new House("beijing", 10);
    }

    /*比较推荐该注入，这样不会因为错误的设置了proxyBeanMethods，导致直接调用方法时获取的不是容器中*/
    @Bean
    public House getSecondHouse(@Qualifier(value = "getRoom") Room room) { // 会从容器中去找，和proxyBeanMethods无关
        System.out.println("second house");
        return new House("shanghai", 12);
    }

    @Bean
    public Room getRoom() {
        System.out.println("room"); // 会打印2次
        return new Room();
    }
}
```



## 4. DI

### 4.1 @Vaue

- 基本类型, String属性，Date等注入
- 作用在字段，方法上(普通方法)，形参上
- 凡是被@Value作用的属性，普通方法，都会在对象初始化的时候，主动调用
- 作用范围：必须是spring管理的类

```java
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
```

#### FIELD

- 直接利用反射对字段进行赋值

```xml
  <context:component-scan base-package="com.erick"></context:component-scan>
  <!--读取对应的数据-->
  <context:property-placeholder location="jdbc.properties"></context:property-placeholder>
```

```properties
erick.url=www.baidu.com
erick.password=123456
erick.username=lucy
```

```java
package com.erick.service;

import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@ToString
@Component
public class FirstService {

    /*1.可以写死*/
    @Value("erick")
    private String name;

    @Value("2022/09/14")
    private Date createdTime;

    /*2. 通过spring-el表达式来读取配置文件中属性*/
    @Value("${erick.url}")
    private String url;

    @Value("{erick.password}")
    private String password;

    /*如果配置了，则取配置值。否则取默认值erick*/
    @Value("${erick.username:erick}")
    private String userName;
}
```

#### METHOD

- 一个方法，无视权限，只要被@Value标记了，那么在DI时候就会被执行

```java
package com.erick.service;

import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@ToString
public class SecondService {

    private String userName;

    private String password;

    /*执行：无视权限*/
    @Value("xxx")
    private int xxx01() {
        System.out.println("xxx01被执行了");
        return 0;
    }

    /*执行，并用xxx来接收@Value解析后的值*/
    @Value("erick")
    public void xxx02(String xxx) {
        System.out.println("xxx02被执行了");
        this.userName = xxx;
    }

    /*执行，所有行参都会被赋该@Value，形参必须类型匹配*/
    @Value("lucy")
    public void xxx03(String x, String y) {
        System.out.println("xxx03被执行了");
        this.password = x;
        System.out.println("xxx03中间步骤" + "x=" + x + " Y=" + y);
        this.password = y;
    }
}
```

#### PARAMETER

- 必须作用在spring能主动调用的方法上

```java
package com.erick.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThirdService {

    @Bean
    public String xxx04(@Value("abc") String x, @Value("ert") String y) {
        System.out.println("xxx04被执行了");
        System.out.println("xxx04中间步骤" + "x=" + x + " Y=" + y);
        return "success";
    }
}
```

### 4.2 @Autowired

- 一般进行对象引用的注入
- 凡是被@Autowired标记的，都会在对象初始化时，进行主动调用来注入

```java
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
```

```bash
# 匹配规则
1.  byType: 如果只发现一个该类型bean，匹配成功
2.  byName: 如果发现多个同类型bean，用field名字去同类型中的bean的名字匹配
3.  如果存在多个相同的bean type，且名字都和成员变量名对不上，则spring不知道到底该拿哪个，则报错
    NoUniqueBeanDefinationException
```

#### FIELD

```java
package com.erick.lucy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnimalService {

    /*可以注入成功*/
    @Autowired
    private Animal cat;

    /*可以注入成功:xxx进行匹配*/
    @Autowired
    private Animal xxx;

    public void show() {
        System.out.println(cat.getClass());
        System.out.println(xxx.getClass());
    }

}

@Component
class Cat implements Animal {
}

@Component(value = "xxx")
class Dog implements Animal {
}

interface Animal {

}
```

- spring不推荐使用-NPE
- 更加推荐使用构造器注入的方式

```bash
# 可能引发  NPE 问题，   但是只要required=false,就是安全的
# 默认：   required = true，只要找不到，就会在JVM启动时候报错
```

```java
package com.erick.lucy;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FruitService {

    @Autowired(required = false)
    private Fruit fruit;
    
    // 启动没问题，但是调用时候就会NPE 
    public void show() {
        fruit.say();
    }
}

@Data
class Fruit {
    public void say() {
        System.out.println("hello");
    }
}
```

#### METHOD

- 被@Auwowired标记的方法，会被调用

```java
package com.erick.lucy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnimalService {

    /*x和对应的当前type不匹配，则会报错*/
    @Autowired
    private void xxx01(Animal x) {
        System.out.println("xxx01执行");
    }

    @Autowired
    public int xxx02(Animal cat) {
        System.out.println("xxx02执行");
        System.out.println(cat.getClass());
        return 1;
    }

    @Autowired
    public int xxx03(Animal xxx) {
        System.out.println("xxx02执行");
        System.out.println(xxx.getClass());
        return 1;
    }
  
   // 参数，也可以定义为一个集合，那就会把容器中所有的这个对象放在List<Animal>中
    @Autowired
    public int xxx04(List<Animal> xxx) {
        System.out.println("xxx02执行");
        System.out.println(xxx.size());
        return 1;
    }
}

@Component
class Cat implements Animal {
}

@Component(value = "xxx")
class Dog implements Animal {
}

interface Animal {

}
```

#### CONSTRUCTOR

- 标记在哪个构造方法上，就会调用哪个来构建对象，只有一个构造方法时，不需要标注
- 不能同时注解在两个构造方法上
- 属性注入时：将对应的字段声明为final

```java
package com.erick.lucy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnimalService {
    public AnimalService() {
        System.out.println("无参构造");
    }
    
    public AnimalService(Animal cat) {
        System.out.println("一个参数" + cat.getClass());
    }

    /*匹配规则类似*/
    @Autowired
    public AnimalService(Animal cat, Animal xxx) {
        System.out.println("二个参数" + cat.getClass());
        System.out.println("二个参数" + xxx.getClass());
    }
}

@Component
class Cat implements Animal {
}

@Component(value = "xxx")
class Dog implements Animal {
}

interface Animal {

}
```

#### PARAMETER

- 必须标注在可以被spring主动调用的地方
- 但是IDEA会爆红，运行时候没问题？

```java
package com.erick.lucy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public String test(@Autowired Animal cat, @Autowired Animal xxx) {
        System.out.println(cat.getClass());
        System.out.println(xxx.getClass());
        return "success";
    }
}
```

### 4.3 @Autowired + @Qualifier

-  规则类似上面，不过变量名显示指定

```java
// @Qualifier:  package org.springframework.beans.factory.annotation;
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
```

```java
@Autowired
@Qualifier(value = "melon")
```

### 4.4 @Resource

```bash
# 注解说明
- java本身的注解
- java11被移除，需要额外添加依赖
- 是JSR-250定义的注解。Spring 在 CommonAnnotationBeanPostProcessor 实现了对JSR-250的注解的处理

# 适配性
- @Resource是java本身的注解，如果不使用spring框架，切换到其他的框架的时候，也能很好的适配
```

```xml
<dependency>
    <groupId>javax.annotation</groupId>
    <artifactId>javax.annotation-api</artifactId>
    <version>1.3.2</version>
</dependency>
```

```java
// @Resource
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
```

```bash
# @Resource: 包含name和type属性
- name和type都不指定：则按照类型注入
- 指定name：寻找对应的name
- 指定type：找唯一类型匹配的bean，找不到或者多个，抛出异常
- 指定name和type：寻找唯一的bean
```

```java
package com.nancy;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FruitService {
    @Resource(name = "apple", type = Apple.class)
    private Fruit apple;

    @Resource(name = "erickPeach")
    private Fruit erickPeach;
    
    @Resource
    private Fruit erickMelon;

    public void work() {
        System.out.println(apple.getClass());
        System.out.println(erickPeach.getClass());
        System.out.println(erickMelon.getClass());
    }
}

@Component(value = "erickMelon")
class Melon implements Fruit {
}

@Component(value = "erickPeach")
class Peach implements Fruit {
}

@Component
class Apple implements Fruit {
}

interface Fruit {
}
```

## 5. @Lazy

```bash
# 可以作用在@Component的四个，@Bean, 调用getBean时才加载
@Component
@Lazy
```

## 6. scope

```bash
# 可以作用在@Component的四个，@Bean, 默认都是单例
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
```

# 三方包

- 如果A-项目需要导入三方包的spring项目，不能直接在A-项目来配置@ComponentScan， 需要三方包主动来暴露

## 1. @Import

### 1.1 三方包

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>5.3.27</version>
    </dependency>
</dependencies>
```

```java
package com.daydreamer.jms;

public class EmsConfig {
}
```

```java
package com.daydreamer.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmsService {

    @Autowired
    private EmsConfig emsConfig;
}
```

```java
package com.daydreamer.jms;

import org.springframework.stereotype.Service;

@Service
public class KafkaService {
}
```

```java
package com.daydreamer;

import com.daydreamer.jms.EmsConfig;
import com.daydreamer.jms.EmsService;
import com.daydreamer.jms.KafkaService;
import org.springframework.context.annotation.Import;
// 声明自己要导出的哪些类
@Import(value = {EmsService.class, EmsConfig.class, KafkaService.class})
public class JmsScan {
}
```

### 1.2 调用方

- 通过import来导入的类，beanName是全限定类名

```xml
<dependency>
    <groupId>com.erick.daydreamer</groupId>
    <artifactId>jms-third-party</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

```java
package com.erick.config;

import com.daydreamer.JmsScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = "com.erick")
@Import(JmsScan.class) // 导入对应的类即可
public class ProjectConfig {
}
```

## 2. 注解版

### 2.1 三方包

- @Import升级注解
- 在原来基础上，添加注解，并@Import(JmsScan.class)

```java
package com.daydreamer.annotation;

import com.daydreamer.JmsScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Import(JmsScan.class)
public @interface EnableJms {
}
```

### 2.2 调用方

- 直接标记注解即可，不用再去@Import，因为注解本身@Import了
- 其实就是spring在解析配置类的时候，去解析了注解

```java
package com.erick.config;

import com.daydreamer.annotation.EnableJms;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.erick")
@EnableJms
public class ProjectConfig {
}
```

## 3. ImportSelector

#### 批量添加组件

- 定义一个类来实现ImportSelector接口，并进行批量注册。
- 只需要在一个地方去@Import该类，该类会放在容器中，同时调用selectImports来批量注册组件
- 可以扫本三方包下所有的bean，然后进行批量注册

```java
package com.erick;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 凡是@Import了ErickImportSelector
 * 1. 首先会把ErickImportSelector放在容器中
 * 2. 如果@Import的类，实现了ImportSelector接口，则会回调selectImports方法，将返回值也放在容器中
 */
public class ErickImportSelector implements ImportSelector {

    /**
     * @return - 需要被注册到spring容器中的bean的全限定类名
                 注册的bean的beanName: 全限定类名
     */
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        System.out.println("coming");
        String[] beanNames = {"com.erick.entity.AwsService", "com.erick.entity.SnsService"};
        return beanNames;
    }
}
```

```java
package com.erick.config;

import com.erick.ErickImportSelector;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = "com.erick")
@Import(value = {ErickImportSelector.class})
public class ProjectConfig {
}
```

```java
package com.erick.config;

import com.erick.ErickImportSelector;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {ErickImportSelector.class})
public class RandomConfig {
}
```

#### 获取AnnotationMetadata

- 一个类@Import了该实现类，则这个类上的其他注解信息，会被封装在AnnotationMetadata中
- 继承第三方框架，方便扫描第三方包自己扫自己的包，然后进行批量注册

```java
package com.erick;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Set;

/**
 * 凡是@Import了ErickImportSelector
 * 1. 首先会把ErickImportSelector放在容器中
 * 2. 如果@Import的类，实现了ImportSelector接口，则会回调selectImports方法，将返回值也放在容器中
 */
public class ErickImportSelector implements ImportSelector {

    /**
     * @param importingClassMetadata AnnotationMetadata
     *                               - A-类只要@Import了当前ErickImportSelector
     *                               - 则AnnotationMetadata就是A-类上的其他注解的信息
     */
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Set<String> annotationTypes = importingClassMetadata.getAnnotationTypes();

        /*可以获取到对应的类上的注解，就是springboot的扫包原理*/
        for (String type : annotationTypes) {
            if (type.equals(ComponentScan.class.getName())) {
                Map<String, Object> componentScan = importingClassMetadata.getAnnotationAttributes(type);
                System.out.println(componentScan);
            }
        }
        return new String[0];
    }
}
```

```java
package com.erick.config;

import com.erick.ErickImportSelector;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = "com.erick")
@Import(value = {ErickImportSelector.class})
public class ProjectConfig {
}
```

## 4. ImportBeanDefinitionRegistrar

- 类似3.3，提供了更加强大的功能，也是第三方通过自己配置的包扫描，批量向spring注入组件

```java
package com.erick.config;

import com.erick.entity.AwsService;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class NancyImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        /*importingClassMetadata也是三方包自己扫自己的包，然后通过registry来注册*/
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClassName(AwsService.class.getName());
        registry.registerBeanDefinition("awsService", beanDefinition);
    }
}
```

```java
package com.erick.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = "com.erick")
@Import(value = {NancyImportBeanDefinitionRegistrar.class})
public class ProjectConfig {
}
```



# 生命周期

![image-20230601155352771](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230601155352771.png)

## 1.@Component

```java
package com.erick.service;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class FirstService implements InitializingBean, BeanNameAware {

    static {
        System.out.println("静态代码块");
    }

    public FirstService(@Value("erick") String name) {
        System.out.println("构造方法：" + name);
    }

    @Autowired
    private void xxx(Dog xxx) {
        System.out.println("set方法，属性注入");
    }

    @PostConstruct
    public void postConstruct() {
        System.out.println("post-construct");
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("InitializingBean - afterPropertiesSet");
    }

    @Override
    public void setBeanName(String name) {
        System.out.println("BeanNameAware -- setBeanName");
    }
}

@Component
class Dog {

}
```

```java
package com.erick.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErickBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FirstService) {
            System.out.println("ErickBeanPostProcessor -- postProcessBeforeInitialization");
        }

        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FirstService) {
            System.out.println("ErickBeanPostProcessor -- postProcessAfterInitialization");
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
```

## 2. @Configuration

```java
package com.erick.config;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.PostConstruct;

public class AnimalService implements BeanNameAware, InitializingBean {

    static {
        System.out.println("静态代码块");
    }

    private String name;

    public AnimalService(String name) {
        System.out.println("构造方法");
        this.name = name;
    }

    @PostConstruct
    public void construct() {
        System.out.println("post-construct");
    }

    public void initErick() {
        System.out.println("init-method");
    }

    @Override
    public void setBeanName(String name) {
        System.out.println("aware接口");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("InitializingBean--afterPropertiesSet");
    }
}
```

```java
package com.erick.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class JmsConfig {


    /*先于bean执行*/
    @PostConstruct
    public void init() {
        System.out.println("@Configuration--PostConstruct");
    }

    @Bean(initMethod = "initErick")
    public AnimalService get() {
        return new AnimalService("erick");
    }
}

```

# 注解扫描原理

```java
ApplicationContext context = new AnnotationConfigApplicationContext(ProjectConfig.class);
```

```java
// AnnotationConfigApplicationContext
public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
		this(); // 下一步
		register(componentClasses);
		refresh();
	}
```

```java
	public AnnotationConfigApplicationContext() {
		StartupStep createAnnotatedBeanDefReader = this.getApplicationStartup().start("spring.context.annotated-bean-reader.create");
		this.reader = new AnnotatedBeanDefinitionReader(this);// 下一步
		createAnnotatedBeanDefReader.end();
		this.scanner = new ClassPathBeanDefinitionScanner(this);
	}
```

```java
	public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
		this(registry, getOrCreateEnvironment(registry));
	}
```

```java
	public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry, Environment environment) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		Assert.notNull(environment, "Environment must not be null");
		this.registry = registry;
		this.conditionEvaluator = new ConditionEvaluator(registry, environment, null);
		AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry); // 下一步
	}
```

```java
	public static void registerAnnotationConfigProcessors(BeanDefinitionRegistry registry) {
		registerAnnotationConfigProcessors(registry, null); // 下一步
	}
```

```java
public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(
			BeanDefinitionRegistry registry, @Nullable Object source) {

		DefaultListableBeanFactory beanFactory = unwrapDefaultListableBeanFactory(registry);
		if (beanFactory != null) {
			if (!(beanFactory.getDependencyComparator() instanceof AnnotationAwareOrderComparator)) {
				beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);
			}
			if (!(beanFactory.getAutowireCandidateResolver() instanceof ContextAnnotationAutowireCandidateResolver)) {
				beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
			}
		}

		Set<BeanDefinitionHolder> beanDefs = new LinkedHashSet<>(8);

   // ConfigurationClassPostProcessor : 处理Configuration的对应的 BeanPostProcessor
		if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

    // @Autowired的BeanPostProcessor 
		if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		// Check for JSR-250 support, and if present add the CommonAnnotationBeanPostProcessor.
		if (jsr250Present && !registry.containsBeanDefinition(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		// Check for JPA support, and if present add the PersistenceAnnotationBeanPostProcessor.
		if (jpaPresent && !registry.containsBeanDefinition(PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition();
			try {
				def.setBeanClass(ClassUtils.forName(PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME,
						AnnotationConfigUtils.class.getClassLoader()));
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalStateException(
						"Cannot load optional framework class: " + PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME, ex);
			}
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		if (!registry.containsBeanDefinition(EVENT_LISTENER_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(EventListenerMethodProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_PROCESSOR_BEAN_NAME));
		}

		if (!registry.containsBeanDefinition(EVENT_LISTENER_FACTORY_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(DefaultEventListenerFactory.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_FACTORY_BEAN_NAME));
		}

		return beanDefs;
	}
```

## 1. ConfigurationClassPostProcessor

- 处理@Configuration，@Component, @Service, @Repository的类，BeanFactoryPostProcessor

![ConfigurationClassPostProcessor](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/ConfigurationClassPostProcessor.png)

```java
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
		int registryId = System.identityHashCode(registry);
		if (this.registriesPostProcessed.contains(registryId)) {
			throw new IllegalStateException(
					"postProcessBeanDefinitionRegistry already called on this post-processor against " + registry);
		}
		if (this.factoriesPostProcessed.contains(registryId)) {
			throw new IllegalStateException(
					"postProcessBeanFactory already called on this post-processor against " + registry);
		}
		this.registriesPostProcessed.add(registryId);

		processConfigBeanDefinitions(registry); // 下一步
	}
```

## 2. AutowiredAnnotationBeanPostProcessor

- 是进行属性注入，实现的是BeanPostProcessor，是在对象构建完毕后，来进行@Autowired, @Value等
- 属性填充

![AutowiredAnnotationBeanPostProcessor](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/AutowiredAnnotationBeanPostProcessor.png)

![image-20230601150622253](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230601150622253.png)
