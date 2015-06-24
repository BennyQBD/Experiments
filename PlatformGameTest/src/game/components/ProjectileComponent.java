package game.components;

import engine.components.ColliderComponent;
import engine.components.CollisionComponent;
import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.core.entity.IEntityVisitor;
import engine.space.AABB;
import engine.util.IDAssigner;

public class ProjectileComponent extends EntityComponent {
	public static final int ID = IDAssigner.getId();
	private double velX;
	private double velY;
	private final double gravity;
	
	public ProjectileComponent(Entity entity, double velX, double velY, double gravity) {
		super(entity, ID);
		this.velX = velX;
		this.velY = velY;
		this.gravity = gravity;
	}
	
	@Override
	public void update(double delta) {
		velY += gravity * delta;
		float newMoveX = (float)(delta * velX);
		float newMoveY = (float)(delta * velY);
		
		getEntity().move(newMoveX, 0);
		getEntity().move(0, newMoveY);
		
		removeOnCollision();
	}
	
	private void removeOnCollision() {
		final EntityComponent thisEntity = this;
		ColliderComponent c = (ColliderComponent)getEntity().getComponent(ColliderComponent.ID);
		final AABB collider = c.getAABB();
		getEntity().visitInRange(CollisionComponent.ID, collider, new IEntityVisitor() {
			@Override
			public void visit(Entity entity, EntityComponent component) {
				ColliderComponent c2 = (ColliderComponent)entity.getComponent(ColliderComponent.ID);
				AABB otherCollider = c2.getAABB();
				
				if(otherCollider.intersects(collider)) {
					getEntity().remove();
					getEntity().remove(thisEntity);
				}
			}
		});
	}

}
