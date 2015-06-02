package engine.rendering;

import engine.core.space.AABB;
import java.io.IOException;
import java.io.File;

public interface IBitmap {
	public int getWidth();
	public int getHeight();
	public int getPixel(int x, int y);

	public AABB getAABB();
	public IBitmap clear(int color);
	public IBitmap drawPixel(int x, int y, int color);
	public int[] copyToIntArray(int[] dest);
	public void save(String filetype, File file) throws IOException;
}
