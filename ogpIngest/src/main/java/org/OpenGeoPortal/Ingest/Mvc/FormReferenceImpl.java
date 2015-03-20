package org.OpenGeoPortal.Ingest.Mvc;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.OpenGeoPortal.Ingest.IngestProperties;
import org.OpenGeoPortal.Ingest.Metadata.MetadataElement;
import org.OpenGeoPortal.Keyword.PlaceKeywordThesaurusResolver;
import org.OpenGeoPortal.Keyword.ThemeKeywordThesaurusResolver;
import org.OpenGeoPortal.Keyword.KeywordThesauri.PlaceKeywordThesaurus;
import org.OpenGeoPortal.Keyword.KeywordThesauri.ThemeKeywordThesaurus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class FormReferenceImpl implements FormReference {
	@Autowired
	IngestProperties ingestProperties;
	
	@Autowired
	PlaceKeywordThesaurusResolver placeKeywordThesaurusResolver;
	
	@Autowired
	ThemeKeywordThesaurusResolver themeKeywordThesaurusResolver;
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	

	public Map<String,String> getInstitutionMap() throws IOException {
		String[] institutions = ingestProperties.getPropertyArray("institutions");
		
		//Data referencing for required metadata element checkboxes 
		Map<String,String> uiList = new LinkedHashMap<String,String>();
		for (String institution: institutions){
			logger.debug(institution);
			String institutionDisplay = institution.substring(0,1).toUpperCase() + institution.substring(1);
			logger.debug(institutionDisplay);
			try {
				institutionDisplay = ingestProperties.getProperty(institution + ".displayName").trim();
			} catch (Exception e){
				//ignore exceptions
			}
			uiList.put(institution, institutionDisplay);
		}

 
		return uiList;
	}
	
	public Map<String,String> getMetadataElementMap() {

		//Data referencing for required metadata element checkboxes
		Map<String,String> uiList = new LinkedHashMap<String,String>();
		for (MetadataElement element: MetadataElement.values()){
			uiList.put(element.toString(), element.getDisplayName());
		}
 
		return uiList;
	}
	
	public Map<String,String> getAvailablePlaceKeywordThesaurusMap(){
		Set<PlaceKeywordThesaurus> placeKeywordThesauri = placeKeywordThesaurusResolver.getPlaceKeywordThesauri();
		Map<String,String> uiList = new LinkedHashMap<String,String>();
		for (PlaceKeywordThesaurus thesaurus: placeKeywordThesauri){
			logger.info(thesaurus.getPreferredThesaurusName());
			logger.info(thesaurus.getThesaurusDescription());
			uiList.put(thesaurus.getPreferredThesaurusName(), thesaurus.getThesaurusDescription());
		}
		return uiList;
		
	}
	
	public Map<String,String> returnAllowed(Map<String,String> uiList, String[] allowedArray){
		if (!allowedArray[0].equalsIgnoreCase("all")){
			Map<String,String> adjustedMap = new LinkedHashMap<String,String>();
			for (String key: uiList.keySet()){
				for (String allowedKey: allowedArray){
					if (allowedKey.equalsIgnoreCase(key)){
						adjustedMap.put(key, uiList.get(key));
					}
				}
			}
			return adjustedMap;
		} else {
			return uiList;
		}
	}
	
	public Map<String,String> getAllowedPlaceKeywordThesaurusMap(){
		Map<String,String> uiList = this.getAvailablePlaceKeywordThesaurusMap();
		String[] allowedArray = {};
		try {
			allowedArray = ingestProperties.getPropertyArray("allowedPlaceThesauri");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return returnAllowed(uiList, allowedArray);
	}
	
	public Map<String,String> getAvailableThemeKeywordThesaurusMap(){
		Set<ThemeKeywordThesaurus> themeKeywordThesauri = themeKeywordThesaurusResolver.getThemeKeywordThesauri();
		Map<String,String> uiList = new LinkedHashMap<String,String>();
		for (ThemeKeywordThesaurus thesaurus: themeKeywordThesauri){
			logger.info(thesaurus.getPreferredThesaurusName());
			logger.info(thesaurus.getThesaurusDescription());
			uiList.put(thesaurus.getPreferredThesaurusName(), thesaurus.getThesaurusDescription());
		}
		return uiList;
		
	}
	
	public Map<String,String> getAllowedThemeKeywordThesaurusMap(){
		Map<String,String> uiList = this.getAvailableThemeKeywordThesaurusMap();
		String[] allowedArray = {};
		try {
			allowedArray = ingestProperties.getPropertyArray("allowedThemeThesauri");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return returnAllowed(uiList, allowedArray);
	}
}
