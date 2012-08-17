package org.OpenGeoPortal.Ingest;

import java.util.UUID;

public class BartonIngestSubmitter extends AbstractIngestJobSubmitter implements LibraryRecordIngestSubmitter {

	public UUID runIngestJob(String sessionId) {
		UUID jobId = registerJob(sessionId);
		LibraryRecordIngestJob ingestJob = (LibraryRecordIngestJob) beanFactory.getBean("bartonIngestJob");
		ingestJob.init(jobId);

		asyncTaskExecutor.execute(ingestJob);
		return jobId;
	}
}
