package org.OpenGeoPortal.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;


public class PropertyFileProperties {
	Properties properties;
	Resource resource;
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void setResource(Resource resource){
		this.resource = resource;
	}
	
	public String getProperty(String propertyName) throws IOException{
		if (properties == null){
			properties = new Properties();
			InputStream MyInputStream = resource.getInputStream();
			properties.load(MyInputStream);
		}
		return properties.getProperty(propertyName);
	}
	
	public Properties getProperties() throws IOException {
		if (properties == null){
			properties = new Properties();
			InputStream MyInputStream = resource.getInputStream();
			properties.load(MyInputStream);
		}
		return properties;
	}
}
