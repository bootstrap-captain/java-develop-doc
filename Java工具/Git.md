# GIT指令

## 1. 基本指令

```bash
# 查看git版本
git --version

# 配置用户名： 配置好的信息，就会在git提交代码的时候进行显示
git config --global --list      # 查看git配置的参数
git config --global -l

git config --global user.name jack                       # 配置用户名
git config --global user.email 1037289945@qq.com         # 配置邮箱名

# 删除某个配置
git config --global --unset key

# mac的git相关配置的信息保存在如下文件中
/Users/shuzhan/.gitconfig文件中保存

[user]
	name = jack
	email = 1037289945@qq.com
```

![image-20230613171306687](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20230613171306687.png)

## 2. 进阶指令

### 2.1 本地仓库变git并推送到远程

```bash
# 进入本地一个文件目录, 文件夹会多出   .git目录,  默认是master，目录结构为hahaha
git init 

# 将仓库和远程的进行映射，远程仓库默认是main分支
git remote add origin git@gitee.com:daydreamer9451/repo-from-local.git

# 推送代码: 将本地的master的branch的代码，推送代码到远程仓库的master
git push -u origin master


# 异常报错： 说明该项目在远程已经有一个仓库地址了，需要先移除之前的仓库地址，添加新的仓库地址
关于push推送报错解决：fatal: remote origin already exists
git remote remove origin     # //移除原来远程git仓库地址
git remote add origin git@gitee.com:daydreamer9451/repo-from-local.git # 和新的仓库进行绑定
```

```bash
# 1.2 远程仓库
git clone git@gitee.com:daydreamer9451/secret-manager.git

# 将被git跟踪的文件回退到未跟踪
git restore --staged 1.txt
git rm --cached 1.txt
```

## 3. 回退已push

### 3.1 版本号

```bash
# 1. 查看版本号, 按Q退出
git log

commit b1177ad7b674e426085670ea7bb48d61fd174c58 (HEAD -> main, origin/main, origin/HEAD)
Author: jack <1037289945@qq.com>
Date:   Wed Jun 14 21:58:50 2023 +0800

    add 2.txt

commit 35d49baef0d820dd4b32419274424911de9f0779  # 即这个版本号
Author: jack <1037289945@qq.com>
Date:   Wed Jun 14 21:58:23 2023 +0800

    add 1.txt

# 要回退到添加2.txt的版本， 则需要回退到2.txt提交的上一次提交的commitId

# 2. 本地回退: 会将本地工作区的改动删除，但是远程代码还没有影响
git reset --hard 35d49baef0d820dd4b32419274424911de9f0779

# 3. 回退 推送远程
git push origin main -f
```

### 3.2 提交次数

```bash
# 1. 本地回退上一次的提交
git reset --hard head~1      # 回退上一次push
git reset --hard head~2      # 回退上两次push

# 2. 回退 推送远程
git push origin main -f
```

### 3.3 回退指令

```bash
git reset --soft head~1    # 撤销commit，不撤销add，工作区代码改动不会丢失
git reset --mixed head~1   # 撤销commit，撤销add，工作区代码改动不会丢失
git reset --hard head~1    # 撤销commit，撤销add，工作区代码改动丢失。完成后回退到上一次commit之前的代码
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

#  分支管理

![image-20241107144432819](https://erick-typora-image.oss-cn-shanghai.aliyuncs.com/img/image-20241107144432819.png)
