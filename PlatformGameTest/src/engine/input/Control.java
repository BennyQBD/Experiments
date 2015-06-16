package engine.input;

public class Control {
	private int[] keyCodes;
	private IInput input;

	public Control(IInput input, int[] keyCodes) {
		this.keyCodes = keyCodes;
		this.input = input;
	}

	public boolean isDown() {
		for(int i = 0; i < keyCodes.length; i++) {
			if(input.getKey(keyCodes[i])) {
				return true;
			}
		}
		return false;
	}
}
