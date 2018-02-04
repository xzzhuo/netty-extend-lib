/**
 * Author: xiaozhao
 */

package exhi.net.database;

/*
 * The database configuration parameter
 */
public class DatabaseParam {

	private String mUrl = "localhost";
	private int mPort = 3306;
	private String mDatabaseName = "database";
	private String mUser = "root";
	private String mPassword = "root";
	
	/**
	 * get database url
	 * @return return the database url
	 */
	public String getUrl() {
		return mUrl;
	}
	
	/**
	 * update a new database url, default is "localhost"
	 * @param url the new database url
	 */
	public void setUrl(String url) {
		this.mUrl = url;
	}
	
	/**
	 * get database port
	 * @return return the database port
	 */
	public int getPort() {
		return mPort;
	}
	
	/**
	 * update a new database port, default is "3306"
	 * @param port the new port
	 */
	public void setPort(int port) {
		this.mPort = port;
	}
	
	/**
	 * get the database name
	 * @return return the database name
	 */
	public String getDatabaseName() {
		return mDatabaseName;
	}
	
	/**
	 * set a new database, default is "database"
	 * @param databaseName the new database name
	 */
	public void setDatabaseName(String databaseName) {
		this.mDatabaseName = databaseName;
	}
	
	/**
	 * get user name
	 * @return return the user name
	 */
	public String getUser() {
		return mUser;
	}
	
	/**
	 * update a new user name, default is "root"
	 * @param user the new user name
	 */
	public void setUser(String user) {
		this.mUser = user;
	}
	
	/**
	 * get password
	 * @return return the password
	 */
	public String getPassword() {
		return mPassword;
	}
	
	/**
	 * update a new password, default is "root"
	 * @param password the new password
	 */
	public void setPassword(String password) {
		this.mPassword = password;
	}
}
