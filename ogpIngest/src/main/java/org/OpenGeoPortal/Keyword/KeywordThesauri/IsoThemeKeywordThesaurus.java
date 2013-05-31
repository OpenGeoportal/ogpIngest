package org.OpenGeoPortal.Keyword.KeywordThesauri;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.core.io.Resource;


public class IsoThemeKeywordThesaurus extends AbstractKeywordThesaurus implements ThemeKeywordThesaurus{
	//	ISOKeywords(new String[]{"ISO 19115"}),
	private static final String THESAURUS_NAME = "ISO_19115";
	private static final String THESAURUS_DESCRIPTION = "ISO 19115 Topic Categories";
	private static final Boolean STRICT = false;
	
	private Resource controlledVocab;

	IsoThemeKeywordThesaurus(Resource controlledVocab){
		this.controlledVocab = controlledVocab;
		init();
	}
	
	public void setControlledVocab(Resource controlledVocab){
		this.controlledVocab = controlledVocab;
		
	}
	
	public Resource getControlledVocab(){
		return this.controlledVocab;
	}
	
	public void init(){
		this.setPreferredThesaurusName(THESAURUS_NAME);
		this.setThesaurusDescription(THESAURUS_DESCRIPTION);
		try {
			this.loadTermsFile(controlledVocab.getFile());
		} catch (IOException e) {
			//
		}
		
	}

	@Override
	public Boolean isRestrictedToControlledVocab() {
		return true;
	}

	@Override
	public Set<String> getNameVariants() {
		// TODO Auto-generated method stub
		Set<String> nameVariants = new HashSet<String>();
		nameVariants.add("ISO19115");
		nameVariants.add("ISO 19115");

		return nameVariants;
	}

	@Override
	public Boolean isStrict() {
		return STRICT;
	}
	
}
