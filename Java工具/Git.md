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