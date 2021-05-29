# Redis 使用笔记

## 节省内存

- 控制 key 的长度是一个直接有效的方法，譬如 user:name:abc 可以优化成 u:ne:abc
- 避免 big value，譬如
    - String 大小尽量控制在 10 KB 以下
    - List / Hash / Set / ZSet 元素个数控制在 1w 以下
- 