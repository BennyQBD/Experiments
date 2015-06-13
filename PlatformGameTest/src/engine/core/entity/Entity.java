package engine.core.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import engine.rendering.IRenderContext;
import engine.space.AABB;
import engine.space.ISpatialObject;
import engine.space.ISpatialStructure;

public class Entity implements ISpatialObject {
	private ISpatialStructure<Entity> structure;
	private AABB aabb;
	private boolean isBlocking;
	private boolean isRemoved;
	private List<EntityComponent> components;

	public Entity(ISpatialStructure<Entity> structure, double posX,
			double posY, double posZ, boolean isBlocking) {
		this.structure = structure;
		this.aabb = new AABB(posX, posY, posZ, posX, posY);
		this.isBlocking = isBlocking;
		this.isRemoved = false;
		structure.add(this);
		components = new ArrayList<EntityComponent>();
	}

	public void fitAABB(AABB newAABB) {
		structure.remove(this);
		double width = aabb.getWidth();
		double height = aabb.getHeight();
		if (width < newAABB.getWidth()) {
			width = newAABB.getWidth();
		}
		if (height < newAABB.getHeight()) {
			height = newAABB.getHeight();
		}
		double newMinX = aabb.getMinX() + newAABB.getMinX();
		double newMinY = aabb.getMinY() + newAABB.getMinY();
		this.aabb = new AABB(newMinX, newMinY, aabb.getMinZ(), newMinX + width,
				newMinY + height);
		structure.add(this);
	}

	public EntityComponent getComponent(String name) {
		Iterator<EntityComponent> it = components.iterator();
		while (it.hasNext()) {
			EntityComponent current = it.next();
			if (current.getName().equals(name)) {
				return current;
			}

		}
		return null;
	}

	public void visitInRange(String name, AABB range, IEntityVisitor visitor) {
		Set<Entity> entities = structure.queryRange(new HashSet<Entity>(),
				range);
		Iterator<Entity> it = entities.iterator();
		while (it.hasNext()) {
			Entity entity = it.next();
			EntityComponent component = name == null ? null : entity
					.getComponent(name);
			if (component != null || name == null) {
				visitor.visit(entity, component);
			}
		}
	}

	public void add(EntityComponent component) {
		components.add(component);
	}

	public void remove(EntityComponent component) {
		components.remove(component);
	}

	public float move(float amtXIn, float amtYIn) {
		if (amtXIn != 0.0f && amtYIn != 0.0f) {
			throw new IllegalArgumentException(
					"Can only move in 1 dimension per call");
		}
		structure.remove(this);
		double amtX = (double) amtXIn;
		double amtY = (double) amtYIn;
		AABB newAABB = aabb.stretch(amtX, amtY);

		if (isBlocking) {
			Set<Entity> hitEntities = structure.queryRange(
					new HashSet<Entity>(), newAABB);
			Iterator<Entity> it = hitEntities.iterator();
			while (it.hasNext()) {
				Entity current = it.next();
				if (current == this || !current.getBlocking()) {
					continue;
				}

				if (current.getAABB().intersects(newAABB)) {
					amtX = aabb.resolveCollisionX(current.getAABB(), amtX);
					amtY = aabb.resolveCollisionY(current.getAABB(), amtY);
				}
			}
		}

		this.aabb = aabb.move(amtX, amtY);
		structure.add(this);
		if (amtX != 0) {
			return (float) amtX;
		} else {
			return (float) amtY;
		}
	}

	public void remove() {
		structure.remove(this);
		isRemoved = true;
	}

	public boolean getRemoved() {
		return isRemoved;
	}
	
	public boolean getBlocking() {
		return isBlocking;
	}

	public void setBlocking(boolean value) {
		this.isBlocking = value;
	}

	public void update(double delta) {
		Iterator<EntityComponent> it = components.iterator();
		while (it.hasNext()) {
			it.next().update(delta);
		}
	}

	public void render(IRenderContext target, int viewportX, int viewportY) {
		Iterator<EntityComponent> it = components.iterator();
		while (it.hasNext()) {
			it.next().render(target, viewportX, viewportY);
		}
	}

	@Override
	public AABB getAABB() {
		return aabb;
	}
}
