/**
 * Author: xiaozhao
 */

package exhi.net.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exhi.net.constant.NetConstant;
import exhi.net.log.BFCLog;

/**
 * Database engineer
 * @author XiaoZhao
 *
 */
public class NetDatabase {

	private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_VERSION = "version";
	private static final String DB_VALUE = "Value";
	
	private Connection mConnect = null;

	/**
	 * The constructor of NetDatabase class, use for database manage
	 */
	public NetDatabase()
	{
		
	}
	
	/**
	 * Connect the database with new database parameter
	 * eg: String.Format("jdbc:mysql://%s:%d/%s", url, port, database);
	 * @param param the database parameter
	 * @return Connect success return true, or return false
	 * @see DatabaseParam
	 */
	public boolean connect(DatabaseParam param)
	{
		boolean result = false;
		String url = null;
		
		try {
			Class.forName(DB_DRIVER).newInstance();
			result = true;
		} catch (ClassNotFoundException e) {
			BFCLog.error(NetConstant.Database, "Exception: " + e.getMessage());
		} catch (InstantiationException e) {
			BFCLog.error(NetConstant.Database, "Exception: " + e.getMessage());
		} catch (IllegalAccessException e) {
			BFCLog.error(NetConstant.Database, "Exception: " + e.getMessage());
		}
		
		if (result)
		{
			result = false;
			try {
				url = String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8",
						param.getUrl(), param.getPort(), param.getDatabaseName());
				this.mConnect = DriverManager.getConnection(url, param.getUser(), param.getPassword());
				result = !this.mConnect.isClosed();
			} catch (SQLException e) {
				url = String.format("jdbc:mysql://%s:%d/%s", param.getUrl(), param.getPort(), param.getDatabaseName());
				BFCLog.error(NetConstant.Database, "Can't connect database: '" + url + "' Exception: " + e.getMessage());
			}
		}
		
		if (result)
		{
			url = String.format("jdbc:mysql://%s:%d/%s", param.getUrl(), param.getPort(), param.getDatabaseName());
			BFCLog.debug(NetConstant.Database, "Connect database success: '" + url);
		}
		
		return result;
	}
	
	/**
	 * Disconnect the current database
	 */
	public void disconnect()
	{
		try {
			if (this.mConnect != null && !this.mConnect.isClosed())
			{
				this.mConnect.close();
				BFCLog.debug(NetConstant.Database, "Disconnect database success");
			}
		} catch (SQLException e) {
			BFCLog.warning(NetConstant.Database, "Exception: " + e.getMessage());
		}
	}
	
	/**
	 * Create a new table
	 * @param sql The parameter of sql statement
	 * @return Create success return true, or return false
	 */
	public boolean createTable(String sql)
	{
		if (this.mConnect == null)
		{
			BFCLog.error(NetConstant.Database, "Not connect database");
			return false;
		}
		
		boolean result = false;
		Statement statement = null;
		try {
            statement = this.mConnect.createStatement();
            if (statement != null)
            {
            	int value = statement.executeUpdate(sql);
            	BFCLog.debug(NetConstant.Database, "Statement execute update: " + value);
            	result = true;
            }
        }
		catch(SQLException e)
		{
			BFCLog.error(NetConstant.Database, "Create table failed, Exception: " + e.getMessage());
		}
        finally
        {
            if (statement != null) {
            	try {
					statement.close();
				} catch (SQLException e) {
					BFCLog.warning(NetConstant.Database, "Exception: " + e.getMessage());
				}
            }
        }
		
		return result;
	}
	
	/**
	 * * Execute update
	 * 
	 * Executes the given SQL statement, which may be an INSERT, UPDATE, 
	 * or DELETE statement or an SQL statement that returns nothing, such as an SQL DDL statement
	 * @param sql The parameter of sql statement
	 * @return Update success return true, or return false
	 */
	public boolean executeUpdate(String sql)
	{
		if (this.mConnect == null)
		{
			BFCLog.error(NetConstant.Database, "Not connect database");
			return false;
		}
		
		boolean result = false;
		Statement statement = null;
		try {
            statement = this.mConnect.createStatement();
            if (statement != null)
            {
            	int value = statement.executeUpdate(sql);
            	BFCLog.debug(NetConstant.Database, "Statement execute update: " + value);
            	result = true;
            }
        }
		catch(SQLException e)
		{
			BFCLog.error(NetConstant.Database, "execute update failed, Exception: " + e.getMessage());
		}
        finally
        {
            if (statement != null) {
            	try {
					statement.close();
				} catch (SQLException e) {
					BFCLog.warning(NetConstant.Database, "Exception: " + e.getMessage());
				}
            }
        }
		
		return result;
	}

	private static Map<String, Object> getResultMap(ResultSet rs) throws SQLException {
		
        Map<String, Object> map = new HashMap<String, Object>();
        ResultSetMetaData rsmd = rs.getMetaData();  
        int count = rsmd.getColumnCount();  
        for (int i = 1; i <= count; i++) {  
            String key = rsmd.getColumnLabel(i);  
            Object value = rs.getObject(i);  
            map.put(key, value);  
        }
        return map;  
	} 
	
	/**
	 * Query records
	 * eg: SELECT * FROM table
	 * @param sql The parameter of sql statement
	 * @return return the list of mapping data
	 */
	public List<Map<String, Object>> query(String sql)
	{
		if (this.mConnect == null)
		{
			BFCLog.error(NetConstant.Database, "Not connect database");
			return null;
		}
		
		List<Map<String, Object>> result = null;
		Statement statement = null;
		try {
            statement = this.mConnect.createStatement();
            if (statement != null)
            {
            	ResultSet rs = statement.executeQuery(sql);
    			if (rs != null)
    			{
    				result = new ArrayList<Map<String, Object>>();
    				boolean bMove = rs.first();
    				while (bMove)
    				{
    					result.add(getResultMap(rs));
    					
    					bMove = rs.next();
    				}
    				rs.close();
    			}
    			else
    			{
    				BFCLog.debug(NetConstant.Database, "Execute Query return null");
    			}
            }
        }
		catch(SQLException e)
		{
			BFCLog.error(NetConstant.Database, "Execute Query failed, Exception: " + e.getMessage());
		}
        finally
        {
        	if (statement != null) {
            	try {
					statement.close();
				} catch (SQLException e) {
					BFCLog.warning(NetConstant.Database, "Exception: " + e.getMessage());
				}
            }
        }
		
		return result;
	}
	
	/**
	 * Get whether exist a table
	 * 
	 * @param table Given the table name
	 * @return Return the true if exist the table by given the table name, othrwise return false
	 */
	boolean isExist(String table)
	{
		if (this.mConnect == null)
		{
			BFCLog.error(NetConstant.Database, "Not connect the database.");
			return false;
		}
		
		boolean retval = false;
		ResultSet rs;
		try {
			rs = this.mConnect.getMetaData().getTables(null, null, table, null);
			if (rs.next()) {  
				retval = true;
			}
		} catch (SQLException e) {
			BFCLog.error(NetConstant.Database, "Get meta data failed, Exception: " + e.getMessage());
		} catch (Exception e) {
			BFCLog.error(NetConstant.Database, "Get meta data failed, Exception: " + e.getMessage());
		}
		
		return retval;
	}
	
	/**
	 * Get table fields list
	 * 
	 * @param table Given the table name
	 * @return Return the field list by given table name
	 */
	public List<String> getFields(String table)
	{
		if (this.mConnect == null)
		{
			BFCLog.error(NetConstant.Database, "Not connect database");
			return null;
		}
		
		List<String> result = null;
		Statement statement = null;
		try {
            statement = this.mConnect.createStatement();
            if (statement != null)
            {
            	String sql = String.format("SELECT * FROM %s", table);
            	ResultSet rs = statement.executeQuery(sql);
    			if (rs != null)
    			{
    				result = new ArrayList<String>();
    				ResultSetMetaData rsmd = rs.getMetaData();  
			        
					for (int i = 1; i <= rsmd.getColumnCount(); i++)
					{
						result.add(rsmd.getColumnName(i));
					}
    				rs.close();
    			}
    			else
    			{
    				BFCLog.debug(NetConstant.Database, "Execute Query return null");
    			}
            }
        }
		catch(SQLException e)
		{
			BFCLog.error(NetConstant.Database, "Execute Query failed, Exception: " + e.getMessage());
		}
        finally
        {
        	if (statement != null) {
            	try {
					statement.close();
				} catch (SQLException e) {
					BFCLog.warning(NetConstant.Database, "Exception: " + e.getMessage());
				}
            }
        }
		
		return result;
	}
	
	/**
	 * Get database engineer version, eg: "5.7.9"
	 * @return return database engineer version
	 */
	public String getDatabaseVersion()
	{
		String version = "";
		
		String sql = String.format("SHOW VARIABLES LIKE '%s'", DB_VERSION);
		List<Map<String, Object>> list = this.query(sql);
		
		if (list.size() == 1)
		{
			Object val = list.get(0).get(DB_VALUE);
			if (val != null)
			{
				version = val.toString();
			}
		}
		
		return version;
	}
}
