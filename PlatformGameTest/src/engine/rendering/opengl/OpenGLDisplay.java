package engine.rendering.opengl;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import engine.input.IInput;
import engine.input.opengl.OpenGLInput;
import engine.rendering.IDisplay;
import engine.rendering.IRenderContext;
import engine.util.Debug;

public class OpenGLDisplay implements IDisplay {
	private IRenderContext frameBuffer;
	private IInput input;

	public OpenGLDisplay(int width, int height, double scale, String title) throws LWJGLException {
		Display.setTitle(title);
		int scaledWidth = (int)(width*scale);
		int scaledHeight = (int)(height*scale);
		
		Display.setDisplayMode(new DisplayMode(scaledWidth, scaledHeight));
		Display.create();
		Keyboard.create();
		Mouse.create();
		
		Display.setVSyncEnabled(!Debug.IGNORE_FRAME_CAP);
		frameBuffer = new OpenGLRenderContext(width, height);
		input = new OpenGLInput();
	}

	@Override
	public void swapBuffers() {
		Display.update();
	}

	@Override
	public boolean isClosed() {
		return Display.isCloseRequested();
	}

	@Override
	public void dispose() {
		frameBuffer.dispose();
		Display.destroy();
		Keyboard.destroy();
		Mouse.destroy();
	}

	@Override
	public IRenderContext getRenderContext() {
		return frameBuffer;
	}

	@Override
	public IInput getInput() {
		return input;
	}
}
