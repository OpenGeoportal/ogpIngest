package org.OpenGeoPortal.Geoserver.REST;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonProperty;

//{"list":{"string":["GISPORTAL.GISOWNER01.NEWENGESAPTS98COPY_PROJECT",
public class AvailableVectorsResponseJson {
	@JsonProperty("list")
	LayerList layerList;
	
	public class LayerList {
		@JsonProperty("string")
		ArrayList<String> string;
	}
}
