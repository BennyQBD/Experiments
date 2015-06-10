package engine.rendering.opengl;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

import engine.rendering.ARGBColor;
import engine.rendering.IBitmap;

public class OpenGLBitmap implements IBitmap {
	private int width;
	private int height;
	private int id;

	public OpenGLBitmap(int width, int height) {
		this.width = width;
		this.height = height;
		initTex(null);
	}

	public OpenGLBitmap(String fileName) throws IOException {
		BufferedImage image = ImageIO.read(new File(fileName));

		this.width = image.getWidth();
		this.height = image.getHeight();

		int imgPixels[] = new int[width * height];
		image.getRGB(0, 0, width, height, imgPixels, 0, width);
		ByteBuffer buffer = BufferUtils.createByteBuffer(height * width * 4);
		for (int i = 0; i < imgPixels.length; i++) {
			int pixel = imgPixels[i];
			buffer.put((byte) ((pixel >> 16) & 0xFF));
			buffer.put((byte) ((pixel >> 8) & 0xFF));
			buffer.put((byte) ((pixel) & 0xFF));
			buffer.put((byte) ((pixel >> 24) & 0xFF));
		}
		buffer.flip();
		initTex(buffer);
	}

	@Override
	protected void finalize() throws Throwable {
		glDeleteTextures(id);
		super.finalize();
	}

	private void initTex(ByteBuffer buffer) {
		this.id = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, id);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA,
				GL_UNSIGNED_BYTE, buffer);
	}

	private void setTex(ByteBuffer buffer) {
		glBindTexture(GL_TEXTURE_2D, id);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA,
				GL_UNSIGNED_BYTE, buffer);
	}

	@Override
	public int getHardwareAccelerationID() {
		return id;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	// @Override
	// public int getPixel(int x, int y) {
	// ByteBuffer buffer = BufferUtils.createByteBuffer(4);
	// glBindTexture(GL_TEXTURE_2D, id);
	// glReadPixels(x, y, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
	//
	// int r = buffer.get() & 0xFF;
	// int g = buffer.get() & 0xFF;
	// int b = buffer.get() & 0xFF;
	// int a = buffer.get() & 0xFF;
	// return ARGBColor.makeColor(a, r, g, b);
	// // return 0;
	// }

	@Override
	public void clear(int color) {
		ByteBuffer buffer = BufferUtils.createByteBuffer(height * width * 4);
		int a = ARGBColor.getComponent(color, 0);
		int r = ARGBColor.getComponent(color, 1);
		int g = ARGBColor.getComponent(color, 2);
		int b = ARGBColor.getComponent(color, 3);
		for (int i = 0; i < height * width; i++) {
			buffer.put((byte) (r));
			buffer.put((byte) (g));
			buffer.put((byte) (b));
			buffer.put((byte) (a));
		}
		buffer.flip();
		setTex(buffer);
	}

	// @Override
	// public void setPixel(int x, int y, int color) {
	// // TODO Auto-generated method stub
	//
	// }

	@Override
	public int[] getPixels(int[] dest, int x, int y, int width, int height) {
		if (dest == null || dest.length < width * height) {
			dest = new int[width * height];
		}

		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		glBindTexture(GL_TEXTURE_2D, id);
		glReadPixels(x, y, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		for (x = 0; x < width; x++) {
			for (y = 0; y < height; y++) {
				int i = (x + (width * y)) * 4;
				int r = buffer.get(i) & 0xFF;
				int g = buffer.get(i + 1) & 0xFF;
				int b = buffer.get(i + 2) & 0xFF;
				int a = buffer.get(i + 3) & 0xFF;
				dest[x + (height - (y + 1)) * width] = ARGBColor.makeColor(a,
						r, g, b);
			}
		}
		
		
		// int xEnd = x + width;
		// int yEnd = y + height;
		// for(int j = 0; j < height; j++) {
		// for(int i = 0; i < width; i++) {
		// int r = buffer.get() & 0xFF;
		// int g = buffer.get() & 0xFF;
		// int b = buffer.get() & 0xFF;
		// int a = buffer.get() & 0xFF;
		// dest[i + (height - j - 1) * width] = ARGBColor.makeColor(a, r, g, b);
		// }
		// }

		return dest;
	}

	@Override
	public void save(String filetype, File file) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPixels(int[] colors, int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}
}
