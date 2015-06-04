package game.components;

import engine.core.Entity;
import engine.core.EntityComponent;
import engine.core.IEntityVisitor;
import engine.core.SpriteComponent;
import engine.rendering.IRenderContext;

public class EnemyComponent extends EntityComponent {
	private class DoubleVal {
		public double val = 0.0;
	}
	
	public static final String COMPONENT_NAME = "EnemyComponent";
	private static final int STATE_WALK = 0;
	private static final int STATE_DYING = 1;
	private int state;
	private int points;
	private double velY;
	private double lastRenderCounter;
	private double speedX;
	private final double removeDelay;
	private final double gravity;
	private final double cliffLookDownDistance;
	private final double killBounceSpeed;
	private SpriteComponent spriteComponent;

	private SpriteComponent getSpriteComponent() {
		if (spriteComponent != null) {
			return spriteComponent;
		}

		spriteComponent = (SpriteComponent) getEntity().getComponent(
				SpriteComponent.COMPONENT_NAME);
		return spriteComponent;
	}

	public EnemyComponent(Entity entity, int points) {
		super(entity, COMPONENT_NAME);
		this.velY = 0.0;
		this.state = STATE_WALK;
		this.lastRenderCounter = 0.0;
		this.points = points;
		this.speedX = 30.0;
		this.gravity = 157.0;
		this.removeDelay = 0.1;
		this.cliffLookDownDistance = 4;
		this.killBounceSpeed = -100;
	}
	
	public int getPoints() {
		return points;
	}
	
	public boolean kill() {
		if (!isLiving()) {
			return false;
		}
		state = STATE_DYING;
		velY = killBounceSpeed;
		getEntity().setBlocking(false);
		getSpriteComponent().setFlipY(true);
		return true;
	}

	@Override
	public void update(double delta) {
		switch (state) {
		case STATE_WALK:
			walkUpdate(delta);
			break;
		case STATE_DYING:
			if (dyingUpdate(delta)) {
				return;
			}
			break;
		default:
			throw new AssertionError("State " + state
					+ " is an invalid enemy state");
		}
		applyGravity(gravity, delta);
		if(!applyMovementY(delta)) {
			velY = 0.0;
		}
	}
	
	@Override
	public void render(IRenderContext target, int viewportX, int viewportY) {
		lastRenderCounter = 0.0;
	}

	private void walkUpdate(double delta) {
		float newMoveX = (float) (speedX * delta);
		float moveX = getEntity().move(newMoveX, 0.0f);
		if (moveX != newMoveX
				|| aboutToWalkOffCliff(getEntity().getAABB().getWidth()
						* speedX / Math.abs(speedX), cliffLookDownDistance)) {
			speedX = -speedX;
			getSpriteComponent().setFlipX(speedX < 0);
		}
		tryHitPlayer();
	}
	
	private boolean dyingUpdate(double delta) {
		if (this.lastRenderCounter > removeDelay) {
			getEntity().remove();
			return true;
		}
		lastRenderCounter += delta;
		return false;
	}
	
	private boolean applyMovementY(double delta) {
		float newMoveY = (float) (velY * delta);
		float moveY = getEntity().move(0, newMoveY);
		return newMoveY == moveY;
	}

	private void applyGravity(double gravity, double delta) {
		velY += gravity * delta;
	}
	
	private boolean isLiving() {
		return state != STATE_DYING;
	}

	private boolean aboutToWalkOffCliff(double distX, double distY) {
		final DoubleVal val = new DoubleVal();
		getEntity().visitInRange(null,
				getEntity().getAABB().move(distX, distY), new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						if (entity != getEntity() && entity.getBlocking()) {
							val.val = 1.0;
						}
					}
				});
		return val.val != 1.0;
	}

	private void tryHitPlayer() {
		getEntity().visitInRange(PlayerComponent.COMPONENT_NAME,
				getEntity().getAABB().expand(1, 0, 0), new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						((PlayerComponent) component).damage();
					}
				});
	}
}
