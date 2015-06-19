package game.components;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.util.IDAssigner;

public class CollectableComponent extends EntityComponent {
	public static final int ID = IDAssigner.getId();
	private int lives;
	private int health;
	private int points;
	private int checkpoint;
	private int itemId;

	public CollectableComponent(Entity entity, int points, int health, int lives, int checkpoint, int itemId) {
		super(entity, ID);
		this.points = points;
		this.health = health;
		this.lives = lives;
		this.checkpoint = checkpoint;
		this.itemId = itemId;
	}

	public int getPoints() {
		return points;
	}
	
	public int getHealth() {
		return health;
	}
	
	public int getLives() {
		return lives;
	}
	
	public int getCheckpoint() {
		return checkpoint;
	}
	
	public int getItemId() {
		return itemId;
	}
}
