package engine.components;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.core.entity.IEntityVisitor;
import engine.space.AABB;
import engine.util.IDAssigner;

public class CollisionComponent extends EntityComponent {
	public class DoublePair {
		public DoublePair(double val1, double val2) {
			this.val1 = val1;
			this.val2 = val2;
		}

		private double val1;
		private double val2;

		public double getVal1() {
			return val1;
		}

		public double getVal2() {
			return val2;
		}
	}

	public static final int ID = IDAssigner.getId();

	public CollisionComponent(Entity entity) {
		super(entity, ID);
	}

	public DoublePair resolveCollisions(double amtX, double amtY) {
		final DoublePair amts = new DoublePair(amtX, amtY);
		ColliderComponent c = (ColliderComponent)getEntity().getComponent(ColliderComponent.ID);
		if(c == null) {
			return amts;
		}
		final AABB collider = c.getAABB();
		final AABB collisionRange = getEntity().getAABB().stretch(amtX, amtY);
		getEntity().visitInRange(CollisionComponent.ID, collisionRange,
				new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						if (entity == getEntity()) {
							return;
						}

						if (entity.getAABB().intersects(collisionRange)) {
							amts.val1 = collider.resolveCollisionX(
									entity.getAABB(), amts.val1);
							amts.val2 = collider.resolveCollisionY(
									entity.getAABB(), amts.val2);
						}
					}
				});
		return amts;
	}
}
