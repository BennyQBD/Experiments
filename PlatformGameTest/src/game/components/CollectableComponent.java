package game.components;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.util.IDAssigner;

public class CollectableComponent extends EntityComponent {
	public static final int ID = IDAssigner.getId();
	private int points;

	public CollectableComponent(Entity entity, int points) {
		super(entity, ID);
		this.points = points;
	}

	public int getPoints() {
		return points;
	}
}
