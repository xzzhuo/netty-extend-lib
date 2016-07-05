package exhi.net.utils;

public class ExcelException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5920241755218461844L;

	public ExcelException(String message)
	{
		super(message);
	}
	
	public ExcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
