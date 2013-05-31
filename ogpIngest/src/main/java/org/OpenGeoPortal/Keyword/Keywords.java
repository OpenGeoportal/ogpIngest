package org.OpenGeoPortal.Keyword;

import java.util.HashSet;
import java.util.Set;

import org.OpenGeoPortal.Keyword.KeywordThesauri.KeywordThesaurus;

public class Keywords {
		
	private KeywordThesaurus keywordThesaurus;
	private Set<String> keywords = new HashSet<String>();
	
	public void addKeyword(String keyword){
		keywords.add(keyword.trim());
	}
	
	public Set<String> getKeywords(){
		return keywords;
	}

	public KeywordThesaurus getKeywordThesaurus() {
		return keywordThesaurus;
	}

	public void setKeywordThesaurus(KeywordThesaurus keywordThesaurus) {
		this.keywordThesaurus = keywordThesaurus;
	}
	
}
