package com.wssc.filenet.migration.dao;

import java.io.Serializable;


public class ViewScannedDocuments implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String docId;
	public String subCategory;
	public String title;
	public String date;
	public boolean flag;
	public boolean mimeType = true;
	public boolean isMimeType() {
		return mimeType;
	}
	public void setMimeType(boolean mimeType) {
		this.mimeType = mimeType;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	public String getSubCategory() {
		return subCategory;
	}
	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
