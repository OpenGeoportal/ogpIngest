package org.OpenGeoPortal.Ingest;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.OpenGeoPortal.Layer.GeometryType;
import org.OpenGeoPortal.Layer.LocationLink;
import org.OpenGeoPortal.Layer.Metadata;
import org.OpenGeoPortal.Layer.LocationLink.LocationType;
import org.OpenGeoPortal.Utilities.PropertyFileProperties;

public class PropertyFileIngestProperties extends PropertyFileProperties implements IngestProperties {
	public String getWorkspace(Metadata metadata, String institution) throws IOException{
		institution = institution.toLowerCase();
		String workspaceLogic = null;
		try {
			workspaceLogic = getProperty(institution + ".workspaceLogic");
		} catch (NullPointerException e){}
		
		String prefix = institution;
		if (workspaceLogic != null){
			logger.info("workspaceLogic: " + workspaceLogic);
			String logicString = "";
			if (workspaceLogic.equalsIgnoreCase("access")){
				logicString = metadata.getAccess().toString().toLowerCase();
			} else if (workspaceLogic.equalsIgnoreCase("dataType")){
				if (GeometryType.isRaster(metadata.getGeometryType())){
					logicString += "raster";
				} else if (GeometryType.isRaster(metadata.getGeometryType())){
					logicString += "vector";
				} else {
					logicString += "vector";//if the dataType can't be guessed, guess vector
				}
			}
			prefix += "." + logicString;
			logger.debug(prefix + ".GeoServerWorkspace");
			return getProperty(prefix + ".GeoServerWorkspace");
		} else {
			return ""; //no workspace
		}

	}
	
	public Set<LocationLink> getLocation(Metadata metadata) throws IOException {
		Set<LocationLink> links = new HashSet<LocationLink>();
		try{
		String locationPrefix = metadata.getInstitution().toLowerCase() + ".location.";
		String accessString = metadata.getAccess().toString().toLowerCase();
		//Map<String,String> locationMap = new HashMap<String,String>();
		GeometryType dataType = metadata.getGeometryType();
		Properties properties = getProperties();
		Set<String> propertyNames = properties.stringPropertyNames();
		for (String key: propertyNames){
			//we only want location info for our layer's institution
			if (key.startsWith(locationPrefix)){
				if (key.contains("geoserver")){
					handleLocationGeoserver(key, accessString, dataType, links);
				} else if (key.contains("wms")){
					handleLocationWms(key, accessString, links);
				} else if (key.contains("wfs")){
					handleLocationWfs(key, accessString, dataType, links);
				} else if (key.contains("wcs")){
					handleLocationWcs(key, accessString, dataType, links);
				} else if (key.contains("tilecache")){
					handleLocationTilecache(key, accessString, links);
				} else if (key.contains("serviceStart")){
					handleLocationServiceStart(key, links);
				} else if (key.contains("download")){
					handleLocationDownload(key, links);
				}
			}
		}		
		return links;

		} catch (Exception e){
			throw new IOException();
		}
	}
	
	/*public String getLocation(Metadata metadata) throws IOException {
		try{
		String locationPrefix = metadata.getInstitution().toLowerCase() + ".location.";
		String accessString = metadata.getAccess().toString().toLowerCase();
		Map<String,String> locationMap = new HashMap<String,String>();
		GeometryType dataType = metadata.getGeometryType();
		Properties properties = getProperties();
		Set<String> propertyNames = properties.stringPropertyNames();
		for (String key: propertyNames){
			//we only want location info for our layer's institution
			if (key.startsWith(locationPrefix)){
				if (key.contains("geoserver")){
					handleLocationGeoserver(key, accessString, dataType, locationMap);
				} else if (key.contains("wms")){
					handleLocationWms(key, accessString, locationMap);
				} else if (key.contains("wfs")){
					handleLocationWfs(key, accessString, dataType, locationMap);
				} else if (key.contains("wcs")){
					handleLocationWcs(key, accessString, dataType, locationMap);
				} else if (key.contains("tilecache")){
					handleLocationTilecache(key, accessString, locationMap);
				} else if (key.contains("serviceStart")){
					handleLocationServiceStart(key, locationMap);
				} else if (key.contains("download")){
					handleLocationDownload(key, locationMap);
				}
			}
		}
		String locationString = "";
		for (String protocol: locationMap.keySet()){
			String currentLocation = locationMap.get(protocol);
			if (currentLocation.startsWith("[")){
				locationString += "\"" + protocol + "\":" + currentLocation + ",";
			} else {
				locationString += "\"" + protocol + "\":\"" + currentLocation + "\",";
			}
		}
		if (!locationString.isEmpty()){
			locationString = "{" + locationString.substring(0, locationString.length() - 1) + "}";
		}
		return locationString;
		} catch (Exception e){
			throw new IOException();
		}
	}*/

	private void handleLocationTilecache(String key, String accessString,
			Set<LocationLink> links) throws IOException {
		String[] keyArray = key.split("\\.");
		String rawLocationString = null;
		if (keyArray[2].equalsIgnoreCase("tilecache")){
			//no access info
			rawLocationString = getProperty(key);
			links.add(new LocationLink(LocationType.tilecache, new URL(rawLocationString)));
		} else if(keyArray[2].equalsIgnoreCase(accessString)){
			rawLocationString = getProperty(key);
			links.add(new LocationLink(LocationType.tilecache, new URL(rawLocationString)));
		}
	}
	
	private void handleLocationServiceStart(String key,
			Set<LocationLink> links) throws IOException {
		links.add(new LocationLink(LocationType.serviceStart, new URL(getProperty(key))));
	}
	
	private void handleLocationDownload(String key,
			Set<LocationLink> links) throws IOException {
		links.add(new LocationLink(LocationType.download, new URL(getProperty(key))));
	}

	private void handleLocationWfs(String key, String accessString,
			GeometryType dataType, Set<LocationLink> links) throws IOException {
		String[] keyArray = key.split("\\.");
		if (GeometryType.isVector(dataType)){
			String locationString = null;
			if (keyArray[2].equalsIgnoreCase("wfs")){
				//no access info
				String rawLocationString = getProperty(key);
				String[] locationArray = rawLocationString.split(",");
				locationString = locationArray[0];
				links.add(new LocationLink(LocationType.wfs, new URL(locationString + "/wfs")));
			} else if(keyArray[2].equalsIgnoreCase(accessString)){
				String rawLocationString = getProperty(key);
				String[] locationArray = rawLocationString.split(",");
				locationString = locationArray[0];
				links.add(new LocationLink(LocationType.wfs, new URL(locationString + "/wfs")));
			}
		}
		
	}
	
	private void handleLocationWcs(String key, String accessString,
			GeometryType dataType, Set<LocationLink> links) throws IOException {
		String[] keyArray = key.split("\\.");
		if (GeometryType.isRaster(dataType)){
			if (keyArray[2].equalsIgnoreCase("wcs")){
				//no access info
				String rawLocationString = getProperty(key);
				String[] locationArray = rawLocationString.split(",");
				String locationString = locationArray[0];
				links.add(new LocationLink(LocationType.wcs, new URL(locationString)));
			} else if(keyArray[2].equalsIgnoreCase(accessString)){
				String rawLocationString = getProperty(key);
				String[] locationArray = rawLocationString.split(",");
				String locationString = locationArray[0];
				links.add(new LocationLink(LocationType.wcs, new URL(locationString + "/wcs")));
			}
		}
		
	}

	private void handleLocationWms(String key, String accessString,
			Set<LocationLink> links) throws IOException {
		String[] keyArray = key.split("\\.");
		if (keyArray[2].equalsIgnoreCase("wms")){
			//no access info
			String rawLocationString = getProperty(key);
			String[] locationArray = rawLocationString.split(",");
			String locationString = "";
			for (int i=0; i < locationArray.length; i++){
				locationString += "\"" + locationArray[i] + "/wms\"";
				locationString += ",";
			}
			locationString = locationString.substring(0, locationString.length() - 1);
			links.add(new LocationLink(LocationType.wms, new URL("[" + locationString + "]")));

		} else if(keyArray[2].equalsIgnoreCase(accessString)){
			String rawLocationString = getProperty(key);
			String[] locationArray = rawLocationString.split(",");
			String locationString = "";
			for (int i=0; i < locationArray.length; i++){
				locationString += "\"" + locationArray[i] + "/wms\"";
				locationString += ",";
			}
			locationString = locationString.substring(0, locationString.length() - 1);
			links.add(new LocationLink(LocationType.wms, new URL("[" + locationString + "]")));
		}
	}

	private void handleLocationGeoserver(String key, String accessString, GeometryType dataType, Set<LocationLink> links) throws IOException {
		String[] keyArray = key.split("\\.");
		if (keyArray[2].equalsIgnoreCase("geoserver")){
			//this is one case; no differentiation b/w access levels
			String rawLocationString = getProperty(key);
			String[] locationArray = rawLocationString.split(",");
			String locationString = "";
			for (int i=0; i < locationArray.length; i++){
				locationString += "\"" + locationArray[i] + "/wms\"";
				locationString += ",";
			}
			locationString = locationString.substring(0, locationString.length() - 1);
			links.add(new LocationLink(LocationType.wms, new URL("[" + locationString + "]")));
			if (GeometryType.isVector(dataType)){
				links.add(new LocationLink(LocationType.wfs, new URL(locationArray[0] + "/wfs")));
			} else if (GeometryType.isRaster(dataType)){
				links.add(new LocationLink(LocationType.wcs, new URL(locationArray[0]+ "/wcs")));
			}
		} else if(keyArray[2].equalsIgnoreCase(accessString)){
			if (keyArray[3].equalsIgnoreCase("geoserver")){
				//we can assign all ows values
				String rawLocationString = getProperty(key);
				String[] locationArray = rawLocationString.split(",");
				String locationString = "";
				for (int i=0; i < locationArray.length; i++){
					locationString += "\"" + locationArray[i] + "/wms\"";
					locationString += ",";
				}
				locationString = locationString.substring(0, locationString.length() - 1);
				links.add(new LocationLink(LocationType.wms, new URL("[" + locationString + "]")));
				if (GeometryType.isVector(dataType)){
					links.add(new LocationLink(LocationType.wfs, new URL(locationArray[0] + "/wfs")));
				} else if (GeometryType.isRaster(dataType)){
					links.add(new LocationLink(LocationType.wcs, new URL(locationArray[0]+ "/wcs")));
				}
			}
		}
	}
}
