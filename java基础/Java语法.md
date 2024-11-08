## 互相转换

```java
package d01;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Demo01 {


    @Test
    public void test00() {
        List<String> list = List.of("hello");
        /*转换后是Object*/
        Object[] array = list.toArray();
    }

    /*指定类型，Set同理*/
    @Test
    public void test01() {
        List<String> list = List.of("hello");
        /*传入一个长度为0的数组:
         * 1. 数组长度>集合长度，则数组不变
         * 2. 数组长度<集合长度，则创建一个新的数组*/
        String[] arr = new String[0];
        System.out.println(arr);
        String[] rest = list.toArray(arr);
        System.out.println(rest);
    }


    /*Map转Array*/
    @Test
    public void test03() {
        Map<String, String> map = new HashMap<>();
        map.put("first", "huawei");
        map.put("second", "xiaomi");
        Set<Map.Entry<String, String>> entries = map.entrySet();

        Map.Entry<String, String>[] arr = new Map.Entry[1];
        Map.Entry<String, String>[] array = entries.toArray(arr);
        System.out.println(array);
    }
}

```

# Stream流

## 不可变集合

- 不是集合的引用不可变，是集合中的内容不可变，readonly

```java
package d01;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Demo02 {

    /*List的*/
    @Test
    public void test01() {
        /*会根据element的数据类型，自动推断*/
        List<String> phone = List.of("apple", "huawei", "xiaomi");
        // java.util.ImmutableCollections$ListN
        System.out.println(phone.getClass());
    }

    /*Set的*/
    @Test
    public void test02() {
        /*Set的参数：必须不能重复，否则运行时报错*/
        Set<Integer> number = Set.of(1, 2, 3);
        // java.util.ImmutableCollections$SetN
        System.out.println(number.getClass());
    }

    @Test
    public void test03() {
        /*Map的参数：key不能重复，k-v对最多不能超过10个*/
        Map<String, String> map = Map.of("first", "apple", "second", "hauwei", "third", "xiaomi");
        // java.util.ImmutableCollections$MapN
        System.out.println(map.getClass());
    }

    @Test
    public void test04() {
        Map<String,String> ori = new HashMap<>();
        ori.put("first","huawei");

        /*Map的参数*/
        Map<String, String> map = Map.copyOf(ori);
        // class java.util.ImmutableCollections$Map1
        System.out.println(map.getClass());
    }
}
```

## 构建

```java
package d01;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Stream;

public class Demo03 {

    /*单列集合*/
    @Test
    public void test01() {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, "hello", "world");

        list.stream().forEach(element -> System.out.println(element));
    }

    /*多列集合*/
    @Test
    public void test02() {
        Map<String, String> map = new HashMap<>();
        map.put("first", "xiaomi");
        map.put("second", "huawei");

        map.keySet().stream().forEach(key -> System.out.println(key));

        System.out.println("=======");

        map.entrySet().stream().forEach(entry -> System.out.println(entry));
    }

    /*零散数据：必须是同一种引用数据类型*/
    @Test
    public void test03() {
        Stream.of("hello", "word").forEach(element -> System.out.println(element));
    }

    /*数组*/
    @Test
    public void test04() {
        String[] arr = {"hello", "world"};
        Arrays.stream(arr).forEach(element -> System.out.println(element));
    }
}
```

## 中间方法

### 1. filter

```java
package d01;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Demo04 {

    private List<String> raw = new ArrayList<>();

    @BeforeEach
    public void init() {
        Collections.addAll(raw, "张无忌", "周芷若", "赵敏", "张强", "张三丰", "张翠山", "张良");
    }

    /*1. 为true的留下，为false则过滤掉
     * 2. 可以在一个filter中设置多个条件判断*/
    @Test
    public void test01() {
        raw.stream()
                .filter(element -> element.startsWith("张") && element.length() == 2)
                .forEach(element -> System.out.println(element));
    }

    /*可以进行多次的filter*/
    @Test
    public void test02() {
        raw.stream()
                .filter(element -> element.startsWith("张"))
                .filter(element -> element.length() == 2)
                .forEach(element -> System.out.println(element));
    }

    @Test
    public void test03() {
        raw.stream()
                .filter(element -> isValidDate(element))
                .forEach(element -> System.out.println(element));
    }

    private boolean isValidDate(String element) {
        if (element.startsWith("张") && element.length() == 2) {
            return true;
        }
        return false;
    }
}
```

### 2. skip/limit/distinct

- distinct依赖于hashcode和equals方法

```java
package d01;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Demo05 {
    private List<String> raw = new ArrayList<>();

    @BeforeEach
    public void init() {
        Collections.addAll(raw, "张无忌","张无忌", "周芷若", "赵敏", "张强", "张三丰", "张翠山", "张良");
    }


    @Test
    public void test01() {
        raw.stream()
                .distinct()
                .skip(2)
                .limit(3)
                .forEach(elment -> System.out.println(elment));
    }
}
```

### 3. contact

- 合并两个流为一个流

```java
package d01;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Demo05 {
    private List<String> first = new ArrayList<>();
    private List<String> second = new ArrayList<>();

    @BeforeEach
    public void init() {
        Collections.addAll(first, "张无忌", "张无忌", "周芷若");
        Collections.addAll(second, "赵敏", "张强", "张三丰", "张翠山", "张良");
    }


    @Test
    public void test01() {
        Stream.concat(first.stream(), second.stream())
                .forEach(element -> System.out.println(element));
    }
}
```

### 4. map

- 拿到数据后，对其中的数据做定制化处理

```java
package d01;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Demo06 {
    private List<String> raw = new ArrayList<>();


    @BeforeEach
    public void init() {
        Collections.addAll(raw, "张无忌-12", "周芷若-22", "张强-33", "张三丰-44", "张翠山-55", "张良-99");
    }


    /*单纯处理数据*/
    @Test
    public void test01() {
        List<Integer> res = raw.stream().map(element -> {
            String[] split = element.split("-");
            return Integer.parseInt(split[1]);
        }).toList();
        System.out.println(res);
    }

    /*可以讲数据处理成对应的对象*/
    @Test
    public void test02() {
        List<Person> list = raw.stream().map(element -> {
            String[] split = element.split("-");
            return new Person(split[0], Integer.parseInt(split[1]));
        }).toList();
        System.out.println(list);
    }
}

@Data
@AllArgsConstructor
class Person {
    private String name;
    private int age;
}
```



## 终结方法

### 1. 基本使用

```java
package d01;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Demo05 {
    private List<String> raw = new ArrayList<>();


    @BeforeEach
    public void init() {
        Collections.addAll(raw, "张无忌-12", "周芷若-22", "张强-33", "张三丰-44", "张翠山-55", "张良-99");
    }


    /*foreach终结*/
    @Test
    public void test01() {
        raw.stream().forEach(element -> System.out.println(element));
    }

    /*count()*/
    @Test
    public void test02() {
        long count = raw.stream().count();
        System.out.println(count);
    }

    /*toArray()*/
    @Test
    public void test03() {
        /*object类型*/
        Object[] array = raw.stream().toArray();

        String[] arr = raw.stream().toArray(element -> new String[element]);
    }
}
```

### 2. 转集合

```java
package d01;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

public class Demo05 {
    private final List<String> raw = new ArrayList<>();


    @BeforeEach
    public void init() {
        Collections.addAll(raw, "张无忌-12", "周芷若-22", "张强-33", "张三丰-44", "张翠山-55", "张良-99");
    }

    /*List*/
    @Test
    public void test01() {
        List<String> res = raw.stream().collect(Collectors.toList());
        System.out.println(res);
    }

    /*Set: 会去重*/
    @Test
    public void test02() {
        Set<String> res = raw.stream().collect(Collectors.toSet());
        System.out.println(res);
    }

    /*Map*/
    @Test
    public void test03() {
        Map<String, String> res = raw.stream()
                .collect(Collectors.toMap(
                        /*表达式一：key的生成规则*/
                        element -> element.split("-")[0],
                        /*表达式二：value的生成规则*/
                        element -> element.split("-")[1]));
        System.out.println(res);
    }
}
```

## 注意事项

```java
package d01;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Demo05 {
    private final List<String> raw = new ArrayList<>();


    @BeforeEach
    public void init() {
        Collections.addAll(raw, "张无忌-12", "周芷若-22", "张强-33", "张三丰-44", "张翠山-55", "张良-99");
    }

    /*1. stream流在处理数据的时候，不会改变原来的数据，而是得到新的数据*/
    @Test
    public void test01() {
        List<String> result = raw.stream().filter(element -> element.startsWith("张")).toList();
        System.out.println(result);
        System.out.println(raw);
    }

    /*2. 流调用终结方法后，就不能再次使用了*/
    @Test
    public void test02() {
        Stream<String> stream = raw.stream();
        List<String> list = stream.toList();

        // java.lang.IllegalStateException: stream has already been operated upon or closed
        // 上面步骤中，流已经关闭了
        List<String> res = stream.filter(element -> element.startsWith("张")).toList();
    }

}
```

## DEBUG

![image-20241108153803148](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20241108153803148.png)

![image-20241108153859347](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20241108153859347.png)