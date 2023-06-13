# GIT指令

```bash
# 查看git版本
git --version

# 配置用户名
git config --global --list      # 查看git配置的参数

git config --global user.name jack                       # 配置用户名
git config --global user.email 1037289945@qq.com         # 配置邮箱名

# 创建项目
# 1.1 本地创建git仓库：在对应的目录内，创建git项目，会有一个  .git来保存版本信息
git init

# 1.2 远程仓库
git clone git@gitee.com:daydreamer9451/secret-manager.git
```



 # 推送权限

## 1. SSH

```bash
# 本地git安装好之后，通过指令生成本地公私密钥对
ssh-keygen

# 将公钥拷贝下来，在git仓库上进行配置，一般名字是以本地设备的名字来命名
cat id_rsa.pub 

# clone代码时候，使用git协议，不能使用https
git@github.com:bootstrap-captain/java-develop-doc.git
```

![image-20230613154729902](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230613154729902.png)

# 其他细节

## 1. git忽略

```bash
# 项目添加 .gitignore文件
* .class就会忽略所有的class文件
```

