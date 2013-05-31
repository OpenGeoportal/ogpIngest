package org.OpenGeoPortal.Ingest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericIngestStatus implements IncrementalIngestStatus, Cloneable {

	protected List<Map<String,String>> successMessage = new ArrayList<Map<String,String>>();
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

	public synchronized GenericIngestStatus clone() throws CloneNotSupportedException{
		return (GenericIngestStatus) super.clone();
	}
	
	public IngestStatus createUpdateStatus() throws CloneNotSupportedException {
		GenericIngestStatus copy = this.clone();
		copy.setSuccesses(this.getUnreadSuccesses());
		copy.setErrors(this.getUnreadErrors());
		copy.setWarnings(this.getUnreadWarnings());
		return copy;
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
	
	public synchronized List<Map<String,String>> getSuccesses(){
		return successMessage;
	}
	
	public List<Map<String,String>> getUnreadSuccesses(){

		return getUnread(getSuccesses());
	}
	
	public static List<Map<String,String>> getUnread(List<Map<String,String>> statuses){
		List<Map<String,String>> unread = new ArrayList<Map<String,String>>();
		for (Map<String,String> status: statuses){
			if (status.get("read").equalsIgnoreCase("false")){
				status.put("read", "true");
				unread.add(status);
			}
		}
		return unread;
	}
	
	public synchronized List<Map<String,String>> getWarnings(){
		return warningMessage;
	}
	
	public List<Map<String,String>> getUnreadWarnings(){

		return getUnread(getWarnings());
	}
	
	public synchronized List<Map<String,String>> getErrors(){
		return errorMessage;
	}
	
	public List<Map<String,String>> getUnreadErrors(){

		return getUnread(getErrors());
	}
	
	private static Map<String,String> statusMessage(String layerName, String status){
		Map<String, String> statusMap = new HashMap<String, String>();
		statusMap.put("layer", layerName);
		statusMap.put("status", status);
		statusMap.put("read", "false");
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

	public void setSuccesses(List<Map<String, String>> successes) {
		this.successMessage = successes;
	}

	public void setWarnings(List<Map<String, String>> warnings) {
		this.warningMessage = warnings;
	}

	public void setErrors(List<Map<String, String>> errors) {
		this.errorMessage = errors;
	}
}
