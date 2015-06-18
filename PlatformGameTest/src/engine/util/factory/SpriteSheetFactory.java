package engine.util.factory;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import engine.rendering.SpriteSheet;

public class SpriteSheetFactory {
	private final BitmapFactory bitmaps;
	private Map<String, SoftReference<SpriteSheet>> loadedSpriteSheets;

	public SpriteSheetFactory(BitmapFactory bitmaps) {
		this.bitmaps = bitmaps;
		this.loadedSpriteSheets = new HashMap<>();
	}

	public SpriteSheet get(String fileName, int spritesPerX, int spritesPerY)
			throws IOException {
		SoftReference<SpriteSheet> ref = loadedSpriteSheets.get(fileName);
		SpriteSheet current = ref == null ? null : ref.get();
		if (current != null) {
			return current;
		} else {
			loadedSpriteSheets.remove(fileName);
			SpriteSheet result = new SpriteSheet(bitmaps.get(fileName),
					spritesPerX, spritesPerY);
			loadedSpriteSheets.put(fileName, new SoftReference<SpriteSheet>(
					result));
			return result;
		}
	}
}
