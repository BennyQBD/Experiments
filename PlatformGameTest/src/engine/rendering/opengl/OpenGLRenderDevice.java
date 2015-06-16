package engine.rendering.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;

import engine.rendering.ARGBColor;
import engine.rendering.IRenderDevice;

public class OpenGLRenderDevice implements IRenderDevice {
	private class FramebufferData {
		public FramebufferData(int width, int height, int projWidth,
				int projHeight) {
			this.width = width;
			this.height = height;
			this.projWidth = projWidth;
			this.projHeight = projHeight;
		}

		private int width;
		private int height;
		private int projWidth;
		private int projHeight;
	}
	
	private class TextureData {
		public TextureData(int width, int height) {
			this.width = width;
			this.height = height;
		}
		private int width;
		private int height;
	}

	private final Map<Integer, FramebufferData> framebuffers = new HashMap<>();
	private final Map<Integer, TextureData> textures = new HashMap<>();
	private int boundFbo;
	private int boundTex;

	public OpenGLRenderDevice(int width, int height, int projWidth,
			int projHeight) {
		boundFbo = -1;
		boundTex = -1;

		framebuffers.put(0, new FramebufferData(width, height, projWidth,
				projHeight));
		bindRenderTarget(0);
		glDisable(GL_DEPTH_TEST);
		glEnable(GL_TEXTURE_2D);

		glEnable(GL_BLEND);
	}
	
	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public int createTexture(int width, int height, int[] data, int filter) {
		return createTexture(width, height, data, filter, GL_RGBA8);
	}

	@Override
	public int createTexture(int width, int height, byte[] data, int filter) {
		return createTexture(width, height, byteToInt(data), filter,
				GL_INTENSITY8);
	}

	@Override
	public int releaseTexture(int id) {
		if (id != 0) {
			glDeleteTextures(id);
			textures.remove(id);
			if (id == boundTex) {
				boundTex = -1;
			}
		}
		return 0;
	}

	@Override
	public void updateTexture(int id, int[] data, int x, int y, int width,
			int height) {
		bindTexture(id);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA,
				GL_UNSIGNED_BYTE, makeRGBABuffer(data, width, height));
	}

	@Override
	public void updateTexture(int id, byte[] data, int x, int y, int width,
			int height) {
		updateTexture(id, byteToInt(data), x, y, width, height);
	}

	@Override
	public int[] getTexture(int id, int[] dest, int x, int y, int width, int height) {
		if (dest == null || dest.length < width * height) {
			dest = new int[width * height];
		}
		TextureData tex = textures.get(id);
		ByteBuffer buffer = BufferUtils.createByteBuffer(tex.width * tex.height * 4);
		bindTexture(id);
		glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		
		if(x == 0 && y == 0 && width == tex.width && height == tex.height) {
			return byteBufferToInt(dest, buffer, tex.width, tex.height);
		}

		int[] pixels = byteBufferToInt(new int[tex.width*tex.height], buffer, tex.width, tex.height);
		for (int j = 0, srcY = y; j < height; j++, srcY++) {
			for (int i = 0, srcX = x; i < width; i++, srcX++) {
				dest[i + j * width] = pixels[srcX + srcY * tex.width];
			}
		}
		return dest;
	}

	@Override
	public int createRenderTarget(int width, int height, int projWidth,
			int projHeight, int texId) {
		int fbo = glGenFramebuffers();
		FramebufferData data = new FramebufferData(width, height, projWidth,
				projHeight);
		framebuffers.put(fbo, data);
		if (texId != 0 && texId != -1) {
			bindRenderTarget(fbo);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
					GL_TEXTURE_2D, texId, 0);
		}
		return fbo;
	}
	
	@Override
	public int getRenderTargetWidth(int fbo) {
		return framebuffers.get(fbo).width;
	}
	
	@Override
	public int getRenderTargetHeight(int fbo) {
		return framebuffers.get(fbo).height;
	}

	@Override
	public int releaseRenderTarget(int fbo) {
		if (fbo != 0 && fbo != -1) {
			glDeleteFramebuffers(fbo);
			framebuffers.remove(fbo);
			if (fbo == boundFbo) {
				boundFbo = -1;
			}
		}
		return 0;
	}

	@Override
	public void clear(int fbo, double a, double r, double g, double b) {
		bindRenderTarget(fbo);
		glClearColor((float) r, (float) g, (float) b, (float) a);
		glClear(GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void drawRect(int fbo, int texId, BlendMode mode, double x,
			double y, double width, double height, double texX, double texY,
			double texWidth, double texHeight) {
		drawRect(fbo, texId, mode, x, y, width, height, texX, texY, texWidth,
				texHeight, 0xFFFFFF, 1.0);
	}

	@Override
	public void drawRect(int fbo, int texId, BlendMode mode, double x,
			double y, double width, double height, double texX, double texY,
			double texWidth, double texHeight, int colorMask,
			double transparency) {
		bindRenderTarget(fbo);

		glColor4f((float) ARGBColor.getComponentd(colorMask, 1),
				(float) ARGBColor.getComponentd(colorMask, 2),
				(float) ARGBColor.getComponentd(colorMask, 3),
				(float) transparency);

		switch (mode) {
		case ADD_LIGHT:
			glBlendFunc(GL_ONE, GL_ONE);
			break;
		case APPLY_LIGHT:
			glBlendFunc(GL_DST_COLOR, GL_ZERO);
			break;
		case SPRITE:
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			break;
		}
		bindTexture(texId);
		
		glBegin(GL_TRIANGLE_FAN);
		{
			glTexCoord2f((float) texX, (float) (texY));
			glVertex2f((float) x, (float) y);
			glTexCoord2f((float) texX, (float) (texY + texHeight));
			glVertex2f((float) x, (float) (y + height));
			glTexCoord2f((float) (texX + texWidth), (float) (texY + texHeight));
			glVertex2f((float) (x + width), (float) (y + height));
			glTexCoord2f((float) (texX + texWidth), (float) (texY));
			glVertex2f((float) (x + width), (float) y);
		}
		glEnd();
	}

	private void bindRenderTarget(int fbo) {
		if (fbo == boundFbo) {
			return;
		}
		FramebufferData data = framebuffers.get(fbo);
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
		boundFbo = fbo;
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, data.width, data.height, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);

		glViewport(0, 0, data.projWidth, data.projHeight);
	}

	private void bindTexture(int texId) {
		if (texId == boundTex) {
			return;
		}
		glBindTexture(GL_TEXTURE_2D, texId);
		boundTex = texId;
	}

	private int createTexture(int width, int height, int[] data, int filter,
			int format) {
		int id = glGenTextures();
		bindTexture(id);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
		glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, GL_RGBA,
				GL_UNSIGNED_BYTE, makeRGBABuffer(data, width, height));
		textures.put(id, new TextureData(width, height));
		return id;
	}

	private static int[] byteToInt(byte[] data) {
		if (data == null) {
			return null;
		}
		int[] result = new int[data.length];
		for (int i = 0; i < result.length; i++) {
			int val = data[i] & 0xFF;
			result[i] = (val << 24) | (val << 16) | (val << 8) | val;
		}
		return result;
	}

	private static ByteBuffer makeRGBABuffer(int[] data, int width, int height) {
		if (data == null) {
			return null;
		}
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		for (int i = 0; i < width * height; i++) {
			int pixel = data[i];
			buffer.put((byte) ((pixel >> 16) & 0xFF));
			buffer.put((byte) ((pixel >> 8) & 0xFF));
			buffer.put((byte) ((pixel) & 0xFF));
			buffer.put((byte) ((pixel >> 24) & 0xFF));
		}
		buffer.flip();
		return buffer;
	}

	private static int[] byteBufferToInt(int[] data, ByteBuffer buffer,
			int width, int height) {
		for (int i = 0; i < width * height; i++) {
			int r = buffer.get() & 0xFF;
			int g = buffer.get() & 0xFF;
			int b = buffer.get() & 0xFF;
			int a = buffer.get() & 0xFF;
			data[i] = ARGBColor.makeColor(a, r, g, b);
		}
		return data;
	}
}
