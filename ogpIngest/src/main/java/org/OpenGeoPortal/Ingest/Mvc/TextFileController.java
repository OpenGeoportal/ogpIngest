package org.OpenGeoPortal.Ingest.Mvc;

import java.io.IOException;
import java.io.InputStream;

import org.OpenGeoPortal.Utilities.AjaxUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/getText")
public class TextFileController {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@ModelAttribute
	public void ajaxAttribute(WebRequest request, Model model) {
		model.addAttribute("ajaxRequest", AjaxUtils.isAjax(request));
	}

	@RequestMapping(method=RequestMethod.GET)
	public void textFileForm() {
	}

	@RequestMapping(method=RequestMethod.POST, produces="application/json")
	public  @ResponseBody StringResponse textFile(@RequestParam("textFile") MultipartFile textFile, Model model) throws IOException {
		StringResponse stringResponse = new StringResponse();
		stringResponse.addFileInfo(textFile.getOriginalFilename(), textFile.getContentType(), textFile.getSize());
		//text file to string		
		String textFileString = "";
		if (textFile != null){
			InputStream inputStream = textFile.getInputStream();
			textFileString = IOUtils.toString(inputStream, "UTF-8");
		}
		stringResponse.setFileText(textFileString);
		return stringResponse;
	}
}
