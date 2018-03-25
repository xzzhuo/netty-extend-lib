package exhi.net.netty;

public class CommandResult {
	enum ResultType {
		TEXT,
		FILE
	}
	
	private ResultType mType = null;
	private StringBuilder mValue = null;

	CommandResult() {
		this.setType(ResultType.TEXT);
		this.mValue = new StringBuilder();
	}

	CommandResult(ResultType type, StringBuilder value) {
		this.setType(type);
		this.mValue = value;
	}
	
	CommandResult(ResultType type, String value) {
		this.setType(type);
		this.mValue = new StringBuilder();
		this.mValue.append(value);
	}

	public ResultType getType() {
		return mType;
	}

	public void setType(ResultType type) {
		this.mType = type;
	}

	public StringBuilder getValue() {
		return this.mValue;
	}

	public void setValue(StringBuilder value) {
		this.mValue = value;
	}
}
