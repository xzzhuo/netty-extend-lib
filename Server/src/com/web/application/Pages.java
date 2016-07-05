package com.web.application;

public class Pages {
	
	private long rows = 0;
	private long offsets = 10;	// 
	
	private long count = 0;		// total pages
	private long current = 1;	// current pages
	
	public long getRows() {
		return rows;
	}
	public void setRows(long rows) {
		this.rows = rows;
	}
	public long getOffsets() {
		return offsets;
	}
	public void setOffsets(long offsets) {
		this.offsets = offsets;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public long getCurrent() {
		return current;
	}
	public void setCurrent(long current) {
		this.current = current;
	}
}
