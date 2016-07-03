/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

class NettyHttpHandler extends SimpleChannelInboundHandler<Object> {

	private String mClient = null;
	
	public NettyHttpHandler(String client)
	{	
		this.mClient = client;
	}
	
	private String getChannelAddress()
	{
		return this.mClient;
	}
	
	@Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	
		BFCLog.debug(getChannelAddress(), "NettyHttpHandler - channelInactive()");

    }
	
	@Override 
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

		BFCLog.warning(getChannelAddress(), "Exception caught: " + cause.getMessage());
		
		this.sendError(ctx, HttpResponseStatus.FORBIDDEN);
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		
		BFCLog.debug(getChannelAddress(), "Enter NettyHttpHandler - channelRead0()");
		
		if (msg instanceof HttpObject)
		{
			this.handleHttpRequest(ctx, (HttpObject)msg);
        }

		BFCLog.debug(getChannelAddress(), "Leave NettyHttpHandler - channelRead0()");
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, HttpObject msg) {
		
		BFCLog.debug(getChannelAddress(), "Enter handleHttpRequest - messageReceived()");

		this.sendError(ctx, HttpResponseStatus.NOT_FOUND);
		
		BFCLog.debug(getChannelAddress(), "Leave handleHttpRequest - messageReceived()");
	}
	
	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));  
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");  
   
        // Close the connection as soon as the error message is sent.  
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);  
        
        ctx.channel().close();
    }
}
