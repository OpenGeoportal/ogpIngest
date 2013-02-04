package org.OpenGeoPortal.Ingest.Metadata;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XmlMetadataParseMethodProvider implements MetadataParseMethodProvider, BeanFactoryAware {
	
	public static MetadataType getMetadataType(Document document) throws Exception {
		//could be useful elsewhere, so might go in a different class
		//Since we are not validating, this is a little kludgey
		MetadataType metadataType = null;
		try {
			//<metstdn>FGDC Content Standards for Digital Geospatial Metadata
			//<metstdv>FGDC-STD-001-1998
			if (document.getElementsByTagName("metstdn").item(0).getTextContent().toLowerCase().contains("fgdc")){
				metadataType = MetadataType.FGDC;
			}
		} catch (Exception e){/*ignore*/
			//document.getElementsByTagName("metstdn").item(0).getTextContent().toLowerCase();
		}

		try {
			//  <gmd:metadataStandardName>
			//  <gmd:spatialRepresentationInfo>
			  //<gmd:metadataStandardName>
			  //  <gco:CharacterString>ISO 19115:2003/19139</gco:CharacterString>
			  //</gmd:metadataStandardName>
			//existence of these two tags (ignoring namespace) should be good enough
			NodeList standardNodes = document.getElementsByTagNameNS("*", "metadataStandardName");
			if (standardNodes.getLength() > 0){
				if (standardNodes.item(0).getTextContent().contains("19139")){
					metadataType = MetadataType.ISO_19139;
				}
			}
		} catch (Exception e){/*ignore*/}

		if (metadataType == null){
			//throw an exception...metadata type is not supported
			throw new Exception("Metadata Type is not supported.");
		}
		return metadataType;
	}

	private BeanFactory beanFactory;

	public MetadataParseMethod getMetadataParseMethod(Document document) throws Exception {
		MetadataType metadataType = getMetadataType(document);
		MetadataParseMethod metadataParser = getMetadataParseMethod(metadataType);
		return metadataParser;
	}
	
	public MetadataParseMethod getMetadataParseMethod(MetadataType metadataType) {
		MetadataParseMethod metadataParseMethod = beanFactory.getBean("parseMethod." + metadataType.toString(), MetadataParseMethod.class);
		return metadataParseMethod;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}
