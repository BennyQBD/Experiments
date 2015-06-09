package engine.input;

public interface IInput {
	public boolean getMouse(int button);
	public int getMouseX();
	public int getMouseY();
	boolean getKey(int code);
}
