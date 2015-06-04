package game.components;

import engine.core.Entity;
import engine.core.EntityComponent;
import engine.core.IEntityVisitor;
import engine.core.InputListener;
import engine.core.SpriteComponent;

public class PlayerComponent extends EntityComponent {
	public static final String COMPONENT_NAME = "PlayerComponent";
	private static final double MAX_BOUNCE_VELOCITY = -200.0;
	private static final double INVULNERABILITY_LENGTH = 3.0;
	private InputListener leftKey;
	private InputListener rightKey;
	private InputListener runKey;
	private InputListener jumpKey;
	private InputListener slamKey;

	private boolean hasJumped;
	private double velY;
	private double jumpCounter;
	private int points;
	private int health;
	private double invulnerabilityTimer;
	private SpriteComponent spriteComponent;

	private SpriteComponent getSpriteComponent() {
		if (spriteComponent != null) {
			return spriteComponent;
		}

		spriteComponent = (SpriteComponent) getEntity().getComponent(
				SpriteComponent.COMPONENT_NAME);
		return spriteComponent;
	}

	public PlayerComponent(Entity entity, int points, int health,
			InputListener leftKey, InputListener rightKey,
			InputListener runKey, InputListener jumpKey, InputListener slamKey) {
		super(entity, COMPONENT_NAME);
		this.leftKey = leftKey;
		this.rightKey = rightKey;
		this.runKey = runKey;
		this.jumpKey = jumpKey;
		this.slamKey = slamKey;

		hasJumped = false;
		velY = 0.0;
		jumpCounter = 0.0;
		this.points = points;
		this.health = health;
		this.invulnerabilityTimer = INVULNERABILITY_LENGTH;
		spriteComponent = null;
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
		}
		if (leftKey.isDown()) {
			moveX -= (float) speed;
			getSpriteComponent().setFlipX(true);
		}
		return moveX;
	}

	private void applyJumpAmt(double jumpSpeed, float moveX, double delta) {
		if (slamKey.isDown()) {
			velY += jumpSpeed * 0.8 * delta;
			getSpriteComponent().setFlipY(true);
		} else {
			getSpriteComponent().setFlipY(false);
		}

		if (runKey.isDown() && moveX != 0.0f) {
			jumpSpeed *= 1.15;
		}

		if (jumpKey.isDown() && !slamKey.isDown()) {
			if (velY == 0.0 && !hasJumped) {
				velY -= jumpSpeed * delta;
				hasJumped = true;
				jumpCounter = 0.0;
			}

			if (hasJumped && jumpCounter < 0.1) {
				velY -= jumpSpeed * delta;
			}
		}

		if (hasJumped) {
			jumpCounter += delta;
		}
	}

	private boolean isInvulnerable() {
		return invulnerabilityTimer < INVULNERABILITY_LENGTH;
	}
	public void damage() {
		if(!isInvulnerable()) {
			health--;
			invulnerabilityTimer = 0.0;
		}
	}

	@Override
	public void update(double delta) {
		if(isInvulnerable()) {
			invulnerabilityTimer += delta;
			double invulnerabilityFlasher = invulnerabilityTimer * 15;
//			invulnerabilityFlasher -= (int)invulnerabilityFlasher;
//			invulnerabilityFlasher *= 2;
//			if(invulnerabilityFlasher > 1) {
//				invulnerabilityFlasher = 1.0 - (invulnerabilityFlasher - 1.0);
//			}
//			invulnerabilityFlasher = 1.0 - invulnerabilityFlasher;
//			getSpriteComponent().setTransparency(invulnerabilityFlasher);
			if((int)(invulnerabilityFlasher) % 2 == 0) {
				getSpriteComponent().setTransparency(1.0);
			} else {
				getSpriteComponent().setTransparency(0.0);
			}
		} else { 
			getSpriteComponent().setTransparency(1.0);
		}
		double oldVelY = velY;
		float moveX = getEntity()
				.move(findLateralMovement(90.0, 2.0, delta), 0);
		applyJumpAmt(1004.8, moveX, delta);
		applyGravity(157.0, delta, oldVelY);
		pickupCollectables();
		//revealLocalHiddenAreas(64.0);
	}

	private void applyGravity(double gravity, double delta, double oldVelY) {
		velY += gravity * delta;
		float newMoveY = (float) (velY * delta);
		float moveY = getEntity().move(0, newMoveY);
		if (newMoveY != moveY) {
			if (oldVelY != 0.0 && tryHitEnemy()) {
				velY = -velY;
				if (velY < MAX_BOUNCE_VELOCITY) {
					velY = MAX_BOUNCE_VELOCITY;
				}
			} else {
				velY = 0;
				hasJumped = false;
				jumpCounter = 0.0;
			}
		}
	}

	private class DoubleVal {
		public double val = 0.0;
	}

	private void pickupCollectables() {
		final DoubleVal val = new DoubleVal();
		getEntity().visitInRange(CollectableComponent.COMPONENT_NAME,
				getEntity().getAABB(), new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						val.val += ((CollectableComponent) component)
								.getPoints();
						entity.remove();
					}
				});
		points += (int) val.val;
	}

	private boolean tryHitEnemy() {
		final DoubleVal result = new DoubleVal();
		getEntity().visitInRange(EnemyComponent.COMPONENT_NAME,
				getEntity().getAABB().expand(0, 1, 0), new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						if(((EnemyComponent) component).kill()) {
							result.val += ((EnemyComponent) component).getPoints();
						}
					}
				});
		points += (int)(result.val);
		return result.val != 0.0;
	}

	private void revealLocalHiddenAreas(final double range) {
		getEntity().visitInRange(SpriteComponent.COMPONENT_NAME,
				getEntity().getAABB().expand(range, range, 0),
				new IEntityVisitor() {
					@Override
					public void visit(Entity entity, EntityComponent component) {
						double distX = getEntity().getAABB().getCenterX()
								- entity.getAABB().getCenterX();
						double distY = getEntity().getAABB().getCenterY()
								- entity.getAABB().getCenterY();
						double dist = Math.sqrt(distX * distX + distY * distY)
								/ range;

						if (entity != getEntity() && !entity.getBlocking()
								&& entity.getDitherable()) {
							if (dist <= 1.0) {
								((SpriteComponent) component)
										.setTransparency(dist * dist);
							} else {
								((SpriteComponent) component)
										.setTransparency(1);
							}
						}
					}
				});
	}

	public int getPoints() {
		return points;
	}
	
	public int getHealth() {
		return health;
	}
}
