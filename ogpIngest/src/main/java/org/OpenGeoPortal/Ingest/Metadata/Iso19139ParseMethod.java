package org.OpenGeoPortal.Ingest.Metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.OpenGeoPortal.Ingest.Metadata.TC211CodeListValues.ISO_CI_PresentationFormCode;
import org.OpenGeoPortal.Ingest.Metadata.TC211CodeListValues.ISO_MD_GeometricObjectTypeCode;
import org.OpenGeoPortal.Ingest.Metadata.TC211CodeListValues.ISO_MI_GeometryTypeCode;
import org.OpenGeoPortal.Layer.AccessLevel;
import org.OpenGeoPortal.Layer.GeometryType;
import org.OpenGeoPortal.Layer.PlaceKeywords;
import org.OpenGeoPortal.Layer.ThemeKeywords;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * parser for ISO 19139/19115
 * reads ISO metadata file to internal hashtable representation
 * 
 * this code isn't complete but works well enough to run searches for ISO layers
 * 
 * @author chrissbarnett
 * @author smcdon08
 *
 */
public class Iso19139ParseMethod extends AbstractXmlMetadataParseMethod implements MetadataParseMethod
{	
	public static enum Iso19139Tag implements Tag {
		Title("title", FieldType.Single), 
		Abstract("abstract", FieldType.Single), 
		LayerName("fileIdentifier", FieldType.Single), 
		Publisher("publish", FieldType.Multiple), 
		Originator("origin", FieldType.Multiple), 
		WestBc("westBoundLongitude", FieldType.Single), 
		EastBc("eastBoundLongitude", FieldType.Single), 
		NorthBc("northBoundLatitude", FieldType.Single), 
		SouthBc("southBoundLatitude",FieldType.Single), 
		KeywordsHeader("keywords", FieldType.Single), 
		PlaceKeywordsHeader("place", FieldType.Multiple), 
		ThemeKeywordsHeader("theme", FieldType.Multiple), 
		PlaceKeywordsThesaurus("placekt", FieldType.Multiple), 
		ThemeKeywordsThesaurus("themekt", FieldType.Multiple),
		PlaceKeywords("placekey", FieldType.Multiple), 
		ThemeKeywords("themekey", FieldType.Multiple),
		Access("accessConstraints",FieldType.Attribute);

		private final String tagName; // XML tag name
		private final FieldType fieldType; // Is there one of these tags, or more?
		
		Iso19139Tag(String tagName, FieldType fieldType) {
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

	protected List<String> getDistributorFormats(){			/*
		 //paper; no digital format
		<gmd:distributorFormat>
      <gmd:MD_Format>
        <gmd:name>
          <gco:CharacterString>No digital format</gco:CharacterString>
        </gmd:name>
        <gmd:version>
          <gco:CharacterString>n.a.</gco:CharacterString>
        </gmd:version>
      </gmd:MD_Format>
    </gmd:distributorFormat>*/
		List<String> distributorFormats = new ArrayList<String>();
		NodeList distFormatNodes = document.getElementsByTagNameNS("*", "distributorFormat");
		for (int i = 0; i < distFormatNodes.getLength(); i++){
			Node currentNode = distFormatNodes.item(i);
			NodeList MD_FormatNodes = currentNode.getChildNodes();
			for (int j=0; j< MD_FormatNodes.getLength(); j++){
				Node currentFormatNode = MD_FormatNodes.item(j);
				NodeList formatNodeDetails = currentFormatNode.getChildNodes();
				for (int k = 0; k < formatNodeDetails.getLength(); k++){
					Node currentDetailNode = formatNodeDetails.item(k);
					if (currentDetailNode.getLocalName().equals("name")){
						distributorFormats.add(currentDetailNode.getTextContent().trim().toLowerCase());
						break;
					}
				}
			}
		}
		return distributorFormats;
	}
	
	protected GeometryType convertCI_PresentationCodeToGeometryType(ISO_CI_PresentationFormCode codeValue) throws Exception{
		GeometryType geomType = GeometryType.Undefined;
		switch (codeValue) {
        	case imageDigital: 
        		geomType = GeometryType.Raster;
        		break;
        	case mapDigital: 
        		geomType = getMapDigitalGeometryType();
        		break;
        	case imageHardcopy: 
        	case mapHardcopy:
        		geomType = GeometryType.PaperMap;
        		break;
        	case documentDigital:
        	case documentHardcopy:
        		geomType = GeometryType.LibraryRecord;
        	default: 
        		//we don't know what to do with all these dataTypes right now.
        		geomType = GeometryType.Undefined;	
        		break;
		}

		return geomType;
	}
	/**
	 */
	@Override
	protected void handleDataType()
	{
		/*
		 <gmd:identificationInfo>
			<gmd:MD_DataIdentification>
  				<gmd:citation>
    				<gmd:CI_Citation>
    					<gmd:presentationForm>
        					<gmd:CI_PresentationFormCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_PresentationFormCode" codeListValue="mapHardcopy" />
      					</gmd:presentationForm>
		 */
		GeometryType geomType = null;		
		try {
			String rawDataType = getAttributeValue("CI_PresentationFormCode", "codeListValue");
			ISO_CI_PresentationFormCode codeValue = ISO_CI_PresentationFormCode.parseISOPresentationFormCode(rawDataType);
			setGeometryType(convertCI_PresentationCodeToGeometryType(codeValue));
		} catch (Exception e){
			List<String> distributorFormats = getDistributorFormats();
			if (distributorFormats.contains("no digital")){
				geomType = GeometryType.LibraryRecord;
			} else {
				geomType = GeometryType.Undefined;
			}
			setGeometryType(geomType);
		}
	}
		
	protected GeometryType getMapDigitalGeometryType(){
		String dataType = null;
		String xmlTag = null;
		GeometryType geomType = GeometryType.Undefined;
		try {
			xmlTag = "MD_SpatialRepresentationTypeCode";
			dataType = getAttributeValue(xmlTag, "codeListValue");
		} catch (Exception e) {
			try{
				xmlTag = "MD_TopologyLevelCode";
				dataType = getAttributeValue(xmlTag, "codeListValue");
			} catch (Exception e1){
				logger.info("Exception getting SpatialRepresentationTypeCode and MD_TopologyLevelCode: DataType:" + geomType.toString());
				return geomType;
			}
		}
//      <gmd:MD_SpatialRepresentationTypeCode codeListValue="grid" codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_SpatialRepresentationTypeCode" />
		/*
		vector	vector data is used to represent geographic data	MD_SpatialRepresentationTypeCode_vector
		grid	grid data is used to represent geographic data	MD_SpatialRepresentationTypeCode_grid
		textTable	textual or tabular data is used to represent geographic data	MD_SpatialRepresentationTypeCode_textTable
		tin	triangulated irregular network	MD_SpatialRepresentationTypeCode_tin
		stereoModel	three-dimensional view formed by the intersecting homologous rays of an overlapping pair of images	MD_SpatialRepresentationTypeCode_stereoModel
		video	scene from a video recording	MD_SpatialRepresentationTypeCode_video
		 */
		if (xmlTag.equals("MD_SpatialRepresentationTypeCode")){
			if (dataType.equalsIgnoreCase("grid"))
				geomType = GeometryType.Raster;
			else if (dataType.equalsIgnoreCase("tin"))
				geomType = GeometryType.Polygon;
			else if (dataType.equalsIgnoreCase("vector")){
				geomType = resolveVectorToGeometryType();
			} else 
				geomType = GeometryType.Undefined;
		} else if (xmlTag.equals("MD_TopologyLevelCode")){
			if (dataType.equalsIgnoreCase("geometryOnly")){
				geomType = resolveVectorToGeometryType();
			}
		}
		 logger.info("DataType:" + geomType.toString());
		return geomType;
	}
			
	protected GeometryType resolveVectorToGeometryType(){
		GeometryType geomType = GeometryType.Undefined;
		try {
			String code = this.getAttributeValue("MI_GeometryTypeCode", "codeListValue");
			geomType = convertMI_GeometryTypeCodeToGeometryType(ISO_MI_GeometryTypeCode.parseISO_MI_GeometryTypeCode(code));
		} catch (Exception e) {}
		if (geomType.equals(GeometryType.Undefined)){
			try {
				String code = this.getAttributeValue("MD_GeometricObjectTypeCode", "codeListValue");
				geomType = convertMD_GeometricObjectTypeCodeToGeometryType(ISO_MD_GeometricObjectTypeCode.parseISO_MD_GeometricObjectTypeCode(code));
			} catch (Exception e) {}
		}
		if (geomType.equals(GeometryType.Undefined)){
			//generic vector 
			geomType = GeometryType.Line;
		}
		return geomType;
	}
	
	protected GeometryType convertMD_GeometricObjectTypeCodeToGeometryType(ISO_MD_GeometricObjectTypeCode codeValue) throws Exception{
		GeometryType geomType = GeometryType.Undefined;
		switch (codeValue) {
        	case complex: 
        	case composite:
        	case surface:
        		geomType = GeometryType.Polygon;
        		break;
        	case curve: 
        		geomType = GeometryType.Line;
        		break;
        	case point: 
        		geomType = GeometryType.Point;
        		break;
        	default: 
        		geomType = GeometryType.Undefined;	
        		break;
		}

		return geomType;
	}		

	protected GeometryType convertMI_GeometryTypeCodeToGeometryType(ISO_MI_GeometryTypeCode codeValue) throws Exception{
		GeometryType geomType = GeometryType.Undefined;
		switch (codeValue) {
        	case areal:
        	case strip:
        		geomType = GeometryType.Polygon;
        		break;
        	case linear: 
        		geomType = GeometryType.Line;
        		break;
        	case point: 
        		geomType = GeometryType.Point;
        		break;
        	default: 
        		geomType = GeometryType.Undefined;	
        		break;
		}

		return geomType;
	}

	protected void setGeometryType(GeometryType geomType){
		try {
			this.metadataParseResponse.metadata.setGeometryType(geomType);
		} catch (Exception e) {
			logger.error("handleDataType: " + e.getMessage());
			this.metadataParseResponse.addError("DataType", "MD_SpatialRepresentationTypeCode", e.getClass().getName(), e.getMessage());
		}
	}
	
	@Override
	void handleOriginator() {
		//prefer CI_RoleCode originator, author, principalInvestigator, owner
/*
 * 1. CI_RoleCode RoleCd function performed by the responsible party
2. resourceProvider 001 party that supplies the resource
3. custodian 002 party that accepts accountability and responsibility for the resource
4. owner 003 party that owns the resource
5. user 004 party who uses the resource
6. distributor 005 party who distributes the resource
7. originator 006 party who created the resource
8. pointOfContact 007 party who can be contacted for acquiring knowledge about or acquisition of the resource
9. principal Investigator 008 key party responsible for gathering information and conducting research 
10. processor 009 party who has processed the data in a manner such that the resource has been modified
11. publisher 010 party who published the resource 
12. author 011 party who authored the resource 
13. sponsor party that sponsors the resource
14. collaborator
 * 
 * <gmd:contact>
    <gmd:CI_ResponsibleParty>
      <gmd:individualName>
        <gco:CharacterString>Francesca Perez</gco:CharacterString>
      </gmd:individualName>
      <gmd:organisationName>
        <gco:CharacterString>ITHACA - Information Technology for Humanitarian Assistance, Cooperation and Action</gco:CharacterString>
      </gmd:organisationName>
      <gmd:positionName gco:nilReason="missing">
        <gco:CharacterString />
      </gmd:positionName>
      <gmd:contactInfo>
        <gmd:CI_Contact>
          <gmd:phone>
            <gmd:CI_Telephone>
              <gmd:voice gco:nilReason="missing">
                <gco:CharacterString />
              </gmd:voice>
              <gmd:facsimile gco:nilReason="missing">
                <gco:CharacterString />
              </gmd:facsimile>
            </gmd:CI_Telephone>
          </gmd:phone>
          <gmd:address>
            <gmd:CI_Address>
              <gmd:deliveryPoint gco:nilReason="missing">
                <gco:CharacterString />
              </gmd:deliveryPoint>
              <gmd:city gco:nilReason="missing">
                <gco:CharacterString />
              </gmd:city>
              <gmd:administrativeArea gco:nilReason="missing">
                <gco:CharacterString />
              </gmd:administrativeArea>
              <gmd:postalCode gco:nilReason="missing">
                <gco:CharacterString />
              </gmd:postalCode>
              <gmd:country gco:nilReason="missing">
                <gco:CharacterString />
              </gmd:country>
              <gmd:electronicMailAddress>
                <gco:CharacterString>francesca.perez@polito.it</gco:CharacterString>
              </gmd:electronicMailAddress>
            </gmd:CI_Address>
          </gmd:address>
        </gmd:CI_Contact>
      </gmd:contactInfo>
      <gmd:role>
        <gmd:CI_RoleCode codeListValue="author" codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_RoleCode" />
      </gmd:role>
    </gmd:CI_ResponsibleParty>
  </gmd:contact>
 * 
 * 
 */
		Map<String, Node> originatorsTable = new HashMap<String,Node>();
		NodeList originators = document.getElementsByTagNameNS("*", "CI_RoleCode");
		for (int i = 0; i < originators.getLength(); i++){
			Node currentNode = originators.item(i);
			String roleCode = currentNode.getAttributes().getNamedItem("codeListValue").getNodeValue();
			originatorsTable.put(roleCode, currentNode);
			logger.debug("citation role: " + roleCode);
		}
		Node originatorNode = null;
		Node publisherNode = null;
		//prefer CI_RoleCode originator, author, principalInvestigator, owner
		List<String> originatorKeys = new ArrayList<String>();
		//in inverse order of preference
		originatorKeys.add("owner");
		originatorKeys.add("principalInvestigator");
		originatorKeys.add("author");
		originatorKeys.add("originator");
		
		List<String> publisherKeys = new ArrayList<String>();
		publisherKeys.add("processor");
		publisherKeys.add("custodian");
		publisherKeys.add("resourceProvider");
		publisherKeys.add("distributor");
		publisherKeys.add("publisher");

		List<String> preferredOriginatorKey = new ArrayList<String>();
		preferredOriginatorKey.addAll(publisherKeys);
		preferredOriginatorKey.addAll(originatorKeys);

		for (String key: preferredOriginatorKey){
			if (originatorsTable.containsKey(key)){
				originatorNode = originatorsTable.get(key);
			} 
		}
		String originatorValue = "";
		if (originatorNode != null){
			try {
				originatorValue = getCitationInfo(originatorNode);
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		
		//prefer CI_RoleCode publisher, distibutor, resourceProvider, custodian, processor
		List<String> preferredPublisherKey = new ArrayList<String>();
		preferredPublisherKey.addAll(originatorKeys);
		preferredPublisherKey.addAll(publisherKeys);

		for (String key: preferredPublisherKey){
			if (originatorsTable.containsKey(key)){
				publisherNode = originatorsTable.get(key);
			} 
		}
		String publisherValue = "";
		if (publisherNode != null){
			try {
				publisherValue = getCitationInfo(publisherNode);
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		try {
			this.metadataParseResponse.metadata.setOriginator(originatorValue.trim());
		} catch (Exception e) {
			logger.error("handleOriginator: " + e.getMessage());
			this.metadataParseResponse.addError("Originator", "CI_ResponsibleParty", e.getClass().getName(), e.getMessage());
		}
		try {
			this.metadataParseResponse.metadata.setPublisher(publisherValue.trim());
		} catch (Exception e) {
			logger.error("handlePublisher: " + e.getMessage());
			this.metadataParseResponse.addError("Publisher", "CI_ResponsibleParty", e.getClass().getName(), e.getMessage());
		}
	}

	String getCitationInfo(Node node) throws DOMException, Exception{
		Node parentNode = node.getParentNode().getParentNode();
		//    <gmd:CI_ResponsibleParty>
		logger.debug(parentNode.getNodeName());
		if (parentNode.getNodeName().contains("CI_ResponsibleParty")){
		      /*<gmd:individualName>
		        <gco:CharacterString>Francesca Perez</gco:CharacterString>
		      </gmd:individualName>
		      <gmd:organisationName>
		        <gco:CharacterString>ITHACA - Information Technology for Humanitarian Assistance, Cooperation and Action</gco:CharacterString>
		      </gmd:organisationName>
		      <gmd:positionName gco:nilReason="missing">
		        <gco:CharacterString />
		      </gmd:positionName>*/
			NodeList childNodes = parentNode.getChildNodes();
			String individualName = "";
			String positionName = "";
			for (int i = 0; i < childNodes.getLength(); i++){
				if (!positionName.isEmpty() && !individualName.isEmpty()){
					break;
				}
				Node currentNode = childNodes.item(i);
				String nodeName = currentNode.getNodeName();
				if (nodeName.contains("organisationName")){
					return getValidValue(currentNode.getTextContent());
				} else if (nodeName.contains("individualName")){
					individualName = getValidValue(currentNode.getTextContent());
				} else if (nodeName.contains("positionName")){
					positionName = getValidValue(currentNode.getTextContent());
				}
			}
			if (!individualName.isEmpty()){
				return individualName;
			} else {
				return positionName;
			}
		} else {
			return "";
		}
	}
	@Override
	void handlePublisher() {
		//don't do anything...we handle this in handleOriginator()
		
	}

	@Override
	String getLayerName() {
		/*
		 * 
		 *   <gmd:fileIdentifier xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gmx="http://www.isotc211.org/2005/gmx">
    <gco:CharacterString>11334f95-ceee-44d9-b2f8-cd9daf08c427</gco:CharacterString>
  </gmd:fileIdentifier>
		 * 
		 */
		Tag tag = Iso19139Tag.LayerName;
		try {
			return getDocumentValue(tag);
		} catch (Exception e) {
			logger.error("getLayerName: " + e.getMessage());
			return "";
		}
	}

	@Override
	void handleAbstract() {
	    /*  <gmd:abstract>
	        <gco:CharacterString>Global Volcano Proportional Economic Loss Risk Deciles is a 2.5 by 2.5 minute grid of volcano hazard economic loss as proportions of gross domestic product (GDP) per analytical unit. Estimates of GDP at risk are based on regional economic loss rates derived from historical records of the Emergency Events Database (EM-DAT). Loss rates are weighted by the hazard's frequency and distribution. The methodology of Sachs et al. (2003) is followed to determine baseline estimates of GDP per grid cell. To better reflect the confidence surrounding the data and procedures, the range of proportionalities is classified into deciles, 10 class of an approximately equal number of grid cells of increasing risk. The dataset is a result of the collaboration among the Center for Hazards and Risk Research (CHRR), International Bank for Reconstruction and Development/The World Bank, and the Columbia University Center for International Earth Science Information Network (CIESIN).
	The Center for Hazards and Risk Research (CHRR) draws on Columbia's acknowledged expertise in Earth and environmental sciences, engineering, social sciences, public policy, public health and business. The mission is to advance the predictive science of natural and environmental hazards and the integration of science with hazard risk assessment and risk management. The CHRR produced the core dataset of Natural Disaster Hotspots – A global risk analysis, which contains 4 libraries of data: Hazard Frequency and/or Distribution; Hazard Mortality Risks and Distribution; Hazard Total Economic Loss Risk Deciles; Hazard Proportional Economic Loss Risk Deciles for each natural disaster.</gco:CharacterString>
	      </gmd:abstract>*/
		Tag tag = Iso19139Tag.Abstract;
		try {
			this.metadataParseResponse.metadata.setDescription(getDocumentValue(tag));
		} catch (Exception e) {
			logger.error("handleAbstract: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}	
		
	}

	protected String getAttributeValue(String tagName, String attributeName) throws Exception{
		String attrValue = "";
		NodeList nodes = document.getElementsByTagNameNS("*", tagName);
		logger.debug(" tagName = " + tagName + " nodes length = " + nodes.getLength());
		if (nodes.getLength() == 0){
			attrValue = null;
		}
		attrValue = nodes.item(0).getAttributes().getNamedItem(attributeName).getNodeValue();
		return attrValue;
	}
	
	@Override
	/**
	 * return the first value for the passed tag
	 * @param tagName
	 * @return
	 * @throws Exception 
	 */
	protected String getDocumentValue(String tagName) throws Exception
	{
		String tagValue = "";
		NodeList nodes = document.getElementsByTagNameNS("*", tagName);
		logger.debug(" tagName = " + tagName + " nodes length = " + nodes.getLength());
		if (nodes.getLength() == 0){
			tagValue = null;
		} else {
			outerloop:
			for (int i = 0; i < nodes.getLength(); i++){
				Node currentNode = nodes.item(i);
				short nodeType = currentNode.getNodeType();
				logger.debug("OUTER LOOP");
				logger.debug(currentNode.getNodeName());
				logger.debug(Short.toString(currentNode.getNodeType()));
				logger.debug(currentNode.getNodeValue());
				logger.debug(currentNode.getTextContent());

				if (nodeType == Node.TEXT_NODE){
					tagValue = currentNode.getTextContent();
					if (!tagValue.trim().isEmpty()){
						logger.debug("<" + tagName + ">:" + tagValue);
						break;//found a text node, so exit
					}
				}
				if (currentNode.hasChildNodes()){
					NodeList children = currentNode.getChildNodes();
					for (int j = 0; j < children.getLength(); j++){
						Node currentChild = children.item(j);
						logger.debug("INNER LOOP");
						logger.debug(currentChild.getNodeName());
						logger.debug(Short.toString(currentChild.getNodeType()));
						logger.debug(currentChild.getNodeValue());
						logger.debug(currentChild.getTextContent());
						short childNodeType = currentChild.getNodeType();
						if (childNodeType == Node.TEXT_NODE){
							tagValue = currentChild.getNodeValue();
							if (!tagValue.trim().isEmpty()){
								logger.debug("<" + tagName + ">:" + tagValue);
								break outerloop;//found a text node, so exit
							}
						}
						if (currentChild.hasChildNodes()){
							NodeList grandChildren = currentChild.getChildNodes();
							for (int k = 0; k < grandChildren.getLength(); k++){
								Node currentGrandChild = grandChildren.item(k);
								logger.debug("INNER INNER LOOP");
								logger.debug(currentGrandChild.getNodeName());
								logger.debug(Short.toString(currentGrandChild.getNodeType()));
								logger.debug(currentGrandChild.getNodeValue());
								logger.debug(currentGrandChild.getTextContent());
								short grandChildNodeType = currentGrandChild.getNodeType();
								if (grandChildNodeType == Node.TEXT_NODE){
									tagValue = currentGrandChild.getNodeValue();
									if (!tagValue.trim().isEmpty()){
										logger.debug("<" + tagName + ">:" + tagValue);
										break outerloop;//found a text node, so exit
									}
								}
							}
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
	
	@Override
	void handleTitle() {
       /* <gmd:title>
        <gco:CharacterString>Global Volcano Proportional Economic Loss Risk Deciles_CHRR</gco:CharacterString>
      </gmd:title>*/
		Tag tag = Iso19139Tag.Title;
		try{
			this.metadataParseResponse.metadata.setTitle(getDocumentValue(tag));
		} catch (Exception e){
			logger.error("handleTitle: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}
		
	}

	@Override
	void handleDate() {
/*
 *           <gmd:date>
            <gmd:CI_Date>
              <gmd:date>
                <gco:DateTime>2005-03-03T09:00:00</gco:DateTime>
              </gmd:date>
              <gmd:dateType>
                <gmd:CI_DateTypeCode codeListValue="publication" codeList="http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode" />
              </gmd:dateType>
            </gmd:CI_Date>
          </gmd:date>
 */
		try{
			this.metadataParseResponse.metadata.setContentDate(getDocumentValue("DateTime").substring(0, 4));
		} catch (Exception e){
			logger.error("handleDate: " + e.getMessage());
			this.metadataParseResponse.addWarning("date", "date", e.getClass().getName(), e.getMessage());
		}
		
	}

	@Override
	void handleAccess() {
/*
 *       <gmd:resourceConstraints>
        <gmd:MD_LegalConstraints>
          <gmd:accessConstraints>
            <gmd:MD_RestrictionCode codeListValue="" codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode" />
          </gmd:accessConstraints>
          <gmd:useConstraints>
            <gmd:MD_RestrictionCode codeListValue="intellectualPropertyRights" codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_RestrictionCode" />
          </gmd:useConstraints>
          <gmd:otherConstraints>
            <gco:CharacterString>The Trustees of Columbia University in the City of New York, the Center for Hazards and Risk Research (CHRR), and the Center for International Earth Science Information Network (CIESIN) hold the copyright of this dataset. Users are prohibited from any commercial, non-free resale, or redistribution without explicit written permission from CHRR or CIESIN. Use of this data set is restricted to scientific research only. Users should acknowledge CHRR and CIESIN as the source used in the creation of any reports, publications, new data sets, derived products, or services resulting from the use of this data set. CHRR and CIESIN also request reprints of any publications and notification of any redistribution efforts.</gco:CharacterString>
          </gmd:otherConstraints>
        </gmd:MD_LegalConstraints>
      </gmd:resourceConstraints>
 */
		String accessValue$ = "";
		try {
			accessValue$ = getAttributeValue("MD_RestrictionCode", "codeListValue");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Tag tag = Iso19139Tag.Access;
		try {
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
	void handleKeywords() {
		/*
		 *       <gmd:topicCategory>
        <gmd:MD_TopicCategoryCode>environment</gmd:MD_TopicCategoryCode>
      </gmd:topicCategory>
		 */
		List<ThemeKeywords> themeKeywordList = new ArrayList<ThemeKeywords>();
		List<PlaceKeywords> placeKeywordList = new ArrayList<PlaceKeywords>();
		//add the iso theme keyword
		try {
			ThemeKeywords isoThemeKeyword = new ThemeKeywords();
			isoThemeKeyword.setThesaurus("ISO 19115");
			isoThemeKeyword.addKeyword(this.getDocumentValue("topicCategory"));
			themeKeywordList.add(isoThemeKeyword);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("No ISO topic category found");
		}
/*
 *       <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <gmd:keyword>
            <gco:CharacterString>Natural risk zones</gco:CharacterString>
          </gmd:keyword>
          <gmd:type>
            <gmd:MD_KeywordTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode" codeListValue="theme" />
          </gmd:type>
          <gmd:thesaurusName>
            <gmd:CI_Citation>
              <gmd:title>
                <gco:CharacterString>GEMET - INSPIRE themes, version 1.0</gco:CharacterString>
              </gmd:title>
              <gmd:date gco:nilReason="unknown" />
            </gmd:CI_Citation>
          </gmd:thesaurusName>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
      <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <gmd:keyword>
            <gco:CharacterString>Natural Disaster</gco:CharacterString>
          </gmd:keyword>
          <gmd:type>
            <gmd:MD_KeywordTypeCode codeListValue="theme" codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode" />
          </gmd:type>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
      <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <gmd:keyword>
            <gco:CharacterString>Volcano</gco:CharacterString>
          </gmd:keyword>
          <gmd:type>
            <gmd:MD_KeywordTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode" codeListValue="theme" />
          </gmd:type>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
      <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <gmd:keyword>
            <gco:CharacterString>Economic Risks</gco:CharacterString>
          </gmd:keyword>
          <gmd:type>
            <gmd:MD_KeywordTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode" codeListValue="" />
          </gmd:type>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
      <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <gmd:keyword>
            <gco:CharacterString>Economic Loss</gco:CharacterString>
          </gmd:keyword>
          <gmd:type>
            <gmd:MD_KeywordTypeCode codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode" codeListValue="" />
          </gmd:type>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
      <gmd:descriptiveKeywords>
        <gmd:MD_Keywords>
          <gmd:keyword>
            <gco:CharacterString>World</gco:CharacterString>
          </gmd:keyword>
          <gmd:type>
            <gmd:MD_KeywordTypeCode codeListValue="place" codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_KeywordTypeCode" />
          </gmd:type>
        </gmd:MD_Keywords>
      </gmd:descriptiveKeywords>
 */
		
		NodeList keywordNodes = document.getElementsByTagNameNS("*", "descriptiveKeywords");
		for (int i = 0; i < keywordNodes.getLength(); i++){
			Node currentNode = keywordNodes.item(i);
			NodeList descriptiveKeywordsChildren = currentNode.getChildNodes();
			Node mDKeywordsNode = null;
			for (int k = 0; k < descriptiveKeywordsChildren.getLength(); k++){
				Node currentDescKeywords = descriptiveKeywordsChildren.item(k);
				if (currentDescKeywords.getNodeName().toLowerCase().contains("md_keywords")){
					mDKeywordsNode = currentDescKeywords;
					break;
				}
			}
			NodeList keywordDetailNodes = mDKeywordsNode.getChildNodes();
			String keywordValue = "";
			String keywordType = "";
			for (int j = 0; j < keywordDetailNodes.getLength(); j++){
				Node currentDetailNode = keywordDetailNodes.item(j);

				if (currentDetailNode.getNodeName().contains("keyword")){
					keywordValue = currentDetailNode.getTextContent().trim();
					logger.debug("keyword value: " + keywordValue);
				} else if (currentDetailNode.getNodeName().contains("type")){
					NodeList keywordTypeNodes = currentDetailNode.getChildNodes();
					for (int n = 0; n < keywordTypeNodes.getLength(); n++){
						Node currentTypeNode = keywordTypeNodes.item(n);
						if (currentTypeNode.getNodeName().toLowerCase().contains("md_keywordtypecode")){
							keywordType = currentTypeNode.getAttributes().getNamedItem("codeListValue").getNodeValue();
						}
					}		
							
					logger.debug("keyword type: " + keywordType);
				}
				//keyword CharacterString
				//type  MD_KeywordTypeCode attr: codeListValue
				//thesaurus Name ...skip this for now
		         /* <gmd:thesaurusName>
		            <gmd:CI_Citation>
		              <gmd:title>
		                <gco:CharacterString>GEMET - INSPIRE themes, version 1.0</gco:CharacterString>
		              </gmd:title>
		              <gmd:date gco:nilReason="unknown" />
		            </gmd:CI_Citation>
		          </gmd:thesaurusName>
		          */
			}
			if (!keywordValue.isEmpty()){
				if (keywordType.equalsIgnoreCase("place")){
					PlaceKeywords placeKeywords = new PlaceKeywords();
					placeKeywords.setThesaurus("unspecified");
					placeKeywords.addKeyword(keywordValue);
					placeKeywordList.add(placeKeywords);
				} else {
					ThemeKeywords themeKeywords = new ThemeKeywords();
					themeKeywords.setThesaurus("unspecified");
					themeKeywords.addKeyword(keywordValue);
					themeKeywordList.add(themeKeywords);
				}
			}
		}

		this.metadataParseResponse.metadata.setThemeKeywords(themeKeywordList);
		this.metadataParseResponse.metadata.setPlaceKeywords(placeKeywordList);
	}

	@Override
	void handleBounds() {
/*
 *       <gmd:extent>
        <gmd:EX_Extent>
          <gmd:geographicElement>
            <gmd:EX_GeographicBoundingBox>
              <gmd:westBoundLongitude>
                <gco:Decimal>-180</gco:Decimal>
              </gmd:westBoundLongitude>
              <gmd:eastBoundLongitude>
                <gco:Decimal>180</gco:Decimal>
              </gmd:eastBoundLongitude>
              <gmd:southBoundLatitude>
                <gco:Decimal>-90</gco:Decimal>
              </gmd:southBoundLatitude>
              <gmd:northBoundLatitude>
                <gco:Decimal>90</gco:Decimal>
              </gmd:northBoundLatitude>
            </gmd:EX_GeographicBoundingBox>
          </gmd:geographicElement>
        </gmd:EX_Extent>
      </gmd:extent>
 */
		Tag tag = Iso19139Tag.NorthBc;
		try{
			String maxY = getDocumentValue(tag);
			tag = Iso19139Tag.EastBc;
			String maxX = getDocumentValue(tag);
			tag = Iso19139Tag.SouthBc;
			String minY = getDocumentValue(tag);
			tag = Iso19139Tag.WestBc;
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

	@Override
	void handleFullText() {
		this.metadataParseResponse.metadata.setFullText(getFullText());				
	}

}
