<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="javax.xml.transform.*, java.net.*, java.util.List, java.util.ArrayList, javax.xml.transform.dom.*, javax.xml.transform.stream.*, java.io.*, java.nio.channels.*, javax.xml.parsers.DocumentBuilderFactory, javax.xml.parsers.DocumentBuilder, org.w3c.dom.NodeList, org.w3c.dom.Node, org.w3c.dom.Document" %>
    <%
    	/*
        		Harvest FGDC XML metadata from MassGIS using the WMS getCapabilities document.  MassGIS is a special case, since they encode the url
        		as a keyword with the prefix "ExtractDoc=".  Additionally, there can be other ExtractDocs besides the XML metadata.
        		
        		*/
        		
        		public class MyCrawler extends WebCrawler {

        	        private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
        	                                                          + "|png|tiff?|mid|mp2|mp3|mp4"
        	                                                          + "|wav|avi|mov|mpeg|ram|m4v|pdf" 
        	                                                          + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

        	        /**
        	         * You should implement this function to specify whether
        	         * the given url should be crawled or not (based on your
        	         * crawling logic).
        	         */
        	        @Override
        	        public boolean shouldVisit(WebURL url) {
        	                String href = url.getURL().toLowerCase();
        	                return !FILTERS.matcher(href).matches() && href.startsWith("http://www.ics.uci.edu/");
        	        }

        	        /**
        	         * This function is called when a page is fetched and ready 
        	         * to be processed by your program.
        	         */
        	        @Override
        	        public void visit(Page page) {          
        	                String url = page.getWebURL().getURL();
        	                System.out.println("URL: " + url);

        	                if (page.getParseData() instanceof HtmlParseData) {
        	                        HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
        	                        String text = htmlParseData.getText();
        	                        String html = htmlParseData.getHtml();
        	                        List<WebURL> links = htmlParseData.getOutgoingUrls();

        	                        System.out.println("Text length: " + text.length());
        	                        System.out.println("Html length: " + html.length());
        	                        System.out.println("Number of outgoing links: " + links.size());
        	                }
        	        }
        	}
           		URL indianaMetadata = new URL("http://maps.indiana.edu/metadata");
        		InputStream inputStream = indianaMetadata.openStream();
        		//parse the returned XML
        		// Create a factory
        		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        		// Use document builder factory
        		//ignore validation, dtd
                factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                factory.setValidating(false);

        		DocumentBuilder builder = factory.newDocumentBuilder();
        		//Parse the document
        		Document document = builder.parse(inputStream);
        		
        		//Here, we're looking for links to external xml metadata documents
        		//MassGIS uses the non-standard method of placing a key-value pair in a keyword tag
        		Writer outputWriter = response.getWriter();
        		
        	//Starting directory for parsing
        	File aStartingDir = new File("C:/Program Files");
        	directoryCrawler(aStartingDir);
        	System.out.println("Parsing complete");
	
        		
        		
        		
        		
        		NodeList elementNodes = document.getElementsByTagName("Keyword");
        		for (int i = 0; i < elementNodes.getLength(); i++){
        	Node keywordElement = elementNodes.item(i);
        	String keywordValue = keywordElement.getTextContent().trim();
           			if (keywordValue.toLowerCase().contains("extractdoc") && keywordValue.toLowerCase().contains(".xml")){
           				keywordValue = keywordValue.replace("\n", "");
           				//for some reason, some urls in the MassGIS doc have an extra http:// appended.
           				//18 characters for "ExtractDoc=http://"
        		String metadataUrlString = keywordValue.substring(18);
        		if (!metadataUrlString.contains("http://")){
        			metadataUrlString = "http://" + metadataUrlString;
        		}
        		String localFileName = metadataUrlString.substring(metadataUrlString.lastIndexOf("/") + 1);
        		//write the metadata to a local directory, add the ftname tag with the correct layer name, possibly an access tag.
        		
        		NodeList layerChildNodes = keywordElement.getParentNode().getParentNode().getChildNodes();
        		//<LatLonBoundingBox minx="-73.533" miny="41.23" maxx="-69.898" maxy="42.888"/>
        		String ftName = "";
        		String minx = "";
        		String miny = "";
        		String maxx = "";
        		String maxy = "";
        		for (int j = 0; j < layerChildNodes.getLength(); j++){
        			if (layerChildNodes.item(j).getNodeName().equals("Name")){
        				String layerName = layerChildNodes.item(j).getTextContent().trim();
        				ftName = layerName.substring(layerName.indexOf(":") + 1);
        			} else if (layerChildNodes.item(j).getNodeName().equals("LatLonBoundingBox")) {
        				Node boundsNode = layerChildNodes.item(j);
        				minx = boundsNode.getAttributes().getNamedItem("minx").getTextContent().trim();
        				miny = boundsNode.getAttributes().getNamedItem("miny").getTextContent().trim();
        				maxx = boundsNode.getAttributes().getNamedItem("maxx").getTextContent().trim();
        				maxy = boundsNode.getAttributes().getNamedItem("maxy").getTextContent().trim();
        			}
        		}
        		
        		try{
        			URL metadataUrl = new URL(metadataUrlString);
        			InputStream metadataInputStream = metadataUrl.openStream();
        			//parse the returned XML
        			// Create a factory
        			DocumentBuilderFactory metadataFactory = DocumentBuilderFactory.newInstance();
        			// Use document builder factory
        			//ignore validation, dtd
                			metadataFactory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                			metadataFactory.setValidating(false);

        			DocumentBuilder metadataBuilder = metadataFactory.newDocumentBuilder();
        			//Parse the document
        			Document metadataDocument = metadataBuilder.parse(metadataInputStream);
        			/*
        					<citation>
        						<citeinfo>
        						<origin>Natural Heritage and Endangered Species Program</origin>
        						<pubdate>20031101</pubdate>
        						<ftname Sync="TRUE">GISDATA.LWCSW_POLY</ftname>
        						<geoform Sync="TRUE">vector digital data</geoform>
        						<onlink Sync="TRUE">Server=sun420.env.state.ma.us; Service=esri_SDE; User=gisdata; Version=SDE.DEFAULT</onlink>
        						<title>NHESP Living Waters Critical Supporting Watersheds</title>
        						</citeinfo>
        					</citation>
        			
        						<bounding>
        			<westbc Sync="TRUE">-73.498869</westbc>
        			<eastbc Sync="TRUE">-69.928437</eastbc>
        			<northbc Sync="TRUE">42.875049</northbc>
        			<southbc Sync="TRUE">41.261859</southbc>
        			</bounding>*/
        			//System.out.println("west " + metadataDocument.getElementsByTagName("westbc").item(0).getTextContent() + ", " + minx);
        			//bounds from wms server are more accurate.
        			metadataDocument.getElementsByTagName("westbc").item(0).setTextContent(minx);
        			metadataDocument.getElementsByTagName("southbc").item(0).setTextContent(miny);
        			metadataDocument.getElementsByTagName("eastbc").item(0).setTextContent(maxx);
        			metadataDocument.getElementsByTagName("northbc").item(0).setTextContent(maxy);
        			Node citationNode = metadataDocument.getElementsByTagName("citeinfo").item(0);

        			NodeList citationChildNodes = citationNode.getChildNodes();
        			boolean hasFtName = false;
        			for (int k = 0; k < citationChildNodes.getLength(); k++){
        				if (citationChildNodes.item(k).getNodeName().equalsIgnoreCase("ftname")){
        					hasFtName = true;
        				}
        			}
        			if (hasFtName){
        				//even if ftname exists, make sure it is right
        				metadataDocument.getElementsByTagName("ftname").item(0).setTextContent(ftName);
        			} else {
        				Node ftNameNode = metadataDocument.createElement("ftname");
        			    ftNameNode.appendChild(metadataDocument.createTextNode(ftName));
        				citationNode.appendChild(ftNameNode);
        			}
        			
        			//NodeList  = metadataDocument.getElementsByTagName("Keyword");
        			Source xmlSource = new DOMSource(metadataDocument);
        		
        			TransformerFactory transformerFactory = TransformerFactory.newInstance();
                			Transformer transformer = transformerFactory.newTransformer();
                			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        		
                	        // Prepare the output file
                	        File xmlFile = new File(metadataDirectory + ftName + ".xml");
                	        Result xmlResult = new StreamResult(xmlFile);
                	        
        			transformer.transform(xmlSource, xmlResult);

        			outputWriter.write("<p>Retrieved Metadata for: " + ftName + ":" + metadataUrlString + "</p>");

        		} catch (java.io.FileNotFoundException e){
        			outputWriter.write("<p>Error Getting: " + ftName + ":" + metadataUrlString + "</p>");
        		} catch (java.lang.NullPointerException e){
        			//Should add code to handle null node
        			System.out.println(ftName + ": unexpected null value.");
        		} catch (Exception e){
        			System.out.println(e);
        		}
           			}
        		}
    %>
