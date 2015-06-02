package engine.rendering;

public interface IRenderContext extends IBitmap {
	void blit(IBitmap image, int offsetX, int offsetY, double transparency,
			boolean flipX, boolean flipY);
}
