package org.OpenGeoPortal.Ingest;

import java.util.UUID;

public interface WMSCapabilitiesIngestSubmitter {

	UUID runIngestJob(String sessionId, String institution, String wmsEndpoint);

}
