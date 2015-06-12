package engine.rendering;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class ArrayBitmap implements IBitmap {
	private final int width;
	private final int height;
	private final int colors[];

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public int getHardwareID() {
		return -1;
	}

	public int getPixel(int i, int j) {
		return colors[getIndex(i, j)];
	}

	public ArrayBitmap(int width, int height) {
		this.width = width;
		this.height = height;
		this.colors = new int[width * height];
	}

	public ArrayBitmap(String fileName) throws IOException {
		BufferedImage image = ImageIO.read(new File(fileName));

		this.width = image.getWidth();
		this.height = image.getHeight();

		int imgPixels[] = new int[getNumPixels()];
		image.getRGB(0, 0, width, height, imgPixels, 0, width);
		this.colors = imgPixels;
	}

	@Override
	public void clear(int color) {
		Arrays.fill(colors, color);
	}

	public void setPixel(int x, int y, int color) {
		colors[getIndex(x, y)] = color;
	}

	@Override
	public int[] getPixels(int[] dest) {
		return getPixels(dest, 0, 0, width, height);
	}
	
	@Override
	public int[] getPixels(int[] dest, int x, int y, int width, int height) {
		if (dest == null || dest.length < width*height) {
			dest = new int[width*height];
		}
		for(int j = 0, srcY = y; j < height; j++, srcY++) {
			for(int i = 0, srcX = x; i < width; i++, srcX++) {
				dest[i + j * width] = colors[srcX + srcY * this.width];
			}
		}
		return dest;
	}
	
	@Override
	public void setPixels(int[] colors, int x, int y, int width, int height) {
		for(int j = y, b = 0; j < y + height; j++, b++) {
			for(int i = x, a = 0; i < x + width; i++, a++) {
				this.colors[i + j * this.width] = colors[a + b * width];
			}
		}
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

	private int getNumPixels() {
		return width * height;
	}

	private int getIndex(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			throw new IllegalArgumentException("Location (" + x + ", " + y
					+ ") is not within the range of (0,0) to (" + width + ", "
					+ height + ")");
		}

		return y * width + x;
	}

	@Override
	public void dispose() {
		// No clean up necessary
	}
}
