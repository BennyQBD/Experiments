package game;

import java.awt.event.KeyEvent;
import java.io.IOException;

import engine.core.BitmapFactory;
import engine.core.Entity;
import engine.core.IInput;
import engine.core.InputListener;
import engine.core.Scene;
import engine.core.SpriteComponent;
import engine.core.Util;
import engine.core.space.Grid;
import engine.core.space.ISpatialStructure;
import engine.parsing.Config;
import engine.rendering.ARGBColor;
import engine.rendering.ArrayBitmap;
import engine.rendering.IBitmap;
import engine.rendering.IRenderContext;
import engine.rendering.SpriteSheet;
import game.components.CollectableComponent;
import game.components.EnemyComponent;
import game.components.PlayerComponent;

public class PlatformScene extends Scene {
	private Entity player;
	private SpriteSheet font;
	private SpriteSheet tileSheet;

	private void loadLevel(Config config, IInput input,
			ISpatialStructure<Entity> structure) throws IOException {
		IBitmap level = new ArrayBitmap("./res/"
				+ config.getString("level.data"));
		BitmapFactory bitmaps = new BitmapFactory();
		IBitmap[] backgrounds = new IBitmap[5];
		
		tileSheet = new SpriteSheet(bitmaps.get("./res/tilesheet.png"), 16);
		font = new SpriteSheet(bitmaps.get("./res/monospace.png"), 16);
		
		backgrounds[0] = bitmaps.get("./res/simprock24.png");
		backgrounds[1] = bitmaps.get("./res/simprockborder.png");
		backgrounds[2] = bitmaps.get("./res/simprockborder3.png");
		backgrounds[3] = bitmaps.get("./res/simprockborder4.png");
		backgrounds[4] = bitmaps.get("./res/simprockborder5.png");

		int tileSize = 16;
		for (int j = 0; j < level.getHeight(); j++) {
			for (int i = 0; i < level.getWidth(); i++) {
				int color = level.getPixel(i, j) & 0x00FFFFFF;
				int x = i * tileSize;
				int y = j * tileSize;
				addRandomBackgroundTile(structure, x, y, backgrounds, 10);
				addEntity(config, input, structure, x, y, bitmaps, color);
			}
		}
	}

	private void addEntity(Config config, IInput input,
			ISpatialStructure<Entity> structure, int x, int y,
			BitmapFactory bitmaps, int color) throws IOException {
		if (color == 255) {
			player = new Entity(structure, x, y, 1, true);
			new SpriteComponent(player, new SpriteSheet(
					bitmaps.get("./res/playertest.png"), 1), 0);
			new PlayerComponent(player, input.register(new InputListener(
					new int[] { KeyEvent.VK_LEFT })),
					input.register(new InputListener(
							new int[] { KeyEvent.VK_RIGHT })),
					input.register(new InputListener(
							new int[] { KeyEvent.VK_SHIFT })),
					input.register(new InputListener(
							new int[] { KeyEvent.VK_SPACE })),
					input.register(new InputListener(
							new int[] { KeyEvent.VK_DOWN })));
		} else if (color == 254) {
			Entity entity = new Entity(structure, x, y, 2, false);
			new CollectableComponent(entity, 10);
			new SpriteComponent(entity, new SpriteSheet(
					bitmaps.get("./res/diamond2.png"), 1), 0);
		} else if (color == 253) {
			Entity entity = new Entity(structure, x, y, 2, false);
			new CollectableComponent(entity, 100);
			new SpriteComponent(entity, new SpriteSheet(
					bitmaps.get("./res/diamond.png"), 1), 0);
		} else if (color == 250) {
			Entity entity = new Entity(structure, x, y, 1, true);
			new SpriteComponent(entity, new SpriteSheet(
					bitmaps.get("./res/slime.png"), 1), 0);
			new EnemyComponent(entity);
		} else if (color != 0) {
			boolean blocking = (color & 0x8000) != 0;
			double layer = 2.0;
			add(structure, x, y, layer, blocking, tileSheet, color)
					.setDitherable(true);
		}
	}

	private static int getRand(int min, int range) {
		return ((int) (Math.random() * range)) % range + min;
	}

	private static void addRandomBackgroundTile(
			ISpatialStructure<Entity> structure, int x, int y,
			IBitmap[] backgrounds, int randBackChance) {
		if (getRand(0, randBackChance) != 0) {
			add2(structure, x, y, -1, false, backgrounds[0]);
		} else {
			add2(structure, x, y, -1, false,
					backgrounds[getRand(1, backgrounds.length - 1)]);
		}
	}

	public PlatformScene(Config config, IInput input) {
		super(new Grid<Entity>(16, 256, 256));
		try {
			loadLevel(config, input, getStructure());
		} catch (IOException e) {
			e.printStackTrace();
			// TODO: Proper exception handling
		}
	}

	private static Entity add2(ISpatialStructure<Entity> structure,
			double posX, double posY, double posZ, boolean isBlocking,
			IBitmap sprite) {
		Entity result = new Entity(structure, posX, posY, posZ, isBlocking);
		new SpriteComponent(result, new SpriteSheet(sprite, 1), 0);
		return result;
	}

	private static Entity add(ISpatialStructure<Entity> structure, double posX,
			double posY, double posZ, boolean isBlocking,
			SpriteSheet spriteSheet, int spriteIndex) {
		Entity result = new Entity(structure, posX, posY, posZ, isBlocking);
		new SpriteComponent(result, spriteSheet, spriteIndex);
		return result;
	}

	public void update(double delta) {
		updateRange(delta, player.getAABB().expand(200, 200, 0));
	}

	private static void drawBackground(IRenderContext target, double r,
			double g, double b, int parallax, int x, int y) {
		int width = target.getWidth();
		int height = target.getHeight();
		for (int j = 0; j < height; j++) {
			int adjustedJ = Util.floorMod(j + (y / parallax), height);
			double jFract = ((double) (adjustedJ) / (double) (height - 1));
			// jFract *= 2.0 * Math.PI;
			// jFract = Math.sin(jFract);
			// jFract = (jFract + 1.0)/2.0;
			jFract *= 2.0;
			if (jFract > 1.0) {
				jFract = 1.0 - (jFract - 1.0);
			}
			int color = ARGBColor.makeColor(jFract * r, jFract * g, jFract * b);

			for (int i = 0; i < width; i++) {
				target.setPixel(i, j, color);
			}
		}

	}

	public void render(IRenderContext target) {
		double viewportOffsetX = Math.round((target.getWidth() - player
				.getAABB().getWidth()) / 2);
		double viewportOffsetY = Math.round((target.getHeight() - player
				.getAABB().getHeight()) / 2);
		double viewportX = player.getAABB().getMinX() - viewportOffsetX;
		double viewportY = player.getAABB().getMinY() - viewportOffsetY;

		drawBackground(target, 1.0, 0.0, 0.0, 4, (int) Math.round(viewportX),
				(int) Math.round(viewportY));

		renderRange(target, viewportX, viewportY);

		target.drawString("1234567890 HP", font, 10, 10, 0xFFFFFF);
	}
}
