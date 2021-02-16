package pro.eddiecache.kits.disk.indexed;

import java.io.Serializable;

public class IndexedDiskProbe
{
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("Usage: java pro.eddievim.kits.disk.indexed.IndexedDiskProbe <cache_name>");
			System.exit(0);
		}

		IndexedDiskCacheAttributes attr = new IndexedDiskCacheAttributes();

		attr.setCacheName(args[0]);
		attr.setDiskPath(args[0]);

		IndexedDiskCache<Serializable, Serializable> dc = new IndexedDiskCache<Serializable, Serializable>(attr);
		dc.dump(true);
		System.exit(0);
	}
}
