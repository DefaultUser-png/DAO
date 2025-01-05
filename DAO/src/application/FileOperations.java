package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.Scanner;

class FileOperations {
	PrintWriter out;

	public void open(String path) {
		try {
			File fPath = new File(path).getParentFile();
			if(!fPath.exists())
				fPath.mkdirs();
			out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
		} catch (IOException e) {
			System.out.println("Failed to open PrinterWriter at: " + path);
			e.printStackTrace();
		}
	}

	public void close() {
		out.close();
		out = null;
	}

	public void append(String txt) {
		out.println(txt);
	}



	// Create file
	public void create(String path) {
		try {
			File myObj = new File(path);
			if (myObj.createNewFile()) {
				System.out.println("File created: " + myObj.getName());
			} else {
				// System.out.println("File already exists.");
			}
		} catch (IOException e) {
			System.out.println("Failed to create the file: " + path);
			e.printStackTrace();
		}
	}

	// Read from file
	public String read(String path) {
		try {
			String data = "";
			File myObj = new File(path);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				data += myReader.nextLine();
				if (myReader.hasNextLine()) {
					data += "\n";
				}
			}
			myReader.close();
			return data;
		} catch (FileNotFoundException e) {
			System.out.println("Failed to read the file: " + path);
			e.printStackTrace();
			return "";
		}
	}

	// Write to file
	public void write(String path, String txt) {
		try {
			FileWriter myWriter = new FileWriter(path);
			myWriter.write(txt);
			myWriter.close();
			// System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			System.out.println("Failed to write to the file: " + path);
			e.printStackTrace();
		}
	}

	// Append to file
	public void append_auto(String path, String txt) {
		try {
			PrintWriter output = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
			output.println(txt);
			output.close();
			// System.out.println("Successfully appended to the file.");
		} catch (IOException e) {
			System.out.println("Failed to append to the file: " + path);
			e.printStackTrace();
		}
	}

	// Delete a file
	public void delete(String path) { 
		File myObj = new File(path); 
		if (myObj.delete()) { 
			System.out.println("Deleted the file: " + myObj.getName());
		} else {
			System.out.println("Failed to delete the file: " + path);
		} 
	}
}