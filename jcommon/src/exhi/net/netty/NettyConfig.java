/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import exhi.net.interface1.INetConfig;
import exhi.net.interface1.LogLevel;
import exhi.net.interface1.NetCharset;

public class NettyConfig implements INetConfig {

	@Override
	public int getServerPort() {
		return 8088;
	}

	@Override
	public String getRootFolder() {
		return "webpages";
	}

	@Override
	public String getTempFolder() {
		return "Temp";
	}

	@Override
	public NetCharset getCharset() {
		return NetCharset.UTF_8;
	}

	@Override
	public LogLevel getLogLevel() {
		return LogLevel.Debug;
	}

	@Override
	public String getUploadFolder() {
		return "upload";
	}
}
