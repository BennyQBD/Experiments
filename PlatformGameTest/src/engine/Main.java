package engine;

import engine.rendering.awt.AWTDisplay;
import engine.rendering.IDisplay;
import engine.core.CoreEngine;
import engine.core.Scene;



import engine.parsing.Config;
import engine.core.Debug;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import game.PlatformScene;


public class Main {
	public static void main(String[] args) throws IOException, ParseException {
		Config test = new Config("./res/test.cfg");

		IDisplay display = new AWTDisplay(256, 224, 3.0, "My Game");
		CoreEngine engine = 
			new CoreEngine(display, 
					new PlatformScene(test, display.getInput()),
					60.0);
		engine.start();
		engine.dispose();
	}
}
