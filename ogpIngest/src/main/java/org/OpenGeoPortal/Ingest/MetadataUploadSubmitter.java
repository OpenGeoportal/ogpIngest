package org.OpenGeoPortal.Ingest;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.OpenGeoPortal.Ingest.Metadata.MetadataElement;

public interface MetadataUploadSubmitter {

	UUID runIngestJob(String sessionId, String institution, Set<MetadataElement> requiredFields, String options,
			List<File> uploadedFiles);

}
