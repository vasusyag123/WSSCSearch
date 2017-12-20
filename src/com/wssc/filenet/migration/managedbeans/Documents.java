package com.wssc.filenet.migration.managedbeans;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
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
import com.wssc.filenet.migration.dao.ViewScannedDocuments;
import com.wssc.filenet.migration.utils.PropertyReader;


@ManagedBean(name="documents")
@SessionScoped
public class Documents implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public boolean showButton=true;
	int msDocCounter = 0;
	public int recordsAvailable;
	public List<EngineeringDocuments> documentsList;
	public Map<String, Id> objectIdMap;
	public List<ViewScannedDocuments> sheetDrawingsList;
	public Map<String, Id> sheetDrawingsMap;
	private Properties props;
	private EngineeringDocuments[] selectedDocuments;
	private static Logger logger =	 Logger.getLogger("com.wssc.filenet.migration.intranet");
	public String title = "Available Engineering Documents";
	public String getTitle() {
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		Documents documentsBean = (Documents) elContext.getELResolver().getValue(elContext, null, "documents");
		documentsBean = new Documents();
		if(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("ContractNo") != null)
			documentsBean.invoke();
		else if(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("SheetId") != null)
			documentsBean.invokeSheetDrawings();
		elContext.getELResolver().setValue(elContext, null, "documents", documentsBean);
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
		
	public void invoke() {
		props = PropertyReader.getInstance().getPropertyBag();
		recordsAvailable = getDocuments();
		
	}
	
	public void invokeSheetDrawings() {
		props = PropertyReader.getInstance().getPropertyBag();
		recordsAvailable = getSheetDrawings();
		
	}
	
	@SuppressWarnings("unchecked")
	public int getDocuments() {
		DateFormat dt = new SimpleDateFormat("MM/dd/yyyy h:mm a");
		showButton=true;	
		String query = prepareRequest();
		documentsList = new ArrayList<EngineeringDocuments>();
		objectIdMap = new HashMap<String, Id>();
		logger.log(Level.INFO, "OpenSession query "+query);
		ObjectStore os = CEConnection.getInstance().getObjectStore();
		SearchScope search = new SearchScope(os);
		SearchSQL sql = new SearchSQL(query);
		DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
		Iterator docIter = documents.iterator();
		int count=0;
		while(docIter.hasNext()) {
			count++;
			Document doc = (Document)docIter.next();
			EngineeringDocuments engDoc = new EngineeringDocuments();
			if(doc.get_Id() != null){
				engDoc.setDocId("DOCID"+count);
				objectIdMap.put("DOCID"+count, doc.get_Id());
				engDoc.setObjId(doc.get_Id());
			}
			if(doc.getProperties().getStringValue("STORAGECATEGORY") != null)
				engDoc.setCategory(doc.getProperties().getStringValue("STORAGECATEGORY"));
			if(doc.getProperties().getStringValue("SUBCATEGORY") != null)
				engDoc.setSubCategory(doc.getProperties().getStringValue("SUBCATEGORY"));
			if(doc.getProperties().getStringValue("Title") != null)
				engDoc.setTitle(doc.getProperties().getStringValue("Title"));   
			if(doc.getProperties().getStringValue("PAGE") != null)
				engDoc.setPage(doc.getProperties().getStringValue("PAGE"));
			if(doc.getProperties().getStringValue("PAGECOUNT") != null)
				engDoc.setPageCount(doc.getProperties().getStringValue("PAGECOUNT"));
			if(doc.getProperties().getDateTimeValue("DOCUMENTDATE") != null)
				engDoc.setDate(dt.format(doc.getProperties().getDateTimeValue("DOCUMENTDATE")));
			if(doc.getProperties().getStringValue("WATERFINAL") != null)
				engDoc.setFinalWater(doc.getProperties().getStringValue("WATERFINAL"));
			if(doc.getProperties().getStringValue("SEWERFINAL") != null)
				engDoc.setFinalSewer(doc.getProperties().getStringValue("SEWERFINAL"));
			if(doc.getProperties().getStringValue("PLAN") != null)
				engDoc.setPlan(doc.getProperties().getStringValue("PLAN"));
			if(doc.getProperties().getStringValue("OLDWSSCCONTRACT") != null)
				engDoc.setOldWsscContract(doc.getProperties().getStringValue("OLDWSSCCONTRACT"));
			if(doc.getProperties().getStringValue("PROJECTTYPE") != null)
				engDoc.setType(doc.getProperties().getStringValue("PROJECTTYPE"));
			if(doc.getProperties().getStringValue("COMMENT") != null)
				engDoc.setComment(doc.getProperties().getStringValue("COMMENT"));
			
			String mimeType = doc.getProperties().getStringValue("MimeType");
			if(mimeType.equalsIgnoreCase("application/pdf") ||
					mimeType.equalsIgnoreCase("application/msword") ||
					mimeType.equalsIgnoreCase("application/vnd.ms-excel") ) {
				engDoc.mimeType = false;
				msDocCounter=msDocCounter+1;
			}
			documentsList.add(engDoc);
		}
		
		if(documentsList.size() == msDocCounter) {
			showButton=false;
			documentsList = sortByDescForMsDocsAndPdfs(query, os);
		}
		 
		return documentsList.size();
	}
	
	public List<EngineeringDocuments> sortByDescForMsDocsAndPdfs(String query, ObjectStore os) {
		documentsList.clear();
		objectIdMap.clear();
		query = query.substring(0,query.indexOf("ORDER BY"))+" ORDER BY DOCUMENTDATE DESC";
		logger.log(Level.INFO, "OpenSession PDF query "+query);
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
				objectIdMap.put("DOCID"+count, doc.get_Id());
			}
			if(doc.getProperties().getStringValue("STORAGECATEGORY") != null)
				engDoc.setCategory(doc.getProperties().getStringValue("STORAGECATEGORY"));
			if(doc.getProperties().getStringValue("SUBCATEGORY") != null)
				engDoc.setSubCategory(doc.getProperties().getStringValue("SUBCATEGORY"));
			if(doc.getProperties().getStringValue("Title") != null)
				engDoc.setTitle(doc.getProperties().getStringValue("Title"));   
			if(doc.getProperties().getStringValue("PAGE") != null)
				engDoc.setPage(doc.getProperties().getStringValue("PAGE"));
			if(doc.getProperties().getStringValue("PAGECOUNT") != null)
				engDoc.setPageCount(doc.getProperties().getStringValue("PAGECOUNT"));
			if(doc.getProperties().getDateTimeValue("DOCUMENTDATE") != null)
				engDoc.setDate(dt.format(doc.getProperties().getDateTimeValue("DOCUMENTDATE")));
			if(doc.getProperties().getStringValue("WATERFINAL") != null)
				engDoc.setFinalWater(doc.getProperties().getStringValue("WATERFINAL"));
			if(doc.getProperties().getStringValue("SEWERFINAL") != null)
				engDoc.setFinalSewer(doc.getProperties().getStringValue("SEWERFINAL"));
			if(doc.getProperties().getStringValue("PLAN") != null)
				engDoc.setPlan(doc.getProperties().getStringValue("PLAN"));
			if(doc.getProperties().getStringValue("OLDWSSCCONTRACT") != null)
				engDoc.setOldWsscContract(doc.getProperties().getStringValue("OLDWSSCCONTRACT"));
			if(doc.getProperties().getStringValue("PROJECTTYPE") != null)
				engDoc.setType(doc.getProperties().getStringValue("PROJECTTYPE"));
			if(doc.getProperties().getStringValue("COMMENT") != null)
				engDoc.setComment(doc.getProperties().getStringValue("COMMENT"));
			documentsList.add(engDoc);
		}
		return documentsList;
	}
	
	@SuppressWarnings("unchecked")
	public int getSheetDrawings() {
		showButton=true;
		String query = prepareSheetDrawingsRequest();
		sheetDrawingsList = new ArrayList<ViewScannedDocuments>();
		sheetDrawingsMap = new HashMap<String, Id>();
		logger.log(Level.INFO, "Sheet Drawings Query "+query);
		ObjectStore os = CEConnection.getInstance().getObjectStore();
		SearchScope search = new SearchScope(os);
		SearchSQL sql = new SearchSQL(query);
		DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
		Iterator docIter = documents.iterator();
		int count=0;
		while(docIter.hasNext()) {
			count++;
			DateFormat dt = new SimpleDateFormat("MM/dd/yyyy h:mm a");
			Document doc = (Document)docIter.next();
			ViewScannedDocuments engDoc = new ViewScannedDocuments();
			if(doc.get_Id() != null){
				engDoc.setDocId("DOCID"+count);
				sheetDrawingsMap.put("DOCID"+count, doc.get_Id());
				
			}
			if(doc.getProperties().getStringValue("SUBCATEGORY") != null)
				engDoc.setSubCategory(doc.getProperties().getStringValue("SUBCATEGORY"));
			if(doc.getProperties().getStringValue("TITLE") != null)
				engDoc.setTitle(doc.getProperties().getStringValue("TITLE"));
			if(doc.getProperties().getDateTimeValue("DOCUMENTDATE") != null)
				engDoc.setDate(dt.format(doc.getProperties().getDateTimeValue("DOCUMENTDATE")));
			String mimeType = doc.getProperties().getStringValue("MimeType");
			if(mimeType.equalsIgnoreCase("application/pdf") ||
					mimeType.equalsIgnoreCase("application/msword") ||
					mimeType.equalsIgnoreCase("application/vnd.ms-excel") ) {
				engDoc.mimeType = false;
				msDocCounter=msDocCounter+1;
			}
			
			sheetDrawingsList.add(engDoc);
		}
		if(sheetDrawingsList.size() == msDocCounter) {
			showButton=false;
			sheetDrawingsList = sortByDescForMsDocsAndPdfsSheetDrawings(query, os);
		}
		return sheetDrawingsList.size();
	}
	
	public Map<String, Id> getSheetDrawingsMap() {
		return sheetDrawingsMap;
	}

	public void setSheetDrawingsMap(Map<String, Id> sheetDrawingsMap) {
		this.sheetDrawingsMap = sheetDrawingsMap;
	}

	public Map<String, Id> getObjectIdMap() {
		return objectIdMap;
	}

	public List<ViewScannedDocuments> sortByDescForMsDocsAndPdfsSheetDrawings(String query, ObjectStore os) {
		sheetDrawingsList.clear();
		sheetDrawingsMap.clear();
		query = query.substring(0,query.indexOf("ORDER BY"))+" ORDER BY DOCUMENTDATE DESC";
		logger.log(Level.INFO, "Sheet Drawings PDF query "+query);
		SearchScope search = new SearchScope(os);
		SearchSQL sql = new SearchSQL(query);
		DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
		Iterator docIter = documents.iterator();
		int count = 0;
		DateFormat dt = new SimpleDateFormat("MM/dd/yyyy h:mm a");
		while(docIter.hasNext()) {
			count = count+1;
			Document doc = (Document)docIter.next();
			ViewScannedDocuments engDoc = new ViewScannedDocuments();
			if(doc.get_Id() != null){
				engDoc.setDocId("DOCID"+count);
				sheetDrawingsMap.put("DOCID"+count, doc.get_Id());
				
			}
			if(doc.getProperties().getStringValue("SUBCATEGORY") != null)
				engDoc.setSubCategory(doc.getProperties().getStringValue("SUBCATEGORY"));
			if(doc.getProperties().getStringValue("TITLE") != null)
				engDoc.setTitle(doc.getProperties().getStringValue("TITLE"));
			if(doc.getProperties().getDateTimeValue("DOCUMENTDATE") != null)
				engDoc.setDate(dt.format(doc.getProperties().getDateTimeValue("DOCUMENTDATE")));
			sheetDrawingsList.add(engDoc);
		}
		return sheetDrawingsList;
	}
	
	public String showDocuments() {
		try {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ServletContext context = (ServletContext)facesContext.getExternalContext().getContext();
			HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
			DataTable dataTable = (DataTable)facesContext.getViewRoot().findComponent("openSessionForm:openSessionTable");
			int availableRows = dataTable.getRowCount();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			Documents documents = (Documents)elContext.getELResolver().getValue(elContext, null, "documents");
			Map docIdMap = documents.objectIdMap;
			StringBuffer prizmBuffer = new StringBuffer();
			Map imageIdMap = new HashMap();
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
			BufferedWriter out = new BufferedWriter(new FileWriter(props.getProperty("prizmfile")));
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
	
	@SuppressWarnings("unchecked")
	public String showSheetDrawings() {
		try {	
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ServletContext context = (ServletContext)facesContext.getExternalContext().getContext();
			HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
			DataTable dataTable = (DataTable)facesContext.getViewRoot().findComponent("viewSheetForm:tableEx1");
			int availableRows = dataTable.getRowCount();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			Documents documents = (Documents)elContext.getELResolver().getValue(elContext, null, "documents");
			Map docIdMap = documents.sheetDrawingsMap;
			StringBuffer prizmBuffer = new StringBuffer();
			Map imageIdMap = new HashMap();
			String servletUrl = "http://"+request.getServerName()+":"+request.getServerPort()+facesContext.getExternalContext().getRequestContextPath()+"/SheetDrawingsServlet?imageId=";
			prizmBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?> <PrizmViewerListFile ArrangeDockingWindows = \"none\"> <ImageFileList>");
			
			for(int i=0; i<availableRows; i++) {
				dataTable.setRowIndex(i);
				ViewScannedDocuments document = (ViewScannedDocuments)dataTable.getRowData();
				if(document.flag) {
					String docId = document.docId;
					imageIdMap.put(docId, docIdMap.get(docId));
					prizmBuffer.append("<ImageFile FileURL=\""+servletUrl+docId+"\"/>");
				}
			}
			facesContext.getExternalContext().getSessionMap().put("sheetDrawingsMap", imageIdMap);
			prizmBuffer.append("</ImageFileList></PrizmViewerListFile>");
			BufferedWriter out = new BufferedWriter(new FileWriter(props.getProperty("sheet.drawings.file")));
			out.write(prizmBuffer.toString());
			out.close();
			
		} catch (IOException e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		return "SHEETVIEW";
	}

	private String prepareRequest() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String contractNumber = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("ContractNo");
		String wildCardSearch = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("WildCardSearch");
		String query = null;
		String appendQuery = "AND ( (STORAGECATEGORY='Water/Sewer')"+
		" OR (STORAGECATEGORY='Special Cat')" +
		" OR (STORAGECATEGORY='On-Site')" +
		" OR (STORAGECATEGORY='On-Site Level 2')" +
		" OR (STORAGECATEGORY='On-Site Level 3')" +
		" OR (STORAGECATEGORY='Miscellaneous')" +
		//" OR (STORAGECATEGORY='DSGCorrspondence')" +
		" )" +
		" ORDER BY STORAGECATEGORY, SUBCATEGORY, TITLE, PAGE , PAGECOUNT";
		if(wildCardSearch.equalsIgnoreCase("Y")) {
			query = "SELECT * FROM Document WHERE IsCurrentVersion = TRUE AND TITLE LIKE '"+contractNumber+"%' "+appendQuery;				
		}
		else if(wildCardSearch.equalsIgnoreCase("Y_ES")) {
			query = "SELECT * FROM Document WHERE (IsCurrentVersion = TRUE) AND (TITLE LIKE '"+contractNumber+"%') OR (" +
					" (WATERFINAL LIKE '"+contractNumber+"%')"+
					" OR (SEWERFINAL LIKE '"+contractNumber+"%')"+
					" OR (PLAN LIKE '"+contractNumber+"%')"+
					" OR (OLDWSSCCONTRACT LIKE '"+contractNumber+"%')"+
					") "+appendQuery; 
		}
		else if(wildCardSearch.equalsIgnoreCase("N")) {
			query = "SELECT * FROM Document WHERE IsCurrentVersion = TRUE AND TITLE LIKE '"+contractNumber+"' "+appendQuery;	
		}
		else if(wildCardSearch.equalsIgnoreCase("N_ES")) {
			query = "SELECT * FROM Document WHERE (IsCurrentVersion = TRUE) AND (TITLE LIKE '"+contractNumber+"') OR (" +
			" (WATERFINAL LIKE '"+contractNumber+"%')"+
			" OR (SEWERFINAL LIKE '"+contractNumber+"%')"+
			" OR (PLAN LIKE '"+contractNumber+"%')"+
			" OR (OLDWSSCCONTRACT LIKE '"+contractNumber+"%')"+
			") "+appendQuery; 
		}
		
		return (query);
	}
	
	private String prepareSheetDrawingsRequest() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String sheetId = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("SheetId");
		String query = "SELECT * FROM Document WHERE IsCurrentVersion = TRUE AND TITLE = '"+sheetId+"' ORDER BY SUBCATEGORY";				
		return query;
	}
	
	public String getHexString(byte[] b) throws Exception {
		  String result = "";
		  for (int i=0; i < b.length; i++) {
		    result +=
		          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		  }
		  return result;
	}

	public int getRecordsAvailable() {
		return recordsAvailable;
	}

	public void setRecordsAvailable(int recordsAvailable) {
		this.recordsAvailable = recordsAvailable;
	}
	
		public List<EngineeringDocuments> getDocumentsList() {
		return documentsList;
	}

	public void setDocumentsList(List<EngineeringDocuments> documentsList) {
		this.documentsList = documentsList;
	}

	public List<ViewScannedDocuments> getSheetDrawingsList() {
		return sheetDrawingsList;
	}

	public void setSheetDrawingsList(List<ViewScannedDocuments> sheetDrawingsList) {
		this.sheetDrawingsList = sheetDrawingsList;
	}

	public void setObjectIdMap(Map<String, Id> objectIdMap) {
		this.objectIdMap = objectIdMap;
	}

		public boolean isShowButton() {
		return showButton;
	}

	public void setShowButton(boolean showButton) {
		this.showButton = showButton;
	}

	public EngineeringDocuments[] getSelectedDocuments() {
		return selectedDocuments;
	}

	public void setSelectedDocuments(EngineeringDocuments[] selectedDocuments) {
		this.selectedDocuments = selectedDocuments;
	}

	
}
