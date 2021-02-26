package pro.eddiecache.core.control.event;

public enum ElementEventType
{
	/**
	 * 缓存过期时长
	 */
	EXCEEDED_MAXLIFE_BACKGROUND,

	EXCEEDED_MAXLIFE_ONREQUEST,

	//缓存闲置时长
	EXCEEDED_IDLETIME_BACKGROUND,

	EXCEEDED_IDLETIME_ONREQUEST,

	//刷盘：磁盘可用
	SPOOLED_DISK_AVAILABLE,

	//刷盘：磁盘不可用
	SPOOLED_DISK_NOT_AVAILABLE,

	//不可刷盘, 在已经存储至磁盘之后，使用此type
	SPOOLED_NOT_ALLOWED
}
