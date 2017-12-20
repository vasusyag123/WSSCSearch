package com.wssc.filenet.migration.managedbeans;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELContext;
 
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
 
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.component.datatable.DataTable;

import com.filenet.api.collection.DocumentSet;
import com.filenet.api.core.Document;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;

 
import com.wssc.filenet.migration.ce.CEConnection;
import com.wssc.filenet.migration.dao.EngineeringDocuments;
import com.wssc.filenet.migration.utils.PropertyReader;
/*
 * http://10.215.40.94:9081/Drawings/facilitiesQuery.faces?EngFac=ACCKBE3452A02-M01
 * */
@ManagedBean(name="documentsBean")
@SessionScoped
public class QueryEngineeringDocumentsBean {
	public String title = "Available Engineering Facilities Documents";
	private String uiFacilityName="";
	private String uicontractNumber="";
	private String uiProjectName="";
	private String uiDrawingType="";
//	private String mimeType="";
//	private String uidocumentTitle="";
	private String uiDescipline="";
	private String uiSheetTitle="";
	
	public String getTitle() {
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		QueryEngineeringDocumentsBean documentsBean = (QueryEngineeringDocumentsBean) elContext.getELResolver().getValue(elContext, null, "documentsBean");
		documentsBean = new QueryEngineeringDocumentsBean();
		documentsBean.invoke();
		elContext.getELResolver().setValue(elContext, null, "documentsBean", documentsBean);
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean showButton=true;
	int msDocCounter = 0;
	public int recordsAvailable = 0;
	public List<EngineeringDocuments> qeDocumentsList;
	public Map<String, Id> qeDocumentsIdMap;
	private Properties props;
	private static Logger logger =	 Logger.getLogger("com.wssc.filenet.migration");
		
	public void invoke() {
		props = PropertyReader.getInstance().getPropertyBag();
		executeQueryEngineeringDocuments();
	}
	
public void executeQueryEngineeringDocuments() {
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String engFac = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("EngFac");
		String query = null;
		String appendEndQuery = ") ORDER BY DOCUMENTTITLE";
		
		if(!engFac.equalsIgnoreCase("")) 
				{
			String replaced = engFac.replace(",", "%' OR  DOCUMENTTITLE like'%");			
			query= "SELECT  * FROM EngineeringFacilitiesDrawings WHERE (IsCurrentVersion = TRUE) AND (DOCUMENTTITLE like '%"+replaced+"%'"+appendEndQuery;
				}
		else
		        {
			query = "SELECT * FROM EngineeringFacilitiesDrawings WHERE (IsCurrentVersion = TRUE) "+appendEndQuery;
		        }
		System.out.println("query :"+query);
		recordsAvailable = findQueryEngineeringDocuments(query);
		if(recordsAvailable==0){
			facesContext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR, "No records found for the given query : '"+query+"' ",  "" ));
		}
	}
	

/*public void executeQueryEngineeringDocuments() {
	
	FacesContext facesContext = FacesContext.getCurrentInstance();
	String engFac = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("EngFac");
	String query = null;
	String appendQuery = " ORDER BY DOCUMENTTITLE";
	if(!engFac.equalsIgnoreCase("")) 
			{
//	query = "SELECT DocumentTitle,FACILITYNAME,CONTRACTCODE,ProjectName,SHEET_TITLE,PRIMDISCIPCODE,SECDISCIPCODE,DESIGN_COMP,SHEETNUM,PLANSETNUM,PLANSETTOTAL,ASBUILTDATE,WSSCMODNUM,WSSCMODDATE,CIPESPNUM,SHOPREF,PLANREF,FINAL_REF,OFFSITE,SCADANAME,Comment" +
//			" FROM EngineeringFacilitiesDrawings WHERE (IsCurrentVersion = TRUE) AND DOCUMENTTITLE like '%"+engFac+"%'"+appendQuery;
		query= "SELECT  * FROM EngineeringFacilitiesDrawings WHERE (IsCurrentVersion = TRUE) AND DOCUMENTTITLE like '%"+engFac+"%'"+appendQuery;
			}
	else
	        {
		query = "SELECT * FROM EngineeringFacilitiesDrawings WHERE (IsCurrentVersion = TRUE) "+appendQuery;
	        }
	System.out.println("query :"+query);
	recordsAvailable = findQueryEngineeringDocuments(query);
	if(recordsAvailable==0){
		facesContext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR, "No records found for the given query : '"+query+"' ",  "" ));
	}
}*/
	public int findQueryEngineeringDocuments(String query) {
		showButton=true;
		qeDocumentsList = new ArrayList<EngineeringDocuments>();
		qeDocumentsIdMap = new HashMap<String, Id>();
		logger.log(Level.INFO, "QueryEngineeringDocuments query :"+query);
		ObjectStore os = CEConnection.getInstance().getObjectStore();
		SearchScope search = new SearchScope(os);
		SearchSQL sql = new SearchSQL(query);
		DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
		Iterator docIter = documents.iterator();
		int count=0;
		DateFormat dt = new SimpleDateFormat("MM/dd/yyyy h:mm a");
		while(docIter.hasNext()) {
			count = count+1;
			Document doc = (Document)docIter.next();
			EngineeringDocuments engDoc = new EngineeringDocuments();
			if(doc.get_Id() != null){
				engDoc.setDocId("DOCID"+count);
				qeDocumentsIdMap.put("DOCID"+count, doc.get_Id());
			}
			if(doc.getProperties().getStringValue("DocumentTitle") != null)
				engDoc.setFileName(doc.getProperties().getStringValue("DocumentTitle"));
			
			if(doc.getProperties().getStringValue("FACILITYNAME") != null)
				engDoc.setFacilityName(doc.getProperties().getStringValue("FACILITYNAME"));
			if(doc.getProperties().getStringValue("CONTRACTCODE") != null)
				engDoc.setContractCode(doc.getProperties().getStringValue("CONTRACTCODE"));   
			if(doc.getProperties().getStringValue("ENGPROJECTNAME") != null)
				engDoc.setProjectName(doc.getProperties().getStringValue("ENGPROJECTNAME"));
			if(doc.getProperties().getStringValue("SHEET_TITLE") != null)
				engDoc.setSheetTitle(doc.getProperties().getStringValue("SHEET_TITLE"));
			
			if(doc.getProperties().getStringValue("PRIMDISCIPCODE") != null)
				engDoc.setPrimDiscipCode(doc.getProperties().getStringValue("PRIMDISCIPCODE"));
			
			if(doc.getProperties().getStringValue("SECDISCIPCODE") != null)
				engDoc.setSecDescipCode(doc.getProperties().getStringValue("SECDISCIPCODE"));
			if(doc.getProperties().getStringValue("DESIGN_COMP") != null)
				engDoc.setDesignComp(doc.getProperties().getStringValue("DESIGN_COMP"));
			if(doc.getProperties().getStringValue("SHEETNUM") != null)
				engDoc.setSheetNum(doc.getProperties().getStringValue("SHEETNUM"));
			if(doc.getProperties().getStringValue("PLANSETNUM") != null)
				engDoc.setPlansetNum(doc.getProperties().getStringValue("PLANSETNUM"));
			if(doc.getProperties().getStringValue("PROJECTTYPE") != null)
				engDoc.setProjectType(doc.getProperties().getStringValue("PROJECTTYPE"));
			if(doc.getProperties().getStringValue("PLANSETTOTAL") != null)
				engDoc.setPlansetTotal(doc.getProperties().getStringValue("PLANSETTOTAL"));
			
			if(doc.getProperties().getStringValue("DWGTYPE") != null)
				engDoc.setDwgType(doc.getProperties().getStringValue("DWGTYPE"));
			
			if(doc.getProperties().getDateTimeValue("ASBUILTDATE") != null)
				engDoc.setAsBuiltDate(doc.getProperties().getDateTimeValue("ASBUILTDATE"));
			if(doc.getProperties().getStringValue("WSSCMODNUM") != null)
				engDoc.setWsscModNum(doc.getProperties().getStringValue("WSSCMODNUM"));
			if(doc.getProperties().getDateTimeValue("WSSCMODDATE") != null)
				engDoc.setWsscModDate(doc.getProperties().getDateTimeValue("WSSCMODDATE"));
			if(doc.getProperties().getStringValue("CIPESPNUM") != null)
				engDoc.setCipespNum(doc.getProperties().getStringValue("CIPESPNUM"));
			
			if(doc.getProperties().getStringValue("SHOPREF") != null)
				engDoc.setShopRef(doc.getProperties().getStringValue("SHOPREF"));
			if(doc.getProperties().getStringValue("PLANREF") != null)
				engDoc.setPlanRef(doc.getProperties().getStringValue("PLANREF"));
			if(doc.getProperties().getStringValue("FINAL_REF") != null)
				engDoc.setFinalRef(doc.getProperties().getStringValue("FINAL_REF"));
			if(doc.getProperties().getStringValue("OFFSITE") != null)
				engDoc.setOffSite(doc.getProperties().getStringValue("OFFSITE"));
			
			
			if(doc.getProperties().getStringValue("SCADANAME") != null)
				engDoc.setScadaName(doc.getProperties().getStringValue("SCADANAME"));
			if(doc.getProperties().getStringValue("COMMENTS") != null)
				engDoc.setComments(doc.getProperties().getStringValue("COMMENTS"));
			
			String mimeType = doc.getProperties().getStringValue("MimeType");
			
			if(mimeType.equalsIgnoreCase("application/pdf") ||
					mimeType.equalsIgnoreCase("application/msword") ||
					mimeType.equalsIgnoreCase("application/vnd.ms-excel") ) {
				engDoc.mimeType = false;
				msDocCounter=msDocCounter+1;
			}
				
			qeDocumentsList.add(engDoc);
		}
		if(qeDocumentsList.size() == msDocCounter) {
			showButton=false;
			qeDocumentsList = sortByDescForMsDocsAndPdfs(query, os);
		}
		return qeDocumentsList.size();
	}
	
	public List<EngineeringDocuments> sortByDescForMsDocsAndPdfs(String query, ObjectStore os) {
		qeDocumentsList.clear();
		qeDocumentsIdMap.clear();
		query = query.substring(0,query.indexOf("ORDER BY"))+" ORDER BY DOCUMENTTITLE";
		logger.log(Level.INFO, "QueryEngineeringDocuments PDF query "+query);
		SearchScope search = new SearchScope(os);
		SearchSQL sql = new SearchSQL(query);
		DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
		Iterator docIter = documents.iterator();
		int count = 0;
		DateFormat dt = new SimpleDateFormat("MM/dd/yyyy h:mm a");
		while(docIter.hasNext()) {
			count = count+1;
			Document doc = (Document)docIter.next();
			EngineeringDocuments engDoc = new EngineeringDocuments();
			if(doc.get_Id() != null){
				engDoc.setDocId("DOCID"+count);
				qeDocumentsIdMap.put("DOCID"+count, doc.get_Id());
			}
			if(doc.getProperties().getStringValue("DocumentTitle") != null)
				engDoc.setFileName(doc.getProperties().getStringValue("DocumentTitle"));
			
			if(doc.getProperties().getStringValue("FACILITYNAME") != null)
				engDoc.setFacilityName(doc.getProperties().getStringValue("FACILITYNAME"));
			if(doc.getProperties().getStringValue("CONTRACTCODE") != null)
				engDoc.setContractCode(doc.getProperties().getStringValue("CONTRACTCODE"));   
			if(doc.getProperties().getStringValue("ENGPROJECTNAME") != null)
				engDoc.setProjectName(doc.getProperties().getStringValue("ENGPROJECTNAME"));
			if(doc.getProperties().getStringValue("SHEET_TITLE") != null)
				engDoc.setSheetTitle(doc.getProperties().getStringValue("SHEET_TITLE"));
			
			if(doc.getProperties().getStringValue("PRIMDISCIPCODE") != null)
				engDoc.setPrimDiscipCode(doc.getProperties().getStringValue("PRIMDISCIPCODE"));
			
			if(doc.getProperties().getStringValue("SECDISCIPCODE") != null)
				engDoc.setSecDescipCode(doc.getProperties().getStringValue("SECDISCIPCODE"));
			if(doc.getProperties().getStringValue("DESIGN_COMP") != null)
				engDoc.setDesignComp(doc.getProperties().getStringValue("DESIGN_COMP"));
			if(doc.getProperties().getStringValue("SHEETNUM") != null)
				engDoc.setSheetNum(doc.getProperties().getStringValue("SHEETNUM"));
			if(doc.getProperties().getStringValue("PLANSETNUM") != null)
				engDoc.setPlansetNum(doc.getProperties().getStringValue("PLANSETNUM"));
			if(doc.getProperties().getStringValue("PROJECTTYPE") != null)
				engDoc.setProjectType(doc.getProperties().getStringValue("PROJECTTYPE"));
			if(doc.getProperties().getStringValue("PLANSETTOTAL") != null)
				engDoc.setPlansetTotal(doc.getProperties().getStringValue("PLANSETTOTAL"));
			
			if(doc.getProperties().getStringValue("DWGTYPE") != null)
				engDoc.setDwgType(doc.getProperties().getStringValue("DWGTYPE"));
			
			if(doc.getProperties().getDateTimeValue("ASBUILTDATE") != null)
				engDoc.setAsBuiltDate(doc.getProperties().getDateTimeValue("ASBUILTDATE"));
			if(doc.getProperties().getStringValue("WSSCMODNUM") != null)
				engDoc.setWsscModNum(doc.getProperties().getStringValue("WSSCMODNUM"));
			if(doc.getProperties().getDateTimeValue("WSSCMODDATE") != null)
				engDoc.setWsscModDate(doc.getProperties().getDateTimeValue("WSSCMODDATE"));
			if(doc.getProperties().getStringValue("CIPESPNUM") != null)
				engDoc.setCipespNum(doc.getProperties().getStringValue("CIPESPNUM"));
			
			if(doc.getProperties().getStringValue("SHOPREF") != null)
				engDoc.setShopRef(doc.getProperties().getStringValue("SHOPREF"));
			if(doc.getProperties().getStringValue("PLANREF") != null)
				engDoc.setPlanRef(doc.getProperties().getStringValue("PLANREF"));
			if(doc.getProperties().getStringValue("FINAL_REF") != null)
				engDoc.setFinalRef(doc.getProperties().getStringValue("FINAL_REF"));
			if(doc.getProperties().getStringValue("OFFSITE") != null)
				engDoc.setOffSite(doc.getProperties().getStringValue("OFFSITE"));
			
			
			if(doc.getProperties().getStringValue("SCADANAME") != null)
				engDoc.setScadaName(doc.getProperties().getStringValue("SCADANAME"));
			if(doc.getProperties().getStringValue("COMMENTS") != null)
				engDoc.setComments(doc.getProperties().getStringValue("COMMENTS"));
			qeDocumentsList.add(engDoc);
		}
		return qeDocumentsList;
	}
	
	public String showDocuments() {
		try {	
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ServletContext context = (ServletContext)facesContext.getExternalContext().getContext();
			HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
			DataTable dataTable = (DataTable)facesContext.getViewRoot().findComponent("qedForm:tableEx1");
			int availableRows = dataTable.getRowCount();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			QueryEngineeringDocumentsBean documents = (QueryEngineeringDocumentsBean)elContext.getELResolver().getValue(elContext, null, "documentsBean");
			Map docIdMap = documents.qeDocumentsIdMap; 
			StringBuffer prizmBuffer = new StringBuffer();
			Map imageIdMap = new HashMap();
			logger.log(Level.INFO, "**********QueryEngineeringDocumentsBean Construction PrizmListFile For Query Engineering Documents***********");
			String servletUrl = "http://"+request.getServerName()+":"+request.getServerPort()+facesContext.getExternalContext().getRequestContextPath()+"/GetContentServlet?imageId=";
			prizmBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?> <PrizmViewerListFile ArrangeDockingWindows = \"none\"> <ImageFileList>");
			
			for(int i=0; i<availableRows; i++) {
				dataTable.setRowIndex(i);
				EngineeringDocuments document = (EngineeringDocuments)dataTable.getRowData();
				
				if(document.selected) {
					String docId = document.docId;
					imageIdMap.put(docId, docIdMap.get(docId));
					prizmBuffer.append("<ImageFile FileURL=\""+servletUrl+docId+"\"/>");
				}
			}
			facesContext.getExternalContext().getSessionMap().put("imageIdMap", imageIdMap);
			prizmBuffer.append("</ImageFileList></PrizmViewerListFile>");
			BufferedWriter out = new BufferedWriter(new FileWriter(props.getProperty("facilities.drawings.file")));
			//BufferedWriter out = new BufferedWriter(new FileWriter(context.getRealPath("/")+"\\prizm"+props.getProperty("facilities.drawings.file")));
			logger.log(Level.INFO,"prizmBuffer.toString():"+prizmBuffer.toString());
			out.write(prizmBuffer.toString());
			out.close();
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		
		return "LISTVIEW";
		
	}
	
	public void processEngineeringFacilitiesSearch() 
	{
		System.out.println("==queryEngineeringFacilitiesBean==");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.getExternalContext().getRequestParameterMap();
		System.out.println("in processEngineeringFacilitiesSearch :"+FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap());
//		uidocumentTitle = (String) facesContext.getExternalContext()
//				.getRequestParameterMap().get("facilityDrawingName");
		uiSheetTitle = (String) facesContext.getExternalContext()
				.getRequestParameterMap().get("sheetTitle");
		uiDescipline = (String) facesContext.getExternalContext()
				.getRequestParameterMap().get("desciplineId_input");
		 uiFacilityName = (String) facesContext.getExternalContext()
				.getRequestParameterMap().get("facilityName");
		 uicontractNumber = (String) facesContext.getExternalContext()
				.getRequestParameterMap().get("contractNumber");
		 uiProjectName = (String) facesContext.getExternalContext()
				.getRequestParameterMap().get("projectName");
//		 uiDrawingType = (String) facesContext.getExternalContext()
//				.getRequestParameterMap().get("drawingType");
		System.out.println("::"+"getUiFacilityName() :" + uiFacilityName + "uicontractNumber :" + uicontractNumber + "uiProjectName :" + uiProjectName 
				+ "uiDescipline :" + uiDescipline  + "uiSheetTitle :" + uiSheetTitle);

		String Query = constructDynamicQueryFromAvailableInputs(uiFacilityName,uicontractNumber,uiProjectName, uiSheetTitle,uiDescipline);
		if(((null!=Query)||(""!=Query)))
		{
			recordsAvailable= findQueryEngineeringDocuments(Query);
		}
		
//		if(recordsAvailable==0){
//			facesContext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR, "No records found for the given Search Criteria  : '"+Query+"' ",  "" ));
//		}
		if(recordsAvailable==0){
			facesContext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR, "No records found for the provided Search Criteria, Please provide relevant search criteria.",  "" ));
		}
		else{
			FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "Number of Facilities drawings found are: "+recordsAvailable+". ",  "" ));
		}
		
	}
	
	 public String constructDynamicQueryFromAvailableInputs(String facilityName, String contractNumber, String projectName,  String sheetTitle, String descipline){
		 String constructedQuery = null;
		 System.out.println("==constructedQuery===");
		 StringBuffer sql = new StringBuffer("SELECT * FROM EngineeringFacilitiesDrawings  WHERE ((IsCurrentVersion = TRUE) ");
//		 if(!documentTitle.equalsIgnoreCase("") && documentTitle!=null)
//		 {
//			 sql.append(" AND (DocumentTitle LIKE '%"+documentTitle+"%')");
//		 }
		 
		 if(!facilityName.equalsIgnoreCase("") && facilityName!=null)
		 {
			 sql.append("AND ( FACILITYNAME LIKE '%"+facilityName+"%')");
		 }
		 
		 if(!contractNumber.equalsIgnoreCase("") && contractNumber!=null)
		 {
			 sql.append("AND ( CONTRACTCODE LIKE '%"+contractNumber+"%')");
		 }
		 
		 if(!projectName.equalsIgnoreCase("") && projectName!=null)
		 {
			 sql.append("AND ( ENGPROJECTNAME LIKE '%"+projectName+"%')");
		 }
		 
		 if(!sheetTitle.equalsIgnoreCase("") && sheetTitle!=null)
		 {
			 sql.append("AND ( SHEET_TITLE LIKE '%"+sheetTitle+"%')");
		 }
		 
		 
		 if(!descipline.equalsIgnoreCase("") && descipline!=null)
		 {
			 sql.append("AND ( PRIMDISCIPCODE LIKE '%"+descipline+"%')");
		 }
		 
		 
//		 if(!drawingType.equalsIgnoreCase("") && drawingType!=null)
//		 {
//			 sql.append("AND  ( DWGTYPE  LIKE '%"+drawingType+"%')");
//		 }
		 sql.append(") ORDER BY DOCUMENTTITLE");
		 
		 //query= "SELECT  * FROM EngineeringFacilitiesDrawings WHERE (IsCurrentVersion = TRUE) AND DOCUMENTTITLE like '%"+engFac+"%'"+appendQuery;
//		 constructedQuery= "SELECT * FROM EngineeringFacilitiesDrawings  WHERE ((IsCurrentVersion = TRUE) AND" +
//			 		" (DocumentTitle LIKE '"+documentTitle+"%')	"	+
//						") ";
		 
		 System.out.println("==2constructedQuery==="+sql);
		 constructedQuery = sql.toString();
		 return constructedQuery;
	 }
	 
	public Map<String, Id> getQeDocumentsIdMap() {
		return qeDocumentsIdMap;
	}

	public void setQeDocumentsIdMap(Map<String, Id> qeDocumentsIdMap) {
		this.qeDocumentsIdMap = qeDocumentsIdMap;
	}

	public List<EngineeringDocuments> getQeDocumentsList() {
		return qeDocumentsList;
	}

	public void setQeDocumentsList(List<EngineeringDocuments> qeDocumentsList) {
		this.qeDocumentsList = qeDocumentsList;
	}

	public int getRecordsAvailable() {
		return recordsAvailable;
	}

	public void setRecordsAvailable(int recordsAvailable) {
		this.recordsAvailable = recordsAvailable;
	}
	
	public boolean isShowButton() {
		return showButton;
	}

	public void setShowButton(boolean showButton) {
		this.showButton = showButton;
	}
	public String getUiFacilityName() {
		return uiFacilityName;
	}

	public void setUiFacilityName(String uiFacilityName) {
		this.uiFacilityName = uiFacilityName;
	}

	public String getUicontractNumber() {
		return uicontractNumber;
	}

	public void setUicontractNumber(String uicontractNumber) {
		this.uicontractNumber = uicontractNumber;
	}

	public String getUiProjectName() {
		return uiProjectName;
	}

	public void setUiProjectName(String uiProjectName) {
		this.uiProjectName = uiProjectName;
	}

	public String getUiDrawingType() {
		return uiDrawingType;
	}

	public void setUiDrawingType(String uiDrawingType) {
		this.uiDrawingType = uiDrawingType;
	}
	 public String clearSearchFields(){
		 System.out.println("::clearSearchFields::");
		 setUiFacilityName("");
		 setUicontractNumber("");
		 setUiDrawingType("");
		 setUiProjectName("");
		 setUiDescipline("");
		 setUiSheetTitle("");
		 setRecordsAvailable(0);
		 return "SUCCESS";
	 }
	 
	 public String back(){
			return "BACK";
		}

		public String getUiDescipline() {
			return uiDescipline;
		}

		public void setUiDescipline(String uiDescipline) {
			this.uiDescipline = uiDescipline;
		}

		public String getUiSheetTitle() {
			return uiSheetTitle;
		}

		public void setUiSheetTitle(String uiSheetTitle) {
			this.uiSheetTitle = uiSheetTitle;
		}

		
}
