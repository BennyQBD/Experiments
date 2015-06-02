package engine.core;

import java.util.HashMap;
import java.io.IOException;
import java.util.Map;

import engine.rendering.IBitmap;
import engine.rendering.ArrayBitmap;

public class BitmapFactory {
	private Map<String, IBitmap> loadedBitmaps;

	public BitmapFactory() {
		this.loadedBitmaps = new HashMap<String, IBitmap>();
	}

	public IBitmap get(String fileName) throws IOException {
		IBitmap current = loadedBitmaps.get(fileName);
		if(current != null) {
			return current;
		} else {
			IBitmap result = new ArrayBitmap(fileName);
			loadedBitmaps.put(fileName, result);
			return result;
		}
	}
}

