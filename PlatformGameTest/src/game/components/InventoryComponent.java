package game.components;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.core.entity.IEntityVisitor;
import engine.util.IDAssigner;

public class InventoryComponent extends EntityComponent {
	public static final int ID = IDAssigner.getId();

	private Set<Integer> itemIds;
	private int points;
	private int maxHealth;
	private int health;
	private int lives;
	private int lifeDeficit;
	private int checkpoint;
	private int pointsForExtraLife;

	public InventoryComponent(Entity entity) {
		this(entity, 0, 0, 0, 0, 0, 0, 0);
	}
	
	public InventoryComponent(Entity entity, int points, int health, int maxHealth, int lives,
			int lifeDeficit, int checkpoint, int pointsForExtraLife) {
		super(entity, ID);
		itemIds = new TreeSet<Integer>();
		this.points = points;
		this.health = health;
		this.maxHealth = maxHealth;
		this.lives = lives;
		this.lifeDeficit = lifeDeficit;
		this.checkpoint = checkpoint;
		this.pointsForExtraLife = pointsForExtraLife;
	}
	
	public Iterator<Integer> getItemIterator() {
		return itemIds.iterator();
	}

	private boolean removeItem(int id) {
		return itemIds.remove(id);
	}

	private boolean hasItem(int id) {
		return itemIds.contains(id);
	}

	@Override
	public void update(double delta) {
		pickupItems();
		unlockObjects();
	}

	private void unlockObjects() {
		getEntity().visitInRange(UnlockComponent.ID, getEntity().getAABB().expand(1, 1, 0),
				new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						UnlockComponent c = (UnlockComponent) component;
						if (c.getUnlockId() == 0 || hasItem(c.getUnlockId())) {
							removeItem(c.getUnlockId());
							entity.remove();
						}
					}
				});
	}

	private void pickupItems() {
		getEntity().visitInRange(CollectableComponent.ID,
				getEntity().getAABB(), new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						CollectableComponent c = (CollectableComponent) component;
						int id = c.getItemId();
						if (id != 0) {
							if(!hasItem(id)) {
								itemIds.add(id);
								pickupItem(entity, c);
							}
						} else if(c.getHealth() <= 0 || health < maxHealth) {
							pickupItem(entity, c);
						}
					}
				});
	}
	
	private void pickupItem(Entity e, CollectableComponent c) {
		addPoints(c.getPoints());
		addHealth(c.getHealth());
		lives += c.getLives();
		if(c.getCheckpoint() > checkpoint) {
			checkpoint = c.getCheckpoint();
		}
		e.remove();
	}

	public void addLives(int numLives) {
		lifeDeficit += numLives;
		if (lifeDeficit > 0) {
			lives += lifeDeficit;
			lifeDeficit = 0;
		}
	}
	
	public void addHealth(int amt) {
		health += amt;
		if(health > maxHealth) {
			health = maxHealth;
		}
	}

	public void addPoints(int amt) {
		if(pointsForExtraLife == 0) {
			points += amt;
			return;
		}
		int livesFromPointsBefore = points / pointsForExtraLife;
		points += amt;
		int extraLives = points / pointsForExtraLife - livesFromPointsBefore;
		if (extraLives > 0) {
			addLives(extraLives);
		} else {
			lifeDeficit += extraLives;
		}
	}

	public int getLifeDeficit() {
		return lifeDeficit;
	}

	public int getLives() {
		return lives;
	}
	
	public int getHealth() {
		return health;
	}

	public int getPoints() {
		return points;
	}
	
	public int getCheckpoint() {
		return checkpoint;
	}
}
