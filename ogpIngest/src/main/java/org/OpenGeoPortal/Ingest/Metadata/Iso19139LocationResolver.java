package org.OpenGeoPortal.Ingest.Metadata;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.OpenGeoPortal.Layer.LocationLink;
import org.OpenGeoPortal.Layer.LocationLink.LocationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Iso19139LocationResolver extends AbstractLocationResolver implements
		LocationResolver {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
 
	
	@Override
	public Set<LocationLink> resolveLocation(Document xmlDocument) {
		/*	<gmd:CI_OnlineResource>
		<gmd:linkage>
		<gmd:URL>http://www.fao.org/figis/geoserver/species/ows?SERVICE=WMS</gmd:URL>
		</gmd:linkage>
		<gmd:protocol>
		<gco:CharacterString>OGC:WMS-1.3.0-http-get-map</gco:CharacterString>
		</gmd:protocol>
		<gmd:name>
		<gco:CharacterString>SPECIES_DIST_AAO</gco:CharacterString>
		</gmd:name>
		<gmd:description>
		<gco:CharacterString>FAO aquatic species distribution map of Acipenser oxyrinchus</gco:CharacterString>
		</gmd:description>
		</gmd:CI_OnlineResource>
		</gmd:onLine>*/
		
		/*
		 * <gmd:graphicOverview><gmd:MD_BrowseGraphic><gmd:fileName><gco:CharacterString>thumbnail_s.gif</gco:CharacterString></gmd:fileName><gmd:fileDescription><gco:CharacterString>thumbnail</gco:CharacterString></gmd:fileDescription><gmd:fileType><gco:CharacterString>gif</gco:CharacterString></gmd:fileType></gmd:MD_BrowseGraphic></gmd:graphicOverview><gmd:graphicOverview><gmd:MD_BrowseGraphic><gmd:fileName><gco:CharacterString>thumbnail.gif</gco:CharacterString></gmd:fileName><gmd:fileDescription><gco:CharacterString>large_thumbnail</gco:CharacterString></gmd:fileDescription><gmd:fileType><gco:CharacterString>gif</gco:CharacterString></gmd:fileType></gmd:MD_BrowseGraphic></gmd:graphicOverview>
		 */
		Set<LocationLink> links = new HashSet<LocationLink>();
		links.addAll(getLinksFromCI_OnlineResource(xmlDocument));
		//look at the links, determine if it's an ows, zip, other filetype
		
		try {
			LocationLink browseLink = getBrowseLink(xmlDocument);
			links.add(browseLink);
		} catch (Exception e) {}
		logger.info("areLinks:" + Boolean.toString(!links.isEmpty()));
		return links;
	}
	
	
	/*
	 * 
	 <gmd:graphicOverview>
	<gmd:MD_BrowseGraphic>
	<gmd:fileName>
	<gco:CharacterString>http://www.fao.org/figis/geoserver/wms?service=WMS&amp;version=1.1.0&amp;request=GetMap&amp;layers=fifao:UN_CONTINENT,SPECIES_DIST_HXC&amp;bbox=-178.480041504,-56.531620026,179.576858521,74.20552063&amp;width=600&amp;height=219&amp;srs=EPSG:4326&amp;format=image%2Fpng</gco:CharacterString>
	</gmd:fileName>
	<gmd:fileDescription>
	<gco:CharacterString>FAO aquatic species distribution map of Chlamydoselachus anguineus</gco:CharacterString>
	</gmd:fileDescription>
	<gmd:fileType>
	<gco:CharacterString>image/png</gco:CharacterString>
	</gmd:fileType>
	</gmd:MD_BrowseGraphic>
	</gmd:graphicOverview>
	 * 
	 */
	public LocationLink getBrowseLink(Document xmlDocument) throws Exception {
		Set<LocationLink> linkValueSet = new HashSet<LocationLink>();
		NodeList linkNodes = xmlDocument.getElementsByTagNameNS("*", "MD_BrowseGraphic");
		for (int j = 0; j < linkNodes.getLength(); j++){
			NodeList children = linkNodes.item(j).getChildNodes();
			for (int i = 0; i < children.getLength(); i++){
				
			Node currentNode = children.item(i);
			String nodeName = currentNode.getNodeName();
			if (nodeName.toLowerCase().contains("fileName")){
				URL url = new URL(currentNode.getTextContent().trim());
				return new LocationLink(LocationType.browseGraphic, url);
			}
		}
		}
		throw new Exception("No browse graphic.");
	}
	
	public Set<LocationLink> getLinksFromCI_OnlineResource(Document xmlDocument){
		Set<LocationLink> linkValueSet = new HashSet<LocationLink>();
		NodeList linkNodes = xmlDocument.getElementsByTagNameNS("*", "CI_OnlineResource");
		//protocol
		//linkage
		//name
		for (int j = 0; j < linkNodes.getLength(); j++){
			NodeList children = linkNodes.item(j).getChildNodes();
			LocationType locType = null;
			URL url = null;
			String name = null;
			for (int i = 0; i < children.getLength(); i++){
				Node currentNode = children.item(i);
				String nodeName = currentNode.getNodeName();
				
				if (nodeName.toLowerCase().contains("protocol")){
					String protocol = currentNode.getTextContent().trim();
					//try to parse service type from protocol
					logger.info("Protocol: " + protocol);
					try {
						locType = parseServiceLocationType(protocol);
						logger.info("Location type: " + locType.toString());
					} catch (Exception e) {logger.info("Unable to parse protocol");}
				} else if (nodeName.toLowerCase().contains("linkage")){
					String url$ = currentNode.getTextContent().trim();
					logger.info("URL: " + url$);

					if (url$.startsWith("http")||url$.startsWith("ftp")){
						try {
							url = new URL(url$);
						} catch (MalformedURLException e) {}
					}
				} else if (nodeName.toLowerCase().contains("name")){
					name = currentNode.getTextContent().trim();
				}
			}
		if ((locType != null)&&(url != null)){
			LocationLink link = new LocationLink(locType, url);
			if (name != null){
				link.setResourceName(name);
			}
			linkValueSet.add(link);
		}
			
	}
		return linkValueSet;
	}
}
