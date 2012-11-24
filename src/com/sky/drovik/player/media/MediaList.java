package com.sky.drovik.player.media;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.sky.drovik.player.pojo.MovieInfo;

public class MediaList {

	String[] thumbColumns = new String[] { 
			MediaStore.Video.Thumbnails.DATA,
			MediaStore.Video.Thumbnails.VIDEO_ID };

	String[] mediaColumns = new String[] { 
			MediaStore.Video.Media._ID, 
			MediaStore.Video.Media.DATA,
			MediaStore.Video.Media.SIZE,
			MediaStore.Video.Media.MIME_TYPE, 
			MediaStore.Video.Media.TITLE
			};
	
	private Cursor cursor;
	
	private Context context;
	
	public MediaList(Context context) {
		this.context = context;
	}
	
	public List<MovieInfo> getVideoListByPage(int page) {
		
		ContentResolver crs =  context.getContentResolver();
		cursor = crs.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Video.Media.DATE_ADDED + " DESC");
		List<MovieInfo> videoList = new ArrayList<MovieInfo>();
		if(cursor.moveToFirst()){
			do{
				MovieInfo info = new MovieInfo();
				info.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
				info.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
				info.size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
				//��ȡ��ǰVideo��Ӧ��Id��Ȼ����ݸ�ID��ȡ��Thumb
				int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
				String selection = MediaStore.Video.Thumbnails.VIDEO_ID +"=?";
				String[] selectionArgs = new String[]{
						id+""
				};
				ContentResolver rs =  context.getContentResolver();
				Cursor thumbCursor = rs.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);
				if(thumbCursor.moveToFirst()){
					info.thumbnailPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
					System.out.println(info.thumbnailPath);
				}
				Uri uri = Uri.parse("file://" + cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
				info.setActivity(uri, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				videoList.add(info);
				thumbCursor.close();
			}while(cursor.moveToNext());
			cursor.close();
		}
		return videoList;
	}
}
