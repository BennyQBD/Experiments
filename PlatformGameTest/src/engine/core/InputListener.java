package engine.core;

public class InputListener {
	private int[] keyCodes;
	private IInput parent;

	public InputListener(IInput parent, int[] keyCodes) {
		this.keyCodes = keyCodes;
		this.parent = parent;
	}

	public boolean isDown() {
		for(int i = 0; i < keyCodes.length; i++) {
			if(parent.getKey(keyCodes[i])) {
				return true;
			}
		}
		return false;
	}
}
