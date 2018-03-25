package exhi.net.netty;

import io.netty.util.internal.SystemPropertyUtil;

import java.io.File;
import java.util.Map;

import exhi.net.constant.NetConstant;
import exhi.net.log.BFCLog;
import exhi.net.netty.NettyResult.ReturnType;
import exhi.net.utils.FileMimeMap;
import exhi.net.utils.NetUtils;

public abstract class WebProcess extends NetProcess {

	private String mUri = "";
	private StringBuilder mResponseText = new StringBuilder();
	private NettyResult mDeathResult = null;		// if this value is not null, return this value
	private boolean bIsNotImplement404Callback = false;

	private void setUri(String uri) {
		this.mUri = uri;
	}

	/**
	 * Get the URI
	 * @return Return current URI
	 */
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

	/**
	 * Response the text value
	 * @param value The text value
	 */
	protected void print(String value)
	{
		setResponseText(new StringBuilder(value));
	}

	/**
	 * Response the last value, after that, no other requests will be responded.
	 * 
	 * @param value The text value
	 */
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

	/**
	 * locate to the special URL, after that, no other requests will be responded.
	 * 
	 * @param url The new URL
	 */
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

	/**
	 * Http request process for file not find
	 * 
	 * @param client client address
	 * @param uri http request uri
	 * @param request request parameters
	 */
	protected void onRedirectProcess(final String client, final String uri, final Map<String, String> request)
	{
		this.bIsNotImplement404Callback = true;
		BFCLog.warning(NetConstant.System, "Not implement the callback for 404 error.");
		this.print("404 Not Found");

		return;
	}

	final NettyResult innerErrorNotFind(final String client, final Map<String, String> request)
	{
		NettyResult result = new NettyResult();
		
		String uri = this.getUri();
		
		this.bIsNotImplement404Callback = false;
		this.onRedirectProcess(client, uri, request);
		
		if (this.bIsNotImplement404Callback)
		{
			result.setReturnType(ReturnType.ERR_NOT_IMPLEMENT_404_CALLBACK);
		}
		else
		{
			result.setReturnType(ReturnType.TEXT);
			result.setMimeType("text/html");
		}
		result.setText(this.getResponseText());

		if (mDeathResult != null)
		{
			result = mDeathResult;
		}

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
	final NettyResult innerWebProcess(final ProcessAdapter processAdapter)
	{
		String client = processAdapter.getClient();
		String uri = processAdapter.getUri();

		BFCLog.debug(client, "Enter innerWebProcess - Process()");

		this.setUri(uri);

		super.innerProcess(processAdapter);

		mDeathResult = null;
		NettyResult result = new NettyResult();
		
		// start handle path for web process
		String path = NetUtils.getAbsoluteUrl(SystemPropertyUtil.get("user.dir"),
				processAdapter.getRootFolder(), uri);
		File p = new File(path);
		if (!p.exists())
		{
			// file or directory is not exist
			BFCLog.warning(client, "'" + p.getAbsolutePath()+"' is not exist");
			BFCLog.debug(client, "Leave innerWebProcess - Process()");
			result.setReturnType(ReturnType.ERR_NOT_FOUND);
			result.setFilePath(p.getAbsolutePath());
			return result;
		}

		if (p.isDirectory())
		{
			/*
			// 忘了有什么作用了？
			String newUri = uri;
			if (newUri.indexOf("?") >= 0 && newUri.indexOf(File.separator+"?") < 0)
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
			*/
			
			// set default file for html or htm
			File p1 = new File(p.getAbsolutePath(),"index.html");
			if (p1.isFile() && p1.exists()) {
				p = p1;
			}
			else {
				p = new File(p.getAbsolutePath(),"index.htm");
			}
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
				if (type[0].trim().equalsIgnoreCase("text"))
				{
					result.setReturnType(ReturnType.TEXT);
					result.setMimeType(mimeType);
					if (type[1].trim().equalsIgnoreCase("html"))
					{
						this.onProcess(client, p.getAbsolutePath(), processAdapter.getRequest());
						result.setText(this.getResponseText());
					}
					else
					{
						StringBuilder text = null;
						
						try {
							// read source files from disk
							text = NetUtils.readText(p.getAbsolutePath(), processAdapter.getCharset());
						} catch (Exception e) {
							text = new StringBuilder();
							BFCLog.error(NetConstant.System, "Error: " + e.getMessage(), true);
						}
						
						result.setText(text);
					}
				}
				else if (type[0].trim().toLowerCase().equals("image".toLowerCase()))
				{
					BFCLog.debug(NetConstant.System, "image request", true);
					
					String pathOfRedirect = this.onImageRedirectCheck(client, p.getAbsolutePath(), processAdapter.getRequest());

					if (null != pathOfRedirect && !pathOfRedirect.isEmpty()) {
						pathOfRedirect = (new File(pathOfRedirect)).getAbsolutePath();
					}

					result.setFilePath(pathOfRedirect);
					result.setReturnType(ReturnType.FILE);
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
		
		if (result != null)
		{
			BFCLog.debug(client, "Response file type = " + result.getReturnType());
		}

		if (mDeathResult != null)
		{
			result = mDeathResult;
		}

		BFCLog.debug(client, "Leave innerWebProcess - Process()");

		return result;
	}

	/**
	 * Image request process
	 * @param client Client address
	 * @param path Image file path
	 * @param request Request parameters
	 * @return Return the new path
	 */
	protected String onImageRedirectCheck(final String client, final String path, final Map<String, String> request) {
		return path;
	}
}
