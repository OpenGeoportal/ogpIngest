package org.OpenGeoPortal.Ingest.Metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This is a work in progress to gather data from metadata (FGDC) files for a
 * thesaurus.
 * 
 * we build a hashtable object whose keys are standard ISO names each value is a
 * vector containing unique names associated with the ISO name
 * 
 * This file contains a main function.  I run it from my IDE.  Here's a small sample
 * of its output:
 *  oceans and estuaries:"Seafloor":7
 *  oceans and estuaries:"Marine Geology":7
 *  oceans and estuaries:"elevation":8
 *  oceans and estuaries:"U.S. Geological Survey":13
 *  oceans and estuaries:"USGS":13
 *  oceans and estuaries:"Coastal and Marine Geology Program":13
 *  oceans and estuaries:"CMGP":13
 *  oceans and estuaries:"Woods Hole Science Center":13
 *  oceans and estuaries:"WHSC":13
 *  oceans and estuaries:"oceans and estuaries":13
 * This says "oceans and stuaries" is a standard ISO term.  In the analyzed data, there were
 * 7 layers where this ISO term appeared as a keyword and "Seafloor" was also used as a keyword.
 * One can imagine this data being further processed and used to generate synonym information.
 * 
 * @author stevemcdonald
 * 
 */
public class DictionaryBuilder
{

	/**
	 * for each iso term we keep a list of AlternateTerm objects
	 * this provides a place to keep track of how many times
	 *   an iso term is associated with a specific alternate term
	 * @author smcdon08
	 *
	 */
	public static class AlternateTerm implements Comparable
	{
		public AlternateTerm(String value)
		{
			this.value = value;
		}
		
		public String value;
		public int count = 1;
		
		public int compareTo(Object arg)
		{
			// TODO Auto-generated method stub
			if (count > ((AlternateTerm)arg).count)
				return 1;
			else if (count < ((AlternateTerm)arg).count)
				return -1;
			return 0;
		}
	}
	
	
	/**
	 *  key is an iso keyword, value is a list of associated values
	 */
	Hashtable<String, List<AlternateTerm>> dictionary;

	public DictionaryBuilder()
	{
		dictionary = new Hashtable<String, List<AlternateTerm>>();
	}
	
	private AlternateTerm contains(List<AlternateTerm> alternates, String value)
	{
		for (AlternateTerm current : alternates)
		{
			if (current.value.equalsIgnoreCase(value))
				return current;
		}
		return null;
	}

	/**
	 * add data from the passed file to the dictionary object we parse the file
	 * as an XML and then look for the theme tags
	 * 
	 * @param fileName
	 */
	private void processFile(File fileName)
	{
		InputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(fileName);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			documentBuilderFactory.setValidating(false); // dtd isn't available
			documentBuilderFactory
					.setFeature(
							"http://apache.org/xml/features/nonvalidating/load-external-dtd",
							false);
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
			Document document = documentBuilder.parse(inputStream);
			NodeList themeNodes = document.getElementsByTagName("theme");
			if (themeNodes.getLength() < 2)
			{
				// with one or zero theme tags, we never have ISO keywords
				inputStream.close();
				return;
			}

			// loop over all theme tags
			// if we have a theme tag with the themekt tag equal to ISO 19115
			// Topic Category
			// get all the themekeys in the theme tag
			// and associate the these themekeyss with the themekeys in the
			// non-FGDC themekt tag
			Vector<String> isoKeywords = getIsoKeywords(themeNodes);
			if (isoKeywords.size() > 0)
			{
				// here if we found iso keywords
				// now get other keywords to associated with these isos
				Vector<String> otherKeywords = getLcshAndOtherKeywords(themeNodes);
				associateKeywords(isoKeywords, otherKeywords);
			}
			else
			{
				Vector<String> otherKeywords = getLcshAndOtherKeywords(themeNodes);
				Vector<String> noIsoKeywords = new Vector<String>();
				noIsoKeywords.add("NoIsoKeywordFound");
				associateKeywords(noIsoKeywords, otherKeywords);	
			}
		}
		catch (FileNotFoundException e)
		{
			System.out
					.println("FileNotFoundException in DictionaryBuilder.processFile: fileName = "
							+ fileName);
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			System.out
					.println("ParserConfigurationException in DictionaryBuilder.processFile");
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			System.out.println("SAXException in DictionaryBuilder.processFile");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.out.println("IOException in DictionaryBuilder.processFile");
			e.printStackTrace();
		}
		finally
		{
			if (inputStream != null)
			{
				try
				{
					inputStream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}

	}

	private void associateKeywords(Vector<String> isoKeywords,
			Vector<String> otherKeywords)
	{
		for (String currentIsoKeyword : isoKeywords)
		{
			List<AlternateTerm> dictionaryValue = dictionary.get(currentIsoKeyword);
			if (dictionaryValue == null)
			{
				// here if this is the first time seeing currentIsoKeyword,
				// initialize value to vector
				dictionaryValue = new Vector<AlternateTerm>();
				dictionary.put(currentIsoKeyword, dictionaryValue);
			}
			for (String currentOtherKeyword : otherKeywords)
			{
				AlternateTerm alternateTerm = contains(dictionaryValue, currentOtherKeyword);
				if (alternateTerm != null)
					alternateTerm.count++;
				else
				{
					alternateTerm = new AlternateTerm(currentOtherKeyword);
					dictionaryValue.add(alternateTerm);
				}
			}

		}

	}

	/**
	 * loop over themes looking ones holding the ISO themekeys and return it
	 * 
	 * @param nodes
	 * @return
	 */
	private Vector<String> getIsoKeywords(NodeList nodes)
	{
		String isoThemeKt = "ISO 19115 Topic Category";
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node currentNode = nodes.item(i); // a theme node
			NodeList childNodes = currentNode.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++)
			{
				Node currentChild = childNodes.item(j);
				if (currentChild.getNodeName() == "themekt")
				{
					// if the themekt is ISO 19115 Topic Category, then return a
					// list of the iso theme keywords
					String tempValue = currentChild.getTextContent().trim();
					if (isoThemeKt.equals(tempValue))
					{
						// here if we have found what we are looking for
						// an iso theme tag, collect all themekeys
						return getThemeKeysFromThemeNode(currentNode);
					}
				}
			}
		}
		return new Vector<String>();
	}

	/**
	 * loop over all the themes looking for non-FGDC and non-ISO themekt
	 * (keyword thesaurus) theme tags return a list of the associated themekeys
	 * 
	 * @param nodes
	 * @return
	 */
	private Vector<String> getLcshAndOtherKeywords(NodeList nodes)
	{
		Vector<String> returnValue = new Vector<String>();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node themeNode = nodes.item(i);
			if (hasThemeKt(themeNode) == false)
			{
				// if no themeKt, we include the nodes themekeys
				Vector<String> temp = getThemeKeysFromThemeNode(themeNode);
				returnValue.addAll(temp);
			}
			else
			{
				NodeList childNodes = themeNode.getChildNodes();
				for (int j = 0; j < childNodes.getLength(); j++)
				{
					// look for themekt that isn't ISO or FGDC
					// if found, then collect the themkeys of the node
					Node currentChild = childNodes.item(j);
					if (currentChild.getNodeName() == "themekt")
					{
						if ((currentChild.getTextContent() != "FGDC")
								&& (currentChild.getTextContent() != "ISO 19115 Topic Category"))
						{
							Vector<String> temp = getThemeKeysFromThemeNode(themeNode);
							returnValue.addAll(temp);
						}
					}
				}
			}
		}
		return returnValue;
	}

	/**
	 * does the passed node have a child with the tag "themekt"
	 * that is, does the passed list of theme nodes have an ISO keyword
	 * @param themeNode
	 * @return
	 */
	private boolean hasThemeKt(Node themeNode)
	{
		NodeList childNodes = themeNode.getChildNodes();
		for (int j = 0; j < childNodes.getLength(); j++)
		{
			Node currentChild = childNodes.item(j);
			if (currentChild.getNodeName() == "themekt")
				return true;
		}
		return false;
	}

	private Vector<String> getThemeKeysFromThemeNode(Node themeNode)
	{
		Vector<String> themeKeys = new Vector<String>();
		NodeList childNodes = themeNode.getChildNodes();
		for (int j = 0; j < childNodes.getLength(); j++)
		{
			Node currentChild = childNodes.item(j);
			if (currentChild.getNodeName() == "themekey")
				themeKeys.add(currentChild.getTextContent());
		}
		return themeKeys;
	}

	/**
	 * dump dictionary in a format useful for future processing
	 */
	private void createReport(PrintStream printStream)
	{
		Set<String> keys = dictionary.keySet();
		for (String currentKey : keys)
		{
			List<AlternateTerm> alternateTerms = dictionary.get(currentKey);
			Collections.sort(alternateTerms);
			for (AlternateTerm alternateTerm : alternateTerms)
			{
				//if (alternateTerm.count > 1)
					printStream.println(currentKey + ":" + "\"" + alternateTerm.value + "\""+ ":" + alternateTerm.count);
			}
		}
	}

	/**
	 * loop over all metadata files in the hard-coded directories to build
	 * dictionary
	 * 
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException
	{
		System.out.println("top of DictionaryBuilder");
		DictionaryBuilder dictionaryBuilder = new DictionaryBuilder();
		String[] directories = {"/Users/smcdon08/tmp/OpenGeoPortal/HarvardMetadata", "/Users/smcdon08/tmp/OpenGeoPortal/MitMetadata",
				"/Users/smcdon08/tmp/OpenGeoPortal/MassGisMetadata2", 
				"/Users/smcdon08/tmp/OpenGeoPortal/Tufts/XML"};
				//"/Users/smcdon08/tmp/OpenGeoPortal/GistMetadata/GISTPublicMetadata"};
		for (String tempDirectory : directories)
		{
			File currentDirectory = new File(tempDirectory);
			File[] fgdcFiles = currentDirectory.listFiles();
			// loop over all the files in the directory and process the xml
			// files
			for (File currentFgdcFile : fgdcFiles)
			{
				String filename = currentFgdcFile.getName();
				if (filename.endsWith(".xml"))
				{
					System.out.println("processing file " + currentFgdcFile);
					dictionaryBuilder.processFile(currentFgdcFile);
				}

			}
		}
		// all done processing files, create report for user
		//dictionaryBuilder.createReport(System.out);
		dictionaryBuilder.createReport(new PrintStream("/Users/smcdon08/tmp/OpenGeoPortal/tmp/dictionaryOutput.txt"));
		System.out.println("end of report");
	}
}
