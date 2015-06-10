package engine.util;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import engine.rendering.ArrayBitmap;
import engine.rendering.IBitmap;
import engine.rendering.opengl.OpenGLBitmap;

public class BitmapFactory {
	public static final int TYPE_ARRAY = 0;
	public static final int TYPE_OPENGL = 1;
	
	private Map<String, SoftReference<IBitmap>> loadedBitmaps;
	private int bitmapType;

	public BitmapFactory(int bitmapType) {
		this.loadedBitmaps = new HashMap<>();
		this.bitmapType = bitmapType;
	}
	
	public IBitmap get(String fileName) throws IOException {
		return get(fileName, bitmapType);
	}

	public IBitmap get(String fileName, int type) throws IOException {
		SoftReference<IBitmap> ref = loadedBitmaps.get(fileName);
		IBitmap current = ref == null ? null : ref.get();
		if(current != null) {
			return current;
		} else {
			loadedBitmaps.remove(fileName);
			IBitmap result;
			switch(type) {
			case TYPE_ARRAY:
				result = new ArrayBitmap(fileName);
				break;
			case TYPE_OPENGL:
				result = new OpenGLBitmap(fileName);
				break;
			default:
				result = loadType(type);
				break;
			}
			loadedBitmaps.put(fileName, new SoftReference<IBitmap>(result));
			return result;
		}
	}
	
	@SuppressWarnings("static-method")
	public IBitmap loadType(int type) {
		// To be implemented in subclasses that have extra types
		return null;
	}
}

