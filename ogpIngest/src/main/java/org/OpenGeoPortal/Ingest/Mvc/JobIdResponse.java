package org.OpenGeoPortal.Ingest.Mvc;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JobIdResponse {

	private String jobId;
	private List<FileInfo> fileInfo = new ArrayList<FileInfo>();

	public JobIdResponse() {
	}

	public JobIdResponse(String jobId) {
		this.jobId = jobId;
	}
	
	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public List<FileInfo> getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(List<FileInfo> fileInfo) {
		this.fileInfo = fileInfo;
	}
	
	public void addFileInfo(String name, String type, Long size){
		fileInfo.add(new FileInfo(name,type,size));
	}

	public class FileInfo {
		private String name;
		private String type;
		private Long size;
	
		FileInfo(String name, String type, Long size){
			this.name = name;
			this.type = type;
			this.size = size;
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Long getSize() {
			return size;
		}

		public void setSize(Long size) {
			this.size = size;
		}
	}
	
	/*@Override
	public String toString() {
		return "JobIdResponse {jobId=[" + jobId + "], name=[" + fileInfo.name + "], type=[" + fileInfo.type + "], size=[" + fileInfo.size + "]}";
	}*/

}


