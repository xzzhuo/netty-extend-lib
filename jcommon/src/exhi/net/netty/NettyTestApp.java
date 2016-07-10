/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import exhi.net.interface1.INetConfig;
import exhi.net.log.BFCLog;
import exhi.net.log.NetLog;

class NettyTestApp extends NetApplication {

	NettyTestApp(INetConfig config)
	{
		super(config);
	}

	@Override
	public NetProcess onGetProcess() {

		return new NettyTestProcess();
	}
	
	public static void main(String[] args) {
		
		NetLog.setLogFileName("Log");
		BFCLog.setDebugMode(true);

		NettyTestApp testApp = new NettyTestApp(new NettyConfig() {
			@Override
			public int getServerPort()
			{
				return 8088;
			}
		});

		testApp.run(args, "14D39CD79FC46B2D7EEC6CFAB80858C7");
	}

	@Override
	public INetConfig onGetConfig() {
		return null;
	}
}
