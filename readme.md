# eddie-cache
一个基于内存与磁盘存储的小型分布式缓存系统。

## 架构
![eddie-cache架构图](https://tva1.sinaimg.cn/large/008eGmZEly1gnqkwy9qd8j30ze0q1ju4.jpg)

## 配置
- `commons-loggin.properties` 使用的日志类
- `simplelog.properties` log打印等级
- xml文件：配置缓存相关信息

## 运行与测试
`src/test`目录中：
- paxos1，paxos2，paxos3用于测试Disk组件与Paxos组件
- test1和test2用于测试Disk组件与Lateral组件

## 缓存实例相关原理
- DiskCache
    - [IndexedDiskCache](./AboutIndexedDiskCacheKit.md)
    - [BlockDiskCache](./AboutBlockDiskCacheKit.md)
- [LateralCache]()
- [PaxosCache]()
