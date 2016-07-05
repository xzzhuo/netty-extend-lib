package exhi.net.utils;

public class TransferException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2562117214100877078L;

	public TransferException(String message)
	{
		super(message);
	}
	
	public TransferException(String message, Throwable cause) {
        super(message, cause);
    }
}
