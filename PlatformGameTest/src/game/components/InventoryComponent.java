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

	private static final int POINTS_FOR_EXTRA_LIFE = 1000;
	private Set<Integer> itemIds;
	private int points;
	private int lives;
	private int lifeDeficit;

	public InventoryComponent(Entity entity, int points, int lives,
			int lifeDeficit) {
		super(entity, ID);
		itemIds = new TreeSet<Integer>();
		this.points = points;
		this.lives = lives;
		this.lifeDeficit = lifeDeficit;
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
						} else {
							pickupItem(entity, c);
						}
					}
				});
	}
	
	private void pickupItem(Entity e, CollectableComponent c) {
		addPoints(c.getPoints());
		lives += c.getLives();
		e.remove();
	}

	public void addLives(int numLives) {
		lifeDeficit += numLives;
		if (lifeDeficit > 0) {
			lives += lifeDeficit;
			lifeDeficit = 0;
		}
	}

	public void addPoints(int amt) {
		int livesFromPointsBefore = points / POINTS_FOR_EXTRA_LIFE;
		points += amt;
		int extraLives = points / POINTS_FOR_EXTRA_LIFE - livesFromPointsBefore;
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

	public int getPoints() {
		return points;
	}
}
