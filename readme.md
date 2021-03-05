# eddie-cache
一个基于内存与磁盘存储的小型分布式缓存系统。

## 架构
![eddie-cache架构](https://tva1.sinaimg.cn/large/008eGmZEly1go90h0gu7yj30mo0hk759.jpg)

### Current features

- 多种缓存淘汰算法
- 缓存正常关闭下的数据持久化
- 基于Socket通信的高可用分布式集群
- 基于Paxos算法的强一致性集群

### Future features

- 借鉴Redis-AOF机制：将日志进行实时记录，达到一个实时持久化的作用
- 事务的支持

## 运行与测试
`src/test`目录中：
- paxos1, paxos2, paxos3用于测试Disk组件与Paxos组件（同时运行三者）
- lateral1, lateral2用于测试Disk组件与Lateral组件（同时运行二者）

## 缓存实例相关原理
- DiskCache
    - [IndexedDiskCache](DesignDocument/AboutIndexedDiskCacheKit.md)
    - [BlockDiskCache](DesignDocument/AboutBlockDiskCacheKit.md)
- [LateralCache](DesignDocument/AboutLateralCacheKit.md)
- [PaxosCache]()
