package org.OpenGeoPortal.Ingest;

import java.util.UUID;

public interface RemoteSolrIngestJob extends Runnable{
	void run();
	void init(UUID jobId, String institution, String remoteSolrUrl);
}
