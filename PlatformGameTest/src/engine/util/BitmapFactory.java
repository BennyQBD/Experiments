package engine.util;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import engine.rendering.ArrayBitmap;
import engine.rendering.IBitmap;

public class BitmapFactory {
	private Map<String, SoftReference<IBitmap>> loadedBitmaps;

	public BitmapFactory() {
		this.loadedBitmaps = new HashMap<>();
	}

	public IBitmap get(String fileName) throws IOException {
		SoftReference<IBitmap> ref = loadedBitmaps.get(fileName);
		IBitmap current = ref == null ? null : ref.get();
		if(current != null) {
			return current;
		} else {
			loadedBitmaps.remove(fileName);
			IBitmap result;
//			if(fileName.equals("./res/testLevel.png")) {
//				Debug.log("Made the right one!");
				result = new ArrayBitmap(fileName);
//			} else {
//				result = new OpenGLBitmap(fileName);
//			}
			loadedBitmaps.put(fileName, new SoftReference<IBitmap>(result));
			return result;
		}
	}
}

