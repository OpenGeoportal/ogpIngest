package org.OpenGeoPortal.Ingest.Mvc;

public class KeywordRequirement {
	String[] required;
	String[] optionList;
	int numberOfOptions;
	
	public KeywordRequirement(String[] required, String[] optionList, int numberOfOptions){
		this.required = required;
		this.optionList = optionList;
		this.numberOfOptions = numberOfOptions;
	}
	
	public String[] getRequired() {
		return required;
	}
	public void setRequired(String[] required) {
		this.required = required;
	}
	public String[] getOptionList() {
		return optionList;
	}
	public void setOptionList(String[] optionList) {
		this.optionList = optionList;
	}
	public int getNumberOfOptions() {
		return numberOfOptions;
	}
	public void setNumberOfOptions(int numberOfOptions) {
		this.numberOfOptions = numberOfOptions;
	}
}
