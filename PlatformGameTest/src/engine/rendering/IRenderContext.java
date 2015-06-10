package engine.rendering;

public interface IRenderContext {
	public void clear(double a, double r, double g, double b);
	public void drawSprite(SpriteSheet sheet, int index, int x, int y,
			double transparency, boolean flipX, boolean flipY, int colorMask);
	public int drawString(String msg, SpriteSheet font, int x, int y,
			int color, int wrapX);

	public void clearLighting();
	public void drawLight(LightMap light, int x, int y, int mapStartX,
			int mapStartY, int width, int height);
	public void applyLighting(double ambientLightAmt);

	public int getWidth();
	public int getHeight();
	void dispose();
}
