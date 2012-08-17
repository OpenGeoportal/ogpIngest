package org.OpenGeoPortal.Ingest;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.task.AsyncTaskExecutor;

public abstract class AbstractIngestJobSubmitter implements BeanFactoryAware{

	IngestStatusManager ingestStatusManager;
	protected AsyncTaskExecutor asyncTaskExecutor;
	protected BeanFactory beanFactory;
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	//private IngestStatus ingestStatus;

	public AsyncTaskExecutor getAsyncTaskExecutor() {
		return asyncTaskExecutor;
	}

	public void setAsyncTaskExecutor(AsyncTaskExecutor asyncTaskExecutor) {
		this.asyncTaskExecutor = asyncTaskExecutor;
	}

	public IngestStatusManager getIngestStatusManager() {
		return ingestStatusManager;
	}

	public void setIngestStatusManager(IngestStatusManager ingestStatusManager) {
		this.ingestStatusManager = ingestStatusManager;
	}
	
	protected UUID registerJob(String sessionId){
		UUID jobId = UUID.randomUUID();
		logger.info(jobId.toString());
		IngestStatus ingestStatus = new GenericIngestStatus();
		ingestStatusManager.addIngestStatus(jobId, sessionId, ingestStatus);
		return jobId;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	//abstract public UUID runIngestJob();

}
