package game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import engine.audio.Sound;
import engine.components.AudioComponent;
import engine.components.CollisionComponent;
import engine.components.FadeRemove;
import engine.components.LightComponent;
import engine.components.LinkComponent;
import engine.components.SpriteComponent;
import engine.core.entity.Entity;
import engine.input.IInput;
import engine.rendering.Color;
import engine.rendering.IRenderDevice;
import engine.rendering.LightMap;
import engine.rendering.SpriteSheet;
import engine.space.AABB;
import engine.space.ISpatialStructure;
import engine.util.factory.LightMapFactory;
import engine.util.factory.SoundFactory;
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
	private InventoryComponent playerInventory;

	private SpriteSheetFactory sprites;
	private LightMapFactory lightMaps;
	private SoundFactory sounds;
	private Config config;
	private IInput input;
	private IRenderDevice device;

	private Map<Integer, SpriteSheet> itemSprites;
	private Map<Integer, Color> itemColors;

	private Color lastColor;

	public PlatformLevel(IRenderDevice device, IInput input, Config config,
			SpriteSheetFactory sprites, LightMapFactory lightMaps,
			SoundFactory sounds) {
		this.sprites = sprites;
		this.lightMaps = lightMaps;
		this.sounds = sounds;
		this.config = config;
		this.input = input;
		this.device = device;
		this.lastColor = null;
	}

	public Entity getPlayer() {
		return player;
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
			int lives, int lifeDeficit, int checkpoint) throws IOException {
		if (staticLightMap != null) {
			staticLightMap.dispose();
		}
		this.staticLightMap = new LightMap(device, 2048, 2048, 2);
		SpriteSheet level = sprites.get(config.getString("level.data"), 4, 2);

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
							lifeDeficit, checkpoint);
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

		Color color = new Color(config.getDoubleWithDefault(prefix + ".r",
				"sprite.default.r"), config.getDoubleWithDefault(prefix + ".g",
				"sprite.default.g"), config.getDoubleWithDefault(prefix + ".b",
				"sprite.default.b"));
		lastColor = color;

		SpriteSheet sheet = sprites.get(fileName, numSpritesX, numSpritesY);
		SpriteComponent sc = null;
		switch (animationType) {
		case "automatic":
			sc = new SpriteComponent(e, sheet, config.getDoubleWithDefault(
					prefix + ".frameTime", "sprite.default.frameTime"), color);
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
			int lifeDeficit, int checkpoint) throws IOException {
		if (color == 0) {
			return;
		}
		boolean blocking = (color & 0x8000) != 0;
		int r = (color >> 16) & 0xFF;
		int b = color & 0xFF;
		String prefixIn = "entity." + b + ".";
		String defaultPrefixIn = "entity." + "default" + ".";

		parseEntity(input, structure, x, y, layer, points, lives, lifeDeficit,
				checkpoint, r, prefixIn, defaultPrefixIn, blocking, b);
	}

	private Entity parseEntity(IInput input,
			ISpatialStructure<Entity> structure, int x, int y, int layer,
			int points, int lives, int lifeDeficit, int checkpoint,
			int entityCheckpoint, String prefixIn, String defaultPrefixIn,
			boolean blocking, int defaultSpriteIndex) throws IOException {
		int componentCounter = 0;
		String prefix = prefixIn + componentCounter;
		String component = config.getString(prefix);
		if (component == null) {
			prefixIn = defaultPrefixIn;
			prefix = prefixIn + componentCounter;
		}
		component = config.getString(prefix);
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
					sheet = addSpriteComponent(e, prefix, defaultSpriteIndex);
					break;
				case "player":
					if (checkpoint != entityCheckpoint) {
						e.forceRemove();
						return getCheckpointEntity(structure, x, y, layer,
								config, entityCheckpoint);
					}
					player = e;
					new PlayerComponent(e, input, config);
					break;
				case "collectable":
					itemId = config.getIntWithDefault(prefix + ".id",
							"collectable.default.id");
					new CollectableComponent(e, config.getIntWithDefault(prefix
							+ ".points", "collectable.default.points"),
							config.getIntWithDefault(prefix + ".health",
									"collectable.default.health"),
							config.getIntWithDefault(prefix + ".lives",
									"collectable.default.lives"), 0, itemId);
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
					double lightR = config.getDoubleWithDefault(prefix + ".r",
							"light.default.r");
					double lightG = config.getDoubleWithDefault(prefix + ".g",
							"light.default.g");
					double lightB = config.getDoubleWithDefault(prefix + ".b",
							"light.default.b");
					LightMap light = lightMaps.get(config.getIntWithDefault(
							prefix + ".radius", "light.default.radius"),
							new Color(lightR, lightG, lightB));
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
						int health = config.getInt("player.health");
						playerInventory = new InventoryComponent(e, points,
								health, health, lives, lifeDeficit, checkpoint);
					} else {
						new InventoryComponent(e, 0, 0, 0, 0, 0, 0);
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
				case "link":
					int offsetX = config.getIntWithDefault(prefix + ".offsetX",
							"link.default.offsetX");
					int offsetY = config.getIntWithDefault(prefix + ".offsetY",
							"link.default.offsetY");
					int layerOffset = config.getIntWithDefault(prefix
							+ ".layerOffset", "link.default.layerOffset");
					boolean isLinkedEntityBlocking = config
							.getBooleanWithDefault(prefix + ".blocking",
									"link.default.blocking");
					int linkedEntityDefaultSpriteIndex = config
							.getIntWithDefault(prefix + ".defaultSpriteIndex",
									"link.default.defaultSpriteIndex");
					Entity linkedEntity = parseEntity(input, structure, x
							+ offsetX, y + offsetY, layer + layerOffset,
							points, lives, lifeDeficit, checkpoint,
							entityCheckpoint, prefix + ".", prefix + ".",
							isLinkedEntityBlocking,
							linkedEntityDefaultSpriteIndex);
					if (linkedEntity != null) {
						new LinkComponent(linkedEntity, e);
					}
					break;
				case "audio":
					List<String> audioNames = new ArrayList<>();
					List<Sound> audioData = new ArrayList<>();
					String defaultAudioName = config
							.getString("audio.default.name");
					String defaultAudioFileName = config
							.getString("audio.default.fileName");
					int audioCounter = 0;
					String audioPrefix = prefix + "." + audioCounter;
					String audioName = config.getStringWithDefault(audioPrefix
							+ ".name", "audio.default.name");
					String audioFileName = config
							.getStringWithDefault(audioPrefix + ".fileName",
									"audio.default.fileName");
					while (!audioName.equals(defaultAudioName)
							&& !audioFileName.equals(defaultAudioFileName)) {
						audioNames.add(audioName);
						audioData.add(sounds.get(audioFileName, config
								.getDoubleWithDefault(audioPrefix + ".volume",
										"audio.default.volume"), config
								.getDoubleWithDefault(audioPrefix + ".pitch",
										"audio.default.pitch"), config
								.getBooleanWithDefault(audioPrefix
										+ ".shouldLoop",
										"audio.default.shouldLoop")));

						audioCounter++;
						audioPrefix = prefix + "." + audioCounter;
						audioName = config.getStringWithDefault(audioPrefix
								+ ".name", "audio.default.name");
						audioFileName = config.getStringWithDefault(audioPrefix
								+ ".fileName", "audio.default.fileName");
					}

					new AudioComponent(e,
							audioNames.toArray(new String[audioNames.size()]),
							audioData.toArray(new Sound[audioData.size()]));
					break;
				}

				componentCounter++;
				prefix = prefixIn + componentCounter;
				component = config.getString(prefix);
			}

			if (itemId != -1 && sheet != null) {
				itemSprites.put(itemId, sheet);
				itemColors.put(itemId, lastColor);
			}

			return e;
		}

		return null;
	}

	private static Entity getCheckpointEntity(
			ISpatialStructure<Entity> structure, int x, int y, int layer,
			Config config, int checkpoint) {
		Entity e = new Entity(structure, x, y, layer);
		new CollectableComponent(e, 0, 0, 0, checkpoint, 0);
		double checkpointSize = config.getDouble("level.checkpointSize");
		e.fitAABB(new AABB(-checkpointSize, -checkpointSize, checkpointSize,
				checkpointSize));
		return e;
	}

	private static void addStaticLight(Entity e, int x, int y,
			LightMap staticLightMap, LightMap lightToAdd) {
		staticLightMap.addLight(lightToAdd, x - lightToAdd.getWidth() / 2
				+ (int) e.getAABB().getWidth() / 2, y - lightToAdd.getHeight()
				/ 2 + (int) e.getAABB().getHeight() / 2, 0, 0,
				lightToAdd.getWidth(), lightToAdd.getHeight(), Color.WHITE);
	}
}
