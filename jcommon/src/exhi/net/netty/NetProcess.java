/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.util.internal.SystemPropertyUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import exhi.net.interface1.ServerType;
import exhi.net.netty.NettyResult.ReturnType;
import exhi.net.utils.FileMimeMap;
import exhi.net.utils.NetUtils;

public abstract class NetProcess {

	private StringBuilder mResponseText = new StringBuilder();
	private NettyResult mDeathResult = null;		// if this value is not null, return this value
	private Map<String, NetFile> mNetFiles = null;
	private String mUri = "";
	private boolean bIsNotImplement404Callback = false;
	
	/**
	 * Http request process
	 * @param client client address
	 * @param uri http request uri
	 * @param request request parameters
	 */
	protected abstract void onProcess(String client, String uri, Map<String, String> request);
	
	private void setUri(String mUri) {
		this.mUri = mUri;
	}
	
	protected String getUri() {
		return mUri;
	}
	
	private StringBuilder getResponseText() {
		return mResponseText;
	}

	void setResponseText(StringBuilder text) {
		if (text != null)
		{
			mResponseText.append(text);
		}
	}
	
	protected void print(String value)
	{
		setResponseText(new StringBuilder(value));
	}
	
	protected void die(String value)
	{
		if (mDeathResult != null)
		{
			return;
		}
		mDeathResult = new NettyResult();
		
		StringBuilder sb = new StringBuilder();
		sb.append(mResponseText);
		sb.append(value);
		mDeathResult.setText(sb);
	}
	
	protected void location(String url)
	{
		if (mDeathResult != null)
		{
			return;
		}
		mDeathResult = new NettyResult();
		mDeathResult.setReturnType(ReturnType.LOCATION);
		mDeathResult.setText(new StringBuilder(url));
	}
	
	protected NetFile getFile(String key)
	{
		if (this.mNetFiles.containsKey(key))
		{
			return this.mNetFiles.get(key);
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Error process for 404
	 */
	protected void onErrorNotFind(String client, String uri)
	{
		this.bIsNotImplement404Callback = true;
		BFCLog.warning(NetConstant.System, "Not implement the callback for 404 error.");
		this.print("404 Not Found");
		return;
	}
	
	NettyResult innerErrorNotFind(String client, String uri)
	{
		NettyResult result = new NettyResult();
		
		this.setUri(uri);
		
		this.bIsNotImplement404Callback = false;
		this.onErrorNotFind(client, uri);
		
		if (this.bIsNotImplement404Callback)
		{
			result.setReturnType(ReturnType.ERR_NOT_IMPLEMENT_404_CALLBACK);
		}
		else
		{
			result.setReturnType(ReturnType.TEXT);
		}
		result.setText(this.getResponseText());
		return result;
	}
	
	/**
	 * Inner process
	 * @param client client address
	 * @param uri request uri
	 * @param cookies cookie values
	 * @param files upload files
	 * @param request request parameters
	 * @param charset charset
	 * @return return the NettyResult object
	 */
	NettyResult innerProcess(String client, String uri, Set<Cookie> cookies, Map<String, NetFile> files,
			Map<String, String> request, String charset)
	{
		this.setUri(uri);
		
		if (files == null)
		{
			mNetFiles = new HashMap<String, NetFile>();
		}
		else
		{
			mNetFiles = files;
		}

		mDeathResult = null;
		NettyResult result = new NettyResult();
		
		BFCLog.debug(client, "Enter innerProcess - Process()");
		BFCLog.debug(client, "uri = " + uri);
		
		BFCLog.debug(client, "user dir = " + SystemPropertyUtil.get("user.dir"));
		
		if (NetHttpHelper.instance().getConfig().getServerType() == ServerType.WEB_SERVER)
		{
			String path = NetUtils.getAbsoluteUrl(NetHttpHelper.instance().getConfig().getRootPath(), uri);
			File p = new File(path);
			if (!p.exists())
			{
				// file or directory is not exist
				BFCLog.warning(client, "'" + p.getAbsolutePath()+"' is not exist");
				BFCLog.debug(client, "Leave innerProcess - Process()");
				result.setReturnType(ReturnType.ERR_NOT_FOUND);
				result.setFilePath(p.getAbsolutePath());
				return result;
			}
	
			if (p.isDirectory())
			{	
				String newUri = uri;
				if (newUri.indexOf("?") >= 0 && newUri.indexOf(File.separator+"?") <= 0)
				{
					newUri = newUri.replace("?", File.separator+"?");
				}
				else if (newUri.indexOf("?") < 0 && !newUri.substring(newUri.length()-1).equals(File.separator))
				{
					newUri += File.separator;
				}
				
				if (!newUri.equals(uri))
				{
					BFCLog.debug(client, "Relocation");
					result.setReturnType(ReturnType.LOCATION);
					result.setText(new StringBuilder(newUri));
					return result;
				}
				
				// set default file for html or htm
				p = new File(p.getAbsolutePath(),"index.html");
			}
			
			if (p.isFile() && p.exists())
			{
				String mimeType = FileMimeMap.getMimeType(p.getName());
				if (null != mimeType) {
					BFCLog.debug(client, "Mime type = " + mimeType);
				} else {
					BFCLog.warning(client, "Get mime type failed");
				}
	
				if (mimeType != null && !mimeType.isEmpty())
				{
					String[] type = mimeType.split("/");
					if (type[0].trim().equals("text"))
					{
						result.setReturnType(ReturnType.TEXT);
						result.setMimeType(mimeType);
						if (type[1].trim().equals("html"))
						{
							this.onProcess(client, p.getAbsolutePath(), request);
							result.setText(this.getResponseText());
						}
						else
						{
							StringBuilder text = null;
							
							try {
								text = NetUtils.readText(p.getAbsolutePath(), charset);
							} catch (Exception e) {
								text = new StringBuilder();
								BFCLog.error(NetConstant.System, "Error: " + e.getMessage(), true); 
							}
							
							result.setText(text);
						}
					}
					else
					{
						result.setReturnType(ReturnType.FILE);
						result.setFilePath(p.getAbsolutePath());
					}
				}
				else
				{
					// not ext name
					result.setReturnType(ReturnType.FILE);
					result.setFilePath(p.getAbsolutePath());
				}
			}
			else
			{
				BFCLog.warning(client, "'" + p.getAbsolutePath()+"' is not exist");
				BFCLog.warning(client, "Not support the request url");
				result.setReturnType(ReturnType.ERR_NOT_FOUND);
				result.setFilePath(p.getAbsolutePath());
			}
		}
		else if (NetHttpHelper.instance().getConfig().getServerType() == ServerType.COMMAND)
		{
			this.onProcess(client, uri, request);
			result.setText(this.getResponseText());
		}
		
		if (result != null)
		{
			BFCLog.debug(client, "Response file type = " + result.getReturnType());
		}
		
		BFCLog.debug(client, "Leave innerProcess - Process()");

		if (mDeathResult != null)
		{
			result = mDeathResult;
		}
		
		return result;
	}
	
	protected String getWorkPath()
	{
		return SystemPropertyUtil.get("user.dir");
	}
}
