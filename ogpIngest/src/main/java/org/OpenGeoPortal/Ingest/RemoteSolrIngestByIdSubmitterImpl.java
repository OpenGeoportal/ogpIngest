package org.OpenGeoPortal.Ingest;

import java.util.UUID;

public class RemoteSolrIngestByIdSubmitterImpl extends AbstractIngestJobSubmitter implements RemoteSolrByIdIngestSubmitter {

	public UUID runIngestJob(String sessionId, String[] layerIds, String remoteSolrUrl) {
		UUID jobId = registerJob(sessionId);
		RemoteSolrIngestByIdJob ingestJob = (RemoteSolrIngestByIdJob) beanFactory.getBean("remoteSolrIngestByIdJob");
		ingestJob.init(jobId, layerIds, remoteSolrUrl);

		asyncTaskExecutor.execute(ingestJob);
		return jobId;
	}
}
