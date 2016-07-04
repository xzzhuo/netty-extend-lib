/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import exhi.net.interface1.INetConfig;

class NetHttpHelper {

	private static NetHttpHelper mHttpHelper = new NetHttpHelper();
	
	private NetProcess mWebProcess = null;
	private INetConfig mConfig = null;
	
	private NetHttpHelper()
	{
		
	}

	public static NetHttpHelper instance()
	{
		return NetHttpHelper.mHttpHelper;
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
}