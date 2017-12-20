package com.wssc.filenet.migration.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.core.ContentElement;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.Id;
import com.wssc.filenet.migration.ce.CEConnection;

/**
 * Servlet implementation class SheetDrawingsServlet
 */
@WebServlet("/SheetDrawingsServlet")
public class SheetDrawingsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Logger logger =	 Logger.getLogger("com.wssc.filenet.migration.intranet");  
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SheetDrawingsServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Servlet#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		Map docIdMap = (Map)session.getAttribute("sheetDrawingsMap");
		String imageId = request.getParameter("imageId");      
	    Id id = (Id)docIdMap.get(imageId);
	    
	    if(id != null)
	    	downloadDocument(request, response, id);
	}
	 
private void downloadDocument(HttpServletRequest request, HttpServletResponse response, Id id) {
	try { 
		Document doc = Factory.Document.getInstance(CEConnection.getInstance().getObjectStore(), "Document", id);
		doc.refresh();
		response.setContentType(doc.get_MimeType());
		response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
		response.setHeader("Pragma","no-cache"); //HTTP 1.0
		response.setHeader("Cache-Control","must-revalidate, post-check=0, pre-check=0");
		ContentElementList contentList = doc.get_ContentElements();
		Iterator contentListIter = contentList.iterator();
		
		while(contentListIter.hasNext()) {
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
		
	}catch( com.ibm.wsspi.webcontainer.ClosedConnectionException e) {
		logger.log(Level.SEVERE, "Prizm Viewer not installed, hence client browser "+request.getHeader("user-agent")+" throws below error.");
		logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
		e.printStackTrace();
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
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
