package org.OpenGeoPortal.Ingest;

public interface IngestStatus extends IngestStatusResponse {
	void addError(String name, String message);
	
	void addSuccess(String name, String message);
	
	void addWarning(String name, String message);

	void setProgress(int current, int total);

	void setJobStatus(IngestJobStatus ingestJobStatus);

	void setReturnValue(String string);
		
}
