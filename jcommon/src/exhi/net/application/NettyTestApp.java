/**
 * Author: xiaozhao
 */

package exhi.net.application;

import exhi.net.interface1.INetConfig;
import exhi.net.log.BFCLog;
import exhi.net.log.NetLog;
import exhi.net.netty.NetApplication;
import exhi.net.netty.NetProcess;
import exhi.net.netty.NettyConfig;

public final class NettyTestApp extends NetApplication {

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

		testApp.run(args, "EFBC12AF3407ACF5A425C653692D3F72");
	}

	@Override
	public INetConfig onGetConfig() {
		return null;
	}
}
