package com.wssc.filenet.migration.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.wssc.filenet.migration.utils.PropertyReader;

import com.wssc.filenet.migration.ce.CEConnection;

/**
 * Servlet implementation class DownloadServlet
 */
@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Properties props;
	private Logger logger =	 Logger.getLogger("com.wssc.filenet.migration.intranet");
    /**
     * @see HttpServlet#HttpServlet()
     */
	public DownloadServlet() {
		super();
		props = PropertyReader.getInstance().getPropertyBag();
	}   
	
	private void downloadDocument(HttpServletResponse response, HttpServletRequest request) {
		try {
			HttpSession session = request.getSession(false);
			String redirectUrl = props.getProperty("surveysRedirect");
			String title = (String)session.getAttribute("surveyId");
			String subCategory = (String)session.getAttribute("surveyType");
			String sessionId = (String)session.getAttribute("surverySession");
			String query = "SELECT * FROM Surveys WHERE (IsCurrentVersion = TRUE) AND (TITLE='"+title+"') AND (StorageCategory='Surveys') AND (SubCategory='"+subCategory+"')";
			ObjectStore objectStore = CEConnection.getInstance().getObjectStore();
			SearchScope search = new SearchScope(objectStore);
			System.out.println("Surveys Query:"+query);
			SearchSQL sql = new SearchSQL(query);
			DocumentSet documents = (DocumentSet)search.fetchObjects(sql, null, null, Boolean.valueOf(true));
			System.out.println("documents empty :"+documents.isEmpty());
			String fileName = null;
			if(!documents.isEmpty()) {
				Iterator docIter = documents.iterator();
				while(docIter.hasNext()) {
					Document doc = (Document)docIter.next();
					doc.refresh();
					ContentElementList contentList = doc.get_ContentElements();
					Iterator contentListIter = contentList.iterator();
					while(contentListIter.hasNext()) {
						response.setContentType(doc.get_MimeType());
						response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
						response.setHeader("Pragma","no-cache"); //HTTP 1.0
						response.setHeader("Cache-Control","must-revalidate, post-check=0, pre-check=0");
						ContentElement contentElement = (ContentElement)contentListIter.next();
						ContentTransfer content = (ContentTransfer)contentElement;
						InputStream is = content.accessContentStream();
						OutputStream os = response.getOutputStream();
						byte[] bytes = new byte[4096];
						int i=0;
						while( (i=is.read(bytes)) != -1 ) {
							os.write(bytes,0,i);
							os.flush();
						}
						os.close();
					}
				}
			}else {
				ServletContext context = session.getServletContext();
				System.out.println(context.getRealPath("/"));
				
				File file = new File(context.getRealPath("/")+"/nodwf.dwf");
				FileInputStream is = new FileInputStream(file); 
				OutputStream os = response.getOutputStream();
				byte[] bytes = new byte[4096];
				int i=0;
				while( (i=is.read(bytes)) != -1 ) {
					os.write(bytes,0,i);
					os.flush();
				}
				os.close();
			}
			
			/*if(!documents.isEmpty()) {
				Iterator docIter = documents.iterator();
				while(docIter.hasNext()) {
					Document doc = (Document)docIter.next();
					doc.refresh();
					ContentElementList contentList = doc.get_ContentElements();
					Iterator contentListIter = contentList.iterator();
					String surveysLocation = props.getProperty("surveysLocation");
					while(contentListIter.hasNext()) {
						ContentElement contentElement = (ContentElement)contentListIter.next();
						ContentTransfer content = (ContentTransfer)contentElement;
						fileName = content.get_RetrievalName().toUpperCase();
						InputStream is = content.accessContentStream();
						FileOutputStream out = new FileOutputStream(surveysLocation+sessionId+"\\"+content.get_RetrievalName().toUpperCase());
						File file = new File(surveysLocation+sessionId);
						if(!file.exists()) {
							file.mkdir();
						}
						byte[] bytes = new byte[4096];
						int i=0;
						while( (i=is.read(bytes)) != -1 ) {
							out.write(bytes,0,i);
							out.flush();
						}
						is.close();
						out.close();
					}
					System.out.println("Redirected URL :"+redirectUrl+fileName);
					response.sendRedirect(redirectUrl+fileName);
					
				}
			}else {
				response.sendRedirect(redirectUrl+"0");
			}*/
		}catch(EngineRuntimeException e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}catch (Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		downloadDocument(response,request);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
