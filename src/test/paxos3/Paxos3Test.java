package paxos3;

import pro.eddiecache.CacheKit;
import pro.eddiecache.access.CacheKitAccess;

public class Paxos3Test
{

	public static void main(String[] args) throws InterruptedException
	{

		CacheKit.setConfigFilename("/paxos3/cachekit.xml");
		CacheKitAccess cacheKitAccess = CacheKit.getInstance("default");
		//main线程休眠，等待服务发现和注册的完成
		Thread.sleep(10000);

		//		测试节点的数据读取
		System.out.println("缓存对象" + cacheKitAccess.get("id2"));

		//
		//		Thread.sleep(30000);

	}

}
