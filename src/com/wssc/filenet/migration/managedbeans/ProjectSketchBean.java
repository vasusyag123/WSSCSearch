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
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.component.datatable.DataTable;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.core.ContentElement;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;

import com.wssc.filenet.migration.ce.CEConnection;
import com.wssc.filenet.migration.dao.EngineeringDocuments;
import com.wssc.filenet.migration.dao.ProjectSketchDocuments;
import com.wssc.filenet.migration.utils.PropertyReader;

@ManagedBean(name="projectSketchBean")
@SessionScoped
public class ProjectSketchBean {
	public boolean showButton=true;
	int msDocCounter = 0;
	int count=0;
	public int recordsAvailable;
	public List<ProjectSketchDocuments> projectSketchList;
	public Map<String, Id> sketchIdMap;
	private Properties props;
	private static Logger logger =	 Logger.getLogger("com.wssc.filenet.migration.intranet");
	public String title = "Available Project Sketches";
	public String getTitle() {
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		ProjectSketchBean documentsBean = (ProjectSketchBean) elContext.getELResolver().getValue(elContext, null, "projectSketchBean");
		documentsBean = new ProjectSketchBean();
		documentsBean.invoke();
		elContext.getELResolver().setValue(elContext, null, "projectSketchBean", documentsBean);
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void invoke() {
		props = PropertyReader.getInstance().getPropertyBag();
		projectSketchList = new ArrayList<ProjectSketchDocuments>();
		sketchIdMap = new HashMap<String, Id>();
		recordsAvailable = getDocuments("ProjectSketch");
		recordsAvailable = recordsAvailable + getDocuments("Correspondence");
	}
	
	public int getDocuments(String docClass) {
		showButton=true;
		String DATE_FORMAT = "MM/dd/yyyy hh:mm:ss a";
		DateFormat dt = new SimpleDateFormat(DATE_FORMAT);
		String query = null;
		if(docClass.equalsIgnoreCase("ProjectSketch"))
			query = prepareRequest(docClass);
		else if(docClass.equalsIgnoreCase("Correspondence"))
			query = prepareRequestForCorrespondence(docClass);
		ObjectStore os = CEConnection.getInstance().getObjectStore();
		SearchScope search = new SearchScope(os);
		SearchSQL sql = new SearchSQL(query);
		DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
		Iterator docIter = documents.iterator();
		
		/*
		Map fileTypes = new HashMap();
		fileTypes.put("DOC", "DOC");
		fileTypes.put("XLS", "XLS");
		fileTypes.put("PDF", "PDF");
		*/
		if(!documents.isEmpty()) {
			while(docIter.hasNext()) {
				count++;
				Document doc = (Document)docIter.next();
				ProjectSketchDocuments projectDoc = new ProjectSketchDocuments();
				if(doc.get_Id() != null){
					projectDoc.setDocId("DOCID"+count);
					sketchIdMap.put("DOCID"+count, doc.get_Id());
				}
				String project = null;
				projectDoc.setDocumentClass(docClass);
				if(doc.getProperties().getStringValue("ProjectType") != null)
					project = (doc.getProperties().getStringValue("ProjectType"));
				if(doc.getProperties().getStringValue("ProjectNumber") != null)
					project = project != null ? (project+doc.getProperties().getStringValue("ProjectNumber")) :  doc.getProperties().getStringValue("ProjectNumber");
				if(doc.getProperties().getStringValue("Title") != null)
					projectDoc.setTitle(doc.getProperties().getStringValue("Title"));   
				if(doc.getProperties().getDateTimeValue("DOCUMENTDATE") != null)
					projectDoc.setDate(dt.format(doc.getProperties().getDateTimeValue("DOCUMENTDATE")));
				if(!docClass.equals("Correspondence")) {
					if(doc.getProperties().getStringValue("ReviewMarkedup") != null)
						projectDoc.setReviewMarkup(doc.getProperties().getStringValue("ReviewMarkedup"));
					if(doc.getProperties().getStringValue("SubmittalNo") != null)
						projectDoc.setSubmittalNo(doc.getProperties().getStringValue("SubmittalNo"));
				}
				if(doc.getProperties().getStringValue("COMMENT") != null)
					projectDoc.setComment(doc.getProperties().getStringValue("COMMENT"));
				String mimeType = doc.getProperties().getStringValue("MimeType");
				if(mimeType.equalsIgnoreCase("application/pdf") || 	mimeType.equalsIgnoreCase("application/msword") ||
					mimeType.equalsIgnoreCase("application/vnd.ms-excel") || mimeType.equalsIgnoreCase("application/octet-stream") ) {
					projectDoc.mimeType = false;
					msDocCounter=msDocCounter+1;
				}
				projectDoc.setProject(project);
				projectSketchList.add(projectDoc);		
			}
			
		}
		if(projectSketchList.size() == msDocCounter)
			showButton=false;
		return projectSketchList.size();
	}
	
	public String showDocuments() {
		try {	
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ServletContext context = (ServletContext)facesContext.getExternalContext().getContext();
			HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
			DataTable dataTable = (DataTable)facesContext.getViewRoot().findComponent("projectSketchForm:projectSketchTable");
			int availableRows = dataTable.getRowCount();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			ProjectSketchBean documents = (ProjectSketchBean)elContext.getELResolver().getValue(elContext, null, "projectSketchBean");
			Map docIdMap = documents.sketchIdMap;
			StringBuffer prizmBuffer = new StringBuffer();
			Map imageIdMap = new HashMap();
			
			String servletUrl = "http://"+request.getServerName()+":"+request.getServerPort()+facesContext.getExternalContext().getRequestContextPath()+"/GetContentServlet?imageId=";
			prizmBuffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?> <PrizmViewerListFile ArrangeDockingWindows = \"none\"> <ImageFileList>");
			for(int i=0; i<availableRows; i++) {
				dataTable.setRowIndex(i);
				ProjectSketchDocuments document = (ProjectSketchDocuments)dataTable.getRowData();
				if(document.selected) {
					String docId = document.docId;
					imageIdMap.put(docId, docIdMap.get(docId));
					prizmBuffer.append("<ImageFile FileURL=\""+servletUrl+docId+"\"/>");
				}
			}
			facesContext.getExternalContext().getSessionMap().put("imageIdMap", imageIdMap);
			prizmBuffer.append("</ImageFileList></PrizmViewerListFile>");
			BufferedWriter out = new BufferedWriter(new FileWriter(props.getProperty("projectLocationPrizmFile")));
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
	
	
	private String prepareRequest(String docClass) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String projNumber = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("ProjcNo");
		String projType = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("ProjcType");
		String query = "SELECT * FROM "+docClass+" WHERE (IsCurrentVersion = TRUE) " +
						" AND (ProjectNumber='"+projNumber.trim()+"') AND (ProjectType='"+projType.trim()+"') ORDER BY DOCUMENTDATE DESC";
		logger.log(Level.INFO, "Project Location Query :"+query);
		
		return (query);
	}
	
	private String prepareRequestForCorrespondence(String docClass) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String projNumber = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("ProjcNo");
		String projType = ""+(String)facesContext.getExternalContext().getRequestParameterMap().get("ProjcType");
		String query = "SELECT * FROM "+docClass+" WHERE (IsCurrentVersion = TRUE) " +
						" AND (ProjectNumber='"+projNumber.trim()+"') ORDER BY DOCUMENTDATE DESC";
		logger.log(Level.INFO, docClass+" Query :"+query);
		
		return (query);
	}
	
	public List<ProjectSketchDocuments> getProjectSketchList() {
		return projectSketchList;
	}
	public void setProjectSketchList(List<ProjectSketchDocuments> projectSketchList) {
		this.projectSketchList = projectSketchList;
	}
	public int getRecordsAvailable() {
		return recordsAvailable;
	}
	public void setRecordsAvailable(int recordsAvailable) {
		this.recordsAvailable = recordsAvailable;
	}
	public Map<String, Id> getSketchIdMap() {
		return sketchIdMap;
	}
	public void setSketchIdMap(Map<String, Id> sketchIdMap) {
		this.sketchIdMap = sketchIdMap;
	}
	public boolean isShowButton() {
		return showButton;
	}

	public void setShowButton(boolean showButton) {
		this.showButton = showButton;
	}
	
}
