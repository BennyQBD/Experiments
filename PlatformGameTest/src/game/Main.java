package game;

import java.io.IOException;
import java.text.ParseException;

import org.lwjgl.LWJGLException;

import engine.core.CoreEngine;
import engine.rendering.IDisplay;
import engine.rendering.opengl.OpenGLDisplay;
import engine.util.BitmapFactory;
import engine.util.parsing.Config;

public class Main {
	public static void main(String[] args) throws IOException, ParseException, LWJGLException {
		Config test = new Config("./res/test.cfg");
		//IDisplay display = new AWTDisplay(256, 224, 3.0, "My Game");
		IDisplay display = new OpenGLDisplay(256, 224, 3.0, "My Game");
		CoreEngine engine = new CoreEngine(display, new PlatformScene(test,
				display.getInput(), BitmapFactory.TYPE_OPENGL), 60.0);
		engine.start();
		engine.dispose();
	}
}
