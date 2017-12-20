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
import com.wssc.filenet.migration.utils.PropertyReader;

import com.wssc.filenet.migration.dao.EngineeringDocuments;
import com.wssc.filenet.migration.dao.PlumbingCardsDocuments;



@ManagedBean(name="pcBean")
@SessionScoped
public class QueryPlumbingCardsBean {
	public String title = "Plumbing Cards";
	public int recordsAvailable = 0;
	
	public List<PlumbingCardsDocuments> pcDocumentsList;
	boolean showButton = false;
	public Map<String, Id> pcDocumentsIdMap;
	private Properties props;
	private static Logger logger =	 Logger.getLogger("com.wssc.filenet.migration");
	int msDocCounter = 0;
	
	public void invoke() {
		props = PropertyReader.getInstance().getPropertyBag();
		fetchPlumbingVars();
	}
	public String getTitle() {
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		QueryPlumbingCardsBean documentsBean = (QueryPlumbingCardsBean) elContext.getELResolver().getValue(elContext, null, "pcBean");
		documentsBean = new QueryPlumbingCardsBean();
		documentsBean.invoke();
		elContext.getELResolver().setValue(elContext, null, "pcBean", documentsBean);
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	
public void fetchPlumbingVars() {
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String houseNumber =""+ (String)facesContext.getExternalContext().getRequestParameterMap().get("HNo");
		String streetName =""+ (String)facesContext.getExternalContext().getRequestParameterMap().get("SName");
		String query = "SELECT  * FROM PlumbingCards WHERE (IsCurrentVersion = TRUE)";
		String appendQuery = ") ORDER BY DOCUMENTTITLE";
		
		if(! houseNumber.equalsIgnoreCase("") && houseNumber!=null) 
				{
			query=query+ " AND ( HouseNo like '%"+houseNumber+"%'";
				}
		if(!streetName.equalsIgnoreCase("") && streetName!=null ) 
		        {
			query = query+ " AND StreetName like  '%"+streetName+"%'";
		        }
		query = query + appendQuery;
		
		logger.log(Level.INFO,"query :"+query);		
		
	
recordsAvailable = queryPlumbingCards(query);
if(recordsAvailable==0){
	facesContext.addMessage(null,new FacesMessage(FacesMessage.SEVERITY_ERROR, "No records found for the given query : '"+query+"' ",  "" ));
}
}

public int queryPlumbingCards(String query) {
	pcDocumentsList = new ArrayList<PlumbingCardsDocuments>();
	showButton = true;
	pcDocumentsIdMap = new HashMap<String, Id>();
	logger.log(Level.INFO, "Query PC Documents query :"+query);
	ObjectStore os = CEConnection.getInstance().getObjectStore();
	SearchScope search = new SearchScope(os);
	SearchSQL sql = new SearchSQL(query);
	DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
	Iterator docIter = documents.iterator();
	int count=0;
	logger.log(Level.INFO,"count :"+count);
	while(docIter.hasNext()) {
		count = count+1;
		Document doc = (Document)docIter.next();
		PlumbingCardsDocuments pcDocs = new PlumbingCardsDocuments();
		if(doc.get_Id() != null){
			pcDocs.setDocId("DOCID"+count);
			pcDocumentsIdMap.put("DOCID"+count, doc.get_Id());
		}
		
		//DocumentTitle -PERMITNO - House No -Street Name - ContractNo - PAGE NO - COUNTYCD- Comment
		
		if(doc.getProperties().getStringValue("DocumentTitle") != null)
			pcDocs.setDocumentTitle(doc.getProperties().getStringValue("DocumentTitle"));
		if(doc.getProperties().getStringValue("PERMITNO") != null)
			pcDocs.setpPermitNo(doc.getProperties().getStringValue("PERMITNO"));
		if(doc.getProperties().getStringValue("HouseNo") != null)
			pcDocs.setpHouseNumber(doc.getProperties().getStringValue("HouseNo"));   
		if(doc.getProperties().getStringValue("StreetName") != null)
			pcDocs.setpStreetName(doc.getProperties().getStringValue("StreetName"));
		if(doc.getProperties().getStringValue("PAGENO") != null)
			pcDocs.setpPgNo(doc.getProperties().getStringValue("PAGENO"));
		if(doc.getProperties().getStringValue("COUNTYCD") != null)
			pcDocs.setpCountyCd(doc.getProperties().getStringValue("COUNTYCD"));
		if(doc.getProperties().getStringValue("ContractNo") != null)
			pcDocs.setpCountyCd(doc.getProperties().getStringValue("ContractNo"));
		if(doc.getProperties().getStringValue("Comment") != null)
			pcDocs.setpComment(doc.getProperties().getStringValue("Comment"));
		
		String mimeType = doc.getProperties().getStringValue("MimeType");
		
		if(mimeType.equalsIgnoreCase("application/pdf") ||
				mimeType.equalsIgnoreCase("application/msword") ||
				mimeType.equalsIgnoreCase("application/vnd.ms-excel") ) {
			pcDocs.mimeType = false;
			msDocCounter=msDocCounter+1;
		}
			
		pcDocumentsList.add(pcDocs);
	}
	if(pcDocumentsList.size() == msDocCounter) {
		showButton=false;
		pcDocumentsList = sortByDescForMsDocsAndPdfs(query, os);
	}
	logger.log(Level.INFO,"pcDocumentsList.size :"+pcDocumentsList.size());
	return pcDocumentsList.size();
}

public List<PlumbingCardsDocuments> sortByDescForMsDocsAndPdfs(String query, ObjectStore os) {
	pcDocumentsList.clear();
	pcDocumentsIdMap.clear();
	query = query.substring(0,query.indexOf("ORDER BY"))+" ORDER BY DOCUMENTTITLE";
	logger.log(Level.INFO, "QueryEngineeringDocuments PDF query "+query);
	SearchScope search = new SearchScope(os);
	SearchSQL sql = new SearchSQL(query);
	DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
	Iterator docIter = documents.iterator();
	int count = 0;
	
	while(docIter.hasNext()) {
		count = count+1;
		Document doc = (Document)docIter.next();
		PlumbingCardsDocuments pcDoc = new PlumbingCardsDocuments();
		if(doc.get_Id() != null){
			pcDoc.setDocId("DOCID"+count);
			pcDocumentsIdMap.put("DOCID"+count, doc.get_Id());
		}
		
		
		if(doc.getProperties().getStringValue("DocumentTitle") != null)
			pcDoc.setDocumentTitle(doc.getProperties().getStringValue("DocumentTitle"));
		if(doc.getProperties().getStringValue("PERMITNO") != null)
			pcDoc.setpPermitNo(doc.getProperties().getStringValue("PERMITNO"));
		if(doc.getProperties().getStringValue("HouseNo") != null)
			pcDoc.setpHouseNumber(doc.getProperties().getStringValue("HouseNo"));   
		if(doc.getProperties().getStringValue("StreetName") != null)
			pcDoc.setpStreetName(doc.getProperties().getStringValue("StreetName"));
		if(doc.getProperties().getStringValue("PAGENO") != null)
			pcDoc.setpPgNo(doc.getProperties().getStringValue("PAGENO"));
		if(doc.getProperties().getStringValue("COUNTYCD") != null)
			pcDoc.setpCountyCd(doc.getProperties().getStringValue("COUNTYCD"));
		if(doc.getProperties().getStringValue("ContractNo") != null)
			pcDoc.setpCountyCd(doc.getProperties().getStringValue("ContractNo"));
		if(doc.getProperties().getStringValue("Comment") != null)
			pcDoc.setpComment(doc.getProperties().getStringValue("Comment"));
		
		String mimeType = doc.getProperties().getStringValue("MimeType");
		pcDocumentsList.add(pcDoc);
	}
	return pcDocumentsList;
}

public String showDocuments(){
	

	try {	
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ServletContext context = (ServletContext)facesContext.getExternalContext().getContext();
		HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
		DataTable dataTable = (DataTable)facesContext.getViewRoot().findComponent("pcForm:tableEx1");
		int availableRows = dataTable.getRowCount();
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		QueryPlumbingCardsBean documents = (QueryPlumbingCardsBean)elContext.getELResolver().getValue(elContext, null, "pcBean");
		Map docIdMap = documents.pcDocumentsIdMap; 
		StringBuffer prizmBuffer = new StringBuffer();
		Map imageIdMap = new HashMap();
		logger.log(Level.INFO, "**********Query Plumbing Cards DocumentsBean Construction PrizmListFile For Query Plumbing Cards***********");
		
		String servletUrl = "http://"+request.getServerName()+":"+request.getServerPort()+facesContext.getExternalContext().getRequestContextPath()+"/GetContentServlet?imageId=";
		prizmBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?> <PrizmViewerListFile ArrangeDockingWindows = \"none\"> <ImageFileList>");
		
		for(int i=0; i<availableRows; i++) {
			dataTable.setRowIndex(i);
			PlumbingCardsDocuments document = (PlumbingCardsDocuments)dataTable.getRowData();
			
			if(document.selected) {
				String docId = document.docId;
				imageIdMap.put(docId, docIdMap.get(docId));
				prizmBuffer.append("<ImageFile FileURL=\""+servletUrl+docId+"\"/>");
			}
		}
		facesContext.getExternalContext().getSessionMap().put("imageIdMap", imageIdMap);
		prizmBuffer.append("</ImageFileList></PrizmViewerListFile>");
		BufferedWriter out = new BufferedWriter(new FileWriter(props.getProperty("pc.drawings.file")));
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
public int getRecordsAvailable() {
	return recordsAvailable;
}
public void setRecordsAvailable(int recordsAvailable) {
	this.recordsAvailable = recordsAvailable;
}
public List<PlumbingCardsDocuments> getPcDocumentsList() {
	return pcDocumentsList;
}
public void setPcDocumentsList(List<PlumbingCardsDocuments> pcDocumentsList) {
	this.pcDocumentsList = pcDocumentsList;
}
public Map<String, Id> getPcDocumentsIdMap() {
	return pcDocumentsIdMap;
}
public void setPcDocumentsIdMap(Map<String, Id> pcDocumentsIdMap) {
	this.pcDocumentsIdMap = pcDocumentsIdMap;
}
public boolean isShowButton() {
	return showButton;
}

public void setShowButton(boolean showButton) {
	this.showButton = showButton;
}


}