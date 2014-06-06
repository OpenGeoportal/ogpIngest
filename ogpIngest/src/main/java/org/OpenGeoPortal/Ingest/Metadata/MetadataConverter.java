package org.OpenGeoPortal.Ingest.Metadata;

import java.io.File;
import java.io.InputStream;

public interface MetadataConverter {
	public MetadataParseResponse parse(File metadataFile, String institution) throws Exception;
	public MetadataParseResponse parse(InputStream metadataStream) throws Exception;
	public MetadataParseResponse parse(InputStream metadataStream, String institution) throws Exception;
	MetadataParseResponse bestEffortParse(File metadataFile) throws Exception;
}
