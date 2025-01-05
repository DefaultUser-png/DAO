package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class FileWrapper {
	public File f;
	public String name;
	public String extension;
	public String parent;
	public long lastModified;
	public long size;
	public FileTime creationTime;
	public FileTime lastModifedTime;
	public FileTime lastAccessTime;

	public FileWrapper(File f) {
		this.f = f;
		this.name = getFileName(f.getName());
		this.extension = getFileExtension(f.getName(), true);
		this.lastModified = f.lastModified();
		this.size = f.length();

		this.creationTime = null;
		this.lastModifedTime = null;
		this.lastAccessTime = null;

		if(f.isDirectory()) return;

		try {
			BasicFileAttributes attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
			this.creationTime = attr.creationTime();
			this.lastModifedTime = attr.lastModifiedTime();
			this.lastAccessTime = attr.lastAccessTime();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// String separation
	public String getFileExtension(String filename, boolean dot) {
		if (filename == null)
			return null;
		int dotIndex = filename.lastIndexOf(".");
		if (dotIndex == -1) return "";

		String output = filename.substring(dotIndex + 1);
		if(dot) output = "." + output;
		return output;
	}
	public String getFileName(String filename) {
		if (filename == null)
			return null;
		int dotIndex = filename.lastIndexOf(".");
		if (dotIndex == -1) return "";

		return filename.substring(0, dotIndex);
	}

	// Restore timestamps
	public void updAttributes(File other) {
		f = other;
		name = getFileName(other.getName());
		extension = getFileExtension(other.getName(), true);

		if(creationTime == null) return;
		if(lastModifedTime == null) return;
		if(lastAccessTime == null) return;

		/* Renaming files in Java also updates its time values;
		 * This is prevented by copying and pasting time value
		 * from older files
		 */
		try {
			BasicFileAttributeView attributes = Files.getFileAttributeView(Paths.get(f.getAbsolutePath()), BasicFileAttributeView.class);
			attributes.setTimes(lastModifedTime, lastAccessTime, creationTime);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File findNextAvailable(String filename) {
		for (int i = 2; i < Integer.MAX_VALUE; i++) {
			String nameNew = "\\" + getFileName(filename) + " (" + i + ")" + getFileExtension(filename, true);
			File fileNew = new File(f.getParent() + nameNew);

			if (!fileNew.exists())
				return fileNew;
		}
		throw new IllegalStateException("How did we get there");
	}


	public boolean isFile() {
		return f.isFile();
	}
	public String getParent() {
		return f.getParent();
	}

	public String getName() {
		return name;
	}
	public String getExtension() {
		return extension;
	}
	public long getLastModified() {
		return lastModified;
	}
	public long getSize() {
		return size;
	}
	public FileTime getAttCreationTime() {
		return creationTime;
	}
}
