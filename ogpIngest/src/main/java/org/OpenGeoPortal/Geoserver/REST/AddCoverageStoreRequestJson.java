package org.OpenGeoPortal.Geoserver.REST;

import org.codehaus.jackson.annotate.JsonProperty;

//curl -v -XPOST -u username:password -H "Content-Type:text/xml" --data "<coverageStore><name>GISPORTAL.GISOWNER01.LANDINFO_K01C</name><type>ArcSDE Raster</type><enabled>true</enabled><url>sde://username:password@sdehost:sdeport/#GISPORTAL.GISOWNER01.LANDINFO_K01C</url></coverageStore>" --url http://geoserver01.uit.tufts.edu/rest/workspaces/sde/coveragestores

public class AddCoverageStoreRequestJson {
	@JsonProperty("coverageStore")
	CoverageStore coverageStore;
	
	AddCoverageStoreRequestJson (){
		this.coverageStore = new CoverageStore();
	}
	public CoverageStore getCoverageStore() {
		return coverageStore;
	}

	public void setCoverageStore(CoverageStore coverageStore) {
		this.coverageStore = coverageStore;
	}

	public class CoverageStore {
		@JsonProperty("name")
		String name;
		@JsonProperty("type")
		String type;
		@JsonProperty("enabled")
		String enabled;
		@JsonProperty("url")
		String url;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getEnabled() {
			return enabled;
		}
		public void setEnabled(String enabled) {
			this.enabled = enabled;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
	}
}
