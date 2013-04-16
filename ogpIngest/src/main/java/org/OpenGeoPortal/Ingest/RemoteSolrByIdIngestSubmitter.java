package org.OpenGeoPortal.Ingest;

import java.util.UUID;

public interface RemoteSolrByIdIngestSubmitter {

	UUID runIngestJob(String sessionId, String[] layerIds, String solrUrl);

}
