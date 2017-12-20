package com.wssc.filenet.migration.utils;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyReader {

	static PropertyReader propertySingleton = null;
	private Properties propertyBag = new Properties();
	private static Logger logger =	 Logger.getLogger("com.wssc.filenet.migration.intranet");
	
	private PropertyReader() { 
		
		try {
			
			propertyBag.load(this.getClass().getResourceAsStream("filenet.properties"));
			
		} catch (IOException e) {
			logger.log(Level.SEVERE,this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
			
		} catch(NullPointerException e) {
			logger.log(Level.SEVERE,this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
			
		}catch(Exception e){
			logger.log(Level.SEVERE,this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
			
		}
	}
	
	/**
	 * @return Returns singleton handle for Property reader
	 */
	public static PropertyReader getInstance() {
	
		if(propertySingleton == null)
			propertySingleton = new PropertyReader();
		return propertySingleton;
	}
	
    /**
     * getValueAsString method returns the value for the specified key
     * @param key
     * @return
     */
    public String getValueAsString(String key){
    	
    	return (propertyBag.getProperty(key)).trim();
    	
    }
    public Properties getPropertyBag(){
    	
    	return propertyBag;
    }
}
