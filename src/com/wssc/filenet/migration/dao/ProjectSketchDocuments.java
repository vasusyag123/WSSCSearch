package com.wssc.filenet.migration.dao;

import java.io.Serializable;

public class ProjectSketchDocuments implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String docId;
	public String documentClass;
	public String project;
	public String title;
	public String date;
	public String reviewMarkup;
	public String submittalNo;
	public String comment;
	public boolean selected;
	public boolean mimeType = true;
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getDocumentClass() {
		return documentClass;
	}
	public void setDocumentClass(String documentClass) {
		this.documentClass = documentClass;
	}
	public String getProject() {
		return project;
	}
	public void setProject(String project) {
		this.project = project;
	}
	public String getReviewMarkup() {
		return reviewMarkup;
	}
	public void setReviewMarkup(String reviewMarkup) {
		this.reviewMarkup = reviewMarkup;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String sketchDate) {
		this.date = sketchDate;
	}
	public String getSubmittalNo() {
		return submittalNo;
	}
	public void setSubmittalNo(String submittalNo) {
		this.submittalNo = submittalNo;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public boolean isMimeType() {
		return mimeType;
	}
	public void setMimeType(boolean mimeType) {
		this.mimeType = mimeType;
	}
}
