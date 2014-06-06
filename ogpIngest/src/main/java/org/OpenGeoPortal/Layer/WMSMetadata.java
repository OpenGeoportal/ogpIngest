package org.OpenGeoPortal.Layer;

import java.net.URL;

public class WMSMetadata extends Metadata {
	private URL metadataUrl = null;

	public URL getMetadataUrl() {
		return metadataUrl;
	}

	public void setMetadataUrl(URL metadataUrl) {
		this.metadataUrl = metadataUrl;
	}
	
	public Boolean hasMetadataUrl(){
		if (metadataUrl != null){
			return true;
		} else {
			return false;
		}
	}
}
