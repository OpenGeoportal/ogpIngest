package org.OpenGeoPortal.Ingest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.OpenGeoPortal.Ingest.AbstractSolrIngest.MetadataElement;
import org.OpenGeoPortal.Ingest.IngestResponse.IngestInfo;
import org.OpenGeoPortal.Ingest.Metadata.MetadataConverter;
import org.OpenGeoPortal.Ingest.Metadata.MetadataParseResponse;
import org.OpenGeoPortal.Layer.Metadata;
import org.OpenGeoPortal.Utilities.ZipFilePackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicUploadMetadataJob implements UploadMetadataJob, Runnable {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private IngestStatusManager ingestStatusManager;
	private MetadataConverter metadataConverter;
	private MapServerIngest mapServerIngest;
	private SolrIngest solrIngest;
	private ExtraTasks extraTasks;
	private IngestProperties ingestProperties;
	private String institution;
	private String options;
	private List<File> fgdcFile;
	private UUID jobId;
	private IngestStatus ingestStatus;
	private Set<MetadataElement> requiredFields;
		
	public void init(UUID jobId, String institution, Set<MetadataElement> requiredFields, String options, List<File> fgdcFile){
			this.institution = institution;
			this.options = options;
			this.fgdcFile = fgdcFile;
			this.jobId = jobId;
			this.requiredFields = requiredFields;
	}
		
	public void setIngestStatusManager(IngestStatusManager ingestStatusManager) {
			this.ingestStatusManager = ingestStatusManager;
		}

		public void setMetadataConverter(MetadataConverter metadataConverter) {
			this.metadataConverter = metadataConverter;
		}

		public void setMapServerIngest(MapServerIngest mapServerIngest) {
			this.mapServerIngest = mapServerIngest;
		}

		public void setSolrIngest(SolrIngest solrIngest) {
			this.solrIngest = solrIngest;
		}

		public void setExtraTasks(ExtraTasks extraTasks) {
			this.extraTasks = extraTasks;
		}

		public void setIngestProperties(IngestProperties ingestProperties) {
			this.ingestProperties = ingestProperties;
		}

	public void uploadMetadata() throws IOException{
		ingestStatus = ingestStatusManager.getIngestStatus(jobId);
		int totalFileCount = fgdcFile.size();
		int fileCounter = 0;
		for (File file: fgdcFile){
			//decide what to do with the file(s)
			fileCounter++;
			ingestStatus.setProgress(fileCounter, totalFileCount);
			String fileName;
			synchronized (this){
				fileName = file.getName();
			}
			if (fileName.toLowerCase().endsWith(".xml")){
				//treat as xml metadata
				try{
					int errorCount = ingestStatus.getErrors().size();
					synchronized(this){
						ingestXmlMetadata(new FileInputStream(file), institution, options);
					}
					if (ingestStatus.getErrors().size() == errorCount){
						ingestStatus.addSuccess(fileName, "added");
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
					ingestStatus.addError(fileName, cause);
				}
			} else if (fileName.toLowerCase().endsWith(".zip")){
				//first unzip the contents
				
				Set<File> xmlFiles = new HashSet<File>();
				try {
					logger.info("Unzipping file '" + fileName + "'");
					xmlFiles.addAll(ZipFilePackager.unarchiveFiles(file));
				} catch (Exception e) {
					ingestStatus.addError(fileName, "Error unzipping: There is a problem with the file.");
				}
				int xmlCounter = 0;
				totalFileCount += xmlFiles.size() - 1;
				for (File xmlFile: xmlFiles){
					if (xmlFile.getName().toLowerCase().endsWith(".xml")&&(!xmlFile.getName().startsWith("."))){
						xmlCounter++;
						try{
							int errorCount = ingestStatus.getErrors().size();
							logger.debug("Processing layer " + xmlCounter + " out of " + totalFileCount);
							ingestStatus.setProgress(xmlCounter, totalFileCount);
							synchronized(this){
									ingestXmlMetadata(new FileInputStream(xmlFile), institution, options);
							}
							if (ingestStatus.getErrors().size() == errorCount){
								ingestStatus.addSuccess(xmlFile.getName(), "added");
							} 
						} catch (Exception e){
							e.printStackTrace();
							logger.error("Failed to ingest '" + xmlFile.getName() + "'");
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
							ingestStatus.addError(xmlFile.getName(), cause);
						}
					} else {
						logger.info("Ignoring file: " + xmlFile.getName());
						//errorMessage.add(statusMessage(xmlFile.getName(), "Filetype is unsupported."));
					}
				}
				
				if (xmlCounter == 0){
					logger.error("No XML files found in file '" + fileName +"'");
					ingestStatus.addError(fileName, "No XML files found in file");
				}
			} else {
				ingestStatus.addError(fileName, "Filetype for [" + fileName + "] is unsupported.");
			}
		}
		if (ingestStatus.getErrors().isEmpty()){
			ingestStatus.setJobStatus(IngestJobStatus.Succeeded);
		} else {
			ingestStatus.setJobStatus(IngestJobStatus.Finished);
		}
	}

	private String ingestXmlMetadata(InputStream fileInputStream, String institution, String options) throws Exception{
		logger.info("Trying to parse metadata...");
		MetadataParseResponse metadataParseResponse = null;
		try {
				metadataParseResponse = metadataConverter.parse(fileInputStream, institution);
		} catch (Exception e){
			throw new Exception(e.getMessage());
		}
		logger.info("Metadata Parsed...");

		Metadata metadata = metadataParseResponse.metadata;
		if (!metadataParseResponse.ingestErrors.isEmpty()){
			for (IngestInfo errorObj: metadataParseResponse.ingestErrors){
				ingestStatus.addError(metadata.getOwsName(), "Parse Error: " + errorObj.getField() + "&lt;" + errorObj.getNativeName() + "&gt;:" + errorObj.getError() + "-" + errorObj.getMessage());
			}
			logger.error("Parse Errors:" + metadataParseResponse.ingestErrors.size());
		}
		if (!metadataParseResponse.ingestWarnings.isEmpty()){
			for (IngestInfo errorObj: metadataParseResponse.ingestWarnings){
				ingestStatus.addWarning(metadata.getOwsName(), "Parse Warnings: " + errorObj.getField() + "&lt;" + errorObj.getNativeName() + "&gt;:" + errorObj.getError() + "-" + errorObj.getMessage());
			}
			logger.error("Parse Warnings:" + metadataParseResponse.ingestWarnings.size());
		}
		logger.info("Metadata parsed?: " + metadataParseResponse.metadataParsed);
		Boolean doMapServerIngest = false;
		Boolean doSolrIngest = true;
		String localInstitution = ingestProperties.getProperty("local.institution");
		if (metadata.getInstitution().equalsIgnoreCase(localInstitution)){	
			doMapServerIngest  = true;
		}
		
		if (options.equalsIgnoreCase("solrOnly")){
			doMapServerIngest = false;
		} else if (options.equalsIgnoreCase("geoServerOnly")){
			doSolrIngest = false;
		}
		
		if (doMapServerIngest){
		// first update geoserver; we don't want layers in the solr index if they aren't available to the user
			logger.info("Trying map server ingest...[" + metadata.getOwsName() + "]");
			String mapServerResponse = mapServerIngest.addLayerToMapServer(localInstitution, metadata);
			//should be able to get a bbox from geoserver at this point;
			//we can update the metadata object with this new value
	
			if (mapServerResponse.toLowerCase().contains("success")){
				// store in database
				String taskResponse;
				try { 
					taskResponse =  extraTasks.doTasks(metadata);
					//this way, each institution can have it's own additional ingest actions if they desire
				} catch (Exception e) {
		
				}
			} else {
				//there was an error; return mapServerResponse as an error
				logger.error(mapServerResponse);
				ingestStatus.addError(metadata.getOwsName(), "GeoServer Error:" + mapServerResponse);
				doSolrIngest = false;
			}
		}

		if (doSolrIngest) {
			logger.info("Trying Solr ingest...[" + metadata.getOwsName() + "]");	
			// and ingest into solr
			SolrIngestResponse solrIngestResponse = null;
			try {				
				solrIngestResponse = solrIngest.writeToSolr(metadata, this.requiredFields);
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
		} else return "";
	}

	
	public void run() {
		try{
			uploadMetadata();
		} catch (Exception e){
			logger.error("Error in uploadMetadata");
			ingestStatus.setJobStatus(IngestJobStatus.Failed);
		}finally {
			try {
				File parentDir = fgdcFile.get(0).getParentFile();
				for (File file : fgdcFile){
					file.delete();
				}
				parentDir.delete();
				
			} catch (Exception e){
				ingestStatus.addError("cleanup", "Failed to delete temp files");
			}
		}
	}
	
}
