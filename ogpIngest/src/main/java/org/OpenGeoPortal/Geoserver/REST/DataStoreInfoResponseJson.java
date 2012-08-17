package org.OpenGeoPortal.Geoserver.REST;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DataStoreInfoResponseJson {

	//{"dataStore":{"name":"ArrowsmithSDE","type":"ArcSDE","enabled":true,"workspace":{"name":"sde","href":"http:\/\/delisle.mit.edu:8080\/geoserver\/rest\/workspaces\/sde.json"},
	//"connectionParameters":{"entry":[{"@key":"port","$":"5150"},{"@key":"dbtype","$":"arcsde"},{"@key":"datastore.allowNonSpatialTables","$":"false"},{"@key":"pool.timeOut","$":"500"},{"@key":"server","$":"arrowsmith.mit.edu"},{"@key":"pool.maxConnections","$":"6"},{"@key":"password","$":"gis"},{"@key":"user","$":"sde_data"},{"@key":"pool.minConnections","$":"2"},{"@key":"namespace","$":"http:\/\/geoserver.sf.net"}]},"__default":false,"featureTypes":"http:\/\/delisle.mit.edu:8080\/geoserver\/rest\/workspaces\/sde\/datastores\/ArrowsmithSDE\/featuretypes.json"}}
	@JsonProperty("dataStore")
	DataStore dataStore;

	class DataStore {
		@JsonProperty("connectionParameters")
		ConnectionParameters connectionParameters;
		
		class ConnectionParameters {
			@JsonProperty("entry")
			List<ValueMap> entry;
			class ValueMap {
				@JsonProperty("@key")
				String key;
				@JsonProperty("$")
				String value;
			}
		}
	}
}
