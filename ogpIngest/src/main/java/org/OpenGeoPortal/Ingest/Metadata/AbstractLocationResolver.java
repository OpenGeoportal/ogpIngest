package org.OpenGeoPortal.Ingest.Metadata;

import java.util.Set;

import org.OpenGeoPortal.Layer.LocationLink;
import org.OpenGeoPortal.Layer.LocationLink.LocationType;
import org.w3c.dom.Document;

public abstract class AbstractLocationResolver implements LocationResolver {

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
	//wms
	//wfs
	//wcs
	//tilecache
	//arcGISRest
	//browseGraphic
	//non-georeferenced
	/*
	 * 	Here is what we want:
http://linuxdev.lib.berkeley.edu:8080/geoserver/UCB/wms?service=WMS&version=1.1.0&request=GetMap&layers=UCB:images&CQL_FILTER=PATH=%27furtwangler_sid/17076013_01_001a_s.sid%27&styles=&bbox=0.0,-65536.0,65536.0,0.0&width=512&height=512&srs=EPSG:404000&format=application/openlayers

	Here is what we get:
	fileName: 17076013_07_072a.tif
	location:
{"imageCollection": [{"collection": "UCB:images", "path": "furtwangler", "url": "http://linuxdev.lib.berkeley.edu:8080/geoserver/UCB/wms", collectionurl: "http://www.lib.berkeley.edu/EART/mapviewer/collections/histoposf/"}]}
	 */
	//serviceStart
	//zipFile
	//download
	
	public LocationType parseServiceLocationType(String link) throws Exception{
		link = link.trim();
		/*wms,
		wfs,
		wcs,
		//tilecache,
		imageCollection,
		ArcGISRest,
		//serviceStart,
		//zipFile,?
		download;*/
		if (isWMS(link)){
			return LocationType.wms;
		} else if (isWFS(link)){
			return LocationType.wfs;
		} else if (isWCS(link)){
			return LocationType.wcs;
		} else if (isArcGISRest(link)){
			return LocationType.ArcGISRest;
		} else if (isFile(link)){
			return LocationType.fileDownload;
		} else {
			throw new Exception("link type not supported.");
		}
		
	}
	
	public Boolean isWMS(String link){
		if (link.toLowerCase().contains("wms")){
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isWFS(String link){
		if (link.toLowerCase().contains("wfs")){
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isWCS(String link){
		if (link.toLowerCase().contains("wcs")){
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isArcGISRest(String link){
		if (link.toLowerCase().contains("arcgis/rest")){
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isFile(String link){
		if (link.startsWith("ftp")){
			//must be zipFile, download
			return true;
		}
		if (link.endsWith(".zip"))
			return true;
		if (link.endsWith(".gz"))
			return true;
		if (link.toLowerCase().contains("download"))
			return true;
		return false;
	}
	
	public abstract Set<LocationLink> resolveLocation(Document xmlDocument);

}
