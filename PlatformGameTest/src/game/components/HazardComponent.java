package game.components;

import engine.components.ColliderComponent;
import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.core.entity.IEntityVisitor;
import engine.space.AABB;
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
		ColliderComponent c = (ColliderComponent) getEntity().getComponent(
				ColliderComponent.ID);
		AABB aabb = c != null ? c.getAABB().expand(spaceX, spaceY, spaceZ)
				: getEntity().translateAABB(
						new AABB(0, 0, 0, 0, 0, 0).expand(spaceX, spaceY,
								spaceZ));
		getEntity().visitInRange(PlayerComponent.ID, aabb,
				new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						((PlayerComponent) component).damage();
					}
				});
	}
}
