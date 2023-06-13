# File 类

## 1. 创建文件

-  File 包含文件( File)和目录(Directory)

```bash
# 1. 类定义： public class File implements Serializable, Comparable<File>

# 2. 获取文件对象
# 2.1 如果文件/目录在磁盘存在，则得到该文件或者目录的File对象
# 2.2 如果文件/目录在磁盘不存在，则可以调用createFile或者mkdir在磁盘中创建对应的文件
public File(String pathname);                 # 根据文件绝对路径获取文件  
public File(String parent, String child);     # 根据绝对路径的父目录名，文件名创建文件
public File(File parent, String child);       # 根据绝对路径的父File， 文件名创建文件

# 3. 在磁盘中创建文件(不是目录)
#    如果File在磁盘中不存在，可以调用createNewFile来创建
#    只是创建具体的文件，需要保证绝对路径下的对应的directory必须存在
public boolean createNewFile() throws IOException;
```

```java
package com.erick.basicio;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class Test01 {

    String prefix = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/";

    @Test
    void test01() throws IOException {
        /*在指定目录下，创建一个名字为 lucy的无后缀的文件*/
        File file1 = new File(prefix + "src/main/resources/erick/lucy");
        file1.createNewFile();

        File file2 = new File(prefix + "src/main/resources/erick/1.txt");
        file2.createNewFile();

        /*如果xxx目录不存在，则创建时候： java.io.IOException: No such file or directory*/
        File file3 = new File(prefix + "src/main/resources/xxx/2.txt");
        file3.createNewFile();
    }

    @Test
    void test02() throws IOException {
        /*父路径： directory，  子路径： 文件名*/
        File file1 = new File(prefix + "src/main/resources/erick", "test02.txt");
        file1.createNewFile();

        /*父路径： directory，  子路径： directory/文件名*/
        File file2 = new File(prefix + "src/main", "resources/erick/erick.txt");
        file2.createNewFile();

        /*文件路径错误，就会报错*/
        File file3 = new File(prefix + "src/main/resources/xxxx", "er.txt");
        file3.createNewFile();
    }

    @Test
    void test03() throws IOException {
        String parentPath01 = prefix;
        File parentFile01 = new File(parentPath01);
        File file = new File(parentFile01, "src/main/resources/haha.txt");
        file.createNewFile();
    }
}
```

## 2. 查看文件信息

```bash
public String getName();
public String getAbsolutePath();
public String getParent();
public long length();
public boolean isFile();
public boolean isDirectory();
```

## 3. 删除操作

```bash
public boolean exists();
public boolean delete();
public boolean mkdirs(); # 多级目录
public boolean mkdir();  # 一级目录
```

```java
public class Demo02 {

    @Test
    public void test01(){
        String filePath = "/Users/EShu/Documents/based-on-java8/io-feature/src/main/resources/1.txt";
        File file = new File(filePath);
        deleteIfExist(file);
    }

    @Test
    public void test02(){
        String dirPath = "/Users/EShu/Documents/based-on-java8/io-feature/src/main/resources/haha";
        File file = new File(dirPath);
        deleteIfExist(file);
    }

    private void deleteIfExist(File file){
        if (!file.exists()){
            System.out.println("directory is not existing");
            return;
        }

        if (file.delete()){
            System.out.println("directory is deleted");
        }else{
            System.out.println("directory is not deleted");
        }
    }

    /*目录是否存在，存在就提示存在，不存在就新建*/
    @Test
    public void test03(){
        String dirPath = "/Users/EShu/Documents/based-on-java8/io-feature/src/main/resources/haha/nice";
        File file = new File(dirPath);
        if (file.exists()){
            System.out.println("directory already existed");
            return;
        }

        if (file.mkdirs()){
            System.out.println("create multi directory");
        }else{
            System.out.println("create multi directory failed");
        }
    }
}
```

# IO流

## 1. 基本介绍

- 输入输出，指的是对Java程序而言
- 流：本质是一种**管道**，将文件数据通过流来传输到Java内存

![image-20230409104510333](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230409104510333.png)

## 2. 基本分类

![image-20230409114801126](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230409114801126.png)

## 3. 节点流

- 可以从一个特定的数据源读写数据, 比如文件，比如数组
- 底层流，直接和数据源相连

![image-20230409160606369](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230409160606369.png)

## 4. 包装流

![image-20230409162626401](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230409162626401.png)

```bash
# 修饰器模式
- Buffered***包含了对应的父类的接口，从而可以消除不同节点流的实现差异，可以包含不同的节点流
  比如  BufferedReader 类内部包含成员属性  private Reader in;
  
- 不会直接与数据源链接，而是使用包含的节点流进行IO
- 增加了缓冲的方式，提高了输入输出的效率
- 提供更方便的方法来处理输入输出
```

# 节点流

## 1. 文件流

### 1.1 字节IO

#### FileInputStream

```bash
# 1. 构造方法
public FileInputStream(String name) throws FileNotFoundException;
public FileInputStream(File file) throws FileNotFoundException;

# 2. 一次读取一个字节，如果读取到最后则为-1
#   2.1 读取到的数据是对应的char的int值
#   2.2 需要将int转换为char
#.  2.3 文本中如果有中文，则会乱码*/
public int read() throws IOException;

# 3. 一次读取多个字节，并将结果放在一个byte[]数组中去，效率高
#   3.1 返回结果为实际读取的字节个数， 如果读取到最后则为-1
#   3.2 文本中如果有中文，不会乱码*/
public int read(byte b[]) throws IOException;
```

```java
package com.erick.basicio;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Test03 {

    String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/1.txt";

    @Test
    void test01() throws IOException {
        InputStream is = new FileInputStream(filePath);
        while (true) {
            int result = is.read();
            if (result == -1) {
                break;
            }
            System.out.println((char) result);
        }
        is.close();
    }

    @Test
    void test02() throws IOException {
        InputStream is = new FileInputStream(new File(filePath));
        byte[] buffer = new byte[1024];
        while (true) {
            int length = is.read(buffer);
            if (length <= 0) {
                break;
            }
            String result = new String(buffer, 0, length);
            System.out.println(result);
        }
        is.close();
    }
}
```

#### FileOutputStream

```bash
# 1. 构造方法： 覆盖写(false,default)，可追加写(true)
public FileOutputStream(String name) throws FileNotFoundException;
public FileOutputStream(String name, boolean append) throws FileNotFoundException;
public FileOutputStream(File file) throws FileNotFoundException;
public FileOutputStream(File file, boolean append) throws FileNotFoundException;

#  2. 一次写入一个字符,如'h'
public void write(int b) throws IOException
#     一次写入一个字节数组
public void write(byte b[]) throws IOException
#    一次写入一个字节数组的一部分
public void write(byte b[], int off, int len) throws IOException
```

```java
package com.erick.basicio;

import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Test04 {

    String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/erick.txt";

    @Test
    void test01() throws IOException {
        OutputStream os = new FileOutputStream(filePath);
        os.write('h');
        os.write("hello".getBytes(StandardCharsets.UTF_8));
        os.write("erick".getBytes(StandardCharsets.UTF_8), 0, 2);
        os.close();
    }
}
```

#### 图片拷贝

- 图片，音频等二进制，必须用字节流来进行拷贝

```java
package com.erick.basicio;

import org.junit.jupiter.api.Test;

import java.io.*;

public class Test05 {

    String filePrefix = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/";
    String filePath = filePrefix + "src/main/resources/erick/1.jpg";
    String destFilePath = filePrefix + "src/main/resources/erick/2.jpg";

    @Test
    void test01() throws IOException {
        InputStream is = new FileInputStream(filePath);
        OutputStream os = new FileOutputStream(destFilePath);

        byte[] buffer = new byte[1024];
        while (true) {
            int length = is.read(buffer);
            if (length < 0) {
                break;
            }
            os.write(buffer, 0, length);
        }
        os.close();
        is.close();
    }
}
```

### 1.2 字符IO

- 只能进行文本类的处理

#### FileReader

```bash
# 构造方法
public FileReader(String fileName) throws FileNotFoundException;
public FileReader(File file) throws FileNotFoundException;

# 一次读一个字符，可以中文，不会乱码
public int read() throws IOException;
# 一次读取一个字符数组
public int read(char cbuf[]) throws IOException;
```

```java
package com.erick.basicio;

import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class Test06 {
    String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/1.txt";

    @Test
    void test01() throws IOException {
        Reader reader = new FileReader(filePath);
        while (true) {
            int result = reader.read();
            if (result == -1) {
                break;
            }
            System.out.println((char) result);
        }
        reader.close();
    }

    @Test
    void test02() throws IOException {
        Reader reader = new FileReader(filePath);
        char[] buffer = new char[1024];
        while (true) {
            int length = reader.read(buffer); // 向数组中读取
            if (length < 0) {
                break;
            }
            String result = new String(buffer,0,length);
            System.out.println(result);
        }
        reader.close();
    }
}
```

#### FileWriter

```bash
# 构造方法
public FileWriter(String fileName) throws IOException;
public FileWriter(String fileName, boolean append) throws IOException;
public FileWriter(File file) throws IOException;
public FileWriter(File file, boolean append) throws IOException;

# 写方法
public void write(String str) throws IOException;
public void write(String str, int off, int len) throws IOException;
public void write(char cbuf[]) throws IOException;
public void write(int c) throws IOException;
```

```java
package com.erick.basicio;

import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Test07 {

    String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/1.txt";

    @Test
    void test01() throws IOException {
        Writer os = new FileWriter(filePath);
        os.write('c');
        os.write("蜀汉o");
        os.write("erick", 0, 3);
        /*close后数据才会写出去， flush和close效果等价*/
        os.flush();
        os.close();
    }
}
```

#### 文本拷贝

- 文本的复制，可以用字符流或者字节流

```java
package com.erick.basicio;

import org.junit.jupiter.api.Test;

import java.io.*;

public class Test08 {

    String srcFile = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/1.txt";
    String destFile = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/2.txt";

    @Test
    void test01() throws IOException {
        Reader reader = new FileReader(srcFile);
        Writer writer = new FileWriter(destFile);

        char[] buffer = new char[1024];
        while (true) {
            int length = reader.read(buffer);
            if (length < 0) {
                break;
            }
            writer.write(buffer, 0, length);
        }
        writer.close();
        reader.close();
    }
}
```

# 包装流

- 关闭时，只需要关闭包装流即可，底层自动会关闭节点流
- 写文件时候的append属性，在对应的字节流中控制

## 1. 文件包转流

### 1.1 字节IO

#### BufferedInputStream

```bash
# BufferedInputStream
# 1. 构造方法
public BufferedInputStream(InputStream in);

# 2. 读取
# 2.1 一次读取一个字节,如果有中文则会乱码
public synchronized int read() throws IOException    

# 2.2 一次读取数据到字节数组中，中文不会乱码， 返回结果为本次读取到的长度
public int read(byte b[]) throws IOException
```

```java
package com.erick.basicio;

import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Test09 {

    String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/1.txt";

    /*一次读取一个字节*/
    @Test
    void test01() throws IOException {
        InputStream is = new FileInputStream(filePath);
        BufferedInputStream bis = new BufferedInputStream(is);
        while (true) {
            int result = bis.read();
            if (result == -1) {
                break;
            }
            System.out.println((char) result);
        }
        bis.close();
    }

    /*一次读取数据到字节数组中*/
    @Test
    void test02() throws IOException {
        InputStream is = new FileInputStream(filePath);
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] buffer = new byte[1024];
        while (true) {
            int length = bis.read(buffer);
            if (length == -1) {
                break;
            }
            System.out.println(new String(buffer, 0, length));
        }
        bis.close();
    }
}
```

#### BufferedOutputStream

```bash
# 1. 构造方法, 可追加写的append，依赖于传入的OutputStream的具体实现类的构造
public BufferedOutputStream(OutputStream out);

# 2. 写方法
public synchronized void write(int b) throws IOException;
public void write(byte b[]) throws IOException;
public synchronized void write(byte b[], int off, int len) throws IOException;
```

```java
package com.erick.two;

import org.junit.jupiter.api.Test;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Test01 {
    String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/test.txt";

    @Test
    void test01() throws IOException {
        OutputStream os = new FileOutputStream(filePath);
        BufferedOutputStream bos = new BufferedOutputStream(os);
        bos.write('c');
        bos.write("Hello".getBytes(StandardCharsets.UTF_8));
        bos.write("erick".getBytes(StandardCharsets.UTF_8), 0, 2);
        bos.close();
    }
}
```

#### 图片复制

```java
package com.erick.two;

import org.junit.jupiter.api.Test;

import java.io.*;

public class Test02 {
    String prefix = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/";

    String srcFile = prefix + "src/main/resources/erick/1.jpg";

    String destFile = prefix + "src/main/resources/erick/2.jpg";

    @Test
    void test01() throws IOException {
        InputStream is = new FileInputStream(srcFile);
        BufferedInputStream bis = new BufferedInputStream(is);
        OutputStream os = new FileOutputStream(destFile);
        BufferedOutputStream bos = new BufferedOutputStream(os);

        byte[] buffer = new byte[1024];
        while (true) {
            int length = bis.read(buffer);
            if (length < 0) {
                break;
            }
            bos.write(buffer, 0, length);
        }

        bos.close();
        bis.close();
    }
}
```

### 1.2 字符IO

#### BufferedReader

```bash
# 1. 构造方法
public BufferedReader(Reader in);

# 2. 读取方法
# 2.1 读取一个char，可以有中文, 返回读取的char对应的int,读取不到则是-1
public int read() throws IOException;
# 2.2 读取一个char数组,返回读取的char数组的长度
public int read(char[] cbuf) throws IOException;
# 2.3 读取一行，返回读取的数据, 没有数据则是null
public String readLine() throws IOException；
```

```java
package com.erick.two;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class Test03 {

    String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/1.txt";

    /*一次读取一个char*/
    @Test
    void test01() throws IOException {
        Reader reader = new FileReader(filePath);
        BufferedReader br = new BufferedReader(reader);
        while (true) {
            int result = br.read();
            if (result == -1) {
                break;
            }
            System.out.println((char) result);
        }
        br.close();
    }

    /*一次读取一个char数组*/
    @Test
    void test02() throws IOException {
        Reader reader = new FileReader(filePath);
        BufferedReader br = new BufferedReader(reader);
        char[] buffer = new char[1024];
        while (true) {
            int length = br.read(buffer);
            if (length < 0) {
                break;
            }
            System.out.println(new String(buffer, 0, length));
        }
        br.close();
    }

    @Test
    void test03() throws IOException {
        Reader reader = new FileReader(filePath);
        BufferedReader br = new BufferedReader(reader);
        while (true) {
            String result = br.readLine();
            if (result == null) {
                break;
            }
            System.out.println(result);
        }
        br.close();
    }
}
```

#### BufferedWriter

```bash
# 1. 构造方法
public BufferedWriter(Writer out);

# 2. 写方法
public void write(int c) throws IOException;
public void write(String str) throws IOException;
public void write(char cbuf[], int off, int len) throws IOException;
public void newLine() throws IOException;    # 插入一个换行符
```

```java
package com.erick.two;

import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class Test04 {
    String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/2.txt";

    @Test
    void test01() throws IOException {
        Writer writer = new FileWriter(filePath);
        BufferedWriter bw = new BufferedWriter(writer);
        bw.write('v');
        bw.write("hello");
        bw.write(new char[]{'f', 'c', 'd'}, 0, 3);
        bw.close();
    }
}
```

#### 文本复制

```java
package com.erick.two;

import org.junit.jupiter.api.Test;

import java.io.*;

public class Test05 {
    String prefix = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/";
    String srcFile = prefix + "src/main/resources/erick/1.txt";
    String destFile = prefix + "src/main/resources/erick/2.txt";

    @Test
    void test01() throws IOException {
        Reader reader = new FileReader(srcFile);
        Writer writer = new FileWriter(destFile);
        BufferedReader br = new BufferedReader(reader);
        BufferedWriter bw = new BufferedWriter(writer);
        while (true) {
            String result = br.readLine();
            if (result == null) {
                break;
            }
            bw.write(result);
            bw.newLine();
        }
        bw.close();
        br.close();
    }
}
```

## 2. 对象处理流

```bash
# 序列化
- 在保存数据到文件、数据库时，不仅保存数据的值，还必须保存数据的类型
- 比如一个java对象的不同 k v

# 反序列化
- 将保存在文件，数据库的数据(值以及数据类型)，重新恢复成对应的对象的过程

#  想要实现对象的序列化，对象必须满足一定的条件
- 实现Serializable接口    # 标记接口，接口不做任何实现
```

### 2.1 基本使用

#### ObjectOutputStream

```bash
# 构造方法
public ObjectOutputStream(OutputStream out) throws IOException;

# 写方法: Integer，Boolean， String, Object等都实现了Seriazable接口
public void writeChar(int val)  throws IOException;
public void writeBoolean(boolean val) throws IOException;
public void writeUTF(String str) throws IOException;
```

```java
package com.erick.two;

import org.junit.jupiter.api.Test;

import java.io.*;

public class Test07 {


    /*文件后缀不重要，序列化后的数据，是按照一定格式保存的*/
    String fileName = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/erick.dat";

    @Test
    void test01() throws IOException {
        OutputStream os = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(os);

        /*每个写入的数据，都实现了Serializable接口*/
        oos.writeChar('c');
        oos.writeBoolean(true);
        oos.writeLong(10);
        oos.writeUTF("hello"); // 写字符串
        oos.writeObject(new Dog("huahua", 12));
        oos.close();
    }
}

class Dog implements Serializable {
    public Dog(String name, int age) {
        this.age = age;
        this.name = name;
    }

    @Override
    public String toString() {
        return "age=" + age + ",name=" + name;
    }

    private String name;
    private int age;

}
```

#### ObjectInputStream

- 读写顺序一定要保持一致

``` bash
# 构造方法
public ObjectInputStream(InputStream in) throws IOException;

# 读方法
public char readChar()  throws IOException;
public boolean readBoolean() throws IOException;
public long readLong()  throws IOException;
public String readUTF() throws IOException;
public final Object readObject();
```

```java
package com.erick.two;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class Test06 {

    String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/erick.dat";

    @Test
    void test01() throws IOException, ClassNotFoundException {
        InputStream inputStream = new FileInputStream(filePath);
        ObjectInputStream ois = new ObjectInputStream(inputStream);
        char c = ois.readChar();
        System.out.println(c);
        boolean b = ois.readBoolean();
        System.out.println(b);
        long l = ois.readLong();
        System.out.println(l);
        String s = ois.readUTF();
        System.out.println(s);
        Object o = ois.readObject();
        System.out.println(o);
        System.out.println(o.getClass());
        ois.close();
    }
}
```

### 2.2 序列化细节

- 读和写的流的读写顺序要保持一致
- 要求实现序列化或者反序列化的对象，必须实现Serializable接口
- 序列化的类中建议添加SerialVersionUID, 提高版本的兼容性

```java
package com.erick.standard.d04;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Dog implements Serializable {

    /*添加该值后，表示版本号码
    *
    * 1. 一个类在序列化的时候，包含2个字段，但是反序列化的时候，假如类中添加了新的字段，就会出现
    *    local class incompatible: stream classdesc serialVersionUID = 5819916523497878209,
    *                              local class serialVersionUID = -2714821408957032839
    *
    * 2. 为了解决该问题，添加serialVersionUID字段，只要写入和读取的时候，该值不变，就会认为是同一个类
    * */
    private static final long serialVersionUID = 42L;

    /*序列化时候
     * 1. 默认将所有的字段都会进行序列化
     * 2. static 和 transient 修饰的成员，不会进行序列化*/

    private String name;
    private int age;

    private String color;

    /*在序列化的时候，并没有保存该属性*/
    transient String address;
    
    /*序列化的时候，要求里面属性的类型也是需要实现序列化*/
    
    
    /*序列化具有可继承性，如果某个类已经实现了序列化，那么它的所有的子类也已经默认实现了序列化*/
}
```

## 3. 标准IO流

- 连接键盘和显示器

```bash
# 字节流             
 System.in           InputStream              标准输入       键盘
 System.out          PrintStream              标准输出       显示器
```

```java
package com.erick.standard.d05;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class StandardStreamDemo {
    public static void main(String[] args) {
        method02();
    }

    private static void method01() {
        /*1.   编译类型：InputStream， 从键盘获取
         *    运行类型：class java.io.BufferedInputStream  */
        InputStream in = System.in;
        System.out.println(System.in.getClass());


        /*2. 编译类型：PrintStream， 向显示器输出
         *    运行类型：class java.io.PrintStream */
        PrintStream out = System.out;
        System.out.println(System.out.getClass());
    }


    /* public Scanner(InputStream source)  */
    private static void method02() {
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入内容");
        String input = sc.next();
        System.out.println(input);
    }
}
```

# Properties

- 处理 xxx.properties文件

```bash
# 类信息
public class Properties extends Hashtable<Object,Object>

# 常见方法
# 1. 加载一个文件的流
public synchronized void load(InputStream inStream) throws IOException;

# 2. 获取一个k的v
public String getProperty(String key);
public String getProperty(String key, String defaultValue)         # 如果没有，则返回一个默认值

# 3. 设置一个k的v，可以覆盖修改, 返回原来的值
public synchronized Object setProperty(String key, String value);
public void store(OutputStream out, String comments) throws IOException    # 将properties的属性添加到文件中
```

# IO模型

- 用什么样的通道(通信模式和架构)进行数据的传输和接收
- 很大程度上决定了程序通信的性能
- java共支持三种网络编程的IO模型：BIO, NIO, AIO
- 实际通信需求下，根据不同的业务场景和性能需求，选择不同的IO模型

# BIO

## 1. 简单介绍

- Blocking IO, 同步并阻塞，传统阻塞型
- 一个链接，一个线程，客户端有连接请求时，服务器端就启动一个线程进行处理
- 如果这个链接不做任何事情，Server端需要持续等待，会造成不必要的线程开销

```bash
# 适用场景
- 连接数比较小，且固定的架构
- 对服务器资源要求比较高，并发局限于应用中
- JDK1.4以前的唯一选择，但程序简单易理解
```

![image-20230405220004290](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230405220004290.png)

## 2. 案列演示

### 2.1 单次对话

- 客户端向服务端发送一次数据

**Server端**

```java
package com.erick.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        System.out.println("服务端启动");

        try {
            //1. 指定端口，创建一个服务端, 如果不写ip，则默认是本机的ip
            ServerSocket serverSocket = new ServerSocket(9999);
            // 2. 监听注册到该Server端的客户端的Socket
            Socket socket = serverSocket.accept();
            // 3. 从socket中获取字节输入流
            InputStream inputStream = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String msg;
            if ((msg = br.readLine()) != null) {
                System.out.println("服务端接收数据：" + msg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

**Client端**

```java
package com.erick.bio;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        // 1. 创建Socket对象请求服务端的连接
        Socket clientSocket = new Socket("127.0.0.1",9999);
        // 2. 从Socket中获取字节输入流
        OutputStream outputStream = clientSocket.getOutputStream();
        PrintStream ps = new PrintStream(outputStream);
        ps.println("hello server");
        ps.flush();
    }
}
```

### 2.2 多发/多收

- 客户端多次向服务端发送消息

**Server端**

```java
package com.erick.bio.two;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        System.out.println("服务端启动");

        try {
            //1. 指定端口，创建一个服务端, 如果不写ip，则默认是本机的ip
            ServerSocket serverSocket = new ServerSocket(9999);
            // 2. 监听注册到该Server端的客户端的Socket
            Socket socket = serverSocket.accept();
            // 3. 从socket中获取字节输入流
            InputStream inputStream = socket.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String msg;

            while ((msg = br.readLine()) != null){
                System.out.println("服务端接收数据：" + msg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

**Client端**

```java
package com.erick.bio.two;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        // 1. 创建Socket对象请求服务端的连接
        Socket clientSocket = new Socket("127.0.0.1", 9999);
        // 2. 从Socket中获取字节输入流
        OutputStream outputStream = clientSocket.getOutputStream();
        PrintStream ps = new PrintStream(outputStream);

        // 3. 不断发送
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("请说：");
            String msg = sc.nextLine();
            ps.println(msg);
            ps.flush();
        }
    }
}
```

### 2.3  多Client

- 一个服务端通过main来启动主线程的时候，只能接受一个客户端的通信请求。即一个Socket。
- 若服务端需要处理多个客户端的消息通信请求，需要在服务端引入多个线程
- 客户端每发起一个请求，服务端就创建一个新的线程来处理这个客户端的请求
- 一个客户端对应一个线程

**Server**

```java
package com.erick.bio.three;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        try {
            // 1.注册服务端
            ServerSocket serverSocket = new ServerSocket(9999);
            // 2. 每次接受一个通信请求Socket，就创建一个Thread来进行处理
            while (true) {
                Socket socket = serverSocket.accept();
                new ServerThread(socket).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class ServerThread extends Thread {
    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String msg;
            while ((msg = br.readLine()) != null) {
                System.out.println("服务端" + Thread.currentThread().getId() + ": " + msg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
```

**Client**

- 启动多个Client

``` java
package com.erick.bio.three;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        // 1. 创建Socket对象请求服务端的连接
        Socket clientSocket = new Socket("127.0.0.1", 9999);
        // 2. 从Socket中获取字节输入流
        OutputStream outputStream = clientSocket.getOutputStream();
        PrintStream ps = new PrintStream(outputStream);

        // 3. 不断发送
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("请说：");
            String msg = sc.nextLine();
            ps.println(msg);
            ps.flush();
        }
    }
}
```

## 3. BIO的劣势

- 服务端的每个Socket接收到通信请求，都会创建一个线程。服务端线程的竞争和切换上下文会影响性能
- 并不是每个Socket都会进行IO操作，无意义的线程处理，浪费了服务端的资源
- 客户端并发访问增加时，服务端将出现1:1的线程开销
- 访问量越大，系统将发生线程栈溢出，线程创建失败，最终导致进程宕机或者僵死，从而不能对外提供服务

```bash
# 解决方案
- 上述的问题，可以通过伪异步编程来进行解决

# 添加线程池和任务队列的方式
- 通过JDK提供的线程池，将客户端的Socket请求封装称任务，交给线程池来处理
- 线程池来保证服务端不会发生宕机
- 但是并不会改变BIO这种1对1的Socket通信请求
```

## 4. 文件上传

- 客户端从本地磁盘中挑选文件，将文件上传发送给服务端
- 服务端接收文件，将文件保存在磁盘

**Server**

```java
package com.erick.bio.four;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class Server {
    public static void main(String[] args) {
        //1. 注册服务端
        try {
            ServerSocket serverSocket = new ServerSocket(8888);
            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();
            // 2. 读取数据
            DataInputStream ds = new DataInputStream(inputStream);

            // 3. 客户端发多少，服务端就要接受多少
            String suffix = ds.readUTF();

            OutputStream os = new FileOutputStream("/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/" +
                    UUID.randomUUID() + suffix);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = ds.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

**Client**

```java
package com.erick.bio.four;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        // 1. 请求与服务端的Socket连接
        Socket clientSocket = new Socket("127.0.0.1", 8888);
        // 2. 将文件数据进行发送
        DataOutputStream ds = new DataOutputStream(clientSocket.getOutputStream());
        // 3.1 发送文件后缀给服务端
        ds.writeUTF(".jpg");
        // 3.2 发送文件的具体数据
        InputStream is = new FileInputStream("/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/src.jpg");
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) > 0) {
            ds.write(buffer, 0, len);
        }
        ds.flush();
    }
}
```

## 5. 端口转发

- 一个客户端的消息，可以发送给所有的客户端去接受(群聊实现)

![image-20230407175039059](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230407175039059.png)

**Server**

- 注册端口
- 接收 客户端的Socket连接，交给一个独立的线程来处理
- 把当前连接的客户端Socket存入到一个所谓的在线socket集合中保存
- 接收客户端的消息，然后推送给当前所有在线的socket接收

```java
package com.erick.bio.five;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public static List<Socket> allOnlineSockets = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(9090);
            while (true) {
                Socket socket = serverSocket.accept();
                // 将获取到的socket添加到在线集合中维护
                allOnlineSockets.add(socket);
                new ServerThread(socket).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class ServerThread extends Thread {
    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        try {
            inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String msg;
            while ((msg = bufferedReader.readLine()) != null) {
                System.out.println("server端收到并转发：" + msg);
                sendMsgToOtherClient(msg);
            }
        } catch (IOException e) {
            System.out.println("当前已经有人下线了");
            Server.allOnlineSockets.remove(socket);
            throw new RuntimeException(e);
        }
    }

    private void sendMsgToOtherClient(String msg) {
        for (Socket socket : Server.allOnlineSockets) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                PrintStream ps = new PrintStream(outputStream);
                ps.println(msg);
                ps.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
```

**Client**

```java
package com.erick.bio.five;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        // 1. 创建Socket对象请求服务端的连接
        Socket clientSocket = new Socket("127.0.0.1", 9090);

        // 1.1 启动一个线程，来进行数据的接收
        new ClientThread(clientSocket).start();

        // 2. 从Socket中获取字节输入流
        OutputStream outputStream = clientSocket.getOutputStream();
        PrintStream ps = new PrintStream(outputStream);

        // 3. 不断发送
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("请说：");
            String msg = sc.nextLine();
            ps.println(msg);
            ps.flush();
        }
    }
}

class ClientThread extends Thread {
    private Socket clientSocket;

    public ClientThread(Socket socket) {
        clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = clientSocket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String msg;
            while ((msg = bufferedReader.readLine()) != null) {
                System.out.println("客户端：" + msg);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
```

# NIO

## 1. 概述

### 1.1 基本概念

- New IO, Non Blocking IO, 同步非阻塞, JDK1.4引入，可以替代标准的Java IO API
- NIO和BIO具有同样的作用和目的，但使用方式完全不一样
- 面向**缓冲区**，基于**通道**，更加**高效**的方式进行文件的读写操作
- NIO相关的类都在java.nio包及子包下，并且对原java.io包中的很多类进行改写
- 三大核心： Channel(通道)， Buffer(缓冲区)， Selector(选择器)

```bash
# 基本介绍
- 一个线程从某个通道发送请求或者读取数据，但是它仅能得到目前可用的数据

# 非阻塞读
- 如果目前没有数据时，什么也不会获取，而不是保持线程阻塞，直到数据变得可以读取之前，该线程可以继续做其他的事情

# 非阻塞写
- 一个线程请求写入一些数据到某个通道中，但不需要等待它完全写入，这个线程可以去做其他的事情
```

### 1.2 NIO vs BIO

|    NIO     |                BIO                 |
| :--------: | :--------------------------------: |
| 面向Buffer | 面向Stream<br />基于字节流和字符流 |
|   非阻塞   |                阻塞                |
|  Selector  |                                    |

```bash
# Selector
- 一个线程处理多个请求(连接)：客户端发送的连接请求都会注册到多路复用器上
- 多路复用器轮询，连接有IO请求就进行处理

# 适用场景
- 客户端连接比较多，但数据比较小
- 聊天服务器，弹幕系统，服务器间通讯
```

## 2. 工作原理

- 相当于在Server端和Client端加了一个消息中间件Channel

![image-20230408211333534](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230408211333534.png)

## 3. Buffer

### 3.1 基本概念

- 一块内存，被包装成 NIO Buffer并提供了一组方法，用来方便的访问该块内存
- 底层是数组
- 可以读写数据，不同于流
- Channel表示打开到IO设备(文件，套接字)的连接
- Buffer负责存储，Channel负责传输

![image-20230411213325413](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230411213325413.png)

```bash
# Buffer类及子类： Buffer就像一个数组，用来保存多个相同类型的数据
- ByteBuffer    CharBuffer   ShortBuffer  IntBuffer   LongBuffer   FloatBuffer   DoubleBuffer

            
- mark：     mark()指定buffer中一个特别的position
             reset() 恢复到该position
```

### 3.2 常见方法一

```bash
# 1. 分配一个capacity(字节长度)为10的数组的Buffer,创建后不能更改 刚创建出来为写模式
public static ByteBuffer allocate(int capacity);

# 2. 下一个要读取或写入的数据的索引,一开始为0
public final int position();

# 3. 获取buffer的容量，内存块的大小，固定大小
public final int capacity();

# 4. 缓冲区可以操作数据的大小(limit后数据不能进行读写)，不能大于capacity
#             写模式： 为buffer的容量
#             读模式： 等于写入的数据量
public final int limit();

# 5. 写入数据到缓冲区
public abstract ByteBuffer put(byte b);
public final ByteBuffer put(byte[] src);

# 6. 将limit设置为当前位置，并把position移动到0索引处
# 切换到读模式
ByteBuffer flip();
```

![image-20230411215704075](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230411215704075.png)

### 3.3 常见方法二

```bash
# 1. 获取当前position的元素
public abstract byte get();
public ByteBuffer get(byte[] dst);    # 读取数据到一个byte[]数组中

# 2. 在当前索引位置进行标记
ByteBuffer mark();

# 3. 恢复到之前标记的位置
ByteBuffer reset();

# 4. 是否还有元素
public final boolean hasRemaining();

# 5. 返回position与limit之间的元素个数
public final int remaining();

# 6. postion回复到0，并不是清除了数据
ByteBuffer clear();
```

![image-20230411220847090](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230411220847090.png)

### 3.4 直接内存 vs JVM内存

- Buffer包含两种内存类型： 直接内存和非直接内存

```bash
# 直接内存
- 操作系统的内存
- JVM在进行IO时具有更高的性能，因为它直接作用于本地操作系统的IO操作

# 非直接内存
- 堆内存
- 如果要进行IO操作，会先从JVM内存复制到直接内存，再利用本地IO进行处理

# 数据流： 非直接内存读写
-  本地IO-->直接内存-->非直接内存-->直接内存-->本地IO

# 数据流： 直接内存读写
- 本地IO-->直接内存-->本地IO    # 从本地IO读取，然后加载到直接内存， 然后再通过本地IO写出去


# 在做IO处理时，直接内存具有更高的效率，绕过了JVM内存
# 直接内存申请普通的堆内存需要耗费更高的性能
# 数据是存在JVM之外的，因此不会占用应用的内存
# 需要很大的数据需要缓存，并且生命周期又很长，才适合使用直接内存。 一般情况下，如果不能带来明显的性能提升，推荐堆内存
- allocateDircet创建
- isDirect()来判断Buffer是不是直接内存
```

## 4. Channel

### 4.1 基本概念

- 类似流，但又不同于流

|                Channel                 |   Stream   |
| :------------------------------------: | :--------: |
|              同时进行读写              | 只能读或写 |
|                异步读写                |            |
|   从Buffer中读数据，向Buffer中写数据   |            |
| 不能直接访问数据，只能和Buffer进行交互 |            |

```bash
# channel接口
public interface Channel extends Closeable

# 常见实现类
- FileChannel:            读取，写入，映射，操作文件
- DatagramChannel:        通过UDP读写网络中的数据
- SocketChannel:          通过TCP读写网络中的数据
- ServerSocketChannel:    监听新进来的TCP连接，对每一个新进来的连接都会创建一个新的SocketChannel
# ServerChannel类似ServerSocket,   SocketChannel类似Socket
```

### 4.2 FileChannel

#### 常用方法

```bash
# 获取通道的一种方式是：对支持通道的对象调用 getChannel()
- FileInputStream + FileOutputStream
- RandomAccessFile

# FileChannel常见方法
# 1. 从(FileInputStream) 将buffer中数据写入到channel中
public abstract int write(ByteBuffer src) throws IOException;
# 2. 从(FileInputStream) 将多个buffer中数据聚集到channel中
public final long write(ByteBuffer[] srcs) throws IOException;
# 3. 从(FileOutputStream)Channel中读取数据到ByteBuffer
public abstract int read(ByteBuffer dst) throws IOException;
# 4. 从(FileOutputStream) 多个channel中分散数据到ByteBuffer[]
public final long read(ByteBuffer[] dsts) throws IOException;
```

#### 读写文件

```java
package com.erick.three;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class Test02 {


    /*向文件中写入数据*/
    @Test
    void test01() throws IOException {
        String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/1.txt";
        FileOutputStream fos = new FileOutputStream(filePath);
        FileChannel channel = fos.getChannel(); // 只是该实现类具有getChannel()
        ByteBuffer buffer = ByteBuffer.allocate(300);
        buffer.put("helloerick".getBytes(StandardCharsets.UTF_8));
        // 切换到读模式
        buffer.flip();
        int write = channel.write(buffer);
        channel.close();
        fos.close();
    }

    /*读取文件中数据*/
    @Test
    void test02() throws IOException {
        String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/2.txt";
        FileInputStream fis = new FileInputStream(filePath);
        FileChannel channel = fis.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer); // 写入数据
        buffer.flip();
        String result = new String(buffer.array(), 0, buffer.remaining()); // 读取的结果
        System.out.println(result);
    }
}
```

#### 文件复制

- 利用ByteBuffer进行文本，图片，音频的复制

![image-20230412091154111](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230412091154111.png)

```java
package com.erick.three;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Test03 {
    String srcFile = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/1.jpg";

    String destFile = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/2.jpg";

    @Test
    void test() throws IOException {
        FileInputStream fis = new FileInputStream(srcFile);
        FileOutputStream fos = new FileOutputStream(destFile);

        FileChannel inChannel = fis.getChannel();
        FileChannel outChannel = fos.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (true) {
            buffer.clear(); // 先清空缓冲区再写入数据
            int length = inChannel.read(buffer);// 从in的channel中读取数据
            if (length == -1) {
                break;
            }
            buffer.flip(); // 切换到读模式
            outChannel.write(buffer);
        }
        outChannel.close();
        inChannel.close();
    }
}
```

#### 分散/聚集

- 分散读取(Scatter)： 把Channel通道中的数据读入到多个Buffer中
- 聚集写入(Gathering): 将多个Buffer中的数据聚集到Channel中

```java
package com.erick.three;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class Test04 {

    String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/1.txt";

    /*Scatter: 通道包含文件输入/输出通道*/
    @Test
    void test01() throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        FileChannel channel = fis.getChannel();

        ByteBuffer buffer01 = ByteBuffer.allocate(10);
        ByteBuffer buffer02 = ByteBuffer.allocate(10);
        ByteBuffer[] buffers = {buffer01, buffer02};

        channel.read(buffers);
        buffer01.flip();
        buffer02.flip();
        String result01 = new String(buffer01.array(), 0, buffer01.remaining());
        String result02 = new String(buffer02.array(), 0, buffer02.remaining());
        System.out.println(result01);
        System.out.println(result02);
    }

    /*Gather*/
    @Test
    void test02() throws IOException {
        String filePath = "/Users/shuzhan/Documents/workspace-java/BIO-NIO-AIO/src/main/resources/erick/2.txt";
        FileOutputStream fos = new FileOutputStream(filePath);
        FileChannel channel = fos.getChannel();

        ByteBuffer buffer01 = ByteBuffer.allocate(10);
        ByteBuffer buffer02 = ByteBuffer.allocate(10);
        buffer01.put("hello".getBytes(StandardCharsets.UTF_8));
        buffer02.put("erick".getBytes(StandardCharsets.UTF_8));
        buffer01.flip();
        buffer02.flip();

        ByteBuffer[] buffers = {buffer01, buffer02};
        channel.write(buffers);
    }
}
```

## 5.  Selector

- 用非阻塞的方式，用一个线程处理多个客户端连接
- 一个Selector能够检测多个注册的通道上是否有事件发生(多个Channel以事件的方式可以注册到同一个Selector)
- 如果有事件发生，便获取事件然后针对每个事件进行相应的处理
- 只有在连接通道真正有读写事件发生时，才会进行读写，大大减小了系统开销
- 避免了多线程之间的上下文切换导致的开销

![](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230409012645340.png)

### 5.1 Server

```java
package com.erick.five;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server {
    public static void main(String[] args) throws IOException {
        // 1. 获取服务端通道，主要是接收客户端请求
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 2. 切换非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 3. 绑定连接： 提供端口让客户端连接
        serverSocketChannel.bind(new InetSocketAddress(9090));

        // 4. 获取选择器
        Selector selector = Selector.open();
        // 5. 将服务端通道注册到选择器上，并且指定 "监听接收时间"
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);  // 接收事件


        // 6. 轮询获取选择器上已经  "准备就绪" 的事件： 看看Selector上获取事件
        while (selector.select() > 0) {  // 查询是否有事件，有的话结果大于0
            System.out.println("开启一轮循环");
            // 7. 获取选择器中所有注册的事件
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();

            // 8.获取准备的事件
            while (it.hasNext()) {
                // 提取当前事件
                SelectionKey selectionKey = it.next();
                // 9. 判断事件类型
                if (selectionKey.isAcceptable()) {
                    // 10. 客户端接入事件
                    SocketChannel clientChannel = serverSocketChannel.accept();
                    // 11. 切换非阻塞模式
                    clientChannel.configureBlocking(false);
                    // 12. 将该通道注册到选择器上, 并监听该通道的 读 事件
                    clientChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("客户端已经连接");
                } else if (selectionKey.isReadable()) { // 下一轮继续处理的其他事件
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    // 13. 读取数据
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int len = 0;
                    while ((len = socketChannel.read(buffer)) > 0) {
                        buffer.flip();
                        System.out.println("coming message: " + new String(buffer.array(), 0, len));
                        buffer.clear();
                    }
                }
                // 13. 处理完毕后，将当前事件移除掉
                it.remove();
            }
        }
    }
}
```

### 5.2 Client

```java
package com.erick.five;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        // 1. 获取通道
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9090));
        // 2. 切换非阻塞模式
        socketChannel.configureBlocking(false);
        // 3. 分配指定大小的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 4. 发送数据给服务端
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("请说：");
            String msg = scanner.nextLine();
            buffer.put(msg.getBytes(StandardCharsets.UTF_8));
            buffer.flip();
            socketChannel.write(buffer);
            buffer.clear();
        }
    }
} 
```

# NIO群聊

```bash
# 群聊实现
- 客户端与客户端的通信需求(非阻塞)
- 服务端：可以检测用户上线，离线，并实现消息转发
- 客户端：通过Channel可以无阻塞发送消息给其他所有客户端用户，同时可以接受其他客户端通过服务端转发过来的消息
```

## Server

```java
package com.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class Server {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private static final int PORT = 9090;

    /**
     * Server端的初始化
     */
    public void init() {
        try {
            // 1. 获取选择器
            selector = Selector.open();
            // 2. 打开服务端channel
            serverSocketChannel = ServerSocketChannel.open();
            // 3. 服务端绑定端口
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            // 4. 服务端设置非阻塞
            serverSocketChannel.configureBlocking(false);
            // 5. 把通道注册到选择器上, 并监听客户端连接
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 监听的具体动作
     */
    public void listen() {
        try {
            while (selector.select() > 0) {
                System.out.println("开始新一轮监听");
                // 1. 获取选择器中所有注册通道的事件
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                // 2. 遍历所有事件
                while (it.hasNext()) {
                    SelectionKey selectionKey = it.next();
                    if (selectionKey.isAcceptable()) {// 客户端接入请求
                        SocketChannel clientChannel = serverSocketChannel.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ); // 注册客户端，并监听客户端读事件(相对于客户端)
                    } else if (selectionKey.isReadable()) {
                        // 如果是读事件，则需要实现转发逻辑
                        readPresentMsg(selectionKey);
                    }
                    // 3. 处理完毕后，需要移除当前事件，继续下一个事件
                    it.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*接受当前客户端消息，转发给其他客户端*/
    public void readPresentMsg(SelectionKey selectionKey) {
        // 得到当前客户端通道
        SocketChannel presentClientChannel = (SocketChannel) selectionKey.channel();
        // 从通道中接收数据
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            int length = presentClientChannel.read(buffer);
            if (length > 0) {
                buffer.flip();
                String msg = new String(buffer.array(), 0, buffer.remaining());
                System.out.println("接收到客户端消息：" + msg);
                // 发送给其他所有人
                sentToAllClient(msg, presentClientChannel);
            }
        } catch (IOException e) {
            // 如果当前客户端离线，则取消当前事件
            try {
                System.out.println("有人离线了：" + presentClientChannel.getRemoteAddress());
                selectionKey.cancel(); // 取消当前事件
                presentClientChannel.close(); // 关闭当前通道
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送给其他客户端消息
     *
     * @param msg：           具体的消息
     * @param presentChanel： 排除当前客户端
     */
    public void sentToAllClient(String msg, SocketChannel presentChanel) throws IOException {
        System.out.println("服务端开始转发，当前转发的线程：" + Thread.currentThread().getName());
        for (SelectionKey key : selector.keys()) {
            Channel channel = key.channel();
            if (channel instanceof SocketChannel && channel != presentChanel) {
                SocketChannel socketChannel = (SocketChannel) channel;
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
                socketChannel.write(buffer);
            }
        }
    }
}

class ServerStarter {
    public static void main(String[] args) {
        Server server = new Server();
        server.init();
        server.listen();
    }
}
```

## Client

```java
package com.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;

public class Client {
    /*是用来监听服务端转发过来的消息*/
    private Selector selector;
    private SocketChannel clientChannel;
    private static final int PORT = 9090;

    public void init() {
        try {
            // 1. 创建选择器
            selector = Selector.open();
            // 2. 连接服务器
            clientChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", PORT));
            // 3. 设置非阻塞模式
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ); // 只监听读事件
            System.out.println("当前客户端准备完成");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMsg() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String msg = scanner.nextLine();
            try {
                clientChannel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void messageListenThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                readInfo();
            }
        }).start();
    }

    public void readInfo() {
        while (true) {
            try {
                if (selector.select() > 0) {
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey selectionKey = it.next();
                        if (selectionKey.isReadable()) {
                            SocketChannel channel = (SocketChannel) selectionKey.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            channel.read(buffer);
                            System.out.println("msg：" + new String(buffer.array()).trim());
                        }
                        it.remove();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.init();
        client.messageListenThread();
        client.sendMsg();
    }
}
```

