/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import exhi.net.utils.NetUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.util.CharsetUtil;

class NettyHttpHandler extends SimpleChannelInboundHandler<Object> {

	private String mClient = null;
	
	private HttpRequest mRequest = null;
	private boolean readingChunks = false;
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); //Disk
	 
    private HttpPostRequestDecoder decoder;
    
    private NetProcess mNettyProcess = null;
    private Map<String, String> mRequestsParam = new HashMap<String, String>();
    private Map<String, NetFile> mNetFiles = new HashMap<String, NetFile>();
    private List<String> mTempFile = new ArrayList<String>();
	
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

	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));  
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");  
   
        // Close the connection as soon as the error message is sent.  
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);  
        
        ctx.channel().close();
    }
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		
		BFCLog.debug(getChannelAddress(), "Enter NettyHttpHandler - channelRead0()");
		boolean result = false;
		
		if (msg instanceof HttpObject)
		{
			NetHttpHelper helper = NetHttpHelper.instance();

			if (helper != null)
			{
				try {
					mNettyProcess = helper.newProcessInstance();
				} catch (InstantiationException e) {
					BFCLog.error(getChannelAddress(), "Instance process failed: " + e.getMessage());
				} catch (IllegalAccessException e) {
					BFCLog.error(getChannelAddress(), "Instance process failed: " + e.getMessage());
				}
				
				if (mNettyProcess != null)
				{
					this.handleHttpRequest(ctx, (HttpObject)msg);
					result = true;
				}
				else
				{
					BFCLog.error(getChannelAddress(), "Instance process failed");
				}
			}
			else
			{
				BFCLog.error(getChannelAddress(), "Not provider the http helper object");
			}
        }

		if (!result)
		{
			this.sendError(ctx, HttpResponseStatus.FORBIDDEN);
		}
		
		BFCLog.debug(getChannelAddress(), "Leave NettyHttpHandler - channelRead0()");
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, HttpObject msg) {
		
		BFCLog.debug(getChannelAddress(), "NettyHttpHandler - handleHttpRequest()");

		BFCLog.debug(getChannelAddress(), "message class name = " + msg.getClass().getName());
		BFCLog.debug(getChannelAddress(), "DiskFileUpload.baseDirectory = " + DiskFileUpload.baseDirectory);
		
		String uri = null;
		Set<Cookie> cookies = null;
				
		if (msg instanceof HttpRequest) {
			BFCLog.debug(getChannelAddress(), "Message type - HttpRequest");
			
			mRequest = (HttpRequest) msg;

			if (!mRequest.getDecoderResult().isSuccess()) {
				BFCLog.error(getChannelAddress(), "Request decoder failed");
				this.sendError(ctx, HttpResponseStatus.BAD_REQUEST);

				return;
			}
			
			try {
				uri = NetUtils.sanitizeUri(mRequest.getUri(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				BFCLog.error(getChannelAddress(), e.getMessage());
			}
			
			if (uri == null) {
				BFCLog.debug(getChannelAddress(), "uri is not excepted");
				this.sendError(ctx, HttpResponseStatus.FORBIDDEN);
				
				return;
			}
			
			HttpMethod method = mRequest.getMethod();
			
			BFCLog.debug(getChannelAddress(), "Require URI: " + uri);
			BFCLog.debug(getChannelAddress(), "Method: " + method);
			BFCLog.debug(getChannelAddress(), "Version: " + mRequest.getProtocolVersion().text());
			
			for (Entry<String, String> entry : mRequest.headers()) {
				BFCLog.debug(getChannelAddress(), "Header: " + entry.getKey() + '=' + entry.getValue());
			}
			
			// new getMethod
            String value = mRequest.headers().get(COOKIE);
            if (value == null) {
                /**
                 * Returns an empty set (immutable).
                 */
                cookies = Collections.emptySet();
            } else {
            	BFCLog.debug(getChannelAddress(), "Cookie: " + value);
                cookies = ServerCookieDecoder.STRICT.decode(value);
            }
            
            QueryStringDecoder decoderQuery = new QueryStringDecoder(mRequest.getUri());
            Map<String, List<String>> uriAttributes = decoderQuery.parameters();
            for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
                for (String attrVal : attr.getValue()) {
                	BFCLog.debug(getChannelAddress(), "Request: " + attr.getKey() + '=' + attrVal);
                	mRequestsParam.put(attr.getKey(), attrVal);
                }
            }

			if (method.equals(HttpMethod.GET))
			{
				this.process(ctx, uri, cookies, mRequestsParam, null);
				reset();
			}
			else if (method.equals(HttpMethod.POST))
			{
                try {
                    /**
                     * HttpDataFactory
                     */
                    decoder = new HttpPostRequestDecoder(factory, mRequest);
                } catch (ErrorDataDecoderException e1) {
                    e1.printStackTrace();
                    BFCLog.error(getChannelAddress(), "Error data decoder exception");
                    this.sendError(ctx, HttpResponseStatus.EXPECTATION_FAILED);
                    return;
                }
 
                readingChunks = HttpHeaders.isTransferEncodingChunked(mRequest);
                BFCLog.debug(getChannelAddress(), "Is Chunked: " + readingChunks);
                BFCLog.debug(getChannelAddress(), "IsMultipart: " + decoder.isMultipart());
                if (readingChunks) {
                    // Chunk version
                	BFCLog.debug(getChannelAddress(), "Chunks: ");
                    readingChunks = true;
                }

				//writeResponse(ctx.channel(), "error");
			}
		}
		
		if (decoder != null) {
            if (msg instanceof HttpContent) {
                // New chunk is received
                HttpContent chunk = (HttpContent) msg;
                try {
                    decoder.offer(chunk);
                } catch (ErrorDataDecoderException e1) {
                	BFCLog.error(getChannelAddress(), "Error data decoder exception");
                	this.sendError(ctx, HttpResponseStatus.EXPECTATION_FAILED);
                    return;
                }
                
                try {
                    while (decoder.hasNext()) {
                        InterfaceHttpData data = decoder.next();
                        if (data != null) {
                            try {
                            	receiveHttpData(data);
                            } finally {
                                data.release();
                            }
                        }
                    }
                } catch (EndOfDataDecoderException e1) {
                	BFCLog.debug(getChannelAddress(), "End of data decoder exception");
                }
 
                // example of reading only if at the end
                if (chunk instanceof LastHttpContent) {

                	// Handle the post request
                	this.process(ctx, uri, cookies, mRequestsParam, mNetFiles);
                	
                	// delete the temporary files
                	for(String temFileName: mTempFile)
                	{
                		try
                		{
                			File file = new File(temFileName);
                			file.delete();
                		}
                		catch(Exception e)
                		{
                			// Ignore exception
                			BFCLog.warning(getChannelAddress(), "Delete temporary file failed: " + e.getMessage());
                		}
                	}
                	
                	// reset others
                    readingChunks = false;
                    reset();
                }
            }
        }
	}

	private void reset() {
		mRequest = null;
        // destroy the decoder to release all resources
		
		mRequestsParam.clear();
		mNetFiles.clear();
		
		if (decoder != null)
		{
			decoder.destroy();
			decoder = null;
		}
    }

	private void receiveHttpData(InterfaceHttpData data) {
		
		BFCLog.debug(getChannelAddress(), "NettyHttpHandler - receiveHttpData()");
		
	}
	
	private void process(ChannelHandlerContext ctx, String uri, Set<Cookie> cookies,
			Map<String, String> request, Map<String, NetFile> files) {
		
		BFCLog.debug(getChannelAddress(), "NettyHttpHandler - process()");
		
		if (this.mNettyProcess == null)
		{
			BFCLog.error(getChannelAddress(), "No web process");
			this.sendError(ctx, HttpResponseStatus.NOT_IMPLEMENTED);
			return;
		}
	}
}
