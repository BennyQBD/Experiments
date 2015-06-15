package engine.rendering;

import java.util.Arrays;

import engine.util.Util;

public class OldLightMap {
	private final int width;
	private final int height;
	private final double scale;
	private final byte[] lighting;

	public OldLightMap(int radius) {
		this(radius * 2, radius * 2, 1);
		generate(radius);
	}

	public OldLightMap(int width, int height, double scale) {
		lighting = new byte[width * height];
		this.width = width;
		this.height = height;
		this.scale = scale;
	}

	public void clear() {
		Arrays.fill(lighting, (byte) 0);
	}

	public double getLight(int x, int y) {
		return (double) ((lighting[x + y * width] & 0xFF));
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

	private static byte toData(double val) {
		return (byte) (Util.saturate(val) * 255.0 + 0.5);
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
				lighting[i + j * width] = toData(((double) radius / (double) (distCenterSq)));
			}
		}
	}

	public void addLight(OldLightMap light, int xIn, int yIn, int mapStartX,
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

		int xDist = xEnd - xStart;
		int yDist = yEnd - yStart;
		double jEnd = (jStart + yDist * step);
		if (jEnd > light.height - 1) {
			yEnd = (int) (((light.height - 1) - jStart) / step) + yStart;
		}

		double iEnd = (iStart + xDist * step);
		if (iEnd > light.width - 1) {
			xEnd = (int) (((light.width - 1) - iStart) / step) + xStart;
		}

		double j = jStart;
		for (int y = yStart; y < yEnd; y++, j += step) {
			double i = iStart;
			for (int x = xStart; x < xEnd; x++, i += step) {
//				if (step >= 1.0) {
					int index = x + y * this.width;
					int light1 = lighting[index] & 0xFF;
					int light2 = light.lighting[(int) (i+0.5) + ((int) (j+0.5))
							* light.width] & 0xFF;
					lighting[index] = (byte) Util
							.clamp(light1 + light2, 0, 255);
//				} else {
//					int iInt = (int) i;
//					int jInt = (int) j;
//
//					int light1 = light.lighting[iInt + (jInt) * light.width] & 0xFF;
//					int light2 = light.lighting[iInt + 1 + (jInt) * light.width] & 0xFF;
//					int light3 = light.lighting[iInt + (jInt + 1) * light.width] & 0xFF;
//					int light4 = light.lighting[iInt + 1 + (jInt + 1)
//							* light.width] & 0xFF;
//
//					int amt1 = (int) ((i - iInt) * 255.0);
//					int amt2 = (int) ((j - jInt) * 255.0);
//
//					int lerpX1 = (light1 * (255 - amt1) + light2 * amt1) >> 8;
//					int lerpX2 = (light3 * (255 - amt1) + light4 * amt1) >> 8;
//					int lightAmt = (lerpX1 * (255 - amt2) + lerpX2 * amt2) >> 8;
//					
//					int index = x + y * this.width;
//					lighting[index] = (byte) Util
//							.clamp(lighting[index] + lightAmt, 0, 255);
//				}
			}
		}
	}
}
