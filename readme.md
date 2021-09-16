# eddie-cache
一个基于内存与磁盘存储的小型分布式缓存系统。

## 架构
![eddie-cache架构](https://tva1.sinaimg.cn/large/008eGmZEly1go90h0gu7yj30mo0hk759.jpg)

### Current features

- 缓存淘汰算法的多样性，FIFO、LRU、LFU等；
- 使用索引数据及块存储的方式对缓存进行持久化至磁盘中；
- 使用Multi-Reactors服务器设计模型实现分布式组件之间的高效率IO，使用UDP组播实现分布式缓存之间的服务发现；
- 使用Multi-Paxos算法解决分布式组件之间的分布式共识问题；
- 使用Paxos Commit保证了多个节点的K-V数据的一致性。

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
- [PaxosCache](DesignDocument/AboutPaxosCacheKit.md)
