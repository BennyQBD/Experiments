package engine.rendering.awt;

import engine.rendering.ARGBColor;
import engine.rendering.ArrayBitmap;
import engine.rendering.IBitmap;
import engine.rendering.IRenderContext;
import engine.rendering.LightMap;
import engine.rendering.SpriteSheet;
import engine.util.Util;

public class AWTRenderContext extends ArrayBitmap implements IRenderContext {
	private LightMap lightMap;

	public AWTRenderContext(int width, int height) {
		super(width, height);
		lightMap = new LightMap(width, height, 1);
		lightMap.clear();
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

	public int drawString(String str, SpriteSheet font, int x, int y,
			int color, int wrapX) {
		int maxLength = (wrapX - x) / font.getSpriteWidth();
		if (wrapX <= x || wrapX <= 0 || str.length() < maxLength) {
			drawStringLine(str, font, x, y, color);
			return font.getSpriteHeight();
		}
		int yStart = y;
		str = Util.wrapString(str, maxLength);
		String[] strs = str.split("\n");
		for (int i = 0; i < strs.length; i++) {
			String[] wrappedStrings = strs[i].split("(?<=\\G.{" + maxLength
					+ "})");
			for (int j = 0; j < wrappedStrings.length; j++, y += font
					.getSpriteHeight()) {
				drawStringLine(wrappedStrings[j], font, x, y, color);
			}
		}
		return y - yStart;
	}

	private void drawStringLine(String str, SpriteSheet font, int x, int y,
			int color) {
		for (int i = 0; i < str.length(); i++, x += font.getSpriteWidth()) {
			char c = str.charAt(i);
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
//				 color = blendColors(color & colorMask, getPixel(x, y),
//				 transparency);
//				 setPixel(x, y, color);
				if (color < 0 && ditherPass(i, j, ditherTransparency)) {
					setPixel(x, y, color & colorMask);
				}
			}
		}
	}

	private static int blendComponent(int comp1, int comp2, int amt1, int amt2) {
		return (comp1 * amt1 + comp2 * amt2) >> 8;
	}

	private static int blendColors(int color1, int color2, double amt) {
		int blendAmt1 = (int) (ARGBColor.getComponent(color1, 0) * amt + 0.5);
		if (blendAmt1 == 0) {
			return color2;
		} else if (blendAmt1 == 255) {
			return color1;
		}
		int blendAmt2 = 255 - blendAmt1;
		return ARGBColor
				.makeColor(
						blendComponent(ARGBColor.getComponent(color1, 1),
								ARGBColor.getComponent(color2, 1), blendAmt1,
								blendAmt2),
						blendComponent(ARGBColor.getComponent(color1, 2),
								ARGBColor.getComponent(color2, 2), blendAmt1,
								blendAmt2),
						blendComponent(ARGBColor.getComponent(color1, 3),
								ARGBColor.getComponent(color2, 3), blendAmt1,
								blendAmt2));
	}

	@Override
	public void clearLighting() {
		lightMap.clear();
	}

	@Override
	public void drawLight(LightMap light, int xIn, int yIn, int mapStartX,
			int mapStartY, int width, int height) {
		lightMap.addLight(light, xIn, yIn, mapStartX, mapStartY, width, height);
	}

	@Override
	public void applyLighting(double ambientLightAmt) {
		double ambientLightScale = (1.0 - ambientLightAmt) / 255.0;
		for (int j = 0; j < getHeight(); j++) {
			for (int i = 0; i < getWidth(); i++) {
				double lightAmt = lightMap.getLight(i, j) * ambientLightScale
						+ ambientLightAmt;
				setPixel(i, j, blendColors(getPixel(i, j), 0x000000, lightAmt));
			}
		}
	}
}