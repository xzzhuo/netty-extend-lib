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

	void onBeforeCreateTable();

	/**
	 * Create a new database override
	 * @param db the new database parameter
	 * @see NetDatabase
	 */
	boolean onCreateTable(NetDatabase db);
	
	/**
	 * After create table callback
	 * @param isCreate The flag of indicate whether create table success, true means succeed, false means failed.
	 * @param isUpgrade The flag of indicate whether update state, -1 means not need upgrade, 0 means succeed, 1 means failed.
	 */
	void onAfterCreateTable(boolean isCreate, int isUpgrade);
}
