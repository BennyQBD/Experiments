package engine.rendering;

public interface IRenderContext extends IBitmap {
	public void drawString(String msg, IBitmap font, int x, int y, int color);
	public void blit(IBitmap image, int offsetX, int offsetY, double transparency,
			boolean flipX, boolean flipY);
}
