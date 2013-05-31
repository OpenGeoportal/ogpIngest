package org.OpenGeoPortal.Ingest;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.OpenGeoPortal.Ingest.Metadata.MetadataElement;

public class BasicMetadataUploadSubmitter extends AbstractIngestJobSubmitter implements MetadataUploadSubmitter {

	public UUID runIngestJob(String sessionId, String institution, Set<MetadataElement> requiredFields, String options, List<File> fgdcFiles) {
		UUID jobId = registerJob(sessionId);
		UploadMetadataJob ingestJob = (UploadMetadataJob) beanFactory.getBean("uploadMetadataJob");
		ingestJob.init(jobId, institution, requiredFields, options, fgdcFiles);
		asyncTaskExecutor.execute(ingestJob);
	return jobId;
	}
}
