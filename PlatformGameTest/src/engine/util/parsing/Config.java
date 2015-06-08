package engine.util.parsing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Config {
	private Map<String, String> map;

	public static void write(String fileName, Map<String, String> map)
			throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
			Iterator<Entry<String, String>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> pair = it.next();
				String line = pair.getKey() + "=" + pair.getValue() + "\n";
				bw.write(line);
			}
		}
	}

	public Config(String fileName) throws IOException, ParseException {
		map = new HashMap<String, String>();

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			int lineNumber = 0;
			while ((line = br.readLine()) != null) {
				lineNumber++;
				if (line.isEmpty()) {
					continue;
				}

				char start = line.charAt(0);
				if (start == '[' || start == '#') {
					continue;
				}
				String[] tokens = line.split("=");
				if (tokens.length != 2) {
					throw new ParseException("Line has too many '='",
							lineNumber);
				}

				map.put(tokens[0].trim(), tokens[1].trim());
			}
		}
	}

	public boolean getBoolean(String entry) {
		return Boolean.parseBoolean(map.get(entry));
	}

	public int getInt(String entry) {
		return Integer.parseInt(map.get(entry));
	}

	public double getDouble(String entry) {
		return Double.parseDouble(map.get(entry));
	}

	public String getString(String entry) {
		return map.get(entry);
	}
}
