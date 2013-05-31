package org.OpenGeoPortal.Ingest.Mvc;

public class MetadataUploadForm {
	/*
	 * @RequestParam("institution") String institution, @RequestParam("ingestOption") String options, 
				@RequestParam("requiredFields") String[] requiredFields, @RequestParam("fgdcFile[]") MultipartFile[] fgdcFile
	 */
	
	//checkbox
	String[] requiredFields;
	
	KeywordRequirement placeKeywordRequirement;
	KeywordRequirement themeKeywordRequirement;

	//radio button
	String ingestOption;
	
	//dropdown box
	String institution;


	public KeywordRequirement getPlaceKeywordRequirement() {
		return placeKeywordRequirement;
	}


	public void setPlaceKeywordRequirement(
			KeywordRequirement placeKeywordRequirement) {
		this.placeKeywordRequirement = placeKeywordRequirement;
	}


	public KeywordRequirement getThemeKeywordRequirement() {
		return themeKeywordRequirement;
	}


	public void setThemeKeywordRequirement(
			KeywordRequirement themeKeywordRequirement) {
		this.themeKeywordRequirement = themeKeywordRequirement;
	}


	public String[] getRequiredFields() {
		return requiredFields;
	}


	public void setRequiredFields(String[] requiredFields) {
		this.requiredFields = requiredFields;
	}


	public String getIngestOption() {
		return ingestOption;
	}


	public void setIngestOption(String ingestOption) {
		this.ingestOption = ingestOption;
	}


	public String getInstitution() {
		return institution;
	}


	public void setInstitution(String institution) {
		this.institution = institution;
	}



 
}
