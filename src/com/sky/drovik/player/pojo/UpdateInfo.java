package com.sky.drovik.player.pojo;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.drovik.utils.StringUtils;

public class UpdateInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int versionCode;
	
	private String versionName;
	
	private String downloadUrl;
	
	private String updateLog;
	
	private String packageSize;

	public UpdateInfo() {
		super();
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getUpdateLog() {
		return updateLog;
	}

	public void setUpdateLog(String updateLog) {
		this.updateLog = updateLog;
	}

	
	public String getPackageSize() {
		return packageSize;
	}

	public void setPackageSize(String packageSize) {
		this.packageSize = packageSize;
	}

	
	@Override
	public String toString() {
		return "UpdateInfo [versionCode=" + versionCode + ", versionName="
				+ versionName + ", downloadUrl=" + downloadUrl + ", updateLog="
				+ updateLog + ", packageSize=" + packageSize + "]";
	}

	public static UpdateInfo parse(InputStream inputStream) throws IOException {
		UpdateInfo updateInfo = null;
		XmlPullParser xmlPullParser = Xml.newPullParser();
		try {
			xmlPullParser.setInput(inputStream, "utf-8");
			xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			int eventType = xmlPullParser.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT) {
				switch(eventType) {
				case XmlPullParser.START_TAG:
					String tag = xmlPullParser.getName();
					if(tag.equalsIgnoreCase("android")) {
						updateInfo = new UpdateInfo();
					}else if(updateInfo != null) {
						if(tag.equalsIgnoreCase("versionCode")) {
							updateInfo.setVersionCode(StringUtils.toInt(xmlPullParser.nextText(),0));
						} else if(tag.equalsIgnoreCase("versionName")) {
							updateInfo.setVersionName(xmlPullParser.nextText());
						} else if(tag.equalsIgnoreCase("downloadUrl")) {
							updateInfo.setDownloadUrl(xmlPullParser.nextText());
						} else if(tag.equalsIgnoreCase("updateLog")) {
							updateInfo.setUpdateLog(xmlPullParser.nextText());
						}else if(tag.equalsIgnoreCase("packageSize")) {
							updateInfo.setPackageSize(xmlPullParser.nextText());
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
		return updateInfo;
	}

}
