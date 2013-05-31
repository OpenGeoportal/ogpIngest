package org.OpenGeoPortal.Keyword.KeywordThesauri;

import java.util.HashSet;
import java.util.Set;

public class FgdcThemeKeywordThesaurus extends AbstractKeywordThesaurus implements ThemeKeywordThesaurus{
	private static final String THESAURUS_NAME = "FGDC";
	private static final String THESAURUS_DESCRIPTION = "FGDC keywords";
	private static final Boolean STRICT = false;
	
	FgdcThemeKeywordThesaurus(){
		init();
	}
	
	public void init(){
		this.setPreferredThesaurusName(THESAURUS_NAME);
		this.setThesaurusDescription(THESAURUS_DESCRIPTION);
		
	}

	@Override
	public Boolean isRestrictedToControlledVocab() {
		//we should pull from a controlled vocab, but since we don't have it right now, skip it
		return false;
	}

	@Override
	public Set<String> getNameVariants() {
		// TODO Auto-generated method stub
		Set<String> nameVariants = new HashSet<String>();
		//nameVariants.add(e);
		return nameVariants;
	}
	
	@Override
	public Boolean isStrict() {
		return STRICT;
	}
	
}
