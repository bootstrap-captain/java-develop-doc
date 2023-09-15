# 入门

## 1. BeanFactory

![image-20230516143604878](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230516143604878.png)

- 导入上面依赖后，IDEA会自动有对应的spring config可以创建

![image-20230516143826657](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230516143826657.png)

```xml
<bean id="bankDaoImpl" class="com.erick.BankDaoImpl"></bean>
```

```java
package com.erick;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

public class Test01 {
    private DefaultListableBeanFactory beanFactory;

    @BeforeEach
    void init() {
        // 创建工厂对象
        beanFactory = new DefaultListableBeanFactory();
        // 创建xml读取器
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        // 加载配置文件给工厂
        reader.loadBeanDefinitions("erick_bean.xml");
    }

    @Test
    void test01() {
        // 根据配置获取具体的实例: 会进行对应的对象擦除
        BankServiceImpl bankServiceImpl = (BankServiceImpl) beanFactory.getBean("bankServiceImpl");
        System.out.println(bankServiceImpl);
    }
}
```

## 2. ApplicationContext

- Spring容器，内部封装了BeanFactory, 功能更加强大

```java
package com.erick;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test02 {
    private ApplicationContext container;

    @BeforeEach
    void init() {
        // 类加载路径下的
        container = new ClassPathXmlApplicationContext("erick_bean.xml");
    }

    @Test
    void test01() {
        BankServiceImpl bankServiceImpl = (BankServiceImpl) container.getBean("bankServiceImpl");
    }
}
```

### 2.1 spring-context

![image-20230516155303344](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230516155303344.png)

```bash
# ClassPathXmlApplicationContext
- 加载类路径下的xml配置

# FileSystemXmlApplicationContext
- 加载磁盘路径下的xml配置

# AnnotationConfigApplicationContext
- 加载注解配置类
```

### 2.2 spring-web

- 添加了其他的依赖后，继承体系会多出一些其他容器实现类

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>5.3.7</version>
</dependency>
```

![image-20230516160511872](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230516160511872.png)

## 3. 二者比较

| BeanFactory             | ApplicationContext                                         |
| ----------------------- | ---------------------------------------------------------- |
| spring的bean工厂        | spring的容器                                               |
| 功能较少                | 扩展了监听，国际化等功能                                   |
| 早期接口，底层接口      | 后期接口，继承并组合BeanFactory                            |
| 首次调用getBean时才创建 | 配置文件加载后，将所有bean都初始化好(可以通过构造方法判断) |

![image-20230516153234399](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230516153234399.png)

- ApplicationContext继承BeanFactory，内部又会组合一个BeanFactory(DefaultListableBeanFactory)，内部组合的BeanFactory中的singletonObjects中会包含对应的创建的单例bean，spring获取对应的单例对象时，都是从这里获取

![image-20230516153757337](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230516153757337.png)

# Bean

## 1. 初始化

- 初始化：Bean的对象创建完毕，但是其中的属性等还没有注入，半成品，不会直接放在单例池中

### 1.1 构造方法

- spring基于id和全类名，通过反射，调用构造方法(无参构造或有参构造)，构造Bean对象
- id：在spring容器中，bean对象的唯一标识
- class：类的全类名，不能是接口，因为需要调用对应的构造方法来创建该对象
- 多个bean按照声明顺序加载

```xml
    <!--无参构造来创建： 找对应的无参构造方法-->
<bean id="first" class="com.erick.AwsService"></bean>

    <!--有参构造来创建： 找对应的有参构造方法
    如果找不到对应的带参构造，则：Could not resolve matching constructor
    1. name: 对应有参构造的构造方法的形参名
    2. value：对应有参构造的构造方法的形参的具体的值-->
<bean id="second" class="com.erick.AwsService">
    <constructor-arg name="xxxPrice" value="12"></constructor-arg>
    <constructor-arg name="xxxRegion" value="beijing"></constructor-arg>
</bean>
```

```java
package com.erick;

public class AwsService {
    private String region;

    private int price;

    public AwsService() {
        System.out.println("no-arg-constructor");
    }

    //  即使是private，spring也会通过反射构造
    private AwsService(String xxxRegion, int xxxPrice) {
        this.price = xxxPrice;
        this.region = xxxRegion;
        System.out.println("args-constructor");
    }
}
```

### 1.2 工厂方式

- 通过自定义的工厂来进行创建
- 调用工厂中的方法，来通过new来进行初始化

#### 普通工厂

- 工厂类也会被放在容器中

```xml
<bean id="erickFactory" class="com.erick.ErickFactory"></bean>

<!--工厂方法对应的要放入容器的类-->
<bean id="erickService01" factory-bean="erickFactory" factory-method="getErickService"></bean>
```

```java
package com.erick;

public class ErickFactory {
    public ErickService getErickService() {
        return new ErickService();
    }
}
```

#### 静态工厂

- 工厂类不会被放在容器中

```xml
<!--无参方法-->
<bean id="first" class="com.erick.lucy.ErickFactory" factory-method="getFirst"></bean>

<!--constructor-arg: 只要是构建一个bean的方法的参数都可以使用，不管是普通方法，静态方法，构造方法-->
<bean id="second" class="com.erick.lucy.ErickFactory" factory-method="getSecond">
    <constructor-arg name="info_1" value="beijing"></constructor-arg>
    <constructor-arg name="name_1" value="erick"></constructor-arg>
</bean>

<!--按照顺序传递-->
<bean id="third" class="com.erick.lucy.ErickFactory" factory-method="getThird">
    <constructor-arg value="er"></constructor-arg>
    <constructor-arg value="12"></constructor-arg>
</bean>
```

```java
package com.erick.lucy;

public class ErickFactory {
    public static ErickService getFirst() {
        return new ErickService();
    }

    public static ErickService getSecond(String name_1, String info_1) {
        return new ErickService();
    }

    public static ErickService getThird(String name_1, int age_1) {
        return new ErickService();
    }
}
```

### 1.3 FactoryBean

- spring提供的一个接口，spring底层大量使用
- 实现了FactoryBean的类也会放在容器中

```bash
# 工厂方法实现的一种
- 约定大于配置： 不需要显示指定对应的factory-method了
- 会将该工厂类，该工程类中的getObject()的方法返回值放在容器中
- 延迟加载：只有获取对应的bean的时候，才会去调用

# 实现步骤   id：为对应Factory和Service的名字
- 将继承了FactoryBean的对象放在容器中的beanFactory中的singletonObject中
- 延迟加载： 调用getBean时，才会创建对象，并放在beanFactory.factoryBeanObjectCache中(必须是单例)

# 获取
- 如果一个类实现了FactoryBean，获取其对应对象的时候 getBean("xxxFactory");
- 从对应的beanFactory.factoryBeanObjectCache中去找，而不是去singletonObject中

# 应用场景： 整合第三方的框架
```

```xml
<bean id="snsService" class="com.erick.lucy.AwsFactory"></bean>
```

```java
package com.erick.lucy;

import org.springframework.beans.factory.FactoryBean;

public class AwsFactory implements FactoryBean {
    @Override
    public SnsService getObject() throws Exception {
        return new SnsService();
    }

    @Override
    public Class<?> getObjectType() {
        return SnsService.class;
    }

    /*控制生产的对象scope*/
    @Override
    public boolean isSingleton() {
        return true;
    }
}
```

## 2. DI

- 初始化后的单例Bean，还是一个半成品对象，并不会立刻放在放在单例池中，而是要进行DI属性注入
- Dependency Injection，属性注入
- 创建一个对象时候，对其成员变量等属性进行赋值

```bash
# Java本身的属性DI
# 1.无参构造创建对象，有参构造对属性赋值
- 必须传递所需要的全部属性值
- 一次性创建好对象并完成属性的赋值，不存在 “半成品” 对象

# 2.调用setter方法
- 可部分属性set
- 可能存在半成品

# 3. 反射直接干字段
- 类似setter方法
```

### 2.1 构造器

- 可以完成部分属性的依赖注入

```bash
# 依赖分类及注入：  调用有参数构造，完成属性的DI
- 基础数据，String
- POJO类型
- 集合类： 一般不会用这种方式来进行构建

# 匹配方式： 其实就是利用反射匹配找到正确的构造方法
- 形参名匹配
- 按顺序匹配
```

```xml
    <!--无参构造来创建： 找对应的无参构造方法-->
<bean id="first" class="com.erick.AwsService"></bean>

    <!--有参构造来创建： 找对应的有参构造方法
    如果找不到对应的带参构造，则：Could not resolve matching constructor
    1. name: 对应有参构造的构造方法的形参名
    2. value：对应有参构造的构造方法的形参的具体的值-->
<bean id="second" class="com.erick.AwsService">
    <constructor-arg name="xxxPrice" value="12"></constructor-arg>
    <constructor-arg name="xxxRegion" value="beijing"></constructor-arg>
</bean>
```

### 2.2 Set方法

```bash
# 依赖分类及注入：调用无参构造创建bean对象， 同时调用set方法来进行bean中属性的赋值
- 基础数据，String
- POJO类型
```

```xml
<bean id="cookingService" class="com.erick.CookingService">
    <!--通过set方法来进行DI:
          1.根据name名首字母大写，再拼接set字符串， 对应CookingService类中的方法名
          2.将ref对应的值，赋值给形参(形参名是什么无所谓,形参只能包含一个)
          3.通过反射调用set方法-->
    <property name="dish" value="chicken"></property>
    <property name="price" value="12"></property>
    <property name="cooker" ref="erickCooker"></property>
    <property name="random" value="520"></property>
</bean>

<bean id="erickCooker" class="com.erick.Cooker"></bean>
```

```java
package com.erick;

public class CookingService {
    private int price;
    private String dish;
    private Cooker cooker;

    public CookingService() {
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setDish(String dish) {
        this.dish = dish;
    }

    public void setCooker(Cooker cooker) {
        this.cooker = cooker;
    }

    public void setRandom(String address) {
        System.out.println(address);
    }
}
```

### 2.3 二者比较

```bash
# constructor-arg
- 有参构造中包含几个参数，就必须有几个constructor-arg
- 对象一旦构建好，就是一个理论意义上的完整对象

# property
- 对象构造完成后，再调用set方法来进行DI
- 不需要和set方法数量一一对应
- 构造的bean对象可能不完整

# 除此之外，也可以暴力反射，对对应的field直接赋值
- 此方法xml不支持
```

## 3. 获取

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

## 4. lazy-init

- ApplicationContext：容器初始化后，就会加载所有定义的Bean并放在容器中，lazy-init可以使当前bean的初始化被延迟到getBean
- 对于Beanfactory无效，因为BeanFactory本身就是getBean时候才会加载

```xml
<!--lazy-init: 默认为false-->
<bean id="first_bank_service" class="com.erick.BankService" lazy-init="true"></bean>
```

## 5. scope

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

## 6. profile

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

# 生命周期

## 1. 定义阶段

### 1.1 加载配置文件

#### 加载

```xml
<bean id="awsService" class="com.erick.AwsService"></bean>
```

```java
package com.erick;

public class AwsService {
}
```

```java
package com.erick;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test01 {
    @Test
    void test01() {
        ApplicationContext container = new ClassPathXmlApplicationContext("first.xml");
        System.out.println(container.getBean(AwsService.class));
    }
}
```

#### 容器

- ClassPathXmlApplicationContext容器，间接继承AbstractRefreshableApplicationContext

![ClassPath](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/ClassPath.png)

```java
// AbstractRefreshableApplicationContext类中维护了beanFactory,是真正的容器
@Nullable
private volatile DefaultListableBeanFactory beanFactory;
```

### 1.2 BeanDefinition

- spring容器初始化时，将xml配置中的配置的bean封装成一个BeanDefinition对象
- 所有的BeanDefination会存储到DefaultListableBeanFactory中维护的名为beanDefinationMap的Map集合中

```java
// DefaultListableBeanFactory类中，维护了BeanDefinition的map集合
private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);
```

#### GenericBeanDefinition

- 不是Bean的真正对象，而是保存了反射创建bean时候所需要的必要信息
- 如Bean名字，全限定类名(beanClass)，是否为单例(scope)，对应的属性(propertyValue)
- 用户自定义的Bean一般使用GenericBeanDefinition

![image-20230530122409747](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230530122409747.png)

#### 继承体系

![image-20230528151913732](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230528151913732.png)

### 1.3 BeanDefinition后置处理器

- 后置处理器，动态修改BeanDefination
- 在BeanDefinationMap填充完毕，Bean创建对象前执行
- 能不能在这里使用@Value，@Autowired来进行对象注入？(生命周期会不会乱？)

#### BeanFactoryPostProcessor

- Bean工厂后置处理器，实现该接口的类先交给spring容器管理，该对象首先要放在容器中
- spring回调该接口的方法，对BeanDefinition注册和修改

```bash
# 1. 读取到xml配置文件，将所有xml配置的bean定义在beanDefinitionMap中

# 2. Bean开始实例化时，先识别实现了BeanFactoryPostProcessor的Bean并对反射(有参或无参)其进行初始化
     - 按照声明顺序构建processor

# 3. 回调其中的postProcessBeanFactory方法，修改beanDefinitionMap

# 4. 初始化其他的Bean
```

![image-20230528231841762](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230528231841762.png)

```java
package com.erick.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

public class SecondBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    public SecondBeanFactoryPostProcessor() {
        System.out.println("SecondBeanFactoryPostProcessor构造方法");
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        /*1. 修改Bean, 不能获取到整个BeanDefinition的map，但是可以获取到单独的*/
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition("awsService");
        beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);

        /*2. 动态添加bean，子类具有更加强大的功能*/
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
        BeanDefinition waitBeanDefinition = new GenericBeanDefinition();
        waitBeanDefinition.setBeanClassName("com.erick.SnsService");
        waitBeanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);

        factory.registerBeanDefinition("snsService", waitBeanDefinition);
    }
}
```

```xml
<bean id="awsService" class="com.erick.AwsService"></bean>


<!--按照processor的bean配置的顺序，依此执行-->
<bean id="secondProcessor" class="com.erick.processor.SecondBeanFactoryPostProcessor">
</bean>

<bean id="fistProcessor" class="com.erick.processor.FirstBeanFactoryPostProcessor">
    <constructor-arg name="name" value="erick"></constructor-arg>
</bean>
```

#### BeanDefinitionRegistryPostProcessor

- 继承BeanFactoryPostProcessor接口，专门用来注册bean，功能和BeanFactoryPostProcessor一样

```java
package com.erick.processor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;

public class FirstBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        System.out.println("FirstBeanDefinitionRegistryPostProcessor中的postProcessBeanDefinitionRegistry");
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setBeanClassName("com.erick.AwsService");
        registry.registerBeanDefinition("first", beanDefinition);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("FirstBeanDefinitionRegistryPostProcessor中的postProcessBeanFactory");
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setBeanClassName("com.erick.AwsService");
        defaultListableBeanFactory.registerBeanDefinition("second", beanDefinition);
    }
}
```

#### 执行顺序

```bash
# 1. BeanDefinitionRegistryPostProcessor
- 先执行每个BeanDefinitionRegistryPostProcessor中的postProcessBeanDefinitionRegistry
- 再执行每个BeanDefinitionRegistryPostProcessor中的postProcessBeanFactory

# 2. BeanFactoryPostProcessor
- 最后执行每个BeanFactoryPostProcessor中的postProcessBeanFactory
```

#### 注解注册bean原理

- 一个类上加载了自定义注解，那么就将该类放在容器中

```xml
<dependency>
    <groupId>org.reflections</groupId>
    <artifactId>reflections</artifactId>
    <version>0.10.2</version>
</dependency>
```

```java
package com.erick.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE) // 作用在类上
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ErickComponent {
}
```

```java
package com.erick;

import com.erick.annotation.ErickComponent;

@ErickComponent(beanName = "awsService")
public class AwsService {
}
```

```java
package com.erick.util;

import com.erick.annotation.ErickComponent;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScanAnnotationUtil {
    public static Map<String, Class> scanPackage(String basePackage) {
        final Map<String, Class> targetClass = new HashMap<>();
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> result = reflections.getTypesAnnotatedWith(ErickComponent.class);
        for (Class clazz : result) {
            ErickComponent annotation = (ErickComponent) clazz.getAnnotation(ErickComponent.class);
            String beanName = annotation.beanName();
            targetClass.put(beanName, clazz);
        }
        return targetClass;
    }
}
```

```java
package com.erick.processor;

import com.erick.util.ScanAnnotationUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

public class ErickBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Map<String, Class> classMap = ScanAnnotationUtil.scanPackage("com.erick");
        classMap.forEach((beanName, clazz) -> {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(clazz);
            beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
            registry.registerBeanDefinition(beanName, beanDefinition);
        });
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
```

## 2. 实例化

- 遍历BeanDefinition对应的beanDefinitionMap，通过反射来创建对象
- 执行Bean的静态代码块，构造方法完成一部分属性注入(如果属性存在循环依赖，则直接报错)
- 创造出来的对象是半成品，并不会立刻放在容器中

```bash
# 创建对象(包括static代码块)
- 构造方法(无参构造或有参构造)，是否是FactoryBean
- 是否是singleton
- 是否是延迟加载
```

## 3. 初始化

- Bean创建出来后仅仅是个半成品，需要对Bean的属性进行填充，是最复杂的阶段
- 部分的属性，可能已经通过构造方法完成，其他通过setter方法来完成

### 3.1 属性填充

#### 基本属性

- 一些基本类型数据，String，集合等数据类型可以通过setter来完成
- 读取xml配置文件中的property，setter对应的属性维护在beanDefinitionMap中的propertyValues中，对象半成品创建好之后，会进行populate对应的setter方法

```xml
<bean id="firstService" class="com.erick.FirstService">
    <!--一部分属性通过构造方法完成-->
    <constructor-arg name="name" value="serviceXXX"></constructor-arg>
    <!--另一部分通过setter来完成-->
    <property name="firstServiceAge" value="12"></property>
    <property name="firstServiceAddress" value="shanghai"></property>
</bean>
```

```java
package com.erick;

public class FirstService {
    private String name;

    private int age;

    private String address;

    public FirstService(String name) {
        this.name = name;
    }

    public void setFirstServiceAge(int age) {
        this.age = age;
    }

    public void setFirstServiceAddress(String address) {
        this.address = address;
    }
}
```

![image-20230530155714637](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230530155714637.png)

#### 单向引用

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

#### 循环引用

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

### 3.2 Aware接口

- 框架辅助属性注入的一种思想，其他框架也可以看到类似的接口
- 框架的高度封装性，底层功能API不能轻易获取，但并不意味永远用不到这些对象，如果用到了，就可以使用框架提供的Aware接口，让框架给我们注入该对象(可以由对应的Bean来实现)

| Aware接口               | 回调方法                                                     | 作用                                     |
| ----------------------- | ------------------------------------------------------------ | ---------------------------------------- |
| ApplicationContextAware | void setApplicationContext(ApplicationContext applicationContext) throws BeansException; | spring框架获取到当前对象                 |
| BeanNameAware           | void setBeanName(String name);                               | spring框架注入当前Bean在容器中的beanName |

![image-20230530171706707](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230530171706707.png)

#### BeanNameAware

```xml
<bean id="snsService" class="com.erick.SnsService">
    <property name="XXXAddress" value="beijing"></property>
</bean>
```

```java
package com.erick;

import org.springframework.beans.factory.BeanNameAware;

public class SnsService implements BeanNameAware {

    private String address;

    /*先执行*/
    public void setXXXAddress(String address) {
        System.out.println("address属性注入");
        this.address = address;
    }

    /*后执行：不用在xml中配置属性注入，会自动回调*/
    @Override
    public void setBeanName(String name) {
        System.out.println("BeanName是：" + name);
    }
}
```

#### ApplicationContextAware

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



### 3.3. BeanPostProcessor-Before

- Bean后置处理器，在Bean创建对象后(半成品对象)
- BeanPostProcessor的对应的实现类来处理Bean，需要交给spring接管来调用

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

### 3.4 InitializingBean接口初始化

- afterPropertiesSet

### 3.5 Bean的init

### 3.6. BeanPostProcessor-After

### 3.7 进入单例池

### 总结

![image-20230530203711146](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230530203711146.png)

生命周期

- 通过反射到创建出对象后，一直到Bean成为一个完整的对象，最终存储到单例池中的过程

