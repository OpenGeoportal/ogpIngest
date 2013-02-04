package org.OpenGeoPortal.Ingest;

import java.util.UUID;

import org.OpenGeoPortal.Layer.Metadata;

public class DirectToSolrSubmitterImpl extends AbstractIngestJobSubmitter implements DirectToSolrSubmitter {

	public UUID runIngestJob(String sessionId, Metadata metadata) {
		UUID jobId = registerJob(sessionId);
		DirectToSolrJob ingestJob = (DirectToSolrJob) beanFactory.getBean("directToSolrJob");
		ingestJob.init(jobId, metadata);
		asyncTaskExecutor.execute(ingestJob);
	return jobId;
	}
}
