package org.OpenGeoPortal.Keyword.KeywordThesauri;

public interface KeywordThesaurus {

	boolean parsable(String docThesaurus);

	String getPreferredThesaurusName();
	String getThesaurusDescription();
}
