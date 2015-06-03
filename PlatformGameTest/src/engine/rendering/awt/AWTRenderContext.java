package engine.rendering.awt;

import engine.core.Util;
import engine.rendering.ArrayBitmap;
import engine.rendering.IBitmap;
import engine.rendering.IRenderContext;
import engine.rendering.SpriteSheet;

public class AWTRenderContext extends ArrayBitmap implements IRenderContext {
	public AWTRenderContext(int width, int height) {
		super(width, height);
	}

	private static final int[] dither = new int[] { 1, 49, 13, 61, 4, 52, 16,
			64, 33, 17, 45, 29, 36, 20, 48, 32, 9, 57, 5, 53, 12, 60, 8, 56,
			41, 25, 37, 21, 44, 28, 40, 24, 3, 51, 15, 63, 2, 50, 14, 62, 35,
			19, 47, 31, 34, 18, 46, 30, 11, 59, 7, 55, 10, 58, 6, 54, 43, 27,
			39, 23, 42, 26, 38, 22, };

	private static boolean ditherPass(int i, int j, int ditherAmt) {
		return dither[(i & 7) + (j & 7) * 8] <= ditherAmt;
	}

	private static int getDitherAmt(double ditherFactor) {
		return (int) (Util.saturate(ditherFactor) * 64.0 + 0.5);
	}

	public void drawString(String msg, SpriteSheet font, int x, int y, int color) {
		for (int i = 0; i < msg.length(); i++, x += font.getSpriteWidth()) {
			char c = msg.charAt(i);
			drawSprite(font, (int) c, x, y, 1.0, false, false, color);
		}
	}

	public void drawSprite(SpriteSheet sheet, int index, int x, int y,
			double transparency, boolean flipX, boolean flipY, int colorMask) {
		blit(sheet.getSheet(), x, y, transparency, flipX, flipY,
				sheet.getStartX(index), sheet.getStartY(index),
				sheet.getSpriteWidth(), sheet.getSpriteHeight(), colorMask);
	}

	private void blit(IBitmap image, int offsetX, int offsetY,
			double transparency, boolean flipX, boolean flipY, int imgStartX,
			int imgStartY, int imgWidth, int imgHeight, int colorMask) {
		colorMask |= 0xFF000000;
		int iStart = imgStartX;
		int jStart = imgStartY;
		int iStep = 1;
		int jStep = 1;

		if (flipX) {
			iStart += imgWidth - 1;
			iStep = -1;
		}
		if (flipY) {
			jStart += imgHeight - 1;
			jStep = -1;
		}

		int xEnd = imgWidth + offsetX;
		int yEnd = imgHeight + offsetY;

		if (offsetY < 0) {
			jStart -= flipY ? -offsetY : offsetY;
			offsetY = 0;
		}
		if (yEnd > getHeight()) {
			yEnd = getHeight();
		}

		if (offsetX < 0) {
			iStart -= flipX ? -offsetX : offsetX;
			offsetX = 0;
		}
		if (xEnd > getWidth()) {
			xEnd = getWidth();
		}

		int ditherTransparency = getDitherAmt(transparency);
		for (int j = jStart, y = offsetY; y < yEnd; j += jStep, y++) {
			for (int i = iStart, x = offsetX; x < xEnd; i += iStep, x++) {
				int color = image.getPixel(i, j);
//				color = blendColors(color & colorMask, getPixel(x, y), transparency);
//				setPixel(x, y, color);
				if (color < 0 && ditherPass(i, j, ditherTransparency)) {
					setPixel(x, y, color & colorMask);
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static int blendColors(int color1, int color2, double amt) {
		int c1a = (color1 >> 24) & 0xFF;
		int blendAmt = (int)(c1a * amt);
		
		if(blendAmt == 0) {
			return color2;
		} else if(blendAmt == 255) {
			return color1;
		}
		
		int blendAmt2 = 255 - blendAmt;
		
		int c1r = (color1 >> 16) & 0xFF;
		int c1g = (color1 >> 8) & 0xFF;
		int c1b = (color1 >> 0) & 0xFF;
		
		int c2r = (color2 >> 16) & 0xFF;
		int c2g = (color2 >> 8) & 0xFF;
		int c2b = (color2 >> 0) & 0xFF;
		
		int newR = (c1r * blendAmt + c2r * blendAmt2) >> 8;
		int newG = (c1g * blendAmt + c2g * blendAmt2) >> 8;
		int newB = (c1b * blendAmt + c2b * blendAmt2) >> 8;
		
		return (newR << 16) | (newG << 8) | (newB);
	}
}