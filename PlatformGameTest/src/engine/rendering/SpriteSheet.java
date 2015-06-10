package engine.rendering;

import engine.space.AABB;
import engine.util.Util;

public class SpriteSheet {
	private IBitmap sheet;
	private int spritesPerX;
	private int spritesPerY;
	private int spriteWidth;
	private int spriteHeight;

	public SpriteSheet(IBitmap spriteSheet, int spritesPerAxis) {
		this(spriteSheet, spritesPerAxis, spritesPerAxis);
	}

	public SpriteSheet(IBitmap spriteSheet, int spritesPerX, int spritesPerY) {
		this.sheet = spriteSheet;
		this.spritesPerX = spritesPerX;
		this.spritesPerY = spritesPerY;
		this.spriteWidth = spriteSheet.getWidth() / spritesPerX;
		this.spriteHeight = spriteSheet.getHeight() / spritesPerY;
	}

	public int getSpriteWidth() {
		return spriteWidth;
	}

	public int getSpriteHeight() {
		return spriteHeight;
	}

	public IBitmap getSheet() {
		return sheet;
	}

	public int getNumSprites() {
		return spritesPerX * spritesPerY - 1;
	}

	public int[] getPixels(int[] dest, int x, int y, int width, int height,
			int spriteIndex) {
		return sheet.getPixels(dest, getStartX(spriteIndex) + x,
				getStartY(spriteIndex) + y, width, height);
		// return sheet.getPixel(getStartX(spriteIndex) + x,
		// getStartY(spriteIndex) + y);
	}

	public int getStartX(int index) {
		Util.boundsCheck(index, 0, getNumSprites());
		return (index % spritesPerX) * spriteWidth;
	}

	public int getStartY(int index) {
		Util.boundsCheck(index, 0, getNumSprites());
		return ((index / spritesPerX) % spritesPerY) * spriteHeight;
	}

	private boolean rowHasOpaque(int y, int[] pixels) {
		for (int x = 0; x < spriteWidth; x++) {
			if (pixels[x + y * spriteWidth] < 0) {
				return true;
			}
		}
		return false;
	}

	private boolean columnHasOpaque(int x, int[] pixels) {
		for (int y = 0; y < spriteHeight; y++) {
			if (pixels[x + y * spriteWidth] < 0) {
				return true;
			}
		}
		return false;
	}

	public AABB getAABB(int index) {
		int imgStartX = getStartX(index);
		int imgStartY = getStartY(index);

		int minY = 0;
		int maxY = 0;
		int minX = 0;
		int maxX = 0;
		int[] pixels = sheet.getPixels(null, imgStartX, imgStartY, spriteWidth,
				spriteHeight);
		for (int j = 0; j < spriteHeight; j++) {
			if (rowHasOpaque(j, pixels)) {
				minY = j;
				break;
			}
		}
		for (int j = spriteHeight - 1; j >= 0; j--) {
			if (rowHasOpaque(j, pixels)) {
				maxY = j + 1;
				break;
			}
		}
		for (int i = 0; i < spriteWidth; i++) {
			if (columnHasOpaque(i, pixels)) {
				minX = i;
				break;
			}
		}
		for (int i = spriteWidth - 1; i >= 0; i--) {
			if (columnHasOpaque(i, pixels)) {
				maxX = i + 1;
				break;
			}
		}
		return new AABB(minX, minY, maxX, maxY);
	}
}
