package org.OpenGeoPortal.Ingest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericIngestStatus implements IngestStatus {

	List<Map<String,String>> successMessage = new ArrayList<Map<String,String>>();
	List<Map<String,String>> errorMessage = new ArrayList<Map<String,String>>();
	List<Map<String,String>> warningMessage = new ArrayList<Map<String,String>>();
	Date timeStamp;
	IngestJobStatus ingestJobStatus;
	int progress = 0;
	private String returnValue;

	
	GenericIngestStatus(){
		//set a time stamp for the IngestStatus so we can do clean up later
		this.timeStamp = new Date();
		this.ingestJobStatus = IngestJobStatus.Processing;
	}
	

	public String getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}
	
	public Date getTimeStamp(){
		return this.timeStamp;
	}
	
	public void addError(String name, String message){
		errorMessage.add(statusMessage(name, message));
	}
	
	public void addSuccess(String name, String message){
		successMessage.add(statusMessage(name, message));
	}
	
	public void addWarning(String name, String message){
		warningMessage.add(statusMessage(name, message));
	}
	
	public List<Map<String,String>> getSuccesses(){
		return successMessage;
	}
	
	public List<Map<String,String>> getWarnings(){
		return warningMessage;
	}
	
	public List<Map<String,String>> getErrors(){
		return errorMessage;
	}
	
	private static Map<String,String> statusMessage(String layerName, String status){
		Map<String, String> statusMap = new HashMap<String, String>();
		statusMap.put("layer", layerName);
		statusMap.put("status", status);
		return statusMap;
	}

	public void setProgress(int current, int total){
	    progress = current / total * 100;	
	}
	
	public int getProgress() {
		return progress;
	}

	public void setJobStatus(IngestJobStatus ingestJobStatus){
		this.ingestJobStatus = ingestJobStatus;
	}
	
	public IngestJobStatus getJobStatus() {
		return ingestJobStatus;
	}
}
