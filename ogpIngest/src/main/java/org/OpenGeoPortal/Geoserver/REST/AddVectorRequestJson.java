package org.OpenGeoPortal.Geoserver.REST;

import org.codehaus.jackson.annotate.JsonProperty;

//String postData = "{\"featureType\":{\"name\":\"" + tempName + "\"}}";
/*
 * {"featureType":{"name":"GISPORTAL.GISOWNER01.SOMERVILLE_BUILDINGS","nativeName":"GISPORTAL.GISOWNER01.SOMERVILLE_BUILDINGS","namespace":{"name":"sde","href":"http:\/\/geoserver-dev.atech.tufts.edu\/rest\/namespaces\/sde.json"},"title":"GISPORTAL.GISOWNER01.SOMERVILLE_BUILDINGS",
 * "metadataLinks":{"metadataLink":[{"type":"text\/plain","metadataType":"FGDC","content":"http:\/\/geodata.tufts.edu\/test.xml"}]},
 * "nativeCRS":{"@class":"projected","$":"PROJCS[\"NAD_1983_StatePlane_Massachusetts_Mainland_FIPS_2001_Feet\", \n  GEOGCS[\"GCS_North_American_1983\", \n    DATUM[\"D_North_American_1983\", \n      SPHEROID[\"GRS_1980\", 6378137.0, 298.257222101]], \n    PRIMEM[\"Greenwich\", 0.0], \n    UNIT[\"degree\", 0.017453292519943295], \n    AXIS[\"Longitude\", EAST], \n    AXIS[\"Latitude\", NORTH]], \n  PROJECTION[\"Lambert_Conformal_Conic_2SP\"], \n  PARAMETER[\"central_meridian\", -71.5], \n  PARAMETER[\"latitude_of_origin\", 41.0], \n  PARAMETER[\"standard_parallel_1\", 42.68333333333334], \n  PARAMETER[\"false_easting\", 656166.6666666665], \n  PARAMETER[\"false_northing\", 2460625.0], \n  PARAMETER[\"scale_factor\", 1.0], \n  PARAMETER[\"standard_parallel_2\", 41.71666666666667], \n  UNIT[\"foot_survey_us\", 0.3048006096012192], \n  AXIS[\"X\", EAST], \n  AXIS[\"Y\", NORTH]]"},"srs":"EPSG:2249","nativeBoundingBox":{"minx":754967.992,"maxx":771464.121,"miny":2961320.4,"maxy":2977387.912,"crs":{"@class":"projected","$":"EPSG:2249"}},"latLonBoundingBox":{"minx":-71.134,"maxx":-71.073,"miny":42.373,"maxy":42.418,"crs":"EPSG:4326"},"projectionPolicy":"FORCE_DECLARED","enabled":true,"store":{"@class":"dataStore","name":"arcsde10","href":"http:\/\/geoserver-dev.atech.tufts.edu\/rest\/workspaces\/sde\/datastores\/arcsde10.json"},"maxFeatures":0,"numDecimals":0,"attributes":{"attribute":{"name":"Shape","minOccurs":0,"maxOccurs":1,"nillable":true,"binding":"com.vividsolutions.jts.geom.MultiPolygon"}}}}
 *  {
  "featureType":{
     "name":"states",
     "title":"USA Population",
     "abstract":"This is some census data on the states.",
     "keywords":{
        "string":[
           "census",
           "states",
           "united",
           "boundaries",
           "state"
        ]
     }
  }
}

 * 
 * 
 */
public class AddVectorRequestJson {

	public AddVectorRequestJson(){
		this.featureType = new FeatureType();
	}
	
	@JsonProperty("featureType")
	FeatureType featureType;
	
	public FeatureType getFeatureType() {
		return featureType;
	}

	public void setFeatureType(FeatureType featureType) {
		this.featureType = featureType;
	}


	public class FeatureType {
		@JsonProperty("name")
		String name;
		/*@JsonProperty("nativeName")
		String nativeName;
		@JsonProperty("title")
		String title;
		@JsonProperty("abstract")
		String description;
		@JsonProperty("keywords")
		Keywords keywords;
		@JsonProperty("metadataLinks")
		MetadataLinks metadataLinks;
		
		public class Keywords {
			@JsonProperty("string")
			ArrayList<String> string;
		}
		
		public class MetadataLinks {
			@JsonProperty("metadataLink")
			ArrayList<MetadataLink> metadataLink;
			MetadataLinks(){
				this.metadataLink.add(new MetadataLink());
			}
			public class MetadataLink {
				//{"type":"text\/plain","metadataType":"FGDC","content":"http:\/\/geodata.tufts.edu\/test.xml"}
				@JsonProperty("type")
				String type;
				@JsonProperty("metadataType")
				String metadataType;
				@JsonProperty("content")
				String content;
			}
		}
		*/

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		/*public String getNativeName() {
			return nativeName;
		}

		public void setNativeName(String nativeName) {
			this.nativeName = nativeName;
		}*/
	}


}