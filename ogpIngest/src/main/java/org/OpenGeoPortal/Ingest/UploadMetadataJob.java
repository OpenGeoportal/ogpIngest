package org.OpenGeoPortal.Ingest;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.OpenGeoPortal.Ingest.AbstractSolrIngest.MetadataElement;

public interface UploadMetadataJob extends Runnable {
	void run();
	void init(UUID jobId, String institution, Set<MetadataElement> requiredFields, String options, List<File> fgdcFile);
}
