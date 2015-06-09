package engine.input.awt;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;

import engine.input.IInput;

public class AWTInput implements KeyListener, FocusListener,
		MouseListener, MouseMotionListener, IInput {
	private static final int NUM_MOUSE_BUTTONS = 4;
	private static final int NUM_KEYS = 65536;
	private final boolean[] keys = new boolean[NUM_KEYS];
	private final boolean[] mouseButtons = new boolean[NUM_MOUSE_BUTTONS];
	private int mouseX = 0;
	private int mouseY = 0;

	@Override
	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int code = e.getButton();
		if(code > 0 && code < mouseButtons.length) {
			mouseButtons[code] = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int code = e.getButton();
		if(code > 0 && code < mouseButtons.length) {
			mouseButtons[code] = false;
		}
	}

	@Override
	public void focusGained(FocusEvent e) {
	}

	@Override
	public void focusLost(FocusEvent e) {
		Arrays.fill(keys, false);
		Arrays.fill(mouseButtons, false);
	}
	
	private void setKey(int code, boolean val) {
		if(code >= 0 && code < keys.length) {
			keys[code] = val;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		setKey(e.getKeyCode(), true);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		setKey(e.getKeyCode(), false);
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
	
	@Override
	public boolean getKey(int code) {
		return keys[code];
	}

	@Override
	public boolean getMouse(int button) {
		return mouseButtons[button];
	}

	@Override
	public int getMouseX() {
		return mouseX;
	}

	@Override
	public int getMouseY() {
		return mouseY;
	}
}
