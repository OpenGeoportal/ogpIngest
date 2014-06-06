package org.OpenGeoPortal.Ingest.Metadata;

public enum MetadataElement {
		Title("Title"), 
		Abstract("Abstract"), 
		LayerName("Layer Name"), 
		Publisher("Publisher"), 
		Originator("Originator"), 
		Bounds("Layer Bounds"), 
		ThemeKeywords("Theme Keywords"), 
		PlaceKeywords("Place Keywords"), 
		Access("Layer Access"),
		LayerId("Layer Id"),
		WorkspaceName("Workspace Name"),
		Location("Web Location"),
		Institution("Institution Name"),
		FullText("Full Metadata document."),
		DataType("Layer Data Type"),
		Georeferenced("Georeferenced"),
		ContentDate("Content Date");
		
		private final String displayName;

		MetadataElement(String displayName){
			this.displayName = displayName;
		}
	
		public String getDisplayName(){
			return displayName;
		}
		
		public static MetadataElement parseMetadataElement(String metadataElementString){
			if (metadataElementString.trim().length() == 0){
				return null;
			}
			for (MetadataElement element: MetadataElement.values()){
				if (element.toString().equalsIgnoreCase(metadataElementString.trim())){
					return element;
				} 
			}
			return null;
		}
}
