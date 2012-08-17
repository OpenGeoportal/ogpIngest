package org.OpenGeoPortal.Ingest;

import java.util.UUID;

public class BasicRemoteSolrIngestSubmitter extends AbstractIngestJobSubmitter implements RemoteSolrIngestSubmitter {

	public UUID runIngestJob(String sessionId, String institution, String remoteSolrUrl) {
		UUID jobId = registerJob(sessionId);
		RemoteSolrIngestJob ingestJob = (RemoteSolrIngestJob) beanFactory.getBean("remoteSolrIngestJob");
		ingestJob.init(jobId, institution, remoteSolrUrl);

		asyncTaskExecutor.execute(ingestJob);
		return jobId;
	}
}
