package org.OpenGeoPortal.Ingest;

import java.util.ArrayList;

public interface MapserverRestClient {

	ArrayList<String> queryAvailableVectors();

	String addVectorLayer(String owsName);

	Boolean featureTypeExists(String owsName);

}
