package engine.rendering;

import engine.core.IInput;

public interface IDisplay {
	public void swapBuffers();

	public boolean isClosed();
	public void dispose();
	
	public IRenderContext getRenderContext();
	public IInput getInput();
}
