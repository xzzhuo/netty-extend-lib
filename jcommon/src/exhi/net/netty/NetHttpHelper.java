/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import exhi.net.interface1.INetConfig;

//private class, use by library
class NetHttpHelper {

	private NetProcess mWebProcess = null;
	private INetConfig mConfig = null;
	private WebSocket mWebsocket = null;
	
	public NetHttpHelper()
	{
		
	}
	
	public void setProcess(NetProcess process)
	{
		this.mWebProcess = process;
	}
	
	public NetProcess newProcessInstance()
			throws InstantiationException, IllegalAccessException {
		if (this.mWebProcess != null)
		{
			return this.mWebProcess.getClass().newInstance();
		}
		else
		{
			return null;
		}
	}

	public void setConfig(INetConfig config)
	{
		this.mConfig = config;
	}
	
	public INetConfig getConfig() {
		return this.mConfig;
	}

	public WebSocket getWebsocket() {
		return mWebsocket;
	}

	public void setWebsocket(WebSocket websocket) {
		this.mWebsocket = websocket;
	}
}
