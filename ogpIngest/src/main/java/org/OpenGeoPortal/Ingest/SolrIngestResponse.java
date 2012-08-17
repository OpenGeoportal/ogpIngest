package org.OpenGeoPortal.Ingest;

import java.util.ArrayList;

import org.OpenGeoPortal.Solr.SolrRecord;

public class SolrIngestResponse extends IngestResponse {
	public SolrRecord solrRecord;

	public Boolean convertedMetadata = false;
	public Boolean addedRecord = false;

	public SolrIngestResponse(){
		solrRecord = new SolrRecord();
		ingestErrors = new ArrayList<IngestInfo>();
		ingestWarnings = new ArrayList<IngestInfo>();
	}
}
