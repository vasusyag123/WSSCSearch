package com.wssc.filenet.migration.ce;

import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.security.auth.Subject;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.exception.EngineRuntimeException;
import com.filenet.api.util.UserContext;
import com.wssc.filenet.migration.utils.PropertyReader;

public class CEConnection {

	static CEConnection instance;
	private static Properties props;
	private static Logger logger =	 Logger.getLogger("com.wssc.filenet.migration.intranet");
	private String uri;
	private String userName;
	private String password;
	private String domainName;
	private String objectStoreName;
	private String loginModule;
	private Connection con = null;
	private ObjectStore os = null;
	
	private CEConnection() {
		props = PropertyReader.getInstance().getPropertyBag();
		uri = props.getProperty("com.wssc.filenet.uri").trim();
		userName = props.getProperty("com.wssc.filenet.user").trim();
		password = props.getProperty("com.wssc.filenet.password").trim();
		domainName = props.getProperty("com.wssc.filenet.domain").trim();
		objectStoreName = props.getProperty("com.wssc.filenet.objectstore").trim();
		loginModule = props.getProperty("com.wssc.filenet.loginmodule").trim();
		
	}
	
	public static CEConnection getInstance() {
		if(instance == null) {
			instance = new CEConnection();		
		}
		return instance;
	}
	
	/**
	 * getConnection method returns the filenet connection object
	 * @return
	 */
	private Connection getConnection() {
		
		try {
			//System.setProperty("com.ibm.CORBA.ConfigURL", "file:C:\\Program Files\\IBM\\WebSphere\\AppServer\\profiles\\default\\properties\\sas.client.props");
			//System.setProperty("java.security.auth.login.config", "C:\\Program Files\\IBM\\WebSphere\\AppServer\\profiles\\default\\properties\\wsjaas_client.conf");
			//System.setProperty("java.naming.factory.initial", "com.ibm.websphere.naming.WsnInitialContextFactory");
			props = PropertyReader.getInstance().getPropertyBag();
			con = Factory.Connection.getConnection(uri);
			Subject sub = UserContext.createSubject(con, userName, password, loginModule);
			UserContext uc = UserContext.get();
			uc.pushSubject(sub);
			
		}catch(EngineRuntimeException e ){
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}catch(Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}
		return con;
	}
	
	/**
	 * getObjectStore method return FileNet ObjectStore using connection and domain objects
	 * @return
	 */
	public ObjectStore getObjectStore() {
		try {
			Domain domain = Factory.Domain.fetchInstance(getConnection(),domainName,null);
			os = Factory.ObjectStore.fetchInstance(domain, objectStoreName, null);
			logger.log(Level.INFO, "Object Store :"+os.get_DisplayName());
			System.out.println("Object Store :"+os.get_DisplayName());
		}catch(EngineRuntimeException e ){
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}catch(Exception e) {
			logger.log(Level.SEVERE, this.getClass().getName()+" "+e.getMessage());
			e.printStackTrace();
		}finally {
			con = null;
		}
		return os;
	}
	
	public static void main(String arg[]) {
		new CEConnection().getInstance().getObjectStore();
	}
		
}
