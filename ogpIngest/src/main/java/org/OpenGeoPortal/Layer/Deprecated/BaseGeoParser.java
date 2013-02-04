package org.OpenGeoPortal.Layer.Deprecated;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.OpenGeoPortal.Ingest.Metadata.FgdcParseMethod;
import org.OpenGeoPortal.Ingest.Metadata.Iso19139ParseMethod;
import org.apache.commons.fileupload.FileItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is the foundation of the FGDC and ISO parsers.
 * It provides most of the functionality needed to process a metadata file and output a Solr ingestable file.
 *  
 * @author smcdon08
 *
 */
public abstract class BaseGeoParser 
{
	
	// variables to maintain some state about the parsing
	public int numberOfParseWarnings = 0;
	public Vector<String> missingParseTags = new Vector<String>();
	
	
	/**
	 * the keys to the state hash that the Solr file is generated from
	 */
	public static enum Key{LayerId, Title, EsriName, Abstract, WestBc, EastBc, NorthBc, SouthBc, DataType, Publisher, Access,
		Originator, ThemeKeywords, PlaceKeywords, ContentDate, FgdcText, WorkspaceName, SrsProjectionCode};
		
	public static enum Access {Public, Restricted};
	
	String inputFilename;
	protected FileItem inputFileItem = null;
	
	public BaseGeoParser(String passedInputFilename)
	{
		inputFilename = passedInputFilename;
	}
	
	public BaseGeoParser(FileItem passedInputFileItem)
	{
		inputFileItem = passedInputFileItem;
	}
	
	/**
	 * could be replace by dependency injection
	 * @param formatType
	 * @param fgdcReference
	 * @return
	 */
	
	public static BaseGeoParser newInstance(String formatType, Object fgdcReference)
	{
		System.out.println("formatType = " + formatType + " and reference = " + fgdcReference + ", " + fgdcReference.getClass());
		if (formatType.equalsIgnoreCase("FGDC"))
		{
			/*if (fgdcReference instanceof String)
				return new FgdcParseMethod((String)fgdcReference);
			else if (fgdcReference instanceof FileItem)
				return new FgdcParseMethod((FileItem)fgdcReference);
		}
		else if (formatType.equalsIgnoreCase("ISO"))
		{
			System.out.println("found ISO");
			if (fgdcReference instanceof String)
				return new IsoParseMethod((String)fgdcReference);
			else if (fgdcReference instanceof FileItem)
				return new IsoParseMethod((FileItem)fgdcReference);*/
		}
		System.out.println(" newInstance returning null!");
		return null;
	}
	
	
	/**
	 * take care of special characters which would otherwise cause a problem in the Solr formatted file
	 * they are simply eliminated, should they instead be quoted?
	 * @param layerInfo
	 * @param key
	 * @return
	 */
	static public String getValue(Hashtable layerInfo, Key key)
	{
		String temp = (String)layerInfo.get(key);
		if (temp == null) return "";
		temp = temp.replace('&', ' ');
		return temp;
	}
	
	
	public Hashtable<Key, Object> readFile()
	{
		InputStream inputStream = null;
		try
		{
			inputStream = getInputStream();
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setValidating(false);  // dtd isn't available
			documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder documentBuilder;
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			
			//File inputFile = new File(inputStream);	
			Document document = documentBuilder.parse(inputStream);
			Hashtable<Key, Object> layerValues = processDocument(document);
			System.out.println("  processDocument returned " + layerValues);
			return layerValues;
		}
		catch (ParserConfigurationException e) 	
	    {
	    	System.out.println("Parser configuration error with file: " + inputStream);
	    	e.printStackTrace();
	    }
	    catch (IOException e)
	    {
	    	System.out.println("IO error with file: " + inputStream);
	    	e.printStackTrace();
	    } 
	    catch (SAXException e) 
	    {
	    	System.out.println("SAX error with file: " + inputStream);
			e.printStackTrace();
		}
	    System.out.println("in readFile, returning null");
	    return null;
	}
	
	public InputStream getInputStream()
	{
		InputStream returnValue = null;
		try
		{
			if (inputFileItem != null)
				return inputFileItem.getInputStream();
			if (inputFilename != null)
				return new FileInputStream(inputFilename);
		}
		catch (IOException e)
		{
			System.out.println("error in BaseGeoParser.open " + e);
		}
		System.out.println("something wrong in BaseGeoParser.getInputStream, inputFilename = " + inputFilename + ", inputFileItem = " + inputFileItem);
		return returnValue;
	}

	

	/**
	 * workspace name is a function of institution, use initialized value
	 * @param layerValues
	 */
	public void handleWorkspaceName(Hashtable<Key, Object> layerValues, String workspaceName)
	{
		layerValues.put(Key.WorkspaceName, workspaceName);
	}
	
	/**
	 * it isn't yet clear how to compute the projection, can it be done from the FGDC file?
	 * @param layerValues
	 */
	public void handleProjection(Hashtable<Key, Object> layerValues, String srsProjectionCode)
	{ 
		layerValues.put(Key.SrsProjectionCode, srsProjectionCode);
	}
	

	public abstract String keyToTag(Key key);
	/**
	 * create a data structure to hold information about this layer
	 * walk the DOM, pulling out the needed information
	 *   and saving it in a hashtable
	 * @param document
	 * @return
	 */
	public Hashtable<Key, Object> processDocument(Document document)
	{
		Hashtable<Key, Object> returnValue = new Hashtable<Key, Object>();
		addToHashtable(document, returnValue, keyToTag(Key.Title), Key.Title);
		addToHashtable(document, returnValue, keyToTag(Key.Abstract), Key.Abstract);
		addToHashtable(document, returnValue, keyToTag(Key.WestBc), Key.WestBc);
		addToHashtable(document, returnValue, keyToTag(Key.EastBc), Key.EastBc);
		addToHashtable(document, returnValue, keyToTag(Key.NorthBc), Key.NorthBc);
		addToHashtable(document, returnValue, keyToTag(Key.SouthBc), Key.SouthBc);
		addToHashtable(document, returnValue, keyToTag(Key.EsriName), Key.EsriName);
		addToHashtable(document, returnValue, keyToTag(Key.Publisher), Key.Publisher);
		addToHashtable(document, returnValue, keyToTag(Key.Originator), Key.Originator);
		addToHashtable(document, returnValue, keyToTag(Key.ThemeKeywords), Key.ThemeKeywords);
		addToHashtable(document, returnValue, keyToTag(Key.PlaceKeywords), Key.PlaceKeywords);
		
		handleAccess(document, returnValue);  // public or restricted
		
		//String publisherValue = getPublisherValue(document);
		//returnValue.put(Key.Publisher, publisherValue);
		String dateValue = getDateValue(document);
		returnValue.put(Key.ContentDate, dateValue);
		
		checkIsoThemes(BaseGeoParser.getValue(returnValue, Key.ThemeKeywords));
		checkLocationThemes(document);
		handleDataType(document, returnValue);
		
		return returnValue;
	}

	/**
	 * look at the document field associated with Key.Access (for FGDC: accconst)
	 * if the field starts with Restricted, the layer is restricted.  Otherwise it is public. 
	 * @param document
	 * @param layerValues
	 */
	protected void handleAccess(Document document, Hashtable<Key, Object> layerValues)
	{
		String tagName = keyToTag(Key.Access);
		String accessValue$ = getDocumentValue(document, tagName);
		Access accessValue = Access.Public;
		if (accessValue$ != null)
		{
			accessValue$ = accessValue$.toLowerCase();
			if (accessValue$.startsWith("restricted"))
				accessValue = Access.Restricted;
		}
		layerValues.put(Key.Access, accessValue);
	}
	
	
	/**
	 * get the values associated with xml tag tagName and store them in the hash with the key key
	 * 
	 * @param document
	 * @param layerValues
	 * @param tagName
	 * @param key
	 */
	protected void addToHashtable(Document document, Hashtable<Key, Object> layerValues, String tagName, Key key)
	{
		NodeList nodes = document.getElementsByTagName(tagName);
		if (nodes.getLength() == 0)
		{
			numberOfParseWarnings++;
			missingParseTags.add(tagName);
			return;
		}
		if ((key == Key.PlaceKeywords) || (key == Key.ThemeKeywords))
		{
			// keys with multiple values must be handled differently
			// should we use a multiple value field in the schema or simply concatenate together a string
			//  for simplicity, we concatenate strings
			String keyValue = getDocumentValues(document, tagName); 
			layerValues.put(key, keyValue.trim());
			if (keyValue.length() == 0)
			{
				numberOfParseWarnings++;
				missingParseTags.add(tagName);
			}
		}
		else
		{
			// for most tags, we get the value from the first tag
			//Node node = nodes.item(0);
			String value = getDocumentValue(document, tagName); 
			if (validValue(value))
				layerValues.put(key,value);
			else
			{
				numberOfParseWarnings++;
				missingParseTags.add(tagName);
			}
		}
	}
	
	/** is the passed value a real value */
	protected boolean validValue(String value)
	{
		if (value == null)
			return false;
		if (value.startsWith("REQUIRED"))
			return false;
		if (value.trim().length() == 0)
			return false;
		return true;
	}
	
	/**
	 * document should have ISO and LCSH/FGDC themes defined
	 * if not, added to the list of missing tags
	 * @param document
	 */
	protected void checkLocationThemes(Document document)
	{
		String documentThemes = getDocumentValue(document, "themekt");
		if (documentThemes == null)
			missingParseTags.add("LCSH/FGDC theme");
		else if (documentThemes.contains("FGDC") == false)
			missingParseTags.add("LCSH/FGDC theme");
	}
	
	
	
	protected void checkIsoThemes(String documentThemes)
	{
		String[] isoThemeKeywords = {"farming", "biota", "boundaries", "climatologyMeteorologyAtmosphere", "economy",
				"elevation", "environment", "geoscientificInformation", "health", "imageryBaseMapsEarthCover", 
				"intelligenceMilitary", "inlandWaters", "location", "oceans", "planningCadastre", "society", 
				"structure", "transportation", "utilitiesCommunication"};
		System.out.println("in BaseGeoParser.checkIsoThemes, isoThemeKeywords = " + isoThemeKeywords.toString());
		for (String currentIsoThemeKeyword : isoThemeKeywords)
		{
			if (documentThemes.contains(currentIsoThemeKeyword))
				return;
		}
		missingParseTags.add("ISO theme");
	}
	
	/**
	 * computing the data type for the layer involves looking at multiple fields in the document
	 * @param document
	 * @param layerValues
	 */
	protected void handleDataType(Document document, Hashtable<Key, Object> layerValues)
	{
		String direct = getDocumentValue(document, "direct");
		String sdtsType = getDocumentValue(document, "sdtstype");
		String srcCiteA = getDocumentValue(document, "srccitea");

		String solrType = "Raster";
		
		if ((direct == null) || (direct.equalsIgnoreCase("raster") == false))
		{
			// here if we don't have a raster, must check another tag
			if (sdtsType != null)
			{
				if (sdtsType.equals("G-polygon") || sdtsType.contains("olygon") || sdtsType.contains("chain"))
					solrType = "Polygon";
				else if (sdtsType.equals("Composite") || sdtsType.contains("omposite") || sdtsType.equals("Entity point"))
					solrType = "Point";
				else if (sdtsType.equals("String"))
					solrType = "Line";
				else
					solrType = "Undefined";
			}
		}
		if (srcCiteA != null)
		{
			if (srcCiteA.equalsIgnoreCase("Paper Map"))
				solrType = "Paper Map";
		}
		
		layerValues.put(Key.DataType, solrType);
	}
	
	/**
	 * concatenate the values for all occurrences for the passed tag
	 * @param document
	 * @param tagName
	 * @return
	 */
	protected String getDocumentValues(Document document, String tagName)
	{
		NodeList nodes = document.getElementsByTagName(tagName);
		System.out.println(" tagName = " + tagName + " nodes length = " + nodes.getLength());
		if (nodes.getLength() == 0)
		{
			return null;
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
					//System.out.println("    " + currentValue);
					tagValues = tagValues + " " + currentValue;
				}
			}
		}
		return tagValues;

	}
	
	/**
	 * return the first value for the passed tag
	 * @param document
	 * @param tagName
	 * @return
	 */
	protected String getDocumentValue(Document document, String tagName)
	{
		NodeList nodes = document.getElementsByTagName(tagName);
		if (nodes.getLength() == 0)
		{
			return null;
		}
		Node node = nodes.item(0);
		if (node == null)
			return null;  
		NodeList children = node.getChildNodes();
		if (children == null)
			return null;
		Node firstGrandChild = children.item(0);
		if (firstGrandChild == null)
			return null;
		String tagValue = firstGrandChild.getNodeValue();
		if (tagValue != null)
			tagValue = tagValue.trim();
		if (tagValue.length() == 0)
		{
			// if we having found text on a node yet, just grab all the text below top node
			tagValue = node.getTextContent().trim();
		}
		return tagValue;
	}
	
	
	/**
	 * check two different tags for the publisher
	 * @param document
	 * @return
	 */
	/*
	private static String getPublisherValue(Document document)
	{
		String value = getDocumentValue(document, "origin");
		if (value == null)
			value = getDocumentValue(document, "publish");
		if (value == null)
		{
			System.out.println("  warning: did not find tags for origin or publish");
			value = "";
		}
		return value;
	}
	*/
	
	
	
	/**
	 * get the content date which could be in one of two different tags
	 * @param document
	 * @return
	 */
	protected String getDateValue(Document document)
	{
		NodeList nodes = document.getElementsByTagName("caldate");
		if (nodes.getLength() == 0)
			nodes = document.getElementsByTagName("begdate");
		if (nodes.getLength() == 0)
		{
			nodes = document.getElementsByTagName("dateStamp");
			System.out.println("checking dateStamp, " + nodes.getLength());
			if (nodes.getLength() ==  0)
			{
				missingParseTags.add("caldate or begdate");
				return "";
			}
		}
		Node node = nodes.item(0);
		//String tagValue = node.getChildNodes().item(0).getNodeValue();
		String tagValue = node.getTextContent();
		System.out.println("  date value = " + tagValue.trim());
		if (tagValue != null)
			tagValue = tagValue.trim();
		return tagValue;
	}

}
