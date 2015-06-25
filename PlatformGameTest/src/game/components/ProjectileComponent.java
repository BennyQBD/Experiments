package game.components;

import engine.components.ColliderComponent;
import engine.components.CollisionComponent;
import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.core.entity.IEntityVisitor;
import engine.rendering.IRenderContext;
import engine.space.AABB;
import engine.util.IDAssigner;

public class ProjectileComponent extends EntityComponent {
	public static final int ID = IDAssigner.getId();
	private double velX;
	private double velY;
	private final double gravity;
	private final double offscreenRemoveTime;
	private double timeSinceRender = 0.0;

	public ProjectileComponent(Entity entity, double velX, double velY,
			double gravity, double offscreenRemoveTime) {
		super(entity, ID);
		this.velX = velX;
		this.velY = velY;
		this.gravity = gravity;
		this.offscreenRemoveTime = offscreenRemoveTime;
	}

	@Override
	public void update(double delta) {
		removeOnCollision();
		velY += gravity * delta;
		float newMoveX = (float) (delta * velX);
		float newMoveY = (float) (delta * velY);

		getEntity().move(newMoveX, 0);
		getEntity().move(0, newMoveY);

		removeIfOffscreenTooLong(delta);
	}

	private void removeIfOffscreenTooLong(double delta) {
		if(timeSinceRender >= offscreenRemoveTime) {
			getEntity().forceRemove();
		} else {
			timeSinceRender += delta;
		}
	}

	@Override
	public void render(IRenderContext target, double viewportX, double viewportY) {
		timeSinceRender = 0.0;
	}

	private void remove() {
		getEntity().remove();
		getEntity().remove(this);
	}

	private void removeOnCollision() {
		ColliderComponent c = (ColliderComponent) getEntity().getComponent(
				ColliderComponent.ID);
		final AABB collider = c.getAABB();
		getEntity().visitInRange(CollisionComponent.ID, collider,
				new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						ColliderComponent c2 = (ColliderComponent) entity
								.getComponent(ColliderComponent.ID);
						AABB otherCollider = c2.getAABB();

						if (otherCollider.intersects(collider)) {
							remove();
						}
					}
				});
	}

}
