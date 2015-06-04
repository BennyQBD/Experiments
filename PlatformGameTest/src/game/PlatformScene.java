package game;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;

import engine.core.BitmapFactory;
import engine.core.Delay;
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
import engine.rendering.IBitmap;
import engine.rendering.IRenderContext;
import engine.rendering.SpriteSheet;
import game.components.CollectableComponent;
import game.components.EnemyComponent;
import game.components.PlayerComponent;

public class PlatformScene extends Scene {
	private static final int STATE_RUNNING = 0;
	private static final int STATE_LOST_LIFE = 1;
	private static final int STATE_ERROR = 2;
	private Entity player;
	private PlayerComponent playerComponent;
	private BitmapFactory bitmaps;
	private SpriteSheet font;
	private SpriteSheet livesIcon;

	private IInput input;
	private Config config;

	private int lives;
	private int state;
	private Delay lostLifeDelay;
	private String errorMessage;

	private void loadLevel(Config config, IInput input,
			ISpatialStructure<Entity> structure, int points) throws IOException {
		IBitmap level = bitmaps.get("./res/" + config.getString("level.data"));
		int[] backgrounds = new int[5];

		SpriteSheet tileSheet = new SpriteSheet(
				bitmaps.get("./res/tilesheet.png"), 16);
		font = new SpriteSheet(bitmaps.get("./res/monospace.png"), 16);
		livesIcon = new SpriteSheet(bitmaps.get("./res/livesicon.png"), 1);

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
						color, points);
			}
		}
	}

	private void addEntity(Config config, IInput input,
			ISpatialStructure<Entity> structure, int x, int y,
			BitmapFactory bitmaps, SpriteSheet tileSheet, int color, int points)
			throws IOException {
		if (color == 255) {
			player = new Entity(structure, x, y, 1, true);
			new SpriteComponent(player, new SpriteSheet(
					bitmaps.get("./res/playertest.png"), 1), 0);
			playerComponent = new PlayerComponent(player, points, 2,
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

	public PlatformScene(Config config, IInput input) throws IOException {
		super(new Grid<Entity>(16, 256, 256));
		this.input = input;
		this.config = config;
		this.bitmaps = new BitmapFactory();
		this.state = STATE_RUNNING;
		this.lostLifeDelay = new Delay(3.0);
		this.errorMessage = "";
		lives = 3;
		loadLevel(config, input, getStructure(), 0);
	}

	private static Entity add(ISpatialStructure<Entity> structure, double posX,
			double posY, double posZ, boolean isBlocking,
			SpriteSheet spriteSheet, int spriteIndex) {
		Entity result = new Entity(structure, posX, posY, posZ, isBlocking);
		new SpriteComponent(result, spriteSheet, spriteIndex);
		return result;
	}

	public void update(double delta) {
		if (playerComponent.getHealth() == 0) {
			getStructure().clear();
			lives--;
			int points = playerComponent.getPoints();
			if (lives <= 0) {
				points = 0;
			}
			try {
				loadLevel(config, input, getStructure(), points);
			} catch (IOException e) {
				state = STATE_ERROR;
				errorMessage = getStackTrace(e);
				return;
			}
			state = STATE_LOST_LIFE;
			lostLifeDelay.reset();
		}

		switch (state) {
		case STATE_RUNNING:
			updateRange(delta, player.getAABB().expand(200, 200, 0));
			break;
		case STATE_LOST_LIFE:
			if (lostLifeDelay.over(delta)) {
				state = STATE_RUNNING;
				if (lives == 0) {
					lives = 3;
				}
			}
			break;
		}
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

	private void drawHUD(IRenderContext target) {
		target.drawString(String.format("%07d", playerComponent.getPoints()),
				font, 0, 0, 0xFFFFFF);
		target.drawString(playerComponent.getHealth() + "", font,
				target.getWidth() - font.getSpriteWidth(), 0, 0xFFFFFF);
		target.drawSprite(livesIcon, 0, 0,
				target.getHeight() - livesIcon.getSpriteHeight(), 1.0, false,
				false, 0xFFFFFF);
		target.drawString(lives + "", font, livesIcon.getSpriteWidth(),
				target.getHeight() - font.getSpriteHeight(), 0xFFFFFF);
	}
	
	private static String wrapString(String str, int maxLength) {
		StringTokenizer st=new StringTokenizer(str);
		int spaceLeft=maxLength;
		int spaceWidth=1;
		StringBuilder sb = new StringBuilder();
		while(st.hasMoreTokens())
		{
			String word=st.nextToken();
			if((word.length()+spaceWidth)>spaceLeft)
			{
				sb.append("\n"+word+" ");
				spaceLeft=maxLength-word.length();
			}
			else
			{
				sb.append(word+" ");
				spaceLeft-=(word.length()+spaceWidth);
			}
		}
		return sb.toString();
	}

	private static void drawMessage(IRenderContext target, String str, int x,
			int y, SpriteSheet font) {
		int maxLength = target.getWidth() / font.getSpriteWidth();
		str = wrapString(str, maxLength);
		String[] strs = str.split("\n");
		for (int i = 0; i < strs.length; i++) {
			String[] wrappedStrings = strs[i].split("(?<=\\G.{" + maxLength
					+ "})");
			for (int j = 0; j < wrappedStrings.length; j++, y += font
					.getSpriteHeight()) {
				target.drawString(wrappedStrings[j], font, x, y, 0xFFFFFF);
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

		switch (state) {
		case STATE_RUNNING:
			renderScene(target, viewportX, viewportY);
			break;
		case STATE_LOST_LIFE:
			target.clear(0x000000);
			player.render(target, (int) Math.round(viewportX),
					(int) Math.round(viewportY));
			String gameOverString = "Game Over";
			if (lives == 0) {
				target.drawString(
						gameOverString,
						font,
						target.getWidth()
								/ 2
								- (int) ((gameOverString.length() / 2.0) * font
										.getSpriteWidth()), 0, 0xFFFFFF);
			}
			break;
		case STATE_ERROR:
			target.clear(0x880000);
			String errorHeader = "Error";
			target.drawString(
					errorHeader,
					font,
					target.getWidth()
							/ 2
							- (int) ((errorHeader.length() / 2.0) * font
									.getSpriteWidth()), 0, 0xFFFFFF);
			drawMessage(target, errorMessage, 0, font.getSpriteHeight(), font);
			return;
		}
		drawHUD(target);
	}
}
