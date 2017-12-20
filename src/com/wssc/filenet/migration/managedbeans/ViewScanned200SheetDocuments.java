package com.wssc.filenet.migration.managedbeans;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
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
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
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
import com.wssc.filenet.migration.dao.GetSheetIds;
import com.wssc.filenet.migration.dao.GetSheetIdsRow;
import com.wssc.filenet.migration.dao.ViewScannedDocuments;
import com.wssc.filenet.migration.utils.PropertyReader;

@ManagedBean(name="scanned200SheetDocuments")
@SessionScoped
public class ViewScanned200SheetDocuments {
	public boolean showButton=true;
	int msDocCounter = 0;
	public int recordsAvailable;
	public List<ViewScannedDocuments> viewScannedSheetDocumentsList;
	public Map<String, Id> viewScannedDocumentsIdMap;
	private Properties props;
	private static Logger logger =	 Logger.getLogger("com.wssc.filenet.migration");
	public String title = "Available Engineering Documents";
	public String getTitle() {
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		ViewScanned200SheetDocuments documentsBean = (ViewScanned200SheetDocuments) elContext.getELResolver().getValue(elContext, null, "scanned200SheetDocuments");
		documentsBean = new ViewScanned200SheetDocuments();
		documentsBean.invoke();
		elContext.getELResolver().setValue(elContext, null, "scanned200SheetDocuments", documentsBean);
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
		
	public void invoke() {
		props = PropertyReader.getInstance().getPropertyBag();
		GetSheetIdsRow[] sheetIds = checkSheetIds();
		if(sheetIds != null && sheetIds.length > 0) {
			findViewScanned200SheetDocuments(sheetIds);
		}
		else {
			recordsAvailable = 0;
		}
	}
	
	public void findViewScanned200SheetDocuments(GetSheetIdsRow[] sheetIds) {
		showButton=true;
		String query = prepareViewScanned200SheetRequest(sheetIds);
		viewScannedSheetDocumentsList = new ArrayList<ViewScannedDocuments>();
		viewScannedDocumentsIdMap = new HashMap<String, Id>();
		logger.log(Level.INFO, "ViewScanned200SheetDocuments query "+query);
		ObjectStore os = CEConnection.getInstance().getObjectStore();
		SearchScope search = new SearchScope(os);
		SearchSQL sql = new SearchSQL(query);
		DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
		if(documents.isEmpty())
			recordsAvailable = 0;
		else {
			Iterator docIter = documents.iterator();
			int count=0;
			DateFormat dt = new SimpleDateFormat("MM/dd/yyyy h:mm a");
			while(docIter.hasNext()) {
				recordsAvailable = count++;
				Document doc = (Document)docIter.next();
				ViewScannedDocuments engDoc = new ViewScannedDocuments();
				if(doc.get_Id() != null){
					engDoc.setDocId("DOCID"+count);
					viewScannedDocumentsIdMap.put("DOCID"+count, doc.get_Id());
					
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
				viewScannedSheetDocumentsList.add(engDoc);
			}
		}
		
		if(viewScannedSheetDocumentsList.size() == msDocCounter) {
			showButton=false;
			viewScannedSheetDocumentsList = sortByDescForMsDocsAndPdfs(query, os);
		}
	}
	
	public List<ViewScannedDocuments> sortByDescForMsDocsAndPdfs(String query, ObjectStore os) {
		viewScannedSheetDocumentsList.clear();
		viewScannedDocumentsIdMap.clear();
		query = query.substring(0,query.indexOf("ORDER BY"))+" ORDER BY DOCUMENTDATE DESC";
		logger.log(Level.INFO, "ViewScanned200SheetDocuments PDF query "+query);
		SearchScope search = new SearchScope(os);
		SearchSQL sql = new SearchSQL(query);
		DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
		if(documents.isEmpty())
			recordsAvailable = 0;
		else {
			Iterator docIter = documents.iterator();
			int count=0;
			DateFormat dt = new SimpleDateFormat("MM/dd/yyyy h:mm a");
			while(docIter.hasNext()) {
				recordsAvailable = count++;
				Document doc = (Document)docIter.next();
				ViewScannedDocuments engDoc = new ViewScannedDocuments();
				if(doc.get_Id() != null){
					engDoc.setDocId("DOCID"+count);
					viewScannedDocumentsIdMap.put("DOCID"+count, doc.get_Id());
					
				}
				if(doc.getProperties().getStringValue("SUBCATEGORY") != null)
					engDoc.setSubCategory(doc.getProperties().getStringValue("SUBCATEGORY"));
				if(doc.getProperties().getStringValue("TITLE") != null)
					engDoc.setTitle(doc.getProperties().getStringValue("TITLE"));
				if(doc.getProperties().getDateTimeValue("DOCUMENTDATE") != null)
					engDoc.setDate(dt.format(doc.getProperties().getDateTimeValue("DOCUMENTDATE")));
				
				viewScannedSheetDocumentsList.add(engDoc);
			}
		}
		return viewScannedSheetDocumentsList;
	}
	
	public String showDocuments() {
		try {	
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ServletContext context = (ServletContext)facesContext.getExternalContext().getContext();
			HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
			DataTable dataTable = (DataTable)facesContext.getViewRoot().findComponent("viewScannedForm:tableEx1");
			int availableRows = dataTable.getRowCount();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
	        ViewScanned200SheetDocuments documents = (ViewScanned200SheetDocuments)elContext.getELResolver().getValue(elContext, null, "scanned200SheetDocuments");
			Map docIdMap = documents.viewScannedDocumentsIdMap;
			StringBuffer prizmBuffer = new StringBuffer();
			Map imageIdMap = new HashMap();
			logger.log(Level.INFO, "**********Construction PrizmListFile For 200 Scale Reference Sheet Documents***********");
			String servletUrl = "http://"+request.getServerName()+":"+request.getServerPort()+facesContext.getExternalContext().getRequestContextPath()+"/ViewScanned200SheetServlet?imageId=";
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
			facesContext.getExternalContext().getSessionMap().put("viewScannedImageIdMap", imageIdMap);
			prizmBuffer.append("</ImageFileList></PrizmViewerListFile>");
			BufferedWriter out = new BufferedWriter(new FileWriter(props.getProperty("viewscanfile")));
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
	
	public String prepareViewScanned200SheetRequest(GetSheetIdsRow[] sheetIds) {
		StringBuffer query = new StringBuffer();
		String subCategory = null;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String docType = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("DOCTYPE");
		
		try {
			if(docType.equalsIgnoreCase("W"))
				subCategory = "Water";
			else if(docType.equalsIgnoreCase("S"))
				subCategory = "Sewer";
			else if(docType.equalsIgnoreCase("C"))
				subCategory = "Historical Permit";
			else if(docType.equalsIgnoreCase("P"))
				subCategory = "Historical Plat";
			recordsAvailable = sheetIds.length;
			
			query.append("SELECT ID, SUBCATEGORY, TITLE, DOCUMENTDATE, MIMETYPE FROM ScaleReferenceSheets WHERE (IsCurrentVersion = TRUE) AND " +
						"(STORAGECATEGORY='200'' Scale Reference Sheets') AND " +
						"(SUBCATEGORY='"+subCategory+"') AND " +
						"(TITLE IN (");
				for(int i=0; i<sheetIds.length; i++) {
					query.append("'"+sheetIds[i].getGRDWSSC91O_SHEET_ID().toString()+"'");
					if( i != (sheetIds.length-1))
						query.append(",");
				}
				query.append(")) ORDER BY TITLE");
		} catch (SQLException e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		return query.toString();
	}
	
	private GetSheetIdsRow[] checkSheetIds() {
		
		GetSheetIdsRow[] sheetIds = null;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String minX = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("MINX");
		String minY = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("MINY");
		String maxX = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("MAXX");
		String maxY = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("MAXY");
		
		try {
			GetSheetIds sheetIdObj = new GetSheetIds();
			sheetIdObj.execute(minX, minY, maxX, maxY);
			sheetIds = (GetSheetIdsRow[])sheetIdObj.getRows();
		} catch (SQLException e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		
		return sheetIds;
	}
	
	public Map<String, Id> getViewScannedDocumentsIdMap() {
		return viewScannedDocumentsIdMap;
	}
	public void setViewScannedDocumentsIdMap(Map<String, Id> viewScannedDocumentsIdMap) {
		this.viewScannedDocumentsIdMap = viewScannedDocumentsIdMap;
	}
	public List<ViewScannedDocuments> getViewScannedSheetDocumentsList() {
		return viewScannedSheetDocumentsList;
	}
	public void setViewScannedSheetDocumentsList(List<ViewScannedDocuments> viewScannedSheetDocumentsList) {
		this.viewScannedSheetDocumentsList = viewScannedSheetDocumentsList;
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
}
