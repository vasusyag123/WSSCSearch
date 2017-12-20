package com.wssc.filenet.migration.dao;

import java.io.Serializable;

import com.filenet.api.util.Id;

public class PlumbingCardsDocuments implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String docId;
	public Id objId;
	public boolean mimeType = true;
	private String documentTitle;  
	private String pcdNo;
	private String pgrpNo;
	
	public boolean selected;
	private String pStreetName ;
	private String pHouseNumber;
	private String pContractNo;
	private String pPermitNo;
	private String pCountyCd;
	private String pPgNo;
	private String pComment;
	
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
	public String getpStreetName() {
		return pStreetName;
	}
	public void setpStreetName(String pStreetName) {
		this.pStreetName = pStreetName;
	}
	public String getpHouseNumber() {
		return pHouseNumber;
	}
	public void setpHouseNumber(String pHouseNumber) {
		this.pHouseNumber = pHouseNumber;
	}
	public String getpContractNo() {
		return pContractNo;
	}
	public void setpContractNo(String pContractNo) {
		this.pContractNo = pContractNo;
	}
	public String getpPermitNo() {
		return pPermitNo;
	}
	public void setpPermitNo(String pPermitNo) {
		this.pPermitNo = pPermitNo;
	}
	public String getpCountyCd() {
		return pCountyCd;
	}
	public void setpCountyCd(String pCountyCd) {
		this.pCountyCd = pCountyCd;
	}
	public String getpPgNo() {
		return pPgNo;
	}
	public void setpPgNo(String pPgNo) {
		this.pPgNo = pPgNo;
	}
	public String getpComment() {
		return pComment;
	}
	public void setpComment(String pComment) {
		this.pComment = pComment;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	

	
}
