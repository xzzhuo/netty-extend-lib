/**
 * Author: xiaozhao
 */

package exhi.net.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import exhi.net.constant.NetConstant;
import exhi.net.log.BFCLog;

/**
 * Table manage of database
 * @author XiaoZhao
 *
 */
public abstract class NetTable implements INetTable {

	private DatabaseParam mParam = null;
	private String mTableName = "";
	private int mOldVersion = 1;
	protected int mVersion = 1;
	private NetDatabase mDatabase = null;
	private int mTakeOver = 0;

	/**
	 * Structure a new net table with parameter and version, if old version not equal version, it will automatic upgrade the database,
	 * eg: tableName=table, oldVersion=1, version=2, if exist table1 and table2 is empty, it will copy table1 filed to table2 if table2 exist that filed
	 * @param param database parameter
	 * @param tableName input table name
	 * @param oldVersion old database version
	 * @param version database version
	 * @see DatabaseParam
	 */
	public NetTable(DatabaseParam param, String tableName, int oldVersion, int version)
	{
		this.mParam = param;
		
		this.mTableName 	= tableName;
		this.mOldVersion 	= oldVersion;
		this.mVersion 		= version;
	}

	/**
	 * Upgrade table old version to new version
	 * @param fields The fields of target table
	 * @return Return true if upgrade success, or return false
	 */
	private boolean UpgradeTable(List<String> fields)
	{
		String fieldString = fields.toString();
		fieldString = fieldString.substring(1, fieldString.length()-1);
		String sql = String.format("INSERT INTO %s(%s) SELECT %s FROM %s%d",
				this.getTableName(), fieldString, fieldString, this.mTableName, this.mOldVersion);
		
		boolean result = this.insert(sql);
		BFCLog.debug(NetConstant.Database, this.getTableName() + " upgrade " + (result?"success":"failed"));
		
		return result;
	}
	
	/**
	 * Get the new table name with version
	 * formater: String.format("%s%d", this.mTableName, this.mVersion)
	 * @return return the table name with version, eg: table name is table, version is 1, then return table1
	 */
	public String getTableName()
	{
		return String.format("%s%d", this.mTableName, this.mVersion);
	}
	
	/**
	 * Get a net database handler and enable database
	 * @return return database handler
	 */
	private NetDatabase getEnableDatabase()
	{
		if (mDatabase == null)
		{
			mDatabase = new NetDatabase();
			if (!mDatabase.connect(this.mParam))
			{
				BFCLog.error(NetConstant.Database, "Connect database failed.");
				mDatabase = null;
				return null;
			}
		}
		mTakeOver++;

		if (!mDatabase.isExist(this.getTableName()))
		{
			BFCLog.info(NetConstant.Database, this.getTableName() + " is not exist, and create it");
			
			this.onBeforeCreateTable();
			boolean bIsCreate = this.onCreateTable(mDatabase);
			int nUpgrade = -1;

			String oldTableName = String.format("%s%d", this.mTableName, this.mOldVersion);
			if (this.mOldVersion != this.mVersion && 
					mDatabase.isExist(this.getTableName()) &&
					mDatabase.isExist(oldTableName) &&
					getCount(oldTableName) > 0)
			{
				if (getCount() == 0)
				{
					List<String> fields1 = this.getFields(oldTableName);
					List<String> fields2 = this.getFields(this.getTableName());
					
					List<String> fields = new ArrayList<String>();
					for (String f: fields1)
					{
						if (fields2.indexOf(f) >= 0)
						{
							fields.add(f);
						}
					}
					
					nUpgrade = (true == this.UpgradeTable(fields) ? 0 : 1);
				}
				else
				{
					BFCLog.error(NetConstant.Database, "Upgrade failed, table '" + this.getTableName() + "' is not empty");
				}
			}
			
			this.onAfterCreateTable(bIsCreate, nUpgrade);
		}
		
		return mDatabase;
	}

	/**
	 * Disconnect the database
	 */
	private void Disconnect()
	{
		mTakeOver--;
		
		// MyLog.debug("TakeOver release", String.format("mTakeOver = %d", mTakeOver));
		if (mTakeOver == 0)
		{
			mDatabase.disconnect();
			mDatabase = null;
		}
	}
	
	/**
	 * Executes the given SQL statement, which returns all set value.
	 * @param sql SQL statement
	 * @return Return all set value
	 */
	public List<Map<String, Object>> query(String sql)
	{
		NetDatabase db = this.getEnableDatabase();
		if (db == null)
		{
			return null;
		}
		List<Map<String, Object>> retval = db.query(sql);
		
		this.Disconnect();
		return retval;
	}
	
	/**
	 * Executes the given set array, which returns the set values by appoint sets.
	 * @param sets Given the set array
	 * @return return the set values by appoint sets
	 */
	public List<Map<String, Object>> query(String[] sets)
	{
		String select = "";
		for (String str : sets)
		{
			select += str + ",";
		}
		if (select.length() > 0)
		{
			select = select.substring(0, select.length()-1);
		}
		String sql = String.format("SELECT %s FROM %s", select, this.getTableName());
		return this.query(sql);
	}
	
	/**
	 * Executes the given set list, which returns the set values by appoint sets.
	 * @param list Given the set list
	 * @return return the set values by appoint sets
	 */
	public List<Map<String, Object>> query(List<String> list)
	{
		String select = list.toString();
		select = select.substring(1, select.length()-1);
		String sql = String.format("SELECT %s FROM %s", select, this.getTableName());
		return this.query(sql);
	}
	
	/**
	 * Executes the given SQL statement, which insert new set value.
	 * @param sql The parameter of sql statement
	 * @return Insert success return true, or return false
	 */
	public boolean insert(String sql)
	{
		NetDatabase db = this.getEnableDatabase();
		if (db == null)
		{
			return false;
		}
		
		boolean retval = db.executeUpdate(sql);
		
		this.Disconnect();
		return retval;
	}

	/**
	 * Executes the given SQL statement, which update database.
	 * @param sql The parameter of sql statement
	 * @return Update success return true, or return false
	 */
	public boolean update(String sql)
	{
		NetDatabase db = this.getEnableDatabase();
		if (db == null)
		{
			return false;
		}
		
		boolean retval = db.executeUpdate(sql);
		
		this.Disconnect();
		return retval;
	}
	
	/**
	 * Execute delete Executes the given SQL statement. 
	 * @param sql The parameter of sql statement
	 * @return Delete success return true, or return false
	 */
	public boolean delete(String sql)
	{
		NetDatabase db = this.getEnableDatabase();
		if (db == null)
		{
			return false;
		}
		boolean retval = db.executeUpdate(sql);
		
		this.Disconnect();
		return retval;
	}
	
	/**
	 * Make database keep connect status
	 * @see #releaseDatabase()
	 */
	public void holdDatabase()
	{
		this.getEnableDatabase();
	}
	
	/**
	 * Disconnect database
	 * @see #holdDatabase()
	 */
	public void releaseDatabase()
	{
		this.Disconnect();
	}

	protected void finalize()
	{
		if (mDatabase != null)
		{
			mDatabase.disconnect();
		}
		try {
			super.finalize();
		} catch (Throwable e) {
		}
	}
	
	/**
	 * Get the count of all sets
	 * @return return the field count of all sets
	 */
	public long getCount()
	{
		return getCount(this.getTableName());
	}
	
	/**
	 * Get the count of given the table
	 * @param tableName The table name
	 * @return return the filed count of given table name
	 */
	private long getCount(String tableName)
	{
		long result = 0;
		
		String query = String.format("SELECT count(*) FROM %s", tableName);
		List<Map<String, Object>> list = this.query(query);
		
		if (list != null && list.size() == 1)
		{
			String val = String.valueOf(list.get(0).get("count(*)"));
			result = Long.valueOf(val).longValue();
		}
		
		return result;
	}

	/**
	 * get the fields of all sets.
	 * @return return the fields of all sets
	 */
	public List<String> getFields()
	{
		return this.getFields(this.getTableName());
	}
	
	/**
	 * get the fields of all sets.
	 * @param table Table name
	 * @return return the fields of all sets
	 */
	private List<String> getFields(String table)
	{
		NetDatabase db = this.getEnableDatabase();
		if (db == null)
		{
			return null;
		}
		List<String> retval = db.getFields(table);
		
		this.Disconnect();
		return retval;
	}
}
