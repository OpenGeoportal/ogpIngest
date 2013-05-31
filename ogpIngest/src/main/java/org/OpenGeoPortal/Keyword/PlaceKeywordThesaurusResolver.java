package org.OpenGeoPortal.Keyword;

import java.util.Set;

import org.OpenGeoPortal.Keyword.KeywordThesauri.PlaceKeywordThesaurus;

public interface PlaceKeywordThesaurusResolver {
	  void setPlaceKeywordThesauri(Set<PlaceKeywordThesaurus> keywordThesauri);
	  Set<PlaceKeywordThesaurus> getPlaceKeywordThesauri();
	  PlaceKeywordThesaurus getPlaceKeywordThesaurus(String docThesaurus);
}
