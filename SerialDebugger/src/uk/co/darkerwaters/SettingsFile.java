package uk.co.darkerwaters;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SettingsFile {
	
	public static void OpenSettingsFile(File sourceFile, MainWindow window) {
		// load this data
		List<String> list = new LinkedList<String>();
		InputStream in = null;
		try {
			in = new FileInputStream(sourceFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				list.add(line);
			}
			reader.close();
			loadStrings(list, window);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != in) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void SaveSettingsFile(File sourceFile, MainWindow window) {
		// save this data
		List<String> list = createStrings(window);
		BufferedOutputStream bout = null;
		try {
			bout = new BufferedOutputStream(new FileOutputStream(sourceFile));
			PrintWriter writer = new PrintWriter(bout);
			Iterator<String> i = list.iterator();
			while (i.hasNext()) {
				String text = (String) i.next();
				writer.println(text);
			}
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (null != bout) {
				try {
					bout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void loadStrings(List<String> strings, MainWindow window) {
		// process all the data from the strings into the open application
		for (String string : strings) {
			String[] pair = string.split("\\|");
			try {
				window.acceptDataPair(pair[0], pair[1]);
			}
			catch (Exception e) {
				System.out.println("Unable to accept data pair of " + (pair.length > 0 ? pair[0] : "none") + "|" + (pair.length > 1 ? pair[1] : "none") + " because " + e.getMessage());
			}
		}
	}
	
	private static List<String> createStrings( MainWindow window) {
		// process all the data from the strings into the open application
		List<String> strings = new LinkedList<String>();
		for (String[] dataPair : window.getDataPairs()) {
			strings.add(dataPair[0] + "|" + dataPair[1]);
		}
		return strings;
	}
}
