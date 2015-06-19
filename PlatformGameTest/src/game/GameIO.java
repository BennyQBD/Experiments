package game;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import engine.util.parsing.Config;

public class GameIO {
	private PlatformScene scene;
	private PlatformLevel level;
	
	public GameIO(PlatformScene scene, PlatformLevel level) {
		this.scene = scene;
		this.level = level;
	}
	
	public static String[] getSaveFiles(int num) {
		String[] result = new String[num];
		for (int i = 0; i < num; i++) {
			result[i] = getSaveName(i);
			File tester = new File(getSavePath(i));
			if (tester.exists() && !tester.isDirectory()) {
				result[i] += "(" + new Date(tester.lastModified()) + ")";
			}
		}
		return result;
	}
	
	public void startNewGame() throws IOException {
		scene.startNewGame();
	}
	
	public void saveGame(int saveNum) throws IOException {
		Map<String, String> saveData = new HashMap<String, String>();
		saveData.put("points", level.getPlayerInventory().getPoints() + "");
		saveData.put("lives", level.getPlayerInventory().getLives() + "");
		saveData.put("lifeDeficit", level.getPlayerInventory().getLifeDeficit()
				+ "");
		Config.write(getSavePath(saveNum), saveData);
	}

	public void loadGame(int saveNum) throws IOException, ParseException {
		Config saveFile = new Config(getSavePath(saveNum));
		scene.startNewGame(saveFile.getInt("points"), saveFile.getInt("lives"),
				saveFile.getInt("lifeDeficit"), 0);
	}
	
	private static String getSaveName(int saveNum) {
		return "save" + saveNum + ".cfg";
	}

	private static String getSavePath(int saveNum) {
		return "./res/" + getSaveName(saveNum);
	}
}
