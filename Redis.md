## Redis 开发使用注意事项

### 内存方面

- 把 Redis 当缓存用，而不是数据库
- 控制 key 长度
- 控制 value 大小
- 设置过期时间
- 设置 maxmemory 和数据淘汰策略
  ```
  volatile-lru / allkeys-lru：优先保留最近访问过的数据
  volatile-lfu / allkeys-lfu：优先保留访问次数最频繁的数据（4.0+版本支持）
  volatile-ttl ：优先淘汰即将过期的数据
  volatile-random / allkeys-random：随机淘汰数据
  ```
- 数据压缩后在写入 Redis

### 性能方面

- 控制 value 大小，大 value 在内存分配和内存释放的时候耗时更多
- 开启 `lazy-free`(4.0+), 内存释放操作交给后台线程执行, 避免阻塞主线程
- 不使用时间复杂度大的命令（大于 `O(N)` 的一些命令），比如一些聚合操作，尽量交给应用程序去做
- 即使是 `O(N)` 的命令，也要注意 N 的大小，比如查询、删除等操作，如果 N 很大，应该分批次处理
- 数据的过期时间避免过分集中，可以给过期时间加上一个随机值
- AOF 持久化配置 `appendfsync everysec` 交给后台线程执行，避免阻塞主线程
- 当单实例顶不住时，通过主从、分片等架构方式提升性能
- 使用物理服务器部署 Redis

### 网络传输方面

- 控制 value 大小，大 value 在网络传输的时候耗时更多
- 不要在 for 循环里面创建 Redis 连接（创建、销毁 TCP 连接的耗时不能忽略）、执行命令（网络传输的耗时不能不略，这种情况应该调整数据结构）
- 用批量操作命令代替单个操作命令（`MGET/MSET/HMGET/HMSET`），或者使用 Pipeline
- 应用程序配置好连接池、tcp-keeplive

## Redis 日常运维注意事项

- 禁止使用 `KEYS/FLUSHALL/FLUSHDB/CONFIG/EVAL` 命令
- `SCAN` 等扫描操作设置休眠时间
- 慎用 `MONITOR` 命令
- 从库必须设置为 `slave-read-only`，从库写入了有过期时间的数据，不会做定时清理和释放内存（4.0修复）
- 不要把 Redis 部署在公网可访问的服务器上
- 部署时不使用默认端口 6379
- 以普通用户启动 Redis 进程，禁止 root 用户启动
- 限制 Redis 配置文件的目录访问权限
- 开启密码认证

## Redis 压测方法

- TODO

### 参考

- Redis最佳实践：7个维度+43条使用规范，带你彻底玩转Redis |
  附最佳实践清单：https://zhuanlan.zhihu.com/p/354486475
- Redis 性能优化的 13 条军规！史上最全：https://zhuanlan.zhihu.com/p/118532234
