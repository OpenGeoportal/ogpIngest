package org.OpenGeoPortal.Ingest.Metadata;

import java.util.ArrayList;

import org.OpenGeoPortal.Ingest.IngestResponse;
import org.OpenGeoPortal.Layer.Metadata;

public class MetadataParseResponse extends IngestResponse {
	public Metadata metadata;

	public Boolean metadataParsed = false;
	
	public MetadataParseResponse(){
		metadata = new Metadata();
		ingestErrors = new ArrayList<IngestInfo>();
		ingestWarnings = new ArrayList<IngestInfo>();
	}
	
}