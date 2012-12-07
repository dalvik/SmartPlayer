package com.sky.drovik.player.pojo;

public class HisInfo {

	private long viewTime;
	
	private String name;
	
	private String path;

	public HisInfo() {
		super();
	}

	public HisInfo(long viewTime, String name, String path) {
		super();
		this.viewTime = viewTime;
		this.name = name;
		this.path = path;
	}

	
	public long getViewTime() {
		return viewTime;
	}


	public void setViewTime(long viewTime) {
		this.viewTime = viewTime;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	@Override
	public String toString() {
		return "HisInfo [viewTime=" + viewTime + ", name=" + name + ", path="
				+ path + "]";
	}
	
	
}
