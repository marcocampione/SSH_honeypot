package filesystem;


/** Generic directory file information.
 * It may be either a {@link File file} or a {@link Directory directory}.
 * It is has a name, a reference to the directory that contains it, and other properties.
 */
public abstract class FileProperties {
	
	protected String name;
	protected Directory parent= null;

	
	public FileProperties(String name) {
		this.name= name;
	}

	public String getName() {
		return name;
	}
	
	protected void setParentDirectory(Directory d) {
		parent= d;
	}
	
	public Directory getParentDirectory() {
		return parent;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
