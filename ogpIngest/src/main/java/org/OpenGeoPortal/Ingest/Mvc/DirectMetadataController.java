package org.OpenGeoPortal.Ingest.Mvc;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.OpenGeoPortal.Ingest.SolrIngest;
import org.OpenGeoPortal.Ingest.SolrIngestResponse;
import org.OpenGeoPortal.Ingest.IngestResponse.IngestInfo;
import org.OpenGeoPortal.Ingest.Metadata.MetadataConverter;
import org.OpenGeoPortal.Ingest.Metadata.MetadataParseResponse;
import org.OpenGeoPortal.Keyword.PlaceKeywords;
import org.OpenGeoPortal.Keyword.ThemeKeywords;
import org.OpenGeoPortal.Layer.LocationLink;
import org.OpenGeoPortal.Layer.LocationLink.LocationType;
import org.OpenGeoPortal.Layer.Metadata;
import org.OpenGeoPortal.Utilities.AjaxUtils;
import org.OpenGeoPortal.Utilities.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class DirectMetadataController {
	@Autowired
	private MetadataConverter metadataConverter;
	@Autowired 
	@Qualifier("solrIngest.ows")
	private SolrIngest solrIngest;
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@ModelAttribute
	public void ajaxAttribute(WebRequest request, Model model) {
		model.addAttribute("ajaxRequest", AjaxUtils.isAjax(request));
	}
	
	@RequestMapping(value="formToSolr", method=RequestMethod.GET)
	public Model getForm(Model model) {
		return model;
	}
	
	@RequestMapping(value="formToSolr", method=RequestMethod.POST, produces="application/json")
	public @ResponseBody Model formToSolr(@RequestParam(value="metadataFile", required=false) MultipartFile metadataFile,
			@RequestParam(value="toSolr", defaultValue="false") Boolean toSolr,
			@RequestParam(value="access", defaultValue="") String access, @RequestParam(value="bounds", defaultValue="") String[] bounds, @RequestParam(value="date", defaultValue="") String contentDate,
			@RequestParam(value="abstract", defaultValue="") String description, @RequestParam(value="geometryType", defaultValue="") String geometryType, 
			@RequestParam(value="georeferenced", defaultValue="true") Boolean georeferenced, @RequestParam(value="layerId", defaultValue="") String id, 
			@RequestParam(value="institution", defaultValue="") String institution, @RequestParam(value="originator", defaultValue="") String originator, 
			@RequestParam(value="publisher", defaultValue="") String publisher, @RequestParam(value="owsName", defaultValue="") String owsName, 
			@RequestParam(value="workspaceName", defaultValue="") String workspaceName, @RequestParam(value="title", defaultValue="") String title, 
			@RequestParam(value="themeKeywords", defaultValue="") List<String> themeKeywords, @RequestParam(value="placeKeywords", defaultValue="") List<String> placeKeywords, 
			@RequestParam(value="location", defaultValue="") String location, Model model) throws Exception {

		Metadata metadata = null;

		if (metadataFile != null){
			File file = validateAndTransferFile(metadataFile);

			if (!institution.isEmpty()){
				metadata = parse(file, institution).metadata;
			} else {
				metadata = bestEffortParse(file).metadata;
			}
		} else {
			metadata = new Metadata();
		}
		
		if (!access.isEmpty()){
			metadata.setAccessLevel(access);
		}
		
		if (bounds.length > 0){
			metadata.setBounds(bounds[0], bounds[1], bounds[2], bounds[3]);
		}
		
		if (!contentDate.isEmpty()){
			metadata.setContentDate(contentDate);
		}
		
		if (!description.trim().isEmpty()){
			metadata.setDescription(description.trim());
		}
		
		if (!geometryType.isEmpty()){
			metadata.setGeometryType(geometryType.trim());
		}
		
		if (!georeferenced){
			metadata.setGeoreferenced(georeferenced);
		}
		
		if (!id.isEmpty()){
			metadata.setId(id);
		}
		
		if (!location.isEmpty()){
			Set<LocationLink> links = new HashSet<LocationLink>();
			//for (String url: location){
				String[] linkInfo = location.split(",");
				LocationType type = LocationType.fromString(linkInfo[0]);
				LocationLink link = new LocationLink(type, new URL(linkInfo[1]));
				links.add(link);
			//}
			metadata.setLocation(links);
		}
		
		if (!originator.isEmpty()){
			metadata.setOriginator(originator);
		}
		
		if (!owsName.isEmpty()){
			metadata.setOwsName(owsName);
		}
		
		if (!placeKeywords.isEmpty()){
			List<PlaceKeywords> plKeywordList = new ArrayList<PlaceKeywords>();
			PlaceKeywords plKeyword = new PlaceKeywords();
			plKeywordList.add(plKeyword);
			for (String keyword: placeKeywords){
				plKeyword.addKeyword(keyword);
			}
			metadata.setPlaceKeywords(plKeywordList);
		}
		
		if (!publisher.isEmpty()){
			metadata.setPublisher(publisher);
		}
		
		if (!themeKeywords.isEmpty()){
			List<ThemeKeywords> thKeywordList = new ArrayList<ThemeKeywords>();
			ThemeKeywords thKeyword = new ThemeKeywords();
			thKeywordList.add(thKeyword);
			for (String keyword: themeKeywords){
				thKeyword.addKeyword(keyword);
			}
			metadata.setThemeKeywords(thKeywordList);
		}
		
		if (!title.isEmpty()){
			metadata.setTitle(title);
		}
		
		if (!workspaceName.isEmpty()){
			metadata.setWorkspaceName(workspaceName);
		}
		
		if (toSolr){
			uploadToSolr(metadata);
		}
		model.addAttribute("parsedValues", metadata);

		return model;
	}

	/*@RequestMapping(value="parseMetadata", method=RequestMethod.POST, produces="application/json")
	public @ResponseBody Model processUpload(@RequestParam("metadataFile") MultipartFile metadataFile, Model model) throws Exception {

		File file = validateAndTransferFile(metadataFile);
		Metadata metadata = bestEffortParse(file).metadata;
		
		return model;
	}*/
	
	private MetadataParseResponse bestEffortParse(File file) throws Exception{
		MetadataParseResponse parsed = metadataConverter.bestEffortParse(file);
		return parsed;
	}
	
	private MetadataParseResponse parse(File file, String institution) throws Exception{
		MetadataParseResponse parsed = metadataConverter.parse(file, institution);
		return parsed;
	}
	
	private String uploadToSolr(Metadata metadata){
			logger.info("Trying Solr ingest...[" + metadata.getOwsName() + "]");	
			// and ingest into solr
			SolrIngestResponse solrIngestResponse = null;
			
			try {				
				solrIngestResponse = solrIngest.writeToSolr(metadata);
			} catch (Exception e){ 
				logger.error(e.getMessage());
				//solrIngestResponse.addError("generalError", metadata.getOwsName(), "generalError", "Solr Error: " + e.getMessage());
			}
			if (!solrIngestResponse.ingestErrors.isEmpty()){
				/*for (IngestInfo errorObj: solrIngestResponse.ingestErrors){
					solrIngestResponse.addError("generalError", metadata.getOwsName(), "generalError", "Solr Ingest Error: " + errorObj.getField() + "&lt;" + errorObj.getNativeName() + "&gt;:" + errorObj.getError() + "-" + errorObj.getMessage());
				}*/
				logger.error("Solr Ingest Errors:" + solrIngestResponse.ingestErrors.size());
			}
			if (!solrIngestResponse.ingestWarnings.isEmpty()){
				/*for (IngestInfo errorObj: solrIngestResponse.ingestWarnings){
					solrIngestResponse.addWarning("generalError", metadata.getOwsName(), "generalError", "Solr Ingest Warnings: " + errorObj.getField() + "&lt;" + errorObj.getNativeName() + "&gt;:" + errorObj.getError() + "-" + errorObj.getMessage());
				}*/
				logger.warn("Solr Ingest Warnings:" + solrIngestResponse.ingestWarnings.size());
			}
			
			return solrIngestResponse.solrRecord.getLayerId();
	}
	
	private File validateAndTransferFile(MultipartFile metadataFile) throws Exception{
		File tempDir = FileUtils.createTempDir();
		String fileSuffix;
		String fileName = metadataFile.getOriginalFilename();
		String contentType = metadataFile.getContentType().toLowerCase();
		Long fileSize;
		try {
			fileSize = metadataFile.getSize();
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
			throw new Exception("Invalid file type!");
		}

		File tempFile = new File(tempDir, fileName.substring(0, fileName.indexOf(".")) + "." + fileSuffix); 
		metadataFile.transferTo(tempFile);
		if (fileSize == null){
			fileSize = (long) 0;
		}

		return tempFile;


	}
	

}