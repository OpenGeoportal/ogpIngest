package org.OpenGeoPortal.Ingest;

import org.OpenGeoPortal.Solr.SolrClient;

public class PaperSolrIngest extends AbstractSolrIngest implements SolrIngest {

	public SolrClient getSolrClient() {
		return solrClient;
	}

	public void setSolrClient(SolrClient solrClient) {
		this.solrClient = solrClient;
	}
	
	@Override
	public String processGeoreferenced() {
		return Boolean.toString(metadata.getGeoreferenced());
	}

	@Override
	public String processCollectionId() {
		//determine source
		if (metadata.getId().toLowerCase().contains("library")){
			return "BARTON";
		} else {
			return "UNCATALOGED";
		}
	}

	@Override
	public String processLocation() {
		return locationLinksToString(metadata.getLocation());
	}

	@Override
	public String processAvailability() {
		return "offline";
	}
	
	@Override
	public String processFullText() {
		return "";
	}
	
}
