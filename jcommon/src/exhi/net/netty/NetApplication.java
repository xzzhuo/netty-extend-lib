package exhi.net.netty;

import java.io.File;

import exhi.net.constant.NetConstant;
import exhi.net.interface1.INetApplication;
import exhi.net.interface1.INetConfig;
import exhi.net.log.BFCLog;
import exhi.net.log.NetLog;
import exhi.net.utils.NetUtils;

public abstract class NetApplication implements INetApplication {

	private NetHttpHelper mWebHelper = NetHttpHelper.instance();
	
	private static String getDictionaryString(byte[] index)
    {
    	final char[] DICTIONARY = {
    		'o','l','p','S','6','4','A','c','D','5','D','s','P','7','g','V',
    		'Y','8','v','D','I','W','r','j','1','9','g','j','3','T','d','6',
    		'G','i','Z','0','q','S','E','a','k','h','b','E','O','K','w','3',
    		'n','u','s','y','C','Q','c','6','9','F','m','p','R','n','h','f',
    		'm','Q','b','O','Y','0','3','x','I','G','T','A','4','U','7','F',
    		'W','p','y','t','8','R','z','L','v','C','J','x','a','I','l','V',
    		'H','r','5','d','q','K','f','P','X','H','o','B','X','C','L','M',
    		'T','N','e','Z','2','w','e','U','2','N','M','1','W','T','D','9'
    	};
    	
    	StringBuilder sb = new StringBuilder();
    	for (byte i : index)
    	{
    		if (i<0)
    		{
    			i += 128;
    		}
    		sb.append(DICTIONARY[i]);
    	}
    	
    	String value = sb.toString();
    	
    	return value;
    }
	
	public void run(String[] args, String apiKey)
	{
		String appName = this.getClass().getName();
		byte[] ps = {-35, 6, 56, -3, 127, 36, 79, -28, 8, 76};
		
		if (!NetUtils.computeMd5(String.format("%s%s", appName, getDictionaryString(ps))).equals(apiKey))
		{
			BFCLog.error(NetConstant.System, "Api key is incorrect");
			return;
		}
		
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
	
	/**
	 * Register websocket handler process
	 * @param websocket Websocket handler class
	 */
	public void registerWebsocket(WebSocket websocket)
	{
		mWebHelper.setWebsocket(websocket);
	}
	
	WebSocket getWebsocketObject()
	{
		return mWebHelper.getWebsocket();
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
