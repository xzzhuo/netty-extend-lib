/**
 * Author: xiaozhao
 */

package exhi.net.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import exhi.net.interface1.NetCharset;

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

	public static String adapterContentCharset(NetCharset charset) {
		String type = "UTF-8";
    	switch (charset)
    	{
    	case ISO_8859_1:
    		type = "ISO-8859-1";
    		break;
    	case US_ASCII:
    		type = "US-ASCII";
    		break;
    	case UTF_16:
    		type = "UTF-16";
    		break;
    	case UTF_16BE:
    		type = "UTF-16BE";
    		break;
    	case UTF_16LE:
    		type = "UTF-16LE";
    		break;
    	case UTF_8:
    		type = "UTF-8";
    		break;
    	}
    	
    	return type;
	}

	public static String getAbsoluteUrl(String root, String path, String uri)
	{
		final String separatorString = String.format("%c", File.separatorChar);

		if (null == uri) {
			uri = "";
		}

		String temp = new String(uri.trim());
		
		// remove '?' and after the request
        if (temp.indexOf('?') >= 0)
        {
        	temp = temp.split("\\?")[0];
        }
        
		// Convert to absolute path.
        temp = String.format("%s%c%s", path.trim(), File.separatorChar, temp);

		// replace '/' or '\\' to File.separatorChar
        temp = temp.replace('/', File.separatorChar);
        temp = temp.replace('\\', File.separatorChar);

        boolean endSeparator = temp.endsWith(separatorString);
        
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(root);
        pathBuilder.append(File.separatorChar);

        // Remove extra characters of File.separatorChar
        String[] pathArray = temp.split('\\'+separatorString);
        for (String str : pathArray) {
        	if (!str.isEmpty()) {
        		pathBuilder.append(str);
        		pathBuilder.append(File.separatorChar);
        	}
        }
        
        if (!endSeparator) {
        	pathBuilder.deleteCharAt(pathBuilder.length()-1);
        }

        return pathBuilder.toString();
	}
	
	public static StringBuilder readText(String path, String charset)
			throws FileNotFoundException, UnsupportedEncodingException, IOException
	{
		StringBuilder text = new StringBuilder();
		
		File inFile = new File(path);
		if (inFile.exists() && inFile.isFile())
		{
			DataInputStream in = null;
			try
			{
				FileInputStream fstream = new FileInputStream(inFile);
				in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in, charset));
				String strLine;
				while ((strLine = br.readLine()) != null)
				{
					text.append(strLine);
				}
			}
			finally
			{
				if (in != null)
				{
					try {
						in.close();
					} catch (IOException e) {
						// skip it
					}
				}
			}
		}
		
		return text;
	}

	public static String getMimeType(String fileUrl) 
    {
    	FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(fileUrl);
    	return type;
    }
	
	/**
     * Compute MD5 value
     * @param string input value
     * @return return MD5 value
     */
    public static String computeMd5(String string) {
		byte[] data = string.getBytes();
		MessageDigest md = null;
		boolean result = false;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(data, 0, data.length);
			data = md.digest();
			result = true;
		} catch (NoSuchAlgorithmException e) {
			
		}

		StringBuilder sb = new StringBuilder();
		if (result == true)
		{
			for (byte b:data)
			{
				sb.append(String.format("%02X", b));
			}
		}

		return sb.toString();
	}
    
    /**
     * Attemp to create an URL directly
     * @param basePath The base path
     * @param fileName The file name
     * @return return URL
     */
    URL locateFromURL(String basePath, String fileName) {
    	try
        {
            URL url;
            if (basePath == null)
            {
                return new URL(fileName);
                //url = new URL(name);
            }
            else
            {
                URL baseURL = new URL(basePath);
                url = new URL(baseURL, fileName);

                // check if the file exists
                InputStream in = null;
                try
                {
                    in = url.openStream();
                }
                finally
                {
                    if (in != null)
                    {
                        in.close();
                    }
                }
                return url;
            }
        }
        catch (IOException e)
        {
            return null;
        }
    }
}
