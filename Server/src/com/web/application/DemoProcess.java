package com.web.application;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.web.config.Config;
import com.web.data.NotificationItem;
import com.web.data.NotificationTable;

import exhi.net.database.DatabaseParam;
import exhi.net.database.NetTable;
import exhi.net.log.NetLog;
import exhi.net.netty.NetFile;
import exhi.net.netty.NetProcess;
import exhi.net.netty.WebUtil;
import exhi.net.utils.ExcelException;
import exhi.net.utils.ExcelUtils;
import exhi.net.utils.TransferException;
import exhi.net.utils.TransferUtils;

public class DemoProcess extends NetProcess {

	private WebUtil mWebUtil = null;
	
	public DemoProcess()
	{
		mWebUtil = new WebUtil(this);
	}
	
	public Pages calculatePages(NetTable table, final Map<String, String> request)
	{
		Pages pages = new Pages();

		long total_count = table.getCount();
		
		long current = 1;
		long offsets = pages.getOffsets();
		
		try
		{
			if (this.getCookie("demo_page_offsets") != null)
			{
				offsets = Integer.parseInt(this.getCookie("demo_page_offsets"));
			}
			
			if (request.containsKey("current_page"))
			{
				current = Integer.parseInt(request.get("current_page"));
			}
		}
		catch(Exception e)
		{
			// 忽略错误，使用默认值
		}
		
		long count = (total_count+offsets-1)/offsets;
		if (count > 0 && current > count)
		{
			current = count;
		}
		
		this.setCookie("demo_page_offsets", String.format("%d", offsets));
		
		NetLog.debug("Pages", String.format("rows = %d, offsets = %d", (current-1)*offsets, offsets));
		
		pages.setRows((current-1)*offsets);
		pages.setOffsets(offsets);
		pages.setCount(count);
		pages.setCurrent(current);
		
		return pages;
	}
	
	@Override
	protected void onErrorNotFind(final String client, final String uri)
	{
		// this.print("Failure: 404 Not Found");
		
		NetLog.info(client, "uri = " + uri);
		
		String workPath = this.getWorkPath();
		mWebUtil.setTemplatePath(String.format("%s/%s", workPath, Config.instance().getRootFolder()));
		mWebUtil.display("404.html");
	}
	
	@Override
	public void onProcess(final String address, final String path, final Map<String, String> request) {
		
		NetLog.debug(address, "=============================");
		NetLog.debug(address, "Enter DemoProcess - Process()");
		NetLog.debug(address, "Full Path:" + path);
		NetLog.debug(address, "Work Path:" + this.getWorkPath());
		NetLog.debug(address, "Uri = " + this.getUri());
		
		File tempFile = new File(path);

		NetLog.debug(address, "Parent path = " + tempFile.getParent());
		mWebUtil.setTemplatePath(tempFile.getParent());
		
		String act = "";
		if (request.containsKey("act"))
		{
			act = request.get("act");
		}
		
		if (act.isEmpty())
		{
			act = "main";
		}
		
		NetLog.debug(address, "act=" + act);
		
		if (act.equals("main"))
		{
			DatabaseParam param = Config.instance().getDatabaseParam();
			NotificationTable table = new NotificationTable(param);
			
			Pages page = calculatePages(table, request);
			
			List<NotificationItem> notifications = table.queryList(page.getRows(), page.getOffsets());
			
			if (notifications != null)
			{
				mWebUtil.assign("title", "My Notification");
				//mWebUtil.assign("name", "test");
				mWebUtil.assign("list", notifications);
				
				mWebUtil.assign("total_records", notifications.size());
				mWebUtil.assign("page", page);
				
				mWebUtil.assign("go_url", "index.html");
				mWebUtil.assign("go_act", "main");
				
				mWebUtil.assign("work_path", this.getWorkPath());
				mWebUtil.assign("root_path", this.getRootPath());
				mWebUtil.assign("resource_path", this.getResourcePath());
				
				mWebUtil.display(tempFile.getName());
			}
			else
			{
				this.print("get data failed");
			}
		}
		else if (act.equals("notification_delete"))
		{
			int id = -1;
			if (request.containsKey("item_id"))
			{
				id = Integer.parseInt(request.get("item_id"));
			}
			
			if (id >= 0)
			{
				DatabaseParam param = Config.instance().getDatabaseParam();
				NotificationTable table = new NotificationTable(param);
				table.deleteById(id);
			}
			this.location("index.html?act=main");
		}
		else if (act.equals("act_add_item"))
		{
			mWebUtil.display(tempFile.getName());
		}
		else if (act.equalsIgnoreCase("act_add_test_items"))
		{
			DatabaseParam param = Config.instance().getDatabaseParam();
			NotificationTable table = new NotificationTable(param);
			
			NotificationItem item = new NotificationItem();
			item.setTitle("test");
			item.setDescription("test");
			item.setDisable(0);
			
			Map<String, Object> map = null;
			try {
				map = TransferUtils.transferBean2Map(item);
			} catch (TransferException e) {
				NetLog.error("Transfer", e.getMessage());
			}
			
			if (map != null)
			{
				map.remove("id");
				
				table.holdDatabase();
				for (int i=0; i<5; i++)
				{
					table.insert(map);
				}
				table.releaseDatabase();
				
				this.location("index.html?act=main");
			}
			else
			{
				
			}
		}
		else if (act.equals("notification_item_insert"))
		{
			DatabaseParam param = Config.instance().getDatabaseParam();
			NotificationTable table = new NotificationTable(param);
			
			NotificationItem item = new NotificationItem();
			Map<String, Object> map = null;
			try {
				map = TransferUtils.transferBean2Map(item);
			} catch (TransferException e) {
				NetLog.error("Transfer", e.getMessage());
			}
			
			if (map != null)
			{
				for (Map.Entry<String, String> entry : request.entrySet()) {
					if (map.containsKey(entry.getKey()))
					{
						map.replace(entry.getKey(), entry.getValue());
					}
				}
				map.remove("id");
				
				table.insert(map);
				
				this.location("index.html?act=main");
			}
			else
			{
				
			}
			
		}
		else if (act.equals("db"))
		{
			DatabaseParam param = Config.instance().getDatabaseParam();
			NotificationTable table = new NotificationTable(param);
			
			NotificationItem item = new NotificationItem();
			item.setTitle("Test");
			item.setDescription("Description");
			item.setDisable(1);
			
			// insert
			int id = table.insert(item);
			NetLog.debug("Insert", "" + id);
			
			// query
			String sql = String.format("SELECT * FROM %s", table.getTableName());
			List<Map<String, Object>> list = table.query(sql);
			
			if (list != null)
			{
				for(Map<String, Object> map : list)
				{
					String text = String.format("Id = %s, Title = %s", map.get("id"), map.get("title"));
					NetLog.debug("Database test", text);
				}
			}
			
			// update
			sql = String.format("UPDATE %s set title ='update' where id=5", table.getTableName());
			table.update(sql);
			
			// query
			sql = String.format("SELECT * FROM %s", table.getTableName());
			list = table.query(sql);
			
			if (list != null)
			{
				for(Map<String, Object> map : list)
				{
					String text = String.format("Id = %s, Title = %s", map.get("id"), map.get("title"));
					NetLog.debug("Database test", text);
				}
			}
			
			// delete
			sql = String.format("DELETE FROM %s where id=20", table.getTableName());
			table.update(sql);
			
			// query
			sql = String.format("SELECT * FROM %s", table.getTableName());
			list = table.query(sql);
			
			if (list != null)
			{
				for(Map<String, Object> map : list)
				{
					String text = String.format("Id = %s, Title = %s", map.get("id"), map.get("title"));
					NetLog.debug("Database test", text);
				}
			}
		}
		else if (act.equals("map"))
		{
			NotificationItem notfi = new NotificationItem();  
			Map<String, Object> mp = new HashMap<String, Object>();  
			mp.put("id", 1);
			mp.put("title", "Title1");
			mp.put("description", "Description1");
			mp.put("disable", 1);
			mp.put("else", "else");
			mp.put("resever", 0);

			this.print("--- transMap2Bean Map Info: ");  
			for (Map.Entry<String, Object> entry : mp.entrySet()) {  
				this.print(entry.getKey() + ": " + entry.getValue()+"<br>");  
			}  
			this.print("<br>");
			
			try {
				notfi = TransferUtils.transferMap2Bean(mp, NotificationItem.class);
				this.print("--- Bean Info: "+"<br>");  
				this.print("id: " + notfi.getId()+"<br>");  
				this.print("title: " + notfi.getTitle()+"<br>");  
				this.print("description: " + notfi.getDescription()+"<br>");
				this.print("disable: " + notfi.getDisable()+"<br>");
				this.print("<br>");
			} catch (TransferException e) {
				NetLog.error("Bean", e.getMessage());
			}
			
			try {
				Map<String, Object> map = TransferUtils.transferBean2Map(notfi);  
				
				this.print("--- transBean2Map Map Info: "+"<br>");  
				for (Map.Entry<String, Object> entry : map.entrySet()) {  
					this.print(entry.getKey() + ": " + entry.getValue()+"<br>");  
				}
				this.print("<br>");
			} catch (TransferException e) {
				NetLog.error("Bean", e.getMessage());
			}
		}
		else if (act.equals("act_upload"))
		{
			NetFile netFile = this.getFile("myFile");
			
			if (netFile != null)
			{
				File file = new File(netFile.tmp_name);
				if (file.exists())
				{
					this.print("File is exist"+"<br>");
				}
				else
				{
					this.print("File is not exist"+"<br>");
				}
				
				this.print("<br>");
				this.print("key=myFile"+"<br>");
				this.print("netFile.name="+netFile.name+"<br>");
				this.print("netFile.size="+String.format("%d", netFile.size)+"<br>");
				this.print("netFile.tmp_name="+netFile.tmp_name+"<br>");
				this.print("netFile.type="+netFile.type+"<br>");
				this.print("netFile.error="+String.format("%d", netFile.error)+"<br>");
			}
			else
			{
				NetLog.error("Upload", "Can't get file object");
			}
		}
		else if(act.equals("cookie"))
		{
			String name = this.getCookie("name");
			if (name != null)
			{
				this.print("cookie: name = " + name);
			}
			else
			{
				this.print("cookie: name = null");
			}
			
			this.setCookie("name", "卓晓招", 60);
			this.setCookie("title", "title", 60);
		}
		else if(act.equals("cookie_del"))
		{
			String name = this.getCookie("name");
			if (name != null)
			{
				this.print("cookie: name = " + name);
			}
			else
			{
				this.print("cookie: name = null");
			}
			
			this.deleteCookie("name");
		}
		else if(act.equals("cookie_show"))
		{
			String name = this.getCookie("name");
			if (name != null)
			{
				this.print("cookie: name = " + name);
			}
			else
			{
				this.print("cookie: name = null");
			}
		}
		else if (act.equals("import_excel"))
		{
			String[][] data = null;
			try {
				File file = new File("./webpages/demo/xxx.xls");
				data = ExcelUtils.importData(file, 0, 1);
			}
			catch (Exception e)
			{
				this.print("exception:" + e.getMessage());
			}
			
			if (data == null)
			{
				this.print("error");
			}
		}
		else if (act.equalsIgnoreCase("export_excel"))
		{
			List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", 1);
			map.put("name", "Tom");
			map.put("age", 20);
			listMap.add(map);
			
			boolean ret = false;
			try {
				ExcelUtils.exportExecel("./webpages/demo/yyy.xls", "test", listMap);
				ret = true;
			}
			catch (ExcelException e)
			{
				this.print("exception:" + e.getMessage());
			}
			
			if (ret)
			{
				this.location("yyy.xls");
			}
		}
		else if (act.equalsIgnoreCase("download_file"))
		{
			
		}
		else
		{
			this.die("Not implement");
		}

		NetLog.debug(address, "Leave DemoProcess - Process()");
		NetLog.debug(address, "=============================");
	}

	@Override
	protected String onImageRedirectCheck(final String client, final String path,
			final Map<String, String> request) {
			
		if (request.containsKey("code") && request.get("code").equals("100"))
		{
			return path;
		}
		return Config.instance().getRootFolder() + "/images/wait.gif";
	}
}
