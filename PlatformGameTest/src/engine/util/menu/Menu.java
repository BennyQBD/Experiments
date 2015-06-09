package engine.util.menu;

import engine.rendering.IRenderContext;
import engine.rendering.SpriteSheet;
import engine.util.Util;

public class Menu {
	private String[] options;
	private IMenuHandler handler;
	private int selectionIndex;

	private MenuStack menuStack;

	public Menu(String[] options, IMenuHandler handler) {
		this.options = options;
		this.handler = handler;
		this.selectionIndex = 0;
		this.menuStack = null;
	}

	public void setMenuStack(MenuStack stack) {
		this.menuStack = stack;
	}

	public void activate() {
		handler.handleMenu(selectionIndex, menuStack);
	}

	public void move(int amt) {
		selectionIndex = Util.floorMod(selectionIndex + amt, options.length);
	}

	public void render(IRenderContext target, SpriteSheet font, int offsetX,
			int offsetY, int selectionColor, int fontColor) {
		for (int i = 0, y = offsetY; i < options.length; i++, y += font
				.getSpriteHeight()) {
			int color = i == selectionIndex ? selectionColor : fontColor;
			target.drawString(options[i], font, offsetX, y, color, target.getWidth());
		}
	}
}
