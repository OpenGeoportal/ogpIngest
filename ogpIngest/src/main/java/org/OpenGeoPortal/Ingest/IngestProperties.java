package org.OpenGeoPortal.Ingest;

import java.io.IOException;

import org.OpenGeoPortal.Layer.Metadata;

public interface IngestProperties {
	String getProperty(String propertyName) throws IOException;
	String getWorkspace(Metadata metadata, String institution) throws IOException;
	String getLocation(Metadata metadata) throws IOException;
}
