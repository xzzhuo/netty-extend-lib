package exhi.net.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class ExcelUtils {

	/**
     * 读取Excel的内容，第一维数组存储的是一行中格列的值，二维数组存储的是多少个行
     * @param file 读取数据的源Excel
     * @param ignoreRows 读取数据忽略的行数，比喻行头不需要读入 忽略的行数为1
     * @return 读出的Excel中数据的内容
     * @throws FileNotFoundException
     * @throws IOException
     */
	public static String[][] importData(File file, int sheetIndex, int ignoreRows)
           throws FileNotFoundException, IOException {
       List<String[]> result = new ArrayList<String[]>();
       int rowSize = 0;
       BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
       // 打开HSSFWorkbook
       POIFSFileSystem fs = new POIFSFileSystem(in);
       HSSFWorkbook wb = new HSSFWorkbook(fs);
       //wb.setSheetName(sheetCount, sheetName , HSSFWorkbook.ENCODING_UTF_16);
       HSSFCell cell = null;
       
       if (sheetIndex >= wb.getNumberOfSheets())
       {
    	   wb.close();
    	   in.close();
    	   return null;
       }

	   HSSFSheet st = wb.getSheetAt(sheetIndex);
       // 第 ignoreRows 不取
	   for (int rowIndex = ignoreRows; rowIndex <= st.getLastRowNum(); rowIndex++)
	   {
		   HSSFRow row = st.getRow(rowIndex);
		   if (row == null) {
			   continue;
		   }
		   int tempRowSize = row.getLastCellNum() + 1;
		   if (tempRowSize > rowSize) {
			   rowSize = tempRowSize;
		   }
		   String[] values = new String[rowSize];
		   Arrays.fill(values, "");
		   boolean hasValue = false;
		   for (int columnIndex = 0; columnIndex <= row.getLastCellNum(); columnIndex++) {
			   String value = "";
			   try
			   {
				   cell = row.getCell(columnIndex);
				   if (cell != null) {
					   // 注意：一定要设成这个，否则可能会出现乱码
					   //cel
					   //cell.setEncoding(HSSFCell.ENCODING_UTF_16);
					   switch (cell.getCellType()) {
					   case HSSFCell.CELL_TYPE_STRING:
						   value = cell.getStringCellValue();
						   break;
					   case HSSFCell.CELL_TYPE_NUMERIC:
						   if (HSSFDateUtil.isCellDateFormatted(cell)) {
							   Date date = cell.getDateCellValue();
							   if (date != null) {
								   value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
							   } else {
								   value = "";
							   }
						   } else {
							   value = new DecimalFormat("0").format(cell
									   .getNumericCellValue());
						   }
						   break;
					   case HSSFCell.CELL_TYPE_FORMULA:
						   // 导入时如果为公式生成的数据则无值
						   try
						   {
							   if (!cell.getStringCellValue().equals("")) {
								   value = cell.getStringCellValue();
								   } else {
									   value = cell.getNumericCellValue() + "";
								   }
						   }
						   catch(Exception e)
						   {
							   double d = cell.getNumericCellValue();
							   value = String.format("%f", d);
						   }
							   
						   break;
					   case HSSFCell.CELL_TYPE_BLANK:
						   break;
					   case HSSFCell.CELL_TYPE_ERROR:
						   value = "";
						   break;
					   case HSSFCell.CELL_TYPE_BOOLEAN:
						   value = (cell.getBooleanCellValue() == true ? "Y" : "N");
						   break;
					   default:
						   value = "";
					   }
				   }
				   values[columnIndex] = rightTrim(value);
				   hasValue = true;
			   }
			   catch(Exception e)
			   {
				   hasValue = false;
			   }
		   }

		   if (hasValue) {
			   result.add(values);
		   }
	   }
       wb.close();
       in.close();
       String[][] returnArray = new String[result.size()][rowSize];
       for (int i = 0; i < returnArray.length; i++) {
           returnArray[i] = (String[]) result.get(i);
       }
       return returnArray;
    }

	/**
     * 去掉字符串右边的空格
     * @param str 要处理的字符串
     * @return 处理后的字符串
	*/
	public static String rightTrim(String str) {
		if (str == null) {
			return "";
		}
		int length = str.length();
		for (int i = length - 1; i >= 0; i--) {
			if (str.charAt(i) != 0x20) {
				break;
			}
			length--;
		}
		return str.substring(0, length);
	}
	
	public static void exportExecel(String fileName, String sheetTitle, List<Map<String, Object>> listMap) throws ExcelException
	{
		exportExecel(fileName, sheetTitle, listMap, null);
	}
	
	public static void exportExecel(String fileName, String sheetTitle, List<Map<String, Object>> listMap, SimpleDateFormat format) throws ExcelException
	{
		if (listMap == null || listMap.size() == 0)
		{
			throw new ExcelException("no data");
		}
		
		// 第一步，创建一个webbook，对应一个Excel文件  
		HSSFWorkbook wb = new HSSFWorkbook();  
		// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet  
		HSSFSheet sheet = wb.createSheet(sheetTitle);  
		// 第三步，在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short  
		HSSFRow row = sheet.createRow((int) 0);  
		// 第四步，创建单元格，并设置值表头 设置表头居中  
		HSSFCellStyle style = wb.createCellStyle();  
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT); // 创建一个居左格式  

		HSSFRow rowTitle = null;
		HSSFCell cell = null;
		int cols = 0;
		int j = 0;
		
		Map<String, Object> map = listMap.get(0);
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			cell = row.createCell(j++);
			cell.setCellValue(entry.getKey());
			cell.setCellStyle(style);
			cols++;
		}
		rowTitle = row;
		
		for (int i = 0; i < listMap.size(); i++)
		{
			map = listMap.get(i);
			row = sheet.createRow((int) i+1);
			for(j=0; j<cols; j++)
			{
				String title = rowTitle.getCell(j).getStringCellValue();
				cell = row.createCell(j);
				if (map.containsKey(title) && map.get(title) != null)
				{
					if (map.get(title).getClass().equals(Date.class) && format != null)
					{
						cell.setCellValue(format.format(map.get(title)));
					}
					else
					{
						cell.setCellValue(map.get(title).toString());
					}
				}
				else
				{
					cell.setCellValue("");
				}
				cell.setCellStyle(style);
			}
		}
		
		// 第六步，将文件存到指定位置  
		try  
		{  
			FileOutputStream fout = new FileOutputStream(fileName);  
			wb.write(fout);  
			fout.close();
			wb.close();
		}  
		catch (Exception e)  
		{  
			throw new ExcelException(e.getMessage()); 
		}
	}
}
