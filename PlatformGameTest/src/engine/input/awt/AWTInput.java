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
	
	private static int[] keyMappings;
	
	public AWTInput() {
		if(keyMappings == null) {
			keyMappings = new int[IInput.NUM_KEYS];
			keyMappings[IInput.KEY_ESCAPE] = KeyEvent.VK_ESCAPE;
			keyMappings[IInput.KEY_1] = KeyEvent.VK_1;
			keyMappings[IInput.KEY_2] = KeyEvent.VK_2;
			keyMappings[IInput.KEY_3] = KeyEvent.VK_3;
			keyMappings[IInput.KEY_4] = KeyEvent.VK_4;
			keyMappings[IInput.KEY_5] = KeyEvent.VK_5;
			keyMappings[IInput.KEY_6] = KeyEvent.VK_6;
			keyMappings[IInput.KEY_7] = KeyEvent.VK_7;
			keyMappings[IInput.KEY_8] = KeyEvent.VK_8;
			keyMappings[IInput.KEY_9] = KeyEvent.VK_9;
			keyMappings[IInput.KEY_0] = KeyEvent.VK_0;
			keyMappings[IInput.KEY_MINUS] = KeyEvent.VK_MINUS;
			keyMappings[IInput.KEY_EQUALS] = KeyEvent.VK_EQUALS;
			keyMappings[IInput.KEY_BACK] = KeyEvent.VK_BACK_SPACE;
			keyMappings[IInput.KEY_TAB] = KeyEvent.VK_TAB;
			keyMappings[IInput.KEY_Q] = KeyEvent.VK_Q;
			keyMappings[IInput.KEY_W] = KeyEvent.VK_W;
			keyMappings[IInput.KEY_E] = KeyEvent.VK_E;
			keyMappings[IInput.KEY_R] = KeyEvent.VK_R;
			keyMappings[IInput.KEY_T] = KeyEvent.VK_T;
			keyMappings[IInput.KEY_Y] = KeyEvent.VK_Y;
			keyMappings[IInput.KEY_U] = KeyEvent.VK_U;
			keyMappings[IInput.KEY_I] = KeyEvent.VK_I;
			keyMappings[IInput.KEY_O] = KeyEvent.VK_O;
			keyMappings[IInput.KEY_P] = KeyEvent.VK_P;
			keyMappings[IInput.KEY_LBRACKET] = KeyEvent.VK_BRACELEFT;
			keyMappings[IInput.KEY_RBRACKET] = KeyEvent.VK_BRACERIGHT;
			keyMappings[IInput.KEY_RETURN] = KeyEvent.VK_ENTER;
			keyMappings[IInput.KEY_LCONTROL] = KeyEvent.VK_CONTROL;
			keyMappings[IInput.KEY_A] = KeyEvent.VK_A;
			keyMappings[IInput.KEY_S] = KeyEvent.VK_S;
			keyMappings[IInput.KEY_D] = KeyEvent.VK_D;
			keyMappings[IInput.KEY_F] = KeyEvent.VK_F;
			keyMappings[IInput.KEY_G] = KeyEvent.VK_G;
			keyMappings[IInput.KEY_H] = KeyEvent.VK_H;
			keyMappings[IInput.KEY_J] = KeyEvent.VK_J;
			keyMappings[IInput.KEY_K] = KeyEvent.VK_K;
			keyMappings[IInput.KEY_L] = KeyEvent.VK_L;
			keyMappings[IInput.KEY_SEMICOLON] = KeyEvent.VK_SEMICOLON;
			keyMappings[IInput.KEY_APOSTROPHE] = KeyEvent.VK_QUOTE;
			keyMappings[IInput.KEY_GRAVE] = KeyEvent.VK_DEAD_GRAVE;
			keyMappings[IInput.KEY_LSHIFT] = KeyEvent.VK_SHIFT;
			keyMappings[IInput.KEY_BACKSLASH] = KeyEvent.VK_BACK_SLASH;
			keyMappings[IInput.KEY_Z] = KeyEvent.VK_Z;
			keyMappings[IInput.KEY_X] = KeyEvent.VK_X;
			keyMappings[IInput.KEY_C] = KeyEvent.VK_C;
			keyMappings[IInput.KEY_V] = KeyEvent.VK_V;
			keyMappings[IInput.KEY_B] = KeyEvent.VK_B;
			keyMappings[IInput.KEY_N] = KeyEvent.VK_N;
			keyMappings[IInput.KEY_M] = KeyEvent.VK_M;
			keyMappings[IInput.KEY_COMMA] = KeyEvent.VK_COMMA;
			keyMappings[IInput.KEY_PERIOD] = KeyEvent.VK_PERIOD;
			keyMappings[IInput.KEY_SLASH] = KeyEvent.VK_SLASH;
			keyMappings[IInput.KEY_RSHIFT] = KeyEvent.VK_SHIFT;
			keyMappings[IInput.KEY_MULTIPLY] = KeyEvent.VK_MULTIPLY;
			keyMappings[IInput.KEY_LMENU] = KeyEvent.VK_ALT;
			keyMappings[IInput.KEY_LALT] = KeyEvent.VK_ALT;
			keyMappings[IInput.KEY_SPACE] = KeyEvent.VK_SPACE;
			keyMappings[IInput.KEY_CAPITAL] = 0; // TODO: Find out what key this is
			keyMappings[IInput.KEY_F1] = KeyEvent.VK_F1;
			keyMappings[IInput.KEY_F2] = KeyEvent.VK_F2;
			keyMappings[IInput.KEY_F3] = KeyEvent.VK_F3;
			keyMappings[IInput.KEY_F4] = KeyEvent.VK_F4;
			keyMappings[IInput.KEY_F5] = KeyEvent.VK_F5;
			keyMappings[IInput.KEY_F6] = KeyEvent.VK_F6;
			// TODO: Finish Key mappings
			
			keyMappings[IInput.KEY_LEFT] = KeyEvent.VK_LEFT;
			keyMappings[IInput.KEY_RIGHT] = KeyEvent.VK_RIGHT;
			keyMappings[IInput.KEY_UP] = KeyEvent.VK_UP;
			keyMappings[IInput.KEY_DOWN] = KeyEvent.VK_DOWN;
		}
	}

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
		return keys[keyMappings[code]];
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
