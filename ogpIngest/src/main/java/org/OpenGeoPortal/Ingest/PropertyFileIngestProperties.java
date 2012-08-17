package org.OpenGeoPortal.Ingest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.OpenGeoPortal.Layer.GeometryType;
import org.OpenGeoPortal.Layer.Metadata;
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
		} 
		logger.debug(prefix + ".GeoServerWorkspace");
		return getProperty(prefix + ".GeoServerWorkspace");
	}
	
	public String getLocation(Metadata metadata) throws IOException {
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
		locationString = "{" + locationString.substring(0, locationString.length() - 1) + "}";

		return locationString;
		} catch (Exception e){
			throw new IOException();
		}
	}

	private void handleLocationTilecache(String key, String accessString,
			Map<String, String> locationMap) throws IOException {
		String[] keyArray = key.split("\\.");
		if (keyArray[2].equalsIgnoreCase("tilecache")){
			//no access info
			String rawLocationString = getProperty(key);
			locationMap.put("tilecache", rawLocationString);
		} else if(keyArray[2].equalsIgnoreCase(accessString)){
			String rawLocationString = getProperty(key);
			locationMap.put("tilecache", rawLocationString);
		}
	}
	
	private void handleLocationServiceStart(String key,
			Map<String, String> locationMap) throws IOException {
		locationMap.put("serviceStart", getProperty(key));
	}
	
	private void handleLocationDownload(String key,
			Map<String, String> locationMap) throws IOException {
		locationMap.put("download", getProperty(key));
	}

	private void handleLocationWfs(String key, String accessString,
			GeometryType dataType, Map<String, String> locationMap) throws IOException {
		String[] keyArray = key.split("\\.");
		if (GeometryType.isVector(dataType)){
			if (keyArray[2].equalsIgnoreCase("wfs")){
				//no access info
				String rawLocationString = getProperty(key);
				String[] locationArray = rawLocationString.split(",");
				String locationString = locationArray[0];
				locationMap.put("wfs", locationString + "/wfs");
			} else if(keyArray[2].equalsIgnoreCase(accessString)){
				String rawLocationString = getProperty(key);
				String[] locationArray = rawLocationString.split(",");
				String locationString = locationArray[0];
				locationMap.put("wfs", locationString + "/wfs");
			}
		}
		
	}
	
	private void handleLocationWcs(String key, String accessString,
			GeometryType dataType, Map<String, String> locationMap) throws IOException {
		String[] keyArray = key.split("\\.");
		if (GeometryType.isRaster(dataType)){
			if (keyArray[2].equalsIgnoreCase("wcs")){
				//no access info
				String rawLocationString = getProperty(key);
				String[] locationArray = rawLocationString.split(",");
				String locationString = locationArray[0];
				locationMap.put("wcs", locationString + "/wcs");
			} else if(keyArray[2].equalsIgnoreCase(accessString)){
				String rawLocationString = getProperty(key);
				String[] locationArray = rawLocationString.split(",");
				String locationString = locationArray[0];
				locationMap.put("wcs", locationString + "/wcs");
			}
		}
		
	}

	private void handleLocationWms(String key, String accessString,
			Map<String, String> locationMap) throws IOException {
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
			locationMap.put("wms", "[" + locationString + "]");
		} else if(keyArray[2].equalsIgnoreCase(accessString)){
			String rawLocationString = getProperty(key);
			String[] locationArray = rawLocationString.split(",");
			String locationString = "";
			for (int i=0; i < locationArray.length; i++){
				locationString += "\"" + locationArray[i] + "/wms\"";
				locationString += ",";
			}
			locationString = locationString.substring(0, locationString.length() - 1);
			locationMap.put("wms", "[" + locationString + "]");
		}
	}

	private void handleLocationGeoserver(String key, String accessString, GeometryType dataType, Map<String, String> locationMap) throws IOException {
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
			locationMap.put("wms", "[" + locationString + "]");
			if (GeometryType.isVector(dataType)){
				locationMap.put("wfs", locationArray[0] + "/wfs");
			} else if (GeometryType.isRaster(dataType)){
				locationMap.put("wcs", locationArray[0]+ "/wcs");
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
				locationMap.put("wms", "[" + locationString + "]");
				if (GeometryType.isVector(dataType)){
					locationMap.put("wfs", locationArray[0] + "/wfs");
				} else if (GeometryType.isRaster(dataType)){
					locationMap.put("wcs", locationArray[0] + "/wcs");
				}
			}
		}
	}
}
