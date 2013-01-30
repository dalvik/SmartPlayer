package com.sky.drovik.player.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.sky.drovik.player.AppContext;
import com.sky.drovik.player.BuildConfig;
import com.sky.drovik.player.R;
import com.sky.drovik.player.engine.BeautyImage;
import com.sky.drovik.player.engine.UpdateManager;
import com.sky.drovik.player.media.Main;
import com.sky.drovik.player.pojo.BaseImage;
import com.sky.drovik.player.pojo.UpdateInfo;
import com.sky.drovik.player.utils.FileUtil;

public class SmartPlayerService extends Service {

	private String TAG = "SmartPlayerService";
	
	private ConnectivityManager connectivityManager;
	
	private NotificationManager notificationManager;
	
	private Thread checkVersionThread;
	
	private final int new_update_version = 1;
	
	private final int new_images = 2;
	
	private String currentVersion = "";
	
	private String newVersion = "";
	
	private String packageSize = "512KB";
	
	private BroadcastReceiver connectionReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mobNetInfo.isConnected() || wifiNetInfo.isConnected()) {//update
				Log.i(TAG, "connect");
				if(checkVersionThread == null || !checkVersionThread.isAlive()) {
					checkVersionThread = new Thread(new CheckVersion());
					checkVersionThread.start();
				}
			} else {
				Log.i(TAG, "unconnect");
			}

		}

	};
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what) {
			case new_update_version:
				Intent intent = new Intent(SmartPlayerService.this, Main.class);
				intent.putExtra("check_update", true);
				ComponentName name = new ComponentName("com.sky.drovik.player", "com.sky.drovik.player.media.Main");
				intent.setComponent(name);
				intent.setAction("android.intent.action.MAIN");
				intent.addCategory("android.intent.category.LAUNCHER");
		        // 设置点击通知时显示内容的类   
				PendingIntent pendIntent = PendingIntent.getActivity(SmartPlayerService.this, 0, intent, 0);   
				Notification notification = new Notification();   
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				notification.icon = R.drawable.ic_launcher; // 设置在状态栏显示的图标   
			    notification.tickerText = getText(R.string._need_update_new_version_str); // 设置在状态栏显示的内容   
			    notification.when = System.currentTimeMillis();
			    // 设置通知显示的参数   
			    notification.setLatestEventInfo(SmartPlayerService.this, getText(R.string._need_update_new_version_str), getString(R.string._can_update_version_str, currentVersion, newVersion, packageSize), pendIntent);
			    notificationManager.notify(0, notification); // 执行通知. 
				break;
			case new_images:
				Intent intent2 = new Intent(SmartPlayerService.this, Main.class);
				intent2.putExtra("check_update", false);
				ComponentName name2 = new ComponentName("com.sky.drovik.player", "com.sky.drovik.player.media.Main");
				intent2.setComponent(name2);
				intent2.setAction("android.intent.action.MAIN");
				intent2.addCategory("android.intent.category.LAUNCHER");
		        // 设置点击通知时显示内容的类   
				PendingIntent pendIntent2 = PendingIntent.getActivity(SmartPlayerService.this, 0, intent2, 0);   
				Notification notification2 = new Notification();   
				notification2.flags = Notification.FLAG_AUTO_CANCEL;
				notification2.icon = R.drawable.ic_launcher; // 设置在状态栏显示的图标   
			    notification2.tickerText = getText(R.string._has_new_images_str); // 设置在状态栏显示的内容
			    notification2.when = System.currentTimeMillis();
			    // 设置通知显示的参数   
			    notification2.setLatestEventInfo(SmartPlayerService.this, getText(R.string._has_new_images_str), getString(R.string._view_new_images_str, msg.obj, msg.arg1), pendIntent2);
			    notificationManager.notify(1, notification2); // 执行通知.
			    break;
				default:
					break;
			}
		};
		
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(connectionReceiver, intentFilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(connectionReceiver != null) {
			unregisterReceiver(connectionReceiver);
		}
	}

	private class CheckVersion implements Runnable {
		
		public CheckVersion() {
			
		}
		
		public void run() {
			Looper.prepare();
			try {
				
				AppContext appContext = (AppContext)getApplication();
				List<BaseImage> beautyImageListTmp = appContext.getBeautyImageList(0, -1, true);
				SharedPreferences photoInfo = appContext.getSharedPreferences(BeautyImage.class.getName(), Context.MODE_PRIVATE);
				int updateImageNum = 0;
				String updateImageName = "";
				for(BaseImage baseImage:beautyImageListTmp) {
					BeautyImage beautyImage = (BeautyImage) baseImage;
					int old = photoInfo.getInt(baseImage.getName() + "_photo_number", 0);
					if(beautyImage.getSrcArr().length>old) {
						updateImageNum++;
						updateImageName = baseImage.getName();
					}
				}
				if(updateImageNum>0) {
					Message msg = new Message();
					msg.what = new_images;
					msg.arg1 = updateImageNum;
					msg.obj = updateImageName;
					handler.sendMessage(msg);
				}
				
				UpdateManager manager =  UpdateManager.getUpdateManager();
				UpdateInfo updateInfo = manager.checkVersion();
				PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				if(updateInfo != null && updateInfo.getVersionCode()>packageInfo.versionCode) {
					String apkName = getString(R.string.app_name) + "_"+updateInfo.getVersionName()+".apk";
					//判断是否挂载了SD卡
					String storageState = Environment.getExternalStorageState();		
					if(storageState.equals(Environment.MEDIA_MOUNTED)){
						String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + FileUtil.parentPath + "update" + File.separator;
						File file = new File(savePath);
						if(BuildConfig.DEBUG) {
							Log.d(TAG, "### save path = " + savePath);
						}
						if(!file.exists()){
							file.mkdirs();
						}else {
							File[] allFile = file.listFiles();
							for(File tmp:allFile) {
								if(tmp.getName().toLowerCase().endsWith("apk")) {
									if(!tmp.getName().equalsIgnoreCase(apkName)) {
										tmp.delete();
									}
								}
							}
						}
						String apkFilePath = savePath + apkName;
						File ApkFile = new File(apkFilePath);
						//是否已下载更新文件
						if(!ApkFile.exists()){
							FileOutputStream fos = new FileOutputStream(ApkFile);
							URL url = new URL(updateInfo.getDownloadUrl());
							HttpURLConnection conn = (HttpURLConnection)url.openConnection();
							conn.connect();
							InputStream is = conn.getInputStream();
							byte buf[] = new byte[1024];
							do{   		   		
					    		int numread = is.read(buf);
					    		if(numread <= 0){	
					    			//下载完成通知安装
					    			break;
					    		}
					    		fos.write(buf,0,numread);
					    		fos.flush();
					    	}while(true);//点击取消就停止下载
							fos.close();
							is.close();
						}
					}
					
					Message msg = new Message();
					msg.what = new_update_version;
					currentVersion = packageInfo.versionName;
					newVersion = updateInfo.getVersionName();
					packageSize = updateInfo.getPackageSize();
					handler.sendEmptyMessage(new_update_version);
				}
			} catch (Exception e) {
				//e.printStackTrace();
				Log.d(TAG, "### CheckVersion = " + e.getLocalizedMessage());
			}
		}
	}
}
