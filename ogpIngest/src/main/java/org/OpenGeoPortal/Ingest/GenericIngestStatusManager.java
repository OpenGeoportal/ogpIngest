package org.OpenGeoPortal.Ingest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericIngestStatusManager implements IngestStatusManager {
	List<GlobalIngestStatus> globalIngestStatus = new ArrayList<GlobalIngestStatus>();
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public synchronized IngestStatus getIngestStatus(UUID jobId){
		for (GlobalIngestStatus status: globalIngestStatus){
			if (status.getJobId().equals(jobId)){
				return status.getIngestStatus();
			}
		}
		return null;
	}

	private synchronized List<GlobalIngestStatus> getStatusBySessionId(String sessionId){
		List<GlobalIngestStatus> sessionStatus = new ArrayList<GlobalIngestStatus>();
		for (GlobalIngestStatus status: globalIngestStatus){
			if (status.getSessionId().equals(sessionId)){
				sessionStatus.add(status);
			}
		}
		return sessionStatus;
	}
	
	public synchronized void removeStatusBySessionId(String sessionId){
		List<GlobalIngestStatus> sessionStatus = getStatusBySessionId(sessionId);
		if (!sessionStatus.isEmpty()){
			globalIngestStatus.removeAll(sessionStatus);
		} else {
			logger.info("No status objects found for this session: " + sessionId);
		}
		
	}
	
	public synchronized void addIngestStatus(UUID jobId, String sessionId, IngestStatus ingestStatus){
		GlobalIngestStatus globalStatus = new GlobalIngestStatus();
		globalStatus.setJobId(jobId);
		globalStatus.setSessionId(sessionId);
		globalStatus.setIngestStatus(ingestStatus);
		globalIngestStatus.add(globalStatus);
	}
	
	class GlobalIngestStatus {
		private UUID jobId;
		private String sessionId;
		private IngestStatus ingestStatus;
		
		public UUID getJobId() {
			return jobId;
		}
		public void setJobId(UUID jobId) {
			this.jobId = jobId;
		}
		public String getSessionId() {
			return sessionId;
		}
		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}
		public IngestStatus getIngestStatus() {
			return ingestStatus;
		}
		public void setIngestStatus(IngestStatus ingestStatus) {
			this.ingestStatus = ingestStatus;
		}
	}
}
