package org.OpenGeoPortal.Keyword;

import java.util.Set;

import org.OpenGeoPortal.Keyword.KeywordThesauri.ThemeKeywordThesaurus;
import org.OpenGeoPortal.Keyword.KeywordThesauri.UnrecognizedKeywordThesaurus;
import org.OpenGeoPortal.Keyword.KeywordThesauri.UnspecifiedKeywordThesaurus;
import org.springframework.beans.factory.annotation.Autowired;

public class ThemeKeywordThesaurusResolverImpl implements ThemeKeywordThesaurusResolver {
	  private Set<ThemeKeywordThesaurus> themeKeywordThesauri;

	  @Autowired
	  public void setThemeKeywordThesauri(Set<ThemeKeywordThesaurus> keywordThesauri) {
	      this.themeKeywordThesauri = keywordThesauri;
	  }
	  
	  public ThemeKeywordThesaurus getThemeKeywordThesaurus(String docThesaurus){
		  if (docThesaurus.trim().length() == 0){
			  return (ThemeKeywordThesaurus) new UnspecifiedKeywordThesaurus();
		  }
		  for (ThemeKeywordThesaurus thesaurus: themeKeywordThesauri){
			  if(thesaurus.parsable(docThesaurus)){
				  return thesaurus;
			  }
		  }
		  
		  return (ThemeKeywordThesaurus) new UnrecognizedKeywordThesaurus(docThesaurus);
	  }
	  
	  public Set<ThemeKeywordThesaurus> getThemeKeywordThesauri() {
		  return this.themeKeywordThesauri;
	  }

}
