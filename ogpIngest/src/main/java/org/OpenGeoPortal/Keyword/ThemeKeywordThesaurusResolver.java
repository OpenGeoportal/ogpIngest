package org.OpenGeoPortal.Keyword;

import java.util.Set;

import org.OpenGeoPortal.Keyword.KeywordThesauri.ThemeKeywordThesaurus;

public interface ThemeKeywordThesaurusResolver {
	  void setThemeKeywordThesauri(Set<ThemeKeywordThesaurus> keywordThesauri);
	  Set<ThemeKeywordThesaurus> getThemeKeywordThesauri();
	  ThemeKeywordThesaurus getThemeKeywordThesaurus(String docThesaurus);
}
