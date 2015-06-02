package engine.core.awt;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import engine.core.IInput;

public class AWTInput implements KeyListener, FocusListener,
		MouseListener, MouseMotionListener, IInput {
	private final boolean[] keys = new boolean[65536];
	private final boolean[] mouseButtons = new boolean[4];
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
		for(int i = 0; i < keys.length; i++) {
			keys[i] = false;
		}
		for(int i = 0; i < mouseButtons.length; i++) {
			mouseButtons[i] = false;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		if (code > 0 && code < keys.length) {
			keys[code] = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();
		if (code > 0 && code < keys.length) {
			keys[code] = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public boolean getKey(int key) {
		return keys[key];
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
