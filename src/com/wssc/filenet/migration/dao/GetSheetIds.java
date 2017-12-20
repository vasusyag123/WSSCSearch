package com.wssc.filenet.migration.dao;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

import com.ibm.db.beans.*;
import com.wssc.filenet.migration.utils.PropertyReader;

/**
 * This class sets the DBSelect property values. It also provides 
 * methods that execute your SQL statement, return 
 * a DBSelect reference, and return an array of objects  
 * representing the rows in the result set.
 * Generated:  Jun 4, 2009 1:06:21 PM
 */

public class GetSheetIds {
	private DBSelect select;
	private Logger logger =	 Logger.getLogger("com.wssc.filenet.migration.intranet");
	private Properties props;

	/**
	 * Constructor for a DBSelect class.
	 */
	public GetSheetIds() {
		super();
		props = PropertyReader.getInstance().getPropertyBag();
		initializer();
	}

	/**
	 * Creates a DBSelect instance and initializes its properties.
	 */
	protected void initializer() {
		select = new DBSelect();
		try {
			select.setDataSourceName(props.getProperty("datasource"));
			
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Executes the SQL statement.
	 */
	public void execute(String minX, String minY, String maxX, String maxY) throws SQLException {
		try {
			String query = "SELECT DISTINCT SHEET_ID FROM GRDWSSC91O WHERE (" +
			"((EMINX BETWEEN "+minX+" AND "+maxX+") AND (EMINY BETWEEN "+minY+" AND "+maxY+"))" +
			" OR ((EMAXX BETWEEN "+minX+" AND "+maxX+") AND (EMAXY BETWEEN "+minY+" AND "+maxY+")))" +
			" UNION SELECT DISTINCT SHEET_ID FROM GRDWSSC91O WHERE (" +
			"(( "+minX+" BETWEEN EMINX AND EMAXX) AND ( "+minY+" BETWEEN EMINY AND EMAXY))" +
			" OR (( "+minX+" BETWEEN EMINX AND EMAXX) AND ( "+maxY+" BETWEEN EMINY AND EMAXY)) " +
			" OR (( "+maxX+" BETWEEN EMINX AND EMAXX) AND ( "+maxY+" BETWEEN EMINY AND EMAXY)) " +
			" OR (( "+maxX+" BETWEEN EMINX AND EMAXX) AND ( "+minY+" BETWEEN EMINY AND EMAXY))" +
			" )";
			
			System.out.println(query);
			select.setCommand(query);
			select.execute();
			
		}

		// Free resources of select object.
		finally {
			select.close();
		}
	}

	/**
	 * Returns a DBSelect reference.
	 */
	public DBSelect getDBSelect() {
		return select;
	}

	/**
	 * Returns an array of objects representing the rows in the result set.
	 */
	public GetSheetIdsRow[] getRows() {
		GetSheetIdsRow[] rows = new GetSheetIdsRow[select.getRowCount()];
		for (int i = 0; i <= select.getRowCount() - 1; i++) {
			rows[i] = new GetSheetIdsRow(select, i + 1);
		}
		return rows;
	}
}