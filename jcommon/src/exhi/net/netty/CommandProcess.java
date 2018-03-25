package exhi.net.netty;

import exhi.net.log.BFCLog;
import exhi.net.netty.NettyResult.ReturnType;

public abstract class CommandProcess extends NetProcess {

	private String mCommand;
	private CommandResult mCommandResult = new CommandResult();

	private void setCommand(String command) {
		this.mCommand = command;
	}

	/**
	 * Get the URI
	 * @return Return current URI
	 */
	protected String getCommand() {
		return mCommand;
	}

	private CommandResult getCommandResult() {
		return mCommandResult;
	}

	/**
	 * Response the text value
	 * @param value The text value
	 */
	protected void print(String value)
	{
		mCommandResult.setType(CommandResult.ResultType.TEXT);
		mCommandResult.setValue(new StringBuilder(value));
	}
	
	protected void setResponseFile(String path) {
		mCommandResult.setType(CommandResult.ResultType.FILE);
		mCommandResult.setValue(new StringBuilder(path));
	}

	/**
	 * Inner process
	 * @param client client address
	 * @param uri request uri
	 * @param cookies cookie values
	 * @param files upload files
	 * @param request request parameters
	 * @param charset charset
	 * @return return the NettyResult object
	 */
	final NettyResult innerCommandProcess(final ProcessAdapter processAdapter)
	{
		BFCLog.debug(processAdapter.getClient(), "Enter innerCommandProcess - Process()");

		this.setCommand(processAdapter.getUri());
		
		super.innerProcess(processAdapter);

		NettyResult result = new NettyResult();

		// start handle for command process
		this.onProcess(processAdapter.getClient(), processAdapter.getUri(), processAdapter.getRequest());
		CommandResult commandResult = this.getCommandResult();
		
		if (commandResult.getType() == CommandResult.ResultType.TEXT)
		{
			result.setText(commandResult.getValue());
			result.setMimeType("text/html");
			result.setReturnType(ReturnType.TEXT);
		} else if (commandResult.getType() == CommandResult.ResultType.FILE) {
			result.setText(commandResult.getValue());
			result.setReturnType(ReturnType.FILE);
		}
		
		BFCLog.debug(processAdapter.getClient(), "Leave innerCommandProcess - Process()");
		
		return result;
	}
}
