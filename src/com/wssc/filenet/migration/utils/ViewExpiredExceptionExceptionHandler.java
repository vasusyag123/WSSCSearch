package com.wssc.filenet.migration.utils;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import java.util.Iterator;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;


public class ViewExpiredExceptionExceptionHandler extends
		ExceptionHandlerWrapper {
	private ExceptionHandler wrapped;

	public ViewExpiredExceptionExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

	@Override
    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }

	@Override
    public void handle() throws FacesException {
		for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable t = context.getException();
            if (t instanceof ViewExpiredException) {
                ViewExpiredException vee = (ViewExpiredException) t;
                FacesContext fc = FacesContext.getCurrentInstance();
                Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
                NavigationHandler nav =
                        fc.getApplication().getNavigationHandler();
                try {
                	requestMap.put("currentViewId", vee.getViewId());
                    nav.handleNavigation(fc, null, "viewExpired");
                    fc.renderResponse();
                	 
                	fc.responseComplete();
                } finally {
                    i.remove();
                }
            } 
        }

       // getWrapped().handle();

    }


}
