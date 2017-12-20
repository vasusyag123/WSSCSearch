package com.wssc.filenet.migration.dao;

import java.sql.*;
import com.ibm.db.beans.*;

/**
 * This class represents a specific row of a result set 
 * contained in a DBSelect.
 * Generated:  Jun 4, 2009 1:06:21 PM
 */

public class GetSheetIdsRow {
	private int rowNumber;

	private DBSelect select;

	/**
	 * Constructs an object that represents a row from a DBSelect.
	 */
	public GetSheetIdsRow(DBSelect aRef, int aRowNumber) {
		select = aRef;
		rowNumber = aRowNumber;
	}

	/**
	 * Returns the value of column GRDWSSC91O_SHEET_ID in the row represented by this object.
	 */
	public Object getGRDWSSC91O_SHEET_ID() throws SQLException {
		return select.getCacheValueAt(rowNumber, 1);
	}

	/**
	 * Returns a String that contains all of the values in the row represented by this object.
	 */
	public String toString() {
		String string = "";
		try {
			for (int i = 1; i <= select.getColumnCount(); i++) {
				string += select.getCacheValueAt(rowNumber, i);
				string += "  ";
			}
		} catch (SQLException ex) {
			return null;
		}
		return string;
	}
}