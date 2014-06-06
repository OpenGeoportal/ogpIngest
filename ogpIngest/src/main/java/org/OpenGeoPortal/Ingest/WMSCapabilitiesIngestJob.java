package org.OpenGeoPortal.Ingest;

import java.util.UUID;

public interface WMSCapabilitiesIngestJob extends Runnable {

	void init(UUID jobId, String institution, String wmsEndpoint);

}
