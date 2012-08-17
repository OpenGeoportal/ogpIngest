package org.OpenGeoPortal.Layer.Deprecated;

import java.io.File;
import java.io.FileInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class IngestXmlToSolr {

	/**
	 * Example how to use unbuffered chunk-encoded POST request.
	 */

	    public static void addToSolr(String[] args) throws Exception {
	        if (args.length != 1)  {
	            System.out.println("File path not given");
	            System.exit(1);
	        }
	        HttpClient httpclient = new DefaultHttpClient();
	        try {
	            HttpPost httppost = new HttpPost("http://localhost:8080" +
	                    "/servlets-examples/servlet/RequestInfoExample");

	            File file = new File(args[0]);

	            InputStreamEntity reqEntity = new InputStreamEntity(
	                    new FileInputStream(file), -1);
	            reqEntity.setContentType("binary/octet-stream");
	            reqEntity.setChunked(true);
	            // It may be more appropriate to use FileEntity class in this particular
	            // instance but we are using a more generic InputStreamEntity to demonstrate
	            // the capability to stream out data from any arbitrary source
	            //
	            // FileEntity entity = new FileEntity(file, "binary/octet-stream");

	            httppost.setEntity(reqEntity);

	            System.out.println("executing request " + httppost.getRequestLine());
	            HttpResponse response = httpclient.execute(httppost);
	            HttpEntity resEntity = response.getEntity();

	            System.out.println("----------------------------------------");
	            System.out.println(response.getStatusLine());
	            if (resEntity != null) {
	                System.out.println("Response content length: " + resEntity.getContentLength());
	                System.out.println("Chunked?: " + resEntity.isChunked());
	            }
	            EntityUtils.consume(resEntity);
	        } finally {
	            // When HttpClient instance is no longer needed,
	            // shut down the connection manager to ensure
	            // immediate deallocation of all system resources
	            httpclient.getConnectionManager().shutdown();
	        }
	    }
	    
	    public static void layerExists(){
	    	
	    }
	    
	    public static void commitToSolr(){
	    	
	    }

	
}
