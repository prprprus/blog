## Nginx 基本配置

```Nginx
# 运行用户
user nobody;

# 启动进程, 通常设置成和 CPU 的数量相等
worker_processes  4;

# 全局错误日志
error_log  logs/error.log;
error_log  logs/error.log  notice;
error_log  logs/error.log  info;

# PID 文件
pid        logs/nginx.pid;

# 工作模式及连接数上限
events {
    # epoll 是 IO 多路复用 (I/O Multiplexing) 中的一种方式,
    # 仅用于 linux2.6 以上内核,可以大大提高 nginx 的性能
    use   epoll; 

    # 单个后台 worker process 进程的最大并发链接数 (可以理解成线程数)    
    worker_connections  8000;
    
    # ⭐️ 关键配置技巧:
    #
    # 1. 并发总数是 worker_processes 和 worker_connections 的乘积
    # 即 max_clients = worker_processes * worker_connections
    #
    # 2. 在设置了反向代理的情况下，max_clients = worker_processes * worker_connections / 4
    # 为什么上面反向代理要除以 4，应该说是一个经验值
    #
    # 3. 根据以上条件，正常情况下的 Nginx Server 可以应付的最大连接数为：4 * 8000 = 32000
    # worker_connections 值的设置跟物理内存大小有关
    #
    # 4. 因为并发受 IO 约束，max_clients 的值须小于系统可以打开的最大文件数 (用 ulimit -n 查看当前可以打开的最大文件数)
    # 而系统可以打开的最大文件数和内存大小成正比，一般 1GB 内存的机器上可以打开的文件数大约是 10 万左右
    # 我们来看看 360M 内存的 VPS 可以打开的文件句柄数是多少：
    # $ cat /proc/sys/fs/file-max
    # 输出 34336
    # 32000 < 34336，即并发连接总数小于系统可以打开的文件句柄总数，这样就在操作系统可以承受的范围之内
    # 所以，worker_connections 的值需根据 worker_processes 进程数目和系统可以打开的最大文件总数进行适当地进行设置
    # 使得并发总数小于操作系统可以打开的最大文件数目
    # 其实质也就是根据主机的物理 CPU 和内存进行配置
    # 当然，理论上的并发总数可能会和实际有所偏差，因为主机还有其他的工作进程需要消耗系统资源。
    # (修改可以打开的最大文件数: ulimit -SHn 65535)
    #
    # 总之一句话: worker_processes 等于 CPU 核心数, worker_connections 等于 ${/proc/sys/fs/file-max} / worker_processes 

}


http {
    # 设定 mime 类型, 类型由 mime.type 文件定义
    include    mime.types;
    default_type  application/octet-stream;
    
    # 设定日志格式
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';
    
    # 全局访问日志
    access_log  logs/access.log  main;
    
    # sendfile 好处的简介: http://www.weixueyuan.net/a/670.html
    # https://blog.csdn.net/qq_34556414/article/details/106918149
    #
    # sendfile 指令指定 nginx 是否调用 sendfile 函数（zero copy 方式）来输出文件，
    # 对于普通应用，必须设为 on,
    # 如果用来进行下载等应用磁盘 IO 重负载应用，可设置为 off，
    # 以平衡磁盘与网络 I/O 处理速度，降低系统的负载.
    sendfile     on;

    # tcp_nopush 和 tcp_nodelay
    # 1，tcp_nopush = on 会设置调用 tcp_cork 方法，这个也是默认的，结果就是数据包不会马上传送出去，等到数据包最大时，一次性的传输出去，这样有助于解决网络堵塞。
    # 2，tcp_nodelay 会尽量发送小数据块，而不是等一定数据量满了之后才发送。
    # 3，当使用 sendfile 函数时，tcp_nopush 才起作用，它和指令 tcp_nodelay 是互斥的
    # tcp_nopush     on;
    # tcp_nodelay     on;
    tcp_nodelay     on;
    
    # keepalive_timeout 65: 如果客户端在 65s 内没有后续的请求过来, Nginx 就会关闭对应的长连接
    # keepalive_request 100: 一个长连接最多处理 100 次请求就会关闭
    keepalive_timeout  65;
    keepalive_request  100;

    # 开启gzip压缩
    gzip  on;
    gzip_disable "MSIE [1-6].";

    # 设定请求缓冲
    client_header_buffer_size    128k;
    large_client_header_buffers  4 128k;
    
    # 负载均衡配置
    # max_fails: 允许的最大失败次数
    # fail_timeout: 失败后的暂停服务时间
    # weight: 轮询时的权重
    # backup: 当其他非 backup 服务器挂掉或者忙的时候, 请求被打到 backup 服务器
    # down: 标记服务器不参与负载均衡   
    upstream cluster {
        server 192.168.1.100:45870 max_fails=2 fail_timeout=30s weight=5;
        server 192.168.1.101:45871 max_fails=2 fail_timeout=30s weight=5;
        server 192.168.1.102:45872 max_fails=2 fail_timeout=30s weight=3;

        server 192.168.1.103:45873 max_fails=2 fail_timeout=30s weight=5 backup;
        server 192.168.1.104:45874 max_fails=2 fail_timeout=30s weight=5 backup;
        
        server 192.168.1.105:45875 down;
        
        # keeplive: Nginx 和后端节点的长连接个数, 这些长连接的关闭时机由后端节点的配置决定,
        # 且由后端节点发起关闭.
        #
        # keeplive 10: 如果客户端有 100 个请求过来, 对于客户端和 Nginx 之间, 会建立 100 个长连接, 
        # 超时时间是 65s, 每个长连接最多处理 100 次请求就会被关闭; 
        # 对于 Nginx 和后端节点来说, 会建立 100 个连接, 如果双方都支持 keeplive, 则这 100 个连接都是长连接,
        # 在请求结束后, Nginx 会立马关闭 90 个长连接 (100-10=90), 只剩下 10 个长连接, 
        # 这 10 个长连接由后端节点的 keepalive 参数控制超时时间
        #
        # 客户端 <----> Nginx <----> 后端节点
        keeplive 10;
    }
  
    # 设定虚拟主机配置
    server {
        # 侦听80端口
        listen    80;
        
        # 定义使用 www.nginx.cn 访问
        server_name  www.nginx.cn;

        # 定义服务器的默认网站根目录位置
        root html;

        # 设定本虚拟主机的访问日志
        access_log  logs/nginx.access.log  main;

        # 默认请求
        location / {
            # 定义首页索引文件的名称
            index index.php index.html index.htm;   
        }

        # 定义错误提示页面
        error_page   500 502 503 504 /50x.html;
        location = /50x.html {
        }

        # 静态文件，nginx 自己处理
        location ~ ^/(images|javascript|js|css|flash|media|static)/ {
            # 过期30天，静态文件不怎么更新，过期可以设大一点，
            # 如果频繁更新，则可以设置得小一点。
            expires 30d;
        }

        # PHP 脚本请求全部转发到 FastCGI 处理. 使用 FastCGI 默认配置.
        location ~ .php$ {
            fastcgi_pass 127.0.0.1:9000;
            fastcgi_index index.php;
            fastcgi_param  SCRIPT_FILENAME  $document_root$fastcgi_script_name;
            include fastcgi_params;
        }
        
        # Python RESTful 反向代理
        location /api {
            # Gunicorn 服务
            proxy_pass  http://cluster;
            
            # uwsgi 服务
            # uwsgi_pass	cluster;
            # include	uwsgi_params;
        }

        # 禁止访问 .htxxx 文件
            location ~ /.ht {
            deny all;
        }

    }
}
```

- nginx基本配置与参数说明：https://www.nginx.cn/76.html
- Full Example
  Configuration：https://www.nginx.com/resources/wiki/start/topics/examples/full/
- Nginx upstream配置说明负载均衡：https://www.jianshu.com/p/50dfb0d69983
- NO KEEP ALIVE IN NGINX：https://cinhtau.net/2017/12/19/no-keep-alive-nginx/
- Nginx实战系列之功能篇----后端节点健康检查：https://blog.51cto.com/nolinux/1594029
- Tengine：http://tengine.taobao.org/book/index.html
- Nginx限速模块初探：https://www.cnblogs.com/CarpenterLee/p/8084533.html
- nginx优化 突破十万并发：https://www.cnblogs.com/sxlfybb/archive/2011/09/15/2178160.html
- Nginx - keepliave 相关知识点：https://www.cnblogs.com/hukey/p/10506556.html
- Nginx 单机百万QPS环境搭建：https://www.cnblogs.com/wunaozai/p/6073731.html
