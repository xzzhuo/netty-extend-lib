package com.web.config;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import exhi.net.database.DatabaseParam;
import exhi.net.interface1.INetConfig;
import exhi.net.interface1.LogLevel;
import exhi.net.interface1.NetCharset;
import exhi.net.log.NetLog;

public class Config implements INetConfig {

	private static final String Tag = "Config";
	
	private final String mConfigPath = "conf";
	private final String mConfigFile = "server.conf";
	
	private int mPort = 8088;
	private String mRootFolder = "webpages";

	private DatabaseParam mDBParam = null;
	
	private NetCharset mCharset = NetCharset.UTF_8;
	private LogLevel mLogLevel = LogLevel.Debug;
	private String mTempFolder = "temp";
	private String mUploadFolder = "upload";
	
	private static Config mConfig = new Config();
	
	private Config()
	{
		mDBParam = new DatabaseParam();
		
		mDBParam.setUrl("localhost");
		mDBParam.setPort(3306);
		mDBParam.setDatabaseName("web_test");
		mDBParam.setUser("root");
		mDBParam.setPassword("root");
	}
	
	public static Config instance()
	{
		return mConfig;
	}
	
	public void readConfig()
	{
		File inFile = new File(mConfigPath, mConfigFile);
		if (!inFile.exists())
		{
			NetLog.error(Tag, "Config file is not exist, use default configure paramters", true);
			return;
		}
		
		try  
		{
			FileInputStream fstream = new FileInputStream(inFile);  
			DataInputStream in = new DataInputStream(fstream);  
			BufferedReader br = new BufferedReader(new InputStreamReader(in));  
			String strLine;  
			while ((strLine = br.readLine()) != null)  
			{
				if (!strLine.isEmpty())
				{
					// System.out.println(strLine);
					try
					{
						String[] split = strLine.split("=");
						if (split.length == 2)
						{
							String key = split[0].trim().toLowerCase();
							String value = split[1].trim();
							
							if (key.equals("port".toLowerCase()))
							{
								this.mPort = Integer.parseInt(value);
							}
							else if (key.equals("root_folder".toLowerCase()))
							{
								this.mRootFolder = value;
							}
							else if (key.equals("db_user".toLowerCase()))
							{
								this.mDBParam.setUser(value);
							}
							else if (key.equals("db_password".toLowerCase()))
							{
								this.mDBParam.setPassword(value);
							}
							else if (key.equals("db_url".toLowerCase()))
							{
								this.mDBParam.setUrl(value);
							}
							else if (key.equals("db_port".toLowerCase()))
							{
								this.mDBParam.setPort(Integer.parseInt(value));
							}
							else if (key.equals("db_name".toLowerCase()))
							{
								this.mDBParam.setDatabaseName(value);
							}
							else if (key.equals("charset".toLowerCase()))
							{
								mCharset = NetCharset.valueOf(value);
							}
							else if (key.equals("log_level".toLowerCase()))
							{
								mLogLevel = LogLevel.valueOf(value);
							}
							else if (key.equalsIgnoreCase("temp_folder"))
							{
								this.mTempFolder = value;
							}
							else if (key.equalsIgnoreCase("upload_folder"))
							{
								this.mUploadFolder = value;
							}
						}
						else if (split.length > 0)
						{
							
						}
					}
					catch(Exception e)  
					{  
						NetLog.error(Tag, "Parse parameter Error: " + e.getStackTrace(), true);  
					}
				}
			}  
			in.close();  
		}  
		catch(Exception e)  
		{  
			NetLog.error(Tag, "Error: " + e.getStackTrace(), true);  
		}
	}
	
	public DatabaseParam getDatabaseParam()
	{
		return this.mDBParam;
	}
	
	@Override
	public int getServerPort() {
		return this.mPort;
	}

	@Override
	public String getRootFolder() {
		return mRootFolder;
	}

	@Override
	public String getTempFolder() {
		return this.mTempFolder;
	}

	@Override
	public NetCharset getCharset() {
		return mCharset;
	}

	@Override
	public LogLevel getLogLevel() {
		return this.mLogLevel;
	}

	@Override
	public String getUploadFolder() {
		return this.mUploadFolder;
	}

}
