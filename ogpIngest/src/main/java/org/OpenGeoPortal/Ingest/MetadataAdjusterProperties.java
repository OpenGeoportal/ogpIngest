package org.OpenGeoPortal.Ingest;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.OpenGeoPortal.Ingest.PropertyFileMetadataAdjusterProperties.PropertyNode;

public interface MetadataAdjusterProperties {
	Map<String, PropertyNode> getPropertyNode() throws IOException;
	String getProperty(String propertyName) throws IOException;
	public Properties getProperties() throws IOException;

}
