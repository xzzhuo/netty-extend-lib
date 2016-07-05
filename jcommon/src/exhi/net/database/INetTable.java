/**
 * Author: xiaozhao
 */

package exhi.net.database;

public interface INetTable {

	String getTableName();
	void onCreateTable(NetDatabase db);
	void onInitTable();
}
