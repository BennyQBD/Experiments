package engine.rendering.opengl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import engine.rendering.IBitmap;

public class OpenGLBitmap implements IBitmap {
	private final int width;
	private final int height;
	private int id;

	public OpenGLBitmap(int width, int height) {
		this.width = width;
		this.height = height;
		this.id = OpenGLUtil.createTexture(width, height, (int[]) null,
				OpenGLUtil.FILTER_NEAREST);
	}

	public OpenGLBitmap(String fileName) throws IOException {
		BufferedImage image = ImageIO.read(new File(fileName));

		this.width = image.getWidth();
		this.height = image.getHeight();

		int imgPixels[] = new int[width * height];
		image.getRGB(0, 0, width, height, imgPixels, 0, width);
		this.id = OpenGLUtil.createTexture(width, height, imgPixels, OpenGLUtil.FILTER_NEAREST);
	}

	@Override
	public void dispose() {
		id = OpenGLUtil.releaseTexture(id);
	}

	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	@Override
	public int getHardwareID() {
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
		int[] newTex = new int[width*height];
		Arrays.fill(newTex, color);
		OpenGLUtil.updateTexture(id, newTex, 0, 0, width, height);
	}

	@Override
	public int[] getPixels(int[] dest) {
		return OpenGLUtil.getTexture(id, dest, width, height);
	}

	@Override
	public int[] getPixels(int[] dest, int x, int y, int width, int height) {
		if (dest == null || dest.length < width * height) {
			dest = new int[width * height];
		}
		int[] pixels = getPixels(null);
		for (int j = 0, srcY = 0; j < height; j++, srcY++) {
			for (int i = 0, srcX = 0; i < width; i++, srcX++) {
				dest[i + j * width] = pixels[srcX + srcY * this.width];
			}
		}
		return dest;
	}

	@Override
	public void save(String filetype, File file) throws IOException {
		OpenGLUtil.saveTexture(id, width, height, filetype, file);
	}

	@Override
	public void setPixels(int[] colors, int x, int y, int width, int height) {
		OpenGLUtil.updateTexture(id, colors, x, y, width, height);
	}
}
