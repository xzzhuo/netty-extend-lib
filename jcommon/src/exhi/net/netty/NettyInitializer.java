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

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {

		BFCLog.debug(getChannelAddress(ch), "Enter NettyInitializer@initChannel()");
		
		ch.pipeline().addLast(new HttpResponseEncoder());
        ch.pipeline().addLast(new HttpRequestDecoder());
        //ch.pipeline().addLast(new HttpServerCodec());
        ch.pipeline().addLast(new HttpObjectAggregator(1024*10240));
        ch.pipeline().addLast(new ChunkedWriteHandler()); 
		ch.pipeline().addLast("NettyHttpHandler", new NettyHttpHandler(getChannelAddress(ch)));
		
		BFCLog.debug(getChannelAddress(ch), "Leave NettyInitializer@initChannel()");
	}

	public static String getChannelAddress(SocketChannel ch)
	{
		return ch.remoteAddress().getAddress().toString() + ":" + ch.remoteAddress().getPort();
	}
}
