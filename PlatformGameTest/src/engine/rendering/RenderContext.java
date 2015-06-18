package engine.rendering;

import engine.util.Util;

public class RenderContext implements IRenderContext {
	private final int width;
	private final int height;
	private final IRenderDevice device;
	private final LightMap lightMap;

	public RenderContext(IRenderDevice device) {
		this.device = device;
		this.width = device.getRenderTargetWidth(0);
		this.height = device.getRenderTargetHeight(0);
		this.lightMap = new LightMap(device, width, height, 1);
	}

	@Override
	public void dispose() {
		lightMap.dispose();
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
	public void clear(double a, double r, double g, double b) {
		device.clear(0, a, r, g, b);
	}

	@Override
	public void drawSprite(SpriteSheet sheet, int index, double x, double y,
			double transparency, boolean flipX, boolean flipY, Color color) {
		int width = sheet.getSpriteWidth();
		int height = sheet.getSpriteHeight();
		double texMinX = ((double) sheet.getStartX(index) / ((double) sheet
				.getSheet().getWidth()));
		double texMinY = ((double) sheet.getStartY(index) / ((double) sheet
				.getSheet().getHeight()));
		double texMaxX = texMinX + ((double) sheet.getSpriteWidth())
				/ ((double) sheet.getSheet().getWidth());
		double texMaxY = texMinY + ((double) sheet.getSpriteHeight())
				/ ((double) sheet.getSheet().getHeight());

		if (flipX) {
			double temp = texMinX;
			texMinX = texMaxX;
			texMaxX = temp;
		}

		if (flipY) {
			double temp = texMinY;
			texMinY = texMaxY;
			texMaxY = temp;
		}

		double texWidth = texMaxX - texMinX;
		double texHeight = texMaxY - texMinY;

		device.drawRect(0, sheet.getSheet().getDeviceID(),
				IRenderDevice.BlendMode.SPRITE, x, y, width, height, texMinX,
				texMinY, texWidth, texHeight, color, transparency);
	}

	@Override
	public double drawString(String str, SpriteSheet font, double x, double y,
			Color color, double wrapX) {
		double maxLength = (wrapX - x) / font.getSpriteWidth();
		if (wrapX <= x || wrapX <= 0 || str.length() < maxLength) {
			drawStringLine(str, font, x, y, color);
			return font.getSpriteHeight();
		}
		double yStart = y;
		str = Util.wrapString(str, maxLength);
		String[] strs = str.split("\n");
		for (int i = 0; i < strs.length; i++) {
			String[] wrappedStrings = strs[i].split("(?<=\\G.{" + maxLength
					+ "})");
			for (int j = 0; j < wrappedStrings.length; j++, y += font
					.getSpriteHeight()) {
				drawStringLine(wrappedStrings[j], font, x, y, color);
			}
		}
		return y - yStart;
	}

	private void drawStringLine(String str, SpriteSheet font, double x,
			double y, Color color) {
		for (int i = 0; i < str.length(); i++, x += font.getSpriteWidth()) {
			char c = str.charAt(i);
			drawSprite(font, (int) c, x, y, 1.0, false, false, color);
		}
	}

	@Override
	public void clearLighting(double a, double r, double g, double b) {
		lightMap.clear(a, r, g, b);
	}

	@Override
	public void drawLight(LightMap light, double x, double y, double mapStartX,
			double mapStartY, double width, double height, Color color) {
		lightMap.addLight(light, x, y, mapStartX, mapStartY, width, height, color);
	}

	@Override
	public void applyLighting() {
		device.drawRect(0, lightMap.getId(),
				IRenderDevice.BlendMode.APPLY_LIGHT, 0, 0, width, height, 0, 0,
				1, 1);
	}

	public void getPixels(int[] dest) {
		device.getTexture(0, dest, 0, 0, width, height);
	}
}
