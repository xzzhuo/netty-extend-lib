/**
 * Author: xiaozhao
 */

package exhi.net.interface1;

import java.net.SocketAddress;

public interface IWebSocket {
	void onConnect(SocketAddress address);
	void onDisconnect(SocketAddress address);
	void onMessage(SocketAddress address, String message);
	void onMessage(SocketAddress address, byte[] data);
}
