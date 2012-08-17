package org.OpenGeoPortal.Ingest.Metadata;

import org.OpenGeoPortal.Layer.Metadata;
import org.codehaus.jackson.JsonNode;

public interface JsonMetadataParseMethod {

	Metadata marshallMetadata(JsonNode rootNode);

}
