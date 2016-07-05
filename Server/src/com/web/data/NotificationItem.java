package com.web.data;

public class NotificationItem {

	private int id;
	private String title;
	private String description = "111";
	private int disable = 1;
	
	private int resever = 0;
	
	public NotificationItem()
	{
		id = 10;
		title = "111";
		description = "111";
		disable = 1;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getDisable() {
		return disable;
	}
	public void setDisable(int disable) {
		this.disable = disable;
	}

	public int getResever() {
		return resever;
	}

	public void setResever(int resever) {
		this.resever = resever;
	}
}
