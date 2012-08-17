package org.OpenGeoPortal.Ingest;

import org.OpenGeoPortal.Layer.Metadata;

public interface ExtraTasks {
	String doTasks(Metadata metadata) throws Exception;
}
