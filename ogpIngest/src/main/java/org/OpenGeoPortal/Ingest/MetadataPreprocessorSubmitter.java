package org.OpenGeoPortal.Ingest;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.OpenGeoPortal.Layer.AccessLevel;

public interface MetadataPreprocessorSubmitter {
	 UUID runIngestJob(String sessionId, List<File> fgdcFiles, String accessConstraints, String useConstraints, AccessLevel access, File metadataDir);
}
