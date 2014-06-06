package org.OpenGeoPortal.Ingest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.OpenGeoPortal.Ingest.IngestResponse.IngestInfo;
import org.OpenGeoPortal.Ingest.Metadata.MetadataConverter;
import org.OpenGeoPortal.Ingest.Metadata.MetadataElement;
import org.OpenGeoPortal.Ingest.Metadata.MetadataParseResponse;
import org.OpenGeoPortal.Keyword.PlaceKeywords;
import org.OpenGeoPortal.Keyword.ThemeKeywords;
import org.OpenGeoPortal.Keyword.KeywordThesauri.ThemeKeywordThesaurus;
import org.OpenGeoPortal.Keyword.KeywordThesauri.UnspecifiedKeywordThesaurus;
import org.OpenGeoPortal.Layer.BoundingBox;
import org.OpenGeoPortal.Layer.GeometryType;
import org.OpenGeoPortal.Layer.LocationLink;
import org.OpenGeoPortal.Layer.LocationLink.LocationType;
import org.OpenGeoPortal.Layer.Metadata;
import org.OpenGeoPortal.Layer.WMSMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WMSCapabilitiesIngestJobImpl implements WMSCapabilitiesIngestJob, Runnable {
	private IngestStatusManager ingestStatusManager;
	private MetadataConverter metadataConverter;
	private String institution;
	private String wmsEndpoint;
	private UUID jobId;
	private IngestStatus ingestStatus;

	private SolrIngest solrIngest;
	private Set<MetadataElement> requiredFields = new HashSet<MetadataElement>();
	private final String VERSION = "1.1.1";
	private DocumentBuilder builder;
	//private DocumentBuilderFactory factory;
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());




	
	public IngestStatusManager getIngestStatusManager() {
		return ingestStatusManager;
	}


	public void setIngestStatusManager(IngestStatusManager ingestStatusManager) {
		this.ingestStatusManager = ingestStatusManager;
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


	private void getWMSCapabilitiesRecords() throws Exception{
		ingestStatus = ingestStatusManager.getIngestStatus(jobId);
		Document document = null;
		try {
			URL wmsCapabilities = new URL(wmsEndpoint
					+ "?service=wms&request=getCapabilities&version=" + VERSION);

			InputStream inputStream = wmsCapabilities.openStream();
			// parse the returned XML
			// Create a factory
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			// Use document builder factory
			// ignore validation, dtd
			factory.setAttribute(
					"http://apache.org/xml/features/nonvalidating/load-external-dtd",
					false);
			factory.setValidating(false);

			builder = factory.newDocumentBuilder();
			// Parse the document
			document = builder.parse(inputStream);
		} catch (Exception e){
			logger.error("Error loading or parsing wms getCapabilities document: " + e.getMessage());
			e.printStackTrace();
		}

		
		Node capabilityNode = document.getElementsByTagName("Capability").item(0);
		NodeList capChildren = capabilityNode.getChildNodes();
		Node layerNode = null;
		for (int k = 0; k < capChildren.getLength(); k++){
			if (capChildren.item(k).getNodeName().equalsIgnoreCase("Layer")){
				layerNode = capChildren.item(k);
				break;
			}
		}
		
		if (layerNode == null){
			String msg = "No <Layer> node found in this document.";
			logger.error(msg);
			throw new Exception(msg);
		}
		
		NodeList layerNodes = document.getElementsByTagName("Layer");
		for (int i = 0; i < layerNodes.getLength(); i++){
			Node layerElement = layerNodes.item(i);
					WMSMetadata metadata = null;
	
					try{
						metadata = processLayerNode(layerElement);
					} catch (Exception e) {
						continue;
					}
					Metadata augmentedMetadata = null;
					if (metadata.hasMetadataUrl()){
						try {
							augmentedMetadata = augmentMetadata(metadata);
							ingestToSolr(augmentedMetadata);
							continue;
						} catch (Exception e) {
							logger.error("Unable to augment GetCapabilities metadata with linked xml.");
							ingestToSolr(metadata);
							e.printStackTrace();
						}
					}
				
					ingestToSolr(metadata);

		}
		ingestStatus.setJobStatus(IngestJobStatus.Succeeded);
	}
	
	
	private Metadata augmentMetadata(WMSMetadata metadata) throws Exception {
		logger.info("Attempting to augment metadata for '" + metadata.getOwsName() + "' with info from '" + metadata.getMetadataUrl().toString() + "'");
   		URL metadataLink = metadata.getMetadataUrl();
   		
   		Metadata linkedMetadata = null;
   		try{
   			InputStream inputStream = metadataLink.openStream();
		
   			MetadataParseResponse response = metadataConverter.parse(inputStream, institution);
		
   			linkedMetadata = response.metadata;
   		} catch (Exception e){
   			logger.error("error getting metadata: " + e.getMessage());
   			throw new Exception("Unable to augment GetCapabilities metadata with linked xml.");
   		}
		//compare metadata object with metadata parsed from the linked xml doc.
		
		//these values are always better from getCaps
		linkedMetadata.setBounds(metadata.getBounds());
		linkedMetadata.setOwsName(metadata.getOwsName());
		linkedMetadata.setId(metadata.getId());
		linkedMetadata.setWorkspaceName(metadata.getWorkspaceName());
		
		//compare and decide.  prefer the first argument value
		linkedMetadata.setTitle(compareTitles(linkedMetadata.getTitle(), metadata.getTitle()));
		linkedMetadata.setLocation(compareLocations(metadata.getLocation(), linkedMetadata.getLocation()));
		linkedMetadata.setDescription(compareAbstracts(linkedMetadata.getDescription(), metadata.getDescription()));
		linkedMetadata.setThemeKeywords(compareThemeKeywords(linkedMetadata.getThemeKeywords(), metadata.getThemeKeywords()));
		
		return linkedMetadata;
	}


	private List<ThemeKeywords> compareThemeKeywords(
			List<ThemeKeywords> themeKeywords,
			List<ThemeKeywords> themeKeywords2) {
		//most likely the keywords from the getCaps document will be worthless.  just exclude them for now, if we have a linked xml metadata doc
		return themeKeywords;
	}


	private String compareAbstracts(String description, String description2) {
		return choosePopulated(description, description2);
	}


	private Set<LocationLink> compareLocations(Set<LocationLink> location,
			Set<LocationLink> location2) {
		//add any additional links found in the metadata, but prefer ogc links from the getCaps document
		Set<LocationType> locTypes = new HashSet<LocationType>();
		for (LocationLink link: location){
			locTypes.add(link.getLocationType());
		}
		
		for (LocationLink addLink: location2){
			if (!locTypes.contains(addLink.locationType)){
				location.add(addLink);
			}
		}
		return location;
	}


	private String compareTitles(String title, String altTitle){
		return choosePopulated(title, altTitle); 
	}
	
	private String choosePopulated(String a, String b){
		if (a.trim().isEmpty()){
			return b;
		} else {
			return a;
		}
	};
	
	private WMSMetadata processLayerNode(Node layer) throws Exception {
		/*
		 * 
<Layer queryable="0">
 <Name>Ozone_adv</Name> 
 <Title>OMI_AI_G:Column Amount Ozone</Title> 
<KeywordList>
 <Keyword>Domain:Aerosol</Keyword> 
 <Keyword>Dataset:OMI_AI_G</Keyword> 
 <Keyword>Platform:Satellite</Keyword> 
 <Keyword>DataType:GRID</Keyword> 
 </KeywordList>
 <SRS>EPSG:4326</SRS> 
 <LatLonBoundingBox minx="-180" miny="-90" maxx="180" maxy="90" /> 
 <Dimension name="time" units="ISO8601" /> 
 <Extent name="time" default="2007-05-12T00:00:00Z">2004-08-17T00:00:00Z/2009-05-28T00:00:00Z/P1D</Extent> 
<MetadataURL type="TC211">
 <Format>text/xml</Format> 
 <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="http://capita.wustl.edu/DataspaceMetadata_ISO/DataFed.OMI_AI_G.Ozone_adv.xml" xlink:type="simple" /> 
 </MetadataURL>
<DataURL>
 <Format>text/html</Format> 
 <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="http://webapps.datafed.net/browser.aspx?dataset_abbr=OMI_AI_G&param_abbr=Ozone_adv" xlink:type="simple" /> 
 </DataURL>
<Style>
 <Name>data</Name> 
 <Title>Data</Title> 
 </Style>
<Style>
 <Name>default</Name> 
 <Title>Default</Title> 
 </Style>
 </Layer>
		 * 
		 */
		
		WMSMetadata metadata = new WMSMetadata();
		metadata.setAccessLevel("public");
		metadata.setInstitution(institution);
		metadata.setLocation(new HashSet<LocationLink>());
		metadata.setGeometryType(GeometryType.Undefined);
		metadata.setThemeKeywords(new ArrayList<ThemeKeywords>());
		metadata.setPlaceKeywords(new ArrayList<PlaceKeywords>());
		
		NodeList layerNodes = layer.getChildNodes();
		Boolean hasName = false;
		for (int k = 0; k < layerNodes.getLength(); k++){
			Node currentLayerNode = layerNodes.item(k);
			String tag = currentLayerNode.getNodeName();
			logger.debug(tag);
			//parse layer name
			if (tag.equalsIgnoreCase("Name")){
				logger.info("Has name");
				logger.info(currentLayerNode.getTextContent());
				hasName = true;
				addName(metadata, currentLayerNode);
				logger.info(metadata.getOwsName());
			//parse bounds
			} else if (tag.equalsIgnoreCase("Title")){
				addTitle(metadata, currentLayerNode);
			//parse bounds
			} else if (tag.equalsIgnoreCase("LatLonBoundingBox")) {
				addBounds(metadata, currentLayerNode);
			} else if (tag.equalsIgnoreCase("Abstract")){
				addAbstract(metadata, currentLayerNode);
			} else if (tag.equalsIgnoreCase("KeywordList")){
				addKeywords(metadata, currentLayerNode);
			} else if (tag.equalsIgnoreCase("MetadataURL")){
				addMetadataUrl(metadata, currentLayerNode);
			}
		}
		if (!hasName){
			throw new Exception("No value for name!");
		}
		calculateLocation(metadata);
		logger.info("After calculate location: " + Integer.toString(metadata.getLocation().size()));
		
		return metadata;
	}
	
	private void addMetadataUrl(WMSMetadata metadata, Node metadataNode) {
		/*<MetadataURL type="TC211">
		 <Format>text/xml</Format> 
		 <OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="http://capita.wustl.edu/DataspaceMetadata_ISO/DataFed.OMI_AI_G.Ozone_adv.xml" xlink:type="simple" /> 
		 </MetadataURL>*/
		Boolean process = false;
		String metadataUrlString = "";
		NodeList detailNodes = metadataNode.getChildNodes();
		for (int i = 0; i < detailNodes.getLength(); i++){
			Node currentNode = detailNodes.item(i);
			String tag = currentNode.getNodeName();
			logger.debug(tag);
			if (tag.equalsIgnoreCase("format")){
				//try to process it if it is xml
				if (tag.toLowerCase().contains("xml")||tag.toLowerCase().contains("text")){
					process = true;
				}
			} else if (tag.equalsIgnoreCase("onlineresource")){
				try{
				metadataUrlString = currentNode.getAttributes().getNamedItem("href").getTextContent().trim();
				logger.info("URL:" + metadataUrlString);
				} catch (NullPointerException e){
					logger.error("URL:" + metadataUrlString);
					e.printStackTrace();
				}
			}
		}
		if (process && !metadataUrlString.isEmpty()){
			try {
				metadata.setMetadataUrl(new URL(metadataUrlString));
			} catch (MalformedURLException e){
				logger.error("Malformed URL: " + metadataUrlString);
			}
		}
	}
	
	private void addTitle(WMSMetadata metadata, Node titleNode) {
		String title = titleNode.getTextContent().trim();
		logger.debug("Adding title..'" + title + "'");

		metadata.setTitle(title);
	}


	private void calculateLocation(WMSMetadata metadata) {
		LocationLink link;
		try {
			link = new LocationLink(LocationType.wms, new URL(this.wmsEndpoint));
			metadata.addLocation(link);
		} catch (MalformedURLException e1) {
			logger.error("Malformed URL: " + wmsEndpoint);
		}
		//how can we get wfs or wcs?  wms describe layer

		
		try {
			String qualifiedName = "";
			if (!metadata.getWorkspaceName().isEmpty()){
				qualifiedName = metadata.getWorkspaceName() + ":" + metadata.getOwsName();
			} else {
				qualifiedName = metadata.getOwsName();
				logger.info(qualifiedName);
			}
			LocationLink owsLink = getOwsUrl(qualifiedName);

			metadata.addLocation(owsLink);
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e){
			logger.error("DescribeLayer returned a faulty value.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Number of links: " + Integer.toString(metadata.getLocation().size()));
		
	}
	
	private LocationLink getOwsUrl(String layerName) throws Exception{
		//http://geoserver01.uit.tufts.edu/wms?request=describeLayer&version=1.1.1&layers=sde:GISPORTAL.GISOWNER01.LANDINFO_K01C
		/*<?xml version="1.0" encoding="UTF-8"?>
		 * <!DOCTYPE WMS_DescribeLayerResponse SYSTEM "http://geoserver01.uit.tufts.edu:80/schemas/wms/1.1.1/WMS_DescribeLayerResponse.dtd">
		 * <WMS_DescribeLayerResponse version="1.1.1">
		 * <LayerDescription name="sde:GISPORTAL.GISOWNER01.LANDINFO_K01C" owsURL="http://geoserver01.uit.tufts.edu:80/wcs?" owsType="WCS">
		 * <Query typeName="sde:GISPORTAL.GISOWNER01.LANDINFO_K01C"/></LayerDescription></WMS_DescribeLayerResponse>
		 */
		/*
		 * 
		 * <WMS_DescribeLayerResponse version="1.1.0"><LayerDescription name="Framework.RESERVE" wfs="http://frameworkwfs.usgs.gov/framework/wms/wms.cgi?DATASTORE=Framework"><Query typeName="RESERVE"/></LayerDescription></WMS_DescribeLayerResponse>
		 * 
		 * 
		 * 
		 */
		logger.info("Requesting 'DescribeLayer' info for '" + layerName + "'");
   		URL describeLayer = new URL(wmsEndpoint + "?request=describeLayer&" +
   				"layers=" + layerName + "&version=" + VERSION);
   		
		InputStream inputStream = describeLayer.openStream();
		
		Document document = builder.parse(inputStream);

		Node description = document.getElementsByTagName("LayerDescription").item(0);
		NamedNodeMap attributes = description.getAttributes();
		Node owsUrl = null;
		URL url = null;
		String type = null;
		try {
			owsUrl = attributes.getNamedItem("owsURL");
			if (owsUrl == null){
				owsUrl = attributes.getNamedItem("wfs");
				if (owsUrl == null){
					owsUrl = attributes.getNamedItem("wcs");
					type = "wcs";
					url = new URL(owsUrl.getTextContent().trim());
				} else {
					type = "wfs";
					url = new URL(owsUrl.getTextContent().trim());
				}
			} else {
				type = attributes.getNamedItem("owsType").getTextContent().trim();
				url = new URL(owsUrl.getTextContent().trim());
				logger.debug("owsURL:" + url.toString());
				logger.debug("owsType:" + type);
			}
		} catch (Exception e){
		}

		
		LocationLink link = new LocationLink(LocationType.fromString(type), url);
		
		return link;
	}


	private void addKeywords(WMSMetadata metadata, Node keywordListNode) {
		/* <KeywordList><Keyword>ArcSDE</Keyword><Keyword>GISPORTAL.GISOWNER01.AFGHANISTANAIRPORTSFIELDS00</Keyword></KeywordList>*/
		//exclude keywords like ArcSDE and the layer name
		List<ThemeKeywords> themeKeywords = metadata.getThemeKeywords();
		NodeList keywordNodes = keywordListNode.getChildNodes();
		for (int k = 0; k < keywordNodes.getLength(); k++){
			String keyword = keywordNodes.item(k).getTextContent().trim();
			String owsName = metadata.getOwsName();
			if (!keyword.isEmpty()){
				if (!keyword.equalsIgnoreCase("ArcSDE") && !keyword.equalsIgnoreCase(owsName)){
					if (keyword.toLowerCase().contains("extractdoc")){
						addMassGISMetadataLink(metadata, keyword);
					} else {
						ThemeKeywords keywords = new ThemeKeywords();
						//keywords.setKeywordThesaurus((ThemeKeywordThesaurus) new UnspecifiedKeywordThesaurus());
						//keywords.addKeyword(keyword);
						themeKeywords.add(keywords);
					}
				}
			}
		}
		
		metadata.setThemeKeywords(themeKeywords);

	}

	private void addMassGISMetadataLink(WMSMetadata metadata, String keyword){
			if (keyword.toLowerCase().endsWith(".xml")){
   				keyword = keyword.replace("\n", "");
   				//for some reason, some urls in the MassGIS doc have an extra http:// appended.
   				//18 characters for "ExtractDoc=http://"
				String metadataUrlString = keyword.substring(18);
				if (!metadataUrlString.contains("http://")){
					metadataUrlString = "http://" + metadataUrlString;
				}
				try {
					URL metadataUrl = new URL(metadataUrlString);
					metadata.setMetadataUrl(metadataUrl);
				} catch (MalformedURLException e) {
					logger.error("Malformed URL: " + metadataUrlString);
					e.printStackTrace();
				}
  			}
	}

	private void addAbstract(WMSMetadata metadata, Node currentLayerNode) {
		metadata.setDescription(currentLayerNode.getTextContent().trim());
		
	}


	public void addName(WMSMetadata metadata, Node nameNode) throws Exception{
		String qualifiedLayerName = nameNode.getTextContent().trim();
		if (qualifiedLayerName.isEmpty()){
			logger.info("name is empty");
			throw new Exception("Name is required!");
		}
		logger.debug("Adding qualified layer name..'" + qualifiedLayerName + "'");

		int delimiterIdx = qualifiedLayerName.indexOf(":");
		String layerName = "";
		String workspaceName = "";
		if (delimiterIdx > -1){
			layerName = qualifiedLayerName.substring(delimiterIdx + 1);
			workspaceName = qualifiedLayerName.substring(0, delimiterIdx);
		} else {
			layerName = qualifiedLayerName;
		}
		metadata.setOwsName(layerName);
		metadata.setWorkspaceName(workspaceName);
		
		metadata.setId(layerName);
	}
	
	public void addBounds(WMSMetadata metadata, Node geographicBoundsNode){
		//   v 1.1.1    <LatLonBoundingBox minx="-74.2574650485478" miny="40.49580519536013" maxx="-73.6994503244414" maxy="40.91580641842171"/>

		String minX = null;
		String minY = null;
		String maxX = null;
		String maxY = null;
		minX = geographicBoundsNode.getAttributes().getNamedItem("minx").getTextContent().trim();
		minY = geographicBoundsNode.getAttributes().getNamedItem("miny").getTextContent().trim();
		maxX = geographicBoundsNode.getAttributes().getNamedItem("maxx").getTextContent().trim();
		maxY = geographicBoundsNode.getAttributes().getNamedItem("maxy").getTextContent().trim();
		
		logger.info("Adding bounds..[" + minX + "," + minY + "," + maxX + "," + maxY + "]");

		metadata.setBounds(new BoundingBox(minX, minY, maxX, maxY));

	}
	
	public void addBounds1_3(WMSMetadata metadata, Node geographicBoundsNode){

		NodeList boundsNodes = geographicBoundsNode.getChildNodes();

		String minX = null;
		String minY = null;
		String maxX = null;
		String maxY = null;
		
		for (int i = 0; i < boundsNodes.getLength(); i++){
			Node boundsNode = boundsNodes.item(i);
			String nodeName = boundsNode.getNodeName();
			if (nodeName.equalsIgnoreCase("westBoundLongitude")){
				minX = boundsNode.getTextContent().trim();
			} else if (nodeName.equalsIgnoreCase("southBoundLongitude")){
				minY = boundsNode.getTextContent().trim();
			} else if (nodeName.equalsIgnoreCase("eastBoundLongitude")){
				maxX = boundsNode.getTextContent().trim();
			} else if (nodeName.equalsIgnoreCase("northBoundLongitude")){
				maxY = boundsNode.getTextContent().trim();
			}
		}

		metadata.setBounds(new BoundingBox(minX, minY, maxX, maxY));

	}

	public void ingestToSolr(Metadata metadata){
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
	}
	
	public void run() {
		try{
			getWMSCapabilitiesRecords();
		} catch (Exception e){
			e.printStackTrace();
			logger.error("Error in WMSCapabilitiesIngest: " + e.getMessage());
			ingestStatus.setJobStatus(IngestJobStatus.Failed);
		}
	}
	
	public void init(UUID jobId, String institution, String wmsEndpoint) {
		this.jobId = jobId;
		this.institution = institution;
		this.wmsEndpoint = wmsEndpoint;
		requiredFields.add(MetadataElement.Bounds);
		requiredFields.add(MetadataElement.Institution);
		requiredFields.add(MetadataElement.LayerName);
		requiredFields.add(MetadataElement.LayerId);

	}
}
