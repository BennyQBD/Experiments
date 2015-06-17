package game.components;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.util.IDAssigner;

public class CollectableComponent extends EntityComponent {
	public static final int ID = IDAssigner.getId();
	private int lives;
	private int points;
	private int itemId;

	public CollectableComponent(Entity entity, int points, int lives, int itemId) {
		super(entity, ID);
		this.points = points;
		this.lives = lives;
		this.itemId = itemId;
	}

	public int getPoints() {
		return points;
	}
	
	public int getLives() {
		return lives;
	}
	
	public int getItemId() {
		return itemId;
	}
}
