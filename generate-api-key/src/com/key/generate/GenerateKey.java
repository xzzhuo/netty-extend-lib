package com.key.generate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GenerateKey {

	public static void main(String[] args) {
		
		System.out.println("Input: ('quit' for exit)");
		
		try {
			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			
			String line = console.readLine();
			
			while (!line.equals("quit"))
			{
				String apiKey = generate(line);
				System.out.println("Api Key: " + apiKey);
				line = console.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("exit");
	}

	public static String generate(String appName)
	{
		byte[] ps = {-35, 6, 56, -3, 127, 36, 79, -28, 8, 76};
		
		return computeMd5(String.format("%s%s", appName, getDictionaryString(ps)));
	}
	
	/**
     * Compute MD5 value
     * @param string input value
     * @return return MD5 value
     */
    private static String computeMd5(String string) {
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
    
    private static String getDictionaryString(byte[] index)
    {
    	final char[] DICTIONARY = {
    			'o','l','p','S','6','4','A','c','D','5','D','s','P','7','g','V',
        		'Y','8','v','D','I','W','r','j','1','9','g','j','3','T','d','6',
        		'G','i','Z','0','q','S','E','a','k','h','b','E','O','K','w','3',
        		'n','u','s','y','C','Q','c','6','9','F','m','p','R','n','h','f',
        		'm','Q','b','O','Y','0','3','x','I','G','T','A','4','U','7','F',
        		'W','p','y','t','8','R','z','L','v','C','J','x','a','I','l','V',
        		'H','r','5','d','q','K','f','P','X','H','o','B','X','C','L','M',
        		'T','N','e','Z','2','w','e','U','2','N','M','1','W','T','D','9'
    	};
    	
    	StringBuilder sb = new StringBuilder();
    	for (byte i : index)
    	{
    		if (i<0)
    		{
    			i += 128;
    		}
    		sb.append(DICTIONARY[i]);
    	}
    	
    	String value = sb.toString();
    	
    	return value;
    }
}
