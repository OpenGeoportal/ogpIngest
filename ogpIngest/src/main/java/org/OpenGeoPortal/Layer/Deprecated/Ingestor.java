package org.OpenGeoPortal.Layer.Deprecated;

//import org.OpenGeoPortal.Geoserver.REST.GsREST;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sun.misc.BASE64Encoder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


/**
 * this code makes Tufts data visible to the Tufts portal
 * @author smcdon08
 *
 */	
public class Ingestor 
{
		public static String propertiesFile = "tufts.properties";
		String baseUrl;
        private String restUrl = "http://geoserver-dev.atech.tufts.edu/rest/";
		String username;
		String password;
		
		public Ingestor(String baseUrl, String username, String password)
		{
			this.baseUrl = baseUrl;
			this.username = username;
			this.password = password;
		}
		
		/**
		 * retrieve the list of layers geoserver needs to ingest
		 * @return
		 */
		public List<String> getLayersToIngest()
		{
			Vector<String> layers = new Vector<String>();
			try
			{
				URL url = new URL("http://geoserver-dev.atech.tufts.edu/rest/workspaces/sde/datastores/tufts_sde/featuretypes.xml?list=available");
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            InputStream stream = connection.getInputStream();
	            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				documentBuilderFactory.setValidating(false);  // dtd isn't available
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
				Document document = documentBuilder.parse(stream);
				NodeList nodes = document.getElementsByTagName("featureTypeName");
				System.out.println("number of layers = " + nodes.getLength());
				for (int i = 0 ; i < nodes.getLength() ; i++)
				{
					Node currentNode = nodes.item(i);
					NodeList tempNodes = currentNode.getChildNodes();
					if (tempNodes != null)
					{
						String value = tempNodes.item(0).getNodeValue();
						System.out.println("  " + value);
						layers.add(value);
					}
				}
			}
			catch (IOException e) 
			{
				System.out.println("error in TuftsIngestor.getLayersToIngest, IO error");
				e.printStackTrace();
				return null;
			}
			catch (ParserConfigurationException e) 	
		    {
		    	System.out.println("error in TuftsIngestor.getLayersToIngest ");
		    	e.printStackTrace();
		    }
		    catch (SAXException e) 
		    {
		    	System.out.println("error in TuftsIngestor.getLayersToIngest ");
				e.printStackTrace();
			}
		    return layers;
		}
		
		public final String METHOD_POST = "POST";
		
		public boolean createFeatureType(String wsName, String dsName, String ftName) throws IOException 
		{
			String xml = "<featureType><name>" + ftName + "</name><title>" + ftName
						+ "</title></featureType>";
			int sendRESTint = sendRESTint(METHOD_POST, "/workspaces/" + wsName
						+ "/datastores/" + dsName + "/featuretypes", xml);
			return 201 == sendRESTint;
		}
		
		 public int sendRESTint(String method, String url, String xmlPostContent)
         throws IOException 
         {
			 return sendRESTint(method, url, xmlPostContent, "application/xml",
                 "application/xml");
         }
		 
        public int sendRESTint(String method, String urlEncoded, String postData,
                String contentType, String accept) throws IOException 
        {
        
        	HttpURLConnection connection = sendREST(method, urlEncoded, postData, contentType, accept);
        	return connection.getResponseCode();
        }

        
        /**
         * Send a REST call to Geoserver as a string
         * @param method
         *            e.g. 'POST', 'GET', 'PUT' or 'DELETE'
         * @param urlEncoded
         *            e.g. '/workspaces' or '/workspaces.xml'
         * @param postData
         *            e.g. xml data
         * @param contentType
         *            format of postData, e.g. null or 'text/xml'
         * @param accept
         *            format of response, e.g. null or 'text/xml'
         * @throws IOException
         * 
         * @return null, or response of server
         */
        public String sendRESTstring(String method, String urlEncoded, String postData, String contentType, String accept)
                throws IOException 
       {
        	HttpURLConnection connection = sendREST(method, urlEncoded, postData, contentType, accept);
        	// Read response
        	InputStream in = connection.getInputStream();
        	try 
        	{
                int len;
                byte[] buf = new byte[1024];
                StringBuffer sbuf = new StringBuffer();
                while ((len = in.read(buf)) > 0) 
                {
                        sbuf.append(new String(buf, 0, len));
                }
                return sbuf.toString();
        	} 
        	finally 
        	{
                in.close();
        	}
       }	
        
        
        
        private HttpURLConnection sendREST(String method, String urlAppend,
                String postData, String contentType, String accept)
                throws MalformedURLException, IOException, ProtocolException 
       {
        	//boolean doOut = !METHOD_DELETE.equals(method) && postData != null;
        	// boolean doIn = true; // !doOut
			//URL url = new URL("http://geoserver-dev.atech.tufts.edu/rest/workspaces/sde/datastores/tufts_sde/featuretypes.xml?list=available");

        	String link = restUrl + urlAppend;
        	URL url = new URL(link);
        	System.out.println("POST Data: " + link);
        	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        	connection.setDoOutput(true);
        	// uc.setDoInput(false);
        	if (contentType != null && !"".equals(contentType)) 
        	{
                connection.setRequestProperty("Content-type", contentType);
        	}
        	if (accept != null && !"".equals(accept)) 
        	{
                connection.setRequestProperty("Accept", accept);
        	}

        	connection.setRequestMethod(method.toString());
        	// 	type XML
        	connection.setRequestProperty("Content-Type", contentType);

        	if (false == true) // isAuthorization()) 
        	{
                String userPasswordEncoded = new BASE64Encoder().encode((username + ":" + password).getBytes());
                connection.setRequestProperty("Authorization", "Basic "
                                + userPasswordEncoded);
        	}

        	if (connection.getDoOutput()) 
        	{
                Writer writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(postData);
                writer.flush();
                writer.close();
        	}
        	connection.connect();
        	return connection;
       }
	
		
		public static void main(String[] args)
		{
			try 
			{
				Properties myProps = new Properties();
				FileInputStream MyInputStream;
				MyInputStream = new FileInputStream("tufts.properties");
		        myProps.load(MyInputStream);        
		        String baseUrl = myProps.getProperty("BaseUrl");
		        String username = myProps.getProperty("Username");
		        String password = myProps.getProperty("Password");
		        if ((baseUrl == null) || (username == null) || (password == null))
		        {
		        	System.out.println("error in TuftsIngestor, properties not found");
		        	System.out.println("  baseUrl = " + baseUrl);
		        	System.out.println("  username = " + username);
		        	System.out.println("  password = " + password);
		        	return;
		        }
		        Ingestor tuftsIngestor = new Ingestor(baseUrl, username, password);
		        List<String> layers = tuftsIngestor.getLayersToIngest();
		        System.out.println("layers count = " + layers);
		        //sde:GISPORTAL.TEST.ESRI_CNTRY08
		        boolean create = tuftsIngestor.createFeatureType("sde", "GISPORTAL", "TEST.ESRI_CNTRY08");
		        System.out.println("create = " + create);
			} 
			catch (FileNotFoundException e) 
			{
				System.out.println("error in TuftsIngestor, could not find properties file " + propertiesFile);
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				System.out.println("error in TuftsIngestor, IO error with properties file " + propertiesFile);
				e.printStackTrace();
			}
		}

}
