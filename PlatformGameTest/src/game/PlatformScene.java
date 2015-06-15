package game;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import engine.core.Scene;
import engine.core.entity.Entity;
import engine.input.IInput;
import engine.input.InputListener;
import engine.rendering.ARGBColor;
import engine.rendering.Bitmap;
import engine.rendering.IRenderContext;
import engine.rendering.IRenderDevice;
import engine.rendering.LightMap;
import engine.rendering.SpriteSheet;
import engine.space.Grid;
import engine.space.ISpatialStructure;
import engine.util.BitmapFactory;
import engine.util.Delay;
import engine.util.SpriteComponent;
import engine.util.Util;
import engine.util.menu.IMenuHandler;
import engine.util.menu.Menu;
import engine.util.menu.MenuStack;
import engine.util.parsing.Config;
import game.components.CollectableComponent;
import game.components.EnemyComponent;
import game.components.PlayerComponent;

public class PlatformScene extends Scene {
	private enum State {
		RUNNING, LOST_LIFE, ERROR;
	}

	private static final int DEFAULT_LIVES = 3;
	private static final int NUM_SAVE_FILES = 5;
	private Entity player;
	private PlayerComponent playerComponent;
	private BitmapFactory bitmaps;
	private SpriteSheet font;
	private SpriteSheet livesIcon;
	private SpriteSheet healthIcon;

	private IInput input;
	private IRenderDevice device;
	private Config config;

	private State state;
	private Delay lostLifeDelay;
	private String errorMessage;
	private MenuStack menu;
	private boolean shouldExit;
	private InputListener helpMenuKey;
	private LightMap bigLightMapTest;

	private void loadLevel(Config config, IInput input,
			ISpatialStructure<Entity> structure, int points, int lives,
			int lifeDeficit) throws IOException {
		if (bigLightMapTest != null) {
			bigLightMapTest.dispose();
		}
		this.bigLightMapTest = new LightMap(device, 2048, 2048, 2);
		SpriteSheet level = new SpriteSheet(bitmaps.get("./res/"
				+ config.getString("level.data")), 4, 2);
		int[] backgrounds = new int[5];
		
		SpriteSheet tileSheet = new SpriteSheet(
				bitmaps.get("./res/tilesheet.png"), 16);
		font = new SpriteSheet(bitmaps.get("./res/monospace.png"), 16);
		livesIcon = new SpriteSheet(bitmaps.get("./res/livesicon.png"), 1);
		healthIcon = new SpriteSheet(bitmaps.get("./res/healthicon.png"), 1);

		backgrounds[0] = 41;
		backgrounds[1] = 14;
		backgrounds[2] = 30;
		backgrounds[3] = 46;
		backgrounds[4] = 62;

		LightMap backgroundLight = new LightMap(device, 100);
		int tileSize = 16;
		int[] pixels = level.getSheet().getPixels(null);
		for (int k = 0; k < level.getNumSprites(); k++) {
			for (int j = 0; j < level.getSpriteHeight(); j++) {
				for (int i = 0; i < level.getSpriteWidth(); i++) {
					int color = pixels[level.getPixelIndex(k, i, j)] & 0x00FFFFFF;
					int x = i * tileSize;
					int y = j * tileSize;
					// addRandomBackgroundTile(structure, x, y, backgrounds,
					// tileSheet, 10, bigLightMapTest);
					addEntity(config, input, structure, x, y, k, bitmaps,
							tileSheet, color, bigLightMapTest, backgroundLight,
							points, lives, lifeDeficit);

				}
			}
		}
	}

	private void addEntity(Config config, IInput input,
			ISpatialStructure<Entity> structure, int x, int y, int layer,
			BitmapFactory bitmaps, SpriteSheet tileSheet, int color,
			LightMap staticLightMap, LightMap lightToAdd,
			int points, int lives, int lifeDeficit) throws IOException {
		if (color == 255) {
			player = new Entity(structure, x, y, layer, true);
			new SpriteComponent(player, new SpriteSheet(
					bitmaps.get("./res/playertest2.png"), 1), 0);
			playerComponent = new PlayerComponent(player, points, 2, lives,
					lifeDeficit, new InputListener(input,
							new int[] { IInput.KEY_LEFT }), new InputListener(
							input, new int[] { IInput.KEY_RIGHT }),
					new InputListener(input, new int[] { IInput.KEY_LSHIFT,
							IInput.KEY_RSHIFT }), new InputListener(input,
							new int[] { IInput.KEY_SPACE }), new InputListener(
							input, new int[] { IInput.KEY_DOWN }));
		} else if (color == 254) {
			Entity entity = new Entity(structure, x, y, layer, false);
			new CollectableComponent(entity, 10);
			new SpriteComponent(entity, new SpriteSheet(
					bitmaps.get("./res/diamond2.png"), 5, 2), 0.1);

			// Entity entityLight = new Entity(structure, x+8, y+8, layer,
			// false);
			// new LightComponent(entityLight, new OpenGLLightMap(8));
			// new LinkComponent(entityLight, entity);
		} else if (color == 253) {
			Entity entity = new Entity(structure, x, y, layer, false);
			new CollectableComponent(entity, 100);
			SpriteSheet sheet = new SpriteSheet(
					bitmaps.get("./res/diamond.png"), 9, 1);
			new SpriteComponent(entity, sheet, 0.1111111111);
			// new SpriteComponent(entity, new SpriteSheet(
			// bitmaps.get("./res/diamond.png"), 1), 0);
		} else if (color == 250) {
			Entity entity = new Entity(structure, x, y, layer, true);
			new SpriteComponent(entity, new SpriteSheet(
					bitmaps.get("./res/slime.png"), 1), 0);
			new EnemyComponent(entity, 20);
		} else if (color != 0) {
			boolean blocking = (color & 0x8000) != 0;
			Entity e = add(structure, x, y, layer, blocking, tileSheet,
					color & 0xFF);
			if (color == 14 || color == 30 || color == 46 || color == 62) {
				staticLightMap.addLight(lightToAdd, x - lightToAdd.getWidth()
						/ 2 + (int) e.getAABB().getWidth() / 2,
						y - lightToAdd.getHeight() / 2
								+ (int) e.getAABB().getHeight() / 2, 0, 0,
						lightToAdd.getWidth(), lightToAdd.getHeight());
			}
		}
	}

	// private static int getRand(int min, int range) {
	// return ((int) (Math.random() * range)) % range + min;
	// }

	// private static void addRandomBackgroundTile(
	// ISpatialStructure<Entity> structure, int x, int y,
	// int[] backgrounds, SpriteSheet tileSheet, int randBackChance, LightMap
	// lightMap) {
	// if (getRand(0, randBackChance) != 0) {
	// add(structure, x, y, -1, false, tileSheet, backgrounds[0]);
	// } else {
	// Entity e = add(structure, x, y, -1, false, tileSheet,
	// backgrounds[getRand(1, backgrounds.length - 1)]);
	// lightMap.addLight(lightMapTest, x - lightMapTest.getWidth()
	// / 2 + (int)e.getAABB().getWidth() / 2,
	// y - lightMapTest.getHeight() / 2 + (int)e.getAABB().getHeight()
	// / 2, 0, 0, lightMapTest.getWidth(),
	// lightMapTest.getHeight());
	// // new LightComponent(new Entity(structure, x +
	// // e.getAABB().getWidth()
	// // / 2, y + e.getAABB().getHeight() / 2, -1, false), lightMapTest);
	// }
	// }

	private static String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	private void startNewGame() throws IOException {
		startNewGame(0, DEFAULT_LIVES, 0);
	}

	private static String[] getSaveFiles(int num) {
		String[] result = new String[num];
		for (int i = 0; i < num; i++) {
			result[i] = getSaveName(i);
			File tester = new File(getSavePath(i));
			if (tester.exists() && !tester.isDirectory()) {
				result[i] += "(" + new Date(tester.lastModified()) + ")";
			}
		}
		return result;
	}

	private Menu getDefaultMenu() {
		return new Menu(new String[] { "New Game", "Save Game", "Load Game",
				"Options", "Help", "Exit" }, new IMenuHandler() {
			@Override
			public void handleMenu(int option, MenuStack stack) {
				try {
					switch (option) {
					case 0:
						startNewGame();
						break;
					case 1:
						stack.push(new Menu(getSaveFiles(NUM_SAVE_FILES),
								new IMenuHandler() {
									@Override
									public void handleMenu(int option,
											MenuStack stack) {
										try {
											saveGame(option);
										} catch (IOException e) {
											enterErrorState(e);
										}
									}
								}));
						break;
					case 2:
						stack.push(new Menu(getSaveFiles(NUM_SAVE_FILES),
								new IMenuHandler() {
									@Override
									public void handleMenu(int option,
											MenuStack stack) {
										try {
											loadGame(option);
										} catch (IOException | ParseException e) {
											// Do nothing; the user will see the
											// game hasn't loaded.
										}
									}
								}));
						break;
					case 3:
						stack.push(new Menu(new String[] { "Test1", "Test2",
								"Test3", "Back" }, new IMenuHandler() {
							@Override
							public void handleMenu(int option, MenuStack stack) {
								switch (option) {
								case 3:
									stack.pop();
									break;
								}
							}
						}));
						break;
					case 4:
						addHelpMenu(stack);
						break;
					case 5:
						shouldExit = true;
						break;
					}
				} catch (Exception e) {
					enterErrorState(e);
				}
			}
		});
	}

	private void initMenu() {
		this.menu = new MenuStack(font, 0xFFFFFF, 0xFF88FF, 20, 20,
				new InputListener(input, new int[] { IInput.KEY_UP }),
				new InputListener(input, new int[] { IInput.KEY_DOWN }),
				new InputListener(input, new int[] { IInput.KEY_RETURN }),
				new InputListener(input, new int[] { IInput.KEY_ESCAPE }), 0.1,
				getDefaultMenu());
	}

	private void initVariables() {
		this.state = State.RUNNING;
		this.lostLifeDelay = new Delay(3.0);
		this.errorMessage = "";
		getStructure().clear();
	}

	private void startNewGame(int points, int lives, int lifeDeficit)
			throws IOException {
		initVariables();
		loadLevel(config, input, getStructure(), points, lives, lifeDeficit);
		initMenu();
	}

	public PlatformScene(Config config, IInput input, IRenderDevice device)
			throws IOException {
		super(new Grid<Entity>(16, 256, 256));
		this.input = input;
		this.device = device;
		this.config = config;
		this.bitmaps = new BitmapFactory(device);
		this.helpMenuKey = new InputListener(input, new int[] { IInput.KEY_F1 });
		startNewGame();
	}

	private static Entity add(ISpatialStructure<Entity> structure, double posX,
			double posY, double posZ, boolean isBlocking,
			SpriteSheet spriteSheet, int spriteIndex) {
		Entity result = new Entity(structure, posX, posY, posZ, isBlocking);
		new SpriteComponent(result, spriteSheet, spriteIndex);
		return result;
	}

	private void enterErrorState(Exception e) {
		state = State.ERROR;
		errorMessage = getStackTrace(e);
	}

	private static String getSaveName(int saveNum) {
		return "save" + saveNum + ".cfg";
	}

	private static String getSavePath(int saveNum) {
		return "./res/" + getSaveName(saveNum);
	}

	private void saveGame(int saveNum) throws IOException {
		Map<String, String> saveData = new HashMap<String, String>();
		saveData.put("points", playerComponent.getPoints() + "");
		saveData.put("lives", playerComponent.getLives() + "");
		saveData.put("lifeDeficit", playerComponent.getLifeDeficit() + "");
		Config.write(getSavePath(saveNum), saveData);
	}

	private void loadGame(int saveNum) throws IOException, ParseException {
		Config saveFile = new Config(getSavePath(saveNum));
		startNewGame(saveFile.getInt("points"), saveFile.getInt("lives"),
				saveFile.getInt("lifeDeficit"));
	}

	private static void addHelpMenu(MenuStack stack) {
		stack.push(new Menu(
				new String[] { "Use arrow keys to move and space to jump. Collect gems for points and "
						+ "jump on the heads of your enemies to destory them." },
				new IMenuHandler() {
					@Override
					public void handleMenu(int option, MenuStack stack) {
						stack.pop();
					}
				}));
	}

	public boolean update(double delta) {
		shouldExit = false;

		menu.update(delta);
		if (shouldExit || menu.isShowing()) {
			return shouldExit;
		}
		if (helpMenuKey.isDown() && !menu.isShowing()) {
			addHelpMenu(menu);
		}

		if (playerComponent.getHealth() == 0) {
			getStructure().clear();
			int lives = playerComponent.getLives() - 1;
			int points = playerComponent.getPoints();
			int lifeDeficit = playerComponent.getLifeDeficit();
			if (lives <= 0) {
				points = 0;
				lifeDeficit = 0;
			}
			try {
				loadLevel(config, input, getStructure(), points, lives,
						lifeDeficit);
			} catch (IOException e) {
				enterErrorState(e);
				return shouldExit;
			}
			state = State.LOST_LIFE;
			lostLifeDelay.reset();
		}

		switch (state) {
		case RUNNING:
			updateRange(delta, player.getAABB().expand(200, 200, 0));
			break;
		case LOST_LIFE:
			if (lostLifeDelay.over(delta)) {
				state = State.RUNNING;
				if (playerComponent.getLives() <= 0) {
					playerComponent.addLives(DEFAULT_LIVES);
				}
			}
			break;
		case ERROR:
			// Nothing to do
			break;
		}
		return shouldExit;
	}

	private static Bitmap background;
	private static SpriteSheet backgroundSpriteSheet;

	private void drawBackground(IRenderContext target, double r,
			double g, double b, int parallax, int x, int y) {
		if (background == null || background.getWidth() != target.getWidth()
				|| background.getHeight() != target.getHeight()) {
			background = new Bitmap(device, target.getWidth(), target.getHeight());
			backgroundSpriteSheet = new SpriteSheet(background, 1);
		}
		int width = target.getWidth();
		int height = target.getHeight();
		int[] result = new int[width * height];
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
				result[i + j * width] = color;
				// background.setPixel(i, j, color);
			}
		}
		background.setPixels(result, 0, 0, width, height);
		target.drawSprite(backgroundSpriteSheet, 0, 0, 0, 1.0, false, false,
				0xFFFFFF);
	}

	private void renderScene(IRenderContext target, double viewportX,
			double viewportY) {
		drawBackground(target, 1.0, 0.0, 0.0, 4, (int) Math.round(viewportX),
				(int) Math.round(viewportY));
		renderRange(target, viewportX, viewportY);
	}

	private void drawPlayerHealth(IRenderContext target) {
		for (int i = 0, x = target.getWidth() - healthIcon.getSpriteWidth() - 1; i < playerComponent
				.getHealth(); i++, x -= (healthIcon.getSpriteWidth() + 1)) {
			target.drawSprite(healthIcon, 0, x, 0, 1.0, false, false, 0xFFFFFF);
		}
	}

	private void drawHUD(IRenderContext target) {
		target.drawString(String.format("%07d", playerComponent.getPoints()),
				font, 0, 0, 0xFFFFFF, 0);
		drawPlayerHealth(target);
		target.drawSprite(livesIcon, 0, 0,
				target.getHeight() - livesIcon.getSpriteHeight(), 1.0, false,
				false, 0xFFFFFF);
		target.drawString(playerComponent.getLives() + "", font,
				livesIcon.getSpriteWidth(),
				target.getHeight() - font.getSpriteHeight(), 0xFFFFFF, 0);
	}

	public void render(IRenderContext target) {
		double viewportOffsetX = Math.round((target.getWidth() - player
				.getAABB().getWidth()) / 2);
		double viewportOffsetY = Math.round((target.getHeight() - player
				.getAABB().getHeight()) / 2);
		double viewportX = player.getAABB().getMinX() - viewportOffsetX;
		double viewportY = player.getAABB().getMinY() - viewportOffsetY;
		int viewportXInt = (int) Math.round(viewportX);
		int viewportYInt = (int) Math.round(viewportY);

		// target.clear(0.0f, 0.0f, 0.0f, 0.0f);
		// player.render(target, (int) Math.round(viewportX), (int)
		// Math.round(viewportY));

		switch (state) {
		case RUNNING:
			target.clearLighting();
			renderScene(target, viewportX, viewportY);
			// target.drawLight((int)Math.round(player.getAABB().getCenterX()-viewportX),
			// (int)Math.round(player.getAABB().getCenterY()-viewportY), 200);
			target.drawLight(bigLightMapTest, 0, 0, viewportXInt, viewportYInt,
					target.getWidth(), target.getHeight());
			target.applyLighting(16.0 / 256.0);
			break;
		case LOST_LIFE:
			target.clear(0.0, 0.0, 0.0, 0.0);
			player.render(target, (int) Math.round(viewportX),
					(int) Math.round(viewportY));
			String gameOverString = "Game Over";
			if (playerComponent.getLives() <= 0) {
				target.drawString(
						gameOverString,
						font,
						target.getWidth()
								/ 2
								- (int) ((gameOverString.length() / 2.0) * font
										.getSpriteWidth()), 0, 0xFFFFFF, 0);
			}
			break;
		case ERROR:
			target.clear(0.0f, 0.5f, 0.0f, 0.0f);
			String errorHeader = "Error";
			target.drawString(
					errorHeader,
					font,
					target.getWidth()
							/ 2
							- (int) ((errorHeader.length() / 2.0) * font
									.getSpriteWidth()), 0, 0xFFFFFF, 0);
			target.drawString(errorMessage, font, 0, font.getSpriteHeight(),
					0xFFFFFF, target.getWidth());
			return;
		}
		drawHUD(target);
		menu.render(target);
	}
}
