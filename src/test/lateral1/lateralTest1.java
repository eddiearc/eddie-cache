package lateral1;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.CacheKit;
import pro.eddiecache.access.CacheKitAccess;

public class lateralTest1
{
	private static final Log log = LogFactory.getLog(lateralTest1.class);

	public static void main(String[] args) throws InterruptedException
	{
		System.out.println(log.isDebugEnabled());

		System.out.println(log.isInfoEnabled());

		CacheKit.setConfigFilename("/lateral1/cachekit.xml");

		CacheKitAccess cacheKitAccess = CacheKit.getInstance("default");
		CacheKitAccess cacheKitAccess1 = CacheKit.getInstance("default1");
		cacheKitAccess1.put("key", "value");

		//main线程休眠，等待服务发现和注册的完成
		Thread.sleep(60);

		System.out.println(cacheKitAccess.get("cacheKit"));

		int max = 3;

		for (int i = 0; i < max; i++)
		{
			cacheKitAccess.put("id" + i, i + 666 + "");
		}
		cacheKitAccess.put("cacheKit", "hello, world!");

		System.out.println("缓存对象" + cacheKitAccess.get("id0"));

		cacheKitAccess.freeMemoryElements(1000);



		cacheKitAccess.dispose();

		//main线程休眠，等待其他节点的读取功能完成
		Thread.sleep(30000);

	}

}
