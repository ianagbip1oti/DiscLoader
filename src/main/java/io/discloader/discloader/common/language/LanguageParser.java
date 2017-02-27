package io.discloader.discloader.common.language;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Stream;

public class LanguageParser {

	public static HashMap<String, HashMap<String, HashMap<String, String>>> parseLang(File lang) {
		HashMap<String, HashMap<String, HashMap<String, String>>> types = new HashMap<String, HashMap<String, HashMap<String, String>>>();
		try {
			Stream<String> stream = Files.lines(lang.toPath());
			for (Object line : stream.toArray()) {
				System.out.println(line.toString());
			}
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return types;
	}

	public static HashMap<String, HashMap<String, HashMap<String, String>>> parseLang(InputStream langStream) {
		HashMap<String, HashMap<String, HashMap<String, String>>> types = new HashMap<String, HashMap<String, HashMap<String, String>>>();
		Scanner sc = new Scanner(langStream);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] l = line.split("[.]");
			if (l.length < 1 || l.length > 3)
				continue;
			String type = l[0], field = l[1], propVal = l[2];
			if (propVal.indexOf('=') == -1) {
				continue;
			}
			String prop = propVal.split("=")[0], value = propVal.split("=")[1];
			System.out.print(String.format("%s.%s.%s = %s", type, field, prop, value));
			if (!types.containsKey(type)) {
				types.put(type, new HashMap<String, HashMap<String, String>>());
			}
			HashMap<String, HashMap<String, String>> fields = types.get(type);
			if (!fields.containsKey(field)) {
				fields.put(field, new HashMap<String, String>());
			}
			HashMap<String, String> props = fields.get(field);
			props.put(prop, value);
		}
		sc.close();
		return types;
	}

}