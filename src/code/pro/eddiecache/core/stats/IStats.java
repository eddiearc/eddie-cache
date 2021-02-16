package pro.eddiecache.core.stats;

import java.io.Serializable;
import java.util.List;

public interface IStats extends Serializable
{

	List<IStatElement<?>> getStatElements();

	void setStatElements(List<IStatElement<?>> stats);

	String getTypeName();

	void setTypeName(String name);
}
