/**
 * Author: xiaozhao
 */

package exhi.net.log;

public class NetTrace {

	/**
	 * Output the trace message
	 * @param messge The trace message
	 */
	public static void output(String messge)
	{
		System.out.println(messge);
	}
	
	/**
	 * Output error level trace
	 * @param messge The trace message
	 */
	public static void error(String messge)
	{
		System.err.println(messge);
	}
	
	/**
	 * Output information level trace
	 * @param messge The trace message
	 */
	public static void info(String messge)
	{
		System.out.println(messge);
	}

	/**
	 * Output debug level trace
	 * @param messge The trace message
	 */
	public static void debug(String messge)
	{
		System.out.println(messge);
	}

	/**
	 * Output warning level trace
	 * @param messge The trace message
	 */
	public static void warning(String messge)
	{
		System.out.println(messge);
	}

	/**
	 * Output fatal level trace
	 * @param messge The trace message
	 */
	public static void fatal(String messge)
	{
		System.out.println(messge);
	}
}
