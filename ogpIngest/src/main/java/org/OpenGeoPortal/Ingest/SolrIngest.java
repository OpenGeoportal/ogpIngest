package org.OpenGeoPortal.Ingest;

import java.util.Set;

import org.OpenGeoPortal.Ingest.AbstractSolrIngest.MetadataElement;
import org.OpenGeoPortal.Layer.Metadata;

public interface SolrIngest {
	public SolrIngestResponse writeToSolr(Metadata metadata,Set<MetadataElement> requiredElements) throws Exception;
	public SolrIngestResponse writeToSolr(Metadata metadata) throws Exception;
	public String getSolrReport(Metadata metadata);
	public SolrIngestResponse auditSolr(Metadata metadata, Set<MetadataElement> requiredElements) throws Exception;
}
