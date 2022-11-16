package filesystem;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Directory extends FileProperties {
	
	protected static String ROOT= "ROOT";

	List<Directory> dirs= new ArrayList<>();
	List<File> files= new ArrayList<>();
	
	
	protected Directory(String name) {
		super(name);
	}
	
	public static Directory createRootDirectory() {
		return new Directory(ROOT);
	}
	
	public boolean isRoot() {
		return getParentDirectory()==null;
	}
	
	public String getPath() {
		if (isRoot()) return "/";
		if (parent.isRoot()) return '/'+name;
		return parent.getPath()+'/'+name;
	}
	
	public List<Directory> getDirectories() {
		return dirs;
	}

	public List<File> getFiles() {
		return files;
	}

	public Directory getDirectory(String name) {
		for (Directory d : dirs) if (d.getName().equals(name)) return d;
		return null;
	}

	public File getFile(String name) {
		for (File f : files) if (f.getName().equals(name)) return f;
		return null;
	}

	public File createFile(String name) throws IOException {
		if (name==null) throw new IOException("File name cannot be null");
		if (name.length()==0) throw new IOException("File name cannot be empty");
		File f= new File(name);
		addFile(f);
		return f;
	}

	protected void addFile(File f) throws IOException {
		if (getFile(f.getName())!=null) throw new IOException("File name already exists");
		files.add(f);
		f.setParentDirectory(this);
	}

	public Directory createDirectory(String name) throws IOException {
		if (name==null) throw new IOException("Directory name cannot be null");
		if (name.length()==0) throw new IOException("Directory name cannot be empty");
		Directory d= new Directory(name);
		addDirectory(d);
		return d;
	}

	protected void addDirectory(Directory d) throws IOException {
		if (getDirectory(d.getName())!=null) throw new IOException("Directory name already exists");
		dirs.add(d);
		d.setParentDirectory(this);
	}

	public void delFile(String name) throws IOException {
		File f= getFile(name);
		if (f==null) throw new IOException("File not found");
		files.remove(f);
		f.setParentDirectory(null);
	}

	public void delDirectory(String name) throws IOException {
		Directory d= getDirectory(name);
		if (d==null) throw new IOException("Directory not found");
		dirs.remove(d);
		d.setParentDirectory(null);
	}

}
