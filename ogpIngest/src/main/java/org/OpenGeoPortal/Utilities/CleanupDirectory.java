package org.OpenGeoPortal.Utilities;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanupDirectory {
	final static Logger logger = LoggerFactory.getLogger(CleanupDirectory.class.getName());

	public static void cleanupDirectory(File downloadDirectory, long fileAgeMinutes){
		try {
			//convert to milliseconds
			long timeInterval = fileAgeMinutes * 60 * 1000;
			if (downloadDirectory.exists()){
				File[] downloadedFiles = downloadDirectory.listFiles();
				for (File downloadedFile : downloadedFiles) {
					long currentTime = System.currentTimeMillis();
					if (currentTime - downloadedFile.lastModified() > timeInterval){
						logger.info("deleting " + downloadedFile.getName());
						downloadedFile.delete();
					}
				}
			} else {
				logger.error("Download directory " + downloadDirectory.getName() + " does not exist.");
			}
		} catch (Exception e) {
			logger.warn("Attempt to delete old files was unsuccessful.");
		}
		
	}
}
