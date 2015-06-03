package engine.rendering;

public interface IRenderContext extends IBitmap {
	public void drawSprite(SpriteSheet sheet, int index, int x, int y,
			double transparency, boolean flipX, boolean flipY, int colorMask);
	public void drawString(String msg, SpriteSheet font, int x, int y, int color);
}
