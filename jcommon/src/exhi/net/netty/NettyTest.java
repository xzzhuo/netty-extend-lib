/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import java.util.Map;

public class NettyTest {

	public static void main(String[] args) {
		
		NetLog.setLogFileName("Log");
		BFCLog.setDebugMode(true);
		
		NetHttpHelper.instance().setConfig(new NettyConfig());
		NetHttpHelper.instance().setProcess(new NetProcess() {

			@Override
			protected void onProcess(String client, String uri,
					Map<String, String> request) {
				
				BFCLog.debug("Test", "uri = " + uri);
				BFCLog.debug("Test", "request = " + request.toString());
				
				String html = "<html><header><title>netty-extend-lib</title></header><body>%s</body></html>";
				if (uri.contains("test"))
				{
					// StringBuilder sb = new StringBuilder();
					this.print(String.format(html, "<p>This is test for netty-extend-lib</p><form action='file' enctype ='multipart/form-data' method='post'><input type='file'><input type='submit' value='submit'></form>"));
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
		});
		
		NettyServer server = new NettyServer();
		try {
			server.start(8088);
		} catch (InterruptedException e) {
			BFCLog.error(NetConstant.System, e.getMessage());
		}
	}
}
