package engine.rendering;

import engine.input.IInput;

public interface IDisplay {
	public void swapBuffers();

	public boolean isClosed();
	public void dispose();
	
	public IRenderContext getRenderContext();
	public IRenderDevice getRenderDevice();
	public IInput getInput();
}
