package game;

import java.util.Iterator;

import engine.rendering.Color;
import engine.rendering.IRenderContext;
import engine.rendering.SpriteSheet;
import game.components.InventoryComponent;

public class HUD {
	private SpriteSheet font;
	private SpriteSheet livesIcon;
	private SpriteSheet healthIcon;

	public HUD(SpriteSheet font, SpriteSheet livesIcon, SpriteSheet healthIcon) {
		this.font = font;
		this.livesIcon = livesIcon;
		this.healthIcon = healthIcon;
	}

	public void render(IRenderContext target, PlatformLevel level,
			String errorMessage) {
		if (!errorMessage.isEmpty()) {
			drawError(target, errorMessage);
		} else {
			drawPoints(target, level.getPlayerInventory().getPoints());
			drawPlayerHealth(target, level.getPlayerInventory().getHealth());
			drawLives(target, level.getPlayerInventory().getLives());
			drawInventory(target, level);
			drawGameOver(target, level.getPlayerInventory().getLives());
		}
	}

	private static void drawInventory(IRenderContext target, PlatformLevel level) {
		InventoryComponent inventory = level.getPlayerInventory();
		double x = (double) target.getWidth();
		boolean adjustedX = false;
		Iterator<Integer> it = inventory.getItemIterator();
		while (it.hasNext()) {
			int id = it.next();
			SpriteSheet sprite = level.getItemSprite(id);
			if (!adjustedX) {
				x -= (sprite.getAABB(0).getMaxX() + 1);
				adjustedX = true;
			} else {
				x -= (sprite.getAABB(0).getWidth() + 1);
			}
			target.drawSprite(sprite, 0, x,
					target.getHeight() - sprite.getSpriteHeight(), 1.0, false,
					false, level.getItemColor(id));
		}
	}

	private void drawGameOver(IRenderContext target, int lives) {
		String gameOverString = "Game Over";
		if (lives <= 0) {
			target.drawString(
					gameOverString,
					font,
					target.getWidth()
							/ 2
							- ((gameOverString.length() / 2.0) * font
									.getSpriteWidth()), 0, Color.WHITE, 0);
		}
	}

	private void drawError(IRenderContext target, String errorMessage) {
		target.clear(0.0f, 0.5f, 0.0f, 0.0f);
		String errorHeader = "Error";
		target.drawString(errorHeader, font, target.getWidth() / 2
				- ((errorHeader.length() / 2.0) * font.getSpriteWidth()),
				0, Color.WHITE, 0);
		target.drawString(errorMessage, font, 0, font.getSpriteHeight(),
				Color.WHITE, target.getWidth());
	}

	private void drawLives(IRenderContext target, int lives) {
		target.drawSprite(livesIcon, 0, 0,
				target.getHeight() - livesIcon.getSpriteHeight(), 1.0, false,
				false, Color.WHITE);
		target.drawString(lives + "", font, livesIcon.getSpriteWidth(),
				target.getHeight() - font.getSpriteHeight(), Color.WHITE, 0);
	}

	private void drawPoints(IRenderContext target, int points) {
		target.drawString(String.format("%07d", points), font, 0, 0, Color.WHITE,
				0);
	}

	private void drawPlayerHealth(IRenderContext target, int health) {
		for (int i = 0, x = target.getWidth() - healthIcon.getSpriteWidth() - 1; i < health; i++, x -= (healthIcon
				.getSpriteWidth() + 1)) {
			target.drawSprite(healthIcon, 0, x, 0, 1.0, false, false, Color.WHITE);
		}
	}
}
