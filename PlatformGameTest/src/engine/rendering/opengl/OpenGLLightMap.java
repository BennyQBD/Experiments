package engine.rendering.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

import engine.rendering.ARGBColor;
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
		this.id = OpenGLUtil.makeTexture(width, height, (byte[]) null,
				GL_LINEAR);

		this.fbo = glGenFramebuffers();
		OpenGLUtil.bindRenderTarget(fbo, this.width, this.height, this.width,
				this.height);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
				GL_TEXTURE_2D, id, 0);
		clear();
	}

	// private void bindAsRenderTarget() {
	// glMatrixMode(GL_PROJECTION);
	// glLoadIdentity();
	// glOrtho(0, width, height, 0, 1, -1);
	// glMatrixMode(GL_MODELVIEW);
	//
	// glViewport(0, 0, width, height);
	// glBindFramebuffer(GL_FRAMEBUFFER, fbo);
	// }

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
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
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

	private static byte calcLight(int radius, int radiusSq, int distX, int distY) {
		int distCenterSq = distY * distY + distX * distX;
		if (distCenterSq > radiusSq) {
			return (byte) 0;
		}
		return toData(((double) radius / (double) (distCenterSq)));
	}

	private static byte[] generateLighting(int radius, int width, int height) {
		byte[] result = new byte[width * height];
		int centerX = width / 2;
		int centerY = height / 2;
		int radiusSq = radius * radius;
		for (int j = 0, distY = -centerY; j < height; j++, distY++) {
			for (int i = 0, distX = -centerX; i < width; i++, distX++) {
				result[i + j * width] = calcLight(radius, radiusSq, distX,
						distY);
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
		double texMaxX = texMinX + texScale * ((double) width)
				/ ((double) light.getWidth());
		double texMaxY = texMinY + texScale * ((double) height)
				/ ((double) light.getHeight());

		double xStart = x * posScale;
		double xEnd = (x + width) * posScale;
		double yStart = y * posScale;
		double yEnd = (y + height) * posScale;

		yStart = this.height - yStart;
		yEnd = this.height - yEnd;

		OpenGLUtil.bindRenderTarget(fbo, this.width, this.height, this.width,
				this.height);
		glBindTexture(GL_TEXTURE_2D, light.id);
		glBlendFunc(GL_ONE, GL_ONE);
		glBegin(GL_QUADS);
		glTexCoord2f((float) texMinX, (float) texMinY);
		glVertex2f((float) xStart, (float) yStart);
		glTexCoord2f((float) texMinX, (float) texMaxY);
		glVertex2f((float) xStart, (float) yEnd);
		glTexCoord2f((float) texMaxX, (float) texMaxY);
		glVertex2f((float) xEnd, (float) yEnd);
		glTexCoord2f((float) texMaxX, (float) texMinY);
		glVertex2f((float) xEnd, (float) yStart);
		glEnd();
	}
}
