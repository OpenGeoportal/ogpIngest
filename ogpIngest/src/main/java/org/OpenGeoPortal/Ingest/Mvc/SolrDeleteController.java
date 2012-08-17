package org.OpenGeoPortal.Ingest.Mvc;

import java.io.IOException;

import org.OpenGeoPortal.Solr.SolrClient;
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
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/solrDelete")
public class SolrDeleteController {
	@Autowired
	private SolrClient solrClient;
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@ModelAttribute
	public void ajaxAttribute(WebRequest request, Model model) {
		model.addAttribute("ajaxRequest", AjaxUtils.isAjax(request));
	}

	@RequestMapping(method=RequestMethod.GET)
	public void solrDeleteForm() {
	}

	@RequestMapping(method=RequestMethod.POST)
	public void solrDelete(@RequestParam("layerIds") String layerIds, Model model) throws IOException {
		
		String[] arrLayerIds = layerIds.split(System.getProperty("line.separator"));
		for (String layerId: arrLayerIds){
			layerId = StringUtils.trimWhitespace(layerId);//clean up extra whitespace
			logger.info(layerId);
		}
		try {
			logger.debug("Deleting layers..." + StringUtils.arrayToCommaDelimitedString(arrLayerIds));
			String result = solrClient.delete(arrLayerIds);
			solrClient.commit();
			model.addAttribute("message", StringUtils.arrayToCommaDelimitedString(arrLayerIds));
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			logger.error(e.getMessage());
		}
	}
}
