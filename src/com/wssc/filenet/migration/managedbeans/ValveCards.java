package com.wssc.filenet.migration.managedbeans;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.core.ContentElement;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.wssc.filenet.migration.ce.CEConnection;

@ManagedBean(name="valveCards")
@RequestScoped
public class ValveCards {
	public int recordsAvailable = 0;
	public String title = "Valve Card";
	final String NUMBER_PATTERN = "^[0-9]+$";
	public String getTitle() {
		
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletResponse response= (HttpServletResponse)facesContext.getExternalContext().getResponse();
			ELContext elContext = FacesContext.getCurrentInstance().getELContext();
			ValveCards documentsBean = (ValveCards) elContext.getELResolver().getValue(elContext, null, "valveCards");
			documentsBean = new ValveCards();
			recordsAvailable = 0;
			String id = (String)FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("ValveCardID");
			facesContext.getExternalContext().getSessionMap().put("valveCardId", id);
			if(id != null & id.length() > 0) {
				String cStart = id.substring(1, id.length());
				String query = "SELECT * FROM ValveCards WHERE (IsCurrentVersion = TRUE) AND (TITLE='"+id+"') AND (CollectionStart='"+cStart+"') AND (CollectionEnd='"+cStart+"') ";
				System.out.println("Individual valve card query :"+query);
				//Look for individual valve card
				if(searchValveCard(query, id, documentsBean, response)) {
					System.out.println("Individual valve card does not exist :"+id);
					GetValveCardId obj = new GetValveCardId();
		  			obj.execute();
		  			String valveCard = obj.getValveCardTitle(id);
		  			if(valveCard != null && valveCard.length() > 0) {
		  				String gquery = "SELECT * FROM ValveCards WHERE (IsCurrentVersion = TRUE) AND (TITLE='"+valveCard+"')";
		  				//look for generic valve card
		  				System.out.println("Searching general valve card query :"+gquery);
		  				searchValveCard(gquery, valveCard, documentsBean, response);
					}else {
						recordsAvailable = 0;
						response.sendRedirect(facesContext.getExternalContext().getRequestContextPath()+"/ViewValveCardServlet");
					}
				}
			
			}else {
				recordsAvailable = 0;
				response.sendRedirect(facesContext.getExternalContext().getRequestContextPath()+"/ViewValveCardServlet");
			}
		}catch(SQLException e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		
		
		return title;
	}
	
	
	private boolean searchValveCard(String query, String valveCard, ValveCards documentsBean, HttpServletResponse response) throws SQLException, IOException {
		ELContext elContext = FacesContext.getCurrentInstance().getELContext();
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ObjectStore objectStore = CEConnection.getInstance().getObjectStore();
		SearchScope search = new SearchScope(objectStore);
		SearchSQL sql = new SearchSQL(query);
		DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
		recordsAvailable = 1;
		facesContext.getExternalContext().getSessionMap().put("valveCardId", valveCard);
		elContext.getELResolver().setValue(elContext, null, "valveCards", documentsBean);
			
		if(!documents.isEmpty()) {
			Iterator docIter = documents.iterator();
			while(docIter.hasNext()) {
				Document doc = (Document)docIter.next(); 
				doc.refresh();
				ContentElementList contentList = doc.get_ContentElements();
				Iterator contentListIter = contentList.iterator();
				while(contentListIter.hasNext()) {
					ContentElement contentElement = (ContentElement)contentListIter.next();
					ContentTransfer content = (ContentTransfer)contentElement;
					if(content.get_ContentType().equalsIgnoreCase("application/pdf")) {
						response.sendRedirect(facesContext.getExternalContext().getRequestContextPath()+"/ViewValveCardServlet");
					}else {
						response.sendRedirect(facesContext.getExternalContext().getRequestContextPath()+"/viewValveCard.faces");
					}
				}
				
			}
		}
		return documents.isEmpty();
	}

	public void setTitle(String title) {
		this.title = title;
	}
	public int getRecordsAvailable() {
		return recordsAvailable;
	}

	public void setRecordsAvailable(int recordsAvailable) {
		this.recordsAvailable = recordsAvailable;
	}

	private Logger logger =	 Logger.getLogger("com.wssc.filenet.migration.intranet");

	public ValveCards() {
		
	}
}
