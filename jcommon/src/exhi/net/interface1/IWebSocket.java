/**
 * Author: xiaozhao
 */

package exhi.net.interface1;

import java.net.SocketAddress;

/**
 * Websocet interface
 * @author XiaoZhao
 *
 */
public interface IWebSocket {

	/**
	 * Trigger callback function for connect websocket server 
	 * @param address The sebsokcet address
	 */
	void onConnect(SocketAddress address);
	
	/**
	 * Trigger callback function for disconnect websocket server 
	 * @param address The sebsokcet address
	 */
	void onDisconnect(SocketAddress address);
	
	/**
	 * Trigger callback function for receive message
	 * @param address The sebsokcet address
	 * @param message The message
	 */
	void onMessage(SocketAddress address, String message);
	
	/**
	 * Trigger callback function for receive message
	 * @param address The sebsokcet address
	 * @param message The message
	 */
	void onMessage(SocketAddress address, byte[] data);
}
