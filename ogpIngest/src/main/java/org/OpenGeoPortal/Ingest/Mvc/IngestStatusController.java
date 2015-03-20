package org.OpenGeoPortal.Ingest.Mvc;

import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.OpenGeoPortal.Ingest.IngestStatus;
import org.OpenGeoPortal.Ingest.IngestStatusManager;
import org.OpenGeoPortal.Ingest.IngestStatusResponse;
import org.OpenGeoPortal.Utilities.AjaxUtils;
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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/ingestStatus")
public class IngestStatusController {

	@Autowired
	private IngestStatusManager ingestStatusManager;
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@ModelAttribute
	public void ajaxAttribute(WebRequest request, Model model) {
		model.addAttribute("ajaxRequest", AjaxUtils.isAjax(request));
	}
	
	@RequestMapping(method=RequestMethod.GET, produces="application/json")
	public @ResponseBody IngestStatusResponse getIngestStatus(@RequestParam("jobId") String jobId, Model model)  {
		IngestStatusResponse ingestStatus = null;
		try {
			ingestStatus = (IngestStatusResponse) ingestStatusManager.getNewIngestStatus(UUID.fromString(jobId));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		model.addAttribute("ingestStatus", ingestStatus);

		return ingestStatus;
	}
	
	@RequestMapping(value="/excel", method=RequestMethod.GET)
	ModelAndView getIngestStatusExcel(@RequestParam("jobId") String jobId, HttpServletResponse response)  {
		IngestStatus ingestStatus = ingestStatusManager.getIngestStatus(UUID.fromString(jobId));
		response.setHeader("Content-Disposition", "attachment;filename=ingestReport.xls");
		return new ModelAndView("ingestReport","ingestStatus", ingestStatus);
	}
}
