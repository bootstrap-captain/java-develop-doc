# Jackson

## 1. 依赖

```xml
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>2.13.4</version>
</dependency>
```

![image-20220915140004289](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20220915140004289.png)

## 2. Java->Json

```bash
# 将一个java对象，转换为对应的json
com.fasterxml.jackson.databind.ObjectMapper
public String writeValueAsString(Object value) throws JsonProcessingException
  
# 如果java对象中某个属性没有赋值， 则转换的json也是对应的默认 '0' 值
```

```java
package com.erick.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class Student {
    private String name;
    private Integer age;

    /*数组和List可以理解为同一个格式*/
    private List<String> hobby;
    private String[] fruits;

    /**
     * com.fasterxml.jackson.annotation.JsonFormat
     * 必须要指定日期格式
     * 指定日期的格式
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date birthday;
}
```

```java
     @Test
    public void pojoToJson() {
        ObjectMapper mapper = new ObjectMapper();
        List<String> hobby = new ArrayList<>();
        hobby.add("swim");
        hobby.add("dance");

        String[] fruits = {"apple", "peach", "melon"};


        Student student = new Student();
        student.setAge(20);
        student.setName("erick");
        student.setFruits(fruits);
        student.setHobby(hobby);

        try {
            String stuJson = mapper.writeValueAsString(student);
            System.out.println(stuJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
```

## 3. Json->Java

```bash
# 将一个json字符串，转换为对应的Java
com.fasterxml.jackson.databind.ObjectMapper
public <T> T readValue(String content, Class<T> valueType)
```

```java
    @Test
    public void jsonToPojo(){
        String jsonMessage = """
                {
                  "name": "erick",
                  "age": 20,
                  "hobby": [
                    "swim",
                    "dance"
                  ],
                  "fruits": [
                    "apple",
                    "peach",
                    "melon"
                  ],
                  "birthday": "2022-09-15 06:26:36"
                }
                """;
        ObjectMapper mapper = new ObjectMapper();
        try {
            /**
             * ObjectMapper支持从byte[]、File、InputStream、字符串等数据的JSON反序列化。
             */
            Student student = mapper.readValue(jsonMessage, Student.class);
            System.out.println(student);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
```

## 4.  注解

```bash
# 用于属性上，进行JSON操作时忽略该属性, 不管是json->java 还 是java->json
@JsonIgnore       

# 此注解用于属性上，把该属性的名称序列化为另外一个名称，别名映射
@JsonProperty    

# 用于属性上，把Date类型直接转化为想要的格式，如@JsonFormat(pattern = "yyyy-MM-dd HH-mm-ss")
@JsonFormat  

# 用于类上，默认为false， 该为true， 如果json里面的字段，pojo里面没有，就直接忽略该字段
@JsonIgnoreProperties(ignoreUnknown = true)
```

```java
package com.erick.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class Student {
    @JsonIgnore
    private String name;

    @JsonProperty("erickAge")
    private Integer age;

    /*数组和List可以理解为同一个格式*/
    private List<String> hobby;
    private String[] fruits;

    /**
     * com.fasterxml.jackson.annotation.JsonFormat
     * 必须要指定日期格式
     * 指定日期的格式
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date birthday;
}

```

