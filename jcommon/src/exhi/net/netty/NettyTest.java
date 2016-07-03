/**
 * Author: xiaozhao
 */

package exhi.net.netty;

public class NettyTest {

	public static void main(String[] args) {
		
		NetLog.setLogFileName("Log");
		BFCLog.setDebugMode(true);
		
		NettyServer server = new NettyServer();
		try {
			server.start(8088);
		} catch (InterruptedException e) {
			BFCLog.error(NetConstant.System, e.getMessage());
		}
	}
}
