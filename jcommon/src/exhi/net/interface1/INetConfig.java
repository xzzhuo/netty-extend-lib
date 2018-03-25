/**
 * Author: xiaozhao
 */

package exhi.net.interface1;

/**
 * The configure interface of NetApplication
 * 
 * @author XiaoZhao
 * @see exhi.net.netty.NetApplication
 *
 */
public interface INetConfig {

	/**
	 * Get the server port
	 * 
	 * @return Return the server port
	 */
	int getServerPort();

	/**
	 * Get the web template root folder name
	 * 
	 * @return Return the web template root folder name
	 */
	String getRootFolder();

	/**
	 * Get the temporary folder name
	 * 
	 * @return Return the temporary folder name
	 */
	String getTempFolder();

	/**
	 * Get char set, eg: NetCharset.UTF_8
	 * 
	 * @return Return char set
	 */
	NetCharset getCharset();

	/**
	 * Get the log output level, eg: LogLevel.Debug
	 * 
	 * @return Return the log output level
	 */
	LogLevel getLogLevel();

	/**
	 * Get the upload folder name
	 * 
	 * @return Return the upload folder name
	 */
	String getUploadFolder();
}
