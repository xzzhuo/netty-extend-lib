/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import exhi.net.interface1.IWebSocket;
import exhi.net.log.BFCLog;

public abstract class WebSocket implements IWebSocket {

	private List<Channel> mChannelList = new ArrayList<Channel>();
	private String mAddress = "";
	
	public WebSocket(String address)
	{
		this.mAddress = address;
	}
	
	public String getAddress()
	{
		return this.mAddress;
	}
	
	void addChannel(Channel channel)
	{
		BFCLog.debug(channel.remoteAddress().toString(), "Add WebSocket channel");
		mChannelList.add(channel);
		this.onConnect(channel.remoteAddress());
	}
	
	void removeChannel(Channel channel)
	{
		if (mChannelList.contains(channel))
		{
			BFCLog.debug(channel.remoteAddress().toString(), "Remove WebSocket channel");
			mChannelList.remove(channel);
			this.onDisconnect(channel.remoteAddress());
		}
	}
	
	private Channel findChannel(SocketAddress address)
	{
		Channel channel = null;
		for (Channel ch : mChannelList)
        {
			if (ch.remoteAddress().equals(address))
			{
				channel = ch;
				break;
			}
        }
		
		return channel;
	}
	
	/**
	 * Send WebSocket binary message
	 * 
	 * @param address Remote address
	 * @param data Send data
	 * @return return true if send success, otherwise return false
	 */
	public boolean sendMessage(SocketAddress address, byte[] data)
	{
		ByteBuf buf = Unpooled.buffer();
		buf.setBytes(data.length, data);
		
		Channel channel = this.findChannel(address);
		
		if (channel == null)
        {
			BFCLog.error(address.toString(), "Can't find WebSocket channel");
			return false;
        }
		
		channel.write(new BinaryWebSocketFrame(buf));
		channel.flush();
		
		return true;
	}
	
	/**
	 * Send WebSocket text message
	 * 
	 * @param address Remote address
	 * @param message Send message
	 * @return return true if send success, otherwise return false
	 */
	public boolean sendMessage(SocketAddress address, String message)
	{
		Channel channel = this.findChannel(address);
		
		if (channel == null)
        {
			BFCLog.error(address.toString(), "Can't find WebSocket channel");
			return false;
        }
		
		channel.write(new TextWebSocketFrame(message));
		channel.flush();
		
		return true;
	}
	
	/**
	 * Close WebSocket
	 * 
	 * @param address Remote address
	 */
	public void close(SocketAddress address)
	{
		Channel channel = this.findChannel(address);
		
		if (channel == null)
        {
			BFCLog.error(address.toString(), "Can't find WebSocket channel");
        }
		
		BFCLog.debug(channel.remoteAddress().toString(), "Close WebSocket");
		
		channel.write(new CloseWebSocketFrame());
		channel.flush();
	}

}
