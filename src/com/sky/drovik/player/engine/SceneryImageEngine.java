package com.sky.drovik.player.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

import com.drovik.utils.StringUtils;
import com.sky.drovik.player.exception.AppException;
import com.sky.drovik.player.pojo.BaseImage;

public class SceneryImageEngine extends ImageEngine {

	
	public SceneryImageEngine() {
		
	}
	
	@Override
	public List<BaseImage> fetchImage(String desc, int catalog) throws AppException {
		return parse(http_get(desc, catalog));
	}

	public List<BaseImage> parse(InputStream inputStream) throws AppException {
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
						image = new BeautyImage();
					}else if(image != null) {
						if(tag.equalsIgnoreCase("id")) {
							image.setId(StringUtils.toInt(xmlPullParser.nextText(),0));
							//TODO
							// del
							image.setId(1);
						} else if(tag.equalsIgnoreCase("name")) {
							image.setName(xmlPullParser.nextText().trim());
						} else if(tag.equalsIgnoreCase("thumbnail")) {
							image.setThumbnail(xmlPullParser.nextText().trim());
						} else if(tag.equalsIgnoreCase("src")) {
							image.setSrc(xmlPullParser.nextText().trim());
						} else if(tag.equalsIgnoreCase("desc")) {
							String desc = xmlPullParser.nextText().trim();
							if(desc.length() == 0) {
								image.setDesc("���޼��");
							} else {
								image.setDesc(desc);
							}
							if(image.getId()>=0) {
								imageList.add(image);
							}
						}else if(tag.equalsIgnoreCase("channel")) {
							image.setChannel(xmlPullParser.nextText().trim());
						}else if(tag.equalsIgnoreCase("star")) {
							try {
								image.setStarLevel(Integer.parseInt(xmlPullParser.nextText().trim()));
							} catch (Exception e) {
								image.setStarLevel(5);
							}
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
					e.printStackTrace();
				}			}
		}
		return imageList;
	}
}
