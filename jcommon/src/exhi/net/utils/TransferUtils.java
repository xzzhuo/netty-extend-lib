/**
 * Author: xiaozhao
 */

package exhi.net.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TransferUtils {

	/**
     * transfer mapping data to object, must transfer all map data to object property, otherwise throw exception 
     * @param map mapping data
     * @param object Object
     * @param format Data format
     * @return get a object
	 * @throws TransferException 
     */
	public static <T> T transferMap2Bean(Map<String, Object> map, Class<T> classOfT, DateFormat format)
			throws TransferException {
		
		if (map == null) {
			throw new TransferException("map data is null");  
		}
		
		T object = null;
		try {
			object = classOfT.newInstance();
		} catch (Exception e) {
			throw new TransferException(e.getMessage(), e.getCause());
		}
		
		if (object == null) {
			throw new TransferException("object is null"); 
		}
		
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());  
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();  
		
			for (PropertyDescriptor property : propertyDescriptors) {  
				String key = property.getName();
		
				if (map.containsKey(key)) {
					Object value = map.get(key);
					Method setter = property.getWriteMethod();

					if (property.getPropertyType().equals(value.getClass()))
					{
						setter.invoke(object, value);
					}
					else
					{
						String str = value.toString();
						if (property.getPropertyType().equals(String.class))
						{
							setter.invoke(object, str);
						}
						else if (property.getPropertyType().equals(Date.class))
						{
							setter.invoke(object, format.parse(str));
						}
						else if (property.getPropertyType().equals(char.class) || property.getPropertyType().equals(Character.class))
						{
							setter.invoke(object, str.charAt(0));
						}
						else if (property.getPropertyType().equals(int.class) || property.getPropertyType().equals(Integer.class))
						{
							setter.invoke(object, Integer.parseInt(str));
						}
						else if (property.getPropertyType().equals(double.class) || property.getPropertyType().equals(Double.class))
						{
							setter.invoke(object, Double.parseDouble(str));
						}
						else if (property.getPropertyType().equals(long.class) || property.getPropertyType().equals(Long.class))
						{
							setter.invoke(object, Long.parseLong(str));
						}
						else if (property.getPropertyType().equals(short.class) || property.getPropertyType().equals(Short.class))
						{
							setter.invoke(object, Short.parseShort(str));
						}
						else
						{
							throw new Exception(property.getPropertyType().toString());
						}
					}
				}
			}
		} catch (Exception e) {  
			throw new TransferException("transferMap2Bean2 Error: " + e, e.getCause());
		}
		
		return object;
	}

	/**
     * transfer mapping data to object, must transfer all map data to object property, otherwise throw exception 
     * @param map mapping data
     * @param object Object
     * @return get a object
	 * @throws TransferException 
     */
	public static <T> T transferMap2Bean(Map<String, Object> map, Class<T> classOfT)
			throws TransferException {
		
		return transferMap2Bean(map, classOfT, DateFormat.getDateInstance(DateFormat.DEFAULT));
	}

	/**
     * transfer object to mapping data
     * @param map mapping data
     * @param object Object
     * @return get a object
	 * @throws TransferException 
     */
	public static Map<String, Object> transferBean2Map(Object object) throws TransferException {  
		
		if(object == null) {
			throw new TransferException("object is null"); 
		}          
		Map<String, Object> map = new HashMap<String, Object>();  
		try {  
			BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());  
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();  
			for (PropertyDescriptor property : propertyDescriptors) {  
				String key = property.getName();  
				if (!key.equals("class")) {  

					Method getter = property.getReadMethod();  
					Object value = getter.invoke(object);  
					map.put(key, value);  
				}  
			}  
		} catch (Exception e) {
			throw new TransferException("transferBean2Map Error: " + e, e.getCause());
		}  
		return map;  
	}
}