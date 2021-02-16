package pro.eddiecache.core.stats;

import java.io.Serializable;

public interface IStatElement<V> extends Serializable
{
	String getName();

	void setName(String name);

	V getData();

	void setData(V data);
}
