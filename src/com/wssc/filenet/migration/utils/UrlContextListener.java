package com.wssc.filenet.migration.utils;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Application Lifecycle Listener implementation class UrlContextListener
 *
 */
@WebListener
public class UrlContextListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public UrlContextListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent sce) {
    	Properties props = PropertyReader.getInstance().getPropertyBag();
     	ServletContext context = sce.getServletContext();
     	context.setAttribute("prizmurl", props.getProperty("prizm.url"));
     	
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub
    }
	
}
