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
	private final int id;
	private int fbo;

	public OpenGLLightMap(int radius) {
		this(radius * 2, radius * 2, 1);
		generate(radius);
	}

	public OpenGLLightMap(int width, int height, double scale) {
		this.width = width;
		this.height = height;
		this.scale = scale;
		this.id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_INTENSITY8, width, height, 0,
				GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);

		this.fbo = glGenFramebuffers();
		bindAsRenderTarget();
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
				GL_TEXTURE_2D, id, 0);
		clear();
	}

	private void bindAsRenderTarget() {
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, 0, height, 1, -1);
		glMatrixMode(GL_MODELVIEW);

		glViewport(0, 0, width, height);
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
	}

	@Override
	protected void finalize() throws Throwable {
		glDeleteFramebuffers(fbo);
		glDeleteTextures(id);
		super.finalize();
	}

	public void clear() {
		bindAsRenderTarget();
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
	}

	// public double getLight(int x, int y) {
	// return (double) ((lighting[x + y * width] & 0xFF));
	// }

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
		byte[] lighting = new byte[width * height];
		Arrays.fill(lighting, (byte) 0);
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

		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		for (int i = 0; i < width * height; i++) {
			byte val = lighting[i];
			buffer.put(val);
			buffer.put(val);
			buffer.put(val);
			buffer.put(val);
		}
		buffer.flip();
		glBindTexture(GL_TEXTURE_2D, id);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA,
				GL_UNSIGNED_BYTE, buffer);
	}

	@SuppressWarnings("unused")
	private void save(String fileName) {
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		glBindTexture(GL_TEXTURE_2D, id);
		glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		BufferedImage output = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		int[] displayComponents = ((DataBufferInt) output.getRaster()
				.getDataBuffer()).getData();
		for (int i = 0; i < width * height; i++) {
			int r = buffer.get() & 0xFF;
			int g = buffer.get() & 0xFF;
			int b = buffer.get() & 0xFF;
			int a = buffer.get() & 0xFF;
			displayComponents[i] = ARGBColor.makeColor(0xFF, r, g, b);
		}

		try {
			ImageIO.write(output, "png", new File(fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getId() {
		return id;
	}

	public void addLight(OpenGLLightMap light, int x, int y, int mapStartX,
			int mapStartY, int width, int height) {
		double scaleAmt = 1.0 / scale;
		double texCoordScale = 1.0 / light.getScale();
		double texMinX = texCoordScale
				* ((double) mapStartX / ((double) light.getWidth()));
		double texMinY = texCoordScale
				* ((double) mapStartY / ((double) light.getHeight()));
		double texMaxX = texMinX + texCoordScale * ((double) width)
				/ ((double) light.getWidth());
		double texMaxY = texMinY + texCoordScale * ((double) height)
				/ ((double) light.getHeight());

		double xStart = x;
		double xEnd = (x + width);
		double yStart = y;
		double yEnd = (y + height);

		xStart *= scaleAmt;
		xEnd *= scaleAmt;
		yStart *= scaleAmt;
		yEnd *= scaleAmt;

		glBindTexture(GL_TEXTURE_2D, light.id);
		bindAsRenderTarget();
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
