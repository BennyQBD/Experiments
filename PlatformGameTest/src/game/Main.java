package game;

import java.io.IOException;
import java.text.ParseException;

import org.lwjgl.LWJGLException;

import engine.core.CoreEngine;
import engine.rendering.IDisplay;
import engine.rendering.opengl.OpenGLDisplay;
import engine.util.parsing.Config;

public class Main {
	public static void main(String[] args) throws IOException, ParseException,
			LWJGLException {
		Config config = new Config("./res/main.cfg");
		int tileSize = config.getInt("level.spriteSize");
		int tilesX = config.getInt("level.display.tilesX");
		int tilesY = config.getInt("level.display.tilesY");
		IDisplay display = new OpenGLDisplay(tileSize * tilesX, tileSize
				* tilesY, config.getInt("level.display.width"),
				config.getInt("level.display.height"), "My Game");
		CoreEngine engine = new CoreEngine(display, new PlatformScene(config,
				display.getInput(), display.getRenderDevice(),
				display.getAudioDevice()),
				config.getDouble("level.display.targetFramerate"));
		engine.start();
		engine.dispose();
	}
}
