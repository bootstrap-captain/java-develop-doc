# XML

## 1. 入门

### 1.1 aspectj

- 也是实现AOP的一个框架，spring将其引入到了spring的aop中

```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.7</version>
</dependency>
```

### 1.2 XML

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/aop
       http://www.springframework.org/schema/aop/spring-aop.xsd
      ">

    <!--被代理类-->
    <bean id="userServiceImpl" class="com.erick.daydreamer.UserServiceImpl"></bean>

    <!--通知类-->
    <bean id="myAdvice" class="com.erick.daydreamer.MyAdvice"></bean>

    <!--aop相关配置-->
    <aop:config>

        <!--切点表达式：指定哪些方法要被增强
                       可以配置多个pointcut-->
        <aop:pointcut id="firstPoint" expression="execution(void com.erick.daydreamer.UserServiceImpl.work())"/>

        <!--织入-->
        <aop:aspect ref="myAdvice">
            <aop:before method="beforeMethod" pointcut-ref="firstPoint"></aop:before>
        </aop:aspect>
    </aop:config>

</beans>
```

### 1.3 被代理类及其接口

```java
package com.erick.daydreamer;

public class UserServiceImpl implements UserService {
    @Override
    public void work() {
        System.out.println("user-service work");
    }

    @Override
    public String get() {
        System.out.println("user-service get");
        return "success";
    }
}
```

```java
package com.erick.daydreamer;

public interface UserService {

    void work();

    String get();
}
```

### 1.4 测试

```java
package com.erick.daydreamer;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test01 {

    @Test
    void test01() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("erick.xml");
        // 获取的是接口对象，而不是具体的实现类，因为是代理
        UserService bean = applicationContext.getBean(UserService.class);
        bean.work();
    }
}
```

## 2. 配置详解

### 2.1 切点配置

```xml
<!--execution([访问修饰符] [返回值类型] [包名.类名.方法名] (参数)])
      1. 访问修饰符可以省略
      2. 返回值，包名，类名，方法名，可以用 * 代表任意
      3. 包之间用 .. 表示该包及子包下的类
      4. 参数列表可以用 .. 表示任意参数-->
```

```bash
# 最详细的
execution(public void com.erick.aop.UserServiceImpl.show())
  
# com.erick.aop.UserServiceImpl的任意方法
execution(* com.erick.aop.UserServiceImpl.*(..))
  
# com.erick.aop包下的任意类的任意方法
execution(* com.erick.aop.*.*(..))

# com.erick.aop包及其子包下的任意类的任意方法
execution(* com.erick.aop..*.*(..))
```

### 2.2 切面配置

```xml
<!--aop相关配置-->
<aop:config>
    <!--公共的可以配置在最外面-->
    <aop:pointcut id="firstPoint" expression="execution(void com.erick.daydreamer.UserServiceImpl.work())"/>
    <aop:pointcut id="secondPoint" expression="execution(void com.erick.daydreamer.UserServiceImpl.get())"/>

    <!--织入-->
    <aop:aspect ref="myAdvice">
        <aop:before method="beforeMethod" pointcut-ref="firstPoint"></aop:before>
        <aop:before method="afterMethod" pointcut-ref="secondPoint"></aop:before>
        <!--也可以单独指定切面-->
        <aop:before method="randomMethod" pointcut="execution(void com.erick.daydreamer.UserServiceImpl.sleep())"></aop:before>
    </aop:aspect>
</aop:config>
```

## 3. 通知类型

- AspectJ的通知包含以下五种类型

![image-20231222142401493](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231222142401493.png)

```bash
# 每种方法在执行的时候，都可以动态获得当前切入点是什么
# JointPoint
- 连接点对象，任何通知都可以使用，可以获得当前目标对象，目标方法参数等信息

# ProceedingJoinPoint
- JointPoint子对象
- 主要在环绕通知中，执行proceed()，从而执行被代理目标方法
- 比如就可以用作统计方法的响应时间
```

```java
package com.erick.daydreamer;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

public class MyAdvice {

    public void before(JoinPoint joinPoint) {
        Object target = joinPoint.getTarget(); // 目标方法所在的类
        Object[] args = joinPoint.getArgs(); // 目标方法的参数
        System.out.println("before-method");
    }

    public void afterReturning() {
        System.out.println("after-returning");
    }

    /*统计耗时的方法*/
    public Object round(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = point.proceed();// 执行目标方法
        System.out.println(System.currentTimeMillis() - start);
        return proceed;
    }

    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    public void after() {
        System.out.println("after");
    }
}
```

## 4. Adviser配置

- 上面的都是通过配置
- 通知类：通过继承spring的Adviser的接口，来表示通知类型

![image-20231222153252383](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231222153252383.png)

## 5. 代理方式

- spring底层，同时兼容了JDK动态代理和Cglib动态代理

| 代理技术 | 使用条件                                                     | 配置方式                                                     |
| -------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| JDK      | 目标对象有接口，基于接口                                     | 目标类有接口时，默认方式                                     |
| Cglib    | 目标对象无接口，且不能使用final修饰，基于被代理对象动态生成子类作为代理对象 | 目标类无接口时，默认方式<br>有接口时，可手动配置<aop:config proxy-target-class="true">强制使用cglib |

![image-20231222155223254](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231222155223254.png)

# Annotation

## 1. 基本案例

```java
package com.erick.daydreamer;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@ComponentScan(basePackages = "com.erick.daydreamer")
@EnableAspectJAutoProxy// 开启aspectJ
public class ErickConfig {
}
```

```java
package com.erick.daydreamer;

import org.aopalliance.aop.Advice;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class MyAdvice implements Advice {

    @Pointcut("execution(void com.erick.daydreamer.UserServiceImpl.*(..))")
    public void firstPointCut() {

    }

    @Before(value = "firstPointCut()")
    public void before(JoinPoint joinPoint) {
        Object target = joinPoint.getTarget(); // 目标方法所在的类
        Object[] args = joinPoint.getArgs(); // 目标方法的参数
        System.out.println("before-method");
    }

    @AfterReturning(value = "firstPointCut()")
    public void afterReturning() {
        System.out.println("after-returning");
    }


    @Around(value = "firstPointCut()")
    public Object round(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = point.proceed();// 执行目标方法
        System.out.println(System.currentTimeMillis() - start);
        return proceed;
    }
}
```

## 2. AOP开启注解

- @EnableAspectJAutoProxy

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AspectJAutoProxyRegistrar.class)
public @interface EnableAspectJAutoProxy {
	boolean proxyTargetClass() default false;
	boolean exposeProxy() default false;
}
```

```java
package org.springframework.context.annotation;

import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

class AspectJAutoProxyRegistrar implements ImportBeanDefinitionRegistrar {

  @Override
	public void registerBeanDefinitions(
			AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    
    // 就是注入进容器一个BeanPostProcessor
		AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(registry);

		AnnotationAttributes enableAspectJAutoProxy =
				AnnotationConfigUtils.attributesFor(importingClassMetadata, EnableAspectJAutoProxy.class);
		if (enableAspectJAutoProxy != null) {
			if (enableAspectJAutoProxy.getBoolean("proxyTargetClass")) {
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
			if (enableAspectJAutoProxy.getBoolean("exposeProxy")) {
				AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
			}
		}
	}
}
```

# 源码解读

- springframework-aop包下的：AopNamespaceHandler处理

```java
public class AopNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		// In 2.0 XSD as well as in 2.5+ XSDs
		registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
    // 注册代理，下一步
		registerBeanDefinitionParser("aspectj-autoproxy", new AspectJAutoProxyBeanDefinitionParser()); 
		registerBeanDefinitionDecorator("scoped-proxy", new ScopedProxyBeanDefinitionDecorator());

		// Only in 2.0 XSD: moved to context namespace in 2.5+
		registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
	}

}
```

```java
// Parser类
class AspectJAutoProxyBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	@Nullable
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext, element);//
		extendBeanDefinition(element, parserContext);
		return null;
	}
```

```bash
# AnnotationAwareAspectJAutoProxyCreator
- 最终向容器中注入一个这个类
- 该类实现了BeanPostProcessor，通过后置处理，去动态修改bean

 # AbstractAutoProxyCreator  
 - 具体处理逻辑在这里
```

![AnnotationAwareAspectJAutoProxyCreator](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/AnnotationAwareAspectJAutoProxyCreator.png)

```java
	@Override
	public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) {
		if (bean != null) {
			Object cacheKey = getCacheKey(bean.getClass(), beanName);
			if (this.earlyBeanReferences.remove(cacheKey) != bean) {
				return wrapIfNecessary(bean, beanName, cacheKey);
			}
		}
		return bean;
	}
```

```java
	protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
		if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
			return bean;
		}
		if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
			return bean;
		}
		if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
			this.advisedBeans.put(cacheKey, Boolean.FALSE);
			return bean;
		}

		// Create proxy if we have advice.
		Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
		if (specificInterceptors != DO_NOT_PROXY) {
			this.advisedBeans.put(cacheKey, Boolean.TRUE);
			// 下一个
      Object proxy = createProxy(
					bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
			this.proxyTypes.put(cacheKey, proxy.getClass());
			return proxy;
		}

		this.advisedBeans.put(cacheKey, Boolean.FALSE);
		return bean;
	}
```

```java
private Object buildProxy(Class<?> beanClass, @Nullable String beanName,
			@Nullable Object[] specificInterceptors, TargetSource targetSource, boolean classOnly) {

		if (this.beanFactory instanceof ConfigurableListableBeanFactory clbf) {
			AutoProxyUtils.exposeTargetClass(clbf, beanName, beanClass);
		}

		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.copyFrom(this);

		if (proxyFactory.isProxyTargetClass()) {
			// Explicit handling of JDK proxy targets and lambdas (for introduction advice scenarios)
			if (Proxy.isProxyClass(beanClass) || ClassUtils.isLambdaClass(beanClass)) {
				// Must allow for introductions; can't just set interfaces to the proxy's interfaces only.
				for (Class<?> ifc : beanClass.getInterfaces()) {
					proxyFactory.addInterface(ifc);
				}
			}
		}
		else {
			// No proxyTargetClass flag enforced, let's apply our default checks...
			if (shouldProxyTargetClass(beanClass, beanName)) {
				proxyFactory.setProxyTargetClass(true);
			}
			else {
				evaluateProxyInterfaces(beanClass, proxyFactory);
			}
		}

		Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
		proxyFactory.addAdvisors(advisors);
		proxyFactory.setTargetSource(targetSource);
		customizeProxyFactory(proxyFactory);

		proxyFactory.setFrozen(this.freezeProxy);
		if (advisorsPreFiltered()) {
			proxyFactory.setPreFiltered(true);
		}

		// Use original ClassLoader if bean class not locally loaded in overriding class loader
		ClassLoader classLoader = getProxyClassLoader();
		if (classLoader instanceof SmartClassLoader smartClassLoader && classLoader != beanClass.getClassLoader()) {
			classLoader = smartClassLoader.getOriginalClassLoader();
		}
  // 下一步
		return (classOnly ? proxyFactory.getProxyClass(classLoader) : proxyFactory.getProxy(classLoader));
	}
```

- 具体生成代理对象的基础接口

```java
package org.springframework.aop.framework;

import org.springframework.lang.Nullable;

public interface AopProxy {
	Object getProxy();
	Object getProxy(@Nullable ClassLoader classLoader);
	Class<?> getProxyClass(@Nullable ClassLoader classLoader);
}
```

![image-20231223112036988](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231223112036988.png)

![image-20231223112936641](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20231223112936641.png)
