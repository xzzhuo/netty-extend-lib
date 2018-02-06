/**
 * Author: xiaozhao
 */

package exhi.net.log;

/**
 * Log output, use by framework
 * @author XiaoZhao
 *
 */
public class BFCLog {

	static private boolean mDebugMode = false;
	
	/**
	 * Set debug mode flag, true will output debug log, or will not output debug log
	 * @param mode The debug mode flag
	 */
	public static void setDebugMode(boolean mode)
	{
		mDebugMode = mode;
	}
	
	/**
	 * return the debug mode flag
	 * @return Return the debug mode flag
	 */
	public static boolean isDebugMode()
	{
		return mDebugMode;
	}
	
	/**
	 * Output information level log
	 * @param tag The tag
	 * @param message The message
	 * @param trace Output trace flag, true meanwhile output trace, or not output trace
	 */
	public static void info(String tag, String message, boolean trace)
	{
		NetLog.info(tag, message, trace);
	}
	
	/**
	 * Output information level log
	 * @param tag The tag
	 * @param message The message
	 */
	public static void info(String tag, String message)
	{
		NetLog.info(tag, message);
	}
	
	/**
	 * Output error level log
	 * @param tag The tag
	 * @param message The message
	 * @param trace Output trace flag, true meanwhile output trace, or not output trace
	 */
	public static void error(String tag, String message, boolean trace)
	{
		NetLog.error(tag, message, trace);
	}
	
	/**
	 * Output error level log
	 * @param tag The tag
	 * @param message The message
	 */
	public static void error(String tag, String message)
	{
		NetLog.error(tag, message);
	}
	
	/**
	 * Output debug level log
	 * @param tag The tag
	 * @param message The message
	 * @param trace Output trace flag, true meanwhile output trace, or not output trace
	 */
	public static void debug(String tag, String message, boolean trace)
	{
		if (isDebugMode())
		{
			NetLog.debug(tag, message, trace);
		}
	}
	
	/**
	 * Output debug level log
	 * @param tag The tag
	 * @param message The message
	 */
	public static void debug(String tag, String message)
	{
		if (isDebugMode())
		{
			NetLog.debug(tag, message);
		}
	}
	
	/**
	 * Output warning level log
	 * @param tag The tag
	 * @param message The message
	 * @param trace Output trace flag, true meanwhile output trace, or not output trace
	 */
	public static void warning(String tag, String message, boolean trace)
	{
		NetLog.warning(tag, message, trace);
	}
	
	/**
	 * Output warning level log
	 * @param tag The tag
	 * @param message The message
	 */
	public static void warning(String tag, String message)
	{
		NetLog.warning(tag, message);
	}
	
	/**
	 * Output fatal level log
	 * @param tag The tag
	 * @param message The message
	 * @param trace Output trace flag, true meanwhile output trace, or not output trace
	 */
	public static void fatal(String tag, String message, boolean trace)
	{
		NetLog.fatal(tag, message, trace);
	}
	
	/**
	 * Output fatal level log
	 * @param tag The tag
	 * @param message The message
	 */
	public static void fatal(String tag, String message)
	{
		NetLog.fatal(tag, message);
	}
}
