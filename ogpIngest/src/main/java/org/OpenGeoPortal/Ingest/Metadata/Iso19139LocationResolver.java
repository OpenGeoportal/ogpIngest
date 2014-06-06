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
	
	/*
	 * 
	 * 
	 * <gmd:transferOptions>
<gmd:MD_DigitalTransferOptions id="http___services_azgs_az_gov_ArcGIS_services_aasggeothermal_AZThermalSprings_MapServer_WFSServer_request_GetCapabilities_service_WFS">
<gmd:onLine>
<gmd:CI_OnlineResource>
<gmd:linkage>
<gmd:URL>http://services.azgs.az.gov/ArcGIS/services/aasggeothermal/AZThermalSprings/MapServer/WFSServer?request=GetCapabilities&amp;service=WFS</gmd:URL>
</gmd:linkage>
<gmd:protocol>
<gco:CharacterString>OGC:WFS</gco:CharacterString>
</gmd:protocol>
<gmd:name>
<gco:CharacterString>Service Description</gco:CharacterString>
</gmd:name>
<gmd:description>
<gco:CharacterString> parameters: {"featureTypes": "OGC:WFS"}</gco:CharacterString>
</gmd:description>
<gmd:function>
<gmd:CI_OnLineFunctionCode codeList="http://www.fgdc.gov/nap/metadata/register/registerItemClasses.html#IC_88" codeListValue="381">webService</gmd:CI_OnLineFunctionCode>
</gmd:function>
</gmd:CI_OnlineResource>
</gmd:onLine>
</gmd:MD_DigitalTransferOptions>
</gmd:transferOptions>
<gmd:transferOptions>
<gmd:MD_DigitalTransferOptions id="http___services_azgs_az_gov_ArcGIS_services_aasggeothermal_AZThermalSprings_MapServer_WMSServer_request_GetCapabilities_service_WMS">
<gmd:onLine>
<gmd:CI_OnlineResource>
<gmd:linkage>
<gmd:URL>http://services.azgs.az.gov/ArcGIS/services/aasggeothermal/AZThermalSprings/MapServer/WMSServer?request=GetCapabilities&amp;service=WMS</gmd:URL>
</gmd:linkage>
<gmd:protocol>
<gco:CharacterString>OGC:WMS</gco:CharacterString>
</gmd:protocol>
<gmd:name>
<gco:CharacterString>Service Description</gco:CharacterString>
</gmd:name>
<gmd:description>
<gco:CharacterString> parameters: {"layers": "OGC:WMS"}</gco:CharacterString>
</gmd:description>
<gmd:function>
<gmd:CI_OnLineFunctionCode codeList="http://www.fgdc.gov/nap/metadata/register/registerItemClasses.html#IC_88" codeListValue="381">webService</gmd:CI_OnLineFunctionCode>
</gmd:function>
</gmd:CI_OnlineResource>
</gmd:onLine>
</gmd:MD_DigitalTransferOptions>
</gmd:transferOptions>
<gmd:transferOptions>
<gmd:MD_DigitalTransferOptions id="http___services_azgs_az_gov_ArcGIS_rest_services_aasggeothermal_AZThermalSprings_MapServer">
<gmd:onLine>
<gmd:CI_OnlineResource>
<gmd:linkage>
<gmd:URL>http://services.azgs.az.gov/ArcGIS/rest/services/aasggeothermal/AZThermalSprings/MapServer</gmd:URL>
</gmd:linkage>
<gmd:protocol>
<gco:CharacterString>ESRI</gco:CharacterString>
</gmd:protocol>
<gmd:name>
<gco:CharacterString>Service Description</gco:CharacterString>
</gmd:name>
<gmd:function>
<gmd:CI_OnLineFunctionCode codeList="http://www.fgdc.gov/nap/metadata/register/registerItemClasses.html#IC_88" codeListValue="381">webService</gmd:CI_OnLineFunctionCode>
</gmd:function>
</gmd:CI_OnlineResource>
</gmd:onLine>
</gmd:MD_DigitalTransferOptions>
</gmd:transferOptions>
<gmd:transferOptions>
<gmd:MD_DigitalTransferOptions id="http___repository_stategeothermaldata_org_metadata_record_4e6b8f72f7d6c3856f092c6b85018dd1_file_azthermalsprings20131118_zip">
<gmd:onLine>
<gmd:CI_OnlineResource>
<gmd:linkage>
<gmd:URL>http://repository.stategeothermaldata.org/metadata/record/4e6b8f72f7d6c3856f092c6b85018dd1/file/azthermalsprings20131118.zip</gmd:URL>
</gmd:linkage>
<gmd:name>
<gco:CharacterString>Zipped Excel file containing Thermal Springs data for the state of Arizona</gco:CharacterString>
</gmd:name>
<gmd:function>
<gmd:CI_OnLineFunctionCode codeList="http://www.fgdc.gov/nap/metadata/register/registerItemClasses.html#IC_88" codeListValue="375">download</gmd:CI_OnLineFunctionCode>
</gmd:function>
</gmd:CI_OnlineResource>
</gmd:onLine>
</gmd:MD_DigitalTransferOptions>
</gmd:transferOptions>
<gmd:transferOptions>
<gmd:MD_DigitalTransferOptions id="http___notifications_usgin_org_">
<gmd:onLine>
<gmd:CI_OnlineResource>
<gmd:linkage>
<gmd:URL>http://notifications.usgin.org/</gmd:URL>
</gmd:linkage>
<gmd:name>
<gco:CharacterString>NGDS RSS feed for services notifications</gco:CharacterString>
</gmd:name>
<gmd:function>
<gmd:CI_OnLineFunctionCode codeList="http://www.fgdc.gov/nap/metadata/register/registerItemClasses.html#IC_88" codeListValue="375">download</gmd:CI_OnLineFunctionCode>
</gmd:function>
</gmd:CI_OnlineResource>
</gmd:onLine>
</gmd:MD_DigitalTransferOptions>
</gmd:transferOptions>

	 * 
	 * 
	 * 
	 * 
	 */
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
