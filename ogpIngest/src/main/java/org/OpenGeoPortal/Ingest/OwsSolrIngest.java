package org.OpenGeoPortal.Ingest;

import org.OpenGeoPortal.Solr.SolrClient;

public class OwsSolrIngest extends AbstractSolrIngest implements SolrIngest {
	IngestProperties ingestProperties;
	
	public IngestProperties getIngestProperties() {
		return ingestProperties;
	}

	public void setIngestProperties(IngestProperties ingestProperties) {
		this.ingestProperties = ingestProperties;
	}

	public SolrClient getSolrClient() {
		return solrClient;
	}

	public void setSolrClient(SolrClient solrClient) {
		this.solrClient = solrClient;
	}
	
	@Override
	public String processGeoreferenced() {
		return "true";
	}

	@Override
	public String processCollectionId() {
		return "geoserver";
	}

	@Override
	public String processLocation() {
		if (ingestProperties == null){
			logger.info("ingest properties is null");
		}
		if (metadata == null){
			logger.info("metadta is null");
		}
		String location = null;
		try{
			location = ingestProperties.getLocation(metadata);
		} catch (Exception e){
			this.solrIngestResponse.addError("location", "location", "Online Location Error:" + e.getMessage(), "");
		}
		return location;
	}

	@Override
	public String processAvailability() {
		return "online";
	}
	/**
	 * the location field holds a json object where, roughly speaking, keys are protocols and values are servers
	 * urls specified in this function should probably come from a config file
	 * perhaps this should take the entire hash as a parameter in case the urls are a function of other values 
	 * @param access
	 * @param solrType
	 * @return
	 */
/*	public static String getLocation(Metadata metadata)
	{
		//can we store all of these in a properties file as well?
		String returnValue = "{";
		institution = institution.toLowerCase();
		if (institution.startsWith("tufts"))
		{
			if (access == AccessLevel.Public)
			{
				returnValue += "\"wms\": [\"http://geoserver01.uit.tufts.edu/wms\"],";
				if (GeometryType.isRaster(solrType))
					returnValue += "\"wcs\": \"http://geoserver01.uit.tufts.edu/wcs\"";
				else
					returnValue += "\"wfs\": \"http://geoserver01.uit.tufts.edu/wfs\"";
			}
			else
			{
				returnValue += "\"wms\": [\"http://geoserver01.uit.tufts.edu:8443/wms\"]";
				if (GeometryType.isRaster(solrType))
					returnValue += "\"wcs\": \"http://geoserver01.uit.tufts.edu:8443/wcs\"";
				else
					returnValue += "\"wfs\": \"http://geoserver01.uit.tufts.edu:8443/wfs\"";
			}
		}
		else if (institution.startsWith("harvard"))
		{
			returnValue += "\"wms\": [\"http://hgl.harvard.edu:8080/geoserver/wms\"]," 
			                 +  "\"tilecache\": [\"http://hgl.harvard.edu/cgi-bin/tilecache/tilecache.cgi\"]," 
			                 +  "\"serviceStart\": \"http://hgl.harvard.edu:8080/HGL/RemoteServiceStarter\",";
			if (GeometryType.isRaster(solrType))
				returnValue += "\"wcs\": \"http://hgl.harvard.edu:8080/geoserver/wcs\","
										  + "\"download\": \"http://hgl.harvard.edu:8080/HGL/HGLOpenDelivery\"";
			else
				returnValue += "\"wfs\": \"http://hgl.harvard.edu:8080/geoserver/wfs\"";
		}
		else if (institution.startsWith("berkeley"))
		{
			returnValue += "\"wms\": [\"http://gis-gs.lib.berkeley.edu:8080/geoserver/wms\"],";
			if (GeometryType.isRaster(solrType))
				returnValue += "\"wcs\": \"http://gis-gs.lib.berkeley.edu:8080/geoserver/wcs\"";
			else
				returnValue += "\"wfs\": \"http://gis-gs.lib.berkeley.edu:8080/geoserver/wfs\"";
		}
		else if (institution.startsWith("massgis"))
		{
			returnValue += "\"wms\": [\"http://giswebservices.massgis.state.ma.us/geoserver/wms\"],";
			if (GeometryType.isRaster(solrType))
				returnValue += "\"wcs\": \"http://giswebservices.massgis.state.ma.us/geoserver/wcs\"";
			else
				returnValue += "\"wfs\": \"http://giswebservices.massgis.state.ma.us/geoserver/wfs\"";
		}
		else if (institution.startsWith("mit"))
		{
			if (access == AccessLevel.Public)
			{
				returnValue += "\"wms\": [\"http://arrowsmith.mit.edu:8080/geoserver/wms\"],";
				if (GeometryType.isRaster(solrType))
					returnValue += "\"wcs\": \"http://arrowsmith.mit.edu:8080/geoserver/wcs\"";
				else
					returnValue += "\"wfs\": \"http://arrowsmith.mit.edu:8080/geoserver/wfs\"";
			}
			else {
				returnValue += "\"wms\": [\"https://arrowsmith.mit.edu/secure-geoserver/wms\"],";
				if (GeometryType.isRaster(solrType))
					returnValue += "\"wcs\": \"https://arrowsmith.mit.edu/secure-geoserver/wcs\"";
				else
					returnValue += "\"wfs\": \"https://arrowsmith.mit.edu/secure-geoserver/wfs\"";
			}
		}
				
		returnValue = returnValue + "}";
		
		return returnValue;
	}*/
	
}
