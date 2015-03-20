package org.OpenGeoPortal.Ingest;

import java.util.List;
import java.util.Map;

public interface IncrementalIngestStatus extends IngestStatus {

	void setSuccesses(List<Map<String,String>> successes);
	void setWarnings(List<Map<String,String>> warnings);
	void setErrors(List<Map<String,String>> errors);

	
	List<Map<String,String>> getUnreadSuccesses();
	List<Map<String,String>> getUnreadWarnings();
	List<Map<String,String>> getUnreadErrors();

	IngestStatus createUpdateStatus() throws Exception;
}
