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
			logger.info("metadata is null");
		}
		String location = "";
		try{
			location = ingestProperties.getLocation(metadata);
		} catch (Exception e){
			this.solrIngestResponse.addWarning("location", "location", "Online Location Error:" + e.getMessage(), "");
		}
		return location;
	}

	@Override
	public String processAvailability() {
		return "online";
	}
	
}
