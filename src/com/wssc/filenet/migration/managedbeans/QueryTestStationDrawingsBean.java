package com.wssc.filenet.migration.managedbeans;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.wssc.filenet.migration.dao.TestStationsDocuments;
import com.wssc.filenet.migration.utils.PropertyReader;
/*
 * http://10.215.40.94:9081/Drawings/facilitiesQuery.faces?EngFac=ACCKBE3452A02-M01
 * */
@ManagedBean(name="testStationBean")
@SessionScoped
public class QueryTestStationDrawingsBean {
	public String title = "Available Test Station Documents";
	private String uiTestStationName="";
	private String uicontractNumber="";
	private String uiWSSCContractNo="";
	private String uiTSNumbe="";
	private String ui200FeetSheet="";
	private String uiLocality="";
	private String mimeType="";
	
	public String getTitle() {
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		QueryTestStationDrawingsBean testStationBean = (QueryTestStationDrawingsBean) elContext.getELResolver().getValue(elContext, null, "testStationBean");
		testStationBean = new QueryTestStationDrawingsBean();
		testStationBean.invoke();
		elContext.getELResolver().setValue(elContext, null, "testStationBean", testStationBean);
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean showButton=true;
	int msDocCounter = 0;
	public int recordsAvailable = 0;
	public List<TestStationsDocuments> tsDocumentsList;
	public Map<String, Id> qeDocumentsIdMap;
	private Properties props;
	private static Logger logger =	 Logger.getLogger("com.wssc.filenet.migration");
		
	public void invoke() {
		props = PropertyReader.getInstance().getPropertyBag();
		executeQueryEngineeringDocuments();
	}
	
public void executeQueryEngineeringDocuments() {
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String testStation = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("testStation");
		String query = null;
		String appendQuery = " ORDER BY DOCUMENTTITLE";
		if(!testStation.equalsIgnoreCase("")) 
				{

			query= "SELECT  * FROM TestStation WHERE (IsCurrentVersion = TRUE) AND DOCUMENTTITLE like '%"+testStation+"%'"+appendQuery;
				}
		else
		        {
			query = "SELECT * FROM testStation WHERE (IsCurrentVersion = TRUE) "+appendQuery;
		        }
		System.out.println("query :"+query);
		recordsAvailable = findQueryEngineeringDocuments(query);
		if(recordsAvailable==0){
			facesContext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR, "No records found for the given query : '"+query+"' ",  "" ));
		}
	}
	
	public int findQueryEngineeringDocuments(String query) {
		showButton=true;
		tsDocumentsList = new ArrayList<TestStationsDocuments>();
		qeDocumentsIdMap = new HashMap<String, Id>();
		logger.log(Level.INFO, "QueryEngineeringDocuments query "+query);
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
			TestStationsDocuments tsDoc = new TestStationsDocuments();
			if(doc.get_Id() != null){
				tsDoc.setDocId("DOCID"+count);
				qeDocumentsIdMap.put("DOCID"+count, doc.get_Id());
			}
			String mimeType = doc.getProperties().getStringValue("MimeType");
			if(doc.getProperties().getStringValue("DocumentTitle") != null)
				tsDoc.setTtestStationName(doc.getProperties().getStringValue("DocumentTitle"));
			
			if(doc.getProperties().getStringValue("Contract") != null)
				tsDoc.setTcontractNumber(doc.getProperties().getStringValue("Contract"));
			if(doc.getProperties().getStringValue("WSSCContractNumber") != null)
				tsDoc.settWSSCContractNo(doc.getProperties().getStringValue("WSSCContractNumber"));   
			if(doc.getProperties().getStringValue("TSNumber") != null)
				tsDoc.settTsNo(doc.getProperties().getStringValue("TSNumber"));
			if(doc.getProperties().getStringValue("TwoHundredFTSheet") != null)
				tsDoc.setT200FTSheet(doc.getProperties().getStringValue("TwoHundredFTSheet"));
			
			if(doc.getProperties().getStringValue("Locality") != null)
				tsDoc.settLocality(doc.getProperties().getStringValue("Locality"));
			
			if(doc.getProperties().getStringValue("Anodes") != null)
				tsDoc.settAnodes(doc.getProperties().getStringValue("Anodes"));
			if(doc.getProperties().getStringValue("CathProtect") != null)
				tsDoc.setTcathProtect(doc.getProperties().getStringValue("CathProtect"));	
		
			if(doc.getProperties().getStringValue("Comment") != null)
				tsDoc.setComment(doc.getProperties().getStringValue("Comment"));
			
			
			
			if(mimeType.equalsIgnoreCase("application/pdf") ||
					mimeType.equalsIgnoreCase("application/msword") ||
					mimeType.equalsIgnoreCase("application/vnd.ms-excel") ) {
				tsDoc.mimeType = false;
				msDocCounter=msDocCounter+1;
			}
				
			tsDocumentsList.add(tsDoc);
		}
		if(tsDocumentsList.size() == msDocCounter) {
			showButton=false;
			tsDocumentsList = sortByDescForMsDocsAndPdfs(query, os);
		}
		return tsDocumentsList.size();
	}
	
	public List<TestStationsDocuments> sortByDescForMsDocsAndPdfs(String query, ObjectStore os) {
		tsDocumentsList.clear();
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
			TestStationsDocuments tsDoc = new TestStationsDocuments();
			if(doc.get_Id() != null){
				tsDoc.setDocId("DOCID"+count);
				qeDocumentsIdMap.put("DOCID"+count, doc.get_Id());
			}
			if(doc.getProperties().getStringValue("DocumentTitle") != null)
				tsDoc.setTtestStationName(doc.getProperties().getStringValue("DocumentTitle"));
			
			if(doc.getProperties().getStringValue("Contract") != null)
				tsDoc.setTcontractNumber(doc.getProperties().getStringValue("Contract"));
			if(doc.getProperties().getStringValue("WSSCContractNumber") != null)
				tsDoc.settWSSCContractNo(doc.getProperties().getStringValue("WSSCContractNumber"));   
			if(doc.getProperties().getStringValue("TSNumber") != null)
				tsDoc.settTsNo(doc.getProperties().getStringValue("TSNumber"));
			if(doc.getProperties().getStringValue("TwoHundredFTSheet") != null)
				tsDoc.setT200FTSheet(doc.getProperties().getStringValue("TwoHundredFTSheet"));
			
			if(doc.getProperties().getStringValue("Locality") != null)
				tsDoc.settLocality(doc.getProperties().getStringValue("Locality"));
			
			if(doc.getProperties().getStringValue("Anodes") != null)
				tsDoc.settAnodes(doc.getProperties().getStringValue("Anodes"));
			if(doc.getProperties().getStringValue("CathProtect") != null)
				tsDoc.setTcathProtect(doc.getProperties().getStringValue("CathProtect"));	
		
			if(doc.getProperties().getStringValue("Comment") != null)
				tsDoc.setComment(doc.getProperties().getStringValue("Comment"));
			tsDocumentsList.add(tsDoc);
		}
		return tsDocumentsList;
	}
	
	public String showDocuments() {
		try {	
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ServletContext context = (ServletContext)facesContext.getExternalContext().getContext();
			HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
			DataTable dataTable = (DataTable)facesContext.getViewRoot().findComponent("qedForm:tableEx1");
			int availableRows = dataTable.getRowCount();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			QueryTestStationDrawingsBean documents = (QueryTestStationDrawingsBean)elContext.getELResolver().getValue(elContext, null, "testStationBean");
			Map docIdMap = documents.qeDocumentsIdMap; 
			StringBuffer prizmBuffer = new StringBuffer();
			Map imageIdMap = new HashMap();
			logger.log(Level.INFO, "**********QueryTestStationDrawingsBean Construction PrizmListFile For Query Engineering Documents***********");
			String servletUrl = "http://"+request.getServerName()+":"+request.getServerPort()+facesContext.getExternalContext().getRequestContextPath()+"/GetContentServlet?imageId=";
			prizmBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?> <PrizmViewerListFile ArrangeDockingWindows = \"none\"> <ImageFileList>");
			
			for(int i=0; i<availableRows; i++) {
				dataTable.setRowIndex(i);
				TestStationsDocuments document = (TestStationsDocuments)dataTable.getRowData();
				
				if(document.selected) {
					String docId = document.docId;
					imageIdMap.put(docId, docIdMap.get(docId));
					prizmBuffer.append("<ImageFile FileURL=\""+servletUrl+docId+"\"/>");
				}
			}
			facesContext.getExternalContext().getSessionMap().put("imageIdMap", imageIdMap);
			prizmBuffer.append("</ImageFileList></PrizmViewerListFile>");
			BufferedWriter out = new BufferedWriter(new FileWriter(context.getRealPath("/")+"\\prizm"+props.getProperty("queryEngrDocsPrizmFile")));
			//BufferedWriter out = new BufferedWriter(new FileWriter(props.getProperty("queryEngrDocsPrizmFile")));
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
	
	public void processTestStationsSearch() 
	{
		System.out.println("==queryTestStationsBean==");
		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.getExternalContext().getRequestParameterMap();
		System.out.println("in processTestStationsSearch :"+FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap());
		uiTestStationName = (String) facesContext.getExternalContext()
				.getRequestParameterMap().get("testStationNames");
		 uicontractNumber = (String) facesContext.getExternalContext()
				.getRequestParameterMap().get("contractNo");
//		 uiWSSCContractNo = (String) facesContext.getExternalContext()
//				.getRequestParameterMap().get("WSSCContractNo");
		 uiTSNumbe = (String) facesContext.getExternalContext()
				.getRequestParameterMap().get("TSNumbe");
		 ui200FeetSheet = (String) facesContext.getExternalContext()
					.getRequestParameterMap().get("UI200FeetSheet");
		 uiLocality = (String) facesContext.getExternalContext()
							.getRequestParameterMap().get("UILocality");
		System.out.println("uiTestStationName() : "+uiTestStationName+"::"+"uicontractNumber() :" + uicontractNumber  + "uiTSNumber :" + uiTSNumbe + "uiDrawingType :" + ui200FeetSheet+"uiLocality :"+uiLocality);

		String Query = constructDynamicQueryFromAvailableInputs(uiTestStationName,uicontractNumber, uiTSNumbe,ui200FeetSheet,uiLocality);
		if(((null!=Query)||(""!=Query)))
		{
			recordsAvailable= findQueryEngineeringDocuments(Query);
		}
		
		if(recordsAvailable==0){
			facesContext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR, "No records found for the given Search Criteria, , Please provide relevant search criteria.",  "" ));
		}
		else{
			FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(FacesMessage.SEVERITY_INFO, "Number of Test Stations found are: '"+recordsAvailable+"' ",  "" ));
		}
		
	}
	
	 public String constructDynamicQueryFromAvailableInputs(String documentTitle,String contractNumber, String TSNumber,String UI200FeetSheet, String Locality){
		 String constructedQuery = null;
		 System.out.println("==constructedQuery===");
		 StringBuffer sql = new StringBuffer("SELECT * FROM TestStation  WHERE ((IsCurrentVersion = TRUE) ");
		 if(!documentTitle.equalsIgnoreCase("") && documentTitle!=null)
		 {
			 sql.append(" AND (DocumentTitle LIKE '%"+documentTitle+"%')");
		 }
		 
		 if(!contractNumber.equalsIgnoreCase("") && contractNumber!=null)
		 {
			 sql.append("AND ( Contract LIKE '%"+contractNumber+"%')");
		 }
		 
//		 if(!WSSCContractNo.equalsIgnoreCase("") && WSSCContractNo!=null)
//		 {
//			 sql.append("AND ( WSSCContractNumber LIKE '%"+contractNumber+"%')");
//		 }
		 
		 if(!TSNumber.equalsIgnoreCase("") && TSNumber!=null)
		 {
			 sql.append("AND ( TSNumber LIKE '%"+TSNumber+"%')");
		 }
		 
		 if(!UI200FeetSheet.equalsIgnoreCase("") && UI200FeetSheet!=null)
		 {
			 sql.append("AND ( TwoHundredFTSheet  LIKE '%"+UI200FeetSheet+"%')");
		 }
		 if(!Locality.equalsIgnoreCase("") && Locality!=null)
		 {
			 sql.append("AND ( Locality  LIKE '%"+Locality+"%')");
		 }
		
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

	public List<TestStationsDocuments> getTsDocumentsList() {
		return tsDocumentsList;
	}

	public void setTsDocumentsList(List<TestStationsDocuments> tsDocumentsList) {
		this.tsDocumentsList = tsDocumentsList;
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
	
	public String getUiTestStationName() {
		return uiTestStationName;
	}

	public void setUiTestStationName(String uiTestStationName) {
		this.uiTestStationName = uiTestStationName;
	}

	public String getUicontractNumber() {
		return uicontractNumber;
	}

	public void setUicontractNumber(String uicontractNumber) {
		this.uicontractNumber = uicontractNumber;
	}

	public String getUiWSSCContractNo() {
		return uiWSSCContractNo;
	}

	public void setUiWSSCContractNo(String uiWSSCContractNo) {
		this.uiWSSCContractNo = uiWSSCContractNo;
	}

	public String getUiTSNumbe() {
		return uiTSNumbe;
	}

	public void setUiTSNumbe(String uiTSNumbe) {
		this.uiTSNumbe = uiTSNumbe;
	}

	public String getUi200FeetSheet() {
		return ui200FeetSheet;
	}

	public void setUi200FeetSheet(String ui200FeetSheet) {
		this.ui200FeetSheet = ui200FeetSheet;
	}

	public String getUiLocality() {
		return uiLocality;
	}

	public void setUiLocality(String uiLocality) {
		this.uiLocality = uiLocality;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public int getMsDocCounter() {
		return msDocCounter;
	}

	public void setMsDocCounter(int msDocCounter) {
		this.msDocCounter = msDocCounter;
	}

	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		QueryTestStationDrawingsBean.logger = logger;
	}

	 public String clearSearchFields(){
		 tsDocumentsList.clear();
		 System.out.println("::clearSearchFields::");
		 setUiTestStationName("");
		 setUicontractNumber("");
		 setUiWSSCContractNo("");
		 setUi200FeetSheet("");
		 setUiLocality("");
		 setUiTSNumbe("");
		 setRecordsAvailable(0);
		 return "SUCCESS";
	 }
	 
	 public String back() {
			return "BACK";
		}

}
