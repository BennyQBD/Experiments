package engine.rendering.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.lwjgl.BufferUtils;

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
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_INTENSITY8, 256, 256, 0, GL_RGBA,
				GL_UNSIGNED_BYTE, (ByteBuffer) null);
		
		this.fbo = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, id, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	@Override
	protected void finalize() throws Throwable {
		glDeleteFramebuffers(fbo);
		glDeleteTextures(id);
		super.finalize();
	}

	public void clear() {
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClear(GL_COLOR_BUFFER_BIT);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

//	public double getLight(int x, int y) {
//		return (double) ((lighting[x + y * width] & 0xFF));
//	}

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
		byte[] lighting = new byte[width*height];
		Arrays.fill(lighting, (byte)0);
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
		
		ByteBuffer buffer = BufferUtils.createByteBuffer(width*height*4);
		for(int i = 0; i < width*height; i++) {
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
	
	public int getId() {
		return id;
	}
	
	public void addLight(OpenGLLightMap light, int x, int y, int mapStartX,
			int mapStartY, int width, int height) {
		double texMinX = ((double) mapStartX / ((double) light.getWidth()*scale));
		double texMinY = ((double) mapStartY / ((double) light.getHeight()*scale));
		double texMaxX = texMinX + ((double) width)/ ((double) light.getWidth()*scale);
		double texMaxY = texMinY + ((double) height)/ ((double) light.getHeight()*scale);
		
		double xStart = x/scale;
		double xEnd = (x+width)/scale;
		double yStart = y/scale;
		double yEnd = (y+height)/scale;
		
		glBindTexture(GL_TEXTURE_2D, light.id);
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
		glBlendFunc(GL_ONE, GL_ONE);
		glBegin(GL_QUADS);
		{
			glTexCoord2f((float) texMinX, (float) texMinY);
			glVertex2f((float)xStart, (float)yStart);
			glTexCoord2f((float) texMinX, (float) texMaxY);
			glVertex2f((float)xStart, (float)yEnd);
			glTexCoord2f((float) texMaxX, (float) texMaxY);
			glVertex2f((float)xEnd, (float)yEnd);
			glTexCoord2f((float) texMaxX, (float) texMinY);
			glVertex2f((float)xEnd, (float)yStart);
		}
		glEnd();
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
}
