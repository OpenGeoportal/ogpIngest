package org.OpenGeoPortal.Ingest.Mvc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.OpenGeoPortal.Ingest.MetadataPreprocessorSubmitter;
import org.OpenGeoPortal.Layer.AccessLevel;
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

/**
 * This controller is used to batch process FGDC XML metadata records and to screen them for issues that might occur in the ingest process
 * 
 * More specifically, this controller accepts multiple files or a zipped directory of files (FGDC XML metadata), 
 * adds supplied use constaints and access constraints (.txt files), adds standardized text to access constraints
 * based on the value supplied to the controller, adds contact info, deletes onlink tags that are
 * not links and adds the standard connection info for sde, and standardizes the format of the ftname tag (should be the layer
 * name as it appears in sde).  Additionally, the controller uses the same parsers as the metadata upload controller
 * (org.OpenGeoPortal.Ingest.Mvc.MetadataUploadController) so that it can report errors that might occur in that process.
 * 
 * @author 	chrissbarnett
 * @version	1.0                                  
 * @since   2012-07-31
 */

@Controller
@RequestMapping("/adjustMetadata")
public class MetadataPreprocessController {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private MetadataPreprocessorSubmitter metadataPreprocessorSubmitter;

	@ModelAttribute
	public void ajaxAttribute(WebRequest request, Model model) {
		model.addAttribute("ajaxRequest", AjaxUtils.isAjax(request));
	}

	@RequestMapping(method=RequestMethod.GET)
	public void processMetadataForm() {
	}

	/**
	 * The main method
	 * 
	 * @param request
	 * @param fgdcFile	xml file or zip archive of a directory of xml files
	 * @param accessConstraintsFile	.txt file
	 * @param useConstraintsFile 	.txt file
	 * @param access 	"restricted" or "public"
	 * @param model
	 * @throws Exception 
	 */
	@RequestMapping(method=RequestMethod.POST, produces="application/json")
	public @ResponseBody JobIdResponse processMetadata(HttpServletRequest request, @RequestParam("fgdcFile[]") MultipartFile[] fgdcFile, 
			@RequestParam("accessConstraints") String accessConstraints, @RequestParam("useConstraints") String useConstraints,
			@RequestParam("access") String access, Model model) throws Exception {	

		//set up the directory to place the returned zip file in
		ServletContext context = request.getSession().getServletContext();
		logger.debug("contextpath: " + context.getContextPath());
		String contextDir = context.getRealPath(context.getContextPath());
		logger.debug(contextDir);
		File parentDir = new File(contextDir).getParentFile();
		File currentDir = new File(parentDir, "resources/metadata");
		if (!currentDir.exists()){
			currentDir.mkdir();
		}
		
		AccessLevel accessLevel;
		if (access.equalsIgnoreCase("public")){
			accessLevel = AccessLevel.Public;
		} else if (access.equalsIgnoreCase("restricted")){
			accessLevel = AccessLevel.Restricted;
		} else {
			throw new Exception("invalid value for access param");
		}
				
		File tempDir = FileUtils.createTempDir();
		List<File> uploadedFiles = new ArrayList<File>();
		JobIdResponse jobIdResponse = new JobIdResponse();
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
			file.transferTo(tempFile);
			uploadedFiles.add(tempFile);
			if (fileSize == null){
				fileSize = (long) 0;
			}
			jobIdResponse.addFileInfo(fileName, contentType, fileSize);

		}
		String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();

		UUID jobId = metadataPreprocessorSubmitter.runIngestJob(sessionId, uploadedFiles, accessConstraints, useConstraints, accessLevel, currentDir);
		
		jobIdResponse.setJobId(jobId.toString());
		logger.info("JobId: " + jobId.toString());
		return jobIdResponse;
	}
	

}