package pro.eddiecache.kits.paxos;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pro.eddiecache.core.CacheInfo;
import pro.eddiecache.core.CacheStatus;
import pro.eddiecache.core.model.ICacheElement;
import pro.eddiecache.core.stats.IStats;
import pro.eddiecache.kits.AbstractKitCacheEvent;
import pro.eddiecache.kits.KitCacheAttributes;
import pro.eddiecache.kits.paxos.comm.Member;
import pro.eddiecache.kits.paxos.comm.Members;

public class PaxosCache<K, V> extends AbstractKitCacheEvent<K, V>
{

	private PaxosGroup group;

	public PaxosCache(PaxosCacheAttributes cattr) throws NumberFormatException, UnknownHostException, SocketException
	{
		List<Member> memberList = new ArrayList<Member>();
		String servers = cattr.getServers();
		String[] server = servers.split(",");

		for (String s : server)
		{
			String ip = s.split(":")[0];
			String port = s.split(":")[1];

			Member member = new Member(InetAddress.getByName(ip), Integer.valueOf(port));

			memberList.add(member);

		}

		Members members = new Members(memberList);

		int myPosition = Integer.valueOf(cattr.getMyPosition());

		group = new PaxosGroup(members.get(myPosition), new PaxosCacheReceiver());

	}

	@Override
	public Set<K> getKeySet() throws IOException
	{

		return null;
	}

	@Override
	public IStats getStatistics()
	{

		return null;
	}

	@Override
	public KitCacheAttributes getKitCacheAttributes()
	{

		return null;
	}

	@Override
	public int getSize()
	{

		return 0;
	}

	@Override
	public CacheStatus getStatus()
	{

		return null;
	}

	@Override
	public String getStats()
	{

		return null;
	}

	@Override
	public String getCacheName()
	{

		return null;
	}

	@Override
	public CacheType getCacheType()
	{
		return CacheType.PAXOS_CACHE;
	}

	@Override
	protected void processUpdate(ICacheElement<K, V> cacheElement) throws IOException
	{

		CacheElementPaxos cep = new CacheElementPaxos(cacheElement, CacheInfo.listenerId);
		group.broadcast(cep);
	}

	@Override
	protected ICacheElement<K, V> processGet(K key) throws IOException
	{
		//		group.broadcast((Serializable) key);
		return null;
	}

	@Override
	protected Map<K, ICacheElement<K, V>> processGetMultiple(Set<K> keys) throws IOException
	{
		return null;
	}

	@Override
	protected Map<K, ICacheElement<K, V>> processGetMatching(String pattern) throws IOException
	{
		return null;
	}

	@Override
	protected boolean processRemove(K key) throws IOException
	{
		return false;
	}

	@Override
	protected void processRemoveAll() throws IOException
	{

	}

	@Override
	protected void processDispose() throws IOException
	{

	}

	@Override
	public String getEventLoggerExtraInfo()
	{
		return null;
	}

}
