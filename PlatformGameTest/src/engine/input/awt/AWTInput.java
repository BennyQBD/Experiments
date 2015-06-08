package engine.input.awt;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import engine.input.IInput;
import engine.input.InputListener;

public class AWTInput implements KeyListener, FocusListener,
		MouseListener, MouseMotionListener, IInput {
	private final List<InputListener> listeners = new ArrayList<InputListener>();
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
		Iterator<InputListener> it = listeners.iterator();
		while(it.hasNext()) {
			it.next().releaseAll();
		}
		for(int i = 0; i < mouseButtons.length; i++) {
			mouseButtons[i] = false;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		Iterator<InputListener> it = listeners.iterator();
		while(it.hasNext()) {
			it.next().pressed(code);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();
		Iterator<InputListener> it = listeners.iterator();
		while(it.hasNext()) {
			it.next().released(code);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
	
	public InputListener register(InputListener listener) {
		listeners.add(listener);
		return listener;
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
