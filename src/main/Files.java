package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class Files {

	private static final String fileDocsPath = "D:\\Documents\\Demo\\vector-space\\IR-vector-space\\src\\data\\doc-text";
	private static final String fileQueriesPath = "D:\\Documents\\Demo\\vector-space\\IR-vector-space\\src\\data\\query-text";
	private static final String fileResultPath = "D:\\Documents\\Demo\\vector-space\\IR-vector-space\\src\\data\\result\\result.txt";

	private static final Logger log = Logger.getLogger(Files.class.getName());

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static Map<Integer, String> readFile(Boolean isQuery) throws Exception {
		File file = new File(fileDocsPath);
		if (isQuery) {
			file = new File(fileQueriesPath);
		}
		Scanner sc = new Scanner(file);
		String line;
		Integer index = null;
		Map<Integer, String> docs = new HashMap<>();
		StringBuilder sb = null;
		log.info("Start read files");
		while (sc.hasNextLine()) {
			line = sc.nextLine();
			if (isNumeric(line)) {
				index = Integer.parseInt(line);
				sb = new StringBuilder();
			}
			if (!line.trim().equalsIgnoreCase("/") && !isNumeric(line)) {
				sb.append(line.trim());
				sb.append(" ");
			}
			if (line.trim().equalsIgnoreCase("/")) {
				docs.put(index, sb.toString().toLowerCase().trim());
			}
		}
		log.info("End read files");
		return docs;
	}

	public static void writeFile(Map<Integer, Map<Integer, Double>> contens) {
		log.info("Start write to file");
		try {
			String str;
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileResultPath));
			for (Map.Entry<Integer, Map<Integer, Double>> data : contens.entrySet()) {
				str = data.getKey().toString();
				writer.write(str);
				writer.newLine();
				for (Map.Entry<Integer, Double> result : data.getValue().entrySet()) {
					str = " doc-" + result.getKey().toString() + ": " + result.getValue();
					writer.write(str);
				}
				writer.newLine();
				writer.write("/");
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			System.out.print(e.getMessage());
		} finally {
			log.info("End write");
		}
	}

}
