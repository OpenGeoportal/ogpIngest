package org.OpenGeoPortal.Ingest;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.OpenGeoPortal.Layer.LocationLink;
import org.OpenGeoPortal.Layer.LocationLink.LocationType;
import org.OpenGeoPortal.Solr.SolrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OwsSolrIngest extends AbstractSolrIngest implements SolrIngest {
	IngestProperties ingestProperties;
	final Logger logger = LoggerFactory.getLogger(this.getClass());

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
		Set<LocationLink> links = metadata.getLocation();
		logger.info("Links: " + Integer.toString(links.size()));
		try{
			Set<LocationLink> propertyLinks = ingestProperties.getLocation(metadata);
			Set<LocationLink> unneededLinks = new HashSet<LocationLink>();
				//iterate over propertyLinks, remove those already defined in links (from metadata)
				Iterator<LocationLink> plinkIterator = propertyLinks.iterator();
				while (plinkIterator.hasNext()){
					LocationLink currentLink = plinkIterator.next();
					LocationType currentType = currentLink.getLocationType();
					Iterator<LocationLink> mlinkIterator = links.iterator();
					while (mlinkIterator.hasNext()){
						LocationLink currentMetadataLink = mlinkIterator.next();
						logger.info("link URL: " + currentMetadataLink.getURL().toString());
						logger.info("link type: " + currentMetadataLink.getLocationType().toString());
						if (currentMetadataLink.getLocationType().equals(currentType)){
							unneededLinks.add(currentLink);
							break;
						}
					}
				}
			
			propertyLinks.removeAll(unneededLinks);
			links.addAll(propertyLinks);
		} catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
			this.solrIngestResponse.addWarning("location", "location", "Online Location Error getting links from properties files:" + e.getMessage(), "");
		}
		location = locationLinksToString(links);

		return location;
	}

	@Override
	public String processAvailability() {
		return "online";
	}
	
}
