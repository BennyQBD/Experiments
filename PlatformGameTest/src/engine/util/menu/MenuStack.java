package engine.util.menu;

import java.util.Stack;

import engine.input.InputListener;
import engine.rendering.IRenderContext;
import engine.rendering.SpriteSheet;
import engine.util.Delay;

public class MenuStack {
	private Stack<Menu> menuStack;
	private Menu defaultMenu;
	private SpriteSheet font;
	private InputListener downKey;
	private InputListener upKey;
	private InputListener activateKey;
	private InputListener toggleKey;
	private Delay moveDelay;
	private Delay toggleDelay;
	private Delay activateDelay;
	private int fontColor;
	private int selectionColor;
	private int offsetX;
	private int offsetY;

	public MenuStack(SpriteSheet font, int fontColor, int selectionColor,
			int offsetX, int offsetY, InputListener upKey,
			InputListener downKey, InputListener activateKey,
			InputListener toggleKey, double usageDelayLength, Menu defaultMenu) {
		this.font = font;
		this.menuStack = new Stack<Menu>();
		this.toggleKey = toggleKey;
		this.toggleDelay = new Delay(usageDelayLength);
		this.defaultMenu = defaultMenu;
		this.fontColor = fontColor;
		this.selectionColor = selectionColor;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.upKey = upKey;
		this.downKey = downKey;
		this.activateKey = activateKey;
		this.moveDelay = new Delay(usageDelayLength);
		this.activateDelay = new Delay(usageDelayLength);
	}

	public void update(double delta) {
		if (toggleDelay.over(delta) && toggleKey.isDown()) {
			toggleDelay.reset();
			if (isShowing()) {
				menuStack.pop();
			} else {
				menuStack.push(defaultMenu);
			}
		}

		if (!isShowing()) {
			return;
		}

		boolean canMove = moveDelay.over(delta);
		if (canMove && upKey.isDown()) {
			getCurrentMenu().move(-1);
			moveDelay.reset();
		}
		if (canMove && downKey.isDown()) {
			getCurrentMenu().move(1);
			moveDelay.reset();
		}

		if (activateDelay.over(delta) && activateKey.isDown()) {
			getCurrentMenu().activate();
			activateDelay.reset();
		}
	}

	public void pop() {
		Menu menu = getCurrentMenu();
		if (menu != null) {
			menu.setMenuStack(null);
			menuStack.pop();
		}
	}

	public void push(Menu menu) {
		menuStack.push(menu);
		menu.setMenuStack(this);
	}

	public void render(IRenderContext target) {
		if (isShowing()) {
			getCurrentMenu().render(target, font, offsetX, offsetY,
					selectionColor, fontColor);
		}
	}

	private Menu getCurrentMenu() {
		if (menuStack.empty()) {
			return null;
		}
		return menuStack.peek();
	}

	public boolean isShowing() {
		return !menuStack.empty();
	}
}
