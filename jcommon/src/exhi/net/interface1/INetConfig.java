/**
 * Author: xiaozhao
 */

package exhi.net.interface1;

/**
 * The configure interface of NetApplication
 * @author XiaoZhao
 * @see exhi.net.netty.NetApplication
 *
 */
public interface INetConfig {

	/**
	 * Return the server port
	 * @return Return the server port
	 */
	int 		getServerPort();
	
	/**
	 * Return the web template root path
	 * @return Return the web template root path
	 */
	String 		getRootPath();
	
	/**
	 * Return the temporary folder
	 * @return Return the temporary folder
	 */
	String 		getTempPath();
	
	/**
	 * Return char set, eg: NetCharset.UTF_8
	 * @return Return char set
	 */
	NetCharset 	getCharset();
	
	/**
	 * Return the log output level, eg: LogLevel.Debug
	 * @return Return the log output level
	 */
	LogLevel 	getLogLevel();
	
	/**
	 * Return the current server type
	 * @return Return the current server type
	 */
	ServerType 	getServerType();
	
}
