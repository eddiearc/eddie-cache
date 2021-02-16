package paxos2;

import pro.eddiecache.CacheKit;
import pro.eddiecache.access.CacheKitAccess;

public class Paxos2Test
{

	public static void main(String[] args) throws InterruptedException
	{

		CacheKit.setConfigFilename("/paxos2/cachekit.xml");
		CacheKitAccess cacheKitAccess = CacheKit.getInstance("paxos2");
		//main线程休眠，等待服务发现和注册的完成
		Thread.sleep(60000);

		// 测试节点的数据读取
		System.out.println("缓存对象" + cacheKitAccess.get("id0"));


		Thread.sleep(30000);
	}

}
