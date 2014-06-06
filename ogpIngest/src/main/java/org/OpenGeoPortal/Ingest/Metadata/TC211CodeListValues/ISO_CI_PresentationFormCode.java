package org.OpenGeoPortal.Ingest.Metadata.TC211CodeListValues;

public enum ISO_CI_PresentationFormCode {
	/*
	 * 
documentDigital 	digital representation of a primarily textual item (can contain illustrations also) 	CI_PresentationFormCode_documentDigital
documentHardcopy
representation of a primarily textual item (can contain illustrations also) on paper, photograhic material, or other media 	CI_PresentationFormCode_documentHardcopy
imageDigital
likeness of natural or man-made features, objects, and activities acquired through the sensing of visual or any other segment of the electromagnetic spectrum by sensors, such as thermal infrared, and high resolution radar and stored in digital format 	CI_PresentationFormCode_imageDigital
imageHardcopy 	likeness of natural or man-made features, objects, and activities acquired through the sensing of visual or any other segment of the electromagnetic spectrum by sensors, such as thermal infrared, and high resolution radar and reproduced on paper, photographic material, or other media for use directly by the human user 	CI_PresentationFormCode_imageHardcopy
mapDigital 	map represented in raster or vector form 	CI_PresentationFormCode_mapDigital
mapHardcopy 	map printed on paper, photographic material, or other media for use directly by the human user 	CI_PresentationFormCode_mapHardcopy
modelDigital 	multi-dimensional digital representation of a feature, process, etc. 	CI_PresentationFormCode_modelDigital
modelHardcopy 	3-dimensional, physical model 	CI_PresentationFormCode_modelHardcopy
profileDigital 	vertical cross-section in digital form 	CI_PresentationFormCode_profileDigital
profileHardcopy 	vertical cross-section printed on paper, etc. 	CI_PresentationFormCode_profileHardcopy
tableDigital 	digital representation of facts or figures systematically displayed, especially in columns 	CI_PresentationFormCode_tableDigital
tableHardcopy 	representation of facts or figures systematically displayed, especially in columns, printed onpapers, photographic material, or other media 	CI_PresentationFormCode_tableHardcopy
videoDigital 	digital video recording 	CI_PresentationFormCode_videoDigital
videoHardcopy 	video recording on film 	CI_PresentationFormCode_videoHardcopy  
	 * 
	 */
	documentDigital("digital representation of a primarily textual item (can contain illustrations also)"),
	documentHardcopy("representation of a primarily textual item (can contain illustrations also) on paper, photograhic material, or other media"),	
	imageDigital("likeness of natural or man-made features, objects, and activities acquired through the sensing of visual or any other segment of the electromagnetic spectrum by sensors, such as thermal infrared, and high resolution radar and stored in digital format"), 
	imageHardcopy("likeness of natural or man-made features, objects, and activities acquired through the sensing of visual or any other segment of the electromagnetic spectrum by sensors, such as thermal infrared, and high resolution radar and reproduced on paper, photographic material, or other media for use directly by the human user"),
	mapDigital("map represented in raster or vector form"),
	mapHardcopy("map printed on paper, photographic material, or other media for use directly by the human user"),
	modelDigital("multi-dimensional digital representation of a feature, process, etc."),
	modelHardcopy("3-dimensional, physical model"),
	profileDigital("vertical cross-section in digital form"),
	profileHardcopy("vertical cross-section printed on paper, etc."),
	tableDigital("digital representation of facts or figures systematically displayed, especially in columns"),
	tableHardcopy("representation of facts or figures systematically displayed, especially in columns, printed onpapers, photographic material, or other media"),
	videoDigital("digital video recording"),
	videoHardcopy("video recording on film"),
	undefined("no matching codeListValue found"),
	unspecified("no codeListValue provided");

	
	private final String description;

	ISO_CI_PresentationFormCode(String description){
		this.description = description;
	}
	
	public String getDescription(){
		return description;
	}
	
	public static ISO_CI_PresentationFormCode parseISOPresentationFormCode(String codeListValue){
		if (codeListValue.trim().length() == 0){
			return unspecified;
		}
		for (ISO_CI_PresentationFormCode code: ISO_CI_PresentationFormCode.values()){
			if (code.toString().equalsIgnoreCase(codeListValue.trim())){
				return code;
			} 
		}
		return ISO_CI_PresentationFormCode.undefined;
	}
}
