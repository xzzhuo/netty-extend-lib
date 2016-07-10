/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import exhi.net.interface1.NetCharset;
import exhi.net.utils.NetUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class WebUtil {

	private NetProcess mProcess = null;
	private NetCharset mCharset = NetCharset.UTF_8;
	private Map<String, Object> mParam = new HashMap<String, Object>();
	private String mTemplatePath = ".";
	
	public WebUtil(NetProcess process)
	{
		this.mProcess = process;
		this.mCharset = process.getCharset();
		this.mTemplatePath = System.getProperty("user.dir");
	}
	
	public WebUtil(NetProcess process, NetCharset charset)
	{
		this.mProcess = process;
		this.mCharset = charset;
		this.mTemplatePath = System.getProperty("user.dir");
	}
	
	public void setTemplatePath(String path)
	{
		this.mTemplatePath = path;
	}
	
	public void assign(String key, Object value)
	{
		this.mParam.put(key, value);
	}
	
	public void display(String name)
	{
		NetCharset charset = this.mCharset;
		try {
			@SuppressWarnings("deprecation")
			Configuration cfg = new Configuration();
			cfg.setDirectoryForTemplateLoading(new File(this.mTemplatePath));
			Template template = cfg.getTemplate(name, NetUtils.adapterContentCharset(charset));
			
			StringWriter sw = new StringWriter();
			template.process(this.mParam, sw);
			sw.flush();
			this.mProcess.setResponseText(new StringBuilder(sw.getBuffer()));
		} catch (TemplateException e) {
			this.mProcess.setResponseText(new StringBuilder(e.getMessage()));
			e.printStackTrace();
		} catch (IOException e) {
			this.mProcess.setResponseText(new StringBuilder(e.getMessage()));
			e.printStackTrace();
		}
	}
}
