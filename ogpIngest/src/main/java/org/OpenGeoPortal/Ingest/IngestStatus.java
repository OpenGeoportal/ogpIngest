package org.OpenGeoPortal.Ingest;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IngestStatus {
	void addError(String name, String message);
	
	void addSuccess(String name, String message);
	
	void addWarning(String name, String message);
	
	List<Map<String,String>> getSuccesses();
	
	List<Map<String,String>> getWarnings();
	
	List<Map<String,String>> getErrors();

	int getProgress();

	IngestJobStatus getJobStatus();
	
	Date getTimeStamp();

	void setProgress(int current, int total);

	void setJobStatus(IngestJobStatus ingestJobStatus);

	void setReturnValue(String string);
	
	String getReturnValue();

}
