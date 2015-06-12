package engine.rendering;

import java.io.File;
import java.io.IOException;

public interface IBitmap {
	public int getWidth();
	public int getHeight();
	public int[] getPixels(int[] dest);
	public int[] getPixels(int[] dest, int x, int y, int width, int height);
	public int getHardwareID();
	
	public void setPixels(int[] colors, int x, int y, int width, int height);

	public void clear(int color);
	public void save(String filetype, File file) throws IOException;
	
	public void dispose();
}
