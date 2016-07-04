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

import exhi.net.interface1.INetConfig;
import exhi.net.interface1.NetCharset;
import exhi.net.netty.NettyResult.ReturnType;
import exhi.net.utils.NetUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
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
			BFCLog.debug(getChannelAddress(), "Charset: " + NetHttpHelper.instance().getConfig().getCharset().name());
			
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
            
            if (value.length() > 4096) {
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
	        		File tempDir = new File(NetHttpHelper.instance().getConfig().getTempPath());
	        		
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
			BFCLog.error(getChannelAddress(), "No web process");
			this.sendError(ctx, HttpResponseStatus.NOT_IMPLEMENTED);
			return;
		}
		
		INetConfig config = NetHttpHelper.instance().getConfig();
		String charset = NetUtils.AdapterContentCharset(config.getCharset());
		
		// Call inner process
		NettyResult nettyResult = this.mNettyProcess.innerProcess(getChannelAddress(), uri, cookies, files, request, charset);
		
		if (nettyResult == null)
		{
			this.sendError(ctx, HttpResponseStatus.NOT_FOUND);
		}
		else if (nettyResult.getReturnType() == ReturnType.ERR_NOT_FOUND)
		{
			String val = uri;
			int index = val.indexOf('?');
			if (index > 0)
			{
				val = val.substring(0, index);
			}
			NettyResult result = this.mNettyProcess.innerErrorNotFind(getChannelAddress(), val);
			if (result.getReturnType() == ReturnType.ERR_NOT_IMPLEMENT_404_CALLBACK)
			{
				// if not implement the callback for 404 then show error message
				BFCLog.error(getChannelAddress(), "'" + nettyResult.getFilePath() +"' is not exist");
				this.sendError(ctx, HttpResponseStatus.NOT_FOUND);
			}
			else
			{
				writeResponse(ctx.channel(), result.getText(), "text/html", config.getCharset(), null);
			}
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

        String contentType = String.format("%s; charset=%s", mimeType, NetUtils.AdapterContentCharset(charset));
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
