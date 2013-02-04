package org.OpenGeoPortal.Ingest;

import java.io.IOException;
import java.util.UUID;

import org.OpenGeoPortal.Ingest.IngestResponse.IngestInfo;
import org.OpenGeoPortal.Layer.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectToSolrJobImpl implements DirectToSolrJob, Runnable {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private IngestStatusManager ingestStatusManager;
	private SolrIngest solrIngest;
	private UUID jobId;
	private Metadata metadata;
	private IngestStatus ingestStatus;
		
	public void init(UUID jobId, Metadata metadata){
			this.jobId = jobId;
			this.metadata = metadata;
	}
		
	public void setIngestStatusManager(IngestStatusManager ingestStatusManager) {
			this.ingestStatusManager = ingestStatusManager;
		}

		public void setSolrIngest(SolrIngest solrIngest) {
			this.solrIngest = solrIngest;
		}


	public void uploadMetadata() throws IOException{
		ingestStatus = ingestStatusManager.getIngestStatus(jobId);

				try{
					int errorCount = ingestStatus.getErrors().size();
					synchronized(this){
						ingestMetadata(metadata);
					}
					if (ingestStatus.getErrors().size() == errorCount){
						ingestStatus.addSuccess(metadata.getOwsName(), "added");
					}
				} catch (Exception e){
					//e.printStackTrace();
					String cause = "";
					if (e.getCause() == null){
						if (e.getMessage() == null){
							cause = "Unspecified error";
						} else {
							cause = e.getMessage();
						}
					} else {
						cause = e.getCause().getClass().getName() + ":" + e.getMessage();
					}
					ingestStatus.addError(metadata.getOwsName(), cause);
				}
		
		if (ingestStatus.getErrors().isEmpty()){
			ingestStatus.setJobStatus(IngestJobStatus.Succeeded);
		} else {
			ingestStatus.setJobStatus(IngestJobStatus.Finished);
		}
	}

	private String ingestMetadata(Metadata metadata) throws Exception{		
			logger.info("Trying Solr ingest...[" + metadata.getOwsName() + "]");	
			// and ingest into solr
			SolrIngestResponse solrIngestResponse = null;
			try {				
				solrIngestResponse = solrIngest.writeToSolr(metadata);
			} catch (Exception e){ 
				ingestStatus.addError(metadata.getOwsName(), "Solr Error: " + e.getMessage());
			}
			if (!solrIngestResponse.ingestErrors.isEmpty()){
				for (IngestInfo errorObj: solrIngestResponse.ingestErrors){
					ingestStatus.addError(metadata.getOwsName(), "Solr Ingest Error: " + errorObj.getField() + "&lt;" + errorObj.getNativeName() + "&gt;:" + errorObj.getError() + "-" + errorObj.getMessage());
				}
				logger.error("Solr Ingest Errors:" + solrIngestResponse.ingestErrors.size());
			}
			if (!solrIngestResponse.ingestWarnings.isEmpty()){
				for (IngestInfo errorObj: solrIngestResponse.ingestWarnings){
					ingestStatus.addWarning(metadata.getOwsName(), "Solr Ingest Warnings: " + errorObj.getField() + "&lt;" + errorObj.getNativeName() + "&gt;:" + errorObj.getError() + "-" + errorObj.getMessage());
				}
				logger.warn("Solr Ingest Warnings:" + solrIngestResponse.ingestWarnings.size());
			}
			
			return solrIngestResponse.solrRecord.getLayerId();

	}

	
	public void run() {
		try{
			uploadMetadata();
		} catch (Exception e){
			logger.error("Error in uploadMetadata");
			ingestStatus.setJobStatus(IngestJobStatus.Failed);
		}finally {

		}
	}
	
}
