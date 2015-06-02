package engine.core;

public interface IInput {
	public boolean getKey(int code);
	public boolean getMouse(int button);
	public int getMouseX();
	public int getMouseY();
}
