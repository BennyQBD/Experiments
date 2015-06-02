package engine.core;

import engine.rendering.IBitmap;

public class CollectableComponent extends EntityComponent {
	public static final String COMPONENT_NAME = "CollectableComponent";
	private int points;

	public CollectableComponent(Entity entity, int points) {
		super(entity, COMPONENT_NAME);
		this.points = points;
	}

	public int getPoints() {
		return points;
	}
}
