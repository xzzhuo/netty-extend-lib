package exhi.net.netty;

import io.netty.handler.codec.http.cookie.Cookie;

import java.util.Map;
import java.util.Set;

import exhi.net.interface1.ServerType;

class ProcessAdapter {
	
	private String rootPath;
	private ServerType serverType;
	private String client;
	private String uri;
	private Set<Cookie> cookies;
	private Map<String, NetFile> files;
	private Map<String, String> request;
	private String charset;
	
	public String getRootPath() {
		return rootPath;
	}
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}
	public ServerType getServerType() {
		return serverType;
	}
	public void setServerType(ServerType serverType) {
		this.serverType = serverType;
	}
	public String getClient() {
		return client;
	}
	public void setClient(String client) {
		this.client = client;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public Set<Cookie> getCookies() {
		return cookies;
	}
	public void setCookies(Set<Cookie> cookies) {
		this.cookies = cookies;
	}
	public Map<String, NetFile> getFiles() {
		return files;
	}
	public void setFiles(Map<String, NetFile> files) {
		this.files = files;
	}
	public Map<String, String> getRequest() {
		return request;
	}
	public void setRequest(Map<String, String> request) {
		this.request = request;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
}
