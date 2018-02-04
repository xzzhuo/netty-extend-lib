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
	 * @return
	 */
	NetProcess onGetProcess();
	
	/**
	 * Return a INetConfig type configuration object
	 * @return
	 */
	INetConfig onGetConfig();
}
