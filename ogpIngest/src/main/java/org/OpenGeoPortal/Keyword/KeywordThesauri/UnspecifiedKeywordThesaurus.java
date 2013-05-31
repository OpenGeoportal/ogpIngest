package org.OpenGeoPortal.Keyword.KeywordThesauri;

import java.util.HashSet;
import java.util.Set;

public class UnspecifiedKeywordThesaurus extends AbstractKeywordThesaurus {
	static final String THESAURUS_NAME = "unspecified";
	static final String THESAURUS_DESCRIPTION = "No specified keyword thesaurus.";
	static final Boolean STRICT = false;
	
	public UnspecifiedKeywordThesaurus(){
		init();
	}
	
	public void init(){
		this.setPreferredThesaurusName(THESAURUS_NAME);
		this.setThesaurusDescription(THESAURUS_DESCRIPTION);
		
	}

	@Override
	public Boolean isRestrictedToControlledVocab() {
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
