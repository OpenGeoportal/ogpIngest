package org.OpenGeoPortal.Geoserver.REST;

import org.codehaus.jackson.annotate.JsonProperty;

public class AddVectorResponseJson {
/*
 * what is the response?
 */
	@JsonProperty("location")
		String location;

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}
}
