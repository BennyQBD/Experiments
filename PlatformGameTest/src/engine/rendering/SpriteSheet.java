package engine.rendering;

import engine.core.Util;
import engine.core.space.AABB;

public class SpriteSheet {
	private IBitmap sheet;
	private int spritesPerAxis;
	private int spriteWidth;
	private int spriteHeight;

	public SpriteSheet(IBitmap spriteSheet, int spritesPerAxis) {
		this.sheet = spriteSheet;
		this.spritesPerAxis = spritesPerAxis;
		this.spriteWidth = spriteSheet.getWidth() / spritesPerAxis;
		this.spriteHeight = spriteSheet.getHeight() / spritesPerAxis;
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
	
	public int getStartX(int index) {
		Util.boundsCheck(index, 0, spritesPerAxis*spritesPerAxis-1);
		return (index % spritesPerAxis) * spriteWidth;
	}

	public int getStartY(int index) {
		Util.boundsCheck(index, 0, spritesPerAxis*spritesPerAxis-1);
		return ((index / spritesPerAxis) % spritesPerAxis) * spriteHeight;
	}
	
	private boolean pixelIsOpaque(int x, int y) {
		return sheet.getPixel(x, y) < 0;
	}

	private boolean rowHasOpaque(int y, int imgStartX, int imgEndX) {
		for (int x = imgStartX; x < imgEndX; x++) {
			if (pixelIsOpaque(x, y)) {
				return true;
			}
		}
		return false;
	}

	private boolean columnHasOpaque(int x, int imgStartY, int imgEndY) {
		for (int y = imgStartY; y < imgEndY; y++) {
			if (pixelIsOpaque(x, y)) {
				return true;
			}
		}
		return false;
	}

	public AABB getAABB(int index) {
		int imgStartX = getStartX(index);
		int imgStartY = getStartY(index);
		int imgEndX = imgStartX + spriteWidth;
		int imgEndY = imgStartY + spriteHeight;

		int minY = 0;
		int maxY = 0;
		int minX = 0;
		int maxX = 0;
		for (int j = imgStartY; j < imgEndY; j++) {
			if (rowHasOpaque(j, imgStartX, imgEndX)) {
				minY = j - imgStartY;
				break;
			}
		}
		for (int j = imgEndY - 1; j >= imgStartY; j--) {
			if (rowHasOpaque(j, imgStartX, imgEndX)) {
				maxY = j + 1 - imgStartY;
				break;
			}
		}
		for (int i = imgStartX; i < imgEndX; i++) {
			if (columnHasOpaque(i, imgStartY, imgEndY)) {
				minX = i - imgStartX;
				break;
			}
		}
		for (int i = imgEndX - 1; i >= imgStartX; i--) {
			if (columnHasOpaque(i, imgStartY, imgEndY)) {
				maxX = i + 1 - imgStartX;
				break;
			}
		}
		return new AABB(minX, minY, maxX, maxY);
	}
}
