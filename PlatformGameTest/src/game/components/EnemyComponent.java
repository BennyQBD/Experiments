package game.components;

import engine.rendering.IBitmap;
import engine.rendering.IRenderContext;
import engine.core.*;

public class EnemyComponent extends EntityComponent {
	public static final String COMPONENT_NAME = "EnemyComponent";
	private static final double REMOVE_DELAY = 0.1;
	private static final int CLIFF_LOOKDOWN_DIST = 4;
	private static final int STATE_WALK = 0;
	private static final int STATE_DYING = 1;
	private int state;
	private double velY;
	private double speedX;
	private double lastRenderCounter;
	private SpriteComponent spriteComponent;

	private SpriteComponent getSpriteComponent() {
		if(spriteComponent != null) {
			return spriteComponent;
		}
		
		spriteComponent = (SpriteComponent)
			getEntity().getComponent(SpriteComponent.COMPONENT_NAME);
		return spriteComponent;
	}


	public EnemyComponent(Entity entity) {
		super(entity, COMPONENT_NAME);
		this.velY = 0.0;
		this.speedX = 30.0;
		this.state = STATE_WALK;
		this.lastRenderCounter = 0.0;
	}

	@Override
	public void update(double delta) {
		switch(state) {
			case STATE_WALK: walkUpdate(delta); break;
			case STATE_DYING: if(dyingUpdate(delta)) { return; }; break;
			default:
				throw new AssertionError("State " + state + " is an invalid enemy state");
		}
		applyGravity(157.0, delta);	
	}

	private void walkUpdate(double delta) {
		float newMoveX = (float)(speedX*delta);
		float moveX = getEntity().move(newMoveX, 0.0f);
		if(moveX != newMoveX ||
				aboutToWalkOffCliff(getEntity().getAABB().getWidth() *
					speedX/Math.abs(speedX), CLIFF_LOOKDOWN_DIST)) {
			speedX = -speedX;
			tryHitPlayer();
		}
	}

	private class DoubleVal {
		public double val = 0.0;
	}

	private boolean aboutToWalkOffCliff(double distX, double distY) {
		final DoubleVal val = new DoubleVal();
		getEntity().visitInRange(null,
				getEntity().getAABB().move(distX, distY), new IEntityVisitor() {
			@Override
			public void visit(Entity entity, EntityComponent component) {
				if(entity != getEntity() && entity.getBlocking()) {
					val.val = 1.0;
				}
			}
		});
		return val.val != 1.0;
	}

	private boolean dyingUpdate(double delta) {
		if(lastRenderCounter > REMOVE_DELAY) {
			getEntity().remove();
			return true;
		}
		lastRenderCounter += delta;
		return false;
	}

	public void kill() {
		state = STATE_DYING;
		velY = -100;
		getEntity().setBlocking(false);
		getSpriteComponent().setFlipY(true);
	}

	public boolean isLiving() {
		return state != STATE_DYING;
	}

	private void tryHitPlayer() {
		getEntity().visitInRange(PlayerComponent.COMPONENT_NAME,
				getEntity().getAABB().expand(1, 0, 0), new IEntityVisitor() {
			@Override
			public void visit(Entity entity, EntityComponent component) {
				((PlayerComponent)component).damage();
			}
		});
	}

	private void applyGravity(double gravity, double delta) {
		velY += gravity * delta;
		float newMoveY = (float)(velY * delta);
		float moveY = getEntity().move(0, newMoveY);
		if(newMoveY != moveY) {
			velY = 0;
		}
	}

	@Override
	public void render(IRenderContext target, int viewportX, int viewportY) {
		lastRenderCounter = 0.0;
	}
}
