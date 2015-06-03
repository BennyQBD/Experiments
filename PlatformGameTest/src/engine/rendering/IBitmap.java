package engine.rendering;

import java.io.File;
import java.io.IOException;

public interface IBitmap {
	public int getWidth();
	public int getHeight();
	public int getPixel(int x, int y);

	public IBitmap clear(int color);
	public IBitmap setPixel(int x, int y, int color);
	public int[] copyToIntArray(int[] dest);
	public void save(String filetype, File file) throws IOException;
}
