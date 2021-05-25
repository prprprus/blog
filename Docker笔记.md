# Docker 笔记

## 介绍和安装

Docker 是一种虚拟化技术，基于 Linux 的 cgroup、namespace、OverlayFS 等技术，对进程进行封装隔离，属于操作系统级别的虚拟化

主要和传统虚拟化技术作对比：

- 传统虚拟化技术会虚拟出一套硬件，在硬件之上运行完整的操作系统，在操作系统之上再运行应用程序；Docker 则不会虚拟化出硬件和操作系统，容器直接运行在宿主机的操作系统上
- Docker 更轻量级、占用更少系统资源、有更快的启动速度、能运行的容器数量比虚拟机数量更多

使用 Docker 的好处：

- 轻量级（省资源、启动快、同时运行的数量多）
- 提供一致的运行环境（开发、测试、运维）
- 方便迁移
- 可以搭配 CI/CD 使用
- 对于 Dockerfile 定义的运行环境，开发和运维都看得懂，消除两者的隔阂

Docker 的三个基本概念：

- 镜像：一种特殊的文件系统，包含容器运行所需的各种文件，镜像是分层存储的
- 容器：本质就是一个有独立命名空间的进程，容器内存储的数据会随着容器的消亡而消亡
- 仓库：集中存储、分发镜像的地方，分为私有仓库和公共仓库两种

Docker 使用上的整体结构：

![](https://raw.githubusercontent.com/hsxhr-10/Blog/master/image/docker-1.png)

### 安装

安装方法比较多，譬如 [用脚本安装](https://github.com/hsxhr-10/Blog/blob/master/%E5%B8%B8%E7%94%A8%E7%BB%84%E4%BB%B6%E5%AE%89%E8%A3%85.md#docker)

## 使用镜像

Docker 容器运行之前需要本地存在对应的镜像文件，如果本地没有，Docker 会尝试从仓库拉取

### 常用命令

- 拉取镜像：`docker pull [options] [Docker Registry <host>[:port]] <image>[:<tag>]`
- 列出镜像：`docker images -a`、`docker image ls`、`docker image ls -a`、`docker image ls -q`
- 列出悬虚镜像：`docker image ls -f dangling=true`
- 清理悬虚镜像：`docker image prune`
- 删除镜像：`docker image rm <image>`

### Dockerfile

#### 一些技巧

- 使用 `RUN` 命令时能作为一层就尽量作为一层，避免多个 `RUN`
- 每一层没用的文件要删除，减少镜像体积
- 镜像构建上下文是指 `docker build` 所在的目录。`ADD`、`COPY` 等命令需要额外赋值对应文件到上下文目录下，也可以通过 `.dockerignore` 忽略需要上传到 Docker 服务端的文件

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

#### 示例

```dockerfile
FROM python:3

WORKDIR /usr/src/app

COPY . .

RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 5000

CMD ["python", "./app.py"]
```

多阶段构建：

```dockerfile
FROM node:alpine as frontend

COPY package.json /app/

RUN set -x ; cd /app \
      && npm install --registry=https://registry.npm.taobao.org

COPY webpack.mix.js /app/
COPY resources/ /app/resources/

RUN set -x ; cd /app \
      && touch artisan \
      && mkdir -p public \
      && npm run production

FROM composer as composer

COPY database/ /app/database/
COPY composer.json /app/

RUN set -x ; cd /app \
      && composer config -g repo.packagist composer https://mirrors.aliyun.com/composer/ \
      && composer install \
           --ignore-platform-reqs \
           --no-interaction \
           --no-plugins \
           --no-scripts \
           --prefer-dist

FROM php:7.4-fpm-alpine as laravel

ARG LARAVEL_PATH=/app/laravel

COPY --from=composer /app/vendor/ ${LARAVEL_PATH}/vendor/
COPY . ${LARAVEL_PATH}
COPY --from=frontend /app/public/js/ ${LARAVEL_PATH}/public/js/
COPY --from=frontend /app/public/css/ ${LARAVEL_PATH}/public/css/
COPY --from=frontend /app/public/mix-manifest.json ${LARAVEL_PATH}/public/mix-manifest.json

RUN set -x ; cd ${LARAVEL_PATH} \
      && mkdir -p storage \
      && mkdir -p storage/framework/cache \
      && mkdir -p storage/framework/sessions \
      && mkdir -p storage/framework/testing \
      && mkdir -p storage/framework/views \
      && mkdir -p storage/logs \
      && chmod -R 777 storage \
      && php artisan package:discover

FROM nginx:alpine as nginx

ARG LARAVEL_PATH=/app/laravel

COPY laravel.conf /etc/nginx/conf.d/
COPY --from=laravel ${LARAVEL_PATH}/public ${LARAVEL_PATH}/public
```

## 操作容器

## 数据管理

## 网络配置
