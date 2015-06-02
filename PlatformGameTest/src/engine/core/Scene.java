package engine.core;

import java.awt.event.KeyEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.Arrays;

import engine.rendering.IRenderContext;
import engine.rendering.IBitmap;
import engine.rendering.ArrayBitmap;
import engine.rendering.ARGBColor;
import engine.parsing.Config;
import engine.core.space.ISpatialStructure;
import engine.core.space.AABB;
import engine.core.space.Grid;

import game.components.PlayerComponent;
import game.components.EnemyComponent;
import game.components.CollectableComponent;

public class Scene {
	private ISpatialStructure<Entity> structure;

	public Scene(ISpatialStructure<Entity> structure) {
		this.structure = structure;
	}

	protected void updateRange(double delta, AABB range) {
		Set<Entity> entities = structure.queryRange(
				new HashSet<Entity>(),
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

	public void update(double delta) {
	}

	protected void renderRange(IRenderContext target,
			double viewportX, double viewportY) {
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
				new HashSet<Entity>(),
				new AABB(viewportX, viewportY, 
					viewportX + target.getWidth(), 
					viewportY + target.getHeight()));
		
		Entity[] entities = renderableEntities.toArray(
				new Entity[renderableEntities.size()]);
		Arrays.sort(entities, spriteSorter);

		for(int i = 0; i < entities.length; i++) {
			Entity entity = entities[i];
			entity.render(target, 
					(int)Math.round(viewportX),
					(int)Math.round(viewportY));
		}
	}

	public void render(IRenderContext target) {
	}
}

