package org.OpenGeoPortal.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;

public interface FilePackager {

	void addFilesToArchive(Set<File> filesToPackage, File zipArchive)
			throws FileNotFoundException;
	Set<File> unarchiveFiles(File zipArchive) throws FileNotFoundException;

}
