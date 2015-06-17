package engine.util.components;

import engine.core.entity.Entity;
import engine.core.entity.EntityComponent;
import engine.rendering.IRenderContext;
import engine.rendering.SpriteSheet;
import engine.space.AABB;
import engine.util.IDAssigner;

public class SpriteComponent extends EntityComponent {
	private class Animation {
		private SpriteSheet[] sheets;
		private int[] indices;
		private double[] frameTimes;
		private int[] nextFrames;
		private int currentFrame;
		private double currentFrameTime;

		public Animation(SpriteSheet[] sheets, int[] indices,
				double[] frameTimes, int[] nextFrames) {
			this.sheets = sheets;
			this.indices = indices;
			this.frameTimes = frameTimes;
			this.nextFrames = nextFrames;
			this.currentFrame = 0;
			this.currentFrameTime = 0.0;
		}

		public void update(double delta) {
			currentFrameTime += delta;
			while (frameTimes[currentFrame] != 0
					&& currentFrameTime > frameTimes[currentFrame]) {
				currentFrameTime -= frameTimes[currentFrame];
				int nextFrame = nextFrames[currentFrame];
				currentFrame = nextFrame;
			}
		}

		public SpriteSheet getSheet() {
			return sheets[currentFrame];
		}

		public int getSpriteIndex() {
			return indices[currentFrame];
		}

		public void setFrame(int frame) {
			currentFrame = frame;
			currentFrameTime = 0.0;
		}
	}

	public static final int ID = IDAssigner.getId();
	private Animation animation;
	private int spriteOffsetX;
	private int spriteOffsetY;
	private int spriteOffsetFlippedX;
	private int spriteOffsetFlippedY;
	private double transparency;
	private boolean flipX;
	private boolean flipY;
	private int colorMask;

	public SpriteComponent(Entity entity, SpriteSheet sheet, int spriteIndex,
			int colorMask) {
		this(entity, new SpriteSheet[] { sheet }, new int[] { spriteIndex },
				0.0, colorMask);
	}

	public SpriteComponent(Entity entity, SpriteSheet sheet, double frameTime,
			int colorMask) {
		super(entity, ID);
		SpriteSheet[] sheets = new SpriteSheet[sheet.getNumSprites()];
		int[] indices = new int[sheets.length];
		for (int i = 0; i < sheets.length; i++) {
			indices[i] = i;
			sheets[i] = sheet;
		}
		init(entity, sheets, indices, frameTime, colorMask);
	}

	public SpriteComponent(Entity entity, SpriteSheet sheet, int[] indices,
			double frameTime, int colorMask) {
		super(entity, ID);
		SpriteSheet[] sheets = new SpriteSheet[indices.length];
		for (int i = 0; i < sheets.length; i++) {
			sheets[i] = sheet;
		}
		init(entity, sheets, indices, frameTime, colorMask);
	}

	public SpriteComponent(Entity entity, SpriteSheet[] sheets, int[] indices,
			double frameTime, int colorMask) {
		super(entity, ID);
		init(entity, sheets, indices, frameTime, colorMask);
	}

	public SpriteComponent(Entity entity, SpriteSheet[] sheets, int[] indices,
			double[] frameTimes, int[] nextFrames, int colorMask) {
		super(entity, ID);
		init(entity, sheets, indices, frameTimes, nextFrames, colorMask);
	}

	private void init(Entity entity, SpriteSheet[] sheets, int[] indices,
			double frameTime, int colorMask) {
		double frameTimes[] = new double[sheets.length];
		int nextFrames[] = new int[sheets.length];

		for (int i = 0; i < sheets.length; i++) {
			frameTimes[i] = frameTime;
			nextFrames[i] = i + 1;
		}
		nextFrames[sheets.length - 1] = 0;
		init(entity, sheets, indices, frameTimes, nextFrames, colorMask);
	}

	private void init(Entity entity, SpriteSheet[] sheets, int[] indices,
			double[] frameTimes, int[] nextFrames, int colorMask) {
		this.animation = new Animation(sheets, indices, frameTimes, nextFrames);

		AABB spriteAABB = animation.getSheet().getAABB(
				animation.getSpriteIndex());
		entity.fitAABB(spriteAABB);
		spriteOffsetX = (int) spriteAABB.getMinX();
		spriteOffsetY = (int) spriteAABB.getMinY();
		spriteOffsetFlippedX = animation.getSheet().getSpriteWidth()
				- (int) spriteAABB.getMaxX();
		spriteOffsetFlippedY = animation.getSheet().getSpriteHeight()
				- (int) spriteAABB.getMaxY();

		this.transparency = 1.0;
		this.flipX = false;
		this.flipY = false;
		this.colorMask = colorMask;
	}

	@Override
	public void update(double delta) {
		animation.update(delta);
	}

	@Override
	public void render(IRenderContext target, int viewportX, int viewportY) {
		SpriteSheet sheet = animation.getSheet();
		int spriteIndex = animation.getSpriteIndex();
		if (sheet != null) {
			int spriteOffX = spriteOffsetX;
			int spriteOffY = spriteOffsetY;
			if (flipX) {
				spriteOffX = spriteOffsetFlippedX;
			}
			if (flipY) {
				spriteOffY = spriteOffsetFlippedY;
			}
			int xStart = (int) Math.round(getEntity().getAABB().getMinX())
					- viewportX - spriteOffX;
			int yStart = (int) Math.round(getEntity().getAABB().getMinY())
					- viewportY - spriteOffY;
			target.drawSprite(sheet, spriteIndex, xStart, yStart, transparency,
					flipX, flipY, colorMask);
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

	public void setFrame(int frame) {
		animation.setFrame(frame);
	}
}