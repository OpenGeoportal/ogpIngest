package org.OpenGeoPortal.Ingest.Metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.OpenGeoPortal.Ingest.IngestProperties;
import org.OpenGeoPortal.Layer.Metadata;
import org.OpenGeoPortal.Utilities.OgpLogger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class FlexibleMetadataConverter implements MetadataConverter {
	//how do I handle files or responses with multiple metadata records
	@OgpLogger
	Logger logger;
	MetadataParseMethodProvider metadataParseMethodProvider;
	IngestProperties ingestProperties;
	DocumentBuilder documentBuilder = null;

	public MetadataParseResponse parse(File metadataFile, String institution) throws Exception {
		if (!metadataFile.exists()){
			//throw or log error
			throw new IOException("File does not exist: " + metadataFile.getName());
		}
		if (!metadataFile.canRead()){
			Boolean isReadable = false;
			try {
				isReadable = metadataFile.setReadable(true);
			} catch (Exception e){
				throw new IOException("File is not readable: " + metadataFile.getName());
			}
			if (!isReadable){
				throw new IOException("File is not readable: " + metadataFile.getName());
			}
		}

		InputStream inputStream = new FileInputStream(metadataFile);
		MetadataParseResponse metadataParseResponse = handleMetadata(inputStream);
		Metadata metadata  = metadataParseResponse.metadata;
		metadata.setInstitution(institution);

		metadata.setWorkspaceName(ingestProperties.getWorkspace(metadata, institution));
		
		return metadataParseResponse;
	}
	
	public MetadataParseResponse parse(InputStream metadataStream) throws Exception{
		return parse (metadataStream, "");
	}

	public MetadataParseResponse parse(InputStream metadataStream, String institution) throws Exception{
			MetadataParseResponse metadataParseResponse = handleMetadata(metadataStream);
			Metadata metadata  = metadataParseResponse.metadata;
			metadata.setInstitution(institution);
			metadata.setWorkspaceName(ingestProperties.getWorkspace(metadata, institution));
			//store the original ows name here for the layer id, since we don't want all the prefixes
			metadata.setId(metadata.getOwsName());
			//should process the layer name here (ftname may or may not contain the appropriate prefix)
			if (metadata.getOwsName() != null && !metadata.getOwsName().isEmpty()){
				metadata.setOwsName(processOwsName(metadata, institution));
			}
			return metadataParseResponse;
	}
	
	private String processOwsName(Metadata metadata, String institution) throws IOException{
		//should process the layer name here (ftname may or may not contain the appropriate prefix)
			String layerNamePrefix = ingestProperties.getProperty(institution.toLowerCase() + ".layerPrefix");
			String layerNameCase = ingestProperties.getProperty(institution.toLowerCase() + ".layerNameCase");
			String owsName = metadata.getOwsName();
			
			if (layerNameCase.equalsIgnoreCase("uppercase")){
				owsName = owsName.toUpperCase();
			} else if (layerNameCase.equalsIgnoreCase("lowercase")){
				owsName = owsName.toLowerCase();
			} else if (layerNameCase.equalsIgnoreCase("mixed")){
				//leave the case as is
			} else {
				//leave the case as is
			}
			if (layerNamePrefix != null){
				if (!owsName.contains(layerNamePrefix)){
					owsName = layerNamePrefix + owsName;
				}
			} 
		
		return owsName;
	}
	
	private MetadataParseResponse handleMetadata(InputStream inputStream) throws Exception{

		MetadataParseResponse metadataParseResponse = null;
		try
		{
			metadataParseResponse = handleAsXml(inputStream);
		}
 
	    catch (SAXException e) 
	    {
	    	logger.error("SAX error with file");
			try {
				metadataParseResponse = handleAsJson(inputStream);
			} catch (JsonParseException e1) {
				//e1.printStackTrace();
			    logger.error("error parsing as json");
			    //couldn't handle metadata as xml or json
			    return null;				
			} catch (JsonMappingException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
			    logger.error("error parsing as json");
			    //couldn't handle metadata as xml or json
			    return null;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
			    logger.error("IO error");
			    //couldn't handle metadata as xml or json
			    return null;
			}
		} catch (Exception e) {
			//this should catch errors caused by unsupported xml metadata types
			//e.printStackTrace();
			String exceptionMessage = e.getMessage();
			if (exceptionMessage.isEmpty()){
				logger.error("unspecified error: returning null");
			} else {
				logger.error(exceptionMessage);
				throw new Exception(exceptionMessage);
			}
			return null;
		    //couldn't handle metadata as xml or json
		}
		
		return metadataParseResponse;

	}
	
	private MetadataParseResponse handleAsJson(InputStream inputStream) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper m = new ObjectMapper();
		// can either use mapper.readTree(JsonParser), or bind to JsonNode
		JsonNode rootNode = m.readTree(inputStream);
		//MetadataParser metadataParser = metadataParserProvider.getMetadataParser(rootNode);
		MetadataParseResponse metadata = null;//metadataParser.marshallMetadata(rootNode);
		return metadata; 
	}

	private synchronized DocumentBuilder getDocumentBuilder() throws ParserConfigurationException{
		if (this.documentBuilder == null){
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		
			documentBuilderFactory.setValidating(false);  // dtd isn't available; would be nice to attempt to validate
			documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			documentBuilderFactory.setNamespaceAware(true);
			this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} 
		return documentBuilder;
	}
	
	private MetadataParseResponse handleAsXml(InputStream inputStream) throws Exception {
		Document document = getDocumentBuilder().parse(inputStream);
		MetadataParseMethod metadataParser = metadataParseMethodProvider.getMetadataParseMethod(document);
		//try {
		MetadataParseResponse mpr = metadataParser.marshallMetadata(document);
		//} catch (Exception e){
			//logger.error("Error marshalling xml");
			//e.printStackTrace();
		//}
		return mpr;
	}

	public MetadataParseMethodProvider getMetadataParseMethodProvider() {
		return metadataParseMethodProvider;
	}

	public void setMetadataParseMethodProvider(
			MetadataParseMethodProvider metadataParseMethodProvider) {
		this.metadataParseMethodProvider = metadataParseMethodProvider;
	}

	public IngestProperties getIngestProperties() {
		return ingestProperties;
	}

	public void setIngestProperties(IngestProperties ingestProperties) {
		this.ingestProperties = ingestProperties;
	}
}
