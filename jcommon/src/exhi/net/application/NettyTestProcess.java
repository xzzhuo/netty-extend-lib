package exhi.net.application;

import java.util.Map;

import exhi.net.log.BFCLog;
import exhi.net.netty.NetFile;
import exhi.net.netty.NetProcess;

public final class NettyTestProcess extends NetProcess {

	@Override
	protected void onProcess(final String client, final String uri,
			final Map<String, String> request) {
		BFCLog.debug("Test", "uri = " + uri);
		BFCLog.debug("Test", "request = " + request.toString());
		
		String html = "<html><head><title>netty-extend-lib</title></head><body>%s</body></html>";
		if (uri.contains("test"))
		{
			this.setCookie("name", "test", 60);
			
			StringBuilder sb = new StringBuilder();
			sb.append("<p>This is test for netty-extend-lib</p>");
			sb.append("<form action='file' enctype ='multipart/form-data' method='post'>");
			sb.append("<input name='file_name' value='test_file'><input type='file' name='test_file'><input type='submit' value='submit'>");
			sb.append("</form>");
			this.print(String.format(html, sb.toString()));
		}
		else if (uri.contains("file"))
		{
			String tableFormat = "<table border='1'><tr><td>file name: %s</td><td>file type: %s</td><td>file size: %s</td></tr></table>";
			String table = "";
			NetFile nf = this.getFile("test_file");
			if (nf != null) {
				table = String.format(tableFormat, nf.name, nf.type, nf.size);
			}
			
			this.print(String.format(html, "<p>test upload file, file size limit to 10M</p>", table));
		}
		else
		{
			this.location("/test?act=test");
		}
	}

}
