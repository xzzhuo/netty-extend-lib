package exhi.net.utils;

import java.util.Map;

class MapHolder 
{
	public String field = "";
	public String value = "";
}

public class DbUtils {

	/**
	 * get set fields and value fields
	 * @param map insert data
	 * @return return map holder object
	 */
	private static MapHolder getMapHolder(Map<String, Object> map)
	{
		MapHolder holder = new MapHolder();
		
		String field = "";
		String value = "";
		
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() != null)
			{
				field += entry.getKey() + ",";
				value += "'" + entry.getValue().toString() + "',";
			}
		}
		
		if (field.endsWith(","))
		{
			field = field.substring(0, field.length()-1);
		}
		
		if (value.endsWith(","))
		{
			value = value.substring(0, value.length()-1);
		}
		
		holder.field = field;
		holder.value = value;
		
		return holder;
	}
	
	/**
	 * Get insert sql string by the map data
	 * 
	 * @param table the name of table
	 * @param map the fileds and values
	 * 
	 * @return return the insert sql string
	 */
	public static String getInsertSql(String table, Map<String, Object> map)
	{
		MapHolder holder = DbUtils.getMapHolder(map);
		
		String returnValue = "INSERT INTO " + table + " (" + holder.field + ") VALUES (" + holder.value + ")";
		
		return returnValue;
	}
	
	/**
	 * Get update sql string by the map data
	 * 
	 * @param table the name of table
	 * @param map the fileds and values
	 * @param where the where sql string
	 * 
	 * @return return the update sql string
	 */
	public static String getUpdateSql(String table, Map<String, Object> map, String where)
	{
		String value = "";
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() != null)
			{
				value += entry.getKey() + "='" + entry.getValue().toString() + "',";
			}
		}
		
		if (value.endsWith(","))
		{
			value = value.substring(0, value.length()-1);
		}
		
		String returnValue = "UPDATE " + table + " SET " + value;
		if (where != null && !where.isEmpty())
		{
			returnValue += " WHERE " + where;
		}
		
		return returnValue;
	}
	
	/**
	 * Get delete sql string
	 * 
	 * @param table the name of table
	 * @param where the where sql string
	 * 
	 * @return return the delete sql string
	 */
	public static String getDeleteSql(String table, String where)
	{
		String returnValue = "DELETE FROM " + table;
		if (where != null && !where.isEmpty())
		{
			returnValue += " WHERE " + where;
		}
		
		return returnValue;
	}
}
