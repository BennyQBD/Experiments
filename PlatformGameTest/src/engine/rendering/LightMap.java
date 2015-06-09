package engine.rendering;

import java.util.Arrays;

import engine.util.Util;

public class LightMap {
	private final int width;
	private final int height;
	private final double scale;
	private final float[] lighting;

	public LightMap(int radius) {
		this(radius * 2, radius * 2, 1);
		generate(radius);
	}

	public LightMap(int width, int height, double scale) {
		lighting = new float[width * height];
		this.width = width;
		this.height = height;
		this.scale = scale;
	}

	public void clear() {
		Arrays.fill(lighting, 0.0f);
	}

	public double getLight(int x, int y) {
		return (double)(lighting[x + y * width]);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public double getScale() {
		return scale;
	}

	private void generate(int radius) {
		clear();
		int centerX = width / 2;
		int centerY = height / 2;
		int radiusSq = radius * radius;
		for (int j = 0, distY = -centerY; j < height; j++, distY++) {
			for (int i = 0, distX = -centerX; i < width; i++, distX++) {
				int distCenterSq = distY * distY + distX * distX;
				if (distCenterSq > radiusSq) {
					continue;
				}
				lighting[i + j * width] = (float)((double) radius
						/ (double) (distCenterSq));
			}
		}
	}

	public void addLight(LightMap light, int xIn, int yIn, int mapStartX,
			int mapStartY, int width, int height) {
		double iStart = mapStartX / light.getScale();
		double jStart = mapStartY / light.getScale();
		int xStart = xIn;
		int yStart = yIn;
		int xEnd = xStart + width;
		int yEnd = yStart + height;
		if (xStart < 0) {
			iStart -= xIn / light.getScale();
		}
		if (yStart < 0) {
			jStart -= yIn / light.getScale();
		}
		xStart = Util.clamp((int) Math.floor(xStart / scale), 0, getWidth());
		yStart = Util.clamp((int) Math.floor(yStart / scale), 0, getHeight());
		xEnd = Util.clamp((int) Math.ceil(xEnd / scale), 0, getWidth());
		yEnd = Util.clamp((int) Math.ceil(yEnd / scale), 0, getHeight());
		double step = scale / light.getScale();
		double j = jStart;
		for (int y = yStart; y < yEnd; y++, j += step) {
			if (j >= light.getHeight() - 1) {
				continue;
			}
			double i = iStart;
			for (int x = xStart; x < xEnd; x++, i += step) {
				if (i >= light.getWidth() - 1) {
					continue;
				}
				// if(step >= 1.0) {
				lighting[x + y * getWidth()] += (float)(light.getLight((int) i, (int) j));
				// } else {
				// double light1 = light.getLight((int)i, (int)j);
				// double light2 = light.getLight((int)i + 1, (int)j);
				// double light3 = light.getLight((int)i, (int)j + 1);
				// double light4 = light.getLight((int)i + 1, (int)j + 1);
				//
				// double amt1 = i - (int)i;
				// double amt2 = j - (int)j;
				//
				// double lerpX1 = light1 * (1.0 - amt1) + light2 * amt1;
				// double lerpX2 = light3 * (1.0 - amt1) + light4 * amt1;
				// double lightAmt = lerpX1 * (1.0 - amt2) + lerpX2 * amt2;
				// lighting[x + y * getWidth()] += (float)lightAmt;
				// }
			}
		}
	}
}
