package org.OpenGeoPortal.Keyword;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeywordCheckerImpl implements KeywordChecker {

	/*private Boolean verifyThemeAuthorities(List<ThemeKeywords> themeKeywords){
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
			missingAuthorities.add(ThemeKeywordAuthority.FGDCKeywords.getAuthorityId()[0]);
		}
		if (!iso){
			missingAuthorities.add(ThemeKeywordAuthority.ISOKeywords.getAuthorityId()[0]);
		}
		if (!lcsh){
			missingAuthorities.add(ThemeKeywordAuthority.LCSHKeywords.getAuthorityId()[0]);
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
	}*/
}
