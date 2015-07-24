package com.alonyx.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class SqlStore {
	
	public SqlStore() {
		try { 
		    System.out.println("Loading driver...");
		    Class.forName("com.mysql.jdbc.Driver");
		    System.out.println("Driver loaded!");
		} catch (ClassNotFoundException e) {
		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
		} 
	}
	
	public static void saveRecord(String tableName, JSONArray nameArray, JSONArray valArray) {
		Connection connection = null;
		try {
			connection = getConnection();
			StringBuffer sb = new StringBuffer("insert into " + tableName + "(");
			int size = nameArray.size();
			for (int i = 0; i < nameArray.size(); ++i) {
				if (i < (size - 1))
					sb.append(nameArray.get(i) + ",");
				else
					sb.append(nameArray.get(i) + ")");
			}
			sb.append(" values(");

			for (int i = 0; i < size; i++) {
				if (i < (size - 1))
					sb.append("?,");
				else
					sb.append("?)");
			}
			System.out.println(sb.toString());
			PreparedStatement pstmt = connection.prepareStatement(sb.toString());
			bindVariables(valArray, pstmt);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Closing the connection.");
		    if (connection != null) {
		    	try { 
		    		connection.close(); 
		    	} catch (SQLException ignore) {
		    		
		    	}
		    }
		}
	}

	private static void bindVariables(JSONArray valArray, PreparedStatement pstmt) throws SQLException {
		for (int i = 0; i < valArray.size(); ++i) {
			JSONValue value = valArray.get(i);
			JSONString jsonString = value.isString();
			if (null != jsonString) {
				pstmt.setString(i, jsonString.stringValue());
			} else {
				JSONNumber jsonNumber = value.isNumber();
				if (null != jsonNumber) {
					pstmt.setDouble(i, jsonNumber.doubleValue());
				}
				else {
					JSONBoolean jsonBoolean = value.isBoolean();
					if (null != jsonBoolean) {
						pstmt.setBoolean(i, jsonBoolean.booleanValue());
					}
					else {
						System.out.println("Unrecognised value type: " + value.toString());
					}
				}
			}
		}
	}

	private static Connection getConnection() {
		
		String url = "jdbc:mysql://79.170.44.123:3306/";
		String username = "cl28-butter";
		String password = "9tNqMJE9/";
		Connection connection = null;
		try { 
		    System.out.println("Connecting database...");
		    connection = DriverManager.getConnection(url, username, password);
		    System.out.println("Database connected!");
		} catch (SQLException e) { 
		    throw new RuntimeException("Cannot connect the database!", e);
		}
		return connection;
	}
}
