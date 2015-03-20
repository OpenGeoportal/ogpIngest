package org.OpenGeoPortal.Ingest;

import java.util.UUID;

public interface IngestStatusManager {
	IngestStatus getIngestStatus(UUID jobId);
	void addIngestStatus(UUID jobId, String sessionId, IngestStatus ingestStatus);
	void removeStatusBySessionId(String sessionId);
	IngestStatus getNewIngestStatus(UUID fromString) throws Exception;
}
