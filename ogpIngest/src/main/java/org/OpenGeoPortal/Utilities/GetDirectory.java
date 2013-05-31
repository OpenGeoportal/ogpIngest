package org.OpenGeoPortal.Utilities;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetDirectory implements DirectoryRetriever {
	private String relativePath;
	final Logger logger = LoggerFactory.getLogger(this.getClass());


	/**
	 * a method to set the relativePath property.  
	 * 
	 * @param relativePath  the path of the web app's root directory relative to the java working directory.
	 */
	public void setRelativePath(String relativePath){
		this.relativePath = relativePath;
	}
	
	/**
	 * a method to create a directory to put downloaded files into, if it doesn't already exist
	 * @throws IOException 
	 * 
	 */
	public File getDirectory(String directoryName) throws IOException{

		/*Resource resource = AppContext.getApplicationContext().getResource("download");
		File downloadDir = resource.getFile();
		*/
		//check permissions
		String directoryString = relativePath;
		directoryString += directoryName;
		//System.out.println(directoryString);
		File theDirectory = new File(directoryString);
				
		if (!theDirectory.exists()){
			theDirectory.mkdir();
		}
		
		if (!theDirectory.canRead() || !theDirectory.canWrite()){
			throw new IOException("Download directory is inaccessible.");
		} else {
			logger.debug(theDirectory.getAbsolutePath());
			return theDirectory;
		}
	}
	
}
