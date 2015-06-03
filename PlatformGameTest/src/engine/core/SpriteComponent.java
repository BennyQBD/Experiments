package engine.core;

import engine.core.space.AABB;
import engine.rendering.IRenderContext;
import engine.rendering.SpriteSheet;

public class SpriteComponent extends EntityComponent {
	public static final String COMPONENT_NAME = "SpriteComponent";
	private SpriteSheet sheet;
	private int spriteIndex;
	private int spriteOffsetX;
	private int spriteOffsetY;
	private int spriteOffsetFlippedX;
	private int spriteOffsetFlippedY;
	private double transparency;
	private boolean flipX;
	private boolean flipY;

	public SpriteComponent(Entity entity, SpriteSheet sheet, int spriteIndex) {
		super(entity, COMPONENT_NAME);
		this.sheet = sheet;
		this.spriteIndex = spriteIndex;
		AABB spriteAABB = sheet.getAABB(spriteIndex);
		entity.fitAABB(spriteAABB);
		spriteOffsetX = (int) spriteAABB.getMinX();
		spriteOffsetY = (int) spriteAABB.getMinY();
		spriteOffsetFlippedX = sheet.getSpriteWidth() - (int) spriteAABB.getMaxX();
		spriteOffsetFlippedY = sheet.getSpriteHeight() - (int) spriteAABB.getMaxY();

		this.transparency = 1.0;
		this.flipX = false;
		this.flipY = false;
	}

	@Override
	public void render(IRenderContext target, int viewportX, int viewportY) {
		if (sheet != null) {
			int spriteOffX = spriteOffsetX;
			int spriteOffY = spriteOffsetY;
			if (flipX) {
				spriteOffX = spriteOffsetFlippedX;
			}
			if (flipY) {
				spriteOffY = spriteOffsetFlippedY;
			}
			target.drawSprite(sheet, spriteIndex,
					(int) Math.round(getEntity().getAABB().getMinX())
							- viewportX - spriteOffX,
					(int) Math.round(getEntity().getAABB().getMinY())
							- viewportY - spriteOffY, transparency, flipX,
					flipY, 0xFFFFFF);
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