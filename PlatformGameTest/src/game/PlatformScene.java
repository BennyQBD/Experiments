package game;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import engine.core.Scene;
import engine.core.entity.Entity;
import engine.input.IInput;
import engine.rendering.IRenderContext;
import engine.rendering.IRenderDevice;
import engine.rendering.SpriteSheet;
import engine.space.Grid;
import engine.util.BitmapFactory;
import engine.util.Delay;
import engine.util.LightMapFactory;
import engine.util.SpriteSheetFactory;
import engine.util.parsing.Config;

public class PlatformScene extends Scene {
	private enum State {
		RUNNING, LOST_LIFE, ERROR;
	}

	private static final int DEFAULT_LIVES = 3;
	private PlatformLevel level;
	private HUD hud;
	private GameMenu gameMenu;
	private GameIO gameIO;
	private GradientBackground background;
	private State state;
	private Delay lostLifeDelay;
	private String errorMessage;

	private static String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public void startNewGame() throws IOException {
		startNewGame(0, DEFAULT_LIVES, 0);
	}

	private void initVariables() {
		this.state = State.RUNNING;
		this.lostLifeDelay = new Delay(3.0);
		this.errorMessage = "";
		getStructure().clear();
	}

	public void startNewGame(int points, int lives, int lifeDeficit)
			throws IOException {
		initVariables();
		level.loadLevel(getStructure(), points, lives, lifeDeficit);
		gameMenu.close();
	}

	public PlatformScene(Config config, IInput input, IRenderDevice device)
			throws IOException {
		super(new Grid<Entity>(16, 256, 256));
		SpriteSheetFactory sprites = new SpriteSheetFactory(new BitmapFactory(
				device));
		this.level = new PlatformLevel(device, input, config, sprites,
				new LightMapFactory(device));
		SpriteSheet font = sprites.get("./res/monospace.png", 16, 16);

		this.gameIO = new GameIO(this, level);
		this.gameMenu = new GameMenu(this, config, gameIO, input, font);
		this.hud = new HUD(font, sprites.get("./res/livesicon.png", 1, 1),
				sprites.get("./res/healthicon.png", 1, 1));
		this.background = new GradientBackground(device);
		startNewGame();
	}

	public void enterErrorState(Exception e) {
		state = State.ERROR;
		errorMessage = getStackTrace(e);
	}

	private void checkForLostLife() {
		if (level.getPlayerComponent().getHealth() <= 0) {
			getStructure().clear();
			int lives = level.getPlayerInventory().getLives() - 1;
			int points = level.getPlayerInventory().getPoints();
			int lifeDeficit = level.getPlayerInventory().getLifeDeficit();
			if (lives <= 0) {
				points = 0;
				lifeDeficit = 0;
			}
			try {
				level.loadLevel(getStructure(), points, lives, lifeDeficit);
			} catch (IOException e) {
				enterErrorState(e);
				return;
			}
			state = State.LOST_LIFE;
			lostLifeDelay.reset();
		}
	}

	public boolean update(double delta) {
		boolean shouldExit = gameMenu.update(delta);
		if (shouldExit || gameMenu.isShowing()) {
			return shouldExit;
		}
		checkForLostLife();
		switch (state) {
		case RUNNING:
			updateRange(delta, level.getPlayer().getAABB().expand(200, 200, 0));
			break;
		case LOST_LIFE:
			if (lostLifeDelay.over(delta)) {
				state = State.RUNNING;
				if (level.getPlayerInventory().getLives() <= 0) {
					while (level.getPlayerInventory().getLives() < DEFAULT_LIVES) {
						level.getPlayerInventory().addLives(1);
					}
				}
			}
			break;
		case ERROR:
			// Nothing to do
			break;
		}
		return false;
	}

	private void renderScene(IRenderContext target, double viewportX,
			double viewportY) {
		background.render(target, 1.0, 0.0, 0.0, 4,
				(int) Math.round(viewportX), (int) Math.round(viewportY));
		renderRange(target, viewportX, viewportY);
	}

	public void render(IRenderContext target) {
		double viewportOffsetX = Math.round((target.getWidth() - level
				.getPlayer().getAABB().getWidth()) / 2);
		double viewportOffsetY = Math.round((target.getHeight() - level
				.getPlayer().getAABB().getHeight()) / 2);
		double viewportX = level.getPlayer().getAABB().getMinX()
				- viewportOffsetX;
		double viewportY = level.getPlayer().getAABB().getMinY()
				- viewportOffsetY;
		int viewportXInt = (int) Math.round(viewportX);
		int viewportYInt = (int) Math.round(viewportY);

		switch (state) {
		case RUNNING:
			target.clearLighting();
			renderScene(target, viewportX, viewportY);
			target.drawLight(level.getStaticLightMap(), 0, 0, viewportXInt,
					viewportYInt, target.getWidth(), target.getHeight());
			target.applyLighting(16.0 / 256.0);
			break;
		case LOST_LIFE:
			target.clear(0.0, 0.0, 0.0, 0.0);
			level.getPlayer().render(target, (int) Math.round(viewportX),
					(int) Math.round(viewportY));
			break;
		case ERROR:
			// Nothing to do
			break;
		}
		hud.render(target, level, errorMessage);
		gameMenu.render(target);
	}
}
