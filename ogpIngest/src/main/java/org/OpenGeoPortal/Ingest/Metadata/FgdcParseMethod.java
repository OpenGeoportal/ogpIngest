package org.OpenGeoPortal.Ingest.Metadata;

import java.util.ArrayList;
import java.util.List;

import org.OpenGeoPortal.Layer.AccessLevel;
import org.OpenGeoPortal.Layer.GeometryType;
import org.OpenGeoPortal.Layer.PlaceKeywords;
import org.OpenGeoPortal.Layer.ThemeKeywords;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * read in FGDC formated file into a hashtable representation this hash can be
 * used to generate a solr file.
 * 
 * @author smcdon08, chrissbarnett
 * 
 */
public class FgdcParseMethod extends AbstractXmlMetadataParseMethod implements
		MetadataParseMethod {
	public static enum FgdcTag implements Tag {
		Title("title", FieldType.Single), 
		Abstract("abstract", FieldType.Single), 
		LayerName("ftname", FieldType.Single), 
		Publisher("publish", FieldType.Single), 
		Originator("origin", FieldType.Single), 
		WestBc("westbc", FieldType.Single), 
		EastBc("eastbc", FieldType.Single), 
		NorthBc("northbc", FieldType.Single), 
		SouthBc("southbc",FieldType.Single), 
		KeywordsHeader("keywords", FieldType.Single), 
		PlaceKeywordsHeader("place", FieldType.Multiple), 
		ThemeKeywordsHeader("theme", FieldType.Multiple), 
		PlaceKeywordsThesaurus("placekt", FieldType.Multiple), 
		ThemeKeywordsThesaurus("themekt", FieldType.Multiple),
		PlaceKeywords("placekey", FieldType.Multiple), 
		ThemeKeywords("themekey", FieldType.Multiple),
		Access("accconst",FieldType.Single);

		private final String tagName; // XML tag name
		private final FieldType fieldType; // Is there one of these tags, or more?
		
		FgdcTag(String tagName, FieldType fieldType) {
			this.tagName = tagName;
			this.fieldType = fieldType;
		}

		public String getTagName() {
			return tagName;
		}

		public FieldType getFieldType() {
			return fieldType;
		}
	}
	
	void handleTitle(){
		Tag tag = FgdcTag.Title;
		try{
			this.metadataParseResponse.metadata.setTitle(getDocumentValue(tag));
		} catch (Exception e){
			logger.error("handleTitle: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}
	}
	
	void handleBounds(){
		Tag tag = FgdcTag.NorthBc;
		try{
			String maxY = getDocumentValue(tag);
			tag = FgdcTag.EastBc;
			String maxX = getDocumentValue(tag);
			tag = FgdcTag.SouthBc;
			String minY = getDocumentValue(tag);
			tag = FgdcTag.WestBc;
			String minX = getDocumentValue(tag);
			//should validate bounds here
			if (validateBounds(minX, minY, maxX, maxY)){
				this.metadataParseResponse.metadata.setBounds(minX, minY, maxX, maxY);
			} else {
				throw new Exception("Invalid Bounds: " + minX + "," + minY + "," + maxX + "," + maxY);
			}
		} catch (Exception e){
			logger.error("handleBounds: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}
	}
	
	
	/**
	 * marshall xml for keywords into theme and place keyword objects
	 * @return
	 */
	void handleKeywords(){
		String tagName = FgdcTag.KeywordsHeader.getTagName();
		String tagValue = "";
		List<ThemeKeywords> themeKeywordList = new ArrayList<ThemeKeywords>();
		List<PlaceKeywords> placeKeywordList = new ArrayList<PlaceKeywords>();

		NodeList nodes = document.getElementsByTagName(tagName);
		if (nodes.getLength() == 0){
			logger.info("no nodes under keyword");
			tagValue = null;//no keywords
		} else {
			Node keywordHeader = nodes.item(0);
			logger.info(keywordHeader.getNodeName());

			if (keywordHeader.hasChildNodes()){
				NodeList keywordNodeList = keywordHeader.getChildNodes();
				for (int j = 0; j < keywordNodeList.getLength(); j++){
					Node currentKeywordNode = keywordNodeList.item(j);
					if (currentKeywordNode.getNodeName().equalsIgnoreCase(FgdcTag.PlaceKeywordsHeader.getTagName())){
						logger.info("attempting to add place keyword node...");
						//add the contents of this node to place keywords
						PlaceKeywords placeKeywords = new PlaceKeywords();
						NodeList currentKeywordChildren = currentKeywordNode.getChildNodes();
						for (int i = 0; i < currentKeywordChildren.getLength(); i++){
							Node currentKeywordChild = currentKeywordChildren.item(i);
							if (currentKeywordChild.getNodeType() == 1){
							logger.info("node name: " + currentKeywordChild.getNodeName());
							if (currentKeywordChild.getNodeName().equalsIgnoreCase(FgdcTag.PlaceKeywordsThesaurus.getTagName())){
								logger.info("thesaurus: " + currentKeywordChild.getTextContent());
								try {
									placeKeywords.setThesaurus(getValidValue(currentKeywordChild.getTextContent()));
								} catch (DOMException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} else if (currentKeywordChild.getNodeName().equalsIgnoreCase(FgdcTag.PlaceKeywords.getTagName())){
								logger.info("keyword: " + currentKeywordChild.getTextContent());
								try {
									placeKeywords.addKeyword(getValidValue(currentKeywordChild.getTextContent()));
								} catch (DOMException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							}
						}
						placeKeywordList.add(placeKeywords);
					} else if (currentKeywordNode.getNodeName().equalsIgnoreCase(FgdcTag.ThemeKeywordsHeader.getTagName())){
						logger.info("attempting to add theme keyword node...");
						//add the contents of this node to theme keywords
						ThemeKeywords themeKeywords = new ThemeKeywords();
						NodeList currentKeywordChildren = currentKeywordNode.getChildNodes();
						for (int i = 0; i < currentKeywordChildren.getLength(); i++){
							Node currentKeywordChild = currentKeywordChildren.item(i);

							if (currentKeywordChild.getNodeType() == 1){
								logger.info("node value: " + currentKeywordChild.getNodeValue());
								logger.info("node name: " + currentKeywordChild.getNodeName());
								logger.info("node type: " + currentKeywordChild.getNodeType());
								if (currentKeywordChild.getNodeName().equalsIgnoreCase(FgdcTag.ThemeKeywordsThesaurus.getTagName())){
									logger.info("thesaurus: " + currentKeywordChild.getTextContent());
									try {
										themeKeywords.setThesaurus(getValidValue(currentKeywordChild.getTextContent()));
									} catch (DOMException e) {

									} catch (Exception e) {
									}
								} else if (currentKeywordChild.getNodeName().equalsIgnoreCase(FgdcTag.ThemeKeywords.getTagName())){
									logger.info("keyword: " + currentKeywordChild.getTextContent());
									try {
										themeKeywords.addKeyword(getValidValue(currentKeywordChild.getTextContent()));
									} catch (DOMException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
						themeKeywordList.add(themeKeywords);
					}
				}
			}
		}
		this.metadataParseResponse.metadata.setThemeKeywords(themeKeywordList);
		this.metadataParseResponse.metadata.setPlaceKeywords(placeKeywordList);
	}
			
			
			
	/*		
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
			//return getValidValue(tagValue);
		} catch (Exception e){
			numberOfParseWarnings++;
			missingParseTags.add(tagName);
			//throw new Exception(e.getMessage());
		}
	}*/


	/**
	 * get the content date which could be in one of three different tags
	 * 
	 * @param document
	 * @return
	 * @throws Exception 
	 */
	protected void handleDate(){
			String dateValue = null;
			try {
				dateValue = getDocumentValue("caldate");//timeperd>timeinfo>sngdate>caldate
			} catch (Exception e){
				try {
					dateValue = getDocumentValue("begdate");
				} catch (Exception e1){
					try {
						dateValue = getDocumentValue("dateStamp");
					} catch (Exception e2) {
						logger.warn("No valid Content Date could be found in the document.");
						this.metadataParseResponse.metadata.setContentDate(null);
						return;
					}
				}
			}
			try {
				logger.debug("DATE VALUE#######:" + dateValue);
				dateValue = processDateString(dateValue);
				this.metadataParseResponse.metadata.setContentDate(dateValue);
			} catch (Exception e) {
				try {
					dateValue = dateValue.substring(0, 3);
					int dateValueInt = Integer.parseInt(dateValue);
					dateValue = Integer.toString(dateValueInt);
					if (dateValue.length() == 4){
						this.metadataParseResponse.metadata.setContentDate(processDateString(dateValue));
					}
				} catch (Exception e1){
					logger.warn("No valid Content Date could be found in the document.");
					this.metadataParseResponse.metadata.setContentDate(null);
				}
			}
	}

	/**
	 * computing the data type for the layer involves looking at multiple fields
	 * in the document
	 * 
	 * @param document
	 * @param layerValues
	 * @throws Exception 
	 */
	protected void handleDataType() {
		String direct = null;// raster?
		String sdtsType = null;// vector type
		String srcCiteA = null;// scanned map
		
		try {
			srcCiteA = getDocumentValue("srccitea");
			if (srcCiteA.equalsIgnoreCase("Paper Map")){
				this.metadataParseResponse.metadata.setGeometryType(GeometryType.ScannedMap);
				return;
			} 
		} catch (Exception e){
			//just continue to next block
		}
		try {
			direct = getDocumentValue("direct");
			if (direct.equalsIgnoreCase("raster") == true) {
				this.metadataParseResponse.metadata.setGeometryType(GeometryType.Raster);
				return;
			}
		} catch (Exception e){
			//again, move to the next block
		}
		try {
			sdtsType = getDocumentValue("sdtstype");
			GeometryType solrType;
			if (sdtsType.equals("G-polygon") || sdtsType.contains("olygon")
						|| sdtsType.contains("chain")){
				solrType = GeometryType.Polygon;
			} else if (sdtsType.equals("Composite")
						|| sdtsType.contains("omposite")
						|| sdtsType.equals("Entity point")){
				solrType = GeometryType.Point;
			} else if (sdtsType.equals("String")){
					solrType = GeometryType.Line;
			} else{
					solrType = GeometryType.Undefined;
			}
			this.metadataParseResponse.metadata.setGeometryType(solrType);
		} catch (Exception e){
			logger.error("null geometry type");
			GeometryType solrType = GeometryType.Undefined;
			this.metadataParseResponse.metadata.setGeometryType(solrType);
			//we should make a note if the geometry type is undefined
		}
	}


	/**
	 * look at the document field associated with Key.Access (for FGDC:
	 * accconst) if the field starts with Restricted, the layer is restricted.
	 * Otherwise it is public.
	 * 
	 * @throws Exception 
	 */
	protected void handleAccess() {
		Tag tag = FgdcTag.Access;
		try {
			String accessValue$ = "";
			try{
				accessValue$ = getDocumentValue(tag);
			} catch (Exception e){
				AccessLevel nullAccess = null;
				this.metadataParseResponse.metadata.setAccessLevel(nullAccess);
				return;
			}
			AccessLevel accessValue = AccessLevel.Public;
			accessValue$ = accessValue$.toLowerCase();
			if (accessValue$.startsWith("restricted")){
				accessValue = AccessLevel.Restricted;
			}
			this.metadataParseResponse.metadata.setAccessLevel(accessValue);
		} catch (Exception e){
			logger.error("handleAccess: " + e.getMessage());
			this.metadataParseResponse.addError(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}
	}

	@Override
	void handleOriginator() {
		Tag tag = FgdcTag.Originator;
		try {
			this.metadataParseResponse.metadata.setOriginator(getDocumentValue(tag));
		} catch (Exception e) {
			logger.error("handleOriginator: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}		
	}

	@Override
	void handlePublisher(){
		Tag tag = FgdcTag.Publisher;
		try {
			this.metadataParseResponse.metadata.setPublisher(getDocumentValue(tag));
		} catch (Exception e) {
			logger.error("handlePublisher: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}		
	}

	@Override
	void handleLayerName() {
		Tag tag = FgdcTag.LayerName;
		try {
			this.metadataParseResponse.metadata.setOwsName(getDocumentValue(tag));
		} catch (Exception e) {
			logger.error("handleLayerName: " + e.getMessage());
			this.metadataParseResponse.addError(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}
	}

	@Override
	void handleAbstract() {
		Tag tag = FgdcTag.Abstract;
		try {
			this.metadataParseResponse.metadata.setDescription(getDocumentValue(tag));
		} catch (Exception e) {
			logger.error("handleAbstract: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}		
	}

	@Override
	void handleFullText() {
		this.metadataParseResponse.metadata.setFullText(getFullText());		
	}
}
