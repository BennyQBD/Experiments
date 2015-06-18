package engine.rendering;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import engine.util.Util;

public class LightMap {
	private final IRenderDevice device;
	private final int width;
	private final int height;
	private final double scale;
	private int id;
	private int fbo;

	public LightMap(IRenderDevice device, int radius, Color color) {
		this.device = device;
		this.width = radius * 2;
		this.height = radius * 2;
		this.scale = 1;
		initTextures(width, height, scale,
				generateLighting(radius, width, height, color));
	}

	public LightMap(IRenderDevice device, int width, int height, double scale) {
		this.device = device;
		this.width = width;
		this.height = height;
		this.scale = scale;
		int[] data = new int[width * height];
		Arrays.fill(data, 0);
		initTextures(width, height, scale, data);
	}

	private void initTextures(int width, int height, double scale, int[] data) {
		this.id = device.createTexture(width, height, data,
				IRenderDevice.FILTER_LINEAR);
		this.fbo = 0;
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

	private int getFbo() {
		if (fbo == 0) {
			fbo = device.createRenderTarget(width, height, width, height, id);
		}
		return fbo;
	}

	public void clear(double a, double r, double g, double b) {
		device.clear(getFbo(), r, g, b, a);
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

	private static int toData(double val, double dither, Color color) {
		double amt = (Util.saturate(val  + dither/255.0));
		return color.scaleToARGB(amt);
	}

	private static int calcLight(int radius, int radiusSq, int distX,
			int distY, double dither, Color color) {
		int distCenterSq = distY * distY + distX * distX;
		if (distCenterSq > radiusSq) {
			return 0;
		}
		return toData(((double) radius / (double) (distCenterSq)), dither, color);
	}

	private static int[] generateLighting(int radius, int width, int height, Color color) {
		int[] result = new int[width * height];
		int centerX = width / 2;
		int centerY = height / 2;
		int radiusSq = radius * radius;
		for (int j = 0, distY = -centerY; j < height; j++, distY++) {
			for (int i = 0, distX = -centerX; i < width; i++, distX++) {
				result[i + j * width] = calcLight(radius, radiusSq, distX,
						distY, Dither.getDither(i, j), color);
			}
		}
		return result;
	}

	public int getId() {
		return id;
	}

	public void save(String filetype, File file) throws IOException {
		BufferedImage output = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		int[] displayComponents = ((DataBufferInt) output.getRaster()
				.getDataBuffer()).getData();
		device.getTexture(id, displayComponents, 0, 0, width, height);
		for (int i = 0; i < displayComponents.length; i++) {
			displayComponents[i] |= 0xFF000000;
		}

		ImageIO.write(output, "png", file);
	}

	public void addLight(LightMap light, double x, double y, double mapStartX,
			double mapStartY, double width, double height, Color color) {
		double posScale = 1.0 / scale;
		double texScale = 1.0 / light.getScale();
		double texMinX = texScale * (mapStartX / ((double) light.getWidth()));
		double texMinY = texScale * (mapStartY / ((double) light.getHeight()));
		double texWidth = texScale * width / ((double) light.getWidth());
		double texHeight = texScale * height / ((double) light.getHeight());

		double xStart = x * posScale;
		double xEnd = (x + width) * posScale;
		double yStart = y * posScale;
		double yEnd = (y + height) * posScale;

		yStart = this.height - yStart;
		yEnd = this.height - yEnd;

		double drawWidth = (xEnd - xStart);
		double drawHeight = (yEnd - yStart);

		device.drawRect(getFbo(), light.id, IRenderDevice.BlendMode.ADD_LIGHT,
				xStart, yStart, drawWidth, drawHeight, texMinX, texMinY,
				texWidth, texHeight, color, 1.0);
	}
}
