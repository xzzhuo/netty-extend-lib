/**
 * Author: xiaozhao
 */

package exhi.net.utils;

import java.util.HashMap;

public class FileMimeMap {
	
	public static HashMap<String, String> streamType = new HashMap<String, String>();
    
	static{
        streamType.put("rtf","text/rtf");
        streamType.put("mif","application/vnd.mif");
        streamType.put("ppt","application/vnd.ms-powerpoin");
        streamType.put("gtar","application/x-gtar");
        streamType.put("latex","application/x-latex");
        streamType.put("aif","audio/x-aiff");
        streamType.put("ief","image/ief");
        streamType.put("pgm","image/x-portable-graymap");
        streamType.put("iges","model/iges");
        streamType.put("mpe","text/plain");
        streamType.put("movie","video/x-sgi-movie");
        streamType.put("ez","application/andrew-inset");
        streamType.put("smi","application/smil");
        streamType.put("js","application/x-javascript");
        streamType.put("nc","application/x-netcdf");
        streamType.put("cdf","application/x-netcdf");
        streamType.put("sv4crc","application/x-sv4crc");
        streamType.put("tar","application/x-tar");
        streamType.put("tcl","application/x-tclc");
        streamType.put("roff","application/x-troff");
        streamType.put("ustar","application/x-ustar");
        streamType.put("zip","application/zip");
        streamType.put("snd","audio/basic");
        streamType.put("xyz","chemical/x-pdb");
        streamType.put("jpg","image/jpeg");
        streamType.put("png","image/png");
        streamType.put("tiff","image/tiff");
        streamType.put("tif","image/tiff");
        streamType.put("ppm","image/x-portable-pixmap");
        streamType.put("rgb","image/x-rgb");
        streamType.put("xpm","model/vrml");
        streamType.put("wrl","image/x-xpixmap");
        streamType.put("css","text/css");
        streamType.put("sgml","text/sgml");
        streamType.put("etx","text/x-setext");
        streamType.put("xwd","image/x-xwindowdump");
        streamType.put("ice","x-conference/x-cooltalk");
        streamType.put("htm","text/html");
        streamType.put("doc","application/msword");
        streamType.put("xls","application/vnd.ms-excel");
        streamType.put("oda","application/oda");
        streamType.put("pdf","application/pdf");
        streamType.put("ai","application/postscript");
        streamType.put("eps","application/postscript");
        streamType.put("csh","application/x-csh");
        streamType.put("dxr","application/x-director");
        streamType.put("hdf","application/x-hdf");
        streamType.put("skp","application/x-koan");
        streamType.put("skd","application/x-koan");
        streamType.put("skt","application/x-koan");
        streamType.put("tr","application/x-troff");
        streamType.put("me","application/x-troff-me");
        streamType.put("src","application/x-wais-source");
        streamType.put("au","audio/basic");
        streamType.put("mid","audio/midi");
        streamType.put("kar","audio/midi");
        streamType.put("mp3","audio/mpeg");
        streamType.put("aiff","audio/x-aiff");
        streamType.put("wav","audio/x-wav");
        streamType.put("pdb","chemical/x-pdb");
        streamType.put("gif","image/gif");
        streamType.put("jpeg","image/jpeg");
        streamType.put("ras","image/x-cmu-raster");
        streamType.put("rtx","text/richtext");
        streamType.put("mpg","video/mpeg");
        streamType.put("mov","video/quicktimef");
        streamType.put("avi","video/x-msvideo");
        streamType.put("hqx","application/mac-binhex40");
        streamType.put("cpt","application/mac-compactpro");
        streamType.put("ps","application/postscript");
        streamType.put("smil","application/smil");
        streamType.put("bcpio","application/x-bcpio");
        streamType.put("vcd","application/x-cdlink");
        streamType.put("dcr","application/x-director");
        streamType.put("pgn","application/x-chess-pgn");
        streamType.put("gz","application/zip");
        streamType.put("tgz","application/zip");
        streamType.put("pgn","application/x-chess-pgn");
        streamType.put("midi","audio/midi");
        streamType.put("mpga","audio/mpeg");
        streamType.put("rm","audio/x-pn-realaudio");
        streamType.put("ra","audio/x-realaudio");
        streamType.put("jpe","image/jpeg");
        streamType.put("asc","text/plain");
        streamType.put("xml","text/xml");
        streamType.put("html","text/html");
         
        streamType.put("docx","application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        streamType.put("pptx","application/vnd.openxmlformats-officedocument.presentationml.presentation");
        streamType.put("xlsx","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
         
        streamType.put("mmap","application/vnd.mindjet.mindmanager");
        streamType.put("mmp","application/vnd.mindjet.mindmanager");
        streamType.put("mmpt","application/vnd.mindjet.mindmanager");
        streamType.put("mmat","application/vnd.mindjet.mindmanager");
        streamType.put("mmmp","application/vnd.mindjet.mindmanager");
        streamType.put("mmas","application/vnd.mindjet.mindmanager");
        streamType.put("xmind","application/xmind");
         
        streamType.put("jar","application/java-archive");
    }
    
    public static String getMimeType(String fileUrl) {
    	
    	if (fileUrl == null || fileUrl.isEmpty())
    	{
    		return null;
    	}
    	
    	String ext = null;
    	if (fileUrl.lastIndexOf('.') < 0)
    	{
    		return null;
    	}
    	
    	ext = fileUrl.substring(fileUrl.lastIndexOf('.')+1,fileUrl.length());
    	
        if(streamType.containsKey(ext.toLowerCase())) {
            return (String)streamType.get(ext.toLowerCase());
        }else{
            return "application/octet-stream";
        }
    }
}
