package game;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import engine.audio.IAudioDevice;
import engine.core.Scene;
import engine.core.entity.Entity;
import engine.input.IInput;
import engine.rendering.Color;
import engine.rendering.IRenderContext;
import engine.rendering.IRenderDevice;
import engine.rendering.SpriteSheet;
import engine.space.AABB;
import engine.space.*;
import engine.util.Delay;
import engine.util.factory.BitmapFactory;
import engine.util.factory.LightMapFactory;
import engine.util.factory.SoundFactory;
import engine.util.factory.SpriteSheetFactory;
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
	private State state;
	private Delay lostLifeDelay;
	private String errorMessage;
	private int updateRangeX;
	private int updateRangeY;

	private static String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public void startNewGame() throws IOException {
		startNewGame(0, DEFAULT_LIVES, 0, 0, 0);
	}

	private void initVariables() {
		this.state = State.RUNNING;
		this.lostLifeDelay = new Delay(3.0);
		this.errorMessage = "";
		getStructure().clear();
	}

	public void startNewGame(int points, int lives, int lifeDeficit,
			int levelNum, int checkpoint) throws IOException {
		initVariables();
		level.loadLevel(getStructure(), points, lives, lifeDeficit, levelNum,
				checkpoint);
		gameMenu.close();
	}

	public PlatformScene(Config config, IInput input, IRenderDevice device,
			IAudioDevice audioDevice) throws IOException {
		super(new QuadTree<Entity>(new AABB(0, 0,
				config.getInt("level.spatialStructure.tileSize")
						* config.getInt("level.spatialStructure.width"),
				config.getInt("level.spatialStructure.tileSize")
						* config.getInt("level.spatialStructure.height")),
				config.getInt("level.spatialStructure.tileCapacity")));
		SpriteSheetFactory sprites = new SpriteSheetFactory(new BitmapFactory(
				device, "./res/gfx/"));
		SoundFactory sounds = new SoundFactory(audioDevice, "./res/sounds/");
		this.level = new PlatformLevel(device, input, config, sprites,
				new LightMapFactory(device), sounds);
		SpriteSheet font = sprites.get("monospace.png", 16, 16);

		this.gameIO = new GameIO(this, level);
		this.gameMenu = new GameMenu(this, config, gameIO, input, font);
		this.hud = new HUD(font, sprites.get("livesicon.png", 1, 1),
				sprites.get("healthicon.png", 1, 1));
		// this.background = new GradientBackground(device, 1.0, 0.0, 0.0);

		int tileSize = config.getInt("level.spriteSize");
		this.updateRangeX = config.getInt("level.updateRangeX") * tileSize;
		this.updateRangeY = config.getInt("level.updateRangeY") * tileSize;

		startNewGame();
	}

	public void enterErrorState(Exception e) {
		state = State.ERROR;
		errorMessage = getStackTrace(e);
	}

	private void checkForLostLife() {
		if (level.getPlayerInventory().getHealth() <= 0) {
			getStructure().clear();
			int lives = level.getPlayerInventory().getLives() - 1;
			int points = level.getPlayerInventory().getPoints();
			int lifeDeficit = level.getPlayerInventory().getLifeDeficit();
			int checkpoint = level.getPlayerInventory().getCheckpoint();
			if (lives <= 0) {
				points = 0;
				lifeDeficit = 0;
				checkpoint = 0;
			}
			try {
				level.loadLevel(getStructure(), points, lives, lifeDeficit,
						level.getLevelNum(), checkpoint);
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
			updateRange(
					delta,
					level.getPlayer().getAABB()
							.expand(updateRangeX, updateRangeY, 0));
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
		level.renderBackground(target, viewportX, viewportY);
		renderRange(target, viewportX, viewportY);
	}

	public void render(IRenderContext target) {
		double viewportOffsetX = ((target.getWidth() - level.getPlayer()
				.getAABB().getWidth()) / 2);
		double viewportOffsetY = ((target.getHeight() - level.getPlayer()
				.getAABB().getHeight()) / 2);
		double viewportX = level.getPlayer().getAABB().getMinX()
				- viewportOffsetX;
		double viewportY = level.getPlayer().getAABB().getMinY()
				- viewportOffsetY;

		switch (state) {
		case RUNNING:
			double ambient = level.getAmbientLight();
			target.clearLighting(ambient, ambient, ambient, ambient);
			renderScene(target, viewportX, viewportY);
			target.drawLight(level.getStaticLightMap(), 0, 0, viewportX,
					viewportY, target.getWidth(), target.getHeight(),
					Color.WHITE);
			target.applyLighting();
			break;
		case LOST_LIFE:
			target.clear(0.0, 0.0, 0.0, 0.0);
			level.getPlayer().render(target, viewportX, viewportY);
			break;
		case ERROR:
			// Nothing to do
			break;
		}
		hud.render(target, level, errorMessage);
		gameMenu.render(target);
	}
}
