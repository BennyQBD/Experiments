package engine.rendering;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class Bitmap {
	private final IRenderDevice device;
	private final int width;
	private final int height;
	private int id;

	public Bitmap(IRenderDevice device, int width, int height) {
		this.device = device;
		this.width = width;
		this.height = height;
		this.id = device.createTexture(width, height, (int[]) null,
				IRenderDevice.FILTER_NEAREST);
	}

	public Bitmap(IRenderDevice device, String fileName)
			throws IOException {
		this.device = device;
		BufferedImage image = ImageIO.read(new File(fileName));

		this.width = image.getWidth();
		this.height = image.getHeight();

		int imgPixels[] = new int[width * height];
		image.getRGB(0, 0, width, height, imgPixels, 0, width);
		this.id = device.createTexture(width, height, imgPixels,
				IRenderDevice.FILTER_NEAREST);
	}

	public void dispose() {
		id = device.releaseTexture(id);
	}

	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	public int getHardwareID() {
		return id;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void clear(int color) {
		int[] newTex = new int[width * height];
		Arrays.fill(newTex, color);
		device.updateTexture(id, newTex, 0, 0, width, height);
	}

	public int[] getPixels(int[] dest) {
		return device.getTexture(id, dest, width, height);
	}

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

	public void save(String filetype, File file) throws IOException {
		BufferedImage output = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		int[] displayComponents = ((DataBufferInt) output.getRaster()
				.getDataBuffer()).getData();
		device.getTexture(id, displayComponents, width, height);

		ImageIO.write(output, "png", file);
	}

	public void setPixels(int[] colors, int x, int y, int width, int height) {
		device.updateTexture(id, colors, x, y, width, height);
	}
}
