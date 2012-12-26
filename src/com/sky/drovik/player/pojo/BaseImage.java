package com.sky.drovik.player.pojo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.drovik.utils.StringUtils;

public class BaseImage {

	private int id;
	
	private String name;
	
	private String thumbnail;
	
	private String src;
	
	private String desc;

	public BaseImage() {
		super();
	}

	public BaseImage(int id, String name, String thumbnail, String src, String desc) {
		super();
		this.id = id;
		this.name = name;
		this.thumbnail = thumbnail;
		this.src = src;
		this.desc = desc;
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

	@Override
	public String toString() {
		return "Image [id=" + id + ", name=" + name + ", thumbnail="
				+ thumbnail + ", src=" + src + ", desc=" + desc + "]";
	}
	

	public static List<BaseImage> parse(InputStream inputStream) throws IOException {
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
							image.setName(xmlPullParser.nextText());
						} else if(tag.equalsIgnoreCase("thumbnail")) {
							image.setThumbnail(xmlPullParser.nextText());
						} else if(tag.equalsIgnoreCase("src")) {
							image.setSrc(xmlPullParser.nextText());
						} else if(tag.equalsIgnoreCase("desc")) {
							image.setDesc(xmlPullParser.nextText());
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
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imageList;
	}
}
