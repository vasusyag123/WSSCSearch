package com.wssc.filenet.migration.dao;

import java.io.Serializable;
import java.util.Date;

import com.filenet.api.util.Id;




public class TestStationsDocuments implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String docId;
	public Id objId;
	
	
	public boolean mimeType = true;
	private String  documentTitle;  
	
	public boolean selected;
	public String securityLevel;

	private String ttestStationName ;
	private String tcontractNumber;
	private String tWSSCContractNo;
	private String tTsNo;
	private String t200FTSheet;
	private String tLocality;
	private String tAnodes;
	private String tcathProtect;
	
	public String comment;
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}

	
		
	public Id getObjId() {
		return objId;
	}
	public void setObjId(Id objId) {
		this.objId = objId;
	}
	public boolean isMimeType() {
		return mimeType;
	}
	public void setMimeType(boolean mimeType) {
		this.mimeType = mimeType;
	}
	

	public String getDocumentTitle() {
		return documentTitle;
	}
	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}

	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public String getSecurityLevel() {
		return securityLevel;
	}
	public void setSecurityLevel(String securityLevel) {
		this.securityLevel = securityLevel;
	}
	
	public String getTtestStationName() {
		return ttestStationName;
	}
	public void setTtestStationName(String ttestStationName) {
		this.ttestStationName = ttestStationName;
	}
	public String getTcontractNumber() {
		return tcontractNumber;
	}
	public void setTcontractNumber(String tcontractNumber) {
		this.tcontractNumber = tcontractNumber;
	}
	public String gettWSSCContractNo() {
		return tWSSCContractNo;
	}
	public void settWSSCContractNo(String tWSSCContractNo) {
		this.tWSSCContractNo = tWSSCContractNo;
	}
	public String gettTsNo() {
		return tTsNo;
	}
	public void settTsNo(String tTsNo) {
		this.tTsNo = tTsNo;
	}
	public String getT200FTSheet() {
		return t200FTSheet;
	}
	public void setT200FTSheet(String t200ftSheet) {
		t200FTSheet = t200ftSheet;
	}
	public String gettLocality() {
		return tLocality;
	}
	public void settLocality(String tLocality) {
		this.tLocality = tLocality;
	}
	public String gettAnodes() {
		return tAnodes;
	}
	public void settAnodes(String tAnodes) {
		this.tAnodes = tAnodes;
	}
	public String getTcathProtect() {
		return tcathProtect;
	}
	public void setTcathProtect(String tcathProtect) {
		this.tcathProtect = tcathProtect;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
		
}
