package com.web.application;

import com.web.config.Config;

import exhi.net.interface1.INetConfig;
import exhi.net.netty.NetApplication;
import exhi.net.netty.NetProcess;

public class DemoApplication extends NetApplication {

	@Override
	public NetProcess onGetProcess() {
		return new DemoProcess();
	}

	public static void main(String[] args)
	{
		DemoApplication app = new DemoApplication();

		app.setDebugMode(true);
		Config.instance().readConfig();
		
		app.run(args, "E7B8D64FCB9F125168BA223BC8DFAF1B");
	}

	@Override
	public INetConfig onGetConfig() {
		return Config.instance();
	}
}
