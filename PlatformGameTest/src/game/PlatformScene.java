package game;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import engine.core.Scene;
import engine.core.entity.Entity;
import engine.input.IInput;
import engine.input.InputListener;
import engine.rendering.ARGBColor;
import engine.rendering.IBitmap;
import engine.rendering.IRenderContext;
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
	private Entity player;
	private PlayerComponent playerComponent;
	private BitmapFactory bitmaps;
	private SpriteSheet font;
	private SpriteSheet livesIcon;
	private SpriteSheet healthIcon;

	private IInput input;
	private Config config;

	private State state;
	private Delay lostLifeDelay;
	private String errorMessage;
	private MenuStack menu;
	private boolean shouldExit;

	private void loadLevel(Config config, IInput input,
			ISpatialStructure<Entity> structure, int points, int lives)
			throws IOException {
		IBitmap level = bitmaps.get("./res/" + config.getString("level.data"));
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

		int tileSize = 16;
		for (int j = 0; j < level.getHeight(); j++) {
			for (int i = 0; i < level.getWidth(); i++) {
				int color = level.getPixel(i, j) & 0x00FFFFFF;
				int x = i * tileSize;
				int y = j * tileSize;
				addRandomBackgroundTile(structure, x, y, backgrounds,
						tileSheet, 10);
				addEntity(config, input, structure, x, y, bitmaps, tileSheet,
						color, points, lives);
			}
		}
	}

	private void addEntity(Config config, IInput input,
			ISpatialStructure<Entity> structure, int x, int y,
			BitmapFactory bitmaps, SpriteSheet tileSheet, int color,
			int points, int lives) throws IOException {
		if (color == 255) {
			player = new Entity(structure, x, y, 1, true);
			new SpriteComponent(player, new SpriteSheet(
					bitmaps.get("./res/playertest.png"), 1), 0);
			playerComponent = new PlayerComponent(player, points, 2, lives,
					input.register(new InputListener(
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
			new EnemyComponent(entity, 20);
		} else if (color != 0) {
			boolean blocking = (color & 0x8000) != 0;
			double layer = 2.0;
			add(structure, x, y, layer, blocking, tileSheet, color & 0xFF)
					.setDitherable(true);
		}
	}

	private static int getRand(int min, int range) {
		return ((int) (Math.random() * range)) % range + min;
	}

	private static void addRandomBackgroundTile(
			ISpatialStructure<Entity> structure, int x, int y,
			int[] backgrounds, SpriteSheet tileSheet, int randBackChance) {
		if (getRand(0, randBackChance) != 0) {
			add(structure, x, y, -1, false, tileSheet, backgrounds[0]);
		} else {
			add(structure, x, y, -1, false, tileSheet,
					backgrounds[getRand(1, backgrounds.length - 1)]);
		}
	}

	private static String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	private void startNewGame() throws IOException {
		startNewGame(0, DEFAULT_LIVES);
	}
	
	private IMenuHandler getMenuHandler() {
		return new IMenuHandler() {
			@Override
			public void handleMenu(int option,
					MenuStack stack) {
				try {
					switch (option) {
					case 0:
						startNewGame();
						break;
					case 1:
						saveGame();
						break;
					case 2:
						loadGame();
						break;
					case 3:
						stack.push(new Menu(new String[] { "Test1", "Test2", "Test3",
								"Back" }, new IMenuHandler() {
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
						shouldExit = true;
						break;
					}
				} catch (Exception e) {
					enterErrorState(e);
				}
			}
		};
	}

	private void startNewGame(int points, int lives) throws IOException {
		this.state = State.RUNNING;
		this.lostLifeDelay = new Delay(3.0);
		this.errorMessage = "";
		getStructure().clear();
		loadLevel(config, input, getStructure(), points, lives);
		this.menu = new MenuStack(
				font,
				0xFFFFFF,
				0xFF0000,
				20,
				20,
				input.register(new InputListener(new int[] { KeyEvent.VK_UP })),
				input.register(new InputListener(new int[] { KeyEvent.VK_DOWN })),
				input.register(new InputListener(
						new int[] { KeyEvent.VK_ENTER })), input
						.register(new InputListener(
								new int[] { KeyEvent.VK_ESCAPE })), 0.1, new Menu(
						new String[] { "New Game", "Save Game", "Load Game",
								"Options", "Exit" }, getMenuHandler()));
	}

	public PlatformScene(Config config, IInput input) throws IOException {
		super(new Grid<Entity>(16, 256, 256));
		this.input = input;
		this.config = config;
		this.bitmaps = new BitmapFactory();
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

	public void handleMenu(int selection, MenuStack menuStack) {
		try {
			switch (selection) {
			case 0:
				startNewGame();
				break;
			case 1:
				saveGame();
				break;
			case 2:
				loadGame();
				break;
			case 3:
				menu.push(new Menu(new String[] { "Test1", "Test2", "Test3",
						"Back" }, new IMenuHandler() {
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
				shouldExit = true;
				break;
			}
		} catch (Exception e) {
			enterErrorState(e);
		}
	}

	private void saveGame() throws IOException {
		Map<String, String> saveData = new HashMap<String, String>();
		saveData.put("points", playerComponent.getPoints() + "");
		saveData.put("lives", playerComponent.getLives() + "");
		Config.write("./res/save.cfg", saveData);
	}

	private void loadGame() throws IOException, ParseException {
		Config saveFile = new Config("./res/save.cfg");
		startNewGame(saveFile.getInt("points"), saveFile.getInt("lives"));
	}

	public boolean update(double delta) {
		shouldExit = false;
		menu.update(delta);
		if (shouldExit || menu.isShowing()) {
			return shouldExit;
		}

		if (playerComponent.getHealth() == 0) {
			getStructure().clear();
			int lives = playerComponent.getLives() - 1;
			int points = playerComponent.getPoints();
			if (lives <= 0) {
				points = 0;
			}
			try {
				loadLevel(config, input, getStructure(), points, lives);
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

		switch (state) {
		case RUNNING:
			renderScene(target, viewportX, viewportY);
			break;
		case LOST_LIFE:
			target.clear(0x000000);
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
			target.clear(0x880000);
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
