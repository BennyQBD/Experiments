package engine.rendering.opengl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

import engine.rendering.ARGBColor;
public class OpenGLUtil {
	public static enum BlendMode {
		SPRITE, ADD_LIGHT, APPLY_LIGHT
	}

	public static final int FILTER_NEAREST = GL_NEAREST;
	public static final int FILTER_LINEAR = GL_LINEAR;

	private static class FramebufferData {
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

	private static final Map<Integer, FramebufferData> framebuffers = new HashMap<>();

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

	// private static FloatBuffer makeBuffer(float[] data) {
	// FloatBuffer result = BufferUtils.createFloatBuffer(data.length);
	// result.put(data);
	// result.flip();
	// return result;
	// }
	//
	// private static IntBuffer makeBuffer(int[] data) {
	// IntBuffer result = BufferUtils.createIntBuffer(data.length);
	// result.put(data);
	// result.flip();
	// return result;
	// }

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

	public static int createTexture(int width, int height, int[] data,
			int filter) {
		return createTexture(width, height, data, filter, GL_RGBA8);
	}

	public static int createTexture(int width, int height, byte[] data,
			int filter) {
		return createTexture(width, height, byteToInt(data), filter,
				GL_INTENSITY8);
	}

	public static int releaseTexture(int id) {
		if (id != 0) {
			glDeleteTextures(id);
		}
		return 0;
	}

	private static int createTexture(int width, int height, int[] data,
			int filter, int format) {
		int id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
		glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, GL_RGBA,
				GL_UNSIGNED_BYTE, makeRGBABuffer(data, width, height));
		return id;
	}

	public static void updateTexture(int id, int[] data, int x, int y,
			int width, int height) {
		glBindTexture(GL_TEXTURE_2D, id);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA,
				GL_UNSIGNED_BYTE, makeRGBABuffer(data, width, height));
	}

	public static void updateTexture(int id, byte[] data, int x, int y,
			int width, int height) {
		updateTexture(id, byteToInt(data), x, y, width, height);
	}

	public static int[] getTexture(int id, int[] dest, int width, int height) {
		if (dest == null || dest.length < width * height) {
			dest = new int[width * height];
		}

		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		glBindTexture(GL_TEXTURE_2D, id);
		glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		return byteBufferToInt(dest, buffer, width, height);
	}

	public static void saveTexture(int id, int width, int height,
			String filetype, File file) throws IOException {
		BufferedImage output = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		int[] displayComponents = ((DataBufferInt) output.getRaster()
				.getDataBuffer()).getData();
		getTexture(id, displayComponents, width, height);

		ImageIO.write(output, "png", file);
	}

	public static void init(int width, int height, int projWidth,
			int projHeight) {
		framebuffers.put(0, new FramebufferData(width, height, projWidth,
				projHeight));
		OpenGLUtil.bindRenderTarget(0);
		glDisable(GL_DEPTH_TEST);
		glEnable(GL_TEXTURE_2D);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	public static int createRenderTarget(int width, int height, int projWidth,
			int projHeight, int texId) {
		int fbo = glGenFramebuffers();
		FramebufferData data = new FramebufferData(width, height, projWidth,
				projHeight);
		framebuffers.put(fbo, data);
		if (texId != 0 && texId != -1) {
			OpenGLUtil.bindRenderTarget(fbo);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0,
					GL_TEXTURE_2D, texId, 0);
		}
		return fbo;
	}

	public static int releaseRenderTarget(int fbo) {
		if (fbo != 0 && fbo != -1) {
			glDeleteFramebuffers(fbo);
			framebuffers.remove(fbo);
		}
		return 0;
	}

	public static void bindRenderTarget(int fbo) {
		FramebufferData data = framebuffers.get(fbo);
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, data.width, data.height, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);

		glViewport(0, 0, data.projWidth, data.projHeight);
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
	}

	// public static void bindRenderTarget(int fbo, int width, int height,
	// int projWidth, int projHeight) {
	// glMatrixMode(GL_PROJECTION);
	// glLoadIdentity();
	// glOrtho(0, width, height, 0, 1, -1);
	// glMatrixMode(GL_MODELVIEW);
	//
	// glViewport(0, 0, projWidth, projHeight);
	// glBindFramebuffer(GL_FRAMEBUFFER, fbo);
	// }

	public static void clear(double a, double r, double g, double b) {
		glClearColor((float) r, (float) g, (float) b, (float) a);
		glClear(GL_COLOR_BUFFER_BIT);
	}

	public static void drawRect(int fbo, int texId, BlendMode mode, double x,
			double y, double width, double height, double texX, double texY,
			double texWidth, double texHeight) {
		bindRenderTarget(fbo);
		switch(mode) {
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
		glBindTexture(GL_TEXTURE_2D, texId);
		glBegin(GL_QUADS);
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

	// public static int createBuffer(float[] data, int elementSize) {
	// int bo = glGenBuffers();
	// glBindBuffer(GL_ARRAY_BUFFER, bo);
	// glBufferData(GL_ARRAY_BUFFER, makeBuffer(data), GL_STATIC_DRAW);
	// return bo;
	// }
	//
	// public static int releaseBuffer(int bo) {
	// if (bo != 0) {
	// glDeleteBuffers(bo);
	// }
	// return 0;
	// }
	//
	// public static void drawMesh(int vertexBuffer, int texCoordBuffer, int
	// numVertices) {
	// glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
	// glVertexPointer(3, GL_FLOAT, 0, 0);
	// glBindBuffer(GL_ARRAY_BUFFER, texCoordBuffer);
	// glTexCoordPointer(3, GL_FLOAT, 0, 0);
	// glEnableClientState(GL_VERTEX_ARRAY);
	// glEnableClientState(GL_TEXTURE_COORD_ARRAY);
	// glDrawArrays(GL_TRIANGLES, 0, numVertices);
	// }

	// public static int createMesh() {
	// return glGenVertexArrays();
	// }
	//
	// public static int releaseMesh(int vao) {
	// if (vao != 0) {
	// glDeleteVertexArrays(vao);
	// }
	// return 0;
	// }
	//
	// public static int createMeshBuffer(int vao, float[] data, int
	// elementSize,
	// int attribLoc) {
	// glBindVertexArray(vao);
	// int bo = glGenBuffers();
	// glBindBuffer(GL_ARRAY_BUFFER, bo);
	// glBufferData(GL_ARRAY_BUFFER, makeBuffer(data), GL_STATIC_DRAW);
	// glEnableVertexAttribArray(attribLoc);
	// glVertexAttribPointer(attribLoc, elementSize, GL_FLOAT, false, 0, 0);
	// return bo;
	// }
	//
	// public static int createMeshIndexBuffer(int vao, int[] data) {
	// glBindVertexArray(vao);
	// int bo = glGenBuffers();
	// glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bo);
	// glBufferData(GL_ELEMENT_ARRAY_BUFFER, makeBuffer(data), GL_STATIC_DRAW);
	// return bo;
	// }
	//
	// public static int releaseMeshBuffer(int bo) {
	// if (bo != 0) {
	// glDeleteBuffers(bo);
	// }
	// return 0;
	// }
}
