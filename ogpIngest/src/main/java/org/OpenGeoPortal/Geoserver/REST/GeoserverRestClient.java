package org.OpenGeoPortal.Geoserver.REST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.OpenGeoPortal.Geoserver.REST.DataStoreInfoResponseJson.DataStore.ConnectionParameters.ValueMap;
import org.OpenGeoPortal.Ingest.IngestProperties;
import org.OpenGeoPortal.Ingest.MapserverRestClient;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class GeoserverRestClient implements MapserverRestClient {
	    private final RestTemplate restTemplate;
	    String workspace;
	    String layerId;
		private String datastore;
		//private String metadataType;
		//private String metadataContentType;
		//private String layerTitle;
		//private String layerAbstract;
		//private String metadataUrl;
		private AddCoverageStoreRequestJson addCoverageStoreRequestJson;
		private AddCoverageRequestJson addCoverageRequestJson;
		private HttpHost targetHost;
		private String geoserverUrl;
		IngestProperties ingestProperties;
		final Logger logger = LoggerFactory.getLogger(this.getClass());

		public GeoserverRestClient(String geoserverUrl, String workspace, String datastore, String username, String password) {
			this.workspace = workspace;
			this.datastore = datastore;
			this.geoserverUrl = geoserverUrl;

			geoserverUrl = geoserverUrl.replace("http://", "");
			geoserverUrl = geoserverUrl.replace("https://", "");
			if (geoserverUrl.indexOf("/") > 0){
				geoserverUrl = geoserverUrl.substring(0, geoserverUrl.indexOf("/"));
			}
	        Credentials credentials = new UsernamePasswordCredentials(username, password);
	    	DefaultHttpClient httpclient = new DefaultHttpClient();
	    	this.targetHost = new HttpHost(geoserverUrl);
			httpclient.getCredentialsProvider().setCredentials(
	    	        new AuthScope(AuthScope.ANY), 
	    	        credentials);
	    	
			// Create AuthCache instance
			AuthCache authCache = new BasicAuthCache();
			// Generate BASIC scheme object and add it to the local auth cache
			BasicScheme basicAuth = new BasicScheme();
			authCache.put(this.targetHost, basicAuth);

			// Add AuthCache to the execution context
			BasicHttpContext localcontext = new BasicHttpContext();
			localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);

			HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpclient);
			
	        this.restTemplate = new RestTemplate(factory);
	    }
	    
		public String getWorkspace(){
			return this.workspace;
		}
		
		public String getGeoserverUrl(){
			return this.geoserverUrl;
		}
		
		public String getDatastore(){
			return this.datastore;
		}

	    public String addVectorLayer(String layerName) {

	    		AddVectorRequestJson addVectorRequestJson = this.createAddVectorRequestObject(layerName);
	    		//rest/workspaces/sde/datastores/arcsde10/featuretypes/GISPORTAL.GISOWNER01.NEWENGESAPTS98COPY_PROJECT.json
	    		//tells if already configured
	    		Map<String, String> vars = new HashMap<String, String>();
	    		vars.put("serverName", this.geoserverUrl);
	    		vars.put("workspace", this.workspace);
	    		vars.put("datastore", this.datastore);
	    		String result = "";
	    		try {
	    			result = this.restTemplate.postForObject("{serverName}/rest/workspaces/{workspace}/datastores/{datastore}/featuretypes.json", 
	    			addVectorRequestJson, String.class, vars);
	    			logger.info("returned:" + result);
	    			logger.info("added layer:" + layerName);
	    		} catch (HttpClientErrorException e){
	    			HttpStatus status = e.getStatusCode();
		    		if ((status == HttpStatus.INTERNAL_SERVER_ERROR)||(status == HttpStatus.SERVICE_UNAVAILABLE)){
		    			//wait and try again
		                try {
							Thread.sleep(10000);
			    			result = this.restTemplate.postForObject("{serverName}/rest/workspaces/{workspace}/datastores/{datastore}/featuretypes.json", 
			    	    			addVectorRequestJson, String.class, vars);
			    	    			logger.info("returned:" + result);
			    	    			logger.info("added layer:" + layerName);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
			    			logger.error("failed to add layer:" + layerName);
			    			return "failed";
						}
		    		} else {
		    			logger.error("failed to add layer:" + layerName);
		    			return "failed";
		    		}
		    	} 
	    		catch (Exception e){
	    			logger.error("failed to add layer:" + layerName);
	    			return "failed";
	    		}
	    		return "success";
	    }
	    
	    public Boolean featureTypeExists(String layerName){
	    	//rest/workspaces/sde/datastores/arcsde10/featuretypes/GISPORTAL.GISOWNER01.NEWENGESAPTS98COPY_PROJECT.json
	    	//tells if already configured
	    	Map<String, String> vars = new HashMap<String, String>();
	    	vars.put("serverName", this.geoserverUrl);
	    	vars.put("workspace", this.workspace);
	    	vars.put("datastore", this.datastore);
	    	vars.put("featuretypename", layerName);
	    	try {
	    	String result = this.restTemplate.getForObject("{serverName}/rest/workspaces/{workspace}/datastores/{datastore}/featuretypes/{featuretypename}.json", 
	    			String.class, vars);
	    	} catch (HttpClientErrorException e){
	    		return false;
	    	}

	    	return true;
	    	
	    }
	    
	    private AddVectorRequestJson createAddVectorRequestObject(String layerName) {
	    	//System.out.println(layerName);
	    	
	    	AddVectorRequestJson addVectorRequestJson = new AddVectorRequestJson();
	    	addVectorRequestJson.featureType.setName(layerName);
	    	//addVectorRequestJson.featureType.setNativeName(layerPrefix + layerName);
	    	//addVectorRequestJson.featureType.title = layerTitle;
	    	//addVectorRequestJson.featureType.description = layerAbstract;
	    	//addVectorRequestJson.featureType.keywords.string.add("keywords");
	    	//addVectorRequestJson.featureType.metadataLinks.metadataLink.get(0).content = this.metadataUrl;
	    	//addVectorRequestJson.featureType.metadataLinks.metadataLink.get(0).metadataType = this.metadataType;
	    	//addVectorRequestJson.featureType.metadataLinks.metadataLink.get(0).type = this.metadataContentType;

	    	return addVectorRequestJson;
	    }
	    
	    public ArrayList<String> queryAvailableVectors(){
	    	Map<String, String> vars = new HashMap<String, String>();
	    	vars.put("serverName", this.geoserverUrl);
	    	vars.put("workspace", this.workspace);
	    	vars.put("datastore", this.datastore);
	    	//System.out.println(this.geoserverUrl + "/rest/workspaces/" + this.workspace + "/datastores/" + this.datastore + "/featuretypes.json?list=available");
	    	AvailableVectorsResponseJson result = null;
	    	try {
	    		result = this.restTemplate.getForObject("{serverName}/rest/workspaces/{workspace}/datastores/{datastore}/featuretypes.json?list=available", 
	    			AvailableVectorsResponseJson.class, vars);
	    	} catch (HttpClientErrorException e){
	    		HttpStatus status = e.getStatusCode();
	    		logger.warn(status.toString());
	    		if ((status == HttpStatus.INTERNAL_SERVER_ERROR)||(status == HttpStatus.SERVICE_UNAVAILABLE)){
	    			//retry once
	                try {
						Thread.sleep(10000);
			    		result = this.restTemplate.getForObject("{serverName}/rest/workspaces/{workspace}/datastores/{datastore}/featuretypes.json?list=available", 
				    			AvailableVectorsResponseJson.class, vars);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
		    			logger.error("failed to query available layers");
					}
	    		} else {
	    			logger.error("failed to query available layers:" + e.getStatusCode().getReasonPhrase());
	    		}
	    	}
	    	return result.layerList.string;
	    }
	    
	    public void createAddCoverageStoreRequestJson(String layerName, String dataStore) throws Exception{
	    	this.addCoverageStoreRequestJson = new AddCoverageStoreRequestJson();
	    	this.addCoverageStoreRequestJson.getCoverageStore().setType("ArcSDE Raster");
	    	this.addCoverageStoreRequestJson.getCoverageStore().setEnabled("true");
	    	String connectionString = getSDECoverageConnectionStringFromDataStore(dataStore);
	    	this.addCoverageStoreRequestJson.getCoverageStore().setUrl(connectionString + layerName);
	    	this.addCoverageStoreRequestJson.getCoverageStore().setName(layerName.replace(".", "_"));
	    }
	    
	    private String getSDECoverageConnectionStringFromDataStore(String dataStore) throws Exception {
    		//http://delisle.mit.edu:8080/geoserver/rest/workspaces/sde/datastores/ArrowsmithSDE.json
	    	//"sde://sde_data:gis@arrowsmith.mit.edu:5150/#"
	    	//{"dataStore":{"name":"ArrowsmithSDE","type":"ArcSDE","enabled":true,"workspace":{"name":"sde","href":"http:\/\/delisle.mit.edu:8080\/geoserver\/rest\/workspaces\/sde.json"},
	    	//"connectionParameters":{"entry":[{"@key":"port","$":"5150"},{"@key":"dbtype","$":"arcsde"},{"@key":"datastore.allowNonSpatialTables","$":"false"},{"@key":"pool.timeOut","$":"500"},{"@key":"server","$":"arrowsmith.mit.edu"},{"@key":"pool.maxConnections","$":"6"},{"@key":"password","$":"gis"},{"@key":"user","$":"sde_data"},{"@key":"pool.minConnections","$":"2"},{"@key":"namespace","$":"http:\/\/geoserver.sf.net"}]},"__default":false,"featureTypes":"http:\/\/delisle.mit.edu:8080\/geoserver\/rest\/workspaces\/sde\/datastores\/ArrowsmithSDE\/featuretypes.json"}}
    		Map<String, String> vars = new HashMap<String, String>();
    		vars.put("serverName", this.geoserverUrl);
    		vars.put("workspace", this.workspace);
    		vars.put("datastore", dataStore);
	    	DataStoreInfoResponseJson dataStoreResponse = restTemplate.getForObject("{serverName}/rest/workspaces/{workspace}/datastores/{datastore}.json", 
	    			DataStoreInfoResponseJson.class, vars);
	    	String user = null;
	    	String password = null;
	    	String server = null;
	    	String port = null;
	    	
	    	for (ValueMap valueMap : dataStoreResponse.dataStore.connectionParameters.entry){
	    		String key = valueMap.key;
	    		if (key.equalsIgnoreCase("port")){
	    			port = valueMap.value;
	    		} else if (key.equalsIgnoreCase("server")){
	    			server = valueMap.value;
	    		} else if (key.equalsIgnoreCase("user")){
	    			user = valueMap.value;
	    		} else if (key.equalsIgnoreCase("password")){
	    			password = valueMap.value;
	    		}
	    	}
	    	if ((user == null)||(password == null)||(port == null)||(server == null)){
	    		throw new Exception("Valid connection parameters not found");
	    	}
			String connectionString = "sde://" + user + ":" + password + "@" + server + ":" + port + "/#";
			return connectionString;
		}

		public String addCoverage(String layerName){
	    	//http://geoserver01.uit.tufts.edu/rest/workspaces/sde/coveragestores
    		Map<String, String> vars = new HashMap<String, String>();
    		vars.put("serverName", this.geoserverUrl);
    		vars.put("workspace", this.workspace);
    		vars.put("coveragestore", layerName.replace(".", "_"));
    		createAddCoverageRequestJson(layerName);
    		String result = restTemplate.postForObject("{serverName}/rest/workspaces/{workspace}/coveragestores/{coveragestore}/coverages", 
    			this.addCoverageRequestJson, String.class, vars);
    		return "success";
	    }
	    
	    public void createAddCoverageRequestJson(String layerName){
	    	this.addCoverageRequestJson = new AddCoverageRequestJson();
	    	this.addCoverageRequestJson.getCoverage().setName(layerName);
	    }
	    
	    public String addCoverageStore(String layerName) throws Exception{
    		Map<String, String> vars = new HashMap<String, String>();
    		vars.put("serverName", this.geoserverUrl);
    		vars.put("workspace", this.workspace);
    		vars.put("datastore", this.datastore);
    		//http://delisle.mit.edu:8080/geoserver/rest/workspaces/sde/datastores/ArrowsmithSDE.json
    		createAddCoverageStoreRequestJson(layerName, this.datastore);
    		String result = restTemplate.postForObject("{serverName}/rest/workspaces/{workspace}/coveragestores", 
    			this.addCoverageStoreRequestJson, String.class, vars);
    		return result;
	    }

	    public String reloadConfig(){
	    	Map<String, String> vars = new HashMap<String, String>();
	    	vars.put("serverName", this.geoserverUrl);
	    	String result = restTemplate.postForObject("{serverName}/rest/reload", 
	    			null, String.class, vars);
	    	return result;
	    }
	    
	    public void seedLayer(String layerName){
	    	// curl -XPOST -u ${username}:${password} -H "Content-type: application/json" -d "{'seedRequest':{'name':'${layernameWithWorkspace}','srs':{'number':${epsgCode},'bounds':{'coords':{'double':['${bounds1}','${bounds2}','${bounds3}','${bounds4}']}},'zoomStart':1,'zoomStop':12,'format':'image\/png','type':'seed','threadCount':4}}}" http://${geoserverhostandpath}/gwc/rest/seed/${layernameWithWorkspace}.json
	    }

}
