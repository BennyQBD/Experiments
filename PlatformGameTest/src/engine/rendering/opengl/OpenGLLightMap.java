package engine.rendering.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.io.File;
import java.io.IOException;

import engine.rendering.Dither;
import engine.util.Util;

public class OpenGLLightMap {
	private final int width;
	private final int height;
	private final double scale;
	private int id;
	private int fbo;

	public OpenGLLightMap(int radius) {
		this(radius * 2, radius * 2, 1);
		generate(radius);
	}

	public OpenGLLightMap(int width, int height, double scale) {
		this.width = width;
		this.height = height;
		this.scale = scale;
		this.id = OpenGLUtil.createTexture(width, height, (byte[]) null,
				OpenGLUtil.FILTER_LINEAR);

		this.fbo = glGenFramebuffers();
		OpenGLUtil.bindRenderTarget(fbo, this.width, this.height, this.width,
				this.height);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
				GL_TEXTURE_2D, id, 0);
		clear();
	}

	public void dispose() {
		if (fbo != 0) {
			glDeleteFramebuffers(fbo);
			fbo = 0;
		}
		if (id != 0) {
			glDeleteTextures(id);
			id = 0;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	public void clear() {
		OpenGLUtil.bindRenderTarget(fbo, this.width, this.height, this.width,
				this.height);
		OpenGLUtil.clear(0.0, 0.0, 0.0, 0.0);
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

	private void generate(int radius) {
		OpenGLUtil.updateTexture(id, generateLighting(radius, width, height),
				0, 0, width, height);
	}

	public void save(String fileName) throws IOException {
		OpenGLUtil.saveTexture(id, width, height, "png", new File(fileName));
	}

	public int getId() {
		return id;
	}

	public void addLight(OpenGLLightMap light, int x, int y, int mapStartX,
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

		OpenGLUtil.bindRenderTarget(fbo, this.width, this.height, this.width,
				this.height);
		glBlendFunc(GL_ONE, GL_ONE);
		OpenGLUtil.drawRect(light.id, xStart, yStart, drawWidth, drawHeight,
				texMinX, texMinY, texWidth, texHeight);
	}
}
