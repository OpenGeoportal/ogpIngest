package org.OpenGeoPortal.Ingest.Metadata.TC211CodeListValues;

public enum ISO_MI_GeometryTypeCode {
	/*
	  MI_GeometryTypeCode

	http://www.ngdc.noaa.gov/metadata/published/xsd/schema/resources/Codelist/gmxCodelists.xml#MI_GeometryTypeCode
	CodeList Definition: geometric description of the collection
	Entry	Description	Definition
	point	single geographic point of interest	MI_GeometryTypeCode_point
	linear	extended collection in a single vector	MI_GeometryTypeCode_linear
	areal	collection of a geographic area defined by a polygon (coverage)	MI_GeometryTypeCode_areal
	strip	series of linear collections grouped by way points	MI_GeometryTypeCode_stri 
	 */
	point("single geographic point of interest"),
	linear("extended collection in a single vector"),	
	areal("collection of a geographic area defined by a polygon (coverage)"),
	strip("series of linear collections grouped by way points"),
	undefined("undefined geometric object type"),
	unspecified("unspecified geometric object type");

	
	private final String description;

	ISO_MI_GeometryTypeCode(String description){
		this.description = description;
	}
	
	public String getDescription(){
		return description;
	}
	
	public static ISO_MI_GeometryTypeCode parseISO_MI_GeometryTypeCode(String codeListValue){
		if (codeListValue.trim().length() == 0){
			return unspecified;
		}
		for (ISO_MI_GeometryTypeCode code: ISO_MI_GeometryTypeCode.values()){
			if (code.toString().equalsIgnoreCase(codeListValue.trim())){
				return code;
			} 
		}
		return undefined;
	}
}
