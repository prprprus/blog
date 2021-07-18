# Redis 使用笔记

## 节省内存

- 控制 key 的长度是一个直接有效的方法，比如 user:name:abc 可以优化成 u:ne:abc
- 避免 bygkey，比如
    - String 大小尽量控制在 10 KB 以下
    - List / Hash / Set / ZSet 元素个数控制在 1w 以下
- 选择合适的数据结构和注意内部编码类型
  ![](https://raw.githubusercontent.com/zongzhenh/Blog/master/image/redis-1.png)
- 为 key 设置过期时间
- 设置 maxmemory 和淘汰策略 
  - volatile-lru / allkeys-lru：优先保留最近访问过的数据
  - volatile-lfu / allkeys-lfu：优先保留访问次数最频繁的数据（4.0+版本支持）
  - volatile-ttl ：优先淘汰即将过期的数据
   -volatile-random / allkeys-random：随机淘汰数据
- 数据压缩后再写入 Redis（gzip 等）

> 主要聚焦内部编码类型和一些配置

## 保持高性能

- 避免 bigkey，避免大内存的分配和回收的耗时、网络传输的耗时的增加
- 开启 lazy-free 机制，将释放内存的操作交给后台线程去做（4.0 后支持）
- 不要使用时间复杂度过高的命令，比如 `SORT`、`SINTER`、`SINTERSTORE`、`ZUNIONSTORE`、`ZINTERSTORE` 等聚合命令，这些操作可以放到应用程序去做
- 就算是 O(N) 复杂度，也要关注 N 的大小，避免网络传输耗时的增加
- 在查询数据时，应该遵守以下步骤
  1. 先查询数据的数量（`LLEN` / `HLEN` / `SCARD` / `ZCARD`）
  2. 如果元素数量较少，可以一次性查询全部数据
  3. 如果数据较多，分批查询数据（`LRANGE` / `HASCAN` / `SSCAN` / `ZSCAN`）
- 关注 `DEL` 的时间复杂度，`DEL` 的耗时会随着元素个数增加而增加
- 用批量操作命令或者 Pipeline 代替单词操作命令，可以减少网络 IO 的次数，减少网络耗时
- 应用程序应该通过长连接、连接池等方式使用 Redis，降低网络开销
- 只是用 db0，避免 `SELECT` 带来的开销，方便迁移 Redis Cluster
- 能不开启 AOF 尽量不开，或者设置 `appendfsync everysec`，把刷盘操作交给后台线程去做

> 主要聚焦命令的时间复杂度和网络耗时

## 日常运维

- 保证机器有足够的 CPU、内存、带宽、磁盘资源
- 尽量使用 Redis 5.0 以上的版本，有比较多的优化
- 单个实例的内存控制在 10G 左右，避免大实例同步的耗时
- 禁止使用 `KEYS / FLUSHALL / FLUSHDB` 命令，避免阻塞主线程
  - 可以用 `SCAN` 代替 `KEYS`
  - 4.0 之后 `FLUSHALL / FLUSHDB` 会交给后台线程去做
- 谨慎使用 `MONITOR`，避免当 Redis 负载较高时会导致内存消耗持续增加
- 从库必须设置为 slave-read-only
- 合理配置 `timeout` 和 `tcp-keepalive`，因为每个连接的 fd 是根据 `timeout` 来定时回收的，所以 `timeout` 的值不能太大，而设置 `tcp-keepalive` 的好处是
  服务端可以主动发送心跳检测给客户端，如果检测到没了心跳，可以主动回收 fd，这些都是为了解决客户端连接意外中断后，无法快速重新建立连接的问题
- 设置 slowlog 和对应的报警

## 保证安全

- 不要把 Redis 部署在公网可访问的服务器上
- 部署时不使用默认端口 6379
- 以普通用户启动 Redis 进程，禁止 root 用户启动
- 限制 Redis 配置文件的目录访问权限
- 推荐开启密码认证
- 禁用/重命名危险命令（`KEYS / FLUSHALL / FLUSHDB / CONFIG / EVAL`）
