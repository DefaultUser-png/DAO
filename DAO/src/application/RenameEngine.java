package application;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

enum sort {
	DATE("By Date"),
	LAST_MODIFIED("By Last Modified"),
	SIZE("By Size"),
	NAME("By Name"),
	TYPE("By Type");

	private String name;

	sort(String name) {
		this.name = name;
	}

	@Override public String toString() { return name; }
}

enum apply_mode {
	NAME("Name"),
	EXTENSION("Extension"),
	BOTH("Both");

	private String name;

	apply_mode(String name) {
		this.name = name;
	}

	@Override public String toString() { return name; }
}

enum log {
	NONE("None"),
	CONSOLE("Console"),
	FILE("File");

	private String name;

	log(String name) {
		this.name = name;
	}

	@Override public String toString() { return name; }
}


public class RenameEngine {
	public String name = "";
	public sort sortBy = sort.DATE;
	public apply_mode mode = apply_mode.NAME;
	public boolean sortReverse = false;
	public log logging = log.CONSOLE;
	public String loggingPath = "";

	public FileWrapper[] files;
	public Map<FileWrapper, String> filesBuffer = new HashMap<FileWrapper, String>();
	public FileOperations fileOBJ = new FileOperations();
	public boolean backup = false;


	// Converts List<File> into FileWrapper array
	public void wrap(List<File> list) {
		files = new FileWrapper[list.size()];
		for(int i=0; i<list.size(); i++) {
			files[i] = new FileWrapper(list.get(i));
		}
	}

	public void sort() {
		sortFiles(files, sortBy, sortReverse);
	}

	// Folder sorting
	private void sortFiles(FileWrapper[] arr, sort sortBy, boolean reverse) {
		Comparator<FileWrapper> c = null;
		switch(sortBy) {
		case DATE:
			c = new Comparator<FileWrapper>(){
				public int compare(FileWrapper f1, FileWrapper f2) {
					return f1.getAttCreationTime().compareTo(f2.getAttCreationTime());
				}};
				break;
		case LAST_MODIFIED:
			c = Comparator.comparingLong(FileWrapper::getLastModified);
			break;
		case SIZE:
			c = Comparator.comparingLong(FileWrapper::getSize);
			break;
		case NAME:
			c = Comparator.comparing(FileWrapper::getName);
			break;
		case TYPE:
			c = Comparator.comparing(FileWrapper::getExtension);
			break;
		}

		if(reverse) {
			Arrays.sort(arr, c.reversed());
			return;
		}
		Arrays.sort(arr, c);
	}



	// Creates a new name according to the mode
	String mode_check(String nameOG, String extension, int i) {
		switch(mode) {
		case NAME:
			return
				strip_keys(name, i, 0) +
				extension;
		case EXTENSION:
			return
				nameOG +
				strip_keys(name, i, 0);
		case BOTH:
			return
				strip_keys(name, i, 0);
		}
		return "";
	}

	// Replaces the key and calls itself to check the rest of the string
	String strip_keys(String s, int increment, int start_from) {
		boolean brackets = false;
		int pos1 = -1;
		int pos2 = -1;
		for(int j = start_from; j < s.length(); j++) {
			char c = s.charAt(j);

			if(!brackets) {
				if(c == '$') brackets = true;
				continue;
			}

			if(c == '<') {
				pos1 = j - 1;
				continue;
			}

			if(c == '>') {
				pos2 = j;
				break;
			}
		}
		// No keys
		if(pos1 == -1 || pos2 == -1) return s;

		// Key found
		String left = s.substring(0, pos1);
		String key = s.substring(pos1 + 2, pos2);
		String right = s.substring(pos2 + 1);
		// No args
		if(key.equals("")) {
			key = increment + "";
			s = left + key + right;
			return strip_keys(s, increment, (left + key).length());
		}

		// Timestamp; I'm so sorry.
		if(key.matches("[yMdHms_-]+")) {
			String toParse = files[increment].getAttCreationTime().toString();
			// Why would they add a random "Z" at the end?
			toParse = toParse.substring(0, toParse.length() - 1);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(key);
			LocalDateTime date = LocalDateTime.parse(toParse);
			key = date.format(formatter);

			s = left + key + right;
			return strip_keys(s, increment, (left + key).length());
		}

		// - Keys with arguments -
		String[] args = key.toUpperCase().split(" ");
		String rand = find_arg(args, "RANDB");
		String randStr = find_arg(args, "RANDSTR");
		String randInt = find_arg(args, "RANDINT");

		String increment_final_str = find_arg(args, "INCREMENT");
		String start = find_arg(args, "START");
		String fixed = find_arg(args, "FIXED");
		
		// Random
		if(!rand.isEmpty()) {
			key = random_fill(true, true, Integer.valueOf(rand));
			s = left + key + right;
			return strip_keys(s, increment, (left + key).length());
		}
		if(!randStr.isEmpty()) {
			key = random_fill(true, false, Integer.valueOf(randStr));
			s = left + key + right;
			return strip_keys(s, increment, (left + key).length());
		}
		if(!randInt.isEmpty()) {
			key = random_fill(false, true, Integer.valueOf(randInt));
			s = left + key + right;
			return strip_keys(s, increment, (left + key).length());
		}
		
		// Increment modifiers
		key = "";
		int increment_final = increment;
		if(!increment_final_str.isEmpty()) {
			increment_final *= Integer.valueOf(increment_final_str);
		}
		if(!start.isEmpty())
			increment_final += Integer.valueOf(start);
		key += increment_final;
		if(!fixed.isEmpty()) {
			int digits = 1;
			if(increment_final != 0) {
				digits = (int) (Math.log10(increment_final) + 1);
			}
			int repeat = Integer.valueOf(fixed) - digits;
			if(repeat > 0) key = "0".repeat(repeat) + key;
		}
		s = left + key + right;
		return strip_keys(s, increment, (left + key).length());
	}

	String find_arg(String[] args, String key) {
		for(String arg : args) {
			if(!arg.contains(key)) continue;

			int index = arg.lastIndexOf("=");
			if (index == -1) return "";
			return arg.substring(index + 1);
		}
		return "";
	}
	String random_fill(boolean str, boolean digits, int max) {
		String output = "";
		String[] tables = {
				"ABCEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx",
				"0123456789",
				"ABCEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx0123456789"
		};

		int cur = 0;
		if(str && digits)
			cur = 2;
		if(!str && digits)
			cur = 1;

		for(int i=0; i<max; i++) {
			int rand = (int) (Math.random() * tables[cur].length());
			output += tables[cur].charAt(rand);
		}
		return output;
	}


	// Returns data for table
	ObservableList<List<String>> table_data(){
		if(files == null) return null;

		List<List<String>> table = new ArrayList<>();
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isFile()) continue;
			String nameOG = files[i].f.getName();
			String extension = files[i].getExtension();

			List<String> row = Arrays.asList(
					nameOG,
					mode_check(files[i].getName(), extension, i)
					);
			table.add(row);
		}
		return FXCollections.observableArrayList(table);
	}



	void renameAll() {
		if(files == null) return;
		// Creates a path for the log with current time
		if(logging == log.FILE) {
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH-mm-ss");
			String nowFormatted = now.format(formatter);

			fileOBJ.open(loggingPath + nowFormatted + ".txt");
		}
		// Previous file backups are discarded
		if(backup && !filesBuffer.isEmpty())
			filesBuffer.clear();

		for (int i = 0; i < files.length; i++) {
			if (!files[i].isFile()) continue;

			FileWrapper f = files[i];
			String nameOG = f.getName();
			String extension = f.getExtension();
			String path = f.getParent() + "\\";
			String nameFinal = mode_check(nameOG, extension, i);
			boolean renamed = renameFile(f, new File(path + nameFinal), backup);

			// Logging area
			if(logging == log.NONE) continue;
			String message = "";
			if(renamed) {
				message = "<" + nameOG + extension + "> has been renamed to <" + nameFinal + ">";
			} else {
				message = "An error accured renaming <" + nameOG + ">";
			}

			if(logging == log.CONSOLE) {
				System.out.println(message);
				continue;
			}
			fileOBJ.append(message);
		}
		// Updates current files
		if(logging == log.FILE)
			fileOBJ.close();
		System.out.println("Finished");
	}

	boolean renameFile(FileWrapper target, File newFile, boolean reversable) {
		String oldName = target.f.getName();
		boolean renamed = target.f.renameTo(newFile);
		// If failed due to a duplicate, try to rename
		if(!renamed) {
			newFile = target.findNextAvailable(newFile.getName());
			renamed = target.f.renameTo(newFile);
		}
		// If failed still, abandon process
		if(!renamed) return false;
		target.updAttributes(newFile);

		// Buffer for old names
		if(reversable)
			filesBuffer.put(target, oldName);
		return true;
	}

	// Empties the file buffer to undo renaming
	void undo() {
		for (Map.Entry<FileWrapper, String> entry : filesBuffer.entrySet()) {
			FileWrapper newFile = entry.getKey();
			String oldName = newFile.getParent() + "\\" + entry.getValue();
			renameFile(newFile, new File(oldName), false);
		}
		if(backup && !filesBuffer.isEmpty())
			filesBuffer.clear();
	}
}
