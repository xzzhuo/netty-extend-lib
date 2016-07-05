/**
 * Author: xiaozhao
 */

package exhi.net.database;

public class DatabaseParam {

	private String mUrl = "localhost";
	private int mPort = 3306;
	private String mDatabaseName = "database";
	private String mUser = "root";
	private String mPassword = "root";
	
	public String getUrl() {
		return mUrl;
	}
	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}
	public int getPort() {
		return mPort;
	}
	public void setPort(int mPort) {
		this.mPort = mPort;
	}
	public String getDatabaseName() {
		return mDatabaseName;
	}
	public void setDatabaseName(String mDatabaseName) {
		this.mDatabaseName = mDatabaseName;
	}
	public String getUser() {
		return mUser;
	}
	public void setUser(String mUser) {
		this.mUser = mUser;
	}
	public String getPassword() {
		return mPassword;
	}
	public void setPassword(String mPassword) {
		this.mPassword = mPassword;
	}
}
