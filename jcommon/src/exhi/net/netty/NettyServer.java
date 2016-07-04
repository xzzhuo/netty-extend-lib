/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

class NettyServer {
	
	public final boolean isSSL = true;
	
	public void start(int port) throws InterruptedException
	{
		ServerBootstrap b = new ServerBootstrap();
		EventLoopGroup groupParent = new NioEventLoopGroup(1);
		EventLoopGroup groupChild = new NioEventLoopGroup();

		try
		{
			b.group(groupParent, groupChild);
			b.channel(NioServerSocketChannel.class);
			b.localAddress(new InetSocketAddress(port));
			b.childHandler(new NettyInitializer());
			
			ChannelFuture f = b.bind().sync();
			BFCLog.info(NetConstant.System, "Started and listen on " + f.channel().localAddress(), true);
			f.channel().closeFuture().sync();
		}
		catch(Exception e)
		{
			BFCLog.error(NetConstant.System, "Start service exception: " + e.getMessage());
		}
		finally
		{
			groupChild.shutdownGracefully().sync();
		}
	}
}