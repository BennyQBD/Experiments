package game.components;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.util.IDAssigner;

public class UnlockComponent extends EntityComponent {
	public static final int ID = IDAssigner.getId();
	private int unlockId;
	
	public UnlockComponent(Entity entity, int unlockId) {
		super(entity, ID);
		this.unlockId = unlockId;
	}
	
	public int getUnlockId() {
		return unlockId;
	}
}
