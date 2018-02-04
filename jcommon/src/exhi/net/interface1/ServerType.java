/**
 * Author: xiaozhao
 */

package exhi.net.interface1;

/**
 * The server type
 * @author XiaoZhao
 *
 */
public enum ServerType {
	
	/**
	 * for web server, need setting htm or html template file
	 */
	WEB_SERVER,
	
	/**
	 * for command server, provider the JSON parser
	 */
	COMMAND
}
