package com.alonyx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class MySqlTest {

	public static void main(String[] args) {
		try { 
		    System.out.println("Loading driver...");
		    Class.forName("com.mysql.jdbc.Driver");
		    System.out.println("Driver loaded!");
		} catch (ClassNotFoundException e) {
		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
		} 
		
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
		} finally { 
		    System.out.println("Closing the connection.");
		    if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
		} 
	}

}
