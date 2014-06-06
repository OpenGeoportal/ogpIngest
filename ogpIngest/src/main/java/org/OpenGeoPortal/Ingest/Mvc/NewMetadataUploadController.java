package org.OpenGeoPortal.Ingest.Mvc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.OpenGeoPortal.Ingest.IngestProperties;
import org.OpenGeoPortal.Ingest.MetadataUploadSubmitter;
import org.OpenGeoPortal.Ingest.Metadata.MetadataElement;
import org.OpenGeoPortal.Utilities.AjaxUtils;
import org.OpenGeoPortal.Utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/metadataUpload")
public class NewMetadataUploadController {
	@Autowired
	private MetadataUploadSubmitter metadataUploadSubmitter;
	
	@Autowired
	FormReference formReference;
	
	@Autowired
	IngestProperties ingestProperties;

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@ModelAttribute
	public void ajaxAttribute(WebRequest request, Model model) {
		model.addAttribute("ajaxRequest", AjaxUtils.isAjax(request));
	}

	@ModelAttribute("metadataElementList")
	public Map<String,String> populateMetadataElementList() {
		return formReference.getMetadataElementMap();
	}
	
	@ModelAttribute("institutionList")
	public Map<String,String> populateInstitutionList() throws IOException {
		return formReference.getInstitutionMap();
	}
	
	@ModelAttribute("placeKeywordConstraintList")
	public Map<String,String> getPlaceKeywordThesaurusList() {
		return formReference.getAvailablePlaceKeywordThesaurusMap(); 
	}
	
	@ModelAttribute("themeKeywordConstraintList")
	public Map<String,String> getThemeKeywordThesaurusList() {
		return formReference.getAvailableThemeKeywordThesaurusMap();
	}
	

	
	@RequestMapping(method=RequestMethod.GET)
	public String initForm(ModelMap model) throws IOException{
		MetadataUploadForm defaultMetadataUploadForm =  new MetadataUploadForm();
		//get this stuff from properties file
		defaultMetadataUploadForm.setInstitution(ingestProperties.getProperty("local.institution"));
		defaultMetadataUploadForm.setRequiredFields(ingestProperties.getPropertyArray("requiredFields"));
		
		String[] requiredPlaceKeywords = ingestProperties.getPropertyArray("requiredPlaceKeywords");
		String[] placeKeywordOptions = ingestProperties.getPropertyArray("placeKeywordOptions");
		int numberOfPlaceOptions = Integer.parseInt(ingestProperties.getProperty("placeOptionsRequired"));
		defaultMetadataUploadForm.setPlaceKeywordRequirement(new KeywordRequirement(requiredPlaceKeywords, placeKeywordOptions, numberOfPlaceOptions));
		
		String[] requiredThemeKeywords = ingestProperties.getPropertyArray("requiredThemeKeywords");
		String[] themeKeywordOptions = ingestProperties.getPropertyArray("themeKeywordOptions");
		int numberOfThemeOptions = Integer.parseInt(ingestProperties.getProperty("themeOptionsRequired"));
		defaultMetadataUploadForm.setThemeKeywordRequirement(new KeywordRequirement(requiredThemeKeywords, themeKeywordOptions, numberOfThemeOptions));
		//command object
		model.addAttribute("defaultForm", defaultMetadataUploadForm);
 
		//return form view
		return "newMetadataUpload";
	}

	@RequestMapping(method=RequestMethod.POST, produces="application/json")
	public @ResponseBody JobIdResponse processUpload(@RequestParam("institution") String institution, @RequestParam("ingestOption") String options, 
				@RequestParam("requiredFields") String[] requiredFields, @RequestParam("fgdcFile[]") MultipartFile[] fgdcFile, Model model) throws IOException {
		List<File> uploadedFiles = new ArrayList<File>();
		JobIdResponse jobIdResponse = new JobIdResponse();
		File tempDir = FileUtils.createTempDir();
		for (MultipartFile file : fgdcFile){
			String fileSuffix;
			String fileName = file.getOriginalFilename();
			String contentType = file.getContentType().toLowerCase();
			Long fileSize;
			try {
				fileSize = file.getSize();
			} catch (Exception e){
				logger.error("problem getting file size:" + e.getMessage());
				fileSize = (long) 0;
			}
			if (contentType.contains("xml")){
				fileSuffix = "xml";
			} else if (contentType.contains("zip")){
				fileSuffix = "zip";
			} else {
				//skip the file...we don't want it
				continue;
			}

			File tempFile = new File(tempDir, fileName.substring(0, fileName.indexOf(".")) + "." + fileSuffix); 
			//File tempFile = File.createTempFile(fileName.substring(0, fileName.indexOf(".")),"." + fileSuffix);
			file.transferTo(tempFile);
			uploadedFiles.add(tempFile);
			if (fileSize == null){
				fileSize = (long) 0;
			}
			jobIdResponse.addFileInfo(fileName, contentType, fileSize);

		}
		String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();

		UUID jobId = metadataUploadSubmitter.runIngestJob(sessionId, institution, getRequiredElements(requiredFields), options, uploadedFiles);
		
		jobIdResponse.setJobId(jobId.toString());
		logger.debug("JobId: " + jobId.toString());
		return jobIdResponse;
	}
	
	private Set<MetadataElement> getRequiredElements(String[] requiredFields) throws IOException{
		Set<MetadataElement> requiredElements = new HashSet<MetadataElement>();
		String[] requiredElementArr = ingestProperties.getPropertyArray("requiredFields");
		for (String element: requiredElementArr){
			requiredElements.add(MetadataElement.parseMetadataElement(element));
		}

		for (String field: requiredFields){
			requiredElements.add(MetadataElement.parseMetadataElement(field));
		}
		return requiredElements;
	}
}