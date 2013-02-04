package org.OpenGeoPortal.Ingest;

import java.net.MalformedURLException;
import java.util.List;
import java.util.UUID;

import org.OpenGeoPortal.Solr.SolrClient;
import org.OpenGeoPortal.Solr.SolrExchangeRecord;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicRemoteSolrIngestJob implements RemoteSolrIngestJob, Runnable {
	private SolrClient solrClient;	
	private IngestStatusManager ingestStatusManager;
	private String institution;
	private String solrUrl;
	private UUID jobId;
	private IngestStatus ingestStatus;
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	int fetchSize = 50;

	private void getSolrRecords() throws MalformedURLException{
		ingestStatus = ingestStatusManager.getIngestStatus(jobId);
		//the client for the external solr
		HttpSolrServer solrCore = new HttpSolrServer(solrUrl);
		solrCore.setParser(new XMLResponseParser());

		try{
		int recordCount = 0;
		int totalCount = 0;
		do {
			//perform the query
			QueryResponse rsp = null;
			try{
				SolrQuery query = new SolrQuery();
				query.setQuery( "Institution:" + institution );
				query.setRows(fetchSize);
				query.setStart(recordCount);
				rsp = solrCore.query(query);
			} catch (Exception e){
				logger.error("query failed");
				ingestStatus.addError("solrQuery  [Institution: " + institution + "]", "query failed");
			}
			if (totalCount == 0){
				totalCount = (int) rsp.getResults().getNumFound();
				logger.info("Number found: " + Integer.toString(totalCount));
			}
			List<SolrExchangeRecord> beans = null;
			//List<SolrDocument> solrdocs = null;
			try{
				//unmarshall the results from the query
				//solrdocs = rsp.getResults();
				beans = rsp.getBeans(SolrExchangeRecord.class);	 
				if (!beans.isEmpty()){
				//if(!solrdocs.isEmpty()){
					try {
						//write the records to the local solr instance
						//Boolean solrClientExists = (solrClient != null);
						//logger.info("solrclientexists?: " + Boolean.toString(solrClientExists));
						//solrClient.getSolrServer().
						solrClient.getSolrServer().addBeans(beans);
						/*Iterator<SolrDocument> docsiterator = solrdocs.iterator();
						HttpSolrServer solr = solrClient.getSolrServer();
						while (docsiterator.hasNext()){
							solr.add(ClientUtils.toSolrInputDocument(docsiterator.next()));
						}*/
					} catch (Exception e){
						logger.error("Error setting beans" + e.getMessage());
						ingestStatus.addError("solrQuery  [Institution: " + institution + "]", "Error setting beans" + e.getMessage());
					}
				}
			} catch (Exception e){
				logger.error("error getting beans" + e.getMessage());	
				ingestStatus.addError("solrQuery  [Institution: " + institution + "]", "Error getting beans" + e.getMessage());

			}

			recordCount += fetchSize;
			//the condition on this while is a little weird, but seems to work
		} while (recordCount <= (totalCount + fetchSize));
		//commit the additions
		solrClient.commit();
	} catch (Exception e){
		logger.error(e.getMessage());
		ingestStatus.addError("solrQuery  [Institution: " + institution + "]", "Unknown Exception: " + e.getMessage());
	}
		ingestStatus.setJobStatus(IngestJobStatus.Succeeded);
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
			getSolrRecords();
		} catch (Exception e){
			logger.error("Error in uploadMetadata");
			ingestStatus.setJobStatus(IngestJobStatus.Failed);
		}
	}

	public void init(UUID jobId, String institution, String remoteSolrUrl) {
		this.jobId = jobId;
		this.institution = institution;
		this.solrUrl = remoteSolrUrl;
	}
}
