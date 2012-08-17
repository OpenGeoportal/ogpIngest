package org.OpenGeoPortal.Ingest;

import java.io.IOException;
import java.util.ArrayList;
 
import org.OpenGeoPortal.Geoserver.REST.GeoserverRestClient;
import org.OpenGeoPortal.Layer.GeometryType;
import org.OpenGeoPortal.Layer.Metadata;
import org.OpenGeoPortal.Utilities.OgpLogger;
import org.slf4j.Logger;

public class SimpleGeoServerIngest implements MapServerIngest {
	IngestProperties ingestProperties;
	@OgpLogger
	Logger logger;
	GeoserverRestClient gsrc = null;
	
	public void setIngestProperties(IngestProperties ingestProperties) {
		this.ingestProperties = ingestProperties;
	}

	public MapserverRestClient getRestClient(String institution, Metadata metadata) throws IOException{
		String accessString = metadata.getAccess().toString().toLowerCase();
		//assume we are only ingesting local layers
		String prefix = "local." + accessString + ".";
		String geoserver = ingestProperties.getProperty(prefix + "GeoServerUrl");
		String workspace = ingestProperties.getWorkspace(metadata, institution);
		String datastore = ingestProperties.getProperty(prefix + "GeoServerDatastore");
		String username = ingestProperties.getProperty(prefix + "GeoServerUserName");
		String password = ingestProperties.getProperty(prefix + "GeoServerPassword");
		
		//System.out.println("in ingestHandler with geoserver = " + geoserver + ", username = " + username);
		if (gsrc == null || (!workspace.equals(gsrc.getWorkspace())) || (!geoserver.equals(gsrc.getGeoserverUrl()))
					|| (!datastore.equals(gsrc.getDatastore()))){
			logger.info("Creating new geoserver client..............");
			gsrc = new GeoserverRestClient(geoserver, workspace, datastore, username, password);
		} 
		return gsrc;
	}
	
	public String addLayerToMapServer(String institution, Metadata metadata) throws IOException{
		//this is the class that should use the esri prefix.  The layer should appear in GeoServer without it
		gsrc = (GeoserverRestClient) getRestClient(institution, metadata);
		String layerName = metadata.getOwsName();
		if(GeometryType.isVector(metadata.getGeometryType())){
			ArrayList<String> availableVectors = gsrc.queryAvailableVectors();

			if (availableVectors.contains(layerName)){
				logger.info("attempting to add..." + layerName);
				String result = gsrc.addVectorLayer(layerName);
				logger.info("returned from mapserver: " + result);
				if (result == null){
					return "unknown GeoServer failure: null response";
				} else {
					return result;
				}
			} else {
				//if layer is not in available layers, check to see if it has already been added
				if (gsrc.featureTypeExists(layerName)){
					logger.info(layerName + " has already been added to GeoServer.");
					return "success: featureType[" + layerName + "] has already been added to GeoServer.";
				} else {
					logger.warn(metadata.getOwsName() + " is not available to the map server.  Make sure the layer is in the data store and has the correct permissions.");
					return "failure: featureType[" + layerName + "] can not be found in the specified data store";
				}
			} 
		} else if (GeometryType.isRaster(metadata.getGeometryType())){

			//add raster
			logger.info("attempting to add..." + layerName);
			String result;
			try {
				result = gsrc.addCoverageStore(layerName);
			} catch (Exception e) {
				return "GeoServer failure: " + e.getMessage();
			}
			logger.info("CoverageStore added");
			result = gsrc.addCoverage(layerName);
			logger.info("returned from mapserver: " + result);
			if (result == null){
				return "unknown GeoServer failure: null response";
			} else {
				return result;
			}
		} else {
			return "Unspecified geometry type.";
		}

	}
}
