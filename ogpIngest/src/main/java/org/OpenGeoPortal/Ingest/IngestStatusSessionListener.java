package org.OpenGeoPortal.Ingest;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class IngestStatusSessionListener implements HttpSessionListener {
@Autowired
private IngestStatusManager ingestStatusManager;
final Logger logger = LoggerFactory.getLogger(this.getClass());

   public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
	   String sessionId = httpSessionEvent.getSession().getId();
	   ingestStatusManager.removeStatusBySessionId(sessionId);
	   logger.info("Removing Status info for session: "+ sessionId);
   }

public void sessionCreated(HttpSessionEvent httpSessionEvent) {
	// not doing anything here for now
	
}	


}
