/**
 * Author: xiaozhao
 */

package exhi.net.netty;

class BFCLog {

	static private boolean mDebugMode = false;
	
	public static void setDebugMode(boolean mode)
	{
		mDebugMode = mode;
	}
	
	public static boolean isDebugMode()
	{
		return mDebugMode;
	}
	
	public static void info(String tag, String message, boolean trace)
	{
		NetLog.info(tag, message, trace);
	}
	
	public static void info(String tag, String message)
	{
		NetLog.info(tag, message);
	}
	
	public static void error(String tag, String message, boolean trace)
	{
		NetLog.error(tag, message, trace);
	}
	
	public static void error(String tag, String message)
	{
		NetLog.error(tag, message);
	}
	
	public static void debug(String tag, String message, boolean trace)
	{
		if (isDebugMode())
		{
			NetLog.debug(tag, message, trace);
		}
	}
	
	public static void debug(String tag, String message)
	{
		if (isDebugMode())
		{
			NetLog.debug(tag, message);
		}
	}
	
	public static void warning(String tag, String message, boolean trace)
	{
		NetLog.warning(tag, message, trace);
	}
	
	public static void warning(String tag, String message)
	{
		NetLog.warning(tag, message);
	}
	
	public static void fatal(String tag, String message, boolean trace)
	{
		NetLog.fatal(tag, message, trace);
	}
	
	public static void fatal(String tag, String message)
	{
		NetLog.fatal(tag, message);
	}
}
