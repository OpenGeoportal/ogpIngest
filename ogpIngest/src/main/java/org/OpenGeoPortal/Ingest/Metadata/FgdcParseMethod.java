package org.OpenGeoPortal.Ingest.Metadata;

import org.OpenGeoPortal.Ingest.IngestProperties;
import org.OpenGeoPortal.Layer.AccessLevel;
import org.OpenGeoPortal.Layer.BoundingBox;
import org.OpenGeoPortal.Layer.GeometryType;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * read in FGDC formated file into a hashtable representation this hash can be
 * used to generate a solr file.
 * 
 * @author smcdon08, chrissbarnett
 * 
 */
public class FgdcParseMethod extends AbstractXmlMetadataParseMethod implements
		MetadataParseMethod {
	public static enum FgdcTag implements Tag {
		Title("title", FieldType.Single), 
		Abstract("abstract", FieldType.Single), 
		LayerName("ftname", FieldType.Single), 
		Publisher("publish", FieldType.Single), 
		Originator("origin", FieldType.Single), 
		WestBc("westbc", FieldType.Single), 
		EastBc("eastbc", FieldType.Single), 
		NorthBc("northbc", FieldType.Single), 
		SouthBc("southbc",FieldType.Single), 
		ThemeKeywords("themekey", FieldType.Multiple), 
		PlaceKeywords("placekey", FieldType.Multiple), 
		Access("accconst",FieldType.Single);

		private final String tagName; // XML tag name
		private final FieldType fieldType; // Is there one of these tags, or more?
		
		FgdcTag(String tagName, FieldType fieldType) {
			this.tagName = tagName;
			this.fieldType = fieldType;
		}

		public String getTagName() {
			return tagName;
		}

		public FieldType getFieldType() {
			return fieldType;
		}
	}
	
	void handleTitle(){
		Tag tag = FgdcTag.Title;
		try{
			this.metadataParseResponse.metadata.setTitle(getDocumentValue(tag));
		} catch (Exception e){
			logger.error("handleTitle: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}
	}
	
	void handleBounds(){
		Tag tag = FgdcTag.NorthBc;
		try{
			String maxY = getDocumentValue(tag);
			tag = FgdcTag.EastBc;
			String maxX = getDocumentValue(tag);
			tag = FgdcTag.SouthBc;
			String minY = getDocumentValue(tag);
			tag = FgdcTag.WestBc;
			String minX = getDocumentValue(tag);
			//should validate bounds here
			if (validateBounds(minX, minY, maxX, maxY)){
				this.metadataParseResponse.metadata.setBounds(minX, minY, maxX, maxY);
			} else {
				throw new Exception("Invalid Bounds: " + minX + "," + minY + "," + maxX + "," + maxY);
			}
		} catch (Exception e){
			logger.error("handleBounds: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}
	}
	
	Boolean validateBounds(String minX, String minY, String maxX, String maxY){
		BoundingBox bounds = new BoundingBox(minX, minY, maxX, maxY);
		if (bounds.isValid()){
			return true;
		} else {
			return false;
		}
	}
	
	void handleKeywords(){
		Tag tag = FgdcTag.ThemeKeywords;
		try{
			String themeKeywordsString = cleanValue(getDocumentValue(tag));
			String[] themeKeywords = themeKeywordsString.split(" ");
			this.metadataParseResponse.metadata.setThemeKeywords(themeKeywords);
		} catch (Exception e){
			logger.error("handleThemeKeywords: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}
		
		tag = FgdcTag.PlaceKeywords;
		try{
			String placeKeywordsString = cleanValue(getDocumentValue(tag));
			String[] placeKeywords = placeKeywordsString.split(" ");
			this.metadataParseResponse.metadata.setPlaceKeywords(placeKeywords);
		} catch (Exception e){
			logger.error("handlePlaceKeywords: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}
	}

	/**
	 * get the content date which could be in one of three different tags
	 * 
	 * @param document
	 * @return
	 * @throws Exception 
	 */
	protected void handleDate(){
			String dateValue = null;
			try {
				dateValue = getDocumentValue("caldate");//timeperd>timeinfo>sngdate>caldate
			} catch (Exception e){
				try {
					dateValue = getDocumentValue("begdate");
				} catch (Exception e1){
					try {
						dateValue = getDocumentValue("dateStamp");
					} catch (Exception e2) {
						logger.warn("No valid Content Date could be found in the document.");
						this.metadataParseResponse.metadata.setContentDate(null);
						return;
					}
				}
			}
			try {
				logger.debug("DATE VALUE#######:" + dateValue);
				dateValue = processDateString(dateValue);
				this.metadataParseResponse.metadata.setContentDate(dateValue);
			} catch (Exception e) {
				try {
					dateValue = dateValue.substring(0, 3);
					int dateValueInt = Integer.parseInt(dateValue);
					dateValue = Integer.toString(dateValueInt);
					if (dateValue.length() == 4){
						this.metadataParseResponse.metadata.setContentDate(processDateString(dateValue));
					}
				} catch (Exception e1){
					logger.warn("No valid Content Date could be found in the document.");
					this.metadataParseResponse.metadata.setContentDate(null);
				}
			}
	}

	/**
	 * computing the data type for the layer involves looking at multiple fields
	 * in the document
	 * 
	 * @param document
	 * @param layerValues
	 * @throws Exception 
	 */
	protected void handleDataType() {
		String direct = null;// raster?
		String sdtsType = null;// vector type
		String srcCiteA = null;// scanned map
		
		try {
			srcCiteA = getDocumentValue("srccitea");
			if (srcCiteA.equalsIgnoreCase("Paper Map")){
				this.metadataParseResponse.metadata.setGeometryType(GeometryType.ScannedMap);
				return;
			} 
		} catch (Exception e){
			//just continue to next block
		}
		try {
			direct = getDocumentValue("direct");
			if (direct.equalsIgnoreCase("raster") == true) {
				this.metadataParseResponse.metadata.setGeometryType(GeometryType.Raster);
				return;
			}
		} catch (Exception e){
			//again, move to the next block
		}
		try {
			sdtsType = getDocumentValue("sdtstype");
			GeometryType solrType;
			if (sdtsType.equals("G-polygon") || sdtsType.contains("olygon")
						|| sdtsType.contains("chain")){
				solrType = GeometryType.Polygon;
			} else if (sdtsType.equals("Composite")
						|| sdtsType.contains("omposite")
						|| sdtsType.equals("Entity point")){
				solrType = GeometryType.Point;
			} else if (sdtsType.equals("String")){
					solrType = GeometryType.Line;
			} else{
					solrType = GeometryType.Undefined;
			}
			this.metadataParseResponse.metadata.setGeometryType(solrType);
		} catch (Exception e){
			logger.error("null geometry type");
			GeometryType solrType = GeometryType.Undefined;
			this.metadataParseResponse.metadata.setGeometryType(solrType);
			//we should make a note if the geometry type is undefined
		}
	}


	/**
	 * look at the document field associated with Key.Access (for FGDC:
	 * accconst) if the field starts with Restricted, the layer is restricted.
	 * Otherwise it is public.
	 * 
	 * @throws Exception 
	 */
	protected void handleAccess() {
		Tag tag = FgdcTag.Access;
		try {
			String accessValue$ = "";
			try{
				accessValue$ = getDocumentValue(tag);
			} catch (Exception e){
				AccessLevel nullAccess = null;
				this.metadataParseResponse.metadata.setAccessLevel(nullAccess);
				return;
			}
			AccessLevel accessValue = AccessLevel.Public;
			accessValue$ = accessValue$.toLowerCase();
			if (accessValue$.startsWith("restricted")){
				accessValue = AccessLevel.Restricted;
			}
			this.metadataParseResponse.metadata.setAccessLevel(accessValue);
		} catch (Exception e){
			logger.error("handleAccess: " + e.getMessage());
			this.metadataParseResponse.addError(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}
	}

	@Override
	void handleOriginator() {
		Tag tag = FgdcTag.Originator;
		try {
			this.metadataParseResponse.metadata.setOriginator(getDocumentValue(tag));
		} catch (Exception e) {
			logger.error("handleOriginator: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}		
	}

	@Override
	void handlePublisher(){
		Tag tag = FgdcTag.Publisher;
		try {
			this.metadataParseResponse.metadata.setPublisher(getDocumentValue(tag));
		} catch (Exception e) {
			logger.error("handlePublisher: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}		
	}

	@Override
	void handleLayerName() {
		Tag tag = FgdcTag.LayerName;
		try {
			this.metadataParseResponse.metadata.setOwsName(getDocumentValue(tag));
		} catch (Exception e) {
			logger.error("handleLayerName: " + e.getMessage());
			this.metadataParseResponse.addError(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}
	}

	@Override
	void handleAbstract() {
		Tag tag = FgdcTag.Abstract;
		try {
			this.metadataParseResponse.metadata.setDescription(getDocumentValue(tag));
		} catch (Exception e) {
			logger.error("handleAbstract: " + e.getMessage());
			this.metadataParseResponse.addWarning(tag.toString(), tag.getTagName(), e.getClass().getName(), e.getMessage());
		}		
	}

	@Override
	void handleFullText() {
		this.metadataParseResponse.metadata.setFullText(getFullText());		
	}
}
