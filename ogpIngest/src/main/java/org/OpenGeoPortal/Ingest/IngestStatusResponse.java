package org.OpenGeoPortal.Ingest;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IngestStatusResponse {
	
	List<Map<String,String>> getSuccesses();
	
	List<Map<String,String>> getWarnings();
	
	List<Map<String,String>> getErrors();

	int getProgress();

	IngestJobStatus getJobStatus();
	
	Date getTimeStamp();
	
	String getReturnValue();
}
