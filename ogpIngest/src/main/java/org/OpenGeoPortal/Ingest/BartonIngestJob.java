package org.OpenGeoPortal.Ingest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.OpenGeoPortal.Ingest.AbstractSolrIngest.MetadataElement;
import org.OpenGeoPortal.Ingest.IngestResponse.IngestInfo;
import org.OpenGeoPortal.Layer.AccessLevel;
import org.OpenGeoPortal.Layer.GeometryType;
import org.OpenGeoPortal.Layer.LocationLink;
import org.OpenGeoPortal.Layer.LocationLink.LocationType;
import org.OpenGeoPortal.Layer.Metadata;
import org.OpenGeoPortal.Solr.SolrClient;
import org.OpenGeoPortal.Utilities.AllTrustingTrustManager;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BartonIngestJob implements LibraryRecordIngestJob, Runnable {
	private SolrClient solrClient;	
	private IngestStatusManager ingestStatusManager;
	private String institution = "MIT";
	private UUID jobId;
	private IngestStatus ingestStatus;
	private SolrIngest solrIngest;

	private final String remoteUrl = "https://delisle.mit.edu/utilities/getMapRecords.php";
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private void getBartonRecords() throws NoSuchAlgorithmException, IOException, KeyManagementException{
		ingestStatus = ingestStatusManager.getIngestStatus(jobId);
		
		try {
			//a query to delete the current library records before reingest
			solrClient.getSolrServer().deleteByQuery("Availability:offline");
		} catch (SolrServerException e1) {
			ingestStatus.addError("SolrDelete", "Unable to delete current library records.");
		}
		solrClient.commit();

		//since the php script we are accessing is https, but doesn't require certs, we need to create a context that essentially 
		//ignores certs
		TrustManager[] trustAllCerts = new TrustManager[] { new AllTrustingTrustManager() };
		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		SchemeSocketFactory sf = (SchemeSocketFactory) new SSLSocketFactory(sc, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme sch = new Scheme("https", 443, sf);
		DefaultHttpClient httpclient = new DefaultHttpClient();

		httpclient.getConnectionManager().getSchemeRegistry().register(sch);
				
		//send the actual request to the script
		try {
			logger.info("connecting to " + remoteUrl);
			HttpGet internalRequest = new HttpGet(remoteUrl);
			HttpResponse internalResponse = httpclient.execute(internalRequest);

			int status = internalResponse.getStatusLine().getStatusCode();
			if (status != 200){
				//throw an error
				ingestStatus.addError(remoteUrl, "Unable to access");
				throw new IOException(status + "Unable to access '" + remoteUrl + "'");
			}

			InputStream inputStream = internalResponse.getEntity().getContent();
			
			try{
				//parse the response and write it to solr
				String layerId = ingestBartonMetadata(inputStream);
				ingestStatus.addSuccess(remoteUrl, "Records Added.");				
			} catch (Exception e){
				//e.printStackTrace();
				String cause = "";
				if (e.getCause() == null){
					if (e.getMessage() == null){
						cause = "Unspecified error";
					} else {
						cause = e.getMessage();
					}
				} else {
					cause = e.getCause().getClass().getName() + ":" + e.getMessage();
				}
				ingestStatus.addError(remoteUrl, cause);
			}
		
			
		} catch (Exception e){
			System.out.println(e.getMessage());
			//e.getStackTrace();
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}

	}
	
	public IngestStatusManager getIngestStatusManager() {
		return ingestStatusManager;
	}

	public void setIngestStatusManager(IngestStatusManager ingestStatusManager) {
		this.ingestStatusManager = ingestStatusManager;
	}

	public SolrClient getSolrClient() {
		return solrClient;
	}

	public void setSolrClient(SolrClient solrClient) {
		this.solrClient = solrClient;
	}
	
	public void run() {
		try{
			getBartonRecords();
		} catch (Exception e){
			logger.error("Error in BartonUpload");
			ingestStatus.setJobStatus(IngestJobStatus.Failed);
		}
	}

	public void init(UUID jobId) {
		this.jobId = jobId;
	}
	
	/**
	 * Uses the Jackson Json library to parse the json response as a stream
	 * 
	 * @param inputStream
	 * @param warningMessage
	 * @param errorMessage
	 * @return
	 * @throws Exception
	 */
	private String ingestBartonMetadata(InputStream inputStream) throws Exception{
		logger.info("Trying to parse metadata...");
		int counter = 0;
		JsonFactory f = new JsonFactory();
		JsonParser jp = f.createJsonParser(inputStream);
   		//populate this map with access info
		while (jp.getCurrentToken() != JsonToken.FIELD_NAME){
			jp.nextToken();
		}
		outerLoop:
   		while ((jp.nextToken() != JsonToken.END_ARRAY)&&(jp.nextToken() != JsonToken.NOT_AVAILABLE)) {
   			try{
   				while (jp.getCurrentToken() != JsonToken.FIELD_NAME){
   					if (jp.getCurrentToken() == JsonToken.END_ARRAY){
   						break outerLoop;
   					}
   					jp.nextToken();
   				}
   				Metadata metadata = getNextRecord(jp);
   				ingestRecord(metadata);
   				counter++;
				logger.debug(counter + ":" + metadata.getId() + ":" + metadata.getTitle().replace("''''", "'") );
				if (jp.getCurrentToken() == JsonToken.END_ARRAY){
					System.out.println(jp.getCurrentToken().asString());
					break outerLoop;
				}

   			} catch (Exception e){
				System.out.println("exception" + jp.getCurrentToken().asString());
   				System.out.println(e.getMessage());
   			}
   		}
		System.out.println("finished...");
   		jp.close(); // ensure resources get cleaned up timely and properly
		return "";
		
	}
	
	/**
	 * method to fix an issue with improper quoting of single quotes in the Oracle tables
	 * 
	 * @param s
	 * @return
	 */
	private String processString(String s){
		s = s.replace("''''", "'");
		return s;
	}
	
	/**
	 * Parses the actual json record from the json stream and unmarshalls it into a Metadata object
	 * @param jp
	 * @return
	 * @throws JsonParseException
	 * @throws IOException
	 */
	public Metadata getNextRecord(JsonParser jp) throws JsonParseException, IOException {
		Metadata metadata = new Metadata();

        String minX = null;
        String minY = null;
        String maxX = null;
        String maxY = null;
        String source = null;
        String localId = null;
        List<String> extraFields = new ArrayList<String>();
        
        while (jp.getCurrentToken() != JsonToken.FIELD_NAME){
        	jp.nextToken();
        }
        
        while (jp.nextValue() != JsonToken.END_OBJECT){
        	String value = jp.getText();
        	value = processString(value);
        	String field = jp.getCurrentName();
        	
        	if (field.equalsIgnoreCase("localid")) {
        		localId = value;
        	} else if (field.equalsIgnoreCase("title")){
        			metadata.setTitle(value);
        	} else if (field.equalsIgnoreCase("catalog_name")){
        			metadata.setOwsName(value);
        	} else if (field.equalsIgnoreCase("abstract")){
        			metadata.setDescription(value);
        	} else if (field.equalsIgnoreCase("publisher")){
        			metadata.setPublisher(value);
        	} else if (field.equalsIgnoreCase("content_start_date")){ 
        		//01\/01\/1974
        			try {
						metadata.setContentDate(processDateString(value));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        	} else if (field.equalsIgnoreCase("content_end_date")){
        	} else if (field.equalsIgnoreCase("publication_start_date")){
        	} else if (field.equalsIgnoreCase("publication_end_date")){
        	} else if (field.equalsIgnoreCase("source")){
        		if (value.equalsIgnoreCase("barton")){
        			source = "barton";
        		} else if (value.equalsIgnoreCase("paper")){
        			source = "paper";
        		}
        	} else if (field.equalsIgnoreCase("west")){
        			minX = value;
        	} else if (field.equalsIgnoreCase("east")){
        			maxX = value;
        	} else if (field.equalsIgnoreCase("south")){
        			minY = value;
        	} else if (field.equalsIgnoreCase("north")){
        			maxY = value;
        	} else {
        			extraFields.add(field);
        	}
        }
        metadata.setInstitution(institution);
        metadata.setAccess(AccessLevel.Public);
        metadata.setGeoreferenced(false);
        metadata.setGeometryType(GeometryType.LibraryRecord);
        
        if (minX != null && minY != null && maxX != null && maxY != null){
        	metadata.setBounds(minX, minY, maxX, maxY);
        }
        if (source.equalsIgnoreCase("barton")){
			metadata.setId("LIBRARY_" + localId);
			metadata.addLocation(new LocationLink(LocationType.libRecord, new URL("http://library.mit.edu/item/" + metadata.getOwsName())));
        	//metadata.setLocation("{\"libRecord\":\"http://library.mit.edu/item/" + metadata.getOwsName() + "\"}");
		} else {
			metadata.setId("UNCATALOGED_" + localId);
			metadata.addLocation(new LocationLink(LocationType.mapRecord, new URL("https://arrowsmith.mit.edu/utilities/paperlookup.php?id=" + metadata.getOwsName())));
        	//metadata.setLocation("{\"mapRecord\":\"https://arrowsmith.mit.edu/utilities/paperlookup.php?id=" + metadata.getOwsName() + "\"}");
		}
        if (extraFields.size() > 0){
        	logger.info("Extra Fields:" + extraFields.size() + ":" + extraFields.toString());
        }
		return metadata;
	}
	/**
	 * Takes the generated Metadata object and ingests it into solr
	 * 
	 * @param metadata
	 * @param warningMessage
	 * @param errorMessage
	 * @return
	 * @throws Exception
	 */
	private String ingestRecord(Metadata metadata) throws Exception{

			logger.info("Trying Solr ingest...[" + metadata.getOwsName() + "]");	
			// and ingest into solr
			SolrIngestResponse solrIngestResponse = null;
			try {
				//determines which MetadataElements are required for successful ingest.  If the element is not
				//required, it will still show a warning if there is a problem with the element value
				Set<MetadataElement> requiredElements = new HashSet<MetadataElement>();
				requiredElements.add(MetadataElement.Institution);
				requiredElements.add(MetadataElement.LayerId);
				requiredElements.add(MetadataElement.LayerName);
				//requiredElements.add(MetadataElement.ContentDate);
				
				solrIngestResponse = solrIngest.writeToSolr(metadata, requiredElements);
			} catch (Exception e){ 
				ingestStatus.addError(metadata.getOwsName(), "Solr Error: " + e.getMessage());
			}
			if (!solrIngestResponse.ingestErrors.isEmpty()){
				for (IngestInfo errorObj: solrIngestResponse.ingestErrors){
					ingestStatus.addError(metadata.getOwsName(), "Solr Ingest Error: " + errorObj.getField() + "&lt;" + errorObj.getNativeName() + "&gt;:" + errorObj.getError() + "-" + errorObj.getMessage());
				}
				logger.error("Solr Ingest Errors:" + solrIngestResponse.ingestErrors.size());
			}
			if (!solrIngestResponse.ingestWarnings.isEmpty()){
				for (IngestInfo errorObj: solrIngestResponse.ingestWarnings){
					ingestStatus.addWarning(metadata.getOwsName(), "Solr Ingest Warnings: " + errorObj.getField() + "&lt;" + errorObj.getNativeName() + "&gt;:" + errorObj.getError() + "-" + errorObj.getMessage());
				}
				logger.warn("Solr Ingest Warnings:" + solrIngestResponse.ingestWarnings.size());
			}
			return solrIngestResponse.solrRecord.getLayerId();
		}
	
	/**
	 * Uses apache commons date utils to parse the returned date string into a date object so we can get
	 * the date in a format we can use
	 * 
	 * @param passedDate
	 * @return
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	protected String processDateString(String passedDate) throws ParseException
	{
		//can't do anything if there's no value passed
		if ((passedDate == null)||(passedDate.equalsIgnoreCase("unknown"))){
			return "";
		}
		List<String> formatsList = new ArrayList<String>();
		//add likely formats in order of likelihood
		formatsList.add("MM/dd/yyyy");
		formatsList.add("yyyyMMdd");
		formatsList.add("yyyyMM");
		formatsList.add("MM/yyyy");
		formatsList.add("MM/dd/yy");
		formatsList.add("MM-dd-yyyy");
		formatsList.add("MMMM yyyy");
		formatsList.add("MMM yyyy");
		formatsList.add("dd MMMM yyyy");
		formatsList.add("dd MMM yyyy");
		formatsList.add("yyyy");


		String[] parsePatterns = formatsList.toArray(new String[formatsList.size()]);
		String returnYear = null;

		passedDate = passedDate.trim();
		Date date = DateUtils.parseDate(passedDate, parsePatterns);
		logger.debug("Document date: " + passedDate + ", Parsed date: " + Integer.toString(date.getYear() + 1900));
		returnYear = Integer.toString(date.getYear() + 1900);

		return returnYear;
	}
	
	public SolrIngest getSolrIngest() {
		return solrIngest;
	}

	public void setSolrIngest(SolrIngest solrIngest) {
		this.solrIngest = solrIngest;
	}
}
