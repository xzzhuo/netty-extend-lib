package exhi.net.interface1;


public interface INetConfig {

	int 		getServerPort();
	String 		getRootPath();
	String 		getTempPath();
	NetCharset 	getCharset();
	LogLevel 	getLogLevel();
	ServerType 	getServerType();
	
}
