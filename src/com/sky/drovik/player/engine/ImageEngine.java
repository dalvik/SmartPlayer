package com.sky.drovik.player.engine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import android.util.Log;

import com.drovik.utils.URLs;
import com.sky.drovik.player.BuildConfig;
import com.sky.drovik.player.exception.AppException;
import com.sky.drovik.player.pojo.BaseImage;

public abstract class ImageEngine {
	
	private final static int TIMEOUT_CONNECTION = 20000;
	
	private final static int TIMEOUT_SOCKET = 20000;
	
	public static final String UTF_8 = "UTF-8";
	
	private final static int RETRY_TIME = 3;
	
	private String TAG = "ImageEngine";
	
	private boolean DEBUG = true;
	
	public ImageEngine() {
		
	}
	
	public abstract List<BaseImage> fetchImage(String desc) throws AppException;
	
	
	public InputStream http_get(String url) throws AppException {
		if(BuildConfig.DEBUG && DEBUG) {
			Log.d(TAG, "### fetch url = " + url);
		}
		HttpClient httpClient = null;
		GetMethod httpGet = null;
		String responseBody = "";
		int time = 0;
		do {
			try {
				httpClient = getHttpClient();
				httpGet = getHttpGet(url, "", "");
				int statusCode = httpClient.executeMethod(httpGet);
				if(statusCode != HttpStatus.SC_OK) {
					throw AppException.io(new IOException());
				}
				responseBody = httpGet.getResponseBodyAsString();
				break;
			} catch (Exception e) {
				time++;
				if(time <RETRY_TIME) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			} finally {
				if(httpGet != null) {
					httpGet.releaseConnection();
					httpGet = null;
				}
			}
		}while(time<RETRY_TIME);
		if(BuildConfig.DEBUG && DEBUG) {
			Log.d(TAG, "### responseBody = " + responseBody);
		}
		//responseBody = responseBody.replace('', '?');
		//if(responseBody.contains("result") && responseBody.contains("errorCode")) {
		//}
		return new ByteArrayInputStream(responseBody.getBytes());
	}
	
	private GetMethod getHttpGet(String url, String cookie, String userAgent) {
		GetMethod httpGetMethod = new GetMethod(url);
		httpGetMethod.getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpGetMethod.setRequestHeader("Host",URLs.HOST);
		httpGetMethod.setRequestHeader("Connection","Keep-Alive");
		httpGetMethod.setRequestHeader("Cookie",cookie);
		httpGetMethod.setRequestHeader("User-Agent",userAgent);
		return httpGetMethod;
	}
	
	private HttpClient getHttpClient() {
		HttpClient httpClient = new HttpClient();
		// 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
		httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		// 设置 默认的超时重试处理策略
		httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		// 设置 连接超时时间
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(TIMEOUT_CONNECTION);
		// 设置 读数据超时时间
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(TIMEOUT_SOCKET);
		httpClient.getParams().setContentCharset(UTF_8);
		return httpClient;
	}
}
