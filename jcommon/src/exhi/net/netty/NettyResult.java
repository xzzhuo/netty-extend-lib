/**
 * Author: xiaozhao
 */

package exhi.net.netty;

public class NettyResult {

	public enum ReturnType
	{
		ERR_NOT_FOUND,
		ERR_NOT_IMPLEMENT_404_CALLBACK,
		
		LOCATION,
		FILE,
		TEXT
	}
	
	private ReturnType mFileType = ReturnType.TEXT;
	private StringBuilder mText = null;
	private String mimeType = null;
	
	public void setReturnType(ReturnType type)
	{
		this.mFileType = type;
	}
	
	public ReturnType getReturnType()
	{
		return this.mFileType;
	}
	
	public void setFilePath(String path)
	{
		this.mText = new StringBuilder();
		this.mText.append(path);
	}
	
	public String getFilePath()
	{
		return this.mText.toString();
	}
	
	public void setText(StringBuilder text)
	{
		this.mText = text;
	}
	
	public StringBuilder getText()
	{
		return this.mText;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
