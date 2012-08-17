package org.OpenGeoPortal.Geoserver.REST;

import org.codehaus.jackson.annotate.JsonProperty;

public class AddCoverageRequestJson {
/*
 * 
 * curl -XPOST -u ${username}:${password} -H "Content-Type:text/xml" -d "<coverage><name>${coverageName}</name></coverage>" http://${geoserverhostandpath}/rest/workspaces/${workspace}/coveragestores/${coverageStoreName}/coverages
 * 
 */
	@JsonProperty("coverage")
	Coverage coverage;
	
	AddCoverageRequestJson(){
		this.coverage = new Coverage();
	}
	public Coverage getCoverage() {
		return coverage;
	}

	public void setCoverage(Coverage coverage) {
		this.coverage = coverage;
	}
	
	public class Coverage {
		@JsonProperty("name")
		String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
