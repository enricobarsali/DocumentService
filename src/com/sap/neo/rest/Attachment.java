package com.sap.neo.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
//@XmlRootElement       //only needed if we also want to generate XML
public class Attachment {
    
    private String externalId;
    private String docVersion;
    private String Parent_externalId;
    private String Parent_docVersion;
    private String attachType;
    private String originalFname;
    private String title;
    private String mimeType;
    private String bytes;
    
	public String getExternalId() {
		return externalId;
	}
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	public String getDocVersion() {
		return docVersion;
	}
	public void setDocVersion(String docVersion) {
		this.docVersion = docVersion;
	}
	public String getParent_externalId() {
		return Parent_externalId;
	}
	public void setParent_externalId(String parent_externalId) {
		Parent_externalId = parent_externalId;
	}
	public String getParent_docVersion() {
		return Parent_docVersion;
	}
	public void setParent_docVersion(String parent_docVersion) {
		Parent_docVersion = parent_docVersion;
	}
	public String getAttachType() {
		return attachType;
	}
	public void setAttachType(String attachType) {
		this.attachType = attachType;
	}
	public String getOriginalFname() {
		return originalFname;
	}
	public void setOriginalFname(String originalFname) {
		this.originalFname = originalFname;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getBytes() {
		return bytes;
	}
	public void setBytes(String bytes) {
		this.bytes = bytes;
	}

    
    
    
}