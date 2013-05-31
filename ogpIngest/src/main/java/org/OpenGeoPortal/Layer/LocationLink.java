package org.OpenGeoPortal.Layer;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationLink {
	public static enum LocationType {
		wms(true),
		wfs(false),
		wcs(false),
		tilecache(true),
		imageCollection(false),
		ArcGISRest(false),
		browseGraphic(false),
		serviceStart(false),
		//zipFile,
		mapRecord(false),
		libRecord(false),
		fileDownload(true),
		download(false);
		
		LocationType(Boolean isArray){
			this.isArray = isArray;
		}
		public final Boolean isArray;
		
		public static LocationType fromString(String locationTypeString) throws Exception{
			for (LocationType locType: LocationType.values()){
				if (locationTypeString.toLowerCase().contains(locType.toString().toLowerCase())){
					return locType;
				}
			}
			throw new Exception("LocationType could not be resolved from: '" + locationTypeString + "'");
		}
	}
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
		public final LocationType locationType;
		public URL url;
		public String resourceName;

		public LocationLink(LocationType locationType, URL url){
			this.locationType = locationType;
			this.url = url;
			logger.info("LocationType: " + locationType.toString());
			logger.info("URL: " + url.toString());
		}
		
		public LocationType getLocationType(){
			return locationType;
		}
		
		public URL getURL(){
			return url;
		}
		
		public void setURL(URL url){
			this.url = url;
		}
		
		public void setURL(String url) throws MalformedURLException{
			this.url = new URL(url);
		}
		
		public String getResourceName() {
			return resourceName;
		}

		public void setResourceName(String resourceName) {
			this.resourceName = resourceName;
		}
		
		public String toString(){
			if (locationType.isArray){
				return "\"" + locationType.toString() + "\": [\"" + url.toString() + "\"]";
			} else {
				return "\"" + locationType.toString() + "\": \"" + url.toString() + "\"";
			}
		}
		
	
}
