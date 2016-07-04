/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.CACHE_CONTROL;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.DATE;
import static io.netty.handler.codec.http.HttpHeaders.Names.EXPIRES;
import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static io.netty.handler.codec.http.HttpHeaders.Names.LAST_MODIFIED;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_MODIFIED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;

class NettyDownload {

	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 60;
    
	public static void sendFile(HttpRequest request, ChannelHandlerContext ctx, String path) throws ParseException, IOException
	{
		// read the file for download
        File file = new File(path);  
        if (file.isHidden() || !file.exists()) {  
        	NettyHttpError.sendError(ctx, NOT_FOUND);  
            return;  
        }  

        if (!file.isFile()) {  
        	NettyHttpError.sendError(ctx, FORBIDDEN);  
            return;  
        }
        
        final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
        
		// Cache Validation  
        String ifModifiedSince = request.headers().get(IF_MODIFIED_SINCE);  
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {  
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);  
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);  
            // Only compare up to the second because the datetime format we send  
            // to the client  
            // does not have milliseconds  
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;  
            long fileLastModifiedSeconds = file.lastModified() / 1000;  
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                sendNotModified(ctx);  
                return;  
            }  
        }  
        RandomAccessFile raf;
        try {  
            raf = new RandomAccessFile(file, "r");  
        } catch (FileNotFoundException ignore) {  
        	NettyHttpError.sendError(ctx, NOT_FOUND);  
            return;  
        }  
        long fileLength = raf.length();  
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);  
        HttpHeaders.setContentLength(response, fileLength);  
        setContentTypeHeader(response, file);  
        setDateAndCacheHeaders(response, file);  
        if (HttpHeaders.isKeepAlive(request)) {  
            response.headers().set("CONNECTION", HttpHeaders.Values.KEEP_ALIVE);  
        }  
   
        // Write the initial line and the header.  
        ctx.write(response);  
   
        // Write the content.  
        ChannelFuture sendFileFuture;  
        if (ctx.pipeline().get(SslHandler.class) == null) {  
            sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise());  
        } else {  
            sendFileFuture = ctx.write(new HttpChunkedInput(new ChunkedFile(raf, 0, fileLength, 8192)), ctx.newProgressivePromise());  
        }
        
        BFCLog.debug(NetConstant.System, String.format("Begin download file: %s", file.getName())); 
        
        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {  
            @Override 
            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {  
                if (total < 0) {
                	// total unknown  
                    BFCLog.debug(NetConstant.System, future.channel() + " Transfer progress: " + progress);  
                } else {  
                	BFCLog.debug(NetConstant.System, future.channel() + " Transfer progress: " + progress + " / " + total);  
                }  
            }  
   
            @Override 
            public void operationComplete(ChannelProgressiveFuture future) {  
            	BFCLog.info(NetConstant.System, future.channel() + " Transfer complete.");  
            }  
        });  
   
        // Write the end marker  
        ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);  
   
        // Decide whether to close the connection or not.  
        if (!HttpHeaders.isKeepAlive(request)) {  
            // Close the connection when the whole content is written out.  
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);  
        }
	}
	
	/** 
     * When file timestamp is the same as what the browser is sending up, send a 
     * "304 Not Modified" 
     *  
     * @param ctx 
     *            Context 
     */ 
    private static void sendNotModified(ChannelHandlerContext ctx) {  
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, NOT_MODIFIED);  
        setDateHeader(response);  
   
        // Close the connection as soon as the error message is sent.  
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);  
    }
	
	/** 
     * Sets the content type header for the HTTP Response 
     *  
     * @param response 
     *            HTTP response 
     * @param file 
     *            file to extract content type 
     */ 
    private static void setContentTypeHeader(HttpResponse response, File file) {  
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();  
        response.headers().set(CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));  
    }
    
    /** 
     * Sets the Date and Cache headers for the HTTP Response 
     *  
     * @param response 
     *            HTTP response 
     * @param fileToCache 
     *            file to extract content type 
     */ 
    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {  
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);  
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));  
   
        // Date header  
        Calendar time = new GregorianCalendar();  
        response.headers().set(DATE, dateFormatter.format(time.getTime()));  
   
        // Add cache headers  
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);  
        response.headers().set(EXPIRES, dateFormatter.format(time.getTime()));  
        response.headers().set(CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);  
        response.headers().set(LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));  
    }
    
    /** 
     * Sets the Date header for the HTTP response 
     *  
     * @param response 
     *            HTTP response 
     */ 
    private static void setDateHeader(FullHttpResponse response) {  
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);  
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));  
   
        Calendar time = new GregorianCalendar();  
        response.headers().set(DATE, dateFormatter.format(time.getTime()));  
    }
}
