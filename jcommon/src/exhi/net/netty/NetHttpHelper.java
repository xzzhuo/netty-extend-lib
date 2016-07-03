/**
 * Author: xiaozhao
 */

package exhi.net.netty;

class NetHttpHelper {

	private static NetHttpHelper mHttpHelper = new NetHttpHelper();
	
	private Class<NetProcess> mWebProcessClass = null;
	
	private NetHttpHelper()
	{
		
	}

	public static NetHttpHelper instance()
	{
		return NetHttpHelper.mHttpHelper;
	}
	
	public void setProcess(Class<NetProcess> processClass)
	{
		this.mWebProcessClass = processClass;
	}
	
	public NetProcess newProcessInstance()
			throws InstantiationException, IllegalAccessException {
		if (mWebProcessClass != null)
		{
			return mWebProcessClass.newInstance();
		}
		else
		{
			return null;
		}
	}
}
