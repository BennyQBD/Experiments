package game;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import engine.core.entity.Entity;
import engine.input.IInput;
import engine.rendering.Color;
import engine.rendering.IRenderDevice;
import engine.rendering.LightMap;
import engine.rendering.SpriteSheet;
import engine.space.ISpatialStructure;
import engine.util.components.CollisionComponent;
import engine.util.components.FadeRemove;
import engine.util.components.LightComponent;
import engine.util.components.SpriteComponent;
import engine.util.factory.LightMapFactory;
import engine.util.factory.SpriteSheetFactory;
import engine.util.parsing.Config;
import game.components.CollectableComponent;
import game.components.EnemyComponent;
import game.components.HazardComponent;
import game.components.InventoryComponent;
import game.components.PlayerComponent;
import game.components.UnlockComponent;

public class PlatformLevel {
	private LightMap staticLightMap;

	private Entity player;
	private PlayerComponent playerComponent;
	private InventoryComponent playerInventory;

	private SpriteSheetFactory sprites;
	private LightMapFactory lightMaps;
	private Config config;
	private IInput input;
	private IRenderDevice device;

	private Map<Integer, SpriteSheet> itemSprites;
	private Map<Integer, Color> itemColors;

	private Color lastColor;

	public PlatformLevel(IRenderDevice device, IInput input, Config config,
			SpriteSheetFactory sprites, LightMapFactory lightMaps) {
		this.sprites = sprites;
		this.lightMaps = lightMaps;
		this.config = config;
		this.input = input;
		this.device = device;
		this.lastColor = null;
	}

	public Entity getPlayer() {
		return player;
	}

	public PlayerComponent getPlayerComponent() {
		return playerComponent;
	}

	public InventoryComponent getPlayerInventory() {
		return playerInventory;
	}

	public LightMap getStaticLightMap() {
		return staticLightMap;
	}

	public SpriteSheet getItemSprite(int itemId) {
		return itemSprites.get(itemId);
	}

	public Color getItemColor(int itemId) {
		return itemColors.get(itemId);
	}

	public void loadLevel(ISpatialStructure<Entity> structure, int points,
			int lives, int lifeDeficit) throws IOException {
		if (staticLightMap != null) {
			staticLightMap.dispose();
		}
		this.staticLightMap = new LightMap(device, 2048, 2048, 2);
		SpriteSheet level = sprites.get(
				"./res/" + config.getString("level.data"), 4, 2);

		this.itemSprites = new HashMap<>();
		this.itemColors = new HashMap<>();

		int tileSize = 16;
		int[] pixels = level.getSheet().getPixels(null);
		for (int k = 0; k < level.getNumSprites(); k++) {
			for (int j = 0; j < level.getSpriteHeight(); j++) {
				for (int i = 0; i < level.getSpriteWidth(); i++) {
					int color = pixels[level.getPixelIndex(k, i, j)] & 0x00FFFFFF;
					int x = i * tileSize;
					int y = j * tileSize;
					addEntity(input, structure, x, y, k, color, points, lives,
							lifeDeficit);
				}
			}
		}
	}

	private SpriteSheet addSpriteComponent(Entity e, String prefix, int b)
			throws IOException {
		String fileName = config.getStringWithDefault(prefix + ".fileName",
				"sprite.default.fileName");
		int numSpritesX = config.getIntWithDefault("sprite.sheet." + fileName
				+ ".spritesX", "sprite.default.spritesX");
		int numSpritesY = config.getIntWithDefault("sprite.sheet." + fileName
				+ ".spritesY", "sprite.default.spritesY");

		String animationType = config.getStringWithDefault(prefix
				+ ".animationType", "sprite.default.animationType");

		Color color = new Color(
				config.getDoubleWithDefault(prefix + ".r", "sprite.default.r"),
				config.getDoubleWithDefault(prefix + ".g", "sprite.default.g"),
				config.getDoubleWithDefault(prefix + ".b", "sprite.default.b"));
		lastColor = color;

		SpriteSheet sheet = sprites.get("./res/" + fileName, numSpritesX,
				numSpritesY);
		SpriteComponent sc = null;
		switch (animationType) {
		case "automatic":
			sc = new SpriteComponent(e, sheet, config.getDoubleWithDefault(
					prefix + ".frameTime", "sprite.default.frameTime"),
					color);
			break;
		case "none":
			int spriteIndex = config.getIntWithDefault(prefix + ".spriteIndex",
					"sprite.default.spriteIndex");
			if (spriteIndex == -1) {
				spriteIndex = b;
			}
			sc = new SpriteComponent(e, sheet, spriteIndex, color);
			break;
		}

		double transparency = config.getDoubleWithDefault(prefix
				+ ".transparency", "sprite.default.transparency");
		sc.setTransparency(transparency);

		return sheet;
	}

	private void addEntity(IInput input, ISpatialStructure<Entity> structure,
			int x, int y, int layer, int color, int points, int lives,
			int lifeDeficit) throws IOException {
		if (color == 0) {
			return;
		}
		boolean blocking = (color & 0x8000) != 0;
		int b = color & 0xFF;
		int componentCounter = 0;
		String center = b + "";
		String prefix = "entity." + center + "." + componentCounter;
		String component = config.getString(prefix);
		if (component == null) {
			center = "default";
			prefix = "entity." + center + "." + componentCounter;
			component = config.getString(prefix);
		}
		if (component != null) {
			Entity e = new Entity(structure, x, y, layer);
			if (blocking) {
				new CollisionComponent(e);
			}
			SpriteSheet sheet = null;
			int itemId = -1;
			while (component != null) {
				switch (component) {
				case "sprite":
					sheet = addSpriteComponent(e, prefix, b);
					break;
				case "player":
					player = e;
					playerComponent = new PlayerComponent(e, input, config);
					break;
				case "collectable":
					itemId = config.getIntWithDefault(prefix + ".id",
							"collectable.default.id");
					new CollectableComponent(e, config.getIntWithDefault(prefix
							+ ".points", "collectable.default.points"),
							config.getIntWithDefault(prefix + ".lives",
									"collectable.default.lives"), itemId);
					break;
				case "enemy":
					new EnemyComponent(e, config, config.getStringWithDefault(
							prefix + ".type", "enemy.default.type"));
					break;
				case "hazard":
					double spaceX = config.getDoubleWithDefault(prefix
							+ ".spaceX", "hazard.default.spaceX");
					double spaceY = config.getDoubleWithDefault(prefix
							+ ".spaceY", "hazard.default.spaceY");
					double spaceZ = config.getDoubleWithDefault(prefix
							+ ".spaceZ", "hazard.default.spaceZ");
					new HazardComponent(e, spaceX, spaceY, spaceZ);
					break;
				case "light":
					LightMap light = lightMaps.get(config.getIntWithDefault(
							prefix + ".radius", "light.default.radius"));
					String lightType = config.getStringWithDefault(prefix
							+ ".type", "light.default.type");
					if (lightType.equals("static")) {
						addStaticLight(e, x, y, staticLightMap, light);
					} else if (lightType.equals("dynamic")) {
						new LightComponent(e, light);
					}
					break;
				case "inventory":
					if (e.equals(player)) {
						playerInventory = new InventoryComponent(e, points,
								lives, lifeDeficit);
					} else {
						new InventoryComponent(e, 0, 0, 0);
					}
					break;
				case "unlock":
					new UnlockComponent(e, config.getIntWithDefault(prefix
							+ ".id", "unlock.default.id"));
					break;
				case "remove":
					String removeType = config.getStringWithDefault(prefix
							+ ".type", "remove.default.type");
					switch (removeType) {
					default:
					case "fade":
						new FadeRemove(e, config.getDoubleWithDefault(prefix
								+ ".duration", "remove.fade.default.duration"),
								config.getIntWithDefault(prefix
										+ ".animationFrame",
										"remove.fade.default.animationFrame"),
								config.getDoubleWithDefault(prefix
										+ ".solidityDuration",
										"remove.fade.default.solidityDuration"));
						break;
					}
					break;
				}

				componentCounter++;
				prefix = "entity." + center + "." + componentCounter;
				component = config.getString(prefix);
			}

			if (itemId != -1 && sheet != null) {
				itemSprites.put(itemId, sheet);
				itemColors.put(itemId, lastColor);
			}
		}
	}

	private static void addStaticLight(Entity e, int x, int y,
			LightMap staticLightMap, LightMap lightToAdd) {
		staticLightMap.addLight(lightToAdd, x - lightToAdd.getWidth() / 2
				+ (int) e.getAABB().getWidth() / 2, y - lightToAdd.getHeight()
				/ 2 + (int) e.getAABB().getHeight() / 2, 0, 0,
				lightToAdd.getWidth(), lightToAdd.getHeight());
	}
}
