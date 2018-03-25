package exhi.net.netty;

import io.netty.handler.codec.http.cookie.Cookie;

import java.util.Map;
import java.util.Set;

class ProcessAdapter {
	
	private String rootFolder;
	private String client;
	private String uri;
	private Set<Cookie> cookies;
	private Map<String, NetFile> files;
	private Map<String, String> request;
	private String charset;
	private String upload;
	
	public String getRootFolder() {
		return rootFolder;
	}
	public void setRootFolder(String rootPath) {
		this.rootFolder = rootPath;
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
	public String getUploadFolder() {
		return upload;
	}
	public void setUploadFolder(String folder) {
		upload = folder;
	}
}
