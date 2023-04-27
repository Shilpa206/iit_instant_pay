package models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * Class for connecting to database using JDBC
 */
public class DBConnect {
	// Return connection to database
	public Connection getConnection(String protocol, String subprotocol, String url, String username, String password)
			throws SQLException {

		String jdbcConnString = protocol + ":" + subprotocol + "://" + url;
		return DriverManager.getConnection(jdbcConnString, username, password);
	}
}
