/**
 * Author: xiaozhao
 */

package exhi.net.log;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import exhi.net.interface1.LogLevel;

/**
 * Log output, common class
 * The net log use double buffer machining, if file1 is get to max size and will delete file2 which exist and change to output to file2,
 * and then file2 is get to max size, it will delete file1 and return to output to file1, and keep going around this way.
 * @author XiaoZhao
 *
 */
public class NetLog {
	private static final String mPath 	= "logs";
	
	private static String mLogFile 		= "error%d.log";
	private static int mMinTagLen 		= 32;
	private static int mLimitTagLen 	= mMinTagLen;
	private static int mMaxSize 		= 8*1024*1024;
	private static int mCurrentIndex 	= 1;
	
	private static LogLevel mLogLevel 	= LogLevel.Debug;
	
	/**
	 * Set log level
	 * @param level log level
	 */
	public static void setLevel(LogLevel level)
	{
		if (level == null)
		{
			return;
		}
		
		mLogLevel = level;
	}
	
	/**
	 * Set max tag length
	 * @param length new tag length
	 */
	public static void setMaxTagLen(int length)
	{
		if (length < mMinTagLen)
		{
			return;
		}
		
		mLimitTagLen = length;
	}
	
	/**
	 * Set log file name
	 * @param name The log file name
	 */
	public static void setLogFileName(String name)
	{
		if (name != null && !name.isEmpty())
		{
			mLogFile = String.format("%s%%d.log", name);
		}
	}
	
	/**
	 * Out the log message to file
	 * @param message The log message
	 */
	private static synchronized void output(String message)
	{
		File path = new File(mPath);
		try {
			path.mkdir();
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
		
		boolean bNeedClear = false;
		File outFile1 = new File(mPath, String.format(mLogFile, 1));
		File outFile = outFile1;
		if (outFile1.exists() && outFile1.length() > mMaxSize)
		{
			// 1 号文件已满
			File outFile2 = new File(mPath, String.format(mLogFile, 2));
			outFile = outFile2;
			if (outFile2.exists() && outFile2.length() > mMaxSize)
			{
				// 2 号文件已满
				if (outFile1.lastModified() < outFile2.lastModified())
				{
					// 比较哪个文件旧就清除数据，并用二当前的log文件
					outFile = outFile1;
				}
				bNeedClear = true;
			}
		}

		if (!outFile.exists()) {
			try {
				outFile.createNewFile();
			} catch (IOException e) {
				System.err.println("Create log file failed, error: " + e.getStackTrace());
				return;
			}
		}
		
		try {
			FileOutputStream fstream = null;
			if (bNeedClear)
			{
				fstream = new FileOutputStream(outFile, false);
				bNeedClear = false;
			}
			else
			{
				fstream = new FileOutputStream(outFile, true);
			}
			DataOutputStream out = new DataOutputStream(fstream);  
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
			bw.write(message+"\r\n");
			bw.close(); 
		}  
		catch(Exception e) {  
			System.err.println("Error: " + e.getStackTrace());
		}
	}
	
	/**
	 * Format the tag and output the message with log level
	 * @param tag The tag information
	 * @param level The log level
	 * @param message The log message
	 * @return
	 */
	private static String LevelOutput(String tag, LogLevel level, String message)
	{
		// date, level, tag, message
		Calendar calendar = Calendar.getInstance();
		Date date = calendar.getTime();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		if (tag.length() > mLimitTagLen)
		{
			tag = tag.substring(0, mLimitTagLen);
		}
		
		String test = String.format("%s\t%s\t%s\t%s", sdf.format(date), level.name(), tag, message);
		
		output(test);
		
		return test; 
	}
	
	/**
	 * Output information level log
	 * @param tag The tag
	 * @param message The message
	 * @param trace Output trace flag, true meanwhile output trace, or not output trace
	 */
	public static void info(String tag, String message, boolean trace)
	{
		if (mLogLevel.ordinal() > LogLevel.Info.ordinal())
		{
			return;
		}
		
		if (trace == false)
		{
			LevelOutput(tag, LogLevel.Info, message);
		}
		else
		{
			info(tag, message);
		}
	}
	
	/**
	 * Output information level log
	 * @param tag The tag
	 * @param message The message
	 */
	public static void info(String tag, String message)
	{
		if (mLogLevel.ordinal() > LogLevel.Info.ordinal())
		{
			return;
		}
		
		String test = LevelOutput(tag, LogLevel.Info, message);
		NetTrace.info(test);
	}
	
	/**
	 * Output error level log
	 * @param tag The tag
	 * @param message The message
	 * @param trace Output trace flag, true meanwhile output trace, or not output trace
	 */
	public static void error(String tag, String message, boolean trace)
	{
		if (mLogLevel.ordinal() > LogLevel.Error.ordinal())
		{
			return;
		}
		
		if (trace == false)
		{
			LevelOutput(tag, LogLevel.Error, message);
		}
		else
		{
			error(tag, message);
		}
	}
	
	/**
	 * Output error level log
	 * @param tag The tag
	 * @param message The message
	 */
	public static void error(String tag, String message)
	{
		if (mLogLevel.ordinal() > LogLevel.Error.ordinal())
		{
			return;
		}
		
		String test = LevelOutput(tag, LogLevel.Error, message);
		NetTrace.error(test);
	}
	
	/**
	 * Output debug level log
	 * @param tag The tag
	 * @param message The message
	 * @param trace Output trace flag, true meanwhile output trace, or not output trace
	 */
	public static void debug(String tag, String message, boolean trace)
	{
		if (mLogLevel.ordinal() > LogLevel.Debug.ordinal())
		{
			return;
		}
		
		if (trace == false)
		{
			LevelOutput(tag, LogLevel.Debug, message);
		}
		else
		{
			debug(tag, message);
		}
	}
	
	/**
	 * Output debug level log
	 * @param tag The tag
	 * @param message The message
	 */
	public static void debug(String tag, String message)
	{
		if (mLogLevel.ordinal() > LogLevel.Debug.ordinal())
		{
			return;
		}
		
		String test = LevelOutput(tag, LogLevel.Debug, message);
		NetTrace.debug(test);
	}
	
	/**
	 * Output warning level log
	 * @param tag The tag
	 * @param message The message
	 * @param trace Output trace flag, true meanwhile output trace, or not output trace
	 */
	public static void warning(String tag, String message, boolean trace)
	{
		if (mLogLevel.ordinal() > LogLevel.Warning.ordinal())
		{
			return;
		}
		
		if (trace == false)
		{
			LevelOutput(tag, LogLevel.Warning, message);
		}
		else
		{
			warning(tag, message);
		}
	}
	
	/**
	 * Output warning level log
	 * @param tag The tag
	 * @param message The message
	 */
	public static void warning(String tag, String message)
	{
		if (mLogLevel.ordinal() > LogLevel.Warning.ordinal())
		{
			return;
		}
		
		String test = LevelOutput(tag, LogLevel.Warning, message);
		NetTrace.warning(test);
	}
	
	/**
	 * Output fatal level log
	 * @param tag The tag
	 * @param message The message
	 * @param trace Output trace flag, true meanwhile output trace, or not output trace
	 */
	public static void fatal(String tag, String message, boolean trace)
	{
		if (mLogLevel.ordinal() > LogLevel.Fatal.ordinal())
		{
			return;
		}
		
		if (trace == false)
		{
			LevelOutput(tag, LogLevel.Fatal, message);
		}
		else
		{
			warning(tag, message);
		}
	}
	
	/**
	 * Output fatal level log
	 * @param tag The tag
	 * @param message The message
	 */
	public static void fatal(String tag, String message)
	{
		if (mLogLevel.ordinal() > LogLevel.Fatal.ordinal())
		{
			return;
		}
		
		String test = LevelOutput(tag, LogLevel.Fatal, message);
		NetTrace.fatal(test);
	}

	/**
	 * Get current log file index
	 * @return Return the current log file index
	 */
	public static int getCurrentIndex() {
		return mCurrentIndex;
	}
}
