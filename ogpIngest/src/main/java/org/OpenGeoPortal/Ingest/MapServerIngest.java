package org.OpenGeoPortal.Ingest;

import java.io.IOException;

import org.OpenGeoPortal.Layer.Metadata;

public interface MapServerIngest {
	MapserverRestClient getRestClient(String institution, Metadata metadata) throws IOException;
	String addLayerToMapServer(String institution, Metadata metadata) throws IOException;

}
