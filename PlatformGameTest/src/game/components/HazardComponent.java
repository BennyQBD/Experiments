package game.components;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.core.entity.IEntityVisitor;
import engine.util.IDAssigner;

public class HazardComponent extends EntityComponent {
	public static final int ID = IDAssigner.getId();
	private double spaceX;
	private double spaceY;
	private double spaceZ;

	public HazardComponent(Entity entity, double spaceX, double spaceY,
			double spaceZ) {
		super(entity, ID);
		this.spaceX = spaceX;
		this.spaceY = spaceY;
		this.spaceZ = spaceZ;
	}

	@Override
	public void update(double delta) {
		getEntity().visitInRange(PlayerComponent.ID,
				getEntity().getAABB().expand(spaceX, spaceY, spaceZ),
				new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						((PlayerComponent) component).damage();
					}
				});
	}
}
