package org.OpenGeoPortal.Ingest;

import java.util.UUID;

import org.OpenGeoPortal.Layer.Metadata;

public interface DirectToSolrSubmitter {
	public UUID runIngestJob(String sessionId, Metadata metadata);
}
