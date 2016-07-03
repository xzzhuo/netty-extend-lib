/**
 * Author: xiaozhao
 */

package exhi.net.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class NetUtils {

	public static String sanitizeUri(String uri, String enc) throws UnsupportedEncodingException {
		
		// Decode the path.  
		uri = URLDecoder.decode(uri, enc);
   
        if (!uri.startsWith("/")) {  
            return null;  
        }

        // Convert file separators.  
        uri = uri.replace('/', File.separatorChar);

        return uri;
	}

}
