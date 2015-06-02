package engine.parsing;

import java.util.Map;
import java.util.HashMap;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import java.text.ParseException;

public class Config {
	private Map<String, String> map;
	public Config(String fileName) throws IOException, ParseException {
		map = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			String line;
			int lineNumber = 0;
			while((line = br.readLine()) != null) {
				lineNumber++;
				if(line.isEmpty()) {
					continue;
				}
				
				char start = line.charAt(0);
				if(start == '[' || start == '#') {
					continue;
				}
				String[] tokens = line.split("=");
				if(tokens.length != 2) {
					throw new ParseException("Line has too many '='", lineNumber);
				}

				map.put(tokens[0].trim(), tokens[1].trim());
			}
			
		} finally {
			br.close();
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
