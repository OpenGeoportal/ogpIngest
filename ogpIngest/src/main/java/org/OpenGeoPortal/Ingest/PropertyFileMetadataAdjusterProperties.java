package org.OpenGeoPortal.Ingest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Map;

import org.OpenGeoPortal.Utilities.PropertyFileProperties;

public class PropertyFileMetadataAdjusterProperties extends
		PropertyFileProperties implements MetadataAdjusterProperties {
	
	public Map<String, PropertyNode> getPropertyNode() throws IOException{
		Properties properties = this.getProperties();
		Set<String> props = this.getProperties().stringPropertyNames();
		Map<String, PropertyNode> propertyNodeMap = new HashMap<String,PropertyNode>();
		for (String prop : props){
			String[] arrProp = prop.split(".");
			String contentText = null;
			String[] arrParent = null;
			String type = arrProp[1];
			Boolean replace = null;
			if (type.equalsIgnoreCase("parent")){
				arrParent = properties.getProperty(prop).split(">");
			} else if (type.equalsIgnoreCase("content")){
				contentText = properties.getProperty(prop);
			} else if (type.equalsIgnoreCase("replace")){
				replace = Boolean.parseBoolean(properties.getProperty(prop));
			}
			
			PropertyNode propertyNode = propertyNodeMap.get(arrProp[0]);
			if (propertyNode == null){
				propertyNodeMap.put(arrProp[0], new PropertyNode());
				propertyNode = propertyNodeMap.get(arrProp[0]);
			}
			propertyNode.insertNode = contentText;
			propertyNode.parentList = arrParent;
			propertyNode.replace = replace;

			logger.info("Properties:" + prop + ":" + properties.getProperty(prop));
		}
		return propertyNodeMap;
	}
	
	public class PropertyNode {
		public String[] parentList; 
		public String insertNode;
		public Boolean replace;
		
	}
	
}
