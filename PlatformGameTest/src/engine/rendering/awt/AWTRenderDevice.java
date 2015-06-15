package engine.rendering.awt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import engine.rendering.ARGBColor;
import engine.rendering.IRenderDevice;
import engine.util.Util;

public class AWTRenderDevice implements IRenderDevice {
	private class ArrayBitmap {
		private final int width;
		private final int height;
		private final int pixels[];

		public ArrayBitmap(int width, int height, int[] pixels) {
			this.width = width;
			this.height = height;
			this.pixels = new int[width * height];
			if (pixels == null) {
				Arrays.fill(this.pixels, 0);
			} else {
				for (int i = 0; i < this.pixels.length; i++) {
					this.pixels[i] = pixels[i];
				}
			}
		}
	}

	private class Framebuffer {
		private final int width;
		private final int height;
		private final int projWidth;
		private final int projHeight;
		private final int texId;

		public Framebuffer(int texId, int width, int height, int projWidth,
				int projHeight) {
			this.width = width;
			this.height = height;
			this.projWidth = projWidth;
			this.projHeight = projHeight;
			this.texId = texId;
		}
	}

	private final List<ArrayBitmap> textures;
	private final List<Framebuffer> framebuffers;

	public AWTRenderDevice(int width, int height, int projWidth, int projHeight) {
		textures = new ArrayList<>();
		framebuffers = new ArrayList<>();

		textures.add(new ArrayBitmap(width, height, null));
		framebuffers.add(new Framebuffer(0, width, height, projWidth,
				projHeight));
	}

	@Override
	public void dispose() {
	}

	@Override
	public int createTexture(int width, int height, int[] data, int filter) {
		textures.add(new ArrayBitmap(width, height, data));
		return textures.size() - 1;
	}

	@Override
	public int createTexture(int width, int height, byte[] data, int filter) {
		return createTexture(width, height, byteToInt(data), filter);
	}

	@Override
	public int releaseTexture(int id) {
		if (id != 0) {
			textures.set(id, null);
		}
		return 0;
	}

	@Override
	public void updateTexture(int id, int[] data, int x, int y, int width,
			int height) {
		ArrayBitmap tex = textures.get(id);
		for (int j = y, b = 0; j < y + height; j++, b++) {
			for (int i = x, a = 0; i < x + width; i++, a++) {
				tex.pixels[i + j * tex.width] = data[a + b * width];
			}
		}
	}

	@Override
	public void updateTexture(int id, byte[] data, int x, int y, int width,
			int height) {
		updateTexture(id, byteToInt(data), x, y, width, height);
	}

	@Override
	public int[] getTexture(int id, int[] dest, int x, int y, int width,
			int height) {
		ArrayBitmap tex = textures.get(id);
		if (dest == null || dest.length < width * height) {
			dest = new int[width * height];
		}
		for (int j = 0, srcY = y; j < height; j++, srcY++) {
			for (int i = 0, srcX = x; i < width; i++, srcX++) {
				dest[i + j * width] = tex.pixels[srcX + srcY * tex.width];
			}
		}
		return dest;
	}

	@Override
	public int createRenderTarget(int width, int height, int projWidth,
			int projHeight, int texId) {
		framebuffers.add(new Framebuffer(texId, width, height, projWidth,
				projHeight));
		return framebuffers.size() - 1;
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
		if (fbo != 0) {
			framebuffers.set(fbo, null);
		}
		return 0;
	}

	@Override
	public void clear(int fbo, double a, double r, double g, double b) {
		Framebuffer framebuffer = framebuffers.get(fbo);
		ArrayBitmap texture = textures.get(framebuffer.texId);
		Arrays.fill(texture.pixels, ARGBColor.makeColor(a, r, g, b));
	}

	@Override
	public void drawRect(int fbo, int texId, BlendMode mode, double x,
			double y, double width, double height, double texX, double texY,
			double texWidth, double texHeight) {
		this.drawRect(fbo, texId, mode, x, texY, width, height, texX, texY,
				texWidth, texHeight, 0xFFFFFF, 1.0);
	}

	private int projectCoord(double val, int dimensionWidth) {
		return Util.clamp((int) Math.floor(val), 0, dimensionWidth - 1);
	}

	@Override
	public void drawRect(int fbo, int texId, BlendMode mode, double x,
			double y, double width, double height, double texX, double texY,
			double texWidth, double texHeight, int colorMask,
			double transparency) {
		// If you want to make a software renderer, you'd implement it here.
//		Framebuffer framebuffer = framebuffers.get(fbo);
//		ArrayBitmap destTexture = textures.get(framebuffer.texId);
//		ArrayBitmap srcTexture = textures.get(texId);
//
//		if (mode != BlendMode.SPRITE) {
//			return;
//		}
//
//		int xStart = projectCoord(x, destTexture.width);
//		int xEnd = projectCoord(x + width, destTexture.width);
//		int yStart = projectCoord(y, destTexture.height);
//		int yEnd = projectCoord(y + height, destTexture.height);
//
//		int texXStart = projectCoord((texX * srcTexture.width), srcTexture.width);
//		int texXEnd = projectCoord((texX + texWidth) * srcTexture.width, srcTexture.width);
//		int texYStart = projectCoord((texY * srcTexture.height), srcTexture.height);
//		int texYEnd = projectCoord((texY + texHeight) * srcTexture.height, srcTexture.height);
//
//		double srcX = texXStart;
//		double srcY = texYStart;
//		double srcXStep = (double) (texXEnd - texXStart)
//				/ (double) (xEnd - xStart);
//		double srcYStep = (double) (texYEnd - texYStart)
//				/ (double) (yEnd - yStart);
//		for (int destY = yStart; destY < yEnd; destY++, srcY += srcYStep) {
//			for (int destX = xStart; destX < xEnd; destX++, srcX += srcXStep) {
//				int texLocX = projectCoord(srcX, srcTexture.width);
//				int texLocY = projectCoord(srcY, srcTexture.height);
//				destTexture.pixels[destX + destY * destTexture.width] = srcTexture.pixels[texLocX
//						+ texLocY * srcTexture.height];
//			}
//		}
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
}
