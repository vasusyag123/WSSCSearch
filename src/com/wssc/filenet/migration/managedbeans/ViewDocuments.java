package com.wssc.filenet.migration.managedbeans;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELContext;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.wssc.filenet.migration.ce.CEConnection;
import com.wssc.filenet.migration.dao.PlumbingCardsDocuments;
import com.wssc.filenet.migration.managedbeans.Documents;
import com.wssc.filenet.migration.managedbeans.ProjectSketchBean;
import com.wssc.filenet.migration.managedbeans.ViewScanned200SheetDocuments;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.core.ContentElement;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;

@ManagedBean(name="viewDocuments")
@SessionScoped
public class ViewDocuments {
	public String pageTitle="";
	private static Logger logger =	 Logger.getLogger("com.wssc.filenet.migration");
	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	
	public ViewDocuments() {
		
	}
	public String viewOpenSessionCrossReferenceDocument() {
		String returnString = "";
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		ServletResponse out = (ServletResponse)facesContext.getExternalContext().getResponse();
		String id = null;
		if(facesContext.getExternalContext().getRequestParameterMap().get("docTitle") != null)
		 id = (String)facesContext.getExternalContext().getRequestParameterMap().get("docTitle");
		if(id != null) {
			ObjectStore os = CEConnection.getInstance().getObjectStore();
			SearchScope search = new SearchScope(os);
			SearchSQL sql = new SearchSQL("SELECT * FROM Document WHERE IsCurrentVersion = TRUE AND TITLE = '"+id+"' AND DocumentTitle='"+id+"'");
			DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
			Iterator docIter = documents.iterator();
			if(!documents.isEmpty()) {
				Document doc = null;
				while(docIter.hasNext()) {
					doc = (Document)docIter.next();
				}
				String mimeType = doc.getProperties().getStringValue("MimeType");
				facesContext.getExternalContext().getSessionMap().put("imageId",doc.get_Id());
				if(mimeType.equalsIgnoreCase("application/pdf") || 	mimeType.equalsIgnoreCase("application/msword") || 	mimeType.equalsIgnoreCase("application/vnd.ms-excel") ) 
				{
					returnString = "";
					try {
						facesContext.getExternalContext().redirect("/Drawings/ViewDocumentServlet");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}else {
					returnString = "SHOWDOC";
				}
			}
			else {
				try {
					returnString="";
				    out.getWriter().write("Drawing not found for "+id+".");
					out.getWriter().flush();
					out.getWriter().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return returnString;
		}else{
			return "";
		}
	    
	}
	
	public String onDocumentClick() {
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			Documents documents = (Documents)elContext.getELResolver().getValue(elContext, null, "documents");
			Map docIdMap = documents.objectIdMap;
			String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
			facesContext.getExternalContext().getSessionMap().put("imageId", docIdMap.get(id));
			facesContext.getExternalContext().getSessionMap().put("docClass", "Document");
			System.out.println(docIdMap.get(id));
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		return "SHOWDOC";
	}
	
	public String viewNonPrizmDocument() {
		try {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response=(HttpServletResponse)facesContext.getExternalContext().getResponse();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			Documents documents = (Documents)elContext.getELResolver().getValue(elContext, null, "documents");
			Map docIdMap = documents.objectIdMap;
			String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
			downloadDocument(response, (Id)docIdMap.get(id), "Document");
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		FacesContext.getCurrentInstance().responseComplete();
		return "";
	}
	
	private void downloadDocument(HttpServletResponse response, Id id, String documentClass) {
		try {
			System.out.println("1 -- downloadDocument");
			ObjectStore objectStore = CEConnection.getInstance().getObjectStore();
			Document doc = Factory.Document.getInstance(objectStore, documentClass, id);
			doc.refresh();
			
			
				System.out.println("1 -- minetype"+doc.get_MimeType().toString());
			
			response.setContentType(doc.get_MimeType());
			
			
			ContentElementList contentList = doc.get_ContentElements();
			Iterator contentListIter = contentList.iterator();
			while(contentListIter.hasNext()) {
				ContentElement contentElement = (ContentElement)contentListIter.next();
				ContentTransfer content = (ContentTransfer)contentElement;
				InputStream is = content.accessContentStream();
				ServletOutputStream os = response.getOutputStream();
				byte[] bytes = new byte[4096];
				int i=0;
				while( (i=is.read(bytes)) != -1 ) {
					os.write(bytes,0,i);
					
				}
				os.flush();
				os.close();
				is.close();
			}
			
		}catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}catch(EngineRuntimeException e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public String onSheetDrawingClick() {
		try {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			Documents documents = (Documents)elContext.getELResolver().getValue(elContext, null, "documents");
			Map docIdMap = documents.sheetDrawingsMap;
			
			String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
			facesContext.getExternalContext().getSessionMap().put("imageId", docIdMap.get(id));
			facesContext.getExternalContext().getSessionMap().put("docClass", "Document");
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		return "SHOWDOC";
	}
	
	public String viewNonPrizmSheetDrawingClick() {
		try {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response=(HttpServletResponse)facesContext.getExternalContext().getResponse();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			Documents documents = (Documents)elContext.getELResolver().getValue(elContext, null, "documents");
			Map docIdMap = documents.sheetDrawingsMap;
			
			String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
			downloadDocument(response, (Id)docIdMap.get(id), "Document");
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		FacesContext.getCurrentInstance().responseComplete();
		return "";
	}

	public String viewNonPrizmScanSheetDocument() {
		try
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletResponse response=(HttpServletResponse)facesContext.getExternalContext().getResponse();
            ELContext elContext = FacesContext.getCurrentInstance().getELContext();
            ViewScanned200SheetDocuments documents = (ViewScanned200SheetDocuments)elContext.getELResolver().getValue(elContext, null, "scanned200SheetDocuments");
            
            Map docIdMap = documents.viewScannedDocumentsIdMap;
            String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
            downloadDocument(response, (Id)docIdMap.get(id), "ScaleReferenceSheets");
            
        }catch(Exception e) {
            logger.log(Level.SEVERE, getClass().getName() + " " + e.getMessage());
            e.printStackTrace();
        }
        FacesContext.getCurrentInstance().responseComplete();
		return "";
	 }
	
	public String onViewScanSheetDocumentClick()
    {
        try
        {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ELContext elContext = FacesContext.getCurrentInstance().getELContext();
            ViewScanned200SheetDocuments documents = (ViewScanned200SheetDocuments)elContext.getELResolver().getValue(elContext, null, "scanned200SheetDocuments");
            Map docIdMap = documents.viewScannedDocumentsIdMap;
            String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
            facesContext.getExternalContext().getSessionMap().put("imageId", docIdMap.get(id));
            facesContext.getExternalContext().getSessionMap().put("docClass", "ScaleReferenceSheets");
        }catch(Exception e) {
            logger.log(Level.SEVERE, getClass().getName() + " " + e.getMessage());
            e.printStackTrace();
        }
        
        return "SHOWSHEET";
    }
	
	public String onQueryEngineeringDocumentClick( ) {
		try {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			QueryEngineeringDocumentsBean documents = (QueryEngineeringDocumentsBean)elContext.getELResolver().getValue(elContext, null, "documentsBean");
			Map docIdMap = documents.qeDocumentsIdMap;
			String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
			System.out.println("test for eng doc  id:"+id);
			facesContext.getExternalContext().getSessionMap().put("imageId", docIdMap.get(id));
			facesContext.getExternalContext().getSessionMap().put("docClass", "Document");
			 
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		return "SHOWDOC";
		
	}
	
	public String onQueryTestStationDocumentClick() {
		try {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			QueryTestStationDrawingsBean documents = (QueryTestStationDrawingsBean)elContext.getELResolver().getValue(elContext, null, "testStationBean");
			Map docIdMap = documents.qeDocumentsIdMap;
			String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
			System.out.println("test for test station id:"+id);
			facesContext.getExternalContext().getSessionMap().put("imageId", docIdMap.get(id));
			facesContext.getExternalContext().getSessionMap().put("docClass", "Document");
			 
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		return "SHOWDOC";
		
	}
	
	public String onQueryPCDocumentClick( ) {
		try {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			QueryPlumbingCardsBean documents = (QueryPlumbingCardsBean)elContext.getELResolver().getValue(elContext, null, "pcBean");
			Map docIdMap = documents.pcDocumentsIdMap;
			String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
			System.out.println("test for Plumbing Card id:"+id);
			facesContext.getExternalContext().getSessionMap().put("imageId", docIdMap.get(id));
			facesContext.getExternalContext().getSessionMap().put("docClass", "Document");
			 
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		return "SHOWDOC";
		
	}
	
	public String viewCrossReferenceDocument() {
		String returnString = "";
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		ServletResponse out = (ServletResponse)facesContext.getExternalContext().getResponse();
		String id = null;
		if(facesContext.getExternalContext().getRequestParameterMap().get("docTitle") != null)
		 id = (String)facesContext.getExternalContext().getRequestParameterMap().get("docTitle");
		if(id != null) {
			ObjectStore os = CEConnection.getInstance().getObjectStore();
			SearchScope search = new SearchScope(os);
			SearchSQL sql = new SearchSQL("SELECT * FROM Document WHERE IsCurrentVersion = TRUE AND TITLE = '"+id+"' AND DocumentTitle='"+id+"'");
			DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
			Iterator docIter = documents.iterator();
			if(!documents.isEmpty()) {
				Document doc = null;
				while(docIter.hasNext()) {
					doc = (Document)docIter.next();
				}
				String mimeType = doc.getProperties().getStringValue("MimeType");
				facesContext.getExternalContext().getSessionMap().put("imageId",doc.get_Id());
				if(mimeType.equalsIgnoreCase("application/pdf") || 	mimeType.equalsIgnoreCase("application/msword") || 	mimeType.equalsIgnoreCase("application/vnd.ms-excel") ) 
				{
					returnString = "";
					try {
						facesContext.getExternalContext().redirect("/Drawings/ViewQueryEngineeringDocumentServlet");
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}else {
					returnString = "SHOWDOC";
				}
			}
			else {
				try {
					returnString="";
				    out.getWriter().write("Drawing not found for "+id+".");
					out.getWriter().flush();
					out.getWriter().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return returnString;
		}else{
			return "";
		}
	    
	}
	
	private void downloadCrossReferenceDocument(ServletResponse response, Document doc) {
		try {
			System.out.println("downloadCrossReferenceDocument ****************");
			StringBuilder sbuilder = new StringBuilder();
			doc.refresh();
			response.setContentType(doc.get_MimeType());
			ContentElementList contentList = doc.get_ContentElements();
			Iterator contentListIter = contentList.iterator();
			while(contentListIter.hasNext()) {
				ContentElement contentElement = (ContentElement)contentListIter.next();
				ContentTransfer content = (ContentTransfer)contentElement;
				//InputStream is = content.accessContentStream();
				
				PrintWriter pw = response.getWriter();
				try (InputStream inputStream = content.accessContentStream()) {
					int c;
					while ((c = inputStream.read()) != -1) {
						sbuilder.append((char) c);
					}                
				}
				pw.print(sbuilder.toString());
				pw.flush();
				pw.close();
				/*
				 * ServletOutputStream os = response.getOutputStream();
				byte[] bytes = new byte[4096];
				int i=0;
				while( (i=is.read(bytes)) != -1 ) {
					os.write(bytes,0,i);
					os.flush();
				}
				os.close();*/
			}
			
		}catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}catch(EngineRuntimeException e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
	}
	

	

	
	public String viewNonPrizmQueryEngineeringDocument() {
		try {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response=(HttpServletResponse)facesContext.getExternalContext().getResponse();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			QueryEngineeringDocumentsBean documents = (QueryEngineeringDocumentsBean)elContext.getELResolver().getValue(elContext, null, "documentsBean");
			Map docIdMap = documents.qeDocumentsIdMap;
			String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
			downloadDocument(response, (Id)docIdMap.get(id), "Document");
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		FacesContext.getCurrentInstance().responseComplete();
		return "";
	}
	
	public String viewNonPrizmQueryTestStationDocument() {
		
		try {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response=(HttpServletResponse)facesContext.getExternalContext().getResponse();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			QueryTestStationDrawingsBean documents = (QueryTestStationDrawingsBean)elContext.getELResolver().getValue(elContext, null, "testStationBean");
			Map docIdMap = documents.qeDocumentsIdMap;
			String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
			downloadDocument(response, (Id)docIdMap.get(id), "Document");
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		FacesContext.getCurrentInstance().responseComplete();
		return "";
	}
	
	public String viewNonPrizmQueryPCDocument() {
		
		try {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response=(HttpServletResponse)facesContext.getExternalContext().getResponse();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			QueryPlumbingCardsBean documents = (QueryPlumbingCardsBean)elContext.getELResolver().getValue(elContext, null, "pcBean");
			Map docIdMap = documents.pcDocumentsIdMap;
			String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
			downloadDocument(response, (Id)docIdMap.get(id), "Document");
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		FacesContext.getCurrentInstance().responseComplete();
		return "";
	}
	public String onProjectSketchClick() {
		try {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			ProjectSketchBean documents = (ProjectSketchBean)elContext.getELResolver().getValue(elContext, null, "projectSketchBean");
	        Map docIdMap = documents.sketchIdMap;
			String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
			String docClass = (String)facesContext.getExternalContext().getRequestParameterMap().get("sketchClass");
			facesContext.getExternalContext().getSessionMap().put("imageId", docIdMap.get(id));
			facesContext.getExternalContext().getSessionMap().put("docClass", docClass);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		return "SHOWSKETCH";
	}
	
	public String viewNonPrizmProjectSketch() {
		try {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response=(HttpServletResponse)facesContext.getExternalContext().getResponse();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			ProjectSketchBean documents = (ProjectSketchBean)elContext.getELResolver().getValue(elContext, null, "projectSketchBean");
			Map docIdMap = documents.sketchIdMap;
			String id = (String)facesContext.getExternalContext().getRequestParameterMap().get("objectId");
			String docClass = (String)facesContext.getExternalContext().getRequestParameterMap().get("sketchClass");
			downloadDocument(response, (Id)docIdMap.get(id), docClass);
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		FacesContext.getCurrentInstance().responseComplete();
		return "";
	}

	public String passTestStationContractReference() {
		
		try {
			String returnValueString = "";
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response=(HttpServletResponse)facesContext.getExternalContext().getResponse();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			ServletResponse outObj = (ServletResponse)facesContext.getExternalContext().getResponse();
			QueryTestStationDrawingsBean documents = (QueryTestStationDrawingsBean)elContext.getELResolver().getValue(elContext, null, "testStationBean");
			Map docIdMap = documents.qeDocumentsIdMap;
			String contractId = (String)facesContext.getExternalContext().getRequestParameterMap().get("contractId");
			System.out.println("contractId :"+contractId);
			if(contractId != null) {
				ObjectStore os = CEConnection.getInstance().getObjectStore();
				SearchScope search = new SearchScope(os);
				SearchSQL sql = new SearchSQL("SELECT * FROM Document WHERE IsCurrentVersion = TRUE AND TITLE = '"+contractId+"' AND DocumentTitle='"+contractId+"'");
				DocumentSet documnet = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
				Iterator docIter = documnet.iterator();
				if(!documnet.isEmpty()) {
					Document docs = null;
					while(docIter.hasNext()) {
						docs = (Document)docIter.next();
					}
					String mimeType = docs.getProperties().getStringValue("MimeType");
					facesContext.getExternalContext().getSessionMap().put("imageId",docs.get_Id());
					if(mimeType.equalsIgnoreCase("application/pdf") || 	mimeType.equalsIgnoreCase("application/msword") || 	mimeType.equalsIgnoreCase("application/vnd.ms-excel") ) 
					{
						returnValueString = "";
						try {
							facesContext.getExternalContext().redirect("/Drawings/ViewDocumentServlet");
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}else {
						returnValueString = "SHOWDOC";
					}
				}
				else {
					try {
						returnValueString="";
						outObj.getWriter().write("Drawing not found for "+contractId+".");
						outObj.getWriter().flush();
						outObj.getWriter().close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return returnValueString;
			}else{
				return "";
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		FacesContext.getCurrentInstance().responseComplete();
		return "";
	}
	public String passTestStationContractId() {
		System.out.println("in passTestStationContractId method of class viewDocuments");
		return "";
	}
	
	
}
