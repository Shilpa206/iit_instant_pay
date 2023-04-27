package models;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class DbCredentials {
	public static final String DB_PROPERTIES_FILE = "DB_PROPERTIES_FILE";
	
	private String host;
	private Short port;
	private String schema;
	private String connOptions;
	private String username;
	private String password;
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Short getPort() {
		return port;
	}
	public void setPort(Short port) {
		this.port = port;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getConnOptions() {
		return connOptions;
	}
	public void setConnOptions(String connOptions) {
		this.connOptions = connOptions;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public DbCredentials(String host, short port, String schema, String connOptions, String username, String password) {
		super();
		this.host = host;
		this.port = port;
		this.schema = schema;
		this.connOptions = connOptions;
		this.username = username;
		this.password = password;
	}
	
	public DbCredentials() {
		super();
	}
	
	/*
	 * Reads properties file whose path is specified in DB_PROPERTIES_FILE environment file and return DbCredentials object
	 */
	public static DbCredentials getCredentials() throws FileNotFoundException, IOException {
		DbCredentials dbCredentials = new DbCredentials();
		
		String propertiesFilePath = System.getenv(DbCredentials.DB_PROPERTIES_FILE); 
		
		if(propertiesFilePath == null) {
			throw new IllegalArgumentException("Databse properties file not found. Set: " + DB_PROPERTIES_FILE + " environment variable.");
		}
		
		Properties prop = new Properties();
		
		prop.load(new FileInputStream(propertiesFilePath));
		
		dbCredentials.setHost(prop.getProperty("host"));
		dbCredentials.setPort(Short.parseShort(prop.getProperty("port")));
		dbCredentials.setSchema(prop.getProperty("schema"));
		dbCredentials.setConnOptions(prop.getProperty("connOptions"));
		dbCredentials.setUsername(prop.getProperty("username"));
		dbCredentials.setPassword(prop.getProperty("password"));
		
		return dbCredentials;
	}
}

