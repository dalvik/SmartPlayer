package com.sky.drovik.player.pojo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.graphics.Bitmap;
import android.util.Xml;

import com.drovik.utils.StringUtils;
import com.sky.drovik.player.exception.AppException;

public class BaseImage {

	public static final int CATALOG_BEAUTY = 0;
	
	public static final int CATALOG_SCENERY = 1;
	
	public static final int CATALOG_OTHER = 2;
	
	public static final String TYPE_BEAUTY = "beauty";
	
	public static final String TYPE_SCENERY = "scenery";
	
	public static final String TYPE_OTHER = "other";
	
	
	private int id;
	
	private String name;
	
	private String thumbnail;
	
	private String src;
	
	private String desc;

	private Bitmap thum;
	
	public BaseImage() {
		super();
	}

	public BaseImage(int id, String name, String thumbnail, String src,
			String desc, Bitmap thum) {
		super();
		this.id = id;
		this.name = name;
		this.thumbnail = thumbnail;
		this.src = src;
		this.desc = desc;
		this.thum = thum;
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

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		return result;
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
		} else if (!src.equals(other.src))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Image [id=" + id + ", name=" + name + ", thumbnail="
				+ thumbnail + ", src=" + src + ", desc=" + desc + "]";
	}
	

	public static List<BaseImage> parse(InputStream inputStream) throws AppException {
		List<BaseImage> imageList = new ArrayList<BaseImage>();
		BaseImage image = null;
		XmlPullParser xmlPullParser = Xml.newPullParser();
		try {
			xmlPullParser.setInput(inputStream, "utf-8");
			xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			int eventType = xmlPullParser.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT) {
				switch(eventType) {
				case XmlPullParser.START_TAG:
					String tag = xmlPullParser.getName();
					if(tag.equalsIgnoreCase("beauty")) {
						image = new BaseImage();
					}else if(image != null) {
						if(tag.equalsIgnoreCase("id")) {
							image.setId(StringUtils.toInt(xmlPullParser.nextText(),0));
						} else if(tag.equalsIgnoreCase("name")) {
							image.setName(xmlPullParser.nextText().trim());
						} else if(tag.equalsIgnoreCase("thumbnail")) {
							image.setThumbnail(xmlPullParser.nextText().trim());
						} else if(tag.equalsIgnoreCase("src")) {
							image.setSrc(xmlPullParser.nextText().trim());
						} else if(tag.equalsIgnoreCase("desc")) {
							image.setDesc(xmlPullParser.nextText().trim());
							imageList.add(image);
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
					default:
						break;
				}
				eventType = xmlPullParser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(inputStream != null) {
				try {
					inputStream.close();
					inputStream = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			}
		}
		return imageList;
	}
}
