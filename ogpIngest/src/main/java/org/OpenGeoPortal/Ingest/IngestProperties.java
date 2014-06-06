package org.OpenGeoPortal.Ingest;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.OpenGeoPortal.Layer.LocationLink;
import org.OpenGeoPortal.Layer.Metadata;

public interface IngestProperties {
	String getProperty(String propertyName) throws IOException;
	String[] getPropertyArray(String propertyName) throws IOException;
	Properties getProperties() throws IOException;
	String getWorkspace(Metadata metadata, String institution) throws IOException;
	Set<LocationLink> getLocation(Metadata metadata) throws IOException; 
}
