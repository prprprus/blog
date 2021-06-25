# Docker 笔记

## 介绍和安装

Docker 是一种虚拟化技术，基于 Linux 的 cgroup、namespace、OverlayFS 等技术，对进程进行封装隔离，属于操作系统级别的虚拟化

### 对比传统虚拟化

- 传统虚拟化技术会虚拟出一套硬件，在硬件之上运行完整的操作系统，在操作系统之上再运行应用程序；Docker 则不会虚拟化出硬件和操作系统，容器直接运行在宿主机的操作系统上
- Docker 更轻量级、占用更少系统资源、有更快的启动速度、能运行的容器数量比虚拟机数量更多

### 使用 Docker 的好处

- 轻量级（省资源、启动快、同时运行的数量多）
- 提供一致的运行环境（开发、测试、运维）
- 方便迁移
- 可以搭配 CI/CD 使用
- 对于 Dockerfile 定义的运行环境，开发和运维都看得懂，消除两者的隔阂

### Docker 的三个基本概念

- 镜像：一种特殊的文件系统，包含容器运行所需的各种文件，镜像是分层存储的
- 容器：本质就是一个有独立命名空间的进程，容器内存储的数据会随着容器的消亡而消亡
- 仓库：集中存储、分发镜像的地方，分为私有仓库和公共仓库两种

### Docker 使用上的整体结构

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/docker-1.png)

### 安装

安装方法比较多，比如 [用脚本安装](https://github.com/zongzhenh/Blog/blob/master/%E5%B8%B8%E7%94%A8%E7%BB%84%E4%BB%B6%E5%AE%89%E8%A3%85.md#docker)

## 镜像

Docker 容器运行之前需要本地存在对应的镜像文件，如果本地没有，Docker 会尝试从仓库拉取

### 常用命令

- 拉取镜像：`docker pull [options] [Docker Registry <host>[:port]] <image>[:<tag>]`
- 列出镜像：`docker images -a`、`docker image ls`、`docker image ls -a`、`docker image ls -q`
- 列出悬虚镜像：`docker image ls -f dangling=true`
- 清理悬虚镜像：`docker image prune`
- 删除镜像：`docker image rm <image>`

### Dockerfile

#### 常用指令

- `FROM <base_image>`：指定基础镜像，Dockerfile 的第一条指令，每个 Dockerfile 都必须存在该指令，可以用 `FROM scratch` 指定空的基础镜像
- `EVN <key>=<VALUE>`：设置环境变量
- `USER <user>[:group]`：切换指定的用户、组
- `WORKDIR <path>`：设置工作目录
- `RUN <command>`：执行命令行命令
- `COPY [--chown=<user>:<group>] <source> <target>`：将源文件复制到目标路径下，源文件的路径是 `docker build` 所在的目录，目标路径是容器内的绝对路径；
  如果源文件是目录，则不会复制目录本身，而是复制目录下的所有文件；源文件的元数据会被保留
- `ADD [--chown=<user>:<group>] <source> <target>`：功能和 `COPY` 差不多，区别是 `COPY` 是单纯复制文件，`ADD` 带有自动解压功能；`ADD` 会让构建缓存失效，可能会令构建时间增加
- `VOLUME ["<path1>", "<path2>"...]`：设置要挂载的数据卷
- `EXPOSE <port1>, <port2>, ...`：声明要暴露的端口号
- `CMD ["executable", "arg1", "arg2", ...]`：设置容器的主进程启动

#### 一些技巧

- 使用 `RUN` 命令时能作为一层就尽量作为一层，避免多个 `RUN`
- 每一层没用的文件要删除，减少镜像体积
- 镜像构建上下文是指 `docker build` 所在的目录。`ADD`、`COPY` 等命令需要额外赋值对应文件到上下文目录下，也可以通过 `.dockerignore` 忽略需要上传到 Docker 服务端的文件

#### 示例

```dockerfile
FROM python:3

WORKDIR /usr/src/app

COPY . .

RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 5000

CMD ["python", "./app.py"]
```

## 容器

### 常用命令

- 启动容器：`docker run [options] <image> [command] [arg]`
    - `--name` 设置容器名称
    - `-d` 设置是否后台运行容器
    - `-i` 设置交互式启动容器
    - `-t` 分配终端，一般 `-it` 搭配起来用
    - `-p` 设置端口映射（宿主机端口：容器端口）
    - `v` 设置目录影射（宿主机目录路径：容器目录路径）
    - `--net` 设置容器网络（bridge、host）
    - `--link` 连接其他容器
    - `--rm` 设置容器停止后自动删除
    - `-m` 设置容器内存上限
    - `--cpuset` 设置容器可以使用哪些 CPU
    - `--dns` 设置 DNS 服务器
    - `--restart` 设置容器停止后是否自动重启（no、on-failure、always）
    - `--privileged` 设置是否特权容器
- 列出容器：`docker ps -a`，`docker container ps -a`
- 重启容器：`docker restart <container>`
- 产看后台容器的日志：`docker logs <container>`
- 进入容器：`docker exec -it <container> <command>`
- 删除容器：`docker rmi <container>`，`docker rmi $(docker ps -a -q)`
- 清理所有已经停止的容器：`docker system prune`，`docker container prune`

## 数据管理

旧版本是通过 `docker run` 的 `-v` 参数来挂载数据卷，新版本可以通过 `--mount` 实现，额外提供了一些新的功能，比如设置 `readonly`

```BASH
# 将 /src/webapp 设置成只读
docker run -d -P \
    --name web \
    # -v /src/webapp:/usr/share/nginx/html:ro \
    --mount type=bind,source=/src/webapp,target=/usr/share/nginx/html,readonly \
    nginx:alpine
```

## 网络配置

### 端口映射

- `P` 随机映射
- `-p 80:80` 将宿主机任意地址的 80 端口映射到容器的 80 端口
- `-p 127.0.0.1:80:80` 将宿主机 127.0.0.1 地址的 80 端口映射到容器的 80 端口
- `-p 127.0.0.1::80` 将宿主机 127.0.0.1 的任意端口映射到容器的 80 端口

### 容器互联

一般使用 Docker Compose，也可以自定义网络实现

```BASH
# 新建网络
docker network create -d bridge my-net

# 启动容器1，并连接到 my-net
docker run -d --name container1 --network my-net imageA

# 启动容器2，并连接到 my-net
docker run -d --name container2 --network my-net imageA

# 此时容器1和容器2可以相互 ping 通
```

### 配置 DNS

- 配置局部 DNS，通过启动参数针对某个容器配置
- 配置全局 DNS：通过 `/etc/docker/daemon.json` 文件
    ```BASH
    {
      "dns" : [
        "114.114.114.114",
        "8.8.8.8"
      ]
    }
    ```

## Docker Compose

Docker Compose 是官方提供的容器编排工具，用来快速部署多个相关联的容器，前身是 Fig 项目

### 示例：WordPress

```yaml
version: "3"
services:

   db:  # 定义 db 服务
     image: mysql:8.0 # 镜像
     command: # 容器启动命令或者启动参数
      - --default_authentication_plugin=mysql_native_password
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_unicode_ci     
     volumes: # 目录映射
       - db_data:/var/lib/mysql
     restart: always  # 重启策略
     environment: # 环境变量
       MYSQL_ROOT_PASSWORD: somewordpress
       MYSQL_DATABASE: wordpress
       MYSQL_USER: wordpress
       MYSQL_PASSWORD: wordpress

   wordpress: # 定义 wordpress 服务
     depends_on:  # 依赖于 db 服务, 会等待 db 启动成功后 wordpress 才会启动 
       - db
     image: wordpress:latest
     ports: # 端口映射
       - "8000:80"
     restart: always
     environment:
       WORDPRESS_DB_HOST: db:3306
       WORDPRESS_DB_USER: wordpress
       WORDPRESS_DB_PASSWORD: wordpress
volumes:  # db 定义了目录影射, 这里必须也声明
  db_data:
```

`docker-compose up -d` 后台启动相关容器
