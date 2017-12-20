package com.wssc.filenet.migration.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Level;

import javax.faces.context.FacesContext;
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
import com.filenet.api.core.ObjectStore;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.Id;
import com.wssc.filenet.migration.ce.CEConnection;

/**
 * Servlet implementation class OpenDocumentServlet
 */
@WebServlet("/OpenDocumentServlet")
public class OpenDocumentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OpenDocumentServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sid = request.getParameter("imageId");
		System.out.println("id "+sid);
		Id id = new Id(sid);
		downloadDocument(response, request, id, "Document"); 
	}

	private void downloadDocument(HttpServletResponse response, HttpServletRequest request, Id id, String documentClass) {
		try {
			ObjectStore objectStore = CEConnection.getInstance().getObjectStore();
			
			Document doc = Factory.Document.getInstance(objectStore, documentClass, id);
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
			
			e.printStackTrace();
		}catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}catch(EngineRuntimeException e) {
			
			e.printStackTrace();
		}catch (Exception e) {
			
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
