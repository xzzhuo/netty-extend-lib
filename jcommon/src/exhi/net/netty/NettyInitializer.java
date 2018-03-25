/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import exhi.net.log.BFCLog;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

class NettyInitializer extends ChannelInitializer<SocketChannel> {

	private NetApplication mApplication = null;
	
	public NettyInitializer(NetApplication app)
	{
		this.mApplication = app;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {

		BFCLog.debug(getChannelAddress(ch), "Enter NettyInitializer@initChannel()");
		
		ch.pipeline().addLast(new HttpResponseEncoder());
        ch.pipeline().addLast(new HttpRequestDecoder());
        //ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(NettyServer.UPLOAD_FILE_SIZE));	// upload file limit size (10M)
        ch.pipeline().addLast(new ChunkedWriteHandler()); 
		ch.pipeline().addLast("NettyHttpHandler", new NettyHttpHandler(this.mApplication, getChannelAddress(ch)));
		
		BFCLog.debug(getChannelAddress(ch), "Leave NettyInitializer@initChannel()");
	}

	public static String getChannelAddress(SocketChannel ch)
	{
		return ch.remoteAddress().getAddress().toString() + ":" + ch.remoteAddress().getPort();
	}
}
