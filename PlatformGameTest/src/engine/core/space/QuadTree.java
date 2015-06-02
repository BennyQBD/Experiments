package engine.core.space;

import java.util.Set;

public class QuadTree<T extends IHasAABB> implements ISpatialStructure<T> {
	private QuadTree<T> nodes[];
	private T           objects[];
	private int         numObjects;
	private AABB        aabb;

	public QuadTree(AABB aabb, int numObjectsPerNode) {
		this.nodes = (QuadTree<T>[])(new QuadTree[4]);
		this.objects = (T[])(new IHasAABB[numObjectsPerNode]);
		this.numObjects = 0;
		this.aabb = aabb;
	}

	private QuadTree(QuadTree<T> other) {
		this.nodes = other.nodes;
		this.objects = other.objects;
		this.numObjects = other.numObjects;
		this.aabb = other.aabb;
	}

	@Override
	public void add(T obj) {
		if (obj.getAABB().intersects(aabb)) {
			if (numObjects < objects.length) {
				objects[numObjects] = obj;
				numObjects++;
			} else {
				addToChild(obj);
			}
		} else {
			QuadTree<T> thisAsNode = new QuadTree<T>(this);

			double dirX = obj.getAABB().getCenterX() - aabb.getCenterX();
			double dirY = obj.getAABB().getCenterY() - aabb.getCenterY();

			double minX = aabb.getMinX();
			double minY = aabb.getMinY();
			double maxX = aabb.getMaxX();
			double maxY = aabb.getMaxY();

			double expanseX = maxX - minX;
			double expanseY = maxY - minY;

			nodes = (QuadTree<T>[])(new QuadTree[4]);
			numObjects = 0;
			objects = (T[])(new IHasAABB[objects.length]);;

			if (dirX <= 0 && dirY <= 0) {
				nodes[1] = thisAsNode;
				aabb = new AABB(minX - expanseX,
						minY - expanseY, maxX, maxY);
			} else if (dirX <= 0 && dirY > 0) {
				nodes[3] = thisAsNode;
				aabb = new AABB(minX - expanseX, minY, maxX,
						maxY + expanseY);

			} else if (dirX > 0 && dirY > 0) {
				nodes[2] = thisAsNode;
				aabb = new AABB(minX, minY, maxX + expanseX,
						maxY + expanseY);

			} else if (dirX > 0 && dirY <= 0) {
				nodes[0] = thisAsNode;
				aabb = new AABB(minX, minY - expanseY, maxX
						+ expanseX, maxY);
			} else {
				throw new AssertionError(
						"Error: QuadTree direction is invalid (?): "
								+ dirX + " (dirX) " + dirY
								+ " (dirY)");
			}

			add(obj);
		}
	}

	private void addToChild(T obj) {
		double minX = aabb.getMinX();
		double minY = aabb.getMinY();
		double maxX = aabb.getMaxX();
		double maxY = aabb.getMaxY();

		double halfXLength = (maxX - minX) / 2.0f;
		double halfYLength = (maxY - minY) / 2.0f;

		minY += halfYLength;
		maxX -= halfXLength;

		tryToAddToChildNode(obj, minX, minY, maxX, maxY, 0);

		minX += halfXLength;
		maxX += halfXLength;

		tryToAddToChildNode(obj, minX, minY, maxX, maxY, 1);

		minY -= halfYLength;
		maxY -= halfYLength;

		tryToAddToChildNode(obj, minX, minY, maxX, maxY, 3);

		minX -= halfXLength;
		maxX -= halfXLength;

		tryToAddToChildNode(obj, minX, minY, maxX, maxY, 2);
	}

	private void tryToAddToChildNode(T obj, double minX,
			double minY, double maxX, double maxY, int nodeIndex) {
		if (obj.getAABB().intersectRect(minX, minY, maxX,
				maxY)) {
			if (nodes[nodeIndex] == null) {
				nodes[nodeIndex] = new QuadTree<T>(new AABB(minX,
						minY, maxX, maxY), objects.length);
			}

			nodes[nodeIndex].add(obj);
		}
	}

	@Override
	public boolean remove(T obj) {
		if (!obj.getAABB().intersects(aabb)) {
			return false;
		}

		for (int i = 0; i < numObjects; i++) {
			if (objects[i] == obj) {
				removeEntityFromList(i);
			}
		}

		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null && nodes[i].remove(obj)) {
				nodes[i] = null;
			}
		}

		return isThisNodeEmpty();
	}

	private boolean isThisNodeEmpty() {
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				return false;
			}
		}

		return numObjects == 0;
	}

	private void removeEntityFromList(int index) {
		for (int i = index + 1; i < numObjects; i++) {
			objects[i - 1] = objects[i];
		}
		objects[numObjects - 1] = null;
		numObjects--;
	}
	
	@Override
	public Set<T> getAll(Set<T> result) {
		return queryRange(result, aabb);
	}

	@Override
	public Set<T> queryRange(Set<T> result, AABB aabb) {
		if (!aabb.intersects(aabb)) {
			return result;
		}

		for (int i = 0; i < numObjects; i++) {
			if (objects[i].getAABB().intersects(aabb)) {
				result.add(objects[i]);
			}
		}

		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				nodes[i].queryRange(result, aabb);
			}
		}

		return result;
	}
}
