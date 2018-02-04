/**
 * Author: xiaozhao
 */

package exhi.net.database;

public interface INetTable {

	/**
	 * get the table name
	 * @return return the table name
	 */
	String getTableName();
	
	/**
	 * Create a new database
	 * @param db the new database parameter
	 * @see NetDatabase
	 */
	void onCreateTable(NetDatabase db);
	
	/**
	 * Initialize a new table (with sql statement)
	 */
	void onInitTable();
}
