package org.OpenGeoPortal.Ingest;

import java.util.List;

public class IngestResponse {
	public List<IngestInfo> ingestErrors;
	public List<IngestInfo> ingestWarnings;
	
	public void addError(String field, String nativeName, String error, String message){
		ingestErrors.add(new IngestInfo(field, nativeName, error, message));
	}
	
	public void addWarning(String field, String nativeName, String error, String message){
		ingestWarnings.add(new IngestInfo(field, nativeName, error, message));
	}
	
	public class IngestInfo {
		String field;
		String nativeName;
		String error;
		String message;
		
		IngestInfo(String field, String nativeName, String error, String message){
			this.field = field;
			this.nativeName = nativeName;
			this.error = error;
			this.message = message;
		}

		public String getField() {
			return field;
		}

		public String getNativeName() {
			return nativeName;
		}

		public String getError() {
			return error;
		}

		public String getMessage() {
			return message;
		}
	}
}
