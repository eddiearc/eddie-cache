package lateral2;

import pro.eddiecache.CacheKit;
import pro.eddiecache.access.CacheKitAccess;

public class lateralTest2
{

	public static void main(String[] args) throws InterruptedException
	{

		CacheKit.setConfigFilename("/lateral2/cachekit.xml");
		CacheKitAccess cacheKitAccess = CacheKit.getInstance("default");
		//main线程休眠，等待服务发现和注册的完成
		Thread.sleep(60000);

		//		测试节点的数据读取
		System.out.println("缓存对象" + cacheKitAccess.get("id2"));

		//
		Thread.sleep(30000);

	}

}
