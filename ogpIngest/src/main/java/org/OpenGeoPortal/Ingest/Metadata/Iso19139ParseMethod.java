package org.OpenGeoPortal.Ingest.Metadata;

import org.OpenGeoPortal.Layer.AccessLevel;
import org.OpenGeoPortal.Layer.GeometryType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * parser for ISO 19139/19115
 * reads ISO metadata file to internal hashtable representation
 * 
 * this code isn't complete but works well enough to run searches for ISO layers
 * 
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
	/**
	 * from the gist data, I've only seen vector and grid values for MD_SpatialRepresentationTypeCode
	 * this function isn't complete 
	 * @throws Exception 
	 */
	@Override
	protected void handleDataType()
	{
		
		GeometryType geomType = null;
		String dataType = null;
			try {
				dataType = getAttributeValue("MD_SpatialRepresentationTypeCode", "codeListValue");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//        <gmd:MD_SpatialRepresentationTypeCode codeListValue="grid" codeList="http://www.isotc211.org/2005/resources/codeList.xml#MD_SpatialRepresentationTypeCode" />

		if (dataType != null)
		{
			if (dataType.equalsIgnoreCase("grid"))
				geomType = GeometryType.Raster;
			else if (dataType.equalsIgnoreCase("tin"))
				geomType = GeometryType.Polygon;
			else if (dataType.equalsIgnoreCase("vector"))
				geomType = GeometryType.Line;
			else 
				geomType = GeometryType.Undefined;
		}
		
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
		
	}

	@Override
	void handlePublisher() {
		//prefer CI_RoleCode publisher, distibutor, resourceProvider, custodian, processor
		
	}

	@Override
	void handleLayerName() {
		/*
		 * 
		 *   <gmd:fileIdentifier xmlns:srv="http://www.isotc211.org/2005/srv" xmlns:gmx="http://www.isotc211.org/2005/gmx">
    <gco:CharacterString>11334f95-ceee-44d9-b2f8-cd9daf08c427</gco:CharacterString>
  </gmd:fileIdentifier>
		 * 
		 */
		Tag tag = Iso19139Tag.LayerName;
		try {
			this.metadataParseResponse.metadata.setOwsName(getDocumentValue(tag));
		} catch (Exception e) {
			logger.error("handleLayerName: " + e.getMessage());
			this.metadataParseResponse.addError(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
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
		Tag tag = Iso19139Tag.Access;
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
	void handleKeywords() {
		/*
		 *       <gmd:topicCategory>
        <gmd:MD_TopicCategoryCode>environment</gmd:MD_TopicCategoryCode>
      </gmd:topicCategory>
		 */
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
