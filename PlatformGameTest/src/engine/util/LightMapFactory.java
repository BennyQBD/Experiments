package engine.util;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import engine.rendering.IRenderDevice;
import engine.rendering.LightMap;

public class LightMapFactory {
	private final IRenderDevice device;
	private Map<Integer, SoftReference<LightMap>> loadedLightMaps;

	public LightMapFactory(IRenderDevice device) {
		this.device = device;
		this.loadedLightMaps = new HashMap<>();
	}

	public LightMap get(int radius) {
		SoftReference<LightMap> ref = loadedLightMaps.get(radius);
		LightMap current = ref == null ? null : ref.get();
		if(current != null) {
			return current;
		} else {
			loadedLightMaps.remove(radius);
			LightMap result = new LightMap(device, radius);
			loadedLightMaps.put(radius, new SoftReference<LightMap>(result));
			return result;
		}
	}
}
