package org.OpenGeoPortal.Ingest.Metadata;

import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.OpenGeoPortal.Layer.BoundingBox;
import org.OpenGeoPortal.Utilities.OgpLogger;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is the foundation of the FGDC and ISO parsers.
 * It provides most of the functionality needed to process a metadata file and output a Solr ingestable file.
 *  
 * @author smcdon08, chrissbarnett
 *
 */
public abstract class AbstractXmlMetadataParseMethod {
	
	// variables to maintain some state about the parsing
	public int numberOfParseWarnings = 0;
	public Vector<String> missingParseTags = new Vector<String>();
	public Document document;
	public MetadataParseResponse metadataParseResponse;
	@OgpLogger
	public Logger logger;
	/**
	 * the keys to the state hash that the Solr file is generated from
	 */
	public static enum FieldType {Single, Multiple, Attribute};
	public interface Tag{
			String getTagName();
			FieldType getFieldType();
		};
				
		public MetadataParseResponse marshallMetadata(Document document){
			if (document == null){
				logger.error("document is null");
			}
			this.document = document;
			this.metadataParseResponse = new MetadataParseResponse();
			handleTitle();
			handleAbstract();
			handleLayerName();
			handlePublisher();
			handleOriginator();
			handleBounds();
			handleKeywords();
			handleAccess();
			handleDataType();
			handleFullText();
			handleDate();
			metadataParseResponse.metadataParsed = true;
			return metadataParseResponse;
		}

	abstract void handleOriginator();

	abstract void handlePublisher();

	abstract void handleLayerName();
	
	abstract void handleAbstract();

	abstract void handleTitle();

	abstract void handleDate();

	abstract void handleDataType();

	abstract void handleAccess();

	abstract void handleKeywords(); 

	abstract void handleBounds();
	
	abstract void handleFullText();

	public String getDocumentValue(Tag tag) throws Exception {
		if (tag.getFieldType() == FieldType.Single){
			return getDocumentValue(tag.getTagName());
		} else if (tag.getFieldType() == FieldType.Multiple){
			return getDocumentValues(tag.getTagName());
		} else {
			return null;
		}
	}
	/**
	 * concatenate the values for all occurrences for the passed tag
	 * @param document
	 * @param tagName
	 * @return
	 * @throws Exception 
	 */
	protected String getDocumentValues(String tagName) throws Exception
	{
		NodeList nodes = document.getElementsByTagName(tagName);
		logger.debug(" tagName = " + tagName + " nodes length = " + nodes.getLength());
		if (nodes.getLength() == 0)
		{
			throw new NullPointerException();
		}
		String tagValues = "";
		for (int i = 0 ; i < nodes.getLength() ; i++)
		{
			Node currentNode = nodes.item(i);
			NodeList tempNodes = currentNode.getChildNodes();
			if (tempNodes != null)
			{
				Node tempNode = tempNodes.item(0);
				if (tempNode != null)
				{
					String currentValue = tempNodes.item(0).getNodeValue();
					tagValues = tagValues + " " + currentValue;
				}
			}
		}
		try {
			return getValidValue(tagValues);
		} catch (Exception e){
			numberOfParseWarnings++;
			missingParseTags.add(tagName);
			throw new Exception("Invalid tag value");
		}
	}
	
	/**
	 * return the first value for the passed tag
	 * @param tagName
	 * @return
	 * @throws Exception 
	 */
	protected String getDocumentValue(String tagName) throws Exception
	{
		String tagValue = "";
		NodeList nodes = document.getElementsByTagName(tagName);
		logger.debug(" tagName = " + tagName + " nodes length = " + nodes.getLength());
		if (nodes.getLength() == 0){
			tagValue = null;
		} else {
			outerloop:
			for (int i = 0; i < nodes.getLength(); i++){
				Node currentNode = nodes.item(i);
				short nodeType = currentNode.getNodeType();
				if (nodeType == Node.TEXT_NODE){
					tagValue = currentNode.getNodeValue();
					//logger.info("<" + tagName + ">:" + tagValue);
					break;//found a text node, so exit
				}
				if (currentNode.hasChildNodes()){
					NodeList children = currentNode.getChildNodes();
					for (int j = 0; j < children.getLength(); j++){
						Node currentChild = children.item(j);
						short childNodeType = currentChild.getNodeType();
						if (childNodeType == Node.TEXT_NODE){
							tagValue = currentChild.getNodeValue();
							//logger.info("<" + tagName + ">:" + tagValue);
							break outerloop;//found a text node, so exit
						}
					}
				} 
			}
		}
			
		if ((tagValue == null) || (tagValue.length() == 0)){
			logger.error("No tag value found [" + tagName + "]");		
		}
	
		try {
			return getValidValue(tagValue);
		} catch (Exception e){
			numberOfParseWarnings++;
			missingParseTags.add(tagName);
			throw new Exception(e.getMessage());
		}
	}
	
	/**
	 * take care of special characters which would otherwise cause a problem in the Solr formatted file
	 * they are simply eliminated, should they instead be quoted?
	 * @param String temp
	 * @return String
	 */
	static public String cleanValue(String temp)
	{
		if (temp == null) return "";
		temp = temp.replace('&', ' ');
		return temp;
	}
	
	/** is the passed value a real value */
	protected String getValidValue(String value) throws Exception
	{
		if (value == null)
			throw new Exception("Null value");
		if (value.startsWith("REQUIRED"))
			throw new Exception("Invalid value: REQUIRED...");
		if (value.trim().length() == 0)
			throw new Exception("Zero length value");
		return cleanValue(value);
	}

	@SuppressWarnings("deprecation")
	protected String processDateString(String passedDate) throws ParseException
	{
		//can't do anything if there's no value passed
		if ((passedDate == null)||(passedDate.equalsIgnoreCase("unknown"))){
			return "";
		}
		List<String> formatsList = new ArrayList<String>();
		//add likely formats in order of likelihood

		formatsList.add("yyyyMMdd");
		formatsList.add("yyyyMM");
		formatsList.add("MM/yyyy");
		formatsList.add("MM/dd/yyyy");
		formatsList.add("MM/dd/yy");
		formatsList.add("MM-dd-yyyy");
		formatsList.add("MMMM yyyy");
		formatsList.add("MMM yyyy");
		formatsList.add("dd MMMM yyyy");
		formatsList.add("dd MMM yyyy");
		formatsList.add("yyyy");


		String[] parsePatterns = formatsList.toArray(new String[formatsList.size()]);
		String returnYear = null;

		passedDate = passedDate.trim();
		Date date = DateUtils.parseDate(passedDate, parsePatterns);
		logger.debug("Document date: " + passedDate + ", Parsed date: " + Integer.toString(date.getYear() + 1900));
		returnYear = Integer.toString(date.getYear() + 1900);

		return returnYear;
	}
	/**
	 * read the file into a string, escape it, and return it
	 * used to add source data to Solr record
	 * @param inputStream
	 * @param layerValues
	 */
	String getFullText()
	{
		try 
		{
			/*
			 * 
			 * Node pi = xmldoc.createProcessingInstruction
         ("xml-stylesheet", "type=\"text/xsl\" href=\"howto.xsl\"");
      xmldoc.insertBefore(pi, root);
			 */
			//get rid of stylesheet declarations
			//document.removeChild(document.getElementsByTagName("xml-stylesheet").item(0));
			Source xmlSource = new DOMSource(document);
			StringWriter stringWriter = new StringWriter();
			StreamResult streamResult = new StreamResult(stringWriter);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        //transformer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");//does this actually translate the text?
			transformer.transform(xmlSource, streamResult);
			String fileContents = stringWriter.toString();

			//I think the solrJ api takes care of the proper escaping
			//filter troublesome characters; does this fail without replacing non-ascii chars?
			//fileContents = fileContents.replaceAll("[^\\p{ASCII}]", "");
			//String escapedFileContents = StringEscapeUtils.escapeJavaScript(fileContents);
			return fileContents;
			
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			logger.error("transformer configuration error");
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			logger.error("transformer error");

			e.printStackTrace();
		} catch (Exception e){
			logger.error("Problem processing full text: " + e.getMessage());
		}
		return null;
	}
	
	protected Boolean validateBounds(String minX, String minY, String maxX, String maxY){
		BoundingBox bounds = new BoundingBox(minX, minY, maxX, maxY);
		if (bounds.isValid()){
			return true;
		} else {
			return false;
		}
	}
}
