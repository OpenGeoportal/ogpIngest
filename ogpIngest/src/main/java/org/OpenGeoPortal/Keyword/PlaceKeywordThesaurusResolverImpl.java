package org.OpenGeoPortal.Keyword;

import java.util.Set;

import org.OpenGeoPortal.Keyword.KeywordThesauri.PlaceKeywordThesaurus;
import org.OpenGeoPortal.Keyword.KeywordThesauri.UnrecognizedKeywordThesaurus;
import org.OpenGeoPortal.Keyword.KeywordThesauri.UnspecifiedKeywordThesaurus;
import org.springframework.beans.factory.annotation.Autowired;

public class PlaceKeywordThesaurusResolverImpl implements PlaceKeywordThesaurusResolver {
	  private Set<PlaceKeywordThesaurus> placeKeywordThesauri;

	  @Autowired
	  public void setPlaceKeywordThesauri(Set<PlaceKeywordThesaurus> keywordThesauri) {
	      this.placeKeywordThesauri = keywordThesauri;
	  }
	  
	  public PlaceKeywordThesaurus getPlaceKeywordThesaurus(String docThesaurus){
		  if (docThesaurus.trim().length() == 0){
			  return (PlaceKeywordThesaurus) new UnspecifiedKeywordThesaurus();
		  }
		  for (PlaceKeywordThesaurus thesaurus: placeKeywordThesauri){
			  if(thesaurus.parsable(docThesaurus)){
				  return thesaurus;
			  }
		  }
		  
		  return (PlaceKeywordThesaurus) new UnrecognizedKeywordThesaurus(docThesaurus);

	  }
	  
	  public Set<PlaceKeywordThesaurus> getPlaceKeywordThesauri(){
		  return this.placeKeywordThesauri;
	  }
}
