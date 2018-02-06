/**
 * Author: xiaozhao
 */

package exhi.net.interface1;

import exhi.net.netty.NetProcess;

/**
 * The application interface
 * @author XiaoZhao
 *
 */
public interface INetApplication {

	/**
	 * Initialize application
	 */
	void onInit();
	
	/**
	 * Do something when start run application
	 */
	void onStart();
	
	/**
	 * Do something when stop run application
	 */
	void onStop();
	
	/**
	 * Return a NetProcess type process object
	 * @return Return the NetProcess type process object for handle the http requests
	 * @see NetProcess
	 */
	NetProcess onGetProcess();
	
	/**
	 * Return a INetConfig type configuration object
	 * @return Return the INetConfig type configuration data for configure the http server
	 * @see INetConfig
	 */
	INetConfig onGetConfig();
}
