package org.OpenGeoPortal.Keyword.KeywordThesauri;

import java.util.HashSet;
import java.util.Set;

public class UnrecognizedKeywordThesaurus extends AbstractKeywordThesaurus {
	static final String THESAURUS_NAME = "unrecognized";
	static final String THESAURUS_DESCRIPTION = "Keyword thesaurus is unrecognized.";
	static final Boolean STRICT = false;
	
	public UnrecognizedKeywordThesaurus(String docThesaurus){
		init(docThesaurus);
	}
	
	public void init(String docThesaurus){
		this.setPreferredThesaurusName(THESAURUS_NAME);
		this.setThesaurusDescription(THESAURUS_DESCRIPTION + " ['" + docThesaurus + "']");
		
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
