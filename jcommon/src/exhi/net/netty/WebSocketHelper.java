/**
 * Author: xiaozhao
 */

package exhi.net.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class WebSocketHelper {

	private final static String SEC_WEBSOCKET_KEY     = "Sec-WebSocket-Key";
    private final static String SEC_WEBSOCKET_ACCEPT  = "Sec-WebSocket-Accept";
    /* websocket version： protocol 8 to 12 is 8，草案13及以后的版本号都和草案号相同 */
    private final static String Sec_WebSocket_Version = "Sec-WebSocket-Version";

    /**
     * Get whether is the web socket request
     * 
     * @param request Http request
     * @return Return true if is the web socket request, otherwise return false
     */
    static boolean isSupportWebSocket(HttpRequest request) {
    	HttpHeaders headers = request.headers();
        return (HttpHeaders.Values.UPGRADE.equalsIgnoreCase(headers.get(HttpHeaders.Names.CONNECTION)) 
        		&& HttpHeaders.Values.WEBSOCKET.equalsIgnoreCase(headers.get(HttpHeaders.Names.UPGRADE)));
    }

    /**
     * Get the handshake type by request, 
     * and return the handshake response with different web socket version
     * 
     * @param request Http request
     * @return return the http response object
     */
    static HttpResponse buildWebSocketResponse(HttpRequest request) {
        String reasonPhrase = "";
        boolean isThirdTypeHandshake = Boolean.FALSE;
        int websocketVersion = 0;
        
        HttpHeaders headers = request.headers();
        
        if (headers.get(Sec_WebSocket_Version) != null) {
            websocketVersion = Integer.parseInt(headers.get(Sec_WebSocket_Version));
        }
        /**
         * 在草案13以及其以前，请求源使用http头是Origin，在草案4到草案10，请求源使用http头是Sec-WebSocket-Origin，
         * 而在草案11及以后使用的请求头又是Origin了，
         * 注意，这里还有一点需要注意的就是"websocketVersion >= 13"这个条件，并不一定适合以后所有的草案，
         * 不过这也只是一个预防，有可能会适应后面的草案， 如果不适合还只有升级对应的websocket协议。<br>
         */
        if (websocketVersion >= 13
            || (headers.contains(Names.SEC_WEBSOCKET_ORIGIN) && headers.contains(SEC_WEBSOCKET_KEY))) {
            isThirdTypeHandshake = Boolean.TRUE;
        }

        // websocket协议草案7后面的格式，可以参看wikipedia上面的说明，比较前后版本的不同：http://en.wikipedia.org/wiki/WebSocket
        if (isThirdTypeHandshake == Boolean.FALSE) {
            reasonPhrase = "Switching Protocols";
        } else {
            reasonPhrase = "Web Socket Protocol Handshake";
        }
        HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(101, reasonPhrase));
        headers.add(HttpHeaders.Names.UPGRADE, HttpHeaders.Values.WEBSOCKET);
        headers.add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.UPGRADE);
        // Fill in the headers and contents depending on handshake method.
        if (headers.contains(Names.SEC_WEBSOCKET_KEY1) && headers.contains(Names.SEC_WEBSOCKET_KEY2)) {
            // New handshake method with a challenge:
            headers.add(Names.SEC_WEBSOCKET_ORIGIN, headers.get(Names.ORIGIN));
            headers.add(Names.SEC_WEBSOCKET_LOCATION, getWebSocketLocation(request));
            String protocol = headers.get(Names.SEC_WEBSOCKET_PROTOCOL);
            if (protocol != null) {
                headers.add(Names.SEC_WEBSOCKET_PROTOCOL, protocol);
            }
            // Calculate the answer of the challenge.
            String key1 = headers.get(Names.SEC_WEBSOCKET_KEY1);
            String key2 = headers.get(Names.SEC_WEBSOCKET_KEY2);
            int a = (int) (Long.parseLong(getNumeric(key1)) / getSpace(key1).length());
            int b = (int) (Long.parseLong(getNumeric(key2)) / getSpace(key2).length());
            
            long c = HttpHeaders.getContentLength(request);
            ByteBuf input = Unpooled.buffer(64);
            input.writeInt(a);
            input.writeInt(b);
            input.writeLong(c);

            ByteBuf output = null;
            try {
                output = Unpooled.copiedBuffer(MessageDigest.getInstance("MD5").digest(input.array()));
            } catch (NoSuchAlgorithmException e) {
            }

            res.headers().set(CONTENT_LENGTH, output.readableBytes());
        } else if (isThirdTypeHandshake = Boolean.FALSE) {
            String protocol = headers.get(Names.SEC_WEBSOCKET_PROTOCOL);
            if (protocol != null) {
                headers.add(Names.SEC_WEBSOCKET_PROTOCOL, protocol);
            }
            headers.add(SEC_WEBSOCKET_ACCEPT, getSecWebSocketAccept(request));
        } else {
            // Old handshake method with no challenge:
            if (headers.get(Names.ORIGIN) != null) {
                headers.add(Names.WEBSOCKET_ORIGIN, headers.get(Names.ORIGIN));
            }
            headers.add(Names.WEBSOCKET_LOCATION, getWebSocketLocation(request));
            String protocol = headers.get(Names.WEBSOCKET_PROTOCOL);
            if (protocol != null) {
                headers.add(Names.WEBSOCKET_PROTOCOL, protocol);
            }
        }

        return res;
    }

    static String getWebSocketLocation(HttpRequest request) {

        String host_addr =  request.headers().get(HttpHeaders.Names.HOST);
        String location =  String.format("ws://%s%s", host_addr, request.getUri());

        return location;
    }

    private static String getSecWebSocketAccept(HttpRequest request) {
        // CHROME WEBSOCKET VERSION 8中定义的GUID，
    	// 详细文档地址：http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-10
        String guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        String key = "";
        key = request.headers().get(SEC_WEBSOCKET_KEY);
        key += guid;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(key.getBytes("iso-8859-1"), 0, key.length());
            byte[] sha1Hash = md.digest();
            key = base64Encode(sha1Hash);
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }
        return key;
    }

    private static String base64Encode(byte[] input) {
        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
        String base64 = encoder.encode(input);
        return base64;
    }

    // 去掉传入字符串的所有非数字
    private static String getNumeric(String value) {
        return value.replaceAll("\\D", "");
    }

    // 返回传入字符串的空格
    private static String getSpace(String value) {
        return value.replaceAll("\\S", "");
    }
    
    static WebSocketServerHandshaker handshakeWebSocket(ChannelHandlerContext ctx, HttpRequest request)
    {
		// Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
        		getWebSocketLocation(request), null, true);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
        if (handshaker == null)
        {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }
        else
        {
        	handshaker.handshake(ctx.channel(), request);
        }
        
        return handshaker;
    }
}
