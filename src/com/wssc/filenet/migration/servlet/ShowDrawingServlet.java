package com.wssc.filenet.migration.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
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
import com.wssc.filenet.migration.utils.PropertyReader;

/**
 * Servlet implementation class ShowDrawingServlet
 */
@WebServlet("/ShowDrawingServlet")
public class ShowDrawingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private Properties props;
	private Logger logger =	 Logger.getLogger("com.wssc.filenet.migration.intranet");
	public void init(ServletConfig config) throws ServletException {
		super.init();
		props = PropertyReader.getInstance().getPropertyBag();
	}
	
	private void downloadDocument(HttpServletRequest request, HttpServletResponse response, Id id) {
		try {
			
			Document doc = Factory.Document.getInstance(CEConnection.getInstance().getObjectStore(), props.getProperty("document.class"), id);
			doc.refresh();
			response.setContentType(doc.get_MimeType());
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); 
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setHeader("Cache-Control","must-revalidate, post-check=0, pre-check=0");
			/*response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
			response.setDateHeader("Expires",System.currentTimeMillis());*/
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
			logger.log(Level.SEVERE, "FileNotFoundException in download servlet "+e.getMessage());
			e.printStackTrace();
		}catch(EngineRuntimeException e) {
			logger.log(Level.SEVERE, "EngineRuntimeException in download servlet "+e.getMessage());
			e.printStackTrace();
		}catch (Exception e) {
			logger.log(Level.SEVERE, "Exception in download servlet "+e.getMessage());
			e.printStackTrace();
		}
	}
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ShowDrawingServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("ShowDrawingServlet");
		HttpSession session = request.getSession(false);
		Id id = (Id)session.getAttribute("crossRefId");
	    if(id != null)
	    	downloadDocument(request, response, id);
	    else {
	    	OutputStream os = response.getOutputStream();
	    	os.write(new String("Document not found.").getBytes());
	    	os.flush();
	    	os.close();
	    }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
