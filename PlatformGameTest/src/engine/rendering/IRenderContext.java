package engine.rendering;

public interface IRenderContext {
	public void clear(double a, double r, double g, double b);
	public void drawSprite(SpriteSheet sheet, int index, double x, double y,
			double transparency, boolean flipX, boolean flipY, Color color);
	public double drawString(String msg, SpriteSheet font, double x, double y,
			Color color, double wrapX);

	public void clearLighting(double a, double r, double g, double b);
	public void drawLight(LightMap light, double x, double y, double mapStartX,
			double mapStartY, double width, double height, Color color);
	public void applyLighting();

	public int getWidth();
	public int getHeight();
	void dispose();
}
