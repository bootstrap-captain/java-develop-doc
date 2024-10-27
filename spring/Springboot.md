# 入门Demo

## 1. 优点

- 创建独立Spring应用
- 内置tomcat，web项目直接打包成jar，开箱即用（之前：打包成war，再放入tomcat运行)
- starter启动器：依赖的自动配置和版本管理
- 生产级别的监控，健康检查，外部配置
- 无代码生成，无需编写XML
- 自定义参数修改

## 2. Demo

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.erick.daydreamer</groupId>
    <artifactId>spring-boot-demo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.12</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>

    <!-- 1. springboot的插件， fat jar
         2. 将springboot微服务打包成带有内置tomcat的jar包，
         3. 在控制台进入jar包所在目录： 通过java -jar 包名，就可以启动项目 -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

```java
package com.erick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
```

- 可以对默认配置的一些自定义进行修改

```yaml
server:
  port: 9090
```

```java
package com.erick.daydreamer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
public class FirstController {

    @GetMapping("/get")
    public void test01() {
        System.out.println("execute");
    }
}
```

# 属性配置

## 1. @Conditional

- 条件装配： 满足@Conditional指定的条件，则进行组件注入
- spring本身的注解，springboot对其进行了扩展
- package org.springframework.context.annotation;

```bash
# @Target({ElementType.TYPE, ElementType.METHOD})
- 作用在类上，如果不满足条件，该类的下面的所有注解都不会生效
- 作用在方法上，满足该条件后，该方法会执行

# @Bean的加载顺序： 从上到下加载，书写顺序会影响@Conditional的执行
```

### 1.1 @ConditionalOnBean

- @ConditionalOnBean，   @ConditionalOnMissingBean
- 容器中存在或不存在某个组件时，才会去加载

#### 基本使用

```bash
# 组件：指的不仅仅是@Configuration下面的@Bean, 也包含@Component等

# 同一个配置类
- 注意@Bean的书写顺序，不同的书写顺序会导致不同的结果
- 在一个配置类中，@Bean的加载顺序是从上而下的。调换两个@Bean的顺序，就会产生不一样的结果

# 不同配置类
- 尽量避免两个依赖的类写在同一个@Configuration里面，写在不同的配置类中，避免因为代码顺序的问题出错
```

```java
package com.erick.daydreamer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirstConfig {

    @Bean
    @ConditionalOnBean(value = {Dog.class})
    public DogOwner getDogOwner() {
        return new DogOwner();
    }

    @Bean
    public Dog getDog() {
        return new Dog();
    }

    public static class Dog {
        public Dog() {
            System.out.println("dog-created");
        }
    }

    public static class DogOwner {
        public DogOwner() {
            System.out.println("dog-owner-created");
        }
    }
}
```

```java
package com.erick.daydreamer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(value = {FirstConfig.Dog.class})
public class SecondConfig {

    @Bean
    public DogOwner getFirstDogOwner() {
        return new DogOwner();
    }

    @Bean
    public DogOwner getSecondDogOwner() {
        return new DogOwner();
    }

    public static class DogOwner {
        public DogOwner() {
            System.out.println("dog-owner-created");
        }
    }
}
```

#### 匹配条件

```java
package com.erick.daydreamer.config;

import com.erick.daydreamer.entity.Dog;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecondConfig {


    /*1. 类的class对象： Class<?>[] value() default {}*/
    /*1.1 本项目的类: 可以通过导入Dog类的方式引入*/
    @Bean
    @ConditionalOnBean(value = {Dog.class})
    public DogOwner getFirstDogOwner01() {
        System.out.println("类的class对象-本项目的类");
        return new DogOwner();
    }

    /*1.2. 其他项目的类
     *       - 使用类的全限定类名来获取class对象，不要使用import com.erick.daydreamer.entity.Dog的方式
     *       - 其他项目识别该SecondConfig后，如果对应的@ConditionalOnBean中包含了该类，则IDEA正常，
     *       - 否则IDEA爆红，导入对应的爆红的类的依赖即可*/
    @Bean
    @ConditionalOnBean(value = {ch.qos.logback.core.BasicStatusManager.class})
    public DogOwner getFirstDogOwner02() {
        System.out.println("类的class对象-外部的类");
        return new DogOwner();
    }

    /*2. 类的String全限定类名： String[] type() default {};*/
    @Bean
    @ConditionalOnBean(type = {"com.erick.daydreamer.entity.Dog"})
    public DogOwner getSecondDogOwner01() {
        System.out.println("类的全限定类名-本项目的类");
        return new DogOwner();
    }

    @Bean
    @ConditionalOnBean(type = {"ch.qos.logback.core.BasicStatusManager.class"})
    public DogOwner getSecondDogOwner02() {
        System.out.println("类的全限定类名-外部的类");
        return new DogOwner();
    }

    /*3. name匹配：某个名字的组件，String[] name() default {}; */
    @Bean
    @ConditionalOnBean(name = {"dog001"})
    public DogOwner getThirdDogOwner01() {
        System.out.println("组件名字");
        return new DogOwner();
    }

    /*什么都不写的话，代表默认检查当前方法返回值的类的类型：是否在容器中存在
     * 1. 一般是 @ConditionalOnMissingBean用的比较多，用来确保该class类型的组件，只存在一份*/
    @Bean
    public DogOwner defaultMethod() {
        System.out.println("default");
        return new DogOwner();
    }

    public static class DogOwner {
    }
}
```

### 1.2 @ConditionalOnClass

- 类路径下包含某个Class才会自动加载

```java
package com.erick.daydreamer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasourceConfig {

    @Bean
    @ConditionalOnClass(value = {org.apache.logging.log4j.simple.SimpleLogger.class})
    public String text01() {
        System.out.println("text01====");
        return "success";
    }
}
```

### 1.3 ConditionalOnProperty

- 根据配置文件做开关，选择性加载某些bean

```java
package com.erick.daydreamer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JmsConfig {

    /*返回值是String: 只是为了让该bean加载*/

    /*prefix：前缀
     * name: 对应某个属性
     * havingValue： 值是不是相同，严格相等，忽略大小写
     * matchIfMissing： 如果该属性没配置，则默认不加载*/
    @Bean
    @ConditionalOnProperty(prefix = "app.datasource", name = "flag", havingValue = "AA", matchIfMissing = false)
    public String get01() {
        System.out.println("加载get001");
        return "success";
    }
}
```

```yaml
app:
  datasource:
    flag: aa
```

## 2. @ConfigurationProperties

- 将配置文件的属性，一一对应到一个JavaBean中
- JavaBean的名字一般是xxxProperties
- @ConfigurationProperties：只是将属性和配置绑定在一起，但是该JavaBean还没放在容器，必须配合其他容器组件结合使用
- 默认是加载application-xxx文件

### 2.1 @ConfigurationProperties + @Component

- @ConfigurationProperties + @Component/@Configuration
-  应用场景：本项目自己配置的，不需要给外界提供服务的

```yaml
citi:
  jms:
    user-name: erick
    password: 123456
    max-pool-size: 10
    df: 123
```

```java
package com.erick.daydreamer.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/* prefix: 识别对应的application文件
 * ignoreUnknownFields=true： application配置的属性,VO如果不包含，则VO不去解析*/

@Data
@ConfigurationProperties(prefix = "citi.jms", ignoreUnknownFields = true)
@Configuration
public class JmsProperties {
    private String userName;
    private String password;
    private int maxPoolSize;
}
```

### 2.2 @ConfigurationProperties + @Import

- 配置类可能是三方包，本项目包扫描不到，将对应的配置类，通过@Import的方式扔进来

```bash
# @ConfigurationProperties + @Import 
- @ConfigurationProperties + @Import这两个注解，还是没有放在容器中，因为@Import必须作用在其他容器类注解的类上
- 比如@Import+@Configuration(三方包如果不把对应的@ConfigurationProperties放在容器中，IDEA会爆红)

# 多个@Import类可以继承
- @Import是springframework自身的实现
```

#### 三方包

```java
package com.lucy;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "erick.desk")
public class DeskProperties {
    private String brand;
    private String address;
}
```

```java
package com.lucy;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/* prefix: 识别对应的application文件
 * ignoreUnknownFields=true： application配置的属性,VO如果不包含，则VO不去解析*/

@Data
@ConfigurationProperties(prefix = "citi.jms", ignoreUnknownFields = true)
public class JmsProperties {
    private String userName;
    private String password;
    private int maxPoolSize;
}
```

```java
package com.third.lucy;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration // 不加容器注解，三方包的IDEA会爆红
@Import(value = {DeskProperties.class, JmsProperties.class})
public class ThirdAutoConfig {
}
```

#### 本项目

```java
package com.erick;

import com.lucy.ThirdAutoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(value = ThirdAutoConfig.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
```

### 2.3 @ConfigurationProperties + @EnableConfigurationProperties

- @EnableConfigurationProperties是springboot的实现方式，和3.2类似功能

```bash
# @EnableConfigurationProperties
- 将导入的类的组件，装配到容器中

# 应用场景 :适用于三方包中
- 主要解决： 被@ConfigurationProperties标注的三方包中的类，本地项目包扫不到的
```

#### 三方包项目

```java
package com.lucy;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/* prefix: 识别对应的application文件
 * ignoreUnknownFields=true： application配置的属性,VO如果不包含，则VO不去解析*/

@Data
@ConfigurationProperties(prefix = "citi.jms", ignoreUnknownFields = true)
public class JmsProperties {
    private String userName;
    private String password;
    private int maxPoolSize;
}
```

```java
package com.lucy;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

/*1. 开启JmsProperties的装配功能
 * 2. 在其他项目中的配置类中，只需要@Import进JmsAutoconfig即可*/
@EnableConfigurationProperties(value = JmsProperties.class)
public class JmsAutoconfig {
}
```

#### 本地项目应用

```java
package com.erick;

import com.lucy.JmsAutoconfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(JmsAutoconfig.class) // 随便找个配置类，将@Import(JmsAutoconfig.class) 扔进去即可
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
```

### 2.4 @ConfigurationProperties + @PropertySource

- 自动装配的类的属性，不是在application.yaml中指定的，则需要该注解来显示指定
- 这两个注解，只是自动装配，不会注入组件

#### 三方包

```java
package com.third;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;


@Data
@ConfigurationProperties(prefix = "erick.jdbc")
@PropertySource(value = "classpath:jdbc.properties")
public class ErickJdbcProperties {
    private String url;
    private String userName;
    private String password;
    private String port;
}
```

```java
package com.third;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ErickJdbcProperties.class)
public class ErickJdbcAutoconfiguration {
}
```

#### 调用方

```properties
jdbc.driverClass = com.mysql.cj.jdbc.Driver
jdbc.url = jdbc:mysql://10.5.16.76:3306/stu_db
jdbc.username = root
jdbc.password = 123456
```

```java
@Import(value = {ErickJdbcAutoconfiguration.class})
```

## 3. 自动提示

- 通过xxxpProperties进行属性配置的时候，希望在IDEA的application.yaml中书写的时候，具有自动提示功能

### 3.1 pom

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

### 3.2 xxxProperties

- 写了配置文件后，一定要重新编译项目，才能使其生效

![image-20230605170105294](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230605170105294.png)

#### 静态内部类

- 如果一个xxxProperties嵌套了其他类，其他类只会在本类中使用，推荐静态内部类

![image-20230605170842798](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230605170842798.png)

```java
package com.erick.daydreamer.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "ems.server")
/*必须添加@Data,ConfigurationProperties是通过set方法来设置值的,同时添加get来属性配置 */
@Setter
@Getter
public class JmsProperties {
    /**
     * USERNAME
     */
    private String userName;

    /**
     * PASSWORD
     */
    private String password;

    /**
     * QUEUE-CONFIG
     */
    // 代码的多行注释，可以在写配置文件的时候进行提示
    // 如果是嵌套类里面的注释，需要加上该注解才会有注释提示
    @NestedConfigurationProperty
    private QueueConfig queueConfig;

    /* 1.如果在外部不使用QueueConfig， 则推荐使用静态内部类的方式保持代码优雅*/
    @Setter
    @Getter
    public static class QueueConfig {
        /**
         * QUEUE-NAME
         */
        private String queueName;
    }
}
```

#### 多个类

- 如果嵌套类也会在其他地方使用，则推荐@NestedConfigurationProperty注释的类
- 作用：声明嵌套关系，同时进行注解提示

```java
package com.erick.daydreamer.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "ems.server")
/*必须添加@Data,ConfigurationProperties是通过set方法来设置值的,同时添加get来属性配置 */
@Setter
@Getter
public class JmsProperties {
    /**
     * USERNAME
     */
    private String userName;

    /**
     * PASSWORD
     */
    private String password;

    /**
     * QUEUE-CONFIG
     */
    // 代码的多行注释，可以在写配置文件的时候进行提示
    // 如果是嵌套类里面的注释，需要加上该注解才会有注释提示
    @NestedConfigurationProperty
    private QueueConfig queueConfig;
}
```

```java
package com.erick.daydreamer.properties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public  class QueueConfig {
    /**
     * QUEUE-NAME
     */
    private String queueName;
}
```

## 4. YAML语法

- yaml, yml 都可以支持
- properties的属性，比yaml/yml的优先级高

```bash
# 简单数据类型，可以使用@Value注入
1. key ---- value； 空格控制层级关系，遵循左对齐的原则， 大小写敏感

2. 字符串：       a. 默认不加引号
                 b. " "  特殊字符     -----  不转义
                 c. ' '  特殊字符     -----  转义 
    
# 复杂数据，不能用@Value来进行注入    
3. 对象，Map:          a. 多行写法，      b. 行内写法

4. List，数组：   a. 多行写法        b. 行内写法

5. 上面的不同数据，都可以进行组合
```

```yaml
student:
  age: ${random.int}
  name: ${random.uuid}
  email: ${student.age}_1037289945@qq.com
  address: ${student.name:shanxi}_beijing
  isActive: true
  birthday: 2022/09/20
  info: { color: red, size: 10 }
  friends: [ erick, tom, lucy ]
  phone:
    - apple
    - huawei
    - xiaomi
      
# el表达式
# ${student.age}_1037289945@qq.com：  将该配置的属性和常量字符串拼接
# ${student.age}_1037289945@qq.com：  如果找不到，则将该表达式直接转换为字符串
# ${student.name:shanxi}_beijing：    如果找不到，则提供默认值shanxi、
```

# 自动装配

## 1. 基本介绍

- @SpringBootApplication是下面三个注解的合成注解

```java
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
```

### 1.1 @SpringBootConfiguration

- 和@Configuration类似，代表当前注解，是一个配置类，表示主启动类就是一个配置类

```java
package org.springframework.boot;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@Indexed
public @interface SpringBootConfiguration {
    @AliasFor(
        annotation = Configuration.class
    )
    boolean proxyBeanMethods() default true;
}

```

### 1.2 @ComponentScan

- 只是排除了一些不需要扫描的包，并不是扫描本项目的包的

```java
@ComponentScan(excludeFilters = { @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
```

### 1.3 @EnableAutoConfiguration

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
	String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";
	Class<?>[] exclude() default {};
	String[] excludeName() default {};
}

```

#### @AutoConfigurationPackage

- 具体的功能是@Import(AutoConfigurationPackages.Registrar.class)来实现的
- 就是扫描本项目的包的bean的实现

```java
package org.springframework.boot.autoconfigure;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(AutoConfigurationPackages.Registrar.class)
public @interface AutoConfigurationPackage {
	String[] basePackages() default {};
	Class<?>[] basePackageClasses() default {};
}
```

```java
// 实现了ImportBeanDefinitionRegistrar，就是批量向容器中注册组件的，参考@Import的讲解
static class Registrar implements ImportBeanDefinitionRegistrar, DeterminableImports {

		@Override
		public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
      // 根据主动启动类上的包名得到默认扫描包：metadata获取到项目扫描包 "com.citi"
      // 将包下的所有组件，批量注册
			register(registry, new PackageImports(metadata).getPackageNames().toArray(new String[0]));
		}

		@Override
		public Set<Object> determineImports(AnnotationMetadata metadata) {
			return Collections.singleton(new PackageImports(metadata));
		}
	}
```

```java
	public static void register(BeanDefinitionRegistry registry, String... packageNames) {
		if (registry.containsBeanDefinition(BEAN)) {
			BasePackagesBeanDefinition beanDefinition = (BasePackagesBeanDefinition) registry.getBeanDefinition(BEAN);
			beanDefinition.addBasePackages(packageNames);
		}
		else {
			registry.registerBeanDefinition(BEAN, new BasePackagesBeanDefinition(packageNames));
		}
	}
```

#### @Import(AutoConfigurationPackages.Registrar.class)

- 主要是实现了ImportSelector，来向容器中批量注册组件

![AutoConfigurationImportSelector](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/AutoConfigurationImportSelector.png)

```java
// 自动装配	
@Override
	public String[] selectImports(AnnotationMetadata annotationMetadata) {
		if (!isEnabled(annotationMetadata)) {
			return NO_IMPORTS;
		}
    // 批量导入组件
		AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(annotationMetadata);
		return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
	}
```

```java
protected AutoConfigurationEntry getAutoConfigurationEntry(AnnotationMetadata annotationMetadata) {
		if (!isEnabled(annotationMetadata)) {
			return EMPTY_ENTRY;
		}
		AnnotationAttributes attributes = getAttributes(annotationMetadata);
    // 获取需要导入的组件的类全名
		List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
		configurations = removeDuplicates(configurations);
		Set<String> exclusions = getExclusions(annotationMetadata, attributes);
		checkExcludedClasses(configurations, exclusions);
		configurations.removeAll(exclusions);
		configurations = getConfigurationClassFilter().filter(configurations);
		fireAutoConfigurationImportEvents(configurations, exclusions);
		return new AutoConfigurationEntry(configurations, exclusions);
	}
```

```java
// 获取自动加载的组件
protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
		List<String> configurations = new ArrayList<>(
				SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(), getBeanClassLoader()));
		ImportCandidates.load(AutoConfiguration.class, getBeanClassLoader()).forEach(configurations::add);
		Assert.notEmpty(configurations,
				"No auto configuration classes found in META-INF/spring.factories nor in META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports. If you "
						+ "are using a custom packaging, make sure that file is correct.");
		return configurations;
	}
```

```java
private static Map<String, List<String>> loadSpringFactories(ClassLoader classLoader) {
		Map<String, List<String>> result = cache.get(classLoader);
		if (result != null) {
			return result;
		}

		result = new HashMap<>();
		try {
      // 从当前系统的class路径下META-INF/spring.factories中加载
      // 从classpath路径下加载所有的META-INF/spring.factories文件
      // file:/Users/shuzhan/Documents/repo/org/springframework/boot/spring-boot/2.7.12/spring-boot-2.7.12.jar!/META-INF/spring.factories
			Enumeration<URL> urls = classLoader.getResources(FACTORIES_RESOURCE_LOCATION);
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				UrlResource resource = new UrlResource(url);
				Properties properties = PropertiesLoaderUtils.loadProperties(resource);
				for (Map.Entry<?, ?> entry : properties.entrySet()) {
					String factoryTypeName = ((String) entry.getKey()).trim();
					String[] factoryImplementationNames =
							StringUtils.commaDelimitedListToStringArray((String) entry.getValue());
					for (String factoryImplementationName : factoryImplementationNames) {
						result.computeIfAbsent(factoryTypeName, key -> new ArrayList<>())
								.add(factoryImplementationName.trim());
					}
				}
			}

			// Replace all lists with unmodifiable lists containing unique elements
			result.replaceAll((factoryType, implementations) -> implementations.stream().distinct()
					.collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList)));
			cache.put(classLoader, result);
		}
		catch (IOException ex) {
			throw new IllegalArgumentException("Unable to load factories from location [" +
					FACTORIES_RESOURCE_LOCATION + "]", ex);
		}
		return result;
	}
```

- 虽然加载了所有的组件，但是spring是按需开启
- 会加载org.springframework.boot.autoconfigure所有的组件

![image-20230605150827694](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230605150827694.png)

```bash
# 加载所有的组件：全限定类名 

# Logging Systems
org.springframework.boot.logging.LoggingSystemFactory=\
org.springframework.boot.logging.logback.LogbackLoggingSystem.Factory,\
org.springframework.boot.logging.log4j2.Log4J2LoggingSystem.Factory,\
org.springframework.boot.logging.java.JavaLoggingSystem.Factory

# PropertySource Loaders
org.springframework.boot.env.PropertySourceLoader=\
org.springframework.boot.env.PropertiesPropertySourceLoader,\
org.springframework.boot.env.YamlPropertySourceLoader
```

- 按需开启，通过配置类中的@Condition来进行按需开启

```java
package org.springframework.boot.autoconfigure.aop;

import org.aspectj.weaver.Advice;

import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.aop", name = "auto", havingValue = "true", matchIfMissing = true)
public class AopAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(Advice.class) 
  // 虽然加载了该组件，但是是按需开启，必须有对应的jar包才会开启，没有jar会爆红，就没开启
	static class AspectJAutoProxyingConfiguration {

		@Configuration(proxyBeanMethods = false)
		@EnableAspectJAutoProxy(proxyTargetClass = false)
		@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "false")
		static class JdkDynamicAutoProxyConfiguration {

		}

		@Configuration(proxyBeanMethods = false)
		@EnableAspectJAutoProxy(proxyTargetClass = true)
		@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
				matchIfMissing = true)
		static class CglibAutoProxyConfiguration {

		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingClass("org.aspectj.weaver.Advice")
	@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
			matchIfMissing = true)
	static class ClassProxyingConfiguration {

		@Bean
		static BeanFactoryPostProcessor forceAutoProxyCreatorToUseClassProxying() {
			return (beanFactory) -> {
				if (beanFactory instanceof BeanDefinitionRegistry) {
					BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
					AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry);
					AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
				}
			};
		}

	}

}
```

### 1.4 按需加载案例

- 都是xxxAutoConfiguration: spring的自动配置类
- 2.7.12的自动配置类的配置都在如下文件中

![image-20230605153616878](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230605153616878.png)

#### AopAutoConfiguration 

```java
package org.springframework.boot.autoconfigure.aop;

import org.aspectj.weaver.Advice;

import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@AutoConfiguration
// 配置文件中按需加载：如果没配，默认你配了
@ConditionalOnProperty(prefix = "spring.aop", name = "auto", havingValue = "true", matchIfMissing = true)
public class AopAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Advice.class)// 如果jar包中包含Advice.class
    static class AspectJAutoProxyingConfiguration {

        @Configuration(proxyBeanMethods = false)
        @EnableAspectJAutoProxy(proxyTargetClass = false)
        @ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "false")
        static class JdkDynamicAutoProxyConfiguration {

        }

        @Configuration(proxyBeanMethods = false)
        @EnableAspectJAutoProxy(proxyTargetClass = true)
        @ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
                matchIfMissing = true)
        static class CglibAutoProxyConfiguration {

        }

    }

    @Configuration(proxyBeanMethods = false)
    /**
     * 场景：选择性开启一个功能中两个服务方
     * 如果没有Advice这个类，则才会生效
     */
    @ConditionalOnMissingClass("org.aspectj.weaver.Advice")
    /**
     * 需要手动在配置文件中配置
     */
    @ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
            matchIfMissing = true)
    static class ClassProxyingConfiguration {

        @Bean
        static BeanFactoryPostProcessor forceAutoProxyCreatorToUseClassProxying() {
            return (beanFactory) -> {
                if (beanFactory instanceof BeanDefinitionRegistry) {
                    BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
                    AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry);
                    AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
                }
            };
        }

    }
}
```

#### DispatcherServletAutoConfiguration

- WebMvcProperties： 对应配置文件，取值后和通过new的方式，对另外一个VO进行映射，即使属性相同
- 组件改名：如果容器中包含某个组件，但是组件名不对，可以通过这种方式对组件进行更改名字

```java
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
// AutoConfiguration通过after和before属性，来进行自动注册的顺序
@AutoConfiguration(after = ServletWebServerFactoryAutoConfiguration.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass(DispatcherServlet.class)
public class DispatcherServletAutoConfiguration {

    public static final String DEFAULT_DISPATCHER_SERVLET_BEAN_NAME = "dispatcherServlet";

    public static final String DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME = "dispatcherServletRegistration";

    @Configuration(proxyBeanMethods = false)
    @Conditional(DefaultDispatcherServletCondition.class)
    @ConditionalOnClass(ServletRegistration.class)
    // 从配置文件的配置文件中去取值，以后容器中就会有该WebMvcProperties组件
    @EnableConfigurationProperties(WebMvcProperties.class)
    protected static class DispatcherServletConfiguration {

        @Bean(name = DEFAULT_DISPATCHER_SERVLET_BEAN_NAME)
        public DispatcherServlet dispatcherServlet(WebMvcProperties webMvcProperties) {
            // 自己new好，然后从WebMvcProperties中去取值，然后将对象放在容器中
            // WebMvcProperties和DispatcherServlet不要做成一个对象，哪怕两个属性一摸一样
            DispatcherServlet dispatcherServlet = new DispatcherServlet();
            dispatcherServlet.setDispatchOptionsRequest(webMvcProperties.isDispatchOptionsRequest());
            dispatcherServlet.setDispatchTraceRequest(webMvcProperties.isDispatchTraceRequest());
            dispatcherServlet.setThrowExceptionIfNoHandlerFound(webMvcProperties.isThrowExceptionIfNoHandlerFound());
            dispatcherServlet.setPublishEvents(webMvcProperties.isPublishRequestHandledEvents());
            dispatcherServlet.setEnableLoggingRequestDetails(webMvcProperties.isLogRequestDetails());
            return dispatcherServlet;
        }

        @Bean
        @ConditionalOnBean(MultipartResolver.class)
        @ConditionalOnMissingBean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
        // 组件改名：但是就会组件重复了
        // 如果容器中有某个组件，但是不知道组件名是什么，通过这种方式，通过方法名或者bean的属性，来对容器中的组件进行改名
        public MultipartResolver multipartResolver(MultipartResolver resolver) {
            // Detect if the user has created a MultipartResolver but named it incorrectly
            return resolver;
        }

    }
```

#### HttpEncodingAutoConfiguration

- ConditionalOnMissingBean： 容器中如果用户没配置，spring才会去给你一个兜底的bean
- spring设计原则一： 底层配置了所有的东西，用户如果配置，则用用户的
- spring设计原则二： 用户不单独配置，但是用户可以去改对应的属性Encoding

```java
@AutoConfiguration
@EnableConfigurationProperties(ServerProperties.class) // 自动属性导入
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(CharacterEncodingFilter.class)
@ConditionalOnProperty(prefix = "server.servlet.encoding", value = "enabled", matchIfMissing = true)
public class HttpEncodingAutoConfiguration {

    private final Encoding properties;

    // 将ServerProperties从容器中去拿，因为这个bean只有一个构造方法，所以省略@Autowired
    public HttpEncodingAutoConfiguration(ServerProperties properties) {
        this.properties = properties.getServlet().getEncoding();
    }

    @Bean
    /**
     * 如果容器中用户没这种组件，才会去使用spring提供的
     */
    @ConditionalOnMissingBean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new OrderedCharacterEncodingFilter();
        filter.setEncoding(this.properties.getCharset().name());
        filter.setForceRequestEncoding(this.properties.shouldForce(Encoding.Type.REQUEST));
        filter.setForceResponseEncoding(this.properties.shouldForce(Encoding.Type.RESPONSE));
        return filter;
    }
```

```java
public class Encoding {
    public static final Charset DEFAULT_CHARSET;
    private Charset charset;

    public Encoding() {
       // 默认值：自己随便去实现 
        this.charset = DEFAULT_CHARSET;
    }

    public Charset getCharset() {
        return this.charset;
    }

    static {
        DEFAULT_CHARSET = StandardCharsets.UTF_8;
    }
}
```

## 2. 启动流程



# 自定义Starter

[官方starter](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.build-systems.starters)

- 官方：spring-boot-starter-data-redis
- 个人定制：xxx-spring-boot-starter

## 1. 入门案例

- spring-starter的基本使用，普通的spring代码的starter抽取

### 1.1 引入原因

- springboot的多个项目中，包含相同的代码(spring代码或者普通java代码)，需要进行抽取成三方包
- 三方包，因为包结构不同，其他项目直接导入依赖的话，不会扫描三方包中的spring组件
- 因此，spring官方推荐使用xxx-spring-starter

### 1.2 功能代码

- 功能代码抽取，其他项目使用时，因为spring的包扫描规则，不能扫描到该三方类
- 直接在主启动类上加@ComponentScan扫第三方的包才能将下面的代码加载到容器中，但是不推荐这样写

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.erick.daydreamer</groupId>
    <artifactId>email-spring-starter</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.12</version>
    </parent>


    <dependencies>
        <!--web场景的依赖-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

        <!--配置解析提示-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>

    </dependencies>

</project>
```

```java
package com.aws.email.service;

import org.springframework.stereotype.Service;

@Service
public class AccountService {
    public void writeDb() {
        System.out.println("write data to db");
    }
}
```

```java
package com.aws.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "aws.email")
@Configuration
public class EmsProperties {
    private String region;
    private String userName;
    private String password;
    private int price;
}
```

```java
package com.aws.email.service;

import com.aws.email.EmsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private EmsProperties emsProperties;

    @Autowired
    private AccountService accountService;

    public void getInfo() {
        accountService.writeDb();
        System.out.println(emsProperties);
    }
}
```

![image-20230606164322644](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230606164322644.png)

### 1.3 基本抽取-@Import

#### starter方

- 提供自动配置类，@Import所有功能代码中的，被spring管理的组件

```java
package com.aws.email;

import com.aws.email.service.AccountService;
import com.aws.email.service.EmailService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {AccountService.class, EmailService.class, EmsProperties.class})
public class EmsAutoConfiguration {
}
```

#### 调用方

```xml
<dependency>
  <groupId>com.erick.daydreamer</groupId>
  <artifactId>email-spring-starter</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

```java
@Import(EmsAutoConfiguration.class)
```

```yaml
aws:
  email:
    password: 123456
    user-name: shuzhan
    price: 14
    region: beijing
```

### 1. 4 再次抽取-注解

- 上面调用方需要知道具体要@Import对应的类，比较麻烦

#### starter方

- 添加对应的注解，声明starter要暴露出去的starter

```java
package com.aws.email.annotation;

import com.aws.email.EmsAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EmsAutoConfiguration.class)
public @interface EnableEms {
}
```

#### 调用方

- 在主启动类上添加该注解即可

```java
package com.erick;

import com.aws.email.annotation.EnableEms;
import com.aws.email.service.EmailService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableEms
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(Application.class);
        EmailService bean = run.getBean(EmailService.class);
        bean.getInfo();
    }
}
```

### 1.5 终极抽取-SPI

- 上面的方案，还是需要调用方来进行标注。希望调用方只要导入了starter的包，就把其对应的spring组件全部加载进来
- 只对starter进行修改，调用方只需要引入依赖和对应的配置文件，就能够导入starter的所有spring的组件

```bash
# 1. 删除starter的注解

# 2. 2.7版本后，在starter的resource目录下，添加META-INF/spring目录下，添加文件
org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

```bash
# 文件中是要暴露的自动配置类的全限定类名
com.aws.email.EmsAutoConfiguration
```

![image-20230606172839733](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230606172839733.png)

## 2. 进阶案列

- 类似springboot官方的案例

### 2.1 starter

#### @Autowired方式

```java
package com.taobao;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "taobao.good")
public class GoodProperties {
    private String brand;
    private String name;
    private int price;
}
```

```java
package com.taobao.service;

import com.taobao.GoodProperties;
import org.springframework.beans.factory.annotation.Autowired;

/*默认service不要放在容器中, 而是通过自动配置，把这个service交给容器*/
public class AccountService {

    @Autowired
    private GoodProperties goodProperties;

    public void showInfo() {
        System.out.println(goodProperties);
    }
}
```

```java
package com.taobao;
/*选择性的把service放在容器中*/

import com.taobao.service.AccountService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// 把properties属性进行填充并放在容器中
@EnableConfigurationProperties(value = GoodProperties.class)
public class AccountAutoConfiguration {

    @Bean
    /*容器中没有AccountService时才生效*/
    @ConditionalOnMissingBean
    public AccountService getAccountService() {
        /*new的时候，自动会把里面的GoodProperties进行初始化*/
        System.out.println("starter中的AccountService");
        return new AccountService();
    }
}
```

```bash
com.taobao.AccountAutoConfiguration
```

#### 常用方式

```java
package com.taobao.service;

import com.taobao.GoodProperties;

/*默认service不要放在容器中, 而是通过自动配置，把这个server交给容器*/
public class AccountService {

    /*属性也不要通过set来注入*/
    private final GoodProperties goodProperties;

    public AccountService(GoodProperties goodProperties) {
        this.goodProperties = goodProperties;
    }

    public void showInfo() {
        System.out.println(goodProperties);
    }
}
```

```java
package com.taobao;
/*选择性的把service放在容器中*/

import com.taobao.service.AccountService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
// 把properties属性进行填充并放在容器中
@EnableConfigurationProperties(value = GoodProperties.class)
public class AccountAutoConfiguration {

    @Bean
    /*容器中没有AccountService时才生效
     * 参数是从容器中拿*/
    @ConditionalOnMissingBean
    public AccountService getAccountService(GoodProperties properties) {
        /*new的时候，自动会把里面的GoodProperties进行初始化*/
        System.out.println("starter中的AccountService");
        return new AccountService(properties);
    }
}
```

### 2.2 调用方

```bash
# 不配置的时候，用starter提供的@Bean

# 调用方配置了三方包的bean的时候，就用配置方的
- 并且也会进行属性的@Autowired自动注入
```

```java
package com.erick.daydreamer.config;

import com.taobao.service.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaobaoConfig {

    /*虽然是自己new的，但是因为AccountService中有@Autowired的属性，
    所以goodProperties属性会自动填充*/
    @Bean
    public AccountService getAccountService() {
        System.out.println("调用方的AccountService");
        return new AccountService();
    }
}
```
