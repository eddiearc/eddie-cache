import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pro.eddiecache.CacheKit;
import pro.eddiecache.access.CacheKitAccess;

public class Test1
{
	private static final Log log = LogFactory.getLog(Test1.class);

	public static void main(String[] args) throws InterruptedException
	{
		System.out.println(log.isDebugEnabled());

		System.out.println(log.isInfoEnabled());

		CacheKitAccess cacheKitAccess = CacheKit.getInstance("default");

		//main线程休眠，等待服务发现和注册的完成
		Thread.sleep(6);

		System.out.println(cacheKitAccess.get("id0"));

		int max = 3;

		for (int i = 0; i < max; i++)
		{
			cacheKitAccess.put("id" + i, i + 100000);
		}
		System.out.println("缓存对象" + cacheKitAccess.get("id0"));

		//main线程休眠，等待其他节点的读取功能完成
		Thread.sleep(30000);

	}

}
