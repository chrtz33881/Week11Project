package projects.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import projects.exception.DbException;

public class DbConnection {
	
	/*
	 * Setting the constants to the correct values
	 */
	private static final String SCHEMA = "projects";
	private static final String USER = "projects";
	private static final String PASSWORD = "projects";
	private static final String HOST = "localhost";
	private static final int PORT = 3306;

	/*
	 * String variable named uri that contains the MySQL connection URI
	 */
	public static Connection getConnection() {
		String url = String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", HOST, PORT, SCHEMA, USER, PASSWORD);
		
		System.out.println("Connecting with url=" + url);
		
		/*
		 * Call DriverManager to obtain a connection. Pass the connection string (URI) to DriverManager.getConnection() surrounded by a try/catch block.
		 */
		try {
			Connection conn = DriverManager.getConnection(url);
			/*
			 * successful connection
			 */
			System.out.println("Successfully obtained connection!");
			return conn;
		} catch (SQLException e) {
			/*
			 * connection failed prompt
			 */
			System.out.println("Error getting connection.");
			throw new DbException(e);
		}
	}


}
