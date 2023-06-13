# 开发模式演变

## 1. 持久层方案

```bash
# 持久化方案
JDBC    Spring的JDBCTemplate   Apache的DBUtils   Mybatis
- JDBCTemplate和DBUtils都是对JDBC的简单封装的工具类，不是框架
- MyBatis是对JDBC的封装，并作为框架使用
```

```bash
# Mybatis: https://github.com/mybatis/mybatis-3
- Java编写：      封装JDBC, 开发者只需关注sql本身，无需注册驱动，创建连接等频繁操作
- ORM：          Object Relational Mapping， 将数据库的表和实体类对应起来
- 核心配置文件：   SqlMapConfig.xml核心配置文件， mapper.xml映射文件
```

## 2. 入门案例

- 主配置文件 + mapper配置文件 + 测试类(SqlSession)
- 依赖： mybatis + mysql-connector-java + junit
- java17, mysql-8.0.28

### 2.1 mybatis_config.xml

- 主配置文件，位置放在Resource的任意路径下, 读取时会指定路径
- 启动测试后，会去加载该配置文件，并获得所对应的Mapper.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
    <!-- 打印查询语句 -->
    <settings>
        <setting name="logImpl" value="STDOUT_LOGGING"/>
    </settings>

    <!-- 一个environments可以配置多个environment，定义多个数据源-->
    <environments default="dev">
        <environment id="dev">
            <!-- 指定事务管理的类型，简单使用Java的JDBC的提交和回滚设置
               JDBC:    JDBC中原生的事务管理方式
               MANAGED: 被管理，比如spring-->
            <transactionManager type="JDBC"/>
          
            <!-- POOLED是JDBC连接对象的数据源连接池的实现
               1. POOLED:   使用数据库链接池
               2. UNPOOLED: 不使用数据库连接池
               3. JNDI:     使用上下文中的数据源-->
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://120.79.28.20:3306/erick"/>
                <property name="username" value="root"/>
                <property name="password" value="123456"/>
            </dataSource>
        </environment>
        <environment id="test">
            <!-- 指定事务管理的类型，简单使用Java的JDBC的提交和回滚设置-->
            <transactionManager type="JDBC"/>
            <!-- POOLED是JDBC连接对象的数据源连接池的实现-->
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://120.79.28.20:3306/erick"/>
                <property name="username" value="root"/>
                <property name="password" value="123456"/>
            </dataSource>
        </environment>
    </environments>

    <mappers>
        <!--映射文件xml，放在resource目录下， 可多个-->
        <mapper resource="com/erick/StudentMapper.xml"/>
    </mappers>
</configuration>

```

### 2.2 StudentMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<!-- namespace: 方法调用时，用namespace.id,用来区分多个Mapper中含有同一个id无法识别的问题-->
<mapper namespace="erefsdf">
    <select id="getAll" resultType="com.erick.mybatis.Student">
        select *
        from student
    </select>
</mapper>
```

### 2.3 Test

```java
public class BasicTest {

    @Test
    public void first() throws IOException {
        InputStream inputStream = Resources.getResourceAsStream("mybatis_config.xml");
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlSessionFactory = builder.build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        List<Student> result = sqlSession.selectList("erefsdf.getAll");
        System.out.println(result);
        sqlSession.close();
        inputStream.close();
    }
}
```

## 3. 传统开发

```bash
# 1.  配置文件:       datasource主配置文件 ， mapper.xml 
# 2.  Dao层：         接口以及实现类

- 实现类中用SqlSession来进行CRUD
- 接口和对应的mapper.xml的出参入参要一致 (因为实现类中每个方法的入参和出参，均是从mapper.xml来的)
- 存在问题： 接口实现类存在硬编码问题
```

```java
package com.erick.mybatis.dao;

import com.erick.mybatis.entity.Student;

import java.util.List;

public interface StudentInter {
    List<Student> getAllStudent();

    Student getAllStudentById(int id);
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<!-- namespace: 方法调用时，用namespace.id,用来区分多个Mapper中含有同一个id无法识别的问题-->
<mapper namespace="erefsdf">
    <select id="getAll" resultType="com.erick.mybatis.entity.Student">
        select *
        from student
    </select>

    <select id="getById" resultType="com.erick.mybatis.entity.Student">
        select *
        from student
        where id = #{student_id}
    </select>
</mapper>
```

```java
public class StudentQuery implements StudentInter {

    private SqlSession getSqlSession() {
        try (InputStream inputStream = Resources.getResourceAsStream("mybatis_config.xml")) {
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            SqlSessionFactory sqlSessionFactory = builder.build(inputStream);
            return sqlSessionFactory.openSession();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Student> getAllStudent() {
        SqlSession sqlSession = getSqlSession();
        List<Student> students = sqlSession.selectList("erefsdf.getAll");
        sqlSession.close();
        return students;
    }

    @Override
    public Student getAllStudentById(int id) {
        SqlSession sqlSession = getSqlSession();
        Student student = sqlSession.selectOne("erefsdf.getById", id);
        sqlSession.close();
        return student;
    }
}
```

## 4. 动态代理

- Mybatis为接口提供了代理对象Mapper，不用写具体的实现类，解决硬编码问题
- 如果要使用动态代理开，必须遵循下面的原则

```bash
# 方法名：         mapper.xml的mapper id 必须和接口的一致
# mapper路径：     放在resource目录下，mapper.xml的 {namespace} 与接口的全路径一致

- 动态代理时，根据Interface接口名和mapper的namespace对应起来， 根据方法名和mapper的id对应起来，执行对应的sql
```

```xml
<mappers>
  <!--映射文件xml，放在resource目录下， 可多个-->
  <mapper resource="com/erick/StudentMapper.xml"/>
  <mapper resource="com/erick/EmployeeMapper.xml"/>
</mappers>
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<!-- namespace: 方法调用时，用namespace.id,用来区分多个Mapper中含有同一个id无法识别的问题-->
<mapper namespace="com.erick.mybatis.dao.EmployeeInter">
    <select id="getAll" resultType="com.erick.mybatis.entity.Employee">
        select *
        from employee
    </select>
</mapper>
```

```java
public interface EmployeeInter {
     List<Employee> getAll();
}
```

```java
public class ProxyTest {

    private SqlSession getSqlSession() {
        try (InputStream inputStream = Resources.getResourceAsStream("mybatis_config.xml")) {
            SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
            SqlSessionFactory sqlSessionFactory = builder.build(inputStream);
            return sqlSessionFactory.openSession();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getAllTest() {
        SqlSession sqlSession = getSqlSession();
        EmployeeInter mapper = sqlSession.getMapper(EmployeeInter.class);
        List<Employee> result = mapper.getAll();
        System.out.println(result);
    }
}
```

# 核心配置文件

## 1. Jdbc.properties

```properties
jdbc.driver=com.mysql.cj.jdbc.Driver
jdbc.url=jdbc:mysql://120.79.28.20:3306/erick
jdbc.username=root
jdbc.password=123456
```

## 2. Mybatis_config

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">

<configuration>
    <!--核心配置文件的顺序，必须按照官方指定-->

    <!--从resource根目录下读取配置文件，下面使用${}-->
    <properties resource="jdbc.properties"/>

    <!-- 打印查询语句 -->
    <settings>
        <setting name="logImpl" value="STDOUT_LOGGING"/>
      <!--默认为false-->
        <setting name="mapUnderscoreToCamelCase" value="false"/>
    </settings>

    <!--类型别名,可以配置多个-->
    <typeAliases>
        <!--不写alias，默认别名是类名， 不区分大小写-->
        <typeAlias type="com.erick.mybatis.entity.People" alias="people"/>
        <typeAlias type="com.erick.mybatis.entity.Student"/>
        <!--通过包来设置： 默认别名是对应实体类 类名，不区分大小写-->
        <package name="com.erick.mybatis.entity"/>
    </typeAliases>

    <!-- 一个environments可以配置多个environment，定义多个数据源-->
    <environments default="dev">
        <environment id="dev">
            <!-- 指定事务管理的类型，简单使用Java的JDBC的提交和回滚设置
               JDBC:    JDBC中原生的事务管理方式
               MANAGED: 被管理，比如spring-->
            <transactionManager type="JDBC"/>

            <!-- POOLED是JDBC连接对象的数据源连接池的实现
               1. POOLED:   使用数据库链接池
               2. UNPOOLED: 不使用数据库连接池
               3. JNDI:     使用上下文中的数据源-->
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
                <property name="url" value="jdbc:mysql://120.79.28.20:3306/erick"/>
                <property name="username" value="root"/>
                <property name="password" value="123456"/>
            </dataSource>
        </environment>

        <environment id="test">
            <!-- 指定事务管理的类型，简单使用Java的JDBC的提交和回滚设置-->
            <transactionManager type="JDBC"/>
            <!-- POOLED是JDBC连接对象的数据源连接池的实现-->
            <dataSource type="POOLED">
                <property name="driver" value="${jdbc.driver}"/>
                <property name="url" value="${jdbc.url}"/>
                <property name="username" value="${jdbc.username}"/>
                <property name="password" value="${jdbc.password}"/>
            </dataSource>
        </environment>
    </environments>

    <mappers>
        <!--映射文件xml，放在resource目录下， 可多个-->
        <mapper resource="com/erick/StudentMapper.xml"/>

        <!--通过包的方式引入： 最终resource和java文件会被打包在target的同一个目录
           1. mapper接口和映射文件所在的包必须一致： 虽然一个在main下面，一个在resource下面
           2. mapper接口的名字和映射文件的名字必须保持一致-->
        <package name="com.erick.mybatis.dao"/>
    </mappers>
</configuration>
```

# 基本用法

## 1. 获取参数

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.erick.mybatis.dao.EmployeeMapper">

    <!-- 1. #{value}:
           1.1 占位符赋值，预编译
           1.2 如果为字符串类型或日期类型的字段，自动加单引号
           1.3 不存在sql注入
         2. ${value}:
           2.1 字符串拼接，无预编译
           2.2 如果为字符串类型或日期类型的字段，需要手动加单引号
           2.3 可能造成sql注入
    -->

    <!--单个参数，#{}中参数可以为任意名字-->
    <select id="getByAddress" resultType="employee">
        /*select * from employee where address = ?*/
        select *
        from employee
        where address = #{address}
    </select>

    <select id="getByAddress1" resultType="employee">
        /*select * from employee where address = 'address'
        1.  如果是string或者data类型，必须加单引号*/
        select *
        from employee
        where address = '${address}'
    </select>

    <!--2. 获取多个字段： mybatis会将接口中参数，按照顺序通过map传递进来
                     比如使用了两个参数，实际会有两个map，key分别为arg* 和 param
      方式一： 以arg0, arg1, arg2 传递
      方式二： 以param1, param2，param3传递
      方式三： 传递进来是map，然后在map中自己定义key
      方式四： 使用mybatis的@Params， 其实还是放在map集合中-->
    <select id="getByAddressAndId1" resultType="employee">
        select *
        from employee
        where id = #{arg0}
          and address = #{arg1};
    </select>

    <select id="getByAddressAndId2" resultType="employee">
        select *
        from employee
        where id = #{param1}
          and address = #{param2};
    </select>

    <select id="getByAddressAndId3" resultType="employee">
        select *
        from employee
        where id = #{erick_id}
          and address = #{erick_address};
    </select>

    <select id="getByAddressAndId4" resultType="employee">
        select *
        from employee
        where id = #{my_id}
          and address = #{my_address};
    </select>

    <!--直接通过pojo传递参数: 访问pojo的属性key，因此属性key必须和pojo的一样
       1. 不用写传递参数的类型
       2. #{key}: 属性名是和对应pojo的 getId,  去掉get，然后首字母小写，
                  和pojo中的属性名没有关系-->
    <select id="getByPojo" resultType="employee">
        select *
        from employee
        where id = #{id}
          and address = #{address}
    </select>
</mapper>
```

```java
package com.erick.mybatis.dao;

import com.erick.mybatis.entity.Employee;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface EmployeeMapper {
    List<Employee> getByAddress(String address);

    List<Employee> getByAddress1(String address);

    List<Employee> getByAddressAndId1(int id, String address);

    List<Employee> getByAddressAndId2(int id, String address);

    // 相当于自己定义了map: 使用的时候自己自定义key和对应的xml参数对应起来即可
    List<Employee> getByAddressAndId3(Map<String, Object> params);

    // 使用mybatis提供的param， 还是将数据放在集合中
    List<Employee> getByAddressAndId4(@Param("my_id") int id, @Param("my_address") String address);

    List<Employee> getByPojo(Employee employee);
}
```

```java
package com.erick;

import com.erick.mybatis.dao.EmployeeMapper;
import com.erick.mybatis.entity.Employee;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PeopleTest {

    private static InputStream in;
    private static SqlSessionFactoryBuilder builder;
    private static SqlSessionFactory factory;
    private static SqlSession sqlSession;

    private static EmployeeMapper employeeMapper;

    @Before
    public void init() throws IOException {
        in = Resources.getResourceAsStream("mybatis_config.xml");
        builder = new SqlSessionFactoryBuilder();
        factory = builder.build(in);
        sqlSession = factory.openSession();
        employeeMapper = sqlSession.getMapper(EmployeeMapper.class);
    }

    @After
    public void close() throws IOException {
        sqlSession.close();
        in.close();
    }

    @Test
    public void getByAddress() {
        System.out.println(employeeMapper.getByAddress("beijing"));
    }

    @Test
    public void getByAddress01() {
        System.out.println(employeeMapper.getByAddress1("beijing"));
    }

    @Test
    public void getByAddressAndId1() {
        System.out.println(employeeMapper.getByAddressAndId1(1, "beijing"));
    }

    @Test
    public void getByAddressAndId2() {
        System.out.println(employeeMapper.getByAddressAndId2(1, "beijing"));
    }

    @Test
    public void getByAddressAndId3() {
        Map<String, Object> params = new HashMap<>();
        /*key和xml中的value相同*/
        params.put("erick_id", 1);
        params.put("erick_address", "beijing");
        System.out.println(employeeMapper.getByAddressAndId3(params));
    }

    @Test
    public void getByAddressAndId4() {
        System.out.println(employeeMapper.getByAddressAndId4(1, "beijing"));
    }

    @Test
    public void getByPojo() {
        Employee employee = new Employee();
        employee.setAddress("beijing");
        employee.setId(1);
        System.out.println(employeeMapper.getByPojo(employee));
    }
}
```

## 2. 查询

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.erick.mybatis.dao.EmployeeInter">

    <select id="getUserById" resultType="employee">
        select *
        from employee
        where id = #{id};
    </select>

    <!--如果count(field), 则不包含null的field
     1. 如果查询的结果是Integer, 可以使用类全名， mybatis对基本数据类型也都做了默认简写值-->
    <select id="getCount" resultType="int">
        select count(1)
        from employee;
    </select>

    <select id="getUserByIdToMap" resultType="map">
        select *
        from employee
        where id = #{id};
    </select>

    <!--返回值还是map： 因为返回值指的是list中的元素
        返回参数是List时，返回值用List中的对象-->
    <select id="getUserByIdToMapList" resultType="map">
        select *
        from employee;
    </select>

    <!--将查询的结果保存在map中-->
    <select id="getUserByIdToMapSpec" resultType="map">
        select *
        from employee;
    </select>

    <!--模糊查询:
       1.  '%${address}%'
       2.  concat('%', #{address}, '%')
       3.  "%" #{address} "%"    较为推荐 -->
    <select id="getByAddress1" resultType="employee">
        select *
        from employee
        where address like '%${address}%';
    </select>
    <select id="getByAddress2" resultType="employee">
        select *
        from employee
        where address like concat('%', #{address}, '%');
    </select>
    <select id="getByAddress3" resultType="employee">
        select *
        from employee
        where address like "%" #{address} "%";
    </select>
</mapper>
```

```java
public interface EmployeeInter {

    /*如果查询的结果为多条时候，一定要用List<Employee> 来接收
     * 1. 建议都加上@Param*/
    Employee getUserById(@Param("id") int id);

    int getCount();

    /*常用的一种：  查询出的结果，以mysql的字段名和value分别作为k-v */
    Map<String, Object> getUserByIdToMap(@Param("id") int id);

    List<Map<String, Object>> getUserByIdToMapList();

    // 将查询出来的数据的id作为map的key，value为每一行数据转换的map
    @MapKey("id")
    Map<String, Object> getUserByIdToMapSpec();

    List<Employee> getByAddress1(String address);
    List<Employee> getByAddress2(String address);
    List<Employee> getByAddress3(String address);
}
```

## 3. 自增主键

```bash
- 写操作时，必须要commit
- 插入数据时候，对于自增主键，不用赋值
- 本次插入，并查询，ID为null
- 本次插入，并查询，想要ID, 可以采用 selectKey
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.erick.mybatis.dao.StudentMapper">

    <insert id="insertOne">
        insert into student (first_name, last_name, name, address, age)
        values (#{firstName}, #{lastName}, #{name}, #{address}, #{age})
    </insert>

    <!--1. id主键自增： 是属于JDBC本身的功能
              1.1 返回插入该对象时候，数据库存储的id;
              1.2. keyProperty: bo属性中的值；    keyColumn： 数据库列名的值;
              1.3. order: BEFORE和AFTER，必须大写，before会返回id为0，after会返回数据库存储的id-->
    <insert id="insertOneWithId">
        insert into student (first_name, last_name, name, address, age)
        values (#{firstName}, #{lastName}, #{name}, #{address}, #{age})

        <selectKey keyColumn="id" keyProperty="id" order="AFTER" resultType="int">
            <!--固定写法-->
            select LAST_INSERT_ID()
        </selectKey>
    </insert>

    <insert id="insertOneWithId1" useGeneratedKeys="true" keyProperty="id" keyColumn="id">
        insert into student (first_name, last_name, name, address, age)
        values (#{firstName}, #{lastName}, #{name}, #{address}, #{age})
    </insert>
</mapper>
```

```java
public interface StudentMapper {
    void insertOne(Student student);

    void insertOneWithId(Student student);

    void insertOneWithId1(Student student);
}
```

```java
public class StudentTest {

    @Test
    public void insertOne() {
        Student student = getStudent();
        studentMapper.insertOne(student);
        /*获取不到对应的id， id为0*/
        System.out.println(student);
        sqlSession.commit();
    }

    @Test
    public void insertOneWithId() {
        Student student = getStudent();
        studentMapper.insertOneWithId(student);
        System.out.println(student);
        sqlSession.commit();
    }

    @Test
    public void insertOneWithId1() {
        Student student = getStudent();
        studentMapper.insertOneWithId1(student);
        System.out.println(student);
        sqlSession.commit();
    }

    private Student getStudent() {
        Student student = new Student();
        student.setAddress("NewYork");
        student.setFirstName("shu");
        student.setLastName("zhan");
        student.setName("shu zhan");
        student.setAge(34);
        return student;
    }
}
```

## 4.  字段映射

```xml
   <!-- 数据库字段和java实体类属性字段对应不上，则得到的对象的某个字段就会为空
       1.1  方式一： 别名映射，最高效，因为是从sql层面进行映射的，但如果类似查询太多，则写起来太麻烦
       1.2. 方式二： 如果mysql和java的对象符合驼峰映射，只需要开启驼峰映射就可
       1.3  方式三： 利用resultMap
                                        1. resultMap和resultType只能写一个
                                        2. column：列字段； property：bean属性；
                                        3. 只需要对字段不统一的进行映射即可；
                                        4. select字段中，可以用*，也可以用数据库的具体字段
                                        5. id: 映射时候的主键-->
    <!--利用驼峰-->
    <select id="getOneById1" resultType="student">
        select *
        from student
        where id = #{id}
    </select>

    <!--利用别名-->
    <select id="getOneById2" resultType="student">
        select id, first_name AS firstName, last_name AS lastName, name, age, address
        from student
        where id = #{id}
    </select>

    <resultMap id="studentRef" type="com.erick.mybatis.entity.Student">
        <id column="id" property="id"></id>
        <result column="first_name" property="firstName"></result>
        <result column="last_name" property="lastName"></result>
    </resultMap>
    <!--利用Map进行结果映射-->
    <select id="getOneById3" resultMap="studentRef">
        select *
        from student
        where id = #{id}
    </select>
```

## 5. 包装pojo

```bash
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.erick.mybatis.dao.IphoneMapperInter">
    <!-- 入参为包装类的POJO：匹配值时： 用包装类中的bean对象变量名. 对象属性名 -->
    <select id="getOne" parameterType="com.erick.mybatis.PhoneInfo" resultType="com.erick.mybatis.Phone">
        select *
        from phone
        where brand =
              #{phone.brand}
    </select>
</mapper>
```

```java
@Data
@Builder
public class PhoneInfo implements Serializable {
    private static final long serialVersionUID = 3369383956720931425L;
    private int weight;

    private Phone phone;
}
```

# 动态SQL

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.erick.mybatis.dao.PeopleMapper">

    <!--if：通过传入的java对象的字段，判断是否加上去-->
    <!--where：
               1. 有条件成立，自动生成where关键字
               2. 如果if条件都不满足，则不会加where
               3. 判断条件中去掉拼接的第一个and
    -->
    <select id="getByPojo" resultType="people">
        select *
        from people
        <where>
            <if test="firstName!=null and firstName!=''">
                and first_name = #{firstName}
            </if>
            <if test="lastName!=null and lastName!=''">
                and last_name = #{lastName}
            </if>
            <if test="address!=null and address!=''">
                and address = #{address}
            </if>
        </where>
    </select>

    <!--foreach: 1. 假如传入的是一个List，mybatis还是会把该list放在map中，key就是list
                 2. 假如传入的是一个Array， mybatis会把该array放在map中，key为array

                 *. 也可以自定义对应的key值，也就是collections对应的value-->

    <!--1. collection: 对应的要遍历的集合的名称
        2. item: 集合中的元素
        3. open:集合遍历开始时候的元素
        4. close: 集合遍历结束时候的元素
        5. separator: 属性的分割符-->
    <insert id="insertBatch1">
        insert into people (first_name, last_name, address, age) values
        <foreach collection="peoples" item="people" separator=",">
            (#{people.firstName},
            #{people.lastName},
            #{people.address},
            #{people.age})
        </foreach>
    </insert>

    <!--默认名称是list-->
    <insert id="insertBatch2">
        insert into people (first_name, last_name, address, age) values
        <foreach collection="list" item="people" separator=",">
            (#{people.firstName},
            #{people.lastName},
            #{people.address},
            #{people.age})
        </foreach>
    </insert>

    <insert id="insetBatchWithArr1">
        insert into people (first_name, last_name, address, age) values
        <foreach collection="array" item="people" separator=",">
            (#{people.firstName},
            #{people.lastName},
            #{people.address},
            #{people.age})
        </foreach>
    </insert>

    <insert id="insetBatchWithArr2">
        insert into people (first_name, last_name, address, age) values
        <foreach collection="peopleArr" item="people" separator=",">
            (#{people.firstName},
            #{people.lastName},
            #{people.address},
            #{people.age})
        </foreach>
    </insert>

    <delete id="deleteBatch">
        delete
        from people
        where id in
        <foreach collection="array" item="id" open="(" close=")" separator=",">
            #{id}
        </foreach>
    </delete>

    <!--sql片段-->
    <sql id="sqlRef">
        select *
        from
    </sql>
    <select id="getAll" resultType="people">
        <include refid="sqlRef"></include>
        people;
    </select>
</mapper>

```

```java
package com.erick.mybatis.dao;

import com.erick.mybatis.entity.People;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PeopleMapper {

    List<People> getByPojo(People people);

    /*会把该list放在map中，key为peoples*/
    void insertBatch1(@Param("peoples") List<People> peoples);

    void insertBatch2(List<People> peoples);

    void insetBatchWithArr1(People[] people);

    void insetBatchWithArr2(@Param("peopleArr") People[] people);

    void deleteBatch(int[] ids);

    List<People> getAll();
}
```

# Mybatis缓存

## 1. 一级缓存

- SQLSession 级别， 使用同一个SqlSession(Mapper代理类)查询的数据会被缓存
- 可以通过查看sql查询日志来发现
- 默认开启一级缓存

### 缓存失效

- 不同的SqlSession对应不同的一级缓存
- 同一个SqlSession，但是查询条件不同
- 同一个SqlSession两次查询期间，执行了任何一次写操作
- 同一个SqlSession两次查询期间，手动清空了缓存:  sqlSession.clearCache()

## 2. 二级缓存

- SqlSessionFactory级别， 通过同一个SqlSessionFactory创建的SqlSession查询的结果会被缓存

```bash
- 1. 在核心配置文件中，设置全局属性   cacheEnabled = "true"，  默认为true，不需要设置
- 2. 在映射文件中设置标签 <cache/>
- 3. 二级缓存必须在SqlSessiont关闭或提交后有效
- 4. 查询的数据锁转换的实体类必须实现序列化的接口

# 二级缓存失效：
- 两次查询期间执行了任意的增删改，会使一级缓存和二级缓存同时失效
```

## 3. 区别

- 查询数据，会先保存在一级缓存。关闭SqlSession后，数据会再次保存在二级缓存中
- 获取数据时候，先从二级缓存中获取，如果没有，再去一级缓存中获取，如果还是没有，则查询数据库

