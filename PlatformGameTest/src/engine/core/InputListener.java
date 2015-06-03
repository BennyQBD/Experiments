package engine.core;

import java.util.Arrays;

public class InputListener {
	private int[] keyCodes;
	private boolean[] downKeys;

	public InputListener(int[] keyCodes) {
		this.keyCodes = keyCodes;
		downKeys = new boolean[keyCodes.length];
		releaseAll();
	}
	
	public void pressed(int keyCode) {
		setKey(keyCode, true);
	}
	
	public void released(int keyCode) {
		setKey(keyCode, false);
	}
	
	public void releaseAll() {
		Arrays.fill(downKeys, false);
	}
	
	private void setKey(int keyCode, boolean value) {
		for(int i = 0; i < keyCodes.length; i++) {
			if(keyCodes[i] == keyCode) {
				downKeys[i] = value;
			}
		}
	}

	public boolean isDown() {
		for(int i = 0; i < keyCodes.length; i++) {
			if(downKeys[i]) {
				return true;
			}
		}
		return false;
	}
}
