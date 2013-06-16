package com.sky.drovik.player.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.content.SharedPreferences;
import android.util.Xml;

import com.drovik.utils.StringUtils;
import com.sky.drovik.player.exception.AppException;
import com.sky.drovik.player.pojo.BaseImage;

public class BeautyImageEngine extends ImageEngine {

	private SharedPreferences photoInfo = null;
	
	public BeautyImageEngine(SharedPreferences photoInfo) {
		this.photoInfo = photoInfo;
	}
	
	@Override
	public List<BaseImage> fetchImage(String desc, int cataLog) throws AppException {
		return parse(http_get(desc, cataLog));
	}

	public List<BaseImage> parse(InputStream inputStream) throws AppException {
		List<BaseImage> imageList = new ArrayList<BaseImage>();
		BeautyImage image = null;
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
						if(tag.equalsIgnoreCase("id")) {// id > 0 Õý³£   == 0 ÉóºË   < 0   Òþ²Ø
							image.setId(StringUtils.toInt(xmlPullParser.nextText(),0));
							//TODO
							// del
							image.setId(1);
						} else if(tag.equalsIgnoreCase("name")) {
							image.setName(xmlPullParser.nextText().trim());
						} else if(tag.equalsIgnoreCase("thumbnail")) {
							image.setThumbnail(xmlPullParser.nextText().trim());
						} else if(tag.equalsIgnoreCase("src")) {
							image.setSrcArr(xmlPullParser.nextText().trim().split(";"));
							int len = image.getSrcArr().length;
							image.setSrcSize(len);
							if(photoInfo != null) {
								int old = photoInfo.getInt(image.getName() + "_photo_number", 0);
								if(len == old) {
									image.setHasNew(false);
								} else {
									image.setNewImageSize(len-old>0 ? len-old : len);
									image.setHasNew(true);
									photoInfo.edit().putInt(image.getName() + "_photo_number", len).commit();
								}
							}
						} else if(tag.equalsIgnoreCase("desc")) {
							String desc = xmlPullParser.nextText().trim();
							if(desc.length() == 0) {
								image.setDesc("ÔÝÎÞ¼ò½é");
							} else {
								image.setDesc(desc);
							}
							if(image.getId()>=0) {
								imageList.add(image);
							}
						} else if(tag.equalsIgnoreCase("channel")) {
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
