package engine.core;

import java.util.Set;

public interface ISpatialStructure<T extends IHasAABB> {
	public void add(T obj);
	public boolean remove(T obj);
	public Set<T> getAll(Set<T> result);
	public Set<T> queryRange(Set<T> result, AABB range);
}
