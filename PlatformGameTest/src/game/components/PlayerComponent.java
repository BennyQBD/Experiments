package game.components;

import engine.components.AudioComponent;
import engine.components.SpriteComponent;
import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.core.entity.IEntityVisitor;
import engine.input.Control;
import engine.input.IInput;
import engine.util.IDAssigner;
import engine.util.Util;
import engine.util.parsing.Config;

public class PlayerComponent extends EntityComponent {
	private class DoubleVal {
		public double val = 0.0;
	}

	public static final int ID = IDAssigner.getId();

	private static final int STATE_MOVING = 0;
	private static final int STATE_IN_AIR = 1;
	private Control leftKey;
	private Control rightKey;
	private Control runKey;
	private Control jumpKey;
	private Control slamKey;
	private int state;
	private int lastState;

	private double velY;
	private double velX;
	private double jumpCounter;
	private int health;
	private double invulnerabilityTimer;
	private final double moveSpeed;
	private final double maxBounceVelocity;
	private final double invulnerabilityLength;
	private final double jumpTime;
	private final double runModifier;
	private final double jumpSpeed;
	private final double gravity;
	private final double jumpModifier;
	private final double flashFrequency;
	private final double moveAccel;
	private final double frictionAmt;
	private final double frictionAirRatio;

	private final int standAnimationFrame;
	private final int glideAnimationFrame;
	private final int hoverAnimationFrame;

	private SpriteComponent spriteComponent;
	private AudioComponent audioComponent;

	private AudioComponent getAudioComponent() {
		if (audioComponent != null) {
			return audioComponent;
		}

		audioComponent = (AudioComponent) getEntity().getComponent(
				AudioComponent.ID);
		return audioComponent;
	}

	private SpriteComponent getSpriteComponent() {
		if (spriteComponent != null) {
			return spriteComponent;
		}

		spriteComponent = (SpriteComponent) getEntity().getComponent(
				SpriteComponent.ID);
		return spriteComponent;
	}

	public PlayerComponent(Entity entity, IInput input, Config config) {
		super(entity, ID);
		this.leftKey = new Control(input, config, "player.leftKey.");
		this.rightKey = new Control(input, config, "player.rightKey.");
		this.runKey = new Control(input, config, "player.runKey.");
		this.jumpKey = new Control(input, config, "player.jumpKey.");
		this.slamKey = new Control(input, config, "player.slamKey.");
		this.state = STATE_MOVING;

		velY = 0.0;
		velX = 0.0;
		jumpCounter = 0.0;
		this.invulnerabilityLength = config
				.getDouble("player.invulnerabilityLength");
		this.health = config.getInt("player.health");

		this.invulnerabilityTimer = invulnerabilityLength;
		spriteComponent = null;

		this.jumpTime = config.getDouble("player.jumpTime");
		this.jumpSpeed = config.getDouble("player.jumpSpeed");
		this.maxBounceVelocity = config.getDouble("player.maxBounceVelocity");
		this.moveSpeed = config.getDouble("player.moveSpeed");
		this.runModifier = config.getDouble("player.runModifier");
		this.gravity = config.getDouble("player.gravity");
		this.jumpModifier = config.getDouble("player.jumpModifier");
		this.flashFrequency = config.getDouble("player.flashFrequency");

		this.moveAccel = config.getDouble("player.moveAccel");
		this.frictionAmt = config.getDouble("player.frictionAmt");
		this.frictionAirRatio = config.getDouble("player.frictionAirRatio");

		this.standAnimationFrame = config.getInt("player.standAnimationFrame");
		this.glideAnimationFrame = config.getInt("player.glideAnimationFrame");
		this.hoverAnimationFrame = config.getInt("player.hoverAnimationFrame");
		this.lastState = state;

		getSpriteComponent().setFrame(standAnimationFrame);
	}

	public void damage() {
		if (!isInvulnerable()) {
			health--;
			invulnerabilityTimer = 0.0;
			getAudioComponent().play("hit");
		}
	}

	public int getHealth() {
		return health;
	}

	@Override
	public void update(double delta) {

		boolean jumped = commonUpdate(delta);
		lastState = state;
		switch (state) {
		case STATE_MOVING:
			movingUpdate(delta, jumped);
			break;
		case STATE_IN_AIR:
			inAirUpdate(delta);
			break;
		default:
			throw new AssertionError("State " + state
					+ " is an invalid enemy state");
		}
	}

	private boolean commonUpdate(double delta) {
		invulnerabilityFlash(delta);
		float moveX = applyLateralMovement(moveSpeed, runModifier, delta);
		applySlam(delta, jumpSpeed);
		applyGravity(gravity, delta);
		return applyJumpAmt(jumpSpeed, moveX, delta);
	}

	private void movingUpdate(double delta, boolean jumped) {
		if (applyMovementY(delta)) {
			state = STATE_IN_AIR;
			jumpCounter = jumped ? delta : jumpTime;
			if (jumped) {
				getAudioComponent().play("jump");
			}
		} else {
			velY = 0.0;
			jumpCounter = 0.0;
		}
	}

	private void inAirUpdate(double delta) {
		if (!applyMovementY(delta) && !tryHitEnemy()) {
			if (velY > 0) {
				state = STATE_MOVING;
			}
			velY = 0.0;
		}
	}

	private float findLateralMovement(double moveSpeed, double runModifier,
			double delta) {
		float moveX = 0;
		double speed = moveSpeed * delta;
		if (runKey.isDown()) {
			speed *= runModifier;
		}

		if (rightKey.isDown()) {
			moveX += (float) speed;
			getSpriteComponent().setFlipX(false);
			getSpriteComponent().setFrame(glideAnimationFrame);
		}
		if (leftKey.isDown()) {
			moveX -= (float) speed;
			getSpriteComponent().setFlipX(true);
			getSpriteComponent().setFrame(glideAnimationFrame);
		}

		if (moveX == 0.0f) {
			if (state == STATE_MOVING) {
				getSpriteComponent().setFrame(standAnimationFrame);
			} else {
				getSpriteComponent().setFrame(hoverAnimationFrame);
			}
		}

		return moveX;
	}

	private boolean isInvulnerable() {
		return invulnerabilityTimer < invulnerabilityLength;
	}

	private void invulnerabilityFlash(double delta) {
		if (isInvulnerable()) {
			invulnerabilityTimer += delta;
			double invulnerabilityFlasher = invulnerabilityTimer
					* flashFrequency;
			if ((int) (invulnerabilityFlasher) % 2 == 0) {
				getSpriteComponent().setTransparency(1.0);
			} else {
				getSpriteComponent().setTransparency(0.0);
			}
		} else {
			getSpriteComponent().setTransparency(1.0);
		}
	}

	private boolean applyJumpAmt(double jumpSpeed, float moveX, double delta) {
		delta = updateJumpCounter(delta);
		if (delta == 0.0) {
			return false;
		}

		if (runKey.isDown() && moveX != 0.0f) {
			jumpSpeed *= jumpModifier;
		}

		if (jumpKey.isDown() && !slamKey.isDown()) {
			velY -= jumpSpeed * delta;
			return true;
		}
		return false;
	}

	private void applyFriction(double delta) {
		double friction = 0;

		if (velX > 0) {
			friction = delta * frictionAmt;
		} else if (velX < 0) {
			friction = -delta * frictionAmt;
		}

		if (state != STATE_MOVING) {
			friction *= frictionAirRatio;
		}

		boolean sign = velX > 0;
		velX -= friction;
		if (velX > 0 != sign) {
			velX = 0;
		}
	}

	private float applyLateralMovement(double moveSpeed, double runModifier,
			double delta) {
		double maxSpeed = moveSpeed * delta;
		if (runKey.isDown()) {
			maxSpeed *= runModifier;
		}

		float moveX = findLateralMovement(moveSpeed, runModifier, delta);
		moveX *= delta * moveAccel;
		velX += moveX;

		applyFriction(delta);

		velX = Util.clamp(velX, -maxSpeed, maxSpeed);
		moveX = (float) velX;
		float result = getEntity().move(moveX, 0);
		if (result != moveX) {
			velX = 0.0;
		}
		return result;
	}

	private double updateJumpCounter(double amt) {
		double newJumpCounter = jumpCounter + amt;
		double jumpDelta = amt;
		if (newJumpCounter >= jumpTime) {
			jumpDelta = jumpTime - jumpCounter;
			if (jumpDelta < 0.0) {
				jumpDelta = 0.0;
			}
		}
		jumpCounter = newJumpCounter;
		return jumpDelta;
	}

	private void applySlam(double jumpSpeed, double delta) {
		if (slamKey.isDown()) {
			velY += jumpSpeed * 0.8 * delta;
			getSpriteComponent().setFlipY(true);
			if(lastState != STATE_MOVING && state == STATE_MOVING) {
				getAudioComponent().play("thunk");
			}
		} else {
			getSpriteComponent().setFlipY(false);
			getAudioComponent().stop("thunk");
		}
	}

	private boolean applyMovementY(double delta) {
		float newMoveY = (float) (velY * delta);
		float moveY = getEntity().move(0, newMoveY);
		return newMoveY == moveY;
	}

	private void applyGravity(double gravity, double delta) {
		velY += gravity * delta;
	}

	private boolean tryHitEnemy() {
		final DoubleVal result = new DoubleVal();
		getEntity().visitInRange(EnemyComponent.ID,
				getEntity().getAABB().expand(0, 1, 0), new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						if (((EnemyComponent) component).kill()) {
							result.val += ((EnemyComponent) component)
									.getPoints();
						}
					}
				});
		((InventoryComponent) (getEntity().getComponent(InventoryComponent.ID)))
				.addPoints((int) (result.val));
		boolean hitEnemy = result.val != 0.0;
		if (hitEnemy) {
			velY = -velY;
			if (velY < maxBounceVelocity) {
				velY = maxBounceVelocity;
			}
		}
		return hitEnemy;
	}

}
