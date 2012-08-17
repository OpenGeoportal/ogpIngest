package org.OpenGeoPortal.Ingest.Metadata;

import org.OpenGeoPortal.Ingest.Metadata.MetadataParseResponse;
import org.w3c.dom.Document;

public interface MetadataParseMethod {

	MetadataParseResponse marshallMetadata(Document document);

}
