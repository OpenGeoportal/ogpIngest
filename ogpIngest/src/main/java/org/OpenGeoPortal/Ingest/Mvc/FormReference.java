package org.OpenGeoPortal.Ingest.Mvc;

import java.io.IOException;
import java.util.Map;

public interface FormReference {

	Map<String,String> getInstitutionMap() throws IOException;
	Map<String,String> getMetadataElementMap();
	Map<String,String> getAvailablePlaceKeywordThesaurusMap();
	Map<String,String> getAvailableThemeKeywordThesaurusMap();
	Map<String,String> getAllowedThemeKeywordThesaurusMap();
	Map<String,String> getAllowedPlaceKeywordThesaurusMap();
}
