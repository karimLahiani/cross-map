package weka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Arrays;

public class Parser {
	
	public static List<String> attributs = null;
	public static List<String[]> datas = new ArrayList<String[]>();

	public static void loadFiles(String path, String[] filtre) {

		List<Path> files;
		try {
			files = Files.walk(Paths.get(path)).filter(Files::isRegularFile).collect(Collectors.toList());
			// System.out.println(dir.toString());
			for (Path file : files) {
				if (file.toString().contains(filtre[0]) || file.toString().contains(filtre[1])) 
					loadFile(file.toString());					
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void loadFile(String path) throws IOException {
		File f = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(f));

		String st;
		int cmpt = 0;
		
		List<String> lines = new ArrayList<String>();
		while ((st = br.readLine()) != null) {
			
			lines.add(st);
		}
		br.close();
		if (lines.size()>1) {
			if (attributs == null)
				attributs = Arrays.asList(lines.get(0).split(";"));
			datas.add(lines.get(1).split(";"));
		}else
			datas.add(lines.get(0).split(";"));
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static String saveARFF(String fileName, String[] filtre) throws IOException {
		String at = "@attribute ";
		String data = "@data\n";
		String separateur = ",";

		String ressourcesDir = "/ressources/learningBase/";
		String path = System.getProperty("user.dir") + ressourcesDir;

		loadFiles(path, filtre);
		
		//System.out.println(datas.size());
		
		String[] type = new String[datas.get(0).length];
		String str = "\n" + data;

		for (String[] line : datas) {
			for (int i = 0; i < line.length; i++) {
				if (isNumeric(line[i])) {
					type[i] = "numeric";
				} else {
					if (type[i] == null)
						type[i] = "{";
					if (!type[i].contains(line[i]))
						type[i] += line[i] + separateur;

				}

				if (i < line.length - 1)
					str += line[i] + separateur;
			}
			str += line[line.length - 1] + "\n";
		}

		String entete = "@relation Duel\n\n";

		for (int i = 0; i < type.length; i++) {
			if (!type[i].equals("numeric")) {
				type[i] = type[i].substring(0, type[i].length() - 1) + "}";
			}
			if (i < type.length - 1) {
				if (attributs.get(i).contains("LastAction"))
					entete += at + attributs.get(i) + " " +"{hunt,retreat,explore_off,explore_def,follow,shoot,idle}" + "\n";
				else
					entete += at + attributs.get(i) + " " + type[i] + "\n";
			}else
				entete += at + "res " + type[i] + "\n";
		}
		
		FileWriter myWriter = new FileWriter(path + fileName + ".arff");
		myWriter.write(entete);
		myWriter.write(str);

		myWriter.close();
		//System.out.println("done!");
		return path;
	}

}
