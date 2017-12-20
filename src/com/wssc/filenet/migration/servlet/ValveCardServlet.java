package com.wssc.filenet.migration.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.core.ContentElement;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.wssc.filenet.migration.ce.CEConnection;
import com.wssc.filenet.migration.managedbeans.GetValveCardId;
import com.wssc.filenet.migration.managedbeans.GetValveCardIdRow;

/**
 * Servlet implementation class ValveCardServlet
 */
@WebServlet("/ValveCardServlet")
public class ValveCardServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ValveCardServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String id = request.getParameter("ValveCardID");
		System.out.println("valve id "+id);
		GetValveCardId obj = new GetValveCardId();
		try {
			String valveCard = null;
			
			String cStart = id.substring(1, id.length());
			String indvquery = "SELECT * FROM ValveCards WHERE (IsCurrentVersion = TRUE) AND (TITLE='"+id+"') AND (CollectionStart='"+cStart+"') AND (CollectionEnd='"+cStart+"') ";
			
			if(downloadDocument(indvquery, request, response)) {
				obj.execute();
	  			valveCard = obj.getValveCardTitle(id);

	  			if(valveCard != null && valveCard.length() > 0) {
					String query = "SELECT * FROM ValveCards WHERE (IsCurrentVersion = TRUE) AND (TITLE='"+valveCard+"')";
					downloadDocument2(query, request, response);
				}else {
					response.getOutputStream().write(new String("Valve Card Not Found.").getBytes());
					response.getOutputStream().flush();
					response.getOutputStream().close();
				}
			}
			
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(EngineRuntimeException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
    
    private boolean downloadDocument(String query, HttpServletRequest request, HttpServletResponse response) throws IOException {
    	ObjectStore objectStore = CEConnection.getInstance().getObjectStore();
		SearchScope search = new SearchScope(objectStore);
		SearchSQL sql = new SearchSQL(query);
		DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
		byte[] bytes = new byte[4096];
		OutputStream os = response.getOutputStream();
		System.out.println("Valve Card documents empty :"+documents.isEmpty());
		if(!documents.isEmpty()) {
			Iterator docIter = documents.iterator();
			while(docIter.hasNext()) {
				Document doc = (Document)docIter.next(); 
				response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
				response.setHeader("Pragma","no-cache"); //HTTP 1.0
				response.setHeader("Cache-Control","must-revalidate, post-check=0, pre-check=0");
				doc.refresh();
				ContentElementList contentList = doc.get_ContentElements();
				Iterator contentListIter = contentList.iterator();
				while(contentListIter.hasNext()) {
					ContentElement contentElement = (ContentElement)contentListIter.next();
					ContentTransfer content = (ContentTransfer)contentElement;
					response.setContentType(content.get_ContentType());
					InputStream is = content.accessContentStream();
					
					int i=0;
					while( (i=is.read(bytes)) != -1 ) {
						os.write(bytes,0,i);
						os.flush();
					}
					
				}
		
			}
			
		}else {
			
		}
		return documents.isEmpty();
    }

    private boolean downloadDocument2(String query, HttpServletRequest request, HttpServletResponse response) throws IOException {
    	ObjectStore objectStore = CEConnection.getInstance().getObjectStore();
		SearchScope search = new SearchScope(objectStore);
		SearchSQL sql = new SearchSQL(query);
		DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
		byte[] bytes = new byte[4096];
		OutputStream os = response.getOutputStream();
		System.out.println("Valve Card documents empty :"+documents.isEmpty());
		if(!documents.isEmpty()) {
			Iterator docIter = documents.iterator();
			while(docIter.hasNext()) {
				Document doc = (Document)docIter.next(); 
				response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
				response.setHeader("Pragma","no-cache"); //HTTP 1.0
				response.setHeader("Cache-Control","must-revalidate, post-check=0, pre-check=0");
				doc.refresh();
				ContentElementList contentList = doc.get_ContentElements();
				Iterator contentListIter = contentList.iterator();
				while(contentListIter.hasNext()) {
					ContentElement contentElement = (ContentElement)contentListIter.next();
					ContentTransfer content = (ContentTransfer)contentElement;
					response.setContentType(content.get_ContentType());
					InputStream is = content.accessContentStream();
					
					int i=0;
					while( (i=is.read(bytes)) != -1 ) {
						os.write(bytes,0,i);
						os.flush();
					}
					
				}
		
			}
			
		}else {
			os.write(new String("Valve Card does not exist.").getBytes());
			os.flush();
		}
		return documents.isEmpty();
    }
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
