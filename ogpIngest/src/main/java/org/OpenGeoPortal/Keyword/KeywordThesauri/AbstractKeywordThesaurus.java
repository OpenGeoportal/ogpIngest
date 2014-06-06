package org.OpenGeoPortal.Keyword.KeywordThesauri;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKeywordThesaurus {
	/*	ISOKeywords(new String[]{"ISO 19115"}),
		FGDCKeywords(new String[] {"FGDC"}),
		LCSHKeywords(new String[] {"LCSH", "Library of Congress Subject Headings"}),
		Unrecognized(new String[] {"unrecognized"}),
		Unspecified(new String[] {"unspecified"});
		
		*/
		protected String preferredName;

		protected String thesaurusDescription;
		protected Boolean strict;

		protected Set<String> controlledVocabulary;
		protected Boolean allowOnlyFromCV;
		
		final Logger logger = LoggerFactory.getLogger(this.getClass());	

		public abstract Boolean isStrict();
		
		public String getPreferredThesaurusName() {
			return preferredName;
		}

		public void setPreferredThesaurusName(String thesaurusName) {
			this.preferredName = thesaurusName;
		}

		public abstract Set<String> getNameVariants();

		public String getThesaurusDescription() {
			return thesaurusDescription;
		}

		public void setThesaurusDescription(String thesaurusDescription) {
			this.thesaurusDescription = thesaurusDescription;
		}
		
		public boolean parsable(String keywordAuthorityString){
			keywordAuthorityString = keywordAuthorityString.toLowerCase().trim();
			if (keywordAuthorityString.equalsIgnoreCase(preferredName)||getNameVariants().contains(keywordAuthorityString)){
				return true;
			} else {
				return false;
			}
		}
		
		public Set<String> getControlledVocabulary(){
			return this.controlledVocabulary;
		}
		
		public abstract Boolean isRestrictedToControlledVocab();
		
		/**
		 * reads the Terms file into memory
		 * @throws java.io.IOException
		 */
		public void loadTermsFile(File termsFile) throws IOException {
			controlledVocabulary = new HashSet<String>();
			for(String line: FileUtils.readLines(termsFile)){
				line = line.toLowerCase().trim();
				controlledVocabulary.add(line);
			}
		}
		
		//returns true only if all terms from the document are from the controlled vocabulary list
		public Boolean checkAllowedKeywordsStrict(Set<String> documentKeywords){
			Set<String> allowedThemeKeywords = this.getControlledVocabulary();
			for (String documentKeyword : documentKeywords){
				
				if (!allowedThemeKeywords.contains(documentKeyword)){
					return false;
				}
				
			}
			return true;
		}
		
		//returns true if one term from the controlled vocabulary list is in the document
		public Boolean checkAllowedKeywordsLoose(Set<String> documentKeywords){
			Set<String> allowedThemeKeywords = this.getControlledVocabulary();
			for (String documentKeyword : documentKeywords){
				
				if (allowedThemeKeywords.contains(documentKeyword)){
					logger.debug("Theme Keyword:" + documentKeyword);
					return true;
				}
				
			}
			return false;
		}
	
}
