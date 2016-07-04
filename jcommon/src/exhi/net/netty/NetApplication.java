package exhi.net.netty;

import java.io.File;

import exhi.net.interface1.INetApplication;
import exhi.net.interface1.INetConfig;

public abstract class NetApplication implements INetApplication {

	private NetHttpHelper mWebHelper = NetHttpHelper.instance();
	
	public void run(String[] args, String apiKey)
	{
		this.initialize();
	}
	
	private void initialize()
	{
		BFCLog.info(NetConstant.System, "------------------------------------", true);
		
		NetProcess p = this.onGetProcess();
		mWebHelper.setProcess(p);
		if (p == null)
		{
			BFCLog.error(NetConstant.System, "No process");
		}

		INetConfig config = this.onGetConfig();
		if (null == config)
		{
			BFCLog.warning(NetConstant.System, "Not set config, will use default");
			config = new NettyConfig();
		}
		
		try
		{
			this.onInit();
			
			String tempPath = config.getTempPath();
			File tempFile = new File(tempPath);
			if (!tempFile.exists() || !tempFile.isDirectory())
			{
				tempFile.mkdirs();
			}
		}
		catch(Exception e)
		{
			// ignore make directory
			BFCLog.warning(NetConstant.System, "Create temp directory failed: " + e.getMessage());
		}
		
		mWebHelper.setConfig(config);
		
		BFCLog.info(NetConstant.System, "Port: " + config.getServerPort());
		BFCLog.info(NetConstant.System, "Charset: " + config.getCharset());
		BFCLog.info(NetConstant.System, "Root: " + config.getRootPath());
		if (p != null)
		{
			BFCLog.info(NetConstant.System, "Porcess: " + p.getClass().getName());
		}
		
		try
		{
			BFCLog.info(NetConstant.System, "Start server", true);
			NetLog.setLevel(config.getLogLevel());
			
			this.onStart();
			
			new NettyServer().start(config.getServerPort());
		}
		catch (InterruptedException e)
		{
			BFCLog.info(NetConstant.System, "Interrupted Exception: " + e.getMessage());
		}
		
		this.onStop();
	}

	public void setDebugMode(boolean debugMode)
	{
		BFCLog.setDebugMode(debugMode);
	}
	
	@Override
	public void onInit()
	{
		
	}
	
	@Override
	public void onStart()
	{
		
	}
	
	@Override
	public void onStop()
	{
		
	}
}
