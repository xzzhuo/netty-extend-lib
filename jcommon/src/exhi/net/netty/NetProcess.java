/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.internal.SystemPropertyUtil;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import exhi.net.interface1.NetCharset;
import exhi.net.log.BFCLog;
import exhi.net.utils.NetUtils;

/**
 * The class of NetProcess, handle the web request
 * 
 * @author XiaoZhao
 *
 */
public abstract class NetProcess {

	private Map<String, NetFile> mNetFiles = null;
	private Map<String, Cookie> mDownCookies = null;
	private Map<String, Cookie> mUpCookies = new HashMap<String, Cookie>();
	private NetCharset mCharset = NetCharset.UTF_8;
	private String mRootPath = "";
	private String mUploadPath = "";

	/**
	 * Http request process
	 * @param client client address
	 * @param uri http request uri
	 * @param request request parameters
	 */
	protected abstract void onProcess(final String client, final String uri, final Map<String, String> request);

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
	 * Inner process
	 * @param client client address
	 * @param uri request uri
	 * @param cookies cookie values
	 * @param files upload files
	 * @param request request parameters
	 * @param charset charset
	 * @return return the NettyResult object
	 */
	final void innerProcess(final ProcessAdapter processAdapter)
	{
		BFCLog.debug(processAdapter.getClient(), "Enter innerProcess - Process()");
		BFCLog.debug(processAdapter.getClient(), "uri = " + processAdapter.getUri());

		String userDir = SystemPropertyUtil.get("user.dir");
		BFCLog.debug(processAdapter.getClient(), "user dir = " + userDir);

		this.mRootPath = NetUtils.getAbsoluteUrl(userDir,
				processAdapter.getRootFolder(), null);
		this.mUploadPath = NetUtils.getAbsoluteUrl(userDir,
				processAdapter.getRootFolder(), processAdapter.getUploadFolder());

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
		
		BFCLog.debug(processAdapter.getClient(), "Leave innerProcess - Process()");
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
	 * Get root path
	 * @return Return the root path
	 */
	protected String getRootPath()
	{
		return this.mRootPath;
	}

	/**
	 * Get the upload path
	 * @return Return the upload path
	 */
	protected String getUploadPath()
	{
		return this.mUploadPath;
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
}
