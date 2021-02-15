package pro.eddiecache.utils.discovery;

public interface IDiscoveryListener
{
	void addDiscoveredService(DiscoveredService service);

	void removeDiscoveredService(DiscoveredService service);
}
