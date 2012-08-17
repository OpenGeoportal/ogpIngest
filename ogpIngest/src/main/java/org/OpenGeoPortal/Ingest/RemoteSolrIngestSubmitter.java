package org.OpenGeoPortal.Ingest;

import java.util.UUID;

public interface RemoteSolrIngestSubmitter {

	UUID runIngestJob(String sessionId, String institution, String solrUrl);

}
