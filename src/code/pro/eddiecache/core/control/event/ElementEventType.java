package pro.eddiecache.core.control.event;

/**
 * @author eddie
 */
public enum ElementEventType
{
	/**
	 * 缓存因生命周期到了过期（在后台发现-shrinkerThread）
	 */
	EXCEEDED_MAXLIFE_BACKGROUND,

	/**
	 * 缓存因生命周期到了过期（在请求的时候发现）
	 */
	EXCEEDED_MAXLIFE_ONREQUEST,

	/**
	 * 缓存因闲置时长到了过期（在后台发现-shrinkerThread）
	 */
	EXCEEDED_IDLETIME_BACKGROUND,

	/**
	 * 缓存因闲置时长到了过期（在请求的时候发现）
	 */
	EXCEEDED_IDLETIME_ONREQUEST,

	/**
	 * 缓存持久化至磁盘（可用）
	 */
	SPOOLED_DISK_AVAILABLE,

	/**
	 * 缓存持久化至磁盘（不可用）
	 */
	SPOOLED_DISK_NOT_AVAILABLE,

	/**
	 * 不允许持久化至磁盘, 在已经存储至磁盘之后，使用此Type
	 */
	SPOOLED_NOT_ALLOWED
}
