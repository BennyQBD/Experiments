package game.components;

import engine.rendering.IBitmap;
import engine.core.*;

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
