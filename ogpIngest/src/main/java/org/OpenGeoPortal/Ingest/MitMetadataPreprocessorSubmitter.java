package org.OpenGeoPortal.Ingest;

import java.io.File;
import java.util.List;
import java.util.UUID;
 
import org.OpenGeoPortal.Layer.AccessLevel;

public class MitMetadataPreprocessorSubmitter extends AbstractIngestJobSubmitter implements MetadataPreprocessorSubmitter {

	public UUID runIngestJob(String sessionId, List<File> fgdcFiles, String accessConstraints, String useConstraints, AccessLevel access, File metadataDir) {
		UUID jobId = registerJob(sessionId);
		MetadataPreprocessorJob ingestJob = (MetadataPreprocessorJob) beanFactory.getBean("metadataPreprocessorJob.mit");
		ingestJob.init(jobId, fgdcFiles, accessConstraints, useConstraints, access, metadataDir);
		asyncTaskExecutor.execute(ingestJob);
		return jobId;
	}
}
