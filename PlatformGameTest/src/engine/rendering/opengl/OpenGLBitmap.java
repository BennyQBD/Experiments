package engine.rendering.opengl;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
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

	@Override
	public int[] getPixels(int[] dest) {
		if (dest == null || dest.length < width * height) {
			dest = new int[width * height];
		}

		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
		glBindTexture(GL_TEXTURE_2D, id);
		glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		for (int i = 0; i < width * height; i++) {
			int r = buffer.get() & 0xFF;
			int g = buffer.get() & 0xFF;
			int b = buffer.get() & 0xFF;
			int a = buffer.get() & 0xFF;
			dest[i] = ARGBColor.makeColor(a, r, g, b);
		}

		return dest;
	}
	
	@Override 
	public int[] getPixels(int[] dest, int x, int y, int width, int height) {
		if (dest == null || dest.length < width * height) {
			dest = new int[width * height];
		}
		int[] pixels = getPixels(null);
		for(int j = 0, srcY = 0; j < height; j++, srcY++) {
			for(int i = 0, srcX = 0; i < width; i++, srcX++) {
				dest[i + j * width] = pixels[srcX + srcY * this.width];
			}
		}
		return dest;
	}
	
	@Override
	public void save(String filetype, File file) throws IOException {
		BufferedImage output = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		int[] displayComponents = ((DataBufferInt) output.getRaster()
				.getDataBuffer()).getData();
		getPixels(displayComponents);

		ImageIO.write(output, filetype, file);
	}

	@Override
	public void setPixels(int[] colors, int x, int y, int width, int height) {
		// TODO Auto-generated method stub

	}
}
