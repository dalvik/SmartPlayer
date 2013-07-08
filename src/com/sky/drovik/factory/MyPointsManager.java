package com.sky.drovik.factory;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.sky.drovik.player.media.MovieList;

public class MyPointsManager {

	//private static final String KEY_FILE_POINTS="Points";
	public static final String KEY_POINTS="BEYING";
	private static final String KEY_FILE_ORDERS="Orders";
	

	private static MyPointsManager instance;
	
	private String TAG = "MyPointsManager";
	
	private boolean DEBUG = true;

	public static MyPointsManager getInstance() {
		if (instance == null) {
			instance = new MyPointsManager();
		}

		return instance;
	}


	/**
	 * 查询积分
	 * @param context
	 * @return
	 */
	public int queryPoints(Context context) {
		SharedPreferences sp = context.getSharedPreferences(MovieList.class.getName(), Context.MODE_PRIVATE);
		return sp.getInt(KEY_POINTS, 0);
	}

	public int queryPoints(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(MovieList.class.getName(), Context.MODE_PRIVATE);
		return sp.getInt(KEY_POINTS, 0);
	}
	
	/**
	 * 消费积分
	 * 
	 * @param context
	 * @param amount
	 * @return
	 */
	public boolean spendPoints(Context context, int amount) {
		if (amount <= 0) {
			return false;
		}
		SharedPreferences sp = context.getSharedPreferences(MovieList.class.getName(), Context.MODE_PRIVATE);
		int p = sp.getInt(KEY_POINTS, 0);
		if (p < amount) {
			return false;
		}
		p -= amount;
		return sp.edit().putInt(KEY_POINTS, p).commit();
	}

	/**
	 * 奖励积分
	 * @param context
	 * @param amount
	 * @return
	 */
	public boolean awardPoints(Context context, int amount) {
		if (amount <= 0) {
			return false;
		}
		SharedPreferences sp = context.getSharedPreferences(MovieList.class.getName(), Context.MODE_PRIVATE);
		int p = sp.getInt(KEY_POINTS, 0);

		p += amount;

		return sp.edit().putInt(KEY_POINTS, p).commit();
	}




	private void errMsg(String msg) {
		Log.e("MyPointsManager", msg);
	}

	private void infoMsg(String msg) {
		Log.e("MyPointsManager", msg);
	}

}
