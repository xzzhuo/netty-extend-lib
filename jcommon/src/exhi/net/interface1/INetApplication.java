/**
 * Author: xiaozhao
 */

package exhi.net.interface1;

import exhi.net.netty.NetProcess;

public interface INetApplication {

	void onInit();
	void onStart();
	void onStop();
	NetProcess onGetProcess();
	INetConfig onGetConfig();
}
