package game;

import java.io.IOException;
import java.text.ParseException;

import engine.input.Control;
import engine.input.IInput;
import engine.rendering.ARGBColor;
import engine.rendering.IRenderContext;
import engine.rendering.SpriteSheet;
import engine.util.menu.IMenuHandler;
import engine.util.menu.Menu;
import engine.util.menu.MenuStack;
import engine.util.parsing.Config;

public class GameMenu {
	private PlatformScene scene;
	private GameIO gameIO;
	private MenuStack menu;
	private Control helpMenuKey;
	private boolean shouldExit;
	private int numSaveFiles;
	private String helpMessage;

	public GameMenu(PlatformScene scene, Config config, GameIO gameIO,
			IInput input, SpriteSheet font) {
		this.scene = scene;
		this.gameIO = gameIO;
		this.shouldExit = false;
		this.numSaveFiles = config.getInt("menu.numSaveFiles");
		this.helpMessage = config.getString("menu.helpMessage");
		this.helpMenuKey = new Control(input, config, "menu.helpKey.");
		int fontColor = ARGBColor.makeColor(
				config.getDouble("menu.fontColor.r"),
				config.getDouble("menu.fontColor.g"),
				config.getDouble("menu.fontColor.b"));
		int selectColor = ARGBColor.makeColor(
				config.getDouble("menu.selectColor.r"),
				config.getDouble("menu.selectColor.g"),
				config.getDouble("menu.selectColor.b"));
		this.menu = new MenuStack(font, fontColor, selectColor,
				config.getInt("menu.x"), config.getInt("menu.y"), new Control(
						input, config, "menu.upKey."), new Control(input,
						config, "menu.downKey."), new Control(input, config,
						"menu.activateKey."), new Control(input, config,
						"menu.toggleKey."),
				config.getDouble("menu.usageDelay"), getDefaultMenu());
	}

	public void close() {
		menu.close();
	}

	public boolean update(double delta) {
		if (helpMenuKey.isDown() && !menu.isShowing()) {
			addHelpMenu(menu);
		}
		menu.update(delta);
		return shouldExit;
	}

	public void render(IRenderContext target) {
		menu.render(target);
	}

	public boolean isShowing() {
		return menu.isShowing();
	}

	private Menu getDefaultMenu() {
		return new Menu(new String[] { "New Game", "Save Game", "Load Game",
				"Options", "Help", "Exit" }, new IMenuHandler() {
			@Override
			public void handleMenu(int option, MenuStack stack) {
				try {
					switch (option) {
					case 0:
						gameIO.startNewGame();
						break;
					case 1:
						stack.push(new Menu(GameIO.getSaveFiles(numSaveFiles),
								new IMenuHandler() {
									@Override
									public void handleMenu(int option,
											MenuStack stack) {
										try {
											gameIO.saveGame(option);
										} catch (IOException e) {
											scene.enterErrorState(e);
										}
									}
								}));
						break;
					case 2:
						stack.push(new Menu(GameIO.getSaveFiles(numSaveFiles),
								new IMenuHandler() {
									@Override
									public void handleMenu(int option,
											MenuStack stack) {
										try {
											gameIO.loadGame(option);
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
					scene.enterErrorState(e);
				}
			}
		});
	}

	private void addHelpMenu(MenuStack stack) {
		stack.push(new Menu(new String[] { helpMessage }, new IMenuHandler() {
			@Override
			public void handleMenu(int option, MenuStack stack) {
				stack.pop();
			}
		}));
	}
}
