package engine.core;

public interface IInput {
	public InputListener register(InputListener listener);
	public boolean getMouse(int button);
	public int getMouseX();
	public int getMouseY();
}
