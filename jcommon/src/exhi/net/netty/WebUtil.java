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
	private Map<String, Object> mParam = new HashMap<String, Object>();
	private String mTemplatePath = ".";
	
	public WebUtil(NetProcess process)
	{
		mProcess = process;
		mTemplatePath = System.getProperty("user.dir");
	}
	
	public void setTemplatePath(String path)
	{
		mTemplatePath = path;
	}
	
	public void assign(String key, Object value)
	{
		mParam.put(key, value);
	}
	
	public void display(String name)
	{
		NetCharset charset = NetHttpHelper.instance().getConfig().getCharset();
		try {
			@SuppressWarnings("deprecation")
			Configuration cfg = new Configuration();
			cfg.setDirectoryForTemplateLoading(new File(mTemplatePath));
			Template template = cfg.getTemplate(name, NetUtils.adapterContentCharset(charset));
			
			StringWriter sw = new StringWriter();
			template.process(mParam, sw);
			sw.flush();
			mProcess.setResponseText(new StringBuilder(sw.getBuffer()));
		} catch (TemplateException e) {
			mProcess.setResponseText(new StringBuilder(e.getMessage()));
			e.printStackTrace();
		} catch (IOException e) {
			mProcess.setResponseText(new StringBuilder(e.getMessage()));
			e.printStackTrace();
		}
	}
}
