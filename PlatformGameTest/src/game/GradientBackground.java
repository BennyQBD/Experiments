package game;

import engine.rendering.ARGBColor;
import engine.rendering.Bitmap;
import engine.rendering.IRenderContext;
import engine.rendering.IRenderDevice;
import engine.rendering.SpriteSheet;
import engine.util.Util;

public class GradientBackground {
	private IRenderDevice device;

	private Bitmap background;
	private SpriteSheet backgroundSpriteSheet;

	public GradientBackground(IRenderDevice device) {
		this.device = device;
	}

	public void render(IRenderContext target, double r, double g, double b,
			int parallax, int x, int y) {
		if (background == null || background.getWidth() != target.getWidth()
				|| background.getHeight() != target.getHeight()) {
			background = new Bitmap(device, target.getWidth(),
					target.getHeight());
			backgroundSpriteSheet = new SpriteSheet(background, 1);
		}
		int width = target.getWidth();
		int height = target.getHeight();
		int[] result = new int[width * height];
		for (int j = 0; j < height; j++) {
			int adjustedJ = Util.floorMod(j + (y / parallax), height);
			double jFract = ((double) (adjustedJ) / (double) (height - 1));
			jFract *= 2.0;
			if (jFract > 1.0) {
				jFract = 1.0 - (jFract - 1.0);
			}
			int color = ARGBColor.makeColor(jFract * r, jFract * g, jFract * b);

			for (int i = 0; i < width; i++) {
				result[i + j * width] = color;
			}
		}
		background.setPixels(result, 0, 0, width, height);
		target.drawSprite(backgroundSpriteSheet, 0, 0, 0, 1.0, false, false,
				0xFFFFFF);
	}
}
