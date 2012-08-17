package org.OpenGeoPortal.Ingest.Mvc;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.OpenGeoPortal.Ingest.LibraryRecordIngestSubmitter;
import org.OpenGeoPortal.Utilities.AjaxUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;

/**
 * This controller is used to ingest records from Oracle tables (papermaps, barton) into an ogp solr instance
 * 
 * More specifically, this controller interfaces with getMapRecords.php, which returns a json representation of
 * records in the Oracle database.  The json response is parsed into a Metadata object (org.OpenGeoPortal.Layer.Metadata),
 * from which the solr client can directly ingest it into the solr instance referenced by the solr client.
 * 
 * @author 	chrissbarnett
 * @version	1.0                                  
 * @since   2012-07-31
 */

@Controller
@RequestMapping("/ingestBarton")
public class BartonIngest {
	@Autowired
	private LibraryRecordIngestSubmitter libraryRecordIngestSubmitter;
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@ModelAttribute
	public void ajaxAttribute(WebRequest request, Model model) {
		model.addAttribute("ajaxRequest", AjaxUtils.isAjax(request));
	}

	@RequestMapping(method=RequestMethod.GET)
	public void fileUploadForm() {
	}

	/**
	 * The main method.
	 * 
	 * @param model
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	@RequestMapping(method=RequestMethod.POST, produces="application/json")
	public @ResponseBody JobIdResponse processUpload(Model model) throws IOException {
		JobIdResponse jobIdResponse = new JobIdResponse();
		String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
		UUID jobId = libraryRecordIngestSubmitter.runIngestJob(sessionId);
		
		jobIdResponse.setJobId(jobId.toString());
		logger.info("JobId: " + jobId.toString());
		return jobIdResponse;
	}
}