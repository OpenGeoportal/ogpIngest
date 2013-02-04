package org.OpenGeoPortal.Ingest.Mvc;

import java.io.IOException;
import java.util.UUID;

import org.OpenGeoPortal.Ingest.DirectToSolrSubmitter;
import org.OpenGeoPortal.Layer.Metadata;
import org.OpenGeoPortal.Utilities.AjaxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/directToSolr")
@SessionAttributes("metadata")
public class DirectToSolrController {	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private DirectToSolrSubmitter directToSolrSubmitter;
	
	@ModelAttribute
	public void ajaxAttribute(WebRequest request, Model model) {
		model.addAttribute("ajaxRequest", AjaxUtils.isAjax(request));
	}

	@ModelAttribute("metadata")
	public Metadata createMetadataForm() {
		return new Metadata();
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public void directToSolrForm() {
	}

	@RequestMapping(method=RequestMethod.POST, produces="application/json")
	public String sendToSolr(Metadata metadata, 
			BindingResult result, Model model) throws IOException {
		JobIdResponse jobIdResponse = new JobIdResponse();
		
		String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();

		UUID jobId = directToSolrSubmitter.runIngestJob(sessionId, metadata);
		
		jobIdResponse.setJobId(jobId.toString());
		logger.info("JobId: " + jobId.toString());
		return "redirect:/directToSolr";
	}
	
}