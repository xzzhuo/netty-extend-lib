/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.internal.SystemPropertyUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import exhi.net.constant.NetConstant;
import exhi.net.interface1.NetCharset;
import exhi.net.interface1.ServerType;
import exhi.net.log.BFCLog;
import exhi.net.netty.NettyResult.ReturnType;
import exhi.net.utils.FileMimeMap;
import exhi.net.utils.NetUtils;

/**
 * The class of NetProcess, handle the web request
 * 
 * @author XiaoZhao
 *
 */
public abstract class NetProcess {

	private StringBuilder mResponseText = new StringBuilder();
	private NettyResult mDeathResult = null;		// if this value is not null, return this value
	private Map<String, NetFile> mNetFiles = null;
	private Map<String, Cookie> mDownCookies = null;
	private Map<String, Cookie> mUpCookies = new HashMap<String, Cookie>();
	private String mUri = "";
	private boolean bIsNotImplement404Callback = false;
	private NetCharset mCharset = NetCharset.UTF_8;

	/**
	 * Http request process
	 * @param client client address
	 * @param uri http request uri
	 * @param request request parameters
	 */
	protected abstract void onProcess(final String client, final String uri, final Map<String, String> request);

	private void setUri(String mUri) {
		this.mUri = mUri;
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
	 * Get net file
	 * @param key The key of net file
	 * @return Return the net file form upload
	 */
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
	protected void onErrorNotFind(final String client, final String uri)
	{
		this.bIsNotImplement404Callback = true;
		BFCLog.warning(NetConstant.System, "Not implement the callback for 404 error.");
		this.print("404 Not Found");
		return;
	}
	
	NettyResult innerErrorNotFind(final String client, final String uri)
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
	NettyResult innerProcess(final ProcessAdapter processAdapter)
	{
		String client = processAdapter.getClient();
		String uri = processAdapter.getUri();
		
		this.setUri(uri);
		
		if (processAdapter.getFiles() == null)
		{
			mNetFiles = new HashMap<String, NetFile>();
		}
		else
		{
			mNetFiles = processAdapter.getFiles();
		}

		// move cookies
		mDownCookies = new HashMap<String, Cookie>();
		for (Cookie cookie : processAdapter.getCookies())
		{
			// MyLog.debug("NetProcess", String.format("%s=%s", cookie.getName(),cookie.getValue()));
			mDownCookies.put(cookie.name(), cookie);
        }

		mDeathResult = null;
		NettyResult result = new NettyResult();
		
		BFCLog.debug(client, "Enter innerProcess - Process()");
		BFCLog.debug(client, "uri = " + uri);
		
		BFCLog.debug(client, "user dir = " + SystemPropertyUtil.get("user.dir"));
		
		if (processAdapter.getServerType() == ServerType.WEB_SERVER)
		{
			String path = NetUtils.getAbsoluteUrl(processAdapter.getRootPath(), uri);
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
					if (type[0].trim().toLowerCase().equals("text".toLowerCase()))
					{
						result.setReturnType(ReturnType.TEXT);
						result.setMimeType(mimeType);
						if (type[1].trim().toLowerCase().equals("html".toLowerCase()))
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
		}
		else if (processAdapter.getServerType() == ServerType.COMMAND)
		{
			this.onProcess(client, uri, processAdapter.getRequest());
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
	
	/**
	 * Get current work path
	 * @return Return the current work path
	 */
	protected String getWorkPath()
	{
		return SystemPropertyUtil.get("user.dir");
	}

	/**
	 * Get the cookie value with special key
	 * @param key The key of cookie
	 * @return Return the cookie value with special key
	 */
	protected String getCookie(String key) {
		
		String value = null;
		if (this.mDownCookies.containsKey(key))
		{
			value = this.mDownCookies.get(key).value();
			try {
				value = java.net.URLDecoder.decode(value, "utf-8");
			} catch (UnsupportedEncodingException e) {
			}
		}
		
		return value;
	}

	/**
	 * Creates a new cookie with the specified name and value.
	 * This cookie will not expire.
	 * 
	 * @param key The key of cookie
	 * @param value The value of cookie
	 */
	protected void setCookie(String key, String value) {
		
		String newValue = "";
		try {
			newValue = java.net.URLEncoder.encode(value, "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		
		DefaultCookie cookie = new DefaultCookie(key, newValue);
		this.mUpCookies.put(key, cookie);	// 用于保存新值
		this.mDownCookies.put(key, cookie);	// 用于即时更新
	}

	/**
	 * Creates a new cookie with the specified name and value.
	 * This cookie will expire after a given time.
	 * 
	 * @param key The key of cookie
	 * @param value The value of cookie
	 * @param expires The maximum age of this cookie in seconds
	 */
	protected void setCookie(String key, String value, long expires) {
		
		String newValue = "";
		try {
			newValue = java.net.URLEncoder.encode(value, "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		
		DefaultCookie cookie = new DefaultCookie(key, newValue);
		cookie.setMaxAge(expires);

		this.mUpCookies.put(key, cookie);	// 用于保存新值
		this.mDownCookies.put(key, cookie);	// 用于即时更新
	}

	/**
	 * Creates a new cookie with the specified name and value.
	 * This cookie will expire after a given time.
	 * 
	 * @param key The key of cookie
	 * @param value The value of cookie
	 * @param expires The maximum age of this cookie in seconds
	 * @param path The path to use for this cookie
	 */
	protected void setCookie(String key, String value, long expires, String path) {
		
		String newValue = "";
		try {
			newValue = java.net.URLEncoder.encode(value, "utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		
		DefaultCookie cookie = new DefaultCookie(key, newValue);
		cookie.setMaxAge(expires);
		cookie.setPath(path);

		this.mUpCookies.put(key, cookie);	// 用于保存新值
		this.mDownCookies.put(key, cookie);	// 用于即时更新
	}
	
	/**
	 * Delete the cookie value by given key
	 * @param key The key of cookie
	 */
	protected void deleteCookie(String key)
	{
		if (this.mUpCookies.containsKey(key) || this.mDownCookies.containsKey(key))
		{
			this.setCookie(key, "", -1);
		}
	}
	
	Map<String, Cookie> getCookies()
	{
		return this.mUpCookies;
	}

	void setCharset(NetCharset charset)
	{
		this.mCharset = charset;
	}
	
	/**
	 * Get current char set
	 * @return Return current char set
	 */
	public NetCharset getCharset() {
		return this.mCharset;
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
