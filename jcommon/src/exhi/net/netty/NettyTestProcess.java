package exhi.net.netty;

import java.util.Map;

import exhi.net.log.BFCLog;

class NettyTestProcess extends NetProcess {

	@Override
	protected void onProcess(String client, String uri,
			Map<String, String> request) {
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
			this.print(String.format(html, "<p>upload file</p>"));
		}
		else
		{
			this.location("/test?act=test");
		}
	}

}
