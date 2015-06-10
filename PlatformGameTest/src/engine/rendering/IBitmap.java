package engine.rendering;

import java.io.File;
import java.io.IOException;

public interface IBitmap {
	public int getWidth();
	public int getHeight();
//	public int getPixel(int x, int y);
	public int getHardwareAccelerationID();

	public void clear(int color);
	public void setPixels(int[] colors, int x, int y, int width, int height);
	public int[] getPixels(int[] dest, int x, int y, int width, int height);
	public void save(String filetype, File file) throws IOException;
}
