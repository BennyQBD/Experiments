package game;

import java.io.IOException;
import java.text.ParseException;

import engine.core.CoreEngine;
import engine.rendering.IDisplay;
import engine.rendering.awt.AWTDisplay;
import engine.util.parsing.Config;

public class Main {
	public static void main(String[] args) throws IOException, ParseException {
		Config test = new Config("./res/test.cfg");
		IDisplay display = new AWTDisplay(256, 224, 3.0, "My Game");
		CoreEngine engine = new CoreEngine(display, new PlatformScene(test,
				display.getInput()), 60.0);
		engine.start();
		engine.dispose();
	}
}
