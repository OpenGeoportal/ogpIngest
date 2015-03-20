package org.OpenGeoPortal.Ingest;

import java.util.UUID;

public interface RemoteSolrIngestByIdJob extends Runnable{
	void run();
	void init(UUID jobId, String[] layerIds, String remoteSolrUrl);
}
