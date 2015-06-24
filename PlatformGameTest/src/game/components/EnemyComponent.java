package game.components;

import engine.components.AudioComponent;
import engine.components.ColliderComponent;
import engine.components.CollisionComponent;
import engine.components.SpriteComponent;
import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.core.entity.IEntityVisitor;
import engine.rendering.IRenderContext;
import engine.util.Delay;
import engine.util.IDAssigner;
import engine.util.parsing.Config;
import game.PlatformLevel;

public class EnemyComponent extends EntityComponent {
	private class DoubleVal {
		public double val = 0.0;
	}

	public static final int ID = IDAssigner.getId();
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
	private final Delay shootDelay;
	private final PlatformLevel level;
	private SpriteComponent spriteComponent;
	private AudioComponent audioComponent;
	private ColliderComponent colliderComponent;
	private final String projectileName;

	private final double projectileOffsetX;
	private final double projectileOffsetY;
	private final double projectileSpeedX;
	private final double projectileSpeedY;
	private final double projectileGravity;

	private ColliderComponent getColliderComponent() {
		if (colliderComponent != null) {
			return colliderComponent;
		}

		colliderComponent = (ColliderComponent) getEntity().getComponent(
				ColliderComponent.ID);
		return colliderComponent;
	}

	private SpriteComponent getSpriteComponent() {
		if (spriteComponent != null) {
			return spriteComponent;
		}

		spriteComponent = (SpriteComponent) getEntity().getComponent(
				SpriteComponent.ID);
		return spriteComponent;
	}

	private AudioComponent getAudioComponent() {
		if (audioComponent != null) {
			return audioComponent;
		}

		audioComponent = (AudioComponent) getEntity().getComponent(
				AudioComponent.ID);
		return audioComponent;
	}

	public EnemyComponent(Entity entity, Config config, String type,
			PlatformLevel level) {
		super(entity, ID);
		this.velY = 0.0;
		this.state = STATE_WALK;
		this.lastRenderCounter = 0.0;
		this.points = config.getIntWithDefault("enemy." + type + ".points", "enemy.default.points");
		this.speedX = config.getDoubleWithDefault("enemy." + type + ".speedX", "enemy.default.speedX");
		this.gravity = config.getDoubleWithDefault("enemy." + type + ".gravity", "enemy.default.gravity");
		this.removeDelay = config.getDoubleWithDefault("enemy." + type + ".removeDelay", "enemy.default.removeDelay");
		this.cliffLookDownDistance = config.getDoubleWithDefault("enemy." + type
				+ ".cliffLookDownDistance", "enemy.default.cliffLookDownDistance");
		this.killBounceSpeed = config.getDoubleWithDefault("enemy." + type
				+ ".killBounceSpeed", "enemy.default.killBounceSpeed");
		double shootDelayAmt = config.getDoubleWithDefault("enemy." + type
				+ ".shootDelay", "enemy.default.shootDelay");
		if(shootDelayAmt != 0.0) {
			this.shootDelay = new Delay(shootDelayAmt);
		} else {
			this.shootDelay = null;
		}
		this.level = level;
		this.projectileName = config.getStringWithDefault("enemy." + type + ".projectile", "enemy.default.projectile");

		this.projectileOffsetX = config.getDoubleWithDefault("enemy." + type
				+ ".projectile.offsetX", "enemy.default.projectile.offsetX");
		this.projectileOffsetY = config.getDoubleWithDefault("enemy." + type
				+ ".projectile.offsetY", "enemy.default.projectile.offsetY");
		this.projectileSpeedX = config.getDoubleWithDefault("enemy." + type
				+ ".projectile.speedX", "enemy.default.projectile.speedX");
		this.projectileSpeedY = config.getDoubleWithDefault("enemy." + type
				+ ".projectile.speedY", "enemy.default.projectile.speedY");
		this.projectileGravity = config.getDoubleWithDefault("enemy." + type
				+ ".projectile.gravity", "enemy.default.projectile.gravity");
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
		getEntity().remove(CollisionComponent.ID);
		getEntity().remove(HazardComponent.ID);
		getSpriteComponent().setFlipY(true);
		getAudioComponent().play("die");
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
		if (!applyMovementY(delta)) {
			velY = 0.0;
		}
	}

	@Override
	public void render(IRenderContext target, double viewportX, double viewportY) {
		lastRenderCounter = 0.0;
	}

	private void walkUpdate(double delta) {
		float newMoveX = (float) (speedX * delta);
		float moveX = getEntity().move(newMoveX, 0.0f);
		if (moveX != newMoveX
				|| aboutToWalkOffCliff(getColliderComponent().getAABB()
						.getWidth() * speedX / Math.abs(speedX),
						cliffLookDownDistance)) {
			speedX = -speedX;
			getSpriteComponent().setFlipX(speedX < 0);
		}

		if (shootDelay != null && shootDelay.over(delta)) {
			shoot();
			shootDelay.reset();
		}
	}

	private void shoot() {
		double x = getEntity().getX();
		double y = getEntity().getY();

		double projVelX = projectileSpeedX;
		double projVelY = projectileSpeedY;

		double shootOffsetX = getColliderComponent().getAABB().getWidth();

		if (speedX < 0) {
			projVelX = -projVelX;
			x -= shootOffsetX;
			x -= projectileOffsetX;
		} else {
			x += shootOffsetX;
			x += projectileOffsetX;
		}
		if (velY < 0) {
			projVelY = -projVelY;
		}
		y += projectileOffsetY;

		Entity e = level.parseEntity(x, y, 0, "enemy.projectile."
				+ projectileName + ".", false);
		if (e != null) {
			new ProjectileComponent(e, projVelX, projVelY, projectileGravity);
		}
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
		getEntity().visitInRange(CollisionComponent.ID,
				getColliderComponent().getAABB().move(distX, distY),
				new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						if (entity != getEntity()) {
							val.val = 1.0;
						}
					}
				});
		return val.val != 1.0;
	}
}
