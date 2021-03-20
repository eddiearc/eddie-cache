# eddie-cache
一个基于内存与磁盘存储的小型分布式缓存系统。

## 架构
![eddie-cache架构](https://tva1.sinaimg.cn/large/008eGmZEly1go90h0gu7yj30mo0hk759.jpg)

### Current features

- 多种缓存淘汰算法
- Multi Reactor服务器模型的高可用分布式集群组件集群
- 基于Paxos算法的强一致性集群

## 运行与测试
`src/test`目录中：
- paxos1, paxos2, paxos3用于测试Disk组件与Paxos组件（同时运行三者）
- lateral1, lateral2用于测试Disk组件与Lateral组件（同时运行二者）

## Design document
- [IO model](DesignDocument/eddie-cache与Redis-IO模型思考.md)
- DiskCache
    - [IndexedDiskCache](DesignDocument/AboutIndexedDiskCacheKit.md)
    - [BlockDiskCache](DesignDocument/AboutBlockDiskCacheKit.md)
- [LateralCache](DesignDocument/AboutLateralCacheKit.md)
- [PaxosCache]()
