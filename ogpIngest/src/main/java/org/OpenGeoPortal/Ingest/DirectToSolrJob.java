package org.OpenGeoPortal.Ingest;

import java.util.UUID;

import org.OpenGeoPortal.Layer.Metadata;

public interface DirectToSolrJob extends Runnable {
	void run();
	void init(UUID jobId, Metadata metadata);
}
