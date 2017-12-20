package com.wssc.filenet.migration.managedbeans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name="surveys")
@RequestScoped
public class Surveys {
	
	private String pageTitle="";

	public String getPageTitle() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		String title = (String)facesContext.getExternalContext().getRequestParameterMap().get("RecordID");
		String subCategory = (String)facesContext.getExternalContext().getRequestParameterMap().get("Type");
		if(subCategory.equalsIgnoreCase("BM"))
			subCategory="Bench Mark";
		else if(subCategory.equalsIgnoreCase("TR"))
			subCategory="Traverse";
		//String sessionId = (String)facesContext.getExternalContext().getRequestParameterMap().get("SessionID");
		
		facesContext.getExternalContext().getSessionMap().put("surveyId", title.trim());
		facesContext.getExternalContext().getSessionMap().put("surveyType", subCategory.trim());
		//facesContext.getExternalContext().getSessionMap().put("surverySession", sessionId.trim());
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

}
