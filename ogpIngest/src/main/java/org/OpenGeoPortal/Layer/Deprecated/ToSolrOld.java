package org.OpenGeoPortal.Layer.Deprecated;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;

import org.OpenGeoPortal.Layer.Deprecated.BaseGeoParser.Access;
import org.OpenGeoPortal.Layer.Deprecated.BaseGeoParser.Key;

import static org.OpenGeoPortal.Layer.Deprecated.BaseGeoParser.getValue;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringEscapeUtils;


/**
 * This class creates a Solr ingestable file.  
 * It uses an FGDC or ISO parser to create a hashtable of metadata.
 * The main program iterates over a directory containing metadata files
 *   and creates Solr ingestable files.  
 * Functions in this class are also called web page in tufts/ingestHandler.jsp
 * 
 * This class is rather crude.  It uses the function setGlobals to 
 * initialize several variable based on the institution.  I use "main" after
 * I receive a set of metadata from an institution.  I edit setGlobals to 
 * specify input and output directories, etc. and then run main.  Then I ingest 
 * the newly Solr ingestable files into my local Solr instance.
 *  
 * @author smcdon08
 *
 */
@Deprecated
public class ToSolrOld 
{
	/*
	 * 
	 * php code to parse harvard fgdc xml docs: (geometry type)

	//assign geometry type

	  if ($xml_file->spdoinfo->direct == 'Raster'){
	        $geometry_type = 'raster';
	  }
	  else {
	        $geotype = (string) $xml_file->spdoinfo->ptvctinf->sdtsterm->sdtstype;

	 switch($geotype){
	             case "G-polygon":
	                     $geometry_type = 'polygon';
	             break;
	             case "Composite object":
	             case "Entity point":
	                     $geometry_type = 'point';
	             break;
	             case "String":
	                     $geometry_type = 'line';
	             break;
	             default:
	 */

		protected FileItem inputFileItem = null;
		public static String inputFormat = "FGDC";  //fgdc or iso 

		protected String inputFilename = null;
		protected OutputStream outputStream;
		//protected int layerId;
		
		//public static int baseLayerId;
		public String institution; 
		public String wmsUrl;
		public static String fgdcDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/TuftsMetadata";
		public static String solrDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/TuftsSolrData/";
		public static String workspaceName = "";
		public static String srsProjectionCode = "";
		public static String esriLayerNamePrefix = "";
		
		BaseGeoParser geoParser = null;
		
		public ToSolrOld(String inputFilename, OutputStream outputStream, String passedInstitution)
		{
			this.inputFilename = inputFilename; 
			this.outputStream = outputStream;
			this.institution = passedInstitution;
			setGlobals(institution);
		}
		
		public ToSolrOld(FileItem inputFileItem, OutputStream outputStream, String passedInstitution)
		{
			this.inputFileItem = inputFileItem; 
			this.outputStream = outputStream;
			this.institution = passedInstitution;
			setGlobals(institution);
		}
		
		
								
		/**
		 * convert the passed Fgdc file to a Solr file at the passed destination
		 * The passed layerId is a Solr key
		 * @param inputStream
		 * @param outputStream
		 * @param layerId
		 */
		public Hashtable<Key, Object> convert() 
		{
			System.out.println("in convert with inputFilename = " + inputFilename + ", inputFormat = " + inputFormat);
			if (inputFileItem != null)
				geoParser =  BaseGeoParser.newInstance(inputFormat, inputFileItem);
			else if (inputFilename != null)
				geoParser =  BaseGeoParser.newInstance(inputFormat, inputFilename);
			Hashtable<Key, Object> layerValues = geoParser.readFile();
			System.out.println("  layerValues = " + layerValues);
			if ((getValue(layerValues, Key.EsriName) == null) || getValue(layerValues, Key.EsriName) == "")
			{
				System.out.println("  layer name not found, preview will not be possible");
			}
			String layerId = institution + "." + getValue(layerValues, Key.EsriName); 
			layerValues.put(Key.LayerId, layerId);
			// add full text of document to hashtable
			//File inputFile = new File(inputStream);
			layerValues.put(Key.FgdcText, getFgdcText());
			saveSolrDocument(layerValues);
			return layerValues;
		}
		
		
		
		/**
		 * read the file into a string, escape it, and return it
		 * used to add source data to Solr record
		 * @param inputStream
		 * @param layerValues
		 */
		protected String getFgdcText()
		{
			try 
			{
				//BufferedReader reader = new BufferedReader(new FileReader("/Users/smcdon08/tmp/OpenGeoPortal/TestMetadata/zw_p53rivers_2002.xml"));
				InputStream inputStream = geoParser.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				//BufferedReader reader = new BufferedReader(new FileReader(inputFilename));

				String fileContents = "";
				String currentLine = reader.readLine();
				while (currentLine != null)
				{
					fileContents += currentLine;
					currentLine = reader.readLine();
				}
				fileContents = fileContents.replaceAll("[^\\p{ASCII}]", "");

				String escapedFileContents = StringEscapeUtils.escapeHtml(fileContents);
				return escapedFileContents;
			} 
			catch (FileNotFoundException e) 
			{
				System.out.println("error in FgedToSolr.getFgdcText");
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				System.out.println("error in FgedToSolr.getFgdcText");
				e.printStackTrace();
			}
			return "";
		}
		
		
		/**
		 * generate a file based on the data in hashtable that can be ingested by Solr
		 * this function simply pounds together data from the hashtable with some literal tags
		 * @param solrFile
		 * @param layerInfo
		 */
		protected void saveSolrDocument(Hashtable<Key, Object> layerInfo)
		{
			PrintStream solrFile = null;
			solrFile = new PrintStream(outputStream);

			solrFile.println("<add allowDups=\"false\">");
			solrFile.println();
			solrFile.println("<doc>");
			solrFile.println("  <field name=\"LayerId\">" + layerInfo.get(Key.LayerId) + "</field>");
			solrFile.println("  <field name=\"Name\">" + esriLayerNamePrefix + getValue(layerInfo, Key.EsriName).toUpperCase() + "</field>");
			solrFile.println("  <field name=\"CollectionId\">initial collection</field>");
			solrFile.println("  <field name=\"Institution\">" + institution + "</field>");
			solrFile.println("  <field name=\"InstitutionSort\">" + institution + "</field>");
			BaseGeoParser.Access access = (BaseGeoParser.Access)layerInfo.get(Key.Access);
			solrFile.println("  <field name=\"Access\">" + access + "</field>");
			String solrType = getValue(layerInfo, Key.DataType); 
		
			solrFile.println("  <field name=\"DataType\">" + solrType + "</field>");
			solrFile.println("  <field name=\"DataTypeSort\">" + solrType + "</field>");
			solrFile.println("  <field name=\"Availability\">Online</field>");
			solrFile.println("  <field name=\"LayerDisplayName\">" + getValue(layerInfo, Key.Title) + "</field>");
			solrFile.println("  <field name=\"LayerDisplayNameSort\">" + getValue(layerInfo, Key.Title) + "</field>");
			solrFile.println("  <field name=\"Publisher\">" + getValue(layerInfo, Key.Publisher) + "</field>");
			solrFile.println("  <field name=\"PublisherSort\">" + getValue(layerInfo, Key.Publisher) + "</field>");
			solrFile.println("  <field name=\"Originator\">" + getValue(layerInfo, Key.Originator) + "</field>");
			solrFile.println("  <field name=\"OriginatorSort\">" + getValue(layerInfo, Key.Originator) + "</field>");
			
			solrFile.println("  <field name=\"ThemeKeywords\">" + getValue(layerInfo, Key.ThemeKeywords) + "</field>");
			solrFile.println("  <field name=\"PlaceKeywords\">" + getValue(layerInfo, Key.PlaceKeywords) + "</field>");		
			solrFile.println("  <field name=\"GeoReferenced\">" + "true" + "</field>");		
			
			solrFile.println("  <field name=\"Abstract\">" + getValue(layerInfo, Key.Abstract) + "</field>");
			
			double northBc = getDoubleValue(layerInfo, Key.NorthBc);
			double southBc = getDoubleValue(layerInfo, Key.SouthBc);
			double eastBc = getDoubleValue(layerInfo, Key.EastBc);
			double westBc = getDoubleValue(layerInfo, Key.WestBc);
			
			double centerX = (eastBc + westBc) / 2.; 
			double centerY = (northBc + southBc) / 2.;
			double halfWidth = Math.abs(eastBc - westBc) / 2.;
			double halfHeight = Math.abs(northBc - southBc) / 2.;
			double area = (halfHeight * 2.) * (halfWidth * 2.);
			//String projectionCode = getValue(layerInfo, Key.SrsProjectionCode);
			String projectionCode = srsProjectionCode;
			//String workspace = getValue(layerInfo, Key.WorkspaceName);
			String workspace = workspaceName;
			String location = getLocation(institution, access, solrType);
			solrFile.println("  <field name=\"Location\">" + location + "</field>");
			solrFile.println("  <field name=\"MaxY\">" + northBc + "</field>");
			solrFile.println("  <field name=\"MinY\">" + southBc + "</field>");
			solrFile.println("  <field name=\"MinX\">" + westBc + "</field>");
			solrFile.println("  <field name=\"MaxX\">" + eastBc + "</field>");
			solrFile.println("  <field name=\"CenterX\">" + centerX + "</field>");
			solrFile.println("  <field name=\"CenterY\">" + centerY + "</field>");
			solrFile.println("  <field name=\"HalfWidth\">" + halfWidth + "</field>");
			solrFile.println("  <field name=\"HalfHeight\">" + halfHeight + "</field>");
			solrFile.println("  <field name=\"Area\">" + area + "</field>");
			solrFile.println("  <field name=\"SrsProjectionCode\">" + projectionCode + "</field>");
			solrFile.println("  <field name=\"WorkspaceName\">" + workspace + "</field>");
			solrFile.println("  <field name=\"ContentDate\">" + processDateString(getValue(layerInfo, Key.ContentDate)) + "</field>");
			solrFile.println("  <field name=\"FgdcText\">" + layerInfo.get(Key.FgdcText) + "</field>");
			
			solrFile.println("</doc>");
			solrFile.println();
			solrFile.println("</add>");
			solrFile.flush();
			solrFile.close();
		}
		
		/**
		 * the location field holds a json object where, roughly speaking, keys are protocols and values are servers
		 * urls specified in this function should probably come from a config file
		 * perhaps this should take the entire hash as a parameter in case the urls are a function of other values 
		 * @param access
		 * @param solrType
		 * @return
		 */
		public static String getLocation(String institution, BaseGeoParser.Access access, String solrType)
		{
			String returnValue = "{";
			if (institution.startsWith("Tufts"))
			{
				if (access == Access.Public)
				{
					returnValue += "\"wms\": [\"http://geoserver01.uit.tufts.edu/wms\"],";
					if (solrType.equalsIgnoreCase("Raster"))
						returnValue += "\"wcs\": \"http://geoserver01.uit.tufts.edu/wcs\"";
					else
						returnValue += "\"wfs\": \"http://geoserver01.uit.tufts.edu/wfs\"";
				}
				else
				{
					returnValue += "\"wms\": [\"http://geoserver01.uit.tufts.edu:8443/wms\"]";
					if (solrType.equalsIgnoreCase("Raster"))
						returnValue += "\"wcs\": \"http://geoserver01.uit.tufts.edu:8443/wcs\"";
					else
						returnValue += "\"wfs\": \"http://geoserver01.uit.tufts.edu:8443/wfs\"";
				}
			}
			else if (institution.startsWith("Harvard"))
			{
				returnValue += "\"wms\": [\"http://hgl.harvard.edu:8080/geoserver/wms\"]," 
				                 +  "\"tilecache\": [\"http://hgl.harvard.edu/cgi-bin/tilecache/tilecache.cgi\"]," 
				                 +  "\"serviceStart\": \"http://hgl.harvard.edu:8080/HGL/RemoteServiceStarter\",";
				if (solrType.equalsIgnoreCase("Raster"))
					returnValue += "\"wcs\": \"http://hgl.harvard.edu:8080/geoserver/wcs\","
											  + "\"download\": \"http://hgl.harvard.edu:8080/HGL/HGLOpenDelivery\"";
				else
					returnValue += "\"wfs\": \"http://hgl.harvard.edu:8080/geoserver/wfs\"";
			}
			else if (institution.startsWith("Berkeley"))
			{
				returnValue += "\"wms\": [\"http://gis-gs.lib.berkeley.edu:8080/geoserver/wms\"],";
				if (solrType.equalsIgnoreCase("Raster"))
					returnValue += "\"wcs\": \"http://gis-gs.lib.berkeley.edu:8080/geoserver/wcs\"";
				else
					returnValue += "\"wfs\": \"http://gis-gs.lib.berkeley.edu:8080/geoserver/wfs\"";
			}
			else if (institution.startsWith("MassGIS"))
			{
				returnValue += "\"wms\": [\"http://giswebservices.massgis.state.ma.us/geoserver/wms\"],";
				if (solrType.equalsIgnoreCase("Raster"))
					returnValue += "\"wcs\": \"http://giswebservices.massgis.state.ma.us/geoserver/wcs\"";
				else
					returnValue += "\"wfs\": \"http://giswebservices.massgis.state.ma.us/geoserver/wfs\"";
			}
			else if (institution.startsWith("MIT"))
			{
				returnValue += "\"wms\": [\"http://arrowsmith.mit.edu:8080/geoserver/wms\"],"
							+ "\"tilecache\": [\"http://arrowsmith.mit.edu:8080/geoserver/gwc/service/wms\"],"
							+ "\"download\": \"https://arrowsmith.mit.edu/geoweb/php_scripts/shpDownloadDirectPublic.php\",";
				if (solrType.equalsIgnoreCase("Raster"))
					returnValue += "\"wcs\": \"http://arrowsmith.mit.edu:8080/geoserver/wcs\"";
				else
					returnValue += "\"wfs\": \"http://arrowsmith.mit.edu:8080/geoserver/wfs\"";
			}
					
			returnValue = returnValue + "}";
			
			return returnValue;
		}
		
		/**
		 * return an html formatted string describing the elements a layer 
		 * @param layerInfo
		 * @return
		 */
		public String getSolrReport(Hashtable<Key, Object> layerInfo)
		{
			String returnString = "";
			String projectionCode = getValue(layerInfo, Key.SrsProjectionCode);
			String workspace = getValue(layerInfo, Key.WorkspaceName);
			
			returnString += "ESRI Layer Name = " + getValue(layerInfo, Key.EsriName) + "<br/>\n";
			returnString += "Access = " + layerInfo.get(Key.Access) + "<br/>\n";
			String solrType = getValue(layerInfo, Key.DataType); 
			returnString += "Data Type = " + solrType + "<br/>\n";
			returnString += "Availability = Online<br/>\n";
			returnString += "Layer Display Name = " + getValue(layerInfo, Key.Title) + "<br/>\n";
			returnString += "Publisher = " + getValue(layerInfo, Key.Publisher) + "<br/>\n";
			returnString += "Originator = " + getValue(layerInfo, Key.Originator) + "<br/>\n";
			returnString += "Theme Keywords = " + getValue(layerInfo, Key.ThemeKeywords) + "<br/>\n";
			returnString += "Place Keywords = " + getValue(layerInfo, Key.PlaceKeywords) + "<br/>\n";		
			returnString += "Srs Projection Code = " + projectionCode + "<br/>\n";
			returnString += "Workspace Name = " + workspace + "<br/>\n";
			//returnString += "Abstract = " + getValue(layerInfo, Key.Abstract) + "<br/>\n";
			
			return returnString;
			
		}

		
		
		
		/**
		 * return the year portion of the date
		 * the FGDC date field is a free format string
		 * this function does a little to clean it up and creates a UTC date
		 *   solr requires the date to be something like 1995-12-31T23:59:59Z
		 * as more data becomes available, it will have to do more error checking
		 * @param date
		 * @return
		 */
		protected String processDateString(String passedDate)
		{
			String returnYear = "0001";
			if (passedDate == null)
				return "";
			if (passedDate.length() >= 6)
			{
				String temp = passedDate.substring(2,6);
				if (isYear(temp))
					returnYear = temp;
				else
				{
					temp = passedDate.substring(0, 4);
					if (isYear(temp))
						returnYear = temp;
				}
			}
			if (passedDate.length() >= 5)
			{
				String temp = passedDate.substring(1, 5);
				if (isYear(temp))  
					returnYear = temp;
			}
			if (passedDate.length() >= 4)
			{
				String temp = passedDate.substring(0, 4);
				if (isYear(passedDate))
					returnYear = temp;
			}
			
			String returnValue = returnYear + "-01-01T01:01:01Z";
			return returnValue;
		}
		
		/**
		 * can the passed string be parsed as an int
		 * @param year$
		 * @return
		 */
		protected boolean isYear(String year$)
		{
			try
			{
				int year = Integer.parseInt(year$);
				return true;
			}
			catch (NumberFormatException e)
			{
				return false;
			}
		}
		
		
		
		
		/**
		 * set pathname and other variables that will be used across all files being converted
		 * they make it easier to 
		 * @param passedInstitution
		 */
		public static void setGlobals(String passedInstitution)
		{
			System.out.println("ToSolr.setGlobals with passedInstitution = " + passedInstitution);
			if (passedInstitution == "Harvard")
			{
				solrDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/HarvardSolrData/";
				fgdcDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/HarvardMetadata/";
				//wmsUrl = "obsolete"; //"http://arrowsmith.mit.edu:8080/geoserver/gwc/service/wms";
				//baseLayerId = 2000;
			}
			else if (passedInstitution == "MIT")
			{
				fgdcDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/MitMetadata";
				solrDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/MitSolrDataLocation/";
				//wmsUrl = "obsolete"; //"http://arrowsmith.mit.edu:8080/geoserver/gwc/service/wms";
				//baseLayerId = 10000;
				workspaceName = "sde";
			}
			else if (passedInstitution == "Princeton")
			{
				fgdcDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/PrincetonMetadata";
				solrDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/PrincetonSolrData/";
				//wmsUrl = "obsolete"; //"http://arrowsmith.mit.edu:8080/geoserver/gwc/service/wms";
				//baseLayerId = 15000;
			}
			else if (passedInstitution.contains("Tufts"))
			{
				fgdcDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/TuftsMetadata2";
				solrDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/TuftsSolrDataLocation2/";
				//wmsUrl = "obsolete"; //"http://arrowsmith.mit.edu:8080/geoserver/gwc/service/wms";
				//baseLayerId = 25000;
				workspaceName = "sde";
				srsProjectionCode = "2249";
				esriLayerNamePrefix = "GISPORTAL.GISOWNER01.";
			}
			else if (passedInstitution == "Test")
			{
				fgdcDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/TestMetadata";
				solrDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/TestSolrData/";
				//wmsUrl = "obsolete"; //"http://arrowsmith.mit.edu:8080/geoserver/gwc/service/wms";
				//baseLayerId = 1;
			} 
			else if (passedInstitution == "MassGIS")
			{
				fgdcDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/MassGisMetadata3";
				solrDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/MassGisSolrDataLocation3/";
				//wmsUrl = "obsolete"; //"http://giswebservices.massgis.state.ma.us/geoserver/wms";
				workspaceName = "massgis";
				//baseLayerId = 1;
			} 
			else if (passedInstitution == "GIST")
			{
				fgdcDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/GistMetadata/GISTPublicMetadata";
				solrDirectoryName = "/Users/smcdon08/tmp/OpenGeoPortal/GistSolr/";
				//wmsUrl = "obsolete"; //"http://giswebservices.massgis.state.ma.us/geoserver/wms";
				workspaceName = "gist";
				srsProjectionCode = "2249";
				inputFormat = "ISO";
				//baseLayerId = 1;
			} 
		}
		
		
		
		/**
		 * I've see lat/lon values of REQUIRED: Eastern-most coordinate of the limit of coverage expressed in longitude.
		 *   they are not yet properly handled
		 * @param layerInfo
		 * @param key
		 * @return
		 */
		protected double getDoubleValue(Hashtable layerInfo, Key key)
		{
			String temp = getValue(layerInfo, key);  
			try
			{
				return Double.parseDouble(temp);
			}
			catch (NumberFormatException e)
			{
				return 0;
			}
		}


		
		//public int numberOfWarnings = 0;
		public Vector<String> missingTags = new Vector<String>();
		
		/**
		 * convert all the metadata files in the input directory to Solr ingestable files in the Solr directory
		 * @param args
		 */
		public static void main(String[] args)
		{
			
			//String passedInstitution = "MIT"; 
			String passedInstitution = "Tufts"; 
			//String passedInstitution = "GIST"; 
			//String passedInstitution = "MassGIS"; 
			//if (args.length > 0)
			//	passedInstitution = args[0];
			
			setGlobals(passedInstitution);
			
			File fgdcDirectory = new File(fgdcDirectoryName); 
			File[] fgdcFiles = fgdcDirectory.listFiles();
			if (fgdcFiles.length == 0)
			{
				System.out.println("error: invalid fgdc directory " + fgdcDirectoryName);
				return;
			}
			
			// loop over all the fgdc files, converting each to our Solr schema
			System.out.println("number files to process =  " + fgdcFiles.length);
			for (int i = 0 ; i < fgdcFiles.length ; i++)
			{
				File currentFgdcFile = fgdcFiles[i];
				String filename = currentFgdcFile.getName();
				if (filename.endsWith(".xml"))
				{
					// here with a real fgdc file
					String solrFilename = solrDirectoryName + filename;
					System.out.println("converting " + currentFgdcFile.toString() + " to " + solrFilename);
					try
					{
						//FileInputStream fileInputStream = new FileInputStream(currentFgdcFile);
						FileOutputStream fileOutputStream = new FileOutputStream(solrFilename);

						ToSolrOld converter = new ToSolrOld(currentFgdcFile.getAbsolutePath(), fileOutputStream, passedInstitution);
						converter.convert();
						
						if (converter.missingTags.size() > 0)
						{
							System.out.print("    missing:");
							for (int j = 0 ; j < converter.missingTags.size() ; j++)
								System.out.print(" " + converter.missingTags.get(j));
							System.out.println();
						}
						//fileInputStream.close();					
						fileOutputStream.close();
					}
					catch (FileNotFoundException e)
					{
						System.out.println("file not found: " + e);
					} catch (IOException e) 
					{
						System.out.println("IOException!");
						e.printStackTrace();
					}
				}
			}
			System.out.println("processing complete");
			//System.out.println("files processed =  " + fgdcFiles.length + ", number of warnings = " + numberOfWarnings);
			
			//convert("/Users/smcdon08/tmp/OpenGeoPortal/HarvardMetadata/AFRICOVER_KE_WOODY_AGG.xml",
			//		"/Users/smcdon08/tmp/OpenGeoPortal/tmp/solrTest.xml", 1001);

		}
		


}
