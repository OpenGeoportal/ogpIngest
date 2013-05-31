package org.OpenGeoPortal.Ingest;

import java.util.UUID;

public class WMSCapabilitiesIngestSubmitterImpl extends AbstractIngestJobSubmitter implements WMSCapabilitiesIngestSubmitter {

	public UUID runIngestJob(String sessionId, String institution, String wmsEndpoint) {
		UUID jobId = registerJob(sessionId);
		WMSCapabilitiesIngestJob ingestJob = (WMSCapabilitiesIngestJob) beanFactory.getBean("wmsCapabilitiesIngestJob");
		ingestJob.init(jobId, institution, wmsEndpoint);

		asyncTaskExecutor.execute(ingestJob);
		return jobId;
	}
}
