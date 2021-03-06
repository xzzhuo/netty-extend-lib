/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static io.netty.handler.codec.http.HttpHeaders.Names.LOCATION;
import static io.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import exhi.net.constant.NetConstant;
import exhi.net.interface1.INetConfig;
import exhi.net.interface1.NetCharset;
import exhi.net.log.BFCLog;
import exhi.net.netty.NettyResult.ReturnType;
import exhi.net.utils.NetUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.util.CharsetUtil;

class NettyHttpHandler extends SimpleChannelInboundHandler<Object> {

	private NetApplication mApplication = null;
	private String mClient = null;
	
	private HttpRequest mRequest = null;
	private boolean readingChunks = false;
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); //Disk
	 
    private HttpPostRequestDecoder decoder;
    
    private NetProcess mNettyProcess = null;
    private Map<String, String> mRequestsParam = new HashMap<String, String>();
    private Map<String, NetFile> mNetFiles = new HashMap<String, NetFile>();
    private List<String> mTempFile = new ArrayList<String>();
	
    // WebSocket handshaker
    private WebSocketServerHandshaker mHandshaker = null;
    private WebSocket mWebsocket = null;
    
	public NettyHttpHandler(NetApplication app, String client)
	{	
		this.mApplication = app;
		this.mClient = client;
		this.mWebsocket = app.getHelper().getWebsocket();
	}
	
	private String getChannelAddress()
	{
		return this.mClient;
	}
	
	@Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	
		BFCLog.debug(getChannelAddress(), "NettyHttpHandler - channelInactive()");

		if (mHandshaker != null)
		{
			BFCLog.debug(NetConstant.WebSocket, "NettyHttpHandler - remove websocket channel");
        	this.mWebsocket.removeChannel(ctx.channel());
            mHandshaker = null;
		}
    }
	
	@Override 
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

		if (mHandshaker == null)
		{
			if (cause instanceof NullPointerException)
			{
				if (cause.getStackTrace().length > 0)
				{
					StackTraceElement stack = cause.getStackTrace()[0];
					BFCLog.error(getChannelAddress(), "Null pointer: " +  stack.toString());
				}
			}
			else
			{
				BFCLog.error(getChannelAddress(), "cause message: " + cause.getMessage());
			}
			
			if (cause instanceof TooLongFrameException)
			{
				if (ctx.channel().isActive()) {  
					this.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);  
				}
			}
			else
			{
				if (ctx.channel().isActive()) {  
					this.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
		else
		{
			BFCLog.warning(getChannelAddress(), "Exception caught: " + cause.getMessage());
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		
		BFCLog.debug(getChannelAddress(), "Enter NettyHttpHandler - channelRead0()");
		
		if (mHandshaker == null)
		{
			BFCLog.debug(getChannelAddress(), "mHandshaker is null");
		}
		else
		{
			BFCLog.debug(getChannelAddress(), "mHandshaker is not null");
		}
		
		if (msg instanceof HttpObject)
		{
			if (WebSocketHelper.isSupportWebSocket((HttpRequest) msg))
			{
				BFCLog.debug(getChannelAddress(), "WebSocket connect request");
				this.handshakeWebSocket(ctx, (FullHttpRequest)msg);
			}
			else
			{
				boolean result = false;
				NetHttpHelper helper = mApplication.getHelper();
	
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
						mNettyProcess.setCharset(this.mApplication.getHelper().getConfig().getCharset());
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
				
				if (!result)
				{
					this.sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				}
			}
        }
		else if (msg instanceof WebSocketFrame)
		{
            this.handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
		else
		{
        	BFCLog.error(getChannelAddress(), "Not handle request");
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
			BFCLog.debug(getChannelAddress(), "Charset: " + mApplication.getHelper().getConfig().getCharset().name());
			
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
	
	private void handshakeWebSocket(ChannelHandlerContext ctx, FullHttpRequest req)
	{
		if (mWebsocket == null)
        {
			BFCLog.debug(getChannelAddress(), "WebSocket not set handle object");
			this.sendError(ctx, HttpResponseStatus.FORBIDDEN);
			return;
        }
		
		String addr = mWebsocket.getAddress();
		if (addr == null || addr.isEmpty())
		{
			BFCLog.debug(getChannelAddress(), "WebSocket address invalide");
			this.sendError(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		
		addr = addr.replace('\\', '/');
		if (!addr.substring(0, 1).equals("/"))
		{
			addr = String.format("%c%s", '/', addr);
		}
		
		String uri = req.getUri().replace('\\', '/');
		if (addr.equalsIgnoreCase(uri))
		{
			BFCLog.debug(getChannelAddress(), String.format("Connect WebSocket: %s",
					WebSocketHelper.getWebSocketLocation((FullHttpRequest) req)));
			
			mHandshaker = WebSocketHelper.handshakeWebSocket(ctx, req);
	        if (mHandshaker != null)
	        {
	        	mWebsocket.addChannel(ctx.channel());
	        }
	        else
	        {
	        	BFCLog.error(getChannelAddress(), "WebSocket connect failed");
	        	this.sendError(ctx, HttpResponseStatus.INSUFFICIENT_STORAGE);
	        }
		}
		else
		{
			BFCLog.debug(getChannelAddress(), "WebSocket address incorrect");
			this.sendError(ctx, HttpResponseStatus.NOT_FOUND);
		}
	}
	
	private void handleWebSocketFrame(ChannelHandlerContext ctx,
			WebSocketFrame frame) {
		
		// Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
        	this.mWebsocket.removeChannel(ctx.channel());
            mHandshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            mHandshaker = null;
            return;
        }
        
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        
        if (!(frame instanceof TextWebSocketFrame)) {
           throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
        }

        if (frame instanceof BinaryWebSocketFrame)
        {
        	BFCLog.debug(getChannelAddress(), "Receive BinaryWebSocketFrame");
        	this.mWebsocket.onMessage(ctx.channel().remoteAddress(), ((BinaryWebSocketFrame) frame).content().array());
        }
        else if (frame instanceof TextWebSocketFrame)
        {
        	BFCLog.debug(getChannelAddress(), "Receive TextWebSocketFrame");
        	this.mWebsocket.onMessage(ctx.channel().remoteAddress(), ((TextWebSocketFrame) frame).text());
        }
        else
        {
        	BFCLog.debug(getChannelAddress(), "Receive type : " + frame.getClass().getName());
        	this.mWebsocket.onMessage(ctx.channel().remoteAddress(), frame.content().array());
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
		
		/**
         * HttpDataType
         * Attribute, FileUpload, InternalAttribute
         */
        if (data.getHttpDataType() == HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value;
            try {
                value = attribute.getValue();
            } catch (IOException e1) {
                BFCLog.debug(getChannelAddress(), "BODY Attribute: " + attribute.getHttpDataType().name() + ":"
                        + attribute.getName() + " Error while reading value: " + e1.getMessage());
                return;
            }
            
            // MyLog.debug(getChannelAddress(), "value = " + value);
            
            if (value.length() > NettyServer.POST_BODY_SIZE) {
            	BFCLog.warning(getChannelAddress(), "BODY Attribute: " + attribute.getHttpDataType().name() + ":"
                        + attribute.getName() + " data too long");
            } else {
            	BFCLog.debug(getChannelAddress(), "BODY Attribute: " + attribute.getHttpDataType().name() + ":"
                        + attribute.toString());
            }
            mRequestsParam.put(attribute.getName(), value);
        }
        else if (data.getHttpDataType() == HttpDataType.FileUpload)
        {
        	BFCLog.debug(getChannelAddress(), "File upload");
        	
        	FileUpload fileUpload = (FileUpload) data;
        	NetFile netFile = new NetFile();
        	
        	if (fileUpload.isCompleted()) {
        		
        		try {
        			File uploadFile = new File(fileUpload.getFilename());
	        		File tempDir = new File(mApplication.getHelper().getConfig().getTempFolder());
	        		
	        		File tempFile = File.createTempFile("netup_", "_"+uploadFile.getName(), tempDir);
	        		fileUpload.renameTo(tempFile);
	        		mTempFile.add(tempFile.getAbsolutePath());
	        		
	            	netFile.name = uploadFile.getName();
	            	netFile.size = fileUpload.length();
	            	netFile.error = 0;
	            	netFile.tmp_name = tempFile.getAbsolutePath();
					netFile.type = NetUtils.getMimeType(netFile.name);
	
	            	mNetFiles.put(fileUpload.getName(), netFile);
	            	
	            	BFCLog.debug(getChannelAddress(), "Upload, key=" + fileUpload.getName());
	            	BFCLog.debug(getChannelAddress(), "Upload, netFile.name="+netFile.name);
	        		BFCLog.debug(getChannelAddress(), "Upload, netFile.size="+String.format("%d", netFile.size));
	        		BFCLog.debug(getChannelAddress(), "Upload, netFile.tmp_name="+netFile.tmp_name);
	        		BFCLog.debug(getChannelAddress(), "Upload, netFile.type="+netFile.type);
	        		BFCLog.debug(getChannelAddress(), "Upload, netFile.error="+String.format("%d", netFile.error));
        		
        		} catch (IOException e) {
        			netFile.error = 1;
        			BFCLog.error(getChannelAddress(), "upload file failed: " + e.getMessage());
				}
        	}
        }
	}
	
	private void process(ChannelHandlerContext ctx, String uri, Set<Cookie> cookies,
			Map<String, String> request, Map<String, NetFile> files) {
		
		BFCLog.debug(getChannelAddress(), "NettyHttpHandler - process()");
		
		if (this.mNettyProcess == null)
		{
			BFCLog.error(getChannelAddress(), "No user process");
			this.sendError(ctx, HttpResponseStatus.NOT_IMPLEMENTED);
			return;
		}
		
		INetConfig config = mApplication.getHelper().getConfig();
		String charset = NetUtils.adapterContentCharset(config.getCharset());
		
		// Call inner process
		ProcessAdapter processAdapter = new ProcessAdapter();
		processAdapter.setCharset(charset);
		processAdapter.setClient(this.getChannelAddress());
		processAdapter.setCookies(cookies);
		processAdapter.setFiles(files);
		processAdapter.setRequest(request);
		processAdapter.setRootFolder(config.getRootFolder());
		processAdapter.setUri(uri);
		processAdapter.setUploadFolder(config.getUploadFolder());
		
		NettyResult nettyResult = null;
		
		if (this.mNettyProcess instanceof WebProcess)
		{
			WebProcess web = (WebProcess)this.mNettyProcess;
			nettyResult = web.innerWebProcess(processAdapter);
			
			if (nettyResult.getReturnType() == ReturnType.ERR_NOT_FOUND) {
				// if not find the file then redirect to process
				nettyResult = web.innerErrorNotFind(getChannelAddress(), processAdapter.getRequest());
			}
			
			if (nettyResult.getReturnType() == ReturnType.ERR_NOT_IMPLEMENT_404_CALLBACK)
			{
				// if not implement the callback for 404 then show error message
				// BFCLog.error(getChannelAddress(), "'" + nettyResult.getFilePath() +"' is not exist");
				nettyResult.setReturnType(ReturnType.ERR_NOT_FOUND);
			}
		} else if (this.mNettyProcess instanceof CommandProcess) {
			CommandProcess web = (CommandProcess)this.mNettyProcess;
			nettyResult = web.innerCommandProcess(processAdapter);
		} else {
			// skip
		}
		
		if (nettyResult == null)
		{
			this.sendError(ctx, HttpResponseStatus.NOT_FOUND);
		}
		else if (nettyResult.getReturnType() == ReturnType.ERR_NOT_FOUND)
		{
			this.sendErrorWithCookies(ctx, HttpResponseStatus.NOT_FOUND, this.mNettyProcess.getCookies());
		}
		else
		{
			if (nettyResult.getReturnType() == ReturnType.LOCATION)
			{
				// relocation
				this.relocation(ctx, nettyResult.getText().toString(), this.mNettyProcess.getCookies());
			}
			else if (nettyResult.getReturnType() == ReturnType.FILE)
			{
				// download files, must give the full file path
				if (nettyResult.getFilePath() == null)
				{
					BFCLog.error(getChannelAddress(), "File not find");
					this.sendErrorWithCookies(ctx, HttpResponseStatus.NOT_FOUND, this.mNettyProcess.getCookies());
				}
				else
				{
					try {
						NettyDownload.sendFile(this.mRequest, ctx, nettyResult.getFilePath());
					} catch (ParseException e) {
						BFCLog.debug(getChannelAddress(), e.getMessage());
						this.sendErrorWithCookies(ctx, HttpResponseStatus.EXPECTATION_FAILED, this.mNettyProcess.getCookies());
					} catch (IOException e) {
						BFCLog.debug(getChannelAddress(), e.getMessage());
						this.sendErrorWithCookies(ctx, HttpResponseStatus.EXPECTATION_FAILED, this.mNettyProcess.getCookies());
					}
				}
			}
			else if (nettyResult.getReturnType() == ReturnType.TEXT)
			{
				// process test file
				if (nettyResult.getText() == null)
				{
					BFCLog.error(getChannelAddress(), "text is empty");
					this.sendErrorWithCookies(ctx, HttpResponseStatus.NOT_FOUND, this.mNettyProcess.getCookies());
				}
				else
				{
					writeResponse(ctx.channel(), nettyResult.getText(), nettyResult.getMimeType(),
							config.getCharset(), this.mNettyProcess.getCookies());
				}
			}
		}
	}
	
	private Charset adapterCharset(NetCharset charset)
    {
    	Charset type = CharsetUtil.UTF_8;
    	switch (charset)
    	{
    	case ISO_8859_1:
    		type = CharsetUtil.ISO_8859_1;
    		break;
    	case US_ASCII:
    		type = CharsetUtil.US_ASCII;
    		break;
    	case UTF_16:
    		type = CharsetUtil.UTF_16;
    		break;
    	case UTF_16BE:
    		type = CharsetUtil.UTF_16BE;
    		break;
    	case UTF_16LE:
    		type = CharsetUtil.UTF_16LE;
    		break;
    	case UTF_8:
    		type = CharsetUtil.UTF_8;
    		break;
    	}
    	
    	return type;
    }
	
	private void writeResponse(Channel channel, StringBuilder responseValue, String mimeType,
			NetCharset charset, Map<String, Cookie> newCookies) {
		// Convert the response content to a ChannelBuffer.
        ByteBuf buf = copiedBuffer(responseValue, adapterCharset(charset));
 
        // Decide whether to close the connection or not.
        boolean close = mRequest.headers().contains(CONNECTION, HttpHeaders.Values.CLOSE, true)
                || mRequest.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !mRequest.headers().contains(CONNECTION, HttpHeaders.Values.KEEP_ALIVE, true);
 
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);

        String contentType = String.format("%s; charset=%s", mimeType, NetUtils.adapterContentCharset(charset));
        response.headers().set(CONTENT_TYPE, contentType);
 
        if (!close) {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.headers().set(CONTENT_LENGTH, buf.readableBytes());
        }
 
        this.saveCookies(response, newCookies);

        // Write the response.
        ChannelFuture future = channel.writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
	}
	
	@SuppressWarnings("unused")
	private void writeResponse(Channel channel, String responseValue, 
			String mimeType, NetCharset charset, Map<String, Cookie> newCookies)
    {
    	writeResponse(channel, new StringBuilder(responseValue), mimeType, charset, newCookies);
    }
	
	private void relocation(ChannelHandlerContext ctx, String newUri, Map<String, Cookie> newCookies) {  
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.FOUND);  
        response.headers().set(LOCATION, newUri);  
   
        this.saveCookies(response, newCookies);
        
        // Close the connection as soon as the error message is sent.  
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
	
	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));  
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");  
   
        this.saveCookies(response, null);
        
        // Close the connection as soon as the error message is sent.  
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);  
        
        ctx.channel().close();
    } 

	private void sendErrorWithCookies(ChannelHandlerContext ctx, HttpResponseStatus status, Map<String, Cookie> newCookies) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));  
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");  
   
        this.saveCookies(response, newCookies);
        
        // Close the connection as soon as the error message is sent.  
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);  
        
        ctx.channel().close();
    }
	
	private void saveCookies(FullHttpResponse response, Map<String, Cookie> newCookies)
	{
		/*
		Set<Cookie> cookies;
        String value = mRequest.headers().get(COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.STRICT.decode(value);
        }
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            for (Cookie cookie : cookies) {
            	response.headers().add(SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            }
        }
        */

		// add new cookies
		if (newCookies != null)
        {
	        for (Entry<String, Cookie> entry : newCookies.entrySet()) {
	        	if (entry.getValue() != null)
	        	{
	        		response.headers().add(SET_COOKIE, ServerCookieEncoder.STRICT.encode(entry.getValue()));
	        	}
	        }
        }
	}
}
