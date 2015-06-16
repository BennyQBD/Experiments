package game.components;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.core.entity.IEntityVisitor;
import engine.input.InputListener;
import engine.util.IDAssigner;
import engine.util.Util;
import engine.util.components.SpriteComponent;

public class PlayerComponent extends EntityComponent {
	private class DoubleVal {
		public double val = 0.0;
	}

	public static final int ID = IDAssigner.getId();
	private static final int POINTS_FOR_EXTRA_LIFE = 1000;
	private static final int STATE_MOVING = 0;
	private static final int STATE_IN_AIR = 1;
	private InputListener leftKey;
	private InputListener rightKey;
	private InputListener runKey;
	private InputListener jumpKey;
	private InputListener slamKey;
	private int state;

	private double velY;
	private double velX;
	private double jumpCounter;
	private int points;
	private int health;
	private int lives;
	private int lifeDeficit;
	private double invulnerabilityTimer;
	private SpriteComponent spriteComponent;
	private final double moveSpeed;
	private final double maxBounceVelocity;
	private final double invulnerabilityLength;
	private final double jumpTime;
	private final double runModifier;
	private final double jumpSpeed;
	private final double gravity;
	private final double jumpModifier;
	private final double flashFrequency;

	private SpriteComponent getSpriteComponent() {
		if (spriteComponent != null) {
			return spriteComponent;
		}

		spriteComponent = (SpriteComponent) getEntity().getComponent(
				SpriteComponent.ID);
		return spriteComponent;
	}

	public PlayerComponent(Entity entity, int points, int health, int lives,
			int lifeDeficit, InputListener leftKey, InputListener rightKey,
			InputListener runKey, InputListener jumpKey, InputListener slamKey) {
		super(entity, ID);
		this.leftKey = leftKey;
		this.rightKey = rightKey;
		this.runKey = runKey;
		this.jumpKey = jumpKey;
		this.slamKey = slamKey;
		this.state = STATE_MOVING;

		velY = 0.0;
		velX = 0.0;
		jumpCounter = 0.0;
		this.invulnerabilityLength = 3.0;
		this.points = points;
		this.health = health;
		this.lives = lives;
		this.lifeDeficit = lifeDeficit;
		this.invulnerabilityTimer = invulnerabilityLength;
		spriteComponent = null;

		this.jumpTime = 0.1375;
		this.jumpSpeed = 1004.8;
		this.maxBounceVelocity = -200.0;
		this.moveSpeed = 120.0;
		this.runModifier = 1.5;
		this.gravity = 157.0;
		this.jumpModifier = 1.15;
		this.flashFrequency = 15.0;

		getSpriteComponent().setFrame(1);
	}

	public void damage() {
		if (!isInvulnerable()) {
			health--;
			invulnerabilityTimer = 0.0;
		}
	}

	public int getLifeDeficit() {
		return lifeDeficit;
	}

	public int getPoints() {
		return points;
	}

	public int getHealth() {
		return health;
	}

	public int getLives() {
		return lives;
	}

	public void addLives(int numLives) {
		lifeDeficit += numLives;
		if (lifeDeficit > 0) {
			lives += lifeDeficit;
			lifeDeficit = 0;
		}
	}

	@Override
	public void update(double delta) {
		boolean jumped = commonUpdate(delta);
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
		pickupCollectables();
		float moveX = applyLateralMovement(moveSpeed, runModifier, delta);
		applySlam(delta, jumpSpeed);
		applyGravity(gravity, delta);
		return applyJumpAmt(jumpSpeed, moveX, delta);
	}

	private void movingUpdate(double delta, boolean jumped) {
		if (applyMovementY(delta)) {
			state = STATE_IN_AIR;
			jumpCounter = jumped ? delta : jumpTime;
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
			getSpriteComponent().setFrame(0);
		}
		if (leftKey.isDown()) {
			moveX -= (float) speed;
			getSpriteComponent().setFlipX(true);
			getSpriteComponent().setFrame(0);
		}

		if (moveX == 0.0f) {
			if (state == STATE_MOVING) {
				getSpriteComponent().setFrame(1);
			} else {
				getSpriteComponent().setFrame(2);
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
		double frictionAmt = 10.0;
		double frictionAirRatio = 0.5;
		
		double friction = 0;
		
		if(velX > 0) {
			friction = delta * frictionAmt;
		} else if(velX < 0) {
			friction = -delta * frictionAmt;
		}
		
		if(state != STATE_MOVING) {
			friction *= frictionAirRatio;
		}
		
		boolean sign = velX > 0;
		velX -= friction;
		if(velX > 0 != sign) {
			velX = 0;
		}
	}

	private float applyLateralMovement(double moveSpeed, double runModifier,
			double delta) {
		double moveAccel = 10.0;
		
		double maxSpeed = moveSpeed * delta;
		if (runKey.isDown()) {
			maxSpeed *= runModifier;
		}
		
		float moveX = findLateralMovement(moveSpeed, runModifier, delta);
		moveX *= delta * moveAccel;
		velX += moveX;
		
		applyFriction(delta);

		velX = Util.clamp(velX, -maxSpeed, maxSpeed);
		moveX = (float)velX;
		float result = getEntity().move(moveX, 0);
		if(result != moveX) {
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
			// getSpriteComponent().setFrame(2);
		} else {
			getSpriteComponent().setFlipY(false);
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

	private void pickupCollectables() {
		final DoubleVal val = new DoubleVal();
		getEntity().visitInRange(CollectableComponent.ID,
				getEntity().getAABB(), new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						val.val += ((CollectableComponent) component)
								.getPoints();
						entity.remove();
					}
				});
		addPoints((int) val.val);
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
		addPoints((int) (result.val));
		boolean hitEnemy = result.val != 0.0;
		if (hitEnemy) {
			velY = -velY;
			if (velY < maxBounceVelocity) {
				velY = maxBounceVelocity;
			}
		}
		return hitEnemy;
	}

	private void addPoints(int amt) {
		int livesFromPointsBefore = points / POINTS_FOR_EXTRA_LIFE;
		points += amt;
		int extraLives = points / POINTS_FOR_EXTRA_LIFE - livesFromPointsBefore;
		if (extraLives > 0) {
			addLives(extraLives);
		} else {
			lifeDeficit += extraLives;
		}
	}
}
