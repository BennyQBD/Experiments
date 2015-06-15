package engine.rendering;

import engine.util.Util;

public class LightMap {
	private final IRenderDevice device;
	private final int width;
	private final int height;
	private final double scale;
	private int id;
	private int fbo;

	public LightMap(IRenderDevice device, int radius) {
		this.device = device;
		this.width = radius * 2;
		this.height = radius * 2;
		this.scale = 1;
		initTextures(width, height, scale,
				generateLighting(radius, width, height));
	}

	public LightMap(IRenderDevice device, int width, int height,
			double scale) {
		this.device = device;
		this.width = width;
		this.height = height;
		this.scale = scale;
		initTextures(width, height, scale, null);
	}

	private void initTextures(int width, int height, double scale, byte[] data) {
		this.id = device.createTexture(width, height, data,
				IRenderDevice.FILTER_LINEAR);
		this.fbo = device.createRenderTarget(width, height, width, height, id);
		clear();
	}

	public void dispose() {
		fbo = device.releaseRenderTarget(fbo);
		id = device.releaseTexture(id);
	}

	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	public void clear() {
		device.clear(fbo, 0.0, 0.0, 0.0, 0.0);
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

	private static byte toData(double val, double dither) {
		return (byte) (Util.saturate(val) * 255.0 + dither);
	}

	private static byte calcLight(int radius, int radiusSq, int distX,
			int distY, double dither) {
		int distCenterSq = distY * distY + distX * distX;
		if (distCenterSq > radiusSq) {
			return (byte) 0;
		}
		return toData(((double) radius / (double) (distCenterSq)), dither);
	}

	private static byte[] generateLighting(int radius, int width, int height) {
		byte[] result = new byte[width * height];
		int centerX = width / 2;
		int centerY = height / 2;
		int radiusSq = radius * radius;
		for (int j = 0, distY = -centerY; j < height; j++, distY++) {
			for (int i = 0, distX = -centerX; i < width; i++, distX++) {
				result[i + j * width] = calcLight(radius, radiusSq, distX,
						distY, Dither.getDither(i, j));
			}
		}
		return result;
	}

	public int getId() {
		return id;
	}

	public void addLight(LightMap light, int x, int y, int mapStartX,
			int mapStartY, int width, int height) {
		double posScale = 1.0 / scale;
		double texScale = 1.0 / light.getScale();
		double texMinX = texScale
				* ((double) mapStartX / ((double) light.getWidth()));
		double texMinY = texScale
				* ((double) mapStartY / ((double) light.getHeight()));
		double texWidth = texScale * ((double) width)
				/ ((double) light.getWidth());
		double texHeight = texScale * ((double) height)
				/ ((double) light.getHeight());

		double xStart = x * posScale;
		double xEnd = (x + width) * posScale;
		double yStart = y * posScale;
		double yEnd = (y + height) * posScale;

		yStart = this.height - yStart;
		yEnd = this.height - yEnd;

		double drawWidth = (xEnd - xStart);
		double drawHeight = (yEnd - yStart);

		device.drawRect(fbo, light.id, IRenderDevice.BlendMode.ADD_LIGHT,
				xStart, yStart, drawWidth, drawHeight, texMinX, texMinY,
				texWidth, texHeight);
	}
}
