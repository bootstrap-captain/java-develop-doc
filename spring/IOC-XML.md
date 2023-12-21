# 入门

## 1. 依赖

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>6.1.2</version>
    </dependency>
</dependencies>
```

![image-20231215094417477](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231215094417477.png)

## 2. 容器

```xml
<bean id="firstService" class="com.erick.daydreamer.FirstService"></bean>
```

### 2.1 BeanFactory

```java
package com.erick.daydreamer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

public class Test01 {
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

    @BeforeEach
    public void init() {
        // 创建xml读取器
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        // 加载配置文件给工厂
        reader.loadBeanDefinitions("erick.xml");
    }

    @Test
    void test01() {
        FirstService bean = beanFactory.getBean(FirstService.class);
        System.out.println(bean);
    }
}
```

![image-20231215154915765](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231215154915765.png)

### 2.2 ApplicationContext

- Spring容器，内部封装了BeanFactory, 功能更加强大

```java
package com.erick.daydreamer;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test01 {

    @Test
    void test01() {
        // 容器
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("erick.xml");
        FirstService bean = applicationContext.getBean(FirstService.class);
        System.out.println(bean);
    }
}
```

#### Spring-Context

![image-20231215103754028](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231215103754028.png)

```bash
# ClassPathXmlApplicationContext
- 加载类路径下的xml配置

# FileSystemXmlApplicationContext
- 加载磁盘路径下的xml配置

# AnnotationConfigApplicationContext
- 加载注解配置类
```

#### Spring-Web

- 添加了其他的依赖后，继承体系会多出一些其他容器实现类

![image-20231215104137571](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231215104137571.png)

### 2.3 二者比较

| BeanFactory             | ApplicationContext                                         |
| ----------------------- | ---------------------------------------------------------- |
| spring的bean工厂        | spring的容器                                               |
| 功能较少                | 扩展了监听，国际化等功能                                   |
| 早期接口，底层接口      | 后期接口，继承并组合BeanFactory                            |
| 首次调用getBean时才创建 | 配置文件加载后，将所有bean都初始化好(可以通过构造方法判断) |

![image-20231215102418471](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231215102418471.png)

- ApplicationContext继承BeanFactory，内部又组合一个BeanFactory(DefaultListableBeanFactory)，内部组合的BeanFactory中的singletonObjects中会包含对应的创建的单例bean，spring获取对应的单例对象时，都是从这里获取

![image-20231215103210983](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231215103210983.png)

# 定义阶段

## 1. 加载配置文件

- 根据配置文件，ClassPathXmlApplicationContext容器，间接继承AbstractRefreshableApplicationContext

```java
// AbstractRefreshableApplicationContext类中维护了beanFactory,是真正的容器
@Nullable
private volatile DefaultListableBeanFactory beanFactory;
```

![ClassPathXmlApplicationContext](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/ClassPathXmlApplicationContext.png)

## 2. BeanDefinition

- spring容器初始化时，将xml配置中的配置的bean封装成一个BeanDefinition对象
- 所有BeanDefination会存储到DefaultListableBeanFactory中的名为beanDefinationMap的Map集合中

```java
// DefaultListableBeanFactory类中，维护了BeanDefinition的map集合
private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
```

![image-20231215140442399](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231215140442399.png)

### GenericBeanDefinition

- 不是Bean的真正对象，而是保存了反射创建bean时候所需要的必要信息
- 如Bean名字，全限定类名(beanClass)，是否为单例(scope)，对应的属性(propertyValue)
- 用户自定义的Bean一般使用GenericBeanDefinition

![image-20230530122409747](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230530122409747.png)

## 3. BeanDefinition后置处理器

- 后置处理器，动态修改BeanDefination
- <font color=orange>后置处理器必须先交给spring来管理，从而能够进入到BeanDefinition的集合中</font>
- BeanDefinationMap填充完毕后，Bean创建对象前执行前

```bash
# 把后置处理器对应的类交给spring管理后，因为里面只有回调方法
- 先对PostProcessor立刻按照类似的逻辑，加入BeanDefinition的Map中
- 立刻对这些类进行初始化，并放在容器单例池中(延迟加载)

# 实现方法
PostProcessorRegistrationDelegate --- invokeBeanFactoryPostProcessors()
```

### 3.1 BeanFactoryPostProcessor

- spring回调该接口的方法，对BeanDefinition注册和修改
- 按照xml中声明的顺序构建processor
- 通过xml的话，如何处理顺序

![image-20231215143924243](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231215143924243.png)

```java
package com.erick.daydreamer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

public class AwsBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        /*1. 修改Bean, 不能获取到整个BeanDefinition的map，但是可以获取到单独的*/
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition("firstService");
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);


        /*2. 动态添加bean，子类具有更加强大的功能*/
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
        BeanDefinition bd = new GenericBeanDefinition();
        bd.setBeanClassName("com.erick.daydreamer.FirstService");
        bd.setScope(BeanDefinition.SCOPE_SINGLETON);
        factory.registerBeanDefinition("dynamicAdd", bd);
    }
}
```

### 3.2 BeanDefinitionRegistryPostProcessor

- 继承BeanFactoryPostProcessor接口，专门用来注册bean，功能和BeanFactoryPostProcessor一样

```java
package com.erick.daydreamer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

public class FirstBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        System.out.println("FirstBeanDefinitionRegistryPostProcessor-postProcessBeanDefinitionRegistry");
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setBeanClassName("com.erick.daydreamer.AwsService");
        registry.registerBeanDefinition("first-postProcessBeanDefinitionRegistry", beanDefinition);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("FirstBeanDefinitionRegistryPostProcessor-postProcessBeanFactory");
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setBeanClassName("com.erick.daydreamer.AwsService");
        defaultListableBeanFactory.registerBeanDefinition("first-postProcessBeanFactory", beanDefinition);
    }
}
```

### 3.3 执行顺序

```bash
# 1. BeanDefinitionRegistryPostProcessor
- 先执行每个BeanDefinitionRegistryPostProcessor中的 postProcessBeanDefinitionRegistry
- 再执行每个BeanDefinitionRegistryPostProcessor中的 postProcessBeanFactory

# 2. BeanFactoryPostProcessor
- 最后执行每个BeanFactoryPostProcessor中的 postProcessBeanFactory
```

# 实例化

- 遍历BeanDefinition对应的beanDefinitionMap，通过反射来创建对象
- 执行Bean的静态代码块，构造方法完成一部分属性注入(如果属性存在循环依赖，则直接报错)
- 创造出来的对象是半成品，并不会立刻放在容器中

```bash
# 创建对象(包括static代码块)
- 构造方法(无参构造或有参构造)，是否是FactoryBean
- 是否是singleton
- 是否是延迟加载
```

## 1. 构造方法

- 基于id(bean对象的唯一标识)和全类名，通过反射，调用构造方法(无参构造或有参构造)，构造Bean对象
- class：类的全类名，不能是接口，因为需要调用对应的构造方法来创建该对象
- 多个bean按照声明顺序加载
- Bean的对象创建完毕，但是其中的属性等还没有注入，半成品，不会直接放在单例池中
- 构造方法中如果包含其他还没创建的bean，则先去调用其他bean的构造方法

```xml
  <!--调用无参构造: 找对应的无参构造方法-->
  <bean id="first" class="com.erick.daydreamer.AWSService"></bean>

  <!--有参构造来创建： 找对应的有参构造方法
         如果找不到对应的带参构造，则：Could not resolve matching constructor
          1. name: 对应有参构造的构造方法的形参名
          2. value：对应有参构造的构造方法的形参的具体的值-->
  <bean id="second" class="com.erick.daydreamer.AWSService">
      <constructor-arg name="xxCountry" value="china"/>
      <constructor-arg name="xxRegion" value="beijing"/>
  </bean>

  <!--有参构造来创建： 按照声明顺序来匹配-->
  <bean id="third" class="com.erick.daydreamer.AWSService">
      <constructor-arg value="us"/>
      <constructor-arg value="california"/>
      <constructor-arg value="2"/>
  </bean>
```

```java
package com.erick.daydreamer;

public class AWSService {
    private String region;

    private String country;

    private int price;

    private AWSService() {
        System.out.println("无参构造");
    }

    //  即使是private，spring也会通过反射构造
    private AWSService(String xxRegion, String xxCountry) {
        System.out.println("两个有参构造");
        this.region = xxRegion;
        this.country = xxCountry;
    }

    private AWSService(String region1, String country1, int price1) {
        this.region = region1;
        this.country = country1;
        this.price = price1;
    }
}
```

![image-20231215185304916](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231215185304916.png)

## 2. 工厂方法

- 通过自定义的工厂来进行创建
- 调用工厂中的方法，来通过new来进行初始化

### 2.1 普通工厂

- 工厂类也会被放在容器中，后面会调用对象中的来进行new
- 工厂类就是一个普通的bean，工厂中的对象，则记录了对应工厂类信息

```xml
<!--工厂方法对应的要放入容器的类-->
<bean id="phoneFactory" class="com.erick.daydreamer.PhoneFactory"></bean>

<!--无参-->
<bean id="normalPhone" class="com.erick.daydreamer.ApplePhone"
      factory-bean="phoneFactory" factory-method="getApplePhone"></bean>

<!--有参-->
<bean id="iphone15" class="com.erick.daydreamer.ApplePhone"
      factory-bean="phoneFactory" factory-method="getApplePhone">
    <constructor-arg name="price" value="5000"></constructor-arg>
    <constructor-arg name="version" value="15"></constructor-arg>
</bean>
```

![image-20231215180832524](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231215180832524.png)

### 2.2 静态工厂

- 工厂类不会被放在容器中,因为工厂类就是一个util类

```xml
<bean id="firstPhone" class="com.erick.daydreamer.PhoneFactory" factory-method="getApplePhone"></bean>

<bean id="secondPhone" class="com.erick.daydreamer.PhoneFactory" factory-method="getApplePhone">
    <constructor-arg name="version" value="iphone-15"/>
    <constructor-arg name="price" value="2999"/>
</bean>
```

### 2.3 FactoryBean

- spring提供的一个接口，spring底层大量使用
- <font color=orange>必须先交给spring来管理</font>

```bash
# 工厂方法实现的一种
- 约定大于配置： 不需要显示指定对应的factory-method了
- 会将该工厂类，该工程类中的getObject()的方法返回值放在容器中
- 延迟加载：只有获取对应的bean的时候，才会去调用
```

```java
package com.erick.daydreamer;

import org.springframework.beans.factory.FactoryBean;

public class PhoneFactory implements FactoryBean<ApplePhone> {

    @Override
    public ApplePhone getObject() throws Exception {
        return new ApplePhone();
    }

    @Override
    public Class<?> getObjectType() {
        return ApplePhone.class;
    }

    @Override
    public boolean isSingleton() {
        return FactoryBean.super.isSingleton();
    }
}
```

```bash
# 实现步骤   id：为对应Factory和Service的名字
# 继承FactoryBean的对象： 
- 放在容器中的beanFactory中的singletonObject中
# 具体保存的实例对象
- 调用getBean时，才会创建对象，并放在beanFactory.factoryBeanObjectCache中(必须是单例)
- 返回对象的时候，也是从beanFactory.factoryBeanObjectCache中去查找，而不是在单例池中找
```

![image-20231215184152221](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231215184152221.png)

#  DI

- Dependency Injection，属性注入
- 初始化后的单例Bean，还是一个半成品对象，并不会立刻放在放在单例池中，而是要进行DI属性注入
- 创建一个对象时候，对其成员变量等属性进行赋值

## 1. 属性注入

### 1.1 构造器

- 在上面实例化的时候，已经完成部分属性的依赖注入

```bash
# 依赖分类及注入：  调用有参数构造，完成属性的DI
- 基础数据，String
- POJO类型
```

### 1.2 set方法

```bash
# 依赖分类及注入：构造对象完毕后， 再调用set方法来进行bean中属性的写操作
```

```xml
<bean id="snsService" class="com.erick.daydreamer.SnsService">
 <!--通过name，首字母大写，再拼接set，匹配到对应的set方法名，并进行调用-->
    <property name="address" value="shanxi"/>
    <property name="region" value="china"/>
</bean>
```

```java
package com.erick.daydreamer;

public class SnsService {
    private String region;
    private String address;

    public void setRegion(String region) {
        System.out.println("region赋值");
        this.region = region;
    }

    public void setAddress(String address) {
        System.out.println("address赋值");
        this.address = address;
    }
}
```

- set里面的属性信息，封装在beanDefinitionMap中

![image-20231220104353137](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231220104353137.png)



## 2. Aware接口

- 框架辅助属性注入的一种思想，其他框架也可以看到类似的接口
- 框架的高度封装性，底层功能API不能轻易获取，但并不意味永远用不到这些对象，如果用到了，就可以使用框架提供的Aware接口

### 2.1 BeanNameAware

- 回调方法：setBeanName

```xml
<bean id="snsService" class="com.erick.daydreamer.SnsService">
    <property name="address" value="shanxi"/>
</bean>
```

```java
package com.erick.daydreamer;

import org.springframework.beans.factory.BeanNameAware;

public class SnsService implements BeanNameAware {
    private String address;

    /*set方法先执行*/
    public void setAddress(String address) {
        System.out.println("address赋值");
        this.address = address;
    }

    /*aware后执行*/
    @Override
    public void setBeanName(String name) {
        System.out.println("beanName:" + name);
    }
}
```

### 2.2 ApplicationContextAware

- springboot项目中，假如想获取到当前spring的容器，可以实现ApplicationContextAware接口

```java
package com.black.pearl.beanProxy;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class CaptainContainerContextAware implements ApplicationContextAware {

    /*spring-boot-container*/
    private static ApplicationContext captainContainer;

    /*call back method*/
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.captainContainer = applicationContext;
    }

    /**
     * provide access to non-spring class in spring-boot-application
     *
     * @return
     */
    public static ApplicationContext getCaptainContainer() {
        return captainContainer;
    }
}
```

## 3. BeanPostProcessor-Before

- Bean后置处理器，在Bean创建对象后(半成品对象)
- BeanPostProcessor的对应的实现类来处理Bean，需要交给spring接管来调用

```java
package com.erick.daydreamer;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class ErickBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SnsService) {
            // 可以修改bean的属性 
            ((SnsService) bean).setAddress("janpan");
            System.out.println("BeanPostProcessor---postProcessBeforeInitialization: " + beanName);
        }
        return bean;
    }
}
```

## 4. InitializingBean接口

## 5. Bean的init

## 6. BeanPostProcessor-After

## 7. 进入单例池

## 总结

![image-20230530203711146](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230530203711146.png)

# 使用

## 1. 获取

```bash
# ID
- id是定义时放在容器中的key

# TYPE
- type指的是该bean的类型或者该bean继承的父类(不能是接口)
         # 要求IOC容器中有且只有一个类型匹配的bean
               - 若没有任何一个匹配的， 则NoSuchBeanDefinitionException
               - 若有多个类型匹配的Bean， 则 NoUniqueBeanDefinitionException
 
# ID + TYPE
- 解决上面 NoUniqueBeanDefinitionException的问题
- 注意传递参数的时候的顺序
```

```java
package com.erick;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test02 {

    private ApplicationContext context;

    @BeforeEach
    void init() {
        context = new ClassPathXmlApplicationContext("create_bean.xml");
    }

    /*根据id来获取bean*/
    @Test
    void test01() {
        BankService service = (BankService) context.getBean("first_bank_service");
        System.out.println(service);
    }

    /*根据type来获取bean*/
    @Test
    void test02() {
        BankService service = context.getBean(BankService.class);
        System.out.println(service);
    }

    /*根据type对应的父类来获取bean*/
    @Test
    void test03() {
        BankService service = (BankService) context.getBean(BaseBank.class);
        System.out.println(service.getClass());
    }

    /*根据type+id来获取*/
    @Test
    void test04() {
        BankService service = (BankService) context.getBean("first_bank_service", BaseBank.class);
        System.out.println(service.getClass());
    }
}
```

## 2. lazy-init

- ApplicationContext：容器初始化后，就会加载所有定义的Bean并放在容器中，lazy-init可以使当前bean的初始化被延迟到getBean
- 对于Beanfactory无效，因为BeanFactory本身就是getBean时候才会加载

```xml
<!--lazy-init: 默认为false-->
<bean id="first_bank_service" class="com.erick.BankService" lazy-init="true"></bean>
```

## 3. scope

```bash
# 1. 在基本的spring环境:  spring-context
# singleton
- 单例:默认配置
- spring容器创建时，就会进行bean的实例化，并存储到容器内部的单例池中
- 每次getbean都是从单例池中获取相同的bean实例

# prototype
- 多例
- spring容器创建时，不会创建bean实例
- 只有当getBean时才会实例化，每次都会创建一个新的bean实例

# 2. 在其他环境下，比如spring-webmvc
request, session
```

```xml
<bean id="awsService" class="com.erick.AwsService" scope="prototype"></bean>
```

## 4. profile

- 类似springboot的不同配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!--默认环境: 不管在哪个环境都会加载，公共配置-->
    <bean id="defaultService" class="com.erick.lucy.SnsService"></bean>

    <!--dev环境： 只有指定环境后才能加载-->
    <beans profile="dev">
        <bean id="devService" class="com.erick.lucy.SnsService"></bean>
    </beans>

    <!--test环境-->
    <beans profile="test">
        <bean id="testService" class="com.erick.lucy.SnsService"></bean>
    </beans>
</beans>
```

```bash
# 切换方式，设置JVM的运行属性：  Java代码的优先级别更高
- VM Option:       -Dspring.profiles.active=test

- Java代码：         System.setProperty("spring.profiles.active", "test");
```

# 引用依赖

## 1. 单向引用

- 如果A-Bean的setter对应的属性，依赖于B-Bean，但B-Bean并不会依赖A-Bean
- 则先暂停A的属性填充(A不会放在容器中)，先去初始化B
- 单例池只会存完整的Bean

```java
package com.erick;

public class FirstService {
    private SecondService secondService;

    public FirstService() {
        System.out.println("first创建");
    }

    public void setXXXSecondService(SecondService secondService) {
        System.out.println("first属性注入");
        this.secondService = secondService;
    }
}

class SecondService {
    public SecondService() {
        System.out.println("second创建");
    }
}
```

```xml
<bean id="firstService" class="com.erick.FirstService">
    <!--bean加载从上而下
     1. FirstService进行set时候，通过ref去容器中找时，发现不存在，则暂停set
     2. 先进行SecondService的初始化-->
    <property name="XXXSecondService" ref="secondService"></property>
</bean>

<bean id="secondService" class="com.erick.SecondService"></bean>
```

## 2. 循环引用

- 循环引用只能存在于通过set方法来进行属性填充的情况
- 构造器属性填充(强依赖)一旦循环依赖，则立马报错

```java
package com.erick;

public class FirstService {
    private SecondService secondService;

    public void setXXXSecondService(SecondService secondService) {
        System.out.println("first属性注入");
        this.secondService = secondService;
    }
}

class SecondService {
    private FirstService firstService;

    public void setXXXFirstService(FirstService firstService) {
        this.firstService = firstService;
    }
}
```

![image-20230530161846258](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230530161846258.png)

###  三级缓存

- 只针对setter属性填充

```java
// DefaultSingletonBeanRegistry类中

// 一级缓存： 最终存储单例Bean成品的容器，即实例化和初始化都完成的Bean
//           开发人员getBean时，都是在这里获取
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

// 二级缓存： 缓存半成品对象，且当前对象已经被其他对象引用了
private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);

// 三级缓存：  单例Bean的工厂池，缓存半成品对象，对象没被引用， 使用时再通过工厂创建Bean
private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);
// ObjectFactory： T getObject() throws BeansException;
```

![image-20230530170355077](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230530170355077.png)





```java
package com.erick.processor;

import com.erick.service.AwsService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

public class ErickBeanPostProcessor implements  {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AwsService) {
            System.out.println("BeanPostProcessor---postProcessBeforeInitialization: " + beanName);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AwsService) {
            System.out.println("BeanPostProcessor---postProcessAfterInitialization: " + beanName);
        }
        return bean;
    }
}
```

