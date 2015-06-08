package engine.core;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import engine.core.entity.Entity;
import engine.rendering.IRenderContext;
import engine.space.AABB;
import engine.space.ISpatialStructure;

public abstract class Scene {
	private ISpatialStructure<Entity> structure;

	public Scene(ISpatialStructure<Entity> structure) {
		this.structure = structure;
	}

	protected void updateRange(double delta, AABB range) {
		Set<Entity> entities = structure.queryRange(new HashSet<Entity>(),
				range);

		Iterator<Entity> it = entities.iterator();
		while (it.hasNext()) {
			Entity current = (Entity) it.next();
			current.update(delta);
		}
	}

	protected ISpatialStructure<Entity> getStructure() {
		return structure;
	}

	public abstract boolean update(double delta);

	protected void renderRange(IRenderContext target, double viewportX,
			double viewportY) {
		Comparator<Entity> spriteSorter = new Comparator<Entity>() {
			public int compare(Entity e0, Entity e1) {
				if (e0.getAABB().getMinZ() > e1.getAABB().getMinZ()) {
					return 1;
				}
				if (e0.getAABB().getMinZ() < e1.getAABB().getMinZ()) {
					return -1;
				}
				return 0;

			}
		};

		Set<Entity> renderableEntities = structure.queryRange(
				new HashSet<Entity>(), new AABB(viewportX, viewportY, viewportX
						+ target.getWidth(), viewportY + target.getHeight()));

		Entity[] entities = renderableEntities
				.toArray(new Entity[renderableEntities.size()]);
		Arrays.sort(entities, spriteSorter);

		for (int i = 0; i < entities.length; i++) {
			Entity entity = entities[i];
			entity.render(target, (int) Math.round(viewportX),
					(int) Math.round(viewportY));
		}
	}

	public abstract void render(IRenderContext target);
}
