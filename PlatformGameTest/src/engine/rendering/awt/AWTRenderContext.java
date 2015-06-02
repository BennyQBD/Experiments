package engine.rendering.awt;

import engine.core.Util;
import engine.rendering.IRenderContext;
import engine.rendering.IBitmap;
import engine.rendering.ArrayBitmap;

public class AWTRenderContext extends ArrayBitmap implements IRenderContext {
	public AWTRenderContext(int width, int height) {
		super(width, height);
	}

	private static final int[] dither = new int[] 
	{
		1, 49, 13, 61, 4, 52, 16, 64,
		33, 17, 45, 29, 36, 20, 48, 32,
		9, 57, 5, 53, 12, 60, 8, 56,
		41, 25, 37, 21, 44, 28, 40, 24,
		3, 51, 15, 63, 2, 50, 14, 62,
		35, 19, 47, 31, 34, 18, 46, 30,
		11, 59, 7, 55, 10, 58, 6, 54,
		43, 27, 39, 23, 42, 26, 38, 22,
	};

	private static boolean ditherPass(int i, int j, int ditherAmt) {
		return dither[(i & 7) + (j & 7) * 8] <= ditherAmt;
	}

	private static int getDitherAmt(double ditherFactor) {
		return (int)(Util.saturate(ditherFactor) * 64.0 + 0.5);
	}

	public void blit(IBitmap image, int offsetX, int offsetY, double transparency,
			boolean flipX, boolean flipY) {
		int iStart = 0;
		int jStart = 0;
		int iStep = 1;
		int jStep = 1;

		if(flipX) {
			iStart = image.getWidth() - 1;
			iStep = -1;
		}
		if(flipY) {
			jStart = image.getHeight() - 1;
			jStep = -1;
		}

		int xEnd = image.getWidth() + offsetX;
		int yEnd = image.getHeight() + offsetY;

		if(offsetY < 0) {
			if(!flipY) {
				jStart -= offsetY;
			} else {
				jStart += offsetY;
			}
			offsetY = 0;
		}
		if(yEnd > getHeight()) {
			yEnd = getHeight();
		}

		if(offsetX < 0) {
			if(!flipX) {
				iStart -= offsetX;
			} else {
				iStart += offsetX;
			}
			offsetX = 0;
		}
		if(xEnd > getWidth()) {
			xEnd = getWidth();
		}

		int ditherTransparency = getDitherAmt(transparency);
		for(int j = jStart, y = offsetY; y < yEnd;
			   	j += jStep, y++) {
			for(int i = iStart, x = offsetX; 
					x < xEnd; i += iStep, x++) {
				int color = image.getPixel(i, j);
				if(color < 0 && ditherPass(i, j, ditherTransparency)) {
					drawPixel(x, y, color);
				}
			}
		}
	}
}
