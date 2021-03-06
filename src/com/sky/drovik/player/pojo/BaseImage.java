package com.sky.drovik.player.pojo;

import android.graphics.Bitmap;

public abstract class BaseImage {

	public static final int CATALOG_LOCAL = 0;
	
	public static final int CATALOG_BEAUTY = 1;
	
	public static final int CATALOG_SCENERY = 2;
	
	public static final int CATALOG_OTHER = 3;
	
	public static final String TYPE_LOCAL = "local";
	
	public static final String TYPE_BEAUTY = "beauty";
	
	public static final String TYPE_SCENERY = "scenery";
	
	public static final String TYPE_OTHER = "other";
	
	
	private int id;
	
	private String name;
	
	private String thumbnail;
	
	private String src;
	
	private String desc;

	private Bitmap thum;
	
	private String channel;
	
	private int starLevel;
	
	public BaseImage() {
		super();
	}

	
	public int getId() {
		return id;
	}



	public void setId(int id) {
		this.id = id;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getThumbnail() {
		return thumbnail;
	}



	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}



	public String getSrc() {
		return src;
	}



	public void setSrc(String src) {
		this.src = src;
	}



	public String getDesc() {
		return desc;
	}



	public void setDesc(String desc) {
		this.desc = desc;
	}



	public Bitmap getThum() {
		return thum;
	}



	public void setThum(Bitmap thum) {
		this.thum = thum;
	}



	public String getChannel() {
		return channel;
	}



	public void setChannel(String channel) {
		this.channel = channel;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		return result;
	}

	
	public int getStarLevel() {
		return starLevel;
	}

	public void setStarLevel(int starLevel) {
		this.starLevel = starLevel;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseImage other = (BaseImage) obj;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src)){
			return false;
		} else if(name == null) {
			if(other.name != null)
				return false;
		} else if(!name.equals(other.name)) {
			return false;
		}
		return true;
	}



	@Override
	public String toString() {
		return "BaseImage [id=" + id + ", name=" + name + ", thumbnail="
				+ thumbnail + ", src=" + src + ", desc=" + desc + ", thum="
				+ thum + ", channel=" + channel + "]";
	}

}
