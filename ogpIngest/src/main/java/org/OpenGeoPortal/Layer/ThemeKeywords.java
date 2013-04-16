package org.OpenGeoPortal.Layer;

import java.util.HashSet;
import java.util.Set;

public class ThemeKeywords {
	
	public enum ThemeKeywordAuthority {
		ISOKeywords(new String[]{"ISO 19115"}),
		FGDCKeywords(new String[] {"FGDC"}),
		LCSHKeywords(new String[] {"LCSH", "Library of Congress Subject Headings"}),
		Unrecognized(new String[] {"unrecognized"}),
		Unspecified(new String[] {"unspecified"});
		
		private final String[] authorityId;
		
		ThemeKeywordAuthority(String[] authorityId){
			this.authorityId = authorityId;
		}
		
		public String[] getAuthorityId(){
			return this.authorityId;
		}
	}
	
	private ThemeKeywordAuthority themeKeywordAuthority = ThemeKeywordAuthority.Unspecified;
	private Set<String> keywords = new HashSet<String>();
	
	public void setThesaurus(String thesaurus){
		if ((thesaurus == null)||thesaurus.isEmpty()){
			themeKeywordAuthority = ThemeKeywordAuthority.Unspecified;
			return;
		}
		thesaurus = thesaurus.trim().toUpperCase();
		for (ThemeKeywordAuthority tkauth : ThemeKeywordAuthority.values()){
			for (String authId: tkauth.getAuthorityId()){
				if (thesaurus.contains(authId.toUpperCase())){
					themeKeywordAuthority = tkauth;
					return;
				}
			}
		}
		themeKeywordAuthority = ThemeKeywordAuthority.Unrecognized;
	}
	
	public ThemeKeywordAuthority getKeywordAuthority(){
		return themeKeywordAuthority;
	}
	
	public void addKeyword(String keyword){
		keywords.add(keyword.trim());
	}
	
	public Set<String> getKeywords(){
		return keywords;
	}
}
