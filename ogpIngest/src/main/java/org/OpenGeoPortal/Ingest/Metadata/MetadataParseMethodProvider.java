package org.OpenGeoPortal.Ingest.Metadata;

import org.OpenGeoPortal.Ingest.Metadata.MetadataType;
import org.w3c.dom.Document;

public interface MetadataParseMethodProvider {
	public MetadataParseMethod getMetadataParseMethod(MetadataType metadataType);
	public MetadataParseMethod getMetadataParseMethod(Document document) throws Exception;

}
