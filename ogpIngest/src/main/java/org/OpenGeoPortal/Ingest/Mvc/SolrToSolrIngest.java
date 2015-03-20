package org.OpenGeoPortal.Ingest.Mvc;

import java.io.IOException;
import java.util.UUID;

import org.OpenGeoPortal.Ingest.RemoteSolrByIdIngestSubmitter;
import org.OpenGeoPortal.Ingest.RemoteSolrIngestSubmitter;
import org.OpenGeoPortal.Utilities.AjaxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;


/**
 * This controller is used to ingest records from an external ogp solr instance into the local ogp solr instance
 * 
 * More specifically, this controller queries an external ogp solr instance (currently by institution name), unmarshalls
 * the response into a SolrExchange object (org.OpenGeoPortal.Solr.SolrExchangeRecord), then writes the objects to the 
 * local ogp solr instance
 * 
 * @author 	chrissbarnett
 * @version	1.0                                  
 * @since   2012-07-31
 */

@Controller
@RequestMapping("/solrToSolr")
public class SolrToSolrIngest {
	@Autowired
	private RemoteSolrByIdIngestSubmitter remoteSolrByIdIngestSubmitter;
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@ModelAttribute
	public void ajaxAttribute(WebRequest request, Model model) {
		model.addAttribute("ajaxRequest", AjaxUtils.isAjax(request));
	}

	@RequestMapping(method=RequestMethod.GET)
	public void fileUploadForm() {
	}

	/**
	 * the main method
	 * 
	 * @param solrUrl
	 * @param institution
	 * @param model
	 * @throws IOException
	 */
	@RequestMapping(method=RequestMethod.POST, produces="application/json")
	public @ResponseBody JobIdResponse processUpload(@RequestParam("solrUrl") String solrUrl, @RequestParam("layerIds") String layerIds, 
			Model model) throws IOException {
		JobIdResponse jobIdResponse = new JobIdResponse();
		String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
		String[] arrLayerIds = layerIds.split(System.getProperty("line.separator"));
		for (String layerId: arrLayerIds){
			layerId = StringUtils.trimWhitespace(layerId);//clean up extra whitespace
			logger.info(layerId);
		}
		UUID jobId = remoteSolrByIdIngestSubmitter.runIngestJob(sessionId, arrLayerIds, solrUrl);
		
		jobIdResponse.setJobId(jobId.toString());
		logger.info("JobId: " + jobId.toString());
		return jobIdResponse;
	}
}