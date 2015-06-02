package engine.core;

import engine.rendering.IBitmap;
import engine.rendering.IRenderContext;

public class SpriteComponent extends EntityComponent{
	public static final String COMPONENT_NAME = "SpriteComponent";
	private IBitmap sprite;
	private int spriteOffsetX;
	private int spriteOffsetY;
	private int spriteOffsetFlippedX;
	private int spriteOffsetFlippedY;
	private double transparency;
	private boolean flipX;
	private boolean flipY;

	public SpriteComponent(Entity entity, IBitmap sprite) {
		super(entity, COMPONENT_NAME);
		this.sprite = sprite;
		AABB spriteAABB = sprite.getAABB();
		entity.fitAABB(spriteAABB);
		spriteOffsetX = (int)spriteAABB.getMinX();
		spriteOffsetY = (int)spriteAABB.getMinY();
		spriteOffsetFlippedX = sprite.getWidth() - (int)spriteAABB.getMaxX();
		spriteOffsetFlippedY = sprite.getHeight() - (int)spriteAABB.getMaxY();

		this.transparency = 1.0;
		this.flipX = false;
		this.flipY = false;
	}

	@Override
	public void render(IRenderContext target, int viewportX, int viewportY) {
		if(sprite != null) {
			int spriteOffX = spriteOffsetX;
			int spriteOffY = spriteOffsetY;
			if(flipX) {
				spriteOffX = spriteOffsetFlippedX;
			}
			if(flipY) {
				spriteOffY = spriteOffsetFlippedY;
			}
			target.blit(sprite, 
					(int)Math.round(getEntity().getAABB().getMinX()) - viewportX - spriteOffX,
					(int)Math.round(getEntity().getAABB().getMinY()) - viewportY - spriteOffY,
					transparency,
					flipX, flipY);
		}
	}

	public void setFlipX(boolean flipX) {
		this.flipX = flipX;
	}

	public void setFlipY(boolean flipY) {
		this.flipY = flipY;
	}

	public double getTransparency() {
		return transparency;
	}

	public void setTransparency(double transparency) {
		this.transparency = transparency;	
	}

}
