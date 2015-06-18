package engine.util.factory;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import engine.rendering.Bitmap;
import engine.rendering.IRenderDevice;

public class BitmapFactory {
	private final IRenderDevice device;
	private Map<String, SoftReference<Bitmap>> loadedBitmaps;

	public BitmapFactory(IRenderDevice device) {
		this.device = device;
		this.loadedBitmaps = new HashMap<>();
	}

	public Bitmap get(String fileName) throws IOException {
		SoftReference<Bitmap> ref = loadedBitmaps.get(fileName);
		Bitmap current = ref == null ? null : ref.get();
		if(current != null) {
			return current;
		} else {
			loadedBitmaps.remove(fileName);
			Bitmap result = new Bitmap(device, fileName);
			loadedBitmaps.put(fileName, new SoftReference<Bitmap>(result));
			return result;
		}
	}
}

