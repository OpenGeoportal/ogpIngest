package org.OpenGeoPortal.Ingest;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.OpenGeoPortal.Layer.AccessLevel;

public interface MetadataPreprocessorJob extends Runnable{
	void run();
	void init(UUID jobId, List<File> fgdcFiles, String accessConstraints, String useConstraints, AccessLevel access, File metadataDir);
}
