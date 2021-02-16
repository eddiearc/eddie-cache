package pro.eddiecache.core.logger;

/**
 * 缓存事件记录器，wrapper即是logger，logger更具体，更实在，而wrapper更抽象，更虚幻，虚实结合，事件和日志结合
 */
public interface ICacheEventWrapper
{
	String UPDATE_EVENT = "update";

	String GET_EVENT = "get";

	String GETMULTIPLE_EVENT = "getMultiple";

	String GETMATCHING_EVENT = "getMatching";

	String REMOVE_EVENT = "remove";

	String REMOVEALL_EVENT = "removeAll";

	String DISPOSE_EVENT = "dispose";

	<T> ICacheEvent<T> createICacheEvent(String source, String cacheName, String eventName, String optionalDetails,
			T key);

	<T> void cacheEventLogger(ICacheEvent<T> event);

	void applicationEventLogger(String source, String eventName, String optionalDetails);

	void errorLogger(String source, String eventName, String errorMessage);
}
