package pro.eddiecache.utils.discovery;

/**
 * @author eddie
 */
public interface IDiscoveryListener
{
	void addDiscoveredService(DiscoveredService service);

	void removeDiscoveredService(DiscoveredService service);
}
