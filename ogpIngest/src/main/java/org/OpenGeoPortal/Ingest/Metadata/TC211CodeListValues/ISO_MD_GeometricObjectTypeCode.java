package org.OpenGeoPortal.Ingest.Metadata.TC211CodeListValues;

public enum ISO_MD_GeometricObjectTypeCode {
	/*
	 MD_GeometricObjectTypeCode

	 http://www.ngdc.noaa.gov/metadata/published/xsd/schema/resources/Codelist/gmxCodelists.xml#MD_GeometricObjectTypeCode
	 CodeList Definition: name of point or vector objects used to locate zero-, one-, two-, or three-dimensional spatial locations in the dataset
	 Entry	Description	Definition
	 complex	set of geometric primitives such that their boundaries can be represented as a union of other primitives	MD_GeometricObjectTypeCode_complex
	 composite	connected set of curves, solids or surfaces	MD_GeometricObjectTypeCode_composite
	 curve	bounded, 1-dimensional geometric primitive, representing the continuous image of a line	MD_GeometricObjectTypeCode_curve
	 point	zero-dimensional geometric primitive, representing a position but not having an extent	MD_GeometricObjectTypeCode_point
	 solid	bounded, connected 3-dimensional geometric primitive, representing the continuous image of a region of space	MD_GeometricObjectTypeCode_solid
	 surface	bounded, connected 2-dimensional geometric primitive, representing the continuous image of a region of a plane	MD_GeometricObjectTypeCode_surface
	 */

	 complex("set of geometric primitives such that their boundaries can be represented as a union of other primitives"),	
	 composite("connected set of curves, solids or surfaces"),
	 curve("bounded, 1-dimensional geometric primitive, representing the continuous image of a line"),	
	 point("zero-dimensional geometric primitive, representing a position but not having an extent"),	
	 solid("bounded, connected 3-dimensional geometric primitive, representing the continuous image of a region of space"),	
	 surface("bounded, connected 2-dimensional geometric primitive, representing the continuous image of a region of a plane"),
	 undefined("undefined geometric object type"),
	 unspecified("unspecified geometric object type");

	
	private final String description;

	ISO_MD_GeometricObjectTypeCode(String description){
		this.description = description;
	}
	
	public String getDescription(){
		return description;
	}
	
	public static ISO_MD_GeometricObjectTypeCode parseISO_MD_GeometricObjectTypeCode(String codeListValue){
		if (codeListValue.trim().length() == 0){
			return unspecified;
		}
		for (ISO_MD_GeometricObjectTypeCode code: ISO_MD_GeometricObjectTypeCode.values()){
			if (code.toString().equalsIgnoreCase(codeListValue.trim())){
				return code;
			} 
		}
		return undefined;
	}
}
