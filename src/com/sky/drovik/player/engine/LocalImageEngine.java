package com.sky.drovik.player.engine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Xml;

import com.drovik.utils.StringUtils;
import com.sky.drovik.player.exception.AppException;
import com.sky.drovik.player.pojo.BaseImage;
import com.sky.drovik.player.pojo.MovieInfo;

public class LocalImageEngine extends ImageEngine {

	private SharedPreferences photoInfo = null;
	
	private String[] thumbColumns = new String[] { 
			MediaStore.Images.Thumbnails.DATA,
			MediaStore.Images.Thumbnails.IMAGE_ID };

	private String[] mediaColumns = new String[] { 
			MediaStore.Images.Media._ID, 
			MediaStore.Images.Media.DATA,
			MediaStore.Images.Media.TITLE
			};
	
	private Context context;
	
	private Cursor cursor;
	
	public LocalImageEngine(Context context, SharedPreferences photoInfo) {
		this.photoInfo = photoInfo;
		this.context = context;
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
						if(tag.equalsIgnoreCase("id")) {// id > 0 正常   == 0 审核   < 0   隐藏
							image.setId(StringUtils.toInt(xmlPullParser.nextText(),0));
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
								image.setDesc("暂无简介");
							} else {
								image.setDesc(desc);
							}
							if(image.getId()>=0) {
								imageList.add(image);
							}
						} else if(tag.equalsIgnoreCase("channel")) {
							image.setChannel(xmlPullParser.nextText().trim());
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
	
	public List<BaseImage> loadLocalImage() {
		List<BaseImage> imageList = new ArrayList<BaseImage>();
		ContentResolver crs =  context.getContentResolver();
		cursor = crs.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaColumns, null, null,  MediaStore.Images.Media.DATE_ADDED + " DESC ");// + " LIMIT " + (index * perPageNum) + " , "+ perPageNum);
		System.out.println("cursor = " + cursor);
		if(cursor != null && cursor.moveToFirst()){
			do{
				BaseImage info = new BeautyImage();
				info.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)));
				info.setSrc(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)));
				//获取当前Video对应的Id，然后根据该ID获取其Thumb
				int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
				String selection = MediaStore.Images.Thumbnails.IMAGE_ID +"=?";
				String[] selectionArgs = new String[]{
						id+""
				};
				ContentResolver rs =  context.getContentResolver();
				Cursor thumbCursor = rs.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);
				if(thumbCursor.moveToFirst()){
					info.setId((int)cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID)));
					info.setThumbnail(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA)));
				}
				imageList.add(info);
				thumbCursor.close();
				System.out.println("image info = " + info.toString());
			}while(cursor.moveToNext());
			if(cursor != null) {
				cursor.close();
			}
		}
		return imageList;
	}
}
