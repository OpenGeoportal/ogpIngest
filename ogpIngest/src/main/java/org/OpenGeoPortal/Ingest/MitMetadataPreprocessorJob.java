package org.OpenGeoPortal.Ingest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.OpenGeoPortal.Ingest.AbstractSolrIngest.MetadataElement;
import org.OpenGeoPortal.Ingest.IngestResponse.IngestInfo;
import org.OpenGeoPortal.Ingest.Metadata.ContactInfo;
import org.OpenGeoPortal.Ingest.Metadata.MetadataConverter;
import org.OpenGeoPortal.Ingest.Metadata.MetadataParseResponse;
import org.OpenGeoPortal.Ingest.Metadata.Utilities;
import org.OpenGeoPortal.Layer.AccessLevel;
import org.OpenGeoPortal.Layer.Metadata;
import org.OpenGeoPortal.Utilities.ZipFilePackager;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class MitMetadataPreprocessorJob implements MetadataPreprocessorJob, Runnable {
	private IngestStatusManager ingestStatusManager;
	private String institution;
	private UUID jobId;
	private IngestStatus ingestStatus;
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	private List<File> fgdcFiles;
	private String accessConstraints;
	private String useConstraints;
	private AccessLevel access;
	private MetadataConverter metadataConverter;

	private SolrIngest solrIngest;
	private File metadataDir;
	private ContactInfo contactInfo;
	private IngestProperties ingestProperties;

	private void processMetadata() throws ParserConfigurationException, IOException {
		ingestStatus = ingestStatusManager.getIngestStatus(jobId);
		int totalFileCount = fgdcFiles.size();
		int fileCounter = 0;
		//set up objects to parse xml
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setValidating(false);  // dtd isn't available; would be nice to attempt to validate
		documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		institution = ingestProperties.getProperty("local.institution");
		contactInfo = getContactInfo();
		//is it bad to put this in the same directory containing the uploaded files? yes....we have to essentially give user
		//access to the temp dir then.

		File returnZipFile = File.createTempFile("metadata", ".zip", metadataDir);
		
		ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(returnZipFile);
		//iterate over uploaded files
		for (File file: fgdcFiles){
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
					File layerFile;
					synchronized(this){
						InputStream fileInputStream = new FileInputStream(file);
						//make the alterations to the XML document
						layerFile = processXmlMetadata(documentBuilder.parse(fileInputStream), access, accessConstraints, useConstraints, file.getName());
					}
					try {
						//try to parse the document for "auditing" purposes
						synchronized(this){
							auditXmlMetadata(new FileInputStream(layerFile), institution);
						}
					} catch (Exception e){
						e.printStackTrace();
						String cause = "";
						if (e.getCause() == null){
							cause = "Unspecified error";
						} else {
							cause = e.getCause().getClass().getName() + ":" + e.getMessage();
						}
						ingestStatus.addError(file.getName(), cause);
					}
					//write the result document to the zip archive
					ZipArchiveEntry entry = new ZipArchiveEntry(file.getName());
					entry.setSize(layerFile.length());
					zipOutput.putArchiveEntry(entry);
					zipOutput.write(IOUtils.toByteArray(new FileInputStream(layerFile)));
					zipOutput.closeArchiveEntry();

					if (ingestStatus.getErrors().size() == errorCount){
						ingestStatus.addSuccess(file.getName(), "added");
					}
				} catch (Exception e){
					e.printStackTrace();
					String cause = "";
					if (e.getCause() == null){
						cause = "Unspecified error";
					} else {
						cause = e.getCause().getClass().getName() + ":" + e.getMessage();
					}
					ingestStatus.addError(file.getName(), cause);
				}
			} else if (fileName.toLowerCase().endsWith(".zip")){
				
				Set<File> xmlFiles = new HashSet<File>();
				try {
					logger.info("Unzipping file '" + file.getName() + "'");
					//unzip the contents
					xmlFiles.addAll(ZipFilePackager.unarchiveFiles(file));
				} catch (Exception e) {
					ingestStatus.addError(file.getName(), "Error unzipping: There is a problem with the file.");
				}
				int xmlCounter = 0;
				totalFileCount += xmlFiles.size() - 1;
				for (File xmlFile: xmlFiles){
					//iterate over files in the uploaded zip archive, filter out files without xml extension
					if (xmlFile.getName().toLowerCase().endsWith(".xml")&&(!xmlFile.getName().startsWith("."))){
						xmlCounter++;
						try{
							int errorCount = ingestStatus.getErrors().size();
							logger.debug("Processing layer " + xmlCounter + " out of " + totalFileCount);
							ingestStatus.setProgress(xmlCounter, totalFileCount);
							File layerFile;
							synchronized(this){
								InputStream fileInputStream = new FileInputStream(xmlFile);
								//make adjustments to metadata
								layerFile = processXmlMetadata(documentBuilder.parse(fileInputStream), access, accessConstraints, useConstraints, file.getName());
							}
							try {
								synchronized(this){
									//try to parse the metadata for "auditing" purposes
									auditXmlMetadata(new FileInputStream(layerFile), institution);
								}
							} catch (Exception e){
								e.printStackTrace();
								logger.error("Failed to parse '" + xmlFile.getName() + "'");
								String cause = "";
								if (e.getCause() == null){
									cause = "Unspecified error";
								} else {
									cause = e.getCause().getClass().getName() + ":" + e.getMessage();
								}
								ingestStatus.addError(xmlFile.getName(), cause);
							}
							//write the result document to the return zip archive
							ZipArchiveEntry entry = new ZipArchiveEntry(xmlFile.getName());
							entry.setSize(layerFile.length());
							zipOutput.putArchiveEntry(entry);
							zipOutput.write(IOUtils.toByteArray(new FileInputStream(layerFile)));
							zipOutput.closeArchiveEntry();
							if (ingestStatus.getErrors().size() == errorCount){
								ingestStatus.addSuccess(xmlFile.getName(), "added");
							} 
						} catch (Exception e){
							e.printStackTrace();
							logger.error("Failed to audit '" + xmlFile.getName() + "'");
							String cause = "";
							if (e.getCause() == null){
								cause = "Unspecified error";
							} else {
								cause = e.getCause().getClass().getName() + ":" + e.getMessage();
							}
							ingestStatus.addError(xmlFile.getName(), cause);
						}
					} else {
						logger.info("Ignoring file: " + xmlFile.getName());
						ingestStatus.addError(xmlFile.getName(), "Filetype is unsupported.");
					}
				}
				
				if (xmlCounter == 0){
					logger.error("No XML files found in file '" + file.getName() +"'");
					ingestStatus.addError(file.getName(), "No XML files found in file");
				}
			} else {
				ingestStatus.addError(file.getName(), "Filetype [" + fileName + "] is unsupported.");
			}
		}
		zipOutput.finish();
		zipOutput.close();
		
		logger.debug(returnZipFile.getAbsolutePath());
		//write the location of the result zip file to the model
	   // model.addAttribute("downloadLink", "resources/metadata/" + returnZipFile.getName());
		ingestStatus.setReturnValue("resources/metadata/" + returnZipFile.getName());
		if (ingestStatus.getErrors().isEmpty()){
			ingestStatus.setJobStatus(IngestJobStatus.Succeeded);
		} else {
			ingestStatus.setJobStatus(IngestJobStatus.Finished);
		}
	}
	
	private ContactInfo getContactInfo() throws IOException{

		ContactInfo contactInfo = new ContactInfo();

		contactInfo.setAddress(ingestProperties.getProperty("local.address"));
		contactInfo.setCity(ingestProperties.getProperty("local.city"));
		contactInfo.setContactEmail(ingestProperties.getProperty("local.email"));
		contactInfo.setContactOrg(ingestProperties.getProperty("local.contactOrg"));
		contactInfo.setContactPerson(ingestProperties.getProperty("local.contactPerson"));
		contactInfo.setContactPhone(ingestProperties.getProperty("local.contactPhone"));
		contactInfo.setContactPosition(ingestProperties.getProperty("local.contactPosition"));
		contactInfo.setCountry(ingestProperties.getProperty("local.country"));
		contactInfo.setState(ingestProperties.getProperty("local.state"));
		contactInfo.setZip(ingestProperties.getProperty("local.zip"));

		return contactInfo;
	}
	
	public IngestStatusManager getIngestStatusManager() {
		return ingestStatusManager;
	}

	public void setIngestStatusManager(IngestStatusManager ingestStatusManager) {
		this.ingestStatusManager = ingestStatusManager;
	}
	
	public IngestProperties getIngestProperties() {
		return ingestProperties;
	}

	public void setIngestProperties(IngestProperties ingestProperties) {
		this.ingestProperties = ingestProperties;
	}
	
	public MetadataConverter getMetadataConverter() {
		return metadataConverter;
	}

	public void setMetadataConverter(MetadataConverter metadataConverter) {
		this.metadataConverter = metadataConverter;
	}
	
	public SolrIngest getSolrIngest() {
		return solrIngest;
	}

	public void setSolrIngest(SolrIngest solrIngest) {
		this.solrIngest = solrIngest;
	}

	public void run() {
		try{
			processMetadata();
		} catch (Exception e){
			logger.error("Error in preprocessMetadata");
			ingestStatus.setJobStatus(IngestJobStatus.Failed);
		} finally {
			try {
				File parentDir = fgdcFiles.get(0).getParentFile();
				for (File file : fgdcFiles){
					file.delete();
				}
				parentDir.delete();
				
			} catch (Exception e){
				ingestStatus.addError("cleanup", "Failed to delete temp files");
			}
		}
	}

	public void init(UUID jobId, List<File> fgdcFiles, String accessConstraints, String useConstraints, AccessLevel access, File metadataDir) {
		this.jobId = jobId;
		this.fgdcFiles = fgdcFiles;
		this.accessConstraints = accessConstraints;
		this.useConstraints = useConstraints;
		this.access = access;
		this.metadataDir = metadataDir;
	}
	
	/**
	 * method that makes desired alterations to the XML document
	 * 
	 * @param metadataDocument
	 * @param access
	 * @param accessConstraintsText
	 * @param constraintsText
	 * @param fileName
	 * @param errorMessage
	 * @return
	 * @throws Exception
	 */
	private File processXmlMetadata(Document metadataDocument, AccessLevel access, String accessConstraintsText, String constraintsText, String fileName) throws Exception{
		logger.info("Trying to adjust metadata...");
			//document
		try {
			metadataDocument = Utilities.setAccessInfo(metadataDocument, access, institution, accessConstraintsText, constraintsText);
		}catch(Exception e){
			logger.error("accessinfo: " + e.getMessage());
			ingestStatus.addError(fileName, "Adjust Error: Problem handling 'accessinfo': " + e.getMessage());
		}
		try{
			metadataDocument = Utilities.handleFtname(metadataDocument, fileName);
		}catch(Exception e){
			logger.error("ftname: " + e.getMessage());
			ingestStatus.addError(fileName, "Adjust Error: Problem handling 'ftname': " + e.getMessage());
		}
		try{
			metadataDocument = Utilities.setContactInfo(metadataDocument, contactInfo);
		}catch(Exception e){
			logger.error("contactinfo: " + e.getMessage());
			ingestStatus.addError(fileName, "Adjust Error: Problem handling 'contactinfo': " + e.getMessage());
		}
		try{
			metadataDocument = Utilities.handleOnlink(metadataDocument, ingestProperties.getProperty("local.onlink"));
		}catch(Exception e){
			logger.error("onlink: " + e.getMessage());
			ingestStatus.addError(fileName, "Adjust Error: Problem handling 'onlink': " + e.getMessage());
		}

		//write the result document to a temp file
		File returnFile = File.createTempFile("doc", ".xml");
		//write the edited document
		Source xmlSource = new DOMSource(metadataDocument);
    	// Prepare the output file
    	Result xmlResult = new StreamResult(returnFile);
    
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		
		transformer.transform(xmlSource, xmlResult);
		logger.debug(returnFile.getAbsolutePath());
		return returnFile;
	}
	
	/**
	 * parses the metadata document so we can check for errors and warnings
	 * 
	 * @param fileInputStream
	 * @param institution
	 * @param warningMessage
	 * @param errorMessage
	 * @return
	 * @throws Exception
	 */
	private String auditXmlMetadata(InputStream fileInputStream, String institution) throws Exception{
		logger.info("Trying to parse metadata...");
		MetadataParseResponse metadataParseResponse = null;
		try {
			synchronized(this){
				metadataParseResponse = metadataConverter.parse(fileInputStream, institution);
			}
		} catch (Exception e){
			e.printStackTrace();

			throw new Exception(e.getMessage());
		}
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

		String layerName = "No Layer Name";
		if (metadata.getOwsName() != null){
			layerName = metadata.getOwsName();
		}
		logger.info("Trying Solr audit...[" + layerName + "]");	
			// and ingest into solr
			SolrIngestResponse solrIngestResponse = null;
			try {
				//determines which MetadataElements are required for successful ingest.  If the element is not
				//required, it will still show a warning if there is a problem with the element value
				Set<MetadataElement> requiredElements = new HashSet<MetadataElement>();
				requiredElements.add(MetadataElement.Institution);
				requiredElements.add(MetadataElement.LayerId);
				requiredElements.add(MetadataElement.LayerName);
				//requiredElements.add(MetadataElement.ContentDate);
				
				solrIngestResponse = solrIngest.auditSolr(metadata, requiredElements);
			} catch (Exception e){ 
				ingestStatus.addError(layerName, "Solr Error: " + e.getMessage());
			}
			if (!solrIngestResponse.ingestErrors.isEmpty()){
				for (IngestInfo errorObj: solrIngestResponse.ingestErrors){
					ingestStatus.addError(layerName, "Solr Audit Error: " + errorObj.getField() + "&lt;" + errorObj.getNativeName() + "&gt;:" + errorObj.getError() + "-" + errorObj.getMessage());
				}
				logger.error("Solr Audit Errors:" + solrIngestResponse.ingestErrors.size());
			}
			if (!solrIngestResponse.ingestWarnings.isEmpty()){
				for (IngestInfo errorObj: solrIngestResponse.ingestWarnings){
					ingestStatus.addWarning(layerName, "Solr Audit Warnings: " + errorObj.getField() + "&lt;" + errorObj.getNativeName() + "&gt;:" + errorObj.getError() + "-" + errorObj.getMessage());
				}
				logger.error("Solr Audit Warnings:" + solrIngestResponse.ingestWarnings.size());
			}
			return solrIngestResponse.solrRecord.getLayerId();
	}
}
