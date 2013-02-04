package org.OpenGeoPortal.Ingest.Mvc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.OpenGeoPortal.Ingest.AbstractSolrIngest.MetadataElement;
import org.OpenGeoPortal.Ingest.MetadataUploadSubmitter;
import org.OpenGeoPortal.Utilities.AjaxUtils;
import org.OpenGeoPortal.Utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
public class MetadataUploadController {
	@Autowired
	private MetadataUploadSubmitter metadataUploadSubmitter;
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Map<String, MetadataElement> elementMap;
    static
    {
        elementMap = new HashMap<String, MetadataElement>();
        elementMap.put("title", MetadataElement.Title);
        elementMap.put("abstract", MetadataElement.Abstract);
        elementMap.put("bounds", MetadataElement.Bounds);
        elementMap.put("publisher", MetadataElement.Publisher);
        elementMap.put("originator", MetadataElement.Originator);
        elementMap.put("datatype", MetadataElement.DataType);
        elementMap.put("access", MetadataElement.Access);
        elementMap.put("themekey", MetadataElement.ThemeKeywords);
        elementMap.put("placekey", MetadataElement.PlaceKeywords);
    }

	@ModelAttribute
	public void ajaxAttribute(WebRequest request, Model model) {
		model.addAttribute("ajaxRequest", AjaxUtils.isAjax(request));
	}

	@RequestMapping(method=RequestMethod.GET)
	public void fileUploadForm() {
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
		logger.info("JobId: " + jobId.toString());
		return jobIdResponse;
	}
	
	private Set<MetadataElement> getRequiredElements(String[] requiredFields){
		Set<MetadataElement> requiredElements = new HashSet<MetadataElement>();
		requiredElements.add(MetadataElement.Institution);
		requiredElements.add(MetadataElement.LayerId);
		requiredElements.add(MetadataElement.LayerName);
		for (String field: requiredFields){
			requiredElements.add(elementMap.get(field.toLowerCase()));
		}
		return requiredElements;
	}
}