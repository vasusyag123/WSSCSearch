package com.wssc.filenet.migration.dao;

import java.io.Serializable;
import java.util.Date;

import com.filenet.api.util.Id;




public class EngineeringDocuments implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String docId;
	public String category;
	public String subCategory;
	public String title;
	public String page;
	public String pageCount;
	public String date;
	public String finalWater;
	public String finalSewer;
	public String plan;
	public String oldWsscContract;
	public String type;
	public String comment;
	
	public boolean mimeType = true;
	public boolean selected;
	public String securityLevel;

	private String  documentTitle;  
	private String  facilityName;  
	private String  projectName; 
	private String  designComp; 
	private String  externalReference;   
	private String  fileName;     
	private String  contractCode;  
	private String  sheetTitle;   
	private String  sheetNum;    
	private String  plansetNum; 
	private String  projectType; 
	private String  primDiscipCode;
	private String  secDescipCode;
	private String  dwgType;     
	private String  statusCode;   

	private String  cipespNum;     
	private String  shopRef;     
	private String 	planRef;   
	private String 	finalRef  ;  
	private String 	offSite   ; 
	private String 	scadaName ;
	private String 	comments ;
	private String  wsscPoc;
	
	private Date  initialDate;
	private Date  approvedDate;
	private Date  confirmedDate;
	private Date  asBuiltDate;
	private Date  wsscModDate  ;
	
	
	private String  wsscPocEnginRefrcNo;
	private String 	plansetTotal ;
	private String 	minX;
 	private String 	minY;  
	private String 	maxX;  
	private String 	maxY;        
	private String 	wsscModNum;    
	private String 	xREf;  
	
	public Id objId;
		
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
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
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
	public String getFinalSewer() {
		return finalSewer;
	}
	public void setFinalSewer(String finalSewer) {
		this.finalSewer = finalSewer;
	}
	public String getFinalWater() {
		return finalWater;
	}
	public void setFinalWater(String finalWater) {
		this.finalWater = finalWater;
	}
	public String getOldWsscContract() {
		return oldWsscContract;
	}
	public void setOldWsscContract(String oldWsscContract) {
		this.oldWsscContract = oldWsscContract;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getPageCount() {
		return pageCount;
	}
	public void setPageCount(String pageCount) {
		this.pageCount = pageCount;
	}
	public String getPlan() {
		return plan;
	}
	public void setPlan(String plan) {
		this.plan = plan;
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
	public String getSecurityLevel() {
		return securityLevel;
	}
	public void setSecurityLevel(String securityLevel) {
		this.securityLevel = securityLevel;
	}
	public String getDocumentTitle() {
		return documentTitle;
	}
	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}
	public String getFacilityName() {
		return facilityName;
	}
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getDesignComp() {
		return designComp;
	}
	public void setDesignComp(String designComp) {
		this.designComp = designComp;
	}
	public String getExternalReference() {
		return externalReference;
	}
	public void setExternalReference(String externalReference) {
		this.externalReference = externalReference;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getContractCode() {
		return contractCode;
	}
	public void setContractCode(String contractCode) {
		this.contractCode = contractCode;
	}
	public String getSheetTitle() {
		return sheetTitle;
	}
	public void setSheetTitle(String sheetTitle) {
		this.sheetTitle = sheetTitle;
	}
	public String getSheetNum() {
		return sheetNum;
	}
	public void setSheetNum(String sheetNum) {
		this.sheetNum = sheetNum;
	}
	public String getPlansetNum() {
		return plansetNum;
	}
	public void setPlansetNum(String plansetNum) {
		this.plansetNum = plansetNum;
	}
	public String getProjectType() {
		return projectType;
	}
	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}
	public String getPrimDiscipCode() {
		return primDiscipCode;
	}
	public void setPrimDiscipCode(String primDiscipCode) {
		this.primDiscipCode = primDiscipCode;
	}
	public String getSecDescipCode() {
		return secDescipCode;
	}
	public void setSecDescipCode(String secDescipCode) {
		this.secDescipCode = secDescipCode;
	}
	public String getDwgType() {
		return dwgType;
	}
	public void setDwgType(String dwgType) {
		this.dwgType = dwgType;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public String getCipespNum() {
		return cipespNum;
	}
	public void setCipespNum(String cipespNum) {
		this.cipespNum = cipespNum;
	}
	public String getShopRef() {
		return shopRef;
	}
	public void setShopRef(String shopRef) {
		this.shopRef = shopRef;
	}
	public String getPlanRef() {
		return planRef;
	}
	public void setPlanRef(String planRef) {
		this.planRef = planRef;
	}
	public String getFinalRef() {
		return finalRef;
	}
	public void setFinalRef(String finalRef) {
		this.finalRef = finalRef;
	}
	public String getOffSite() {
		return offSite;
	}
	public void setOffSite(String offSite) {
		this.offSite = offSite;
	}
	public String getScadaName() {
		return scadaName;
	}
	public void setScadaName(String scadaName) {
		this.scadaName = scadaName;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getWsscPoc() {
		return wsscPoc;
	}
	public void setWsscPoc(String wsscPoc) {
		this.wsscPoc = wsscPoc;
	}
	public Date getInitialDate() {
		return initialDate;
	}
	public void setInitialDate(Date initialDate) {
		this.initialDate = initialDate;
	}
	public Date getApprovedDate() {
		return approvedDate;
	}
	public void setApprovedDate(Date approvedDate) {
		this.approvedDate = approvedDate;
	}
	public Date getConfirmedDate() {
		return confirmedDate;
	}
	public void setConfirmedDate(Date confirmedDate) {
		this.confirmedDate = confirmedDate;
	}
	public Date getAsBuiltDate() {
		return asBuiltDate;
	}
	public void setAsBuiltDate(Date asBuiltDate) {
		this.asBuiltDate = asBuiltDate;
	}
	public Date getWsscModDate() {
		return wsscModDate;
	}
	public void setWsscModDate(Date wsscModDate) {
		this.wsscModDate = wsscModDate;
	}
	public String getWsscPocEnginRefrcNo() {
		return wsscPocEnginRefrcNo;
	}
	public void setWsscPocEnginRefrcNo(String wsscPocEnginRefrcNo) {
		this.wsscPocEnginRefrcNo = wsscPocEnginRefrcNo;
	}
	public String getPlansetTotal() {
		return plansetTotal;
	}
	public void setPlansetTotal(String plansetTotal) {
		this.plansetTotal = plansetTotal;
	}
	public String getMinX() {
		return minX;
	}
	public void setMinX(String minX) {
		this.minX = minX;
	}
	public String getMinY() {
		return minY;
	}
	public void setMinY(String minY) {
		this.minY = minY;
	}
	public String getMaxX() {
		return maxX;
	}
	public void setMaxX(String maxX) {
		this.maxX = maxX;
	}
	public String getMaxY() {
		return maxY;
	}
	public void setMaxY(String maxY) {
		this.maxY = maxY;
	}
	public String getWsscModNum() {
		return wsscModNum;
	}
	public void setWsscModNum(String wsscModNum) {
		this.wsscModNum = wsscModNum;
	}
	public String getxREf() {
		return xREf;
	}
	public void setxREf(String xREf) {
		this.xREf = xREf;
	}
	
}
