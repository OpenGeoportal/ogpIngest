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
						if (mlinkIterator.next().getLocationType().equals(currentType)){
							unneededLinks.add(currentLink);
							break;
						}
					}
				}
			
			propertyLinks.removeAll(unneededLinks);
			links.addAll(propertyLinks);
			location = locationLinksToString(links);
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
