package ar;
import java.io.File;
import java.io.FilenameFilter;


public class TemporaryFilenameFilter implements FilenameFilter {
	
	private String filenameStarter;
	
	public TemporaryFilenameFilter(String filenameStarter) {
		this.filenameStarter = filenameStarter;
	}

	public boolean accept(File dir, String name) {
		return name.startsWith(filenameStarter) && name.endsWith(".mail");
	}

}
