package org.OpenGeoPortal.Ingest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.OpenGeoPortal.Layer.AccessLevel;
import org.OpenGeoPortal.Layer.BoundingBox;
import org.OpenGeoPortal.Layer.GeometryType;
import org.OpenGeoPortal.Layer.Metadata;
import org.OpenGeoPortal.Layer.PlaceKeywords;
import org.OpenGeoPortal.Layer.PlaceKeywords.PlaceKeywordAuthority;
import org.OpenGeoPortal.Layer.ThemeKeywords;
import org.OpenGeoPortal.Layer.ThemeKeywords.ThemeKeywordAuthority;
import org.OpenGeoPortal.Solr.SolrClient;
import org.OpenGeoPortal.Solr.SolrRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class ingests metadata into Solr from Metadata objects    
 *  
 * @author smcdon08, chrissbarnett
 *
 */
public abstract class AbstractSolrIngest implements SolrIngest 
{
		public Metadata metadata;
		public SolrClient solrClient;
		protected final Logger logger = LoggerFactory.getLogger(this.getClass());
		public enum MetadataElement {
			Title, 
			Abstract, 
			LayerName, 
			Publisher, 
			Originator, 
			Bounds, 
			ThemeKeywords, 
			PlaceKeywords, 
			Access,
			LayerId,
			WorkspaceName,
			Location,
			Institution,
			FullText,
			DataType,
			Georeferenced,
			ContentDate
		}
		
		Set<MetadataElement> requiredElementsSet = new HashSet<MetadataElement>();
		protected SolrIngestResponse solrIngestResponse;
		
		public void setRequiredElements(Set<MetadataElement> requiredElements){
			requiredElementsSet.clear();
			requiredElementsSet.addAll(requiredElements);
		}
		
		public Metadata getMetadata(){
			return this.metadata;
		}
		
		public void setMetadata(Metadata metadata){
				this.metadata = metadata;
		}
		
		public SolrIngestResponse auditSolr(Metadata metadata, Set<MetadataElement> requiredElements) throws Exception {
			setRequiredElements(requiredElements);
			setMetadata(metadata);
		
			solrIngestResponse = metadataToSolr();
			logger.debug(solrIngestResponse.solrRecord.toString());
			return solrIngestResponse;
		}
		
		public SolrIngestResponse writeToSolr(Metadata metadata) throws Exception {
			Set<MetadataElement> requiredElements = new HashSet<MetadataElement>();
			requiredElements.add(MetadataElement.LayerId);
			requiredElements.add(MetadataElement.LayerName);
			requiredElements.add(MetadataElement.Title);

			return writeToSolr(metadata, requiredElements);
		}
		
		public SolrIngestResponse writeToSolr(Metadata metadata, Set<MetadataElement> requiredElements) throws Exception {
			setRequiredElements(requiredElements);
			setMetadata(metadata);
		
			solrIngestResponse = metadataToSolr();
			logger.debug(solrIngestResponse.solrRecord.toString());
			
			if (solrIngestResponse.ingestErrors.isEmpty()){
				solrIngestResponse.convertedMetadata = true;
				int solrResponse = solrClient.add(solrIngestResponse.solrRecord);
				if (solrResponse == 0){
					//successfully added to solr
					solrIngestResponse.addedRecord = true;
					logger.info("Successfully added layer to solr.");
				} else {
					//failed to add to solr
					logger.error("Failed to add layer to Solr, Status Code:" + Integer.toString(solrResponse));
				}
			} 
			
			return solrIngestResponse;
		}

		SolrIngestResponse metadataToSolr() throws Exception {
			solrIngestResponse = new SolrIngestResponse();
			SolrRecord solrRecord = solrIngestResponse.solrRecord;
			String institution = metadata.getInstitution();

			try {
				solrRecord.setName(processName());
			} catch (Exception e){
				solrIngestResponse.addError("LayerName", "Name", "Unspecified error setting value.", "");
			}
			try {
				solrRecord.setLayerId(processLayerId(institution));
			} catch (Exception e){
				solrIngestResponse.addError("LayerId", "LayerId", "Unspecified error setting value.", "");
			}
			try{
				solrRecord.setInstitution(institution);
			} catch (Exception e){
				solrIngestResponse.addError("Institution", "Institution", "Unspecified error setting value.", "");
			}
			
			try{
			if (processBounds()){
				solrRecord.setMinX(processMinX());
				solrRecord.setMaxX(processMaxX());
				solrRecord.setMinY(processMinY());
				solrRecord.setMaxY(processMaxY());
				
				//calculated fields
				solrRecord.setHalfHeight(processHalfHeight());
				solrRecord.setHalfWidth(processHalfWidth());

				solrRecord.setCenterX(processCenterX());
				solrRecord.setCenterY(processCenterY());
				
				solrRecord.setArea(processArea());
			}
			} catch (Exception e){
				solrIngestResponse.addError("Bounds", "bounds", "Unspecified error setting value.", "");
			}
			try{
				solrRecord.setContentDate(processContentDate());
			} catch (Exception e){
				solrIngestResponse.addError("Content Date", "Date", "Unspecified error setting value.", "");
			}
			try{
				solrRecord.setThemeKeywords(processThemeKeywords());
			} catch (Exception e){
				solrIngestResponse.addError("Theme Keywords", "themekeywords", "Unspecified error setting value.", "");
				e.printStackTrace();
			}
			try{
				solrRecord.setPlaceKeywords(processPlaceKeywords());
			} catch (Exception e){
				solrIngestResponse.addError("Place Keywords", "placekeywords", "Unspecified error setting value.", "");
			}
			try{
				solrRecord.setAccess(processAccess());
			} catch (Exception e){
				solrIngestResponse.addError("Access", "Access", "Unspecified error setting value.", "");
			}
			try{
			String publisher = processPublisher();
			solrRecord.setPublisher(publisher);
			} catch (Exception e){
				solrIngestResponse.addError("Publisher", "Publisher", "Unspecified error setting value.", "");
			}
			try{
			String originator = processOriginator();
			solrRecord.setOriginator(originator);
			} catch (Exception e){
				solrIngestResponse.addError("Originator", "Originator", "Unspecified error setting value.", "");
			}
			try{
			String dataType = processDataType();
			solrRecord.setDataType(dataType);
			} catch (Exception e){
				solrIngestResponse.addError("Data type", "DataType", "Unspecified error setting value.", "");
			}
			try{
			String layerDisplayName = processTitle();
			solrRecord.setLayerDisplayName(layerDisplayName);
			} catch (Exception e){
				solrIngestResponse.addError("Title", "LayerDisplayName", "Unspecified error setting value.", "");
			}
			try{
			solrRecord.setFgdcText(processFullText());
			} catch (Exception e){
				solrIngestResponse.addError("Full Text", "FgdcText", "Unspecified error setting value.", "");
			}
			try{
			solrRecord.setDescription(processDescription());
			} catch (Exception e){
				solrIngestResponse.addError("Description", "Abstract", "Unspecified error setting value.", "");
			}
			try{
			solrRecord.setAvailability(processAvailability());
			} catch (Exception e){
				solrIngestResponse.addError("Availability", "Availability", "Unspecified error setting value.", "");
			}
			try{
			solrRecord.setCollectionId(processCollectionId());
			} catch (Exception e){
				solrIngestResponse.addError("CollectionId", "CollectionId", "Unspecified error setting value.", "");
			}
			try{
			solrRecord.setGeoreferenced(processGeoreferenced());
			} catch (Exception e){
				solrIngestResponse.addError("Georeferenced", "Georeferenced", "Unspecified error setting value.", "");
			}
			try{
			solrRecord.setLocation(processLocation());
			} catch (Exception e){
				solrIngestResponse.addError("Location", "Location", "Unspecified error setting value.", "");
			}		
			try{
			solrRecord.setWorkspaceName(processWorkspaceName());
			} catch (Exception e){
				solrIngestResponse.addError("WorkspaceName", "WorkspaceName", "Unspecified error setting value.", "");
			}
			return solrIngestResponse;
		}
		
		private String processWorkspaceName() {
			return metadata.getWorkspaceName();
		}

		abstract public String processGeoreferenced();

		abstract public String processCollectionId(); 
		
		abstract public String processLocation();

		abstract public String processAvailability();

		static String combine(String[] s, String glue)
		{
		  int k=s.length;
		  if (k==0)
		    return null;
		  StringBuilder out=new StringBuilder();
		  out.append(s[0]);
		  for (int x=1;x<k;++x)
		    out.append(glue).append(s[x]);
		  return out.toString();
		}
		
		
		public String processPlaceKeywords() {
			List<PlaceKeywords> placeKeywords = metadata.getPlaceKeywords();
			logger.info("place keywords");
			for (PlaceKeywords placeKeyword: placeKeywords){
				logger.info("Keyword Authority: " + placeKeyword.getKeywordAuthority().getAuthorityId());
				for (String keywordString : placeKeyword.getKeywords()){
					logger.info("Keyword: " + keywordString);
				}
			}

			if (requiredElementsSet.contains(MetadataElement.PlaceKeywords)){
				if (verifyPlaceAuthorities(placeKeywords)){
					if (placeKeywords.isEmpty()){
						this.solrIngestResponse.addError("PlaceKeyword", "PlaceKeyword", "Missing Keywords", "No Place Keywords found");
					}

				} else {
					
				}
			}
			return getPlaceKeywordsAsString(placeKeywords);
			
		}
			
		
		private Boolean verifyPlaceAuthorities(List<PlaceKeywords> placeKeywords) {
			Boolean verified = false;
			for (PlaceKeywords placeKeyword: placeKeywords){
				PlaceKeywordAuthority currentAuthority = placeKeyword.getKeywordAuthority();
				if (currentAuthority.equals(PlaceKeywordAuthority.GNISKeywords)|| 
						currentAuthority.equals(PlaceKeywordAuthority.GNSKeywords)||
						currentAuthority.equals(PlaceKeywordAuthority.LCNHKeywords)){
					verified = true;
					break;
				}
			}
			
			if (!verified){
				if (requiredElementsSet.contains(MetadataElement.PlaceKeywords)){
					this.solrIngestResponse.addError("PlaceKeyword", "PlaceKeyword", "MissingKeywords", "Missing required Place Keyword thesaurus (1 of 'GNIS', 'GNS', or 'LCNH')");
				} else {
					this.solrIngestResponse.addWarning("PlaceKeyword", "PlaceKeyword", "MissingKeywords", "Missing required Place Keyword thesaurus (1 of 'GNIS', 'GNS', or 'LCNH')");
				}
			}
					
			return verified;
		}

		private Boolean verifyThemeAuthorities(List<ThemeKeywords> themeKeywords){
			Boolean fgdc = false;
			Boolean iso = false;
			Boolean lcsh = false;
			Set<String> missingAuthorities = new HashSet<String>();

			for (ThemeKeywords themeKeyword: themeKeywords){
				ThemeKeywordAuthority currentAuthority = themeKeyword.getKeywordAuthority();
				if (currentAuthority.equals(ThemeKeywordAuthority.FGDCKeywords)){
					if (!themeKeyword.getKeywords().isEmpty()){
						fgdc = true;
					} 
				} else if (currentAuthority.equals(ThemeKeywordAuthority.ISOKeywords)){
					if (checkIsoThemes(themeKeyword.getKeywords())){
						iso = true;
					} 
				} else if (currentAuthority.equals(ThemeKeywordAuthority.LCSHKeywords)){
					if (!themeKeyword.getKeywords().isEmpty()){
						lcsh = true;
					} 
				}
			}
			if (!fgdc){
				missingAuthorities.add(ThemeKeywordAuthority.FGDCKeywords.getAuthorityId());
			}
			if (!iso){
				missingAuthorities.add(ThemeKeywordAuthority.ISOKeywords.getAuthorityId());
			}
			if (!lcsh){
				missingAuthorities.add(ThemeKeywordAuthority.LCSHKeywords.getAuthorityId());
			}
			Boolean verified = fgdc && iso && lcsh;
			if (!verified){
				String missingAuthoritiesText = combine(missingAuthorities.toArray(new String[missingAuthorities.size()]), ", ");
				if (requiredElementsSet.contains(MetadataElement.ThemeKeywords)){
					this.solrIngestResponse.addError("ThemeKeyword", "ThemeKeyword", "MissingKeywords", "Missing required Theme Keyword thesaurus (" + missingAuthoritiesText + ")");
				} else {
					this.solrIngestResponse.addWarning("ThemeKeyword", "ThemeKeyword", "MissingKeywords", "Missing required Theme Keyword thesaurus (" + missingAuthoritiesText + ")");
				}
			}
					
			return verified;
		}
		
			public String processThemeKeywords() {
				List<ThemeKeywords> themeKeywords = metadata.getThemeKeywords();
				logger.info("theme keywords");
				for (ThemeKeywords themeKeyword: themeKeywords){
					logger.info(themeKeyword.getKeywordAuthority().toString());
					logger.info("Keyword Authority: " + themeKeyword.getKeywordAuthority().getAuthorityId());
					for (String keywordString : themeKeyword.getKeywords()){
						logger.info("Keyword: " + keywordString);
					}
				}
				if (requiredElementsSet.contains(MetadataElement.ThemeKeywords)){
					if (verifyThemeAuthorities(themeKeywords)){
						if (themeKeywords.isEmpty()){
							this.solrIngestResponse.addError("ThemeKeyword", "ThemeKeyword", "Missing Keywords", "No Theme Keywords found");
						}
					} 
				} else {
					if (themeKeywords.isEmpty()){
						this.solrIngestResponse.addWarning("ThemeKeyword", "ThemeKeyword", "Missing Keywords", "No Theme Keywords found");
					}
				}
				
				return getThemeKeywordsAsString(themeKeywords);
			}
			
			public String getThemeKeywordsAsString(List<ThemeKeywords> themeKeywords){
				Set<String> allKeywords = new HashSet<String>();
				for (ThemeKeywords themeKeyword: themeKeywords){
					allKeywords.addAll(themeKeyword.getKeywords());
				}
				return combine(allKeywords.toArray(new String[allKeywords.size()]), " ");
			}
			
			public String getPlaceKeywordsAsString(List<PlaceKeywords> placeKeywords){
				Set<String> allKeywords = new HashSet<String>();
				for (PlaceKeywords placeKeyword: placeKeywords){
					allKeywords.addAll(placeKeyword.getKeywords());
				}
				return combine(allKeywords.toArray(new String[allKeywords.size()]), " ");
			}

		private String processArea() {
			BoundingBox bounds = metadata.getBounds();
			Double area = getLength(bounds.getMinX(), bounds.getMaxX()) * getLength(bounds.getMinY(), bounds.getMaxY());
			return Double.toString(area);
		}

		private static Double getLength(Double a, Double b){
			return Math.abs(a - b);
		}

		private static String getCenter(Double a, Double b){
			return Double.toString((a + b)/2);
		}
		
		private String processCenterY() {
			BoundingBox bounds = metadata.getBounds();
			return getCenter(bounds.getMinY(), bounds.getMaxY());
		}

		private String processCenterX() {
			BoundingBox bounds = metadata.getBounds();
			return getCenter(bounds.getMinX(), bounds.getMaxX());
		}
		
		private static String getHalf(Double a, Double b){
			return Double.toString(Math.abs(a - b) / 2.);
		}
		
		private String processHalfWidth() {
			BoundingBox bounds = metadata.getBounds();
			return getHalf(bounds.getMinX(), bounds.getMaxX());
		}

		private String processHalfHeight() {
			BoundingBox bounds = metadata.getBounds();
			return getHalf(bounds.getMinY(), bounds.getMaxY());
		}

		private Boolean processBounds(){
			BoundingBox bounds = metadata.getBounds();
			if (requiredElementsSet.contains(MetadataElement.Bounds)){
				if (bounds == null){
					this.solrIngestResponse.addError("Bounds", "Bounds", "Bounds are null", "");
					return false;
				} else if (bounds.isValid()){
					return true;
				} else {
					this.solrIngestResponse.addError("Bounds", "Bounds", "Bounds are invalid", "");
					return false;
				}
			} else {
				if (bounds == null){
					this.solrIngestResponse.addWarning("Bounds", "Bounds", "Bounds are null", "");
					return false;
				} else if (bounds.isValid()){
					return true;
				} else {
					this.solrIngestResponse.addWarning("Bounds", "Bounds", "Bounds are invalid", "");
					return false;
				}
			}
		}
		
		private String processMaxY() {
			String coord = Double.toString(metadata.getBounds().getMaxY());
			return coord;
		}

		private String processMinY() {
			String coord = Double.toString(metadata.getBounds().getMinY());
			return coord;
		}

		private String processMaxX() {
			String coord = Double.toString(metadata.getBounds().getMaxX());
			return coord;
		}

		private String processMinX() {
			String coord = Double.toString(metadata.getBounds().getMinX());
			return coord;
		}

		private String processName() {
			String name = this.metadata.getOwsName();
			String message = "Empty Name value";
			String field = "LayerName";
			String nativeName = "Name";
			if (requiredElementsSet.contains(MetadataElement.LayerName)){
				if ((name == null)||(name.isEmpty())){
					this.solrIngestResponse.addError(field, nativeName, message, "");
					return null;
				}
			} else {
				if ((name == null)||(name.isEmpty())){
					this.solrIngestResponse.addWarning(field, nativeName, message, "Defaulting to blank 'Name'");
					name = "";
				}
			}
			return name.toUpperCase();
		}

		public String processFullText(){
			String fullText = metadata.getFullText();
			return fullText;
		}

		private String processTitle() {
			String title = this.metadata.getTitle();
			String message = "Empty title value";
			String field = "title";
			String nativetitle = "LayerName";
			if (requiredElementsSet.contains(MetadataElement.Title)){
				if ((title == null)||(title.isEmpty())){
					this.solrIngestResponse.addError(field, nativetitle, message, "");
					return null;
				}
			} else {
				if ((title == null)||(title.isEmpty())){
					this.solrIngestResponse.addWarning(field, nativetitle, message, "Defaulting to blank 'Title'.");
					title = "";
				}
			}
			return title;
		}

		private String processDescription() {
			String description = this.metadata.getDescription();
			String message = "Empty abstract value";
			String field = "description";
			String nativeName = "Abstract";
			if (requiredElementsSet.contains(MetadataElement.Abstract)){
				if ((description == null)||(description.isEmpty())){
					this.solrIngestResponse.addError(field, nativeName, message, "");
					return null;
				}
			} else {
				if ((description == null)||(description.isEmpty())){
					this.solrIngestResponse.addWarning(field, nativeName, message, "Defaulting to blank 'Abstract'.");
					description = "";
				}
			}
			return description;
		}

		private String processDataType() {
			GeometryType geometry = this.metadata.getGeometryType();
			String geometryVal = "Undefined";
			String field = "GeometryType";
			String nativeName = "DataType";
			if (requiredElementsSet.contains(MetadataElement.DataType)){
				if (geometry == null){
					this.solrIngestResponse.addError(field, nativeName, "No Data Type found.", "");
					return null;
				} else {
					geometryVal = geometry.toString();
				}
			} else {
				geometryVal = geometry.toString();
				if (geometryVal.equalsIgnoreCase("undefined")){
					this.solrIngestResponse.addWarning(field, nativeName, "Data Type undefined for this layer.", "Defaulting to DataType 'Undefined'");
				}
			}
			return geometryVal;
		}

		private String processOriginator() {
			String originator = this.metadata.getOriginator();
			String message = "Empty Originator value";
			String field = "originator";
			String nativeName = "Originator";
			if (requiredElementsSet.contains(MetadataElement.Originator)){
				if ((originator == null)||(originator.isEmpty())){
					this.solrIngestResponse.addError(field, nativeName, message, "");
					return null;
				}
			} else {
				if ((originator == null)||(originator.isEmpty())){
					this.solrIngestResponse.addWarning(field, nativeName, message, "Defaulting to blank 'Originator'");
					originator = "";
				}
			}
			return originator;
		}

		private String processPublisher() {
			String publisher = this.metadata.getPublisher();
			String message = "Empty publisher value";
			String field = "publisher";
			String nativeName = "publisher";
			if (requiredElementsSet.contains(MetadataElement.Publisher)){
				if ((publisher == null)||(publisher.isEmpty())){
					this.solrIngestResponse.addError(field, nativeName, message, "");
					return null;
				}
			} else {
				if ((publisher == null)||(publisher.isEmpty())){
					this.solrIngestResponse.addWarning(field, nativeName, message, "Defaulting to blank 'Publisher'");
					publisher = "";
				}
			}
			return publisher;
		}

		private String processLayerId(String institution) {
			if ((institution == null)||(institution.isEmpty())){
				this.solrIngestResponse.addError("institution", "institution", "Institution must be specified", "Institution must be specified");
			}
			String name = this.metadata.getId();
			if ((name == null)||(name.isEmpty())){
				this.solrIngestResponse.addError("Layer Name", "Name", "Layer Name must be specified.", "Layer Name must be specified.");
			}
			
			String layerId = institution + "." + name;
			return layerId;
		}

		private String processAccess() {
			AccessLevel access = this.metadata.getAccess();
			//defaults to public
			String accessVal = "Public";
			if (requiredElementsSet.contains(MetadataElement.Access)){
				if (access == null){
					this.solrIngestResponse.addError("Access", "Access", "No value found for access constraints.", "");
				} else {
					accessVal = access.toString();
				}
			} else {
				if (access != null){
					accessVal = access.toString();
				}
			}
			return accessVal;
		}
		
		/**
		 *   solr requires the date to be something like 1995-12-31T23:59:59Z
		 * as more data becomes available, it will have to do more error checking
		 * @return
		 * 
		 */
		protected String processContentDate() {
			String passedYear = metadata.getContentDate();
			if ((passedYear == null)|| !isYear(passedYear)){
				if (requiredElementsSet.contains(MetadataElement.ContentDate)){
					passedYear += "";
					this.solrIngestResponse.addError("ContentDate", "ContentDate", "Invalid Date", "Value passed: " + passedYear);
					return null;
				} else {
					this.solrIngestResponse.addWarning("ContentDate", "ContentDate", "Invalid Date", "Defaulting to year '0001'");
					passedYear = "0001";
				}
			} 
			String returnValue = passedYear + "-01-01T01:01:01Z";
			return returnValue;
		}
		
		/**
		 * can the passed string be parsed as an int
		 * @param year$
		 * @return
		 */
		protected boolean isYear(String year$)
		{
			try
			{
				int year = Integer.parseInt(year$);
				return true;
			}
			catch (NumberFormatException e)
			{
				return false;
			}
		}
				


		/**
		 * return an html formatted string describing the elements a layer 
		 * @param layerInfo
		 * @return
		 */
		public String getSolrReport(Metadata metadata)
		{
			//include info for missing tags here:
			String returnString = "<br/><b>Summary Of Layer Values</b></center><br/>";
			//projection is obsolete
			//String projectionCode = getValue(layerInfo, Key.SrsProjectionCode);
			//String workspace = metadata.getWorkspaceName();
			try {
			returnString += "Layer Name = " + metadata.getOwsName() + "<br/>\n";
			} catch (NullPointerException e){
			}
			try {
			returnString += "Access = " + metadata.getAccess().toString() + "<br/>\n";
			} catch (NullPointerException e){
			}
			try {
			String solrType = metadata.getGeometryType().toString(); 

			returnString += "Data Type = " + solrType + "<br/>\n";
			} catch (NullPointerException e){
			}
			try{
			returnString += "Availability = Online<br/>\n";
			returnString += "Layer Display Name = " + metadata.getTitle() + "<br/>\n";
			} catch (NullPointerException e){
			}
			try{
			returnString += "Publisher = " + metadata.getPublisher() + "<br/>\n";
			} catch (NullPointerException e){
			}
			try{
			returnString += "Originator = " + metadata.getOriginator() + "<br/>\n";
			} catch (NullPointerException e){
			}
			try{
			returnString += "Theme Keywords = " + getThemeKeywordsAsString(metadata.getThemeKeywords()) + "<br/>\n";
			} catch (NullPointerException e){
			}
			try{
			returnString += "Place Keywords = " + getPlaceKeywordsAsString(metadata.getPlaceKeywords()) + "<br/>\n";
			} catch (NullPointerException e){
			}
			try{
				returnString += "Date = " + metadata.getContentDate() + "<br/>\n";
			//returnString += "Workspace Name = " + workspace + "<br/>\n";
			//returnString += "Abstract = " + getValue(layerInfo, Key.Abstract) + "<br/>\n";
			} catch (NullPointerException e){
			}
			return returnString;
			
		}
		
		static String processReportValue(String value){
			
			return null;
		}
		
		protected Boolean checkIsoThemes(Set<String> documentThemes)
		{
			String[] isoThemeKeywords = {"farming", "biota", "boundaries", "climatologyMeteorologyAtmosphere", "economy",
					"elevation", "environment", "geoscientificInformation", "health", "imageryBaseMapsEarthCover", 
					"intelligenceMilitary", "inlandWaters", "location", "oceans", "planningCadastre", "society", 
					"structure", "transportation", "utilitiesCommunication"};
			for (String currentIsoThemeKeyword : isoThemeKeywords)
			{
				if (documentThemes.contains(currentIsoThemeKeyword)){
					logger.info("ISO Theme:" + currentIsoThemeKeyword);
					return true;
				}
			}
			return false;
		}
		
		/**
		 * document should have ISO and LCSH/FGDC themes defined if not, added to
		 * the list of missing tags
		 * 
		 * @param String locationThemes
		 */
		protected Boolean checkLocationThemes(String locationThemes) {
			Boolean locationThemesOk = true;
			if (locationThemes == null){
				locationThemesOk = false;
			} else if (locationThemes.contains("FGDC") == false){
				locationThemesOk = false;
				//missingParseTags.add("LCSH/FGDC theme");
			}
			return locationThemesOk;
		}
}
