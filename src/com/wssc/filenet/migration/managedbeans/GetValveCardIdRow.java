package com.wssc.filenet.migration.managedbeans;

import java.sql.*;
import com.ibm.db.beans.*;

/**
 * This class represents a specific row of a result set 
 * contained in a DBSelect.
 * Generated:  Mar 15, 2010 2:47:48 PM
 */

public class GetValveCardIdRow {
	private int rowNumber;

	private DBSelect select;

	/**
	 * Constructs an object that represents a row from a DBSelect.
	 */
	public GetValveCardIdRow(DBSelect aRef, int aRowNumber) {
		select = aRef;
		rowNumber = aRowNumber;
	}

	/**
	 * Returns the value of column DOCVERSION_U32_DOCUMENTTITLE in the row represented by this object.
	 */
	public Object getDOCVERSION_U32_DOCUMENTTITLE() throws SQLException {
		return select.getCacheValueAt(rowNumber, 1);
	}

	/**
	 * Returns the value of column DOCVERSION_U64_TITLE in the row represented by this object.
	 */
	public Object getDOCVERSION_U64_TITLE() throws SQLException {
		return select.getCacheValueAt(rowNumber, 2);
	}

	/**
	 * Returns the value of column DOCVERSION_U75_COLLECTIONSTART in the row represented by this object.
	 */
	public Object getDOCVERSION_U75_COLLECTIONSTART() throws SQLException {
		return select.getCacheValueAt(rowNumber, 3);
	}

	/**
	 * Returns the value of column DOCVERSION_U76_COLLECTIONEND in the row represented by this object.
	 */
	public Object getDOCVERSION_U76_COLLECTIONEND() throws SQLException {
		return select.getCacheValueAt(rowNumber, 4);
	}

	/**
	 * Returns the value of column DOCVERSION_U62_STORAGECATEGORY in the row represented by this object.
	 */
	public Object getDOCVERSION_U62_STORAGECATEGORY() throws SQLException {
		return select.getCacheValueAt(rowNumber, 5);
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