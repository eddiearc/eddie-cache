# LateralCacheKit

> 实现了分布式组件之间的缓存共享（不保证数据一致性）

![LateralCacheKit架构](https://tva1.sinaimg.cn/large/e6c9d24ely1go4dbhabzbj218e0u0tjt.jpg)

## 服务发现

使用UDP组播的方式，从局域网中进行缓存信息的组播与接收组播中其他缓存发送过来的注册信息。`KitCacheFactory`创建缓存实例时，通过检查`KitCacheAttributes`，可以判断当前缓存是否需要创建`LateralCacheKit`。创建了`LateralCacheKit`之后，缓存实例内部会有下述活动发生：

1. `createDiscoveryService()`：创建发现服务，用于监听指定的端口，接收来自局域网中UDP组播；
2. 在上一步创建发现服务的过程中（创建发送线程过程中），会马上往UDP组播中发送一条带有`UDPDiscoveryMessage.BroadcastType.REQUEST`请求组播的信息，让已经启动并加入UDP组播的远程实例，开始组播发送相关注册信息；
3. 调用`discoveryService.startup()`方法后，开始持续轮询监听，这时如果上一条请求组播的信息成功传递至其他远程实例中，就会得到一条带有`UDPDiscoveryMessage.BroadcastType.PASSIVE`标识的注册信息，拿到该信息，注册至本地缓存中；
4. 调用`service.setScheduledExecutorService()`，使用一个定时线程池，每15s进行定时发送注册信息至其他缓存实例中，用于让其他远程实例发现本缓存；
5. 当缓存所处的JVM是正常关闭时（非强制关闭），JVM的`shutdownHook`将起作用，会发送一条带有`UDPDiscoveryMessage.BroadcastType.REMOVE`的移除缓存的信息，至其他远程缓存中，远程缓存将在本地的注册信息中移除该缓存信息。

## 数据同步

**这里的数据同步分为两种：**

- 更新时同步
- 获取时同步

**为什么有两种同步方式？**

假设有`Service-A`与`Service-B`两个远程服务。

1. 更新时同步的意义：假设`Service-A`插入了一个数据`K-V`，此时未开启更新时同步，`Service-B`去根据`K`获取对应数据`V`时，突然`Service-A`宕机了，或因为网络通信原因，无法连接至`Service-A`；
2. 获取时同步的意义：假设`Service-A`先启动，且插入新的数据`K1-V1`，在插入之后`Service-B`才启动，去获取数据`K1`所对应的值，如果此时缓存只开启更新时同步，则无法取得值`V1`，若开启了获取时同步，则可以去请求`Service-A`获取对应值`V1`；

### 更新时同步-流程

`Service-A`往本地缓存存入一个`K-V`数据时，最终会访问到`LateralCacheAsyncFacade.update() / remove()`方法；

这个对象内部维护了一个远程实例`LateralCacheAsyncs`列表，最终会一一调用其中每个实例的`update() / remove()`方法；

在这些实例的`update() / remove()`中，都使用了事件包装的方法，将`update`或`remove`事件进行各自不同的事件封装，最终由`LateralCache`调用`lateralTCPService`中的`sender`进行发送至其他服务。

假设`Service-B`接收到了`Service-A`发送来的更新信息，`LateralTCPReceiver.ListenerReceiverThread`线程会接收到该信息（该线程一直开启，循环监听），并将其包装成对应的事件通过线程池进行处理，最终调用本地缓存的`localUpdate()`方法将`K-V`数据存储到本地中。

### 获取时同步-流程

若`Service-A`需要去获取键`K`对应的值`V`，在本地上并没有存储，则需通过分布式的其他缓存中进行获取。

同样是通过`LateralCacheAsyncFacade`中的一个列表`LateralCacheAsyncs`，对每个`Async`实例进行轮询，若得到了结果，则停止轮询；

在这些实例中，最终调用了`LateralTCPSender.sendAndReceive()`方法，发送信息，并且等待其他客户端，返回对应的信息；

拿到结果后，若为null继续轮询，不为null，则得到对应的值`V`。

