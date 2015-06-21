package game;

import engine.rendering.Bitmap;
import engine.rendering.Color;
import engine.rendering.IRenderContext;
import engine.rendering.IRenderDevice;
import engine.rendering.SpriteSheet;
import engine.util.Util;

public class GradientBackground {
	private Bitmap background;
	private SpriteSheet backgroundSpriteSheet;
	private IRenderDevice device;
	private double r;
	private double g;
	private double b;

	public GradientBackground(IRenderDevice device, double r, double g, double b) {
		this.device = device;
		this.r = r;
		this.g = g;
		this.b = b;
	}
	
	@Override
	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}
	
	public void dispose() {
		if(background != null) {
			background.dispose();
			background = null;
		}
	}

	private SpriteSheet getBackground(IRenderContext target) {
		if (background != null && target.getWidth() == background.getWidth()
				&& target.getHeight() == background.getHeight() / 2) {
			return backgroundSpriteSheet;
		}

		if (background != null) {
			background.dispose();
		}

		int width = target.getWidth();
		int height = target.getHeight() * 2;
		int[] result = new int[width * height];
		for (int j = 0; j < height; j++) {
			int adjustedJ = Util.floorMod(j * 2, height);
			double jFract = ((double) (adjustedJ) / (double) (height - 1));
			jFract *= 2.0;
			if (jFract > 1.0) {
				jFract = 1.0 - (jFract - 1.0);
			}
			int color = Color.makeARGB(jFract * r, jFract * g, jFract * b);

			for (int i = 0; i < width; i++) {
				result[i + j * width] = color;
			}
		}
		background = new Bitmap(device, target.getWidth(),
				target.getHeight() * 2, result);
		backgroundSpriteSheet = new SpriteSheet(background, 1);
		return backgroundSpriteSheet;
	}

	public void render(IRenderContext target, double parallax, double y) {
		y = Util.floorMod((y / parallax), target.getHeight())
				- target.getHeight();
		target.drawSprite(getBackground(target), 0, 0, y, 1.0, false, false,
				Color.WHITE);
	}
}
