package org.OpenGeoPortal.Ingest;

import java.util.UUID;

public interface LibraryRecordIngestSubmitter {
	UUID runIngestJob(String sessionId);
}
